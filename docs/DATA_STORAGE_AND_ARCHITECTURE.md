# RoutineTrack - Salvataggio dati e architettura

## Introduzione

RoutineTrack salva abitudini, completamenti giornalieri e sessione utente in locale. Questo permette all'app di mantenere i dati anche dopo la chiusura dell'app e di continuare a funzionare quando il backend non e disponibile. La UI legge sempre dati osservabili dal database locale, mentre la sincronizzazione remota viene gestita dal livello Repository.

## Perche Room

Android usa SQLite come database locale. Room e una libreria Jetpack che semplifica l'accesso a SQLite usando classi Kotlin, annotazioni e DAO. Rispetto a scrivere query SQLite manuali, Room riduce boilerplate, controlla molte query a compile-time e si integra bene con Flow e coroutine.

Nel progetto Room e utile per tre motivi:

- persistenza locale delle abitudini;
- aggiornamento automatico della UI quando cambiano i dati;
- base offline-first pronta per sincronizzazione con Retrofit/backend.

## Entity

Una Entity rappresenta una tabella del database.

`HabitEntity` rappresenta la tabella `habits`. Contiene titolo, descrizione, categoria, colore, tipo di habit, target numerico, unita, frequenza, reminder, date di creazione/aggiornamento e stato di sincronizzazione.

`HabitCompletionEntity` rappresenta la tabella `habit_completions`. Contiene il riferimento alla habit, la data, il valore completato, lo stato `completed` e lo stato di sync.

`UserEntity` rappresenta la sessione utente locale. Contiene email, display name, id remoto, token opzionale e flag di login.

Le Entity sono separate dai modelli domain per non legare la logica dell'app alla forma fisica del database.

## DAO

DAO significa Data Access Object. Un DAO contiene le query per leggere e modificare una tabella.

`HabitDao` contiene operazioni CRUD sulle abitudini:

- leggere tutte le habit attive;
- leggere una habit per id;
- inserire;
- aggiornare;
- cancellare logicamente;
- leggere elementi non sincronizzati.

`HabitCompletionDao` gestisce i completamenti:

- completamenti per habit;
- completamenti per data;
- completamenti in un intervallo;
- inserimento o aggiornamento;
- eliminazione;
- elementi non sincronizzati.

I metodi di lettura osservabile restituiscono `Flow`, mentre insert/update/delete sono `suspend` per essere eseguiti dentro coroutine.

## Database

`RoutineTrackDatabase` e il punto centrale di accesso a Room. Dichiara le Entity incluse nel database ed espone i DAO.

Il database usa un Singleton thread-safe:

```text
RoutineTrackDatabase.getDatabase(context)
```

Questo evita di creare più istanze del database durante l'esecuzione dell'app. In fase di sviluppo il progetto usa `fallbackToDestructiveMigration`; in produzione sarebbe meglio creare migration esplicite per non perdere dati quando cambia lo schema.

## Repository

Il Repository separa ViewModel e database. I ViewModel non conoscono `HabitDao`, `HabitCompletionDao` o Retrofit.

`HabitRepository` gestisce:

- lettura delle habit da Room;
- creazione e modifica habit;
- completamento giornaliero;
- progresso numerico;
- cancellazione;
- salvataggio offline-first con `pendingSync`.

`StatsRepository` legge habit e completamenti e calcola statistiche tramite use case. Questo evita di mettere calcoli dentro i composable.

`SyncRepository` gestisce invece la comunicazione Retrofit con il backend TiDB e applica la risposta remota nel database locale.

## ViewModel

Il ViewModel mantiene lo stato della schermata ed espone `StateFlow` alla UI Compose.

Esempio:

```text
HomeViewModel
-> osserva HabitRepository
-> combina habit e completamenti
-> produce HomeUiState
-> HomeScreen osserva lo stato
```

Le operazioni come salvataggio e completamento partono dentro `viewModelScope.launch`, quindi non bloccano il thread principale.

## Flow e StateFlow

`Flow` viene usato per osservare dati che cambiano nel database. Quando una habit viene inserita, aggiornata o completata, Room emette una nuova lista.

`StateFlow` viene usato nei ViewModel per rappresentare lo stato corrente della schermata. Compose osserva questo stato con `collectAsStateWithLifecycle` e ricompone la UI quando cambia.

## Flusso completo di salvataggio

Quando l'utente crea una nuova habit:

```text
Utente compila il form
-> AddHabitScreen
-> AddHabitViewModel
-> HabitRepository.saveHabit()
-> HabitDao.insertHabit()
-> Room Database
-> tabella habits
-> HomeViewModel osserva Flow
-> HomeScreen si aggiorna automaticamente
```

La UI non chiama mai direttamente il DAO. Questo rende il codice più pulito, testabile e coerente con MVVM.

## Flusso completamento habit

Quando l'utente preme "Completa":

```text
Utente preme il bottone sulla HabitCard
-> HomeScreen
-> HomeViewModel.toggleComplete()
-> HabitRepository.setCompletion()
-> HabitCompletionDao.insertOrUpdateCompletion()
-> Room Database
-> tabella habit_completions
-> Calendar e Stats ricevono nuovi dati tramite Flow
```

Per habit numeriche il valore viene aggiornato progressivamente. Quando il valore raggiunge il target, la habit risulta completata.

## Main Thread

Il Main Thread deve restare libero per disegnare la UI e rispondere ai tocchi dell'utente. Operazioni lente come database, rete o file system non devono essere eseguite direttamente nei composable.

RoutineTrack usa:

- `suspend fun` nei DAO;
- coroutine nel `viewModelScope`;
- Flow per osservare il database;
- repository per isolare I/O e logica dati.

Questo evita blocchi dell'interfaccia e rende l'app più fluida.

## Collegamento con backend e Retrofit

La struttura attuale è già pronta per backend remoto. Retrofit è collegato tramite `ApiService` e viene usato dai repository, non dalla UI.

Il comportamento e offline-first:

```text
Scrivo prima in Room
-> imposto pendingSync = true
-> SyncRepository invia le modifiche a TiDB
-> se fallisce, pendingSync resta true
-> la sync manuale o un worker può riprovare
```

In futuro si può migliorare aggiungendo:

- login Google opzionale;
- token JWT reale;
- refresh token;
- sync periodica più robusta.
