# Relazione finale RoutineTrack

## 1. Introduzione e scopo dell'applicazione

RoutineTrack è un'app Android sviluppata in Kotlin con Jetpack Compose per il tracciamento di abitudini personali. L'obiettivo è aiutare l'utente a creare routine giornaliere, registrare i progressi, consultare statistiche e mantenere i dati disponibili anche offline.

L'app usa un modello offline-first: i dati vengono salvati prima nel database locale Room e poi sincronizzati con un backend Flask collegato a TiDB. Ogni utente dispone di un account personale e le routine sono associate al relativo `userId`, così account diversi non condividono le stesse informazioni.

## 2. Requisiti e modalità di esecuzione

Il progetto Android si apre da Android Studio importando la cartella principale `RoutineTrack`. Il modulo da eseguire è `app`.

- Linguaggio: Kotlin.
- UI: Jetpack Compose con Material 3.
- Min SDK: 26.
- Target SDK: 36.
- Persistenza locale: Room.
- Comunicazione HTTP: Retrofit con Moshi.
- Backend: Flask.
- Database cloud: TiDB.
- Reminder: notifiche Android pianificate tramite `AlarmManager`.

La URL del backend è configurabile in `gradle.properties`:

```properties
ROUTINETRACK_API_BASE_URL=https://routinetrack.onrender.com/
```

Per avviare il backend in locale:

```bash
cd backend_tidb
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
python app.py
```

Il file `.env` reale non va consegnato perché contiene credenziali. Per il test locale su emulatore si può usare `http://10.0.2.2:5000/`; su telefono fisico serve l'indirizzo IP locale del PC, oppure un backend online stabile come Render.

## 3. Funzionalità principali

RoutineTrack permette di registrarsi e accedere con email e password. Dopo il login, l'utente può creare abitudini booleane o numeriche, scegliere frequenza, unità, colore, periodo e reminder.

La Home mostra le abitudini del giorno con calendario settimanale, stato di completamento, progressi e streak. Per le abitudini numeriche il tap sul pulsante `+` incrementa di uno, mentre una pressione prolungata di circa due secondi apre un dialog per inserire manualmente il progresso. Se l'unità è temporale, il dialog permette di modificare ore, minuti e secondi.

La schermata statistiche mostra l'andamento mensile e riepiloga completamenti e streak. La sezione account/impostazioni consente di sincronizzare i dati, ripristinarli dal cloud, cambiare tema light/dark e selezionare l'icona dell'app.

## 4. Architettura software

Il progetto segue una struttura ispirata a MVVM:

```text
UI Compose -> ViewModel -> Repository -> DAO/Retrofit -> Room/Backend
```

Le schermate Compose osservano lo stato dei ViewModel, ma non accedono direttamente al database o alla rete. I repository concentrano la logica dati, mentre DAO e `ApiService` separano rispettivamente persistenza locale e chiamate HTTP.

Cartelle principali:

- `ui/screens`: schermate Compose e ViewModel.
- `ui/components`: componenti riutilizzabili.
- `ui/theme`: tema Material 3, palette e tipografia.
- `data/local`: Room database, entity e DAO.
- `data/remote`: Retrofit, DTO e client HTTP.
- `data/repository`: logica dati e sincronizzazione.
- `data/session`: sessione utente locale.
- `notification`: reminder e notifiche.
- `backend_tidb`: web service Flask e schema SQL.

Esempio di stato Compose osservato da ViewModel:

```kotlin
val state = viewModel.uiState.collectAsStateWithLifecycle().value
```

## 5. Persistenza locale con Room

Room gestisce abitudini, completamenti e sessione utente locale. Le entity includono campi di sincronizzazione come `remoteId`, `pendingSync`, `createdAt` e `updatedAt`.

Esempio reale da `HabitEntity.kt`:

```kotlin
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "remote_id")
    val remoteId: String? = null,
    @ColumnInfo(name = "user_id")
    val userId: String = "",
    val title: String,
    @ColumnInfo(name = "pending_sync")
    val pendingSync: Boolean = true
)
```

Le query principali filtrano per utente. Esempio reale da `HabitDao.kt`:

```kotlin
@Query("""
    SELECT * FROM habits
    WHERE user_id = :userId
    AND is_deleted = 0
    ORDER BY created_at DESC
""")
fun observeHabitsForUser(userId: String): Flow<List<HabitEntity>>
```

Nel repository il filtro utente viene applicato prima di esporre i dati alla UI:

```kotlin
fun observeHabits(): Flow<List<Habit>> {
    return userDao.getLoggedUser().flatMapLatest { user ->
        val userId = user?.remoteId ?: return@flatMapLatest flowOf(emptyList())
        habitDao.observeHabitsForUser(userId)
            .map { habits -> habits.map { it.toDomain() } }
    }
}
```

Questa scelta consente ad account diversi di vedere dati separati anche quando usano lo stesso dispositivo.

## 6. Backend e sincronizzazione cloud

Il backend si trova in `backend_tidb/app.py` ed espone endpoint REST essenziali:

```text
GET  /health
POST /auth/register
POST /auth/login
GET  /sync/<user_id>
POST /sync/<user_id>
```

Lo schema SQL crea le tabelle `users`, `habits`, `habit_completions` e `sync_state`. Le abitudini e i completamenti hanno chiavi esterne verso `users`, così ogni record cloud rimane associato al proprietario.

Retrofit espone le API in `ApiService.kt`:

```kotlin
@POST("auth/login")
suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

@POST("sync/{userId}")
suspend fun syncData(
    @Path("userId") userId: String,
    @Body request: SyncRequestDto
): SyncResponseDto
```

La sincronizzazione invia solo elementi pendenti e poi applica lo stato remoto:

```kotlin
val pendingHabits = habitDao.getPendingHabits(userId)
val pendingCompletions = completionDao.getPendingCompletions(userId)
val response = apiService.syncData(
    userId = userId,
    request = SyncRequestDto(
        habits = pendingHabits.map { it.toSyncDto() },
        completions = pendingCompletions.map { it.toSyncDto(habitRemoteId) }
    )
)
```

Le password non vengono salvate in chiaro nel backend: durante la registrazione viene memorizzato un hash generato con Werkzeug.

## 7. Interfaccia utente e tema

L'interfaccia è realizzata con Jetpack Compose e Material 3. La palette usa toni caldi bianco/kaki, con variante dark mode. I colori principali sono centralizzati nel tema, in modo da ridurre i colori hardcoded nelle schermate.

Il cambio tema è gestito tramite stato Compose globale e preferenza salvata; non viene ricreata manualmente la `MainActivity`. La schermata Theme permette di selezionare tema chiaro o scuro e mostra le due icone dell'app. Il cambio dell'icona launcher usa `activity-alias` dichiarati nel Manifest, soluzione compatibile con Android perché l'icona non può essere sostituita liberamente a runtime con un'immagine qualsiasi.

Sono presenti componenti riutilizzabili come card, bottom bar, chip, gruppi impostazioni, progress ring e dialog di inserimento manuale progresso.

## 8. Gestione notifiche e reminder

I reminder sono implementati con `ReminderScheduler`, `HabitReminderReceiver` e `NotificationHelper`. Quando una habit ha un orario valido, il repository pianifica una notifica. Su Android 13+ viene richiesto il permesso `POST_NOTIFICATIONS`; se il permesso non è concesso, la notifica non viene mostrata.

Il tap sulla notifica riapre l'app tramite `MainActivity`. Dopo la ricezione, il receiver ripianifica il reminder per il giorno successivo.

## 9. Testing

Test consigliati prima della consegna:

1. Registrare un nuovo account.
2. Effettuare login e logout.
3. Creare una habit booleana e una numerica.
4. Incrementare e decrementare il progresso.
5. Usare il long press sul pulsante `+` per inserire valori manuali.
6. Attivare un reminder e verificare la richiesta permesso notifiche.
7. Premere `Sync Now` e poi `Restore from Cloud`.
8. Creare due account diversi e verificare la separazione dei dati.
9. Cambiare tema light/dark senza uscire dalla schermata corrente.
10. Aprire statistiche e dettaglio habit dopo aver registrato completamenti.

Durante il controllo finale è stata verificata la compilazione Android con `assembleDebug` e la compilazione Python del backend con `py_compile`.

## 10. Limiti e sviluppi futuri

La sincronizzazione è disponibile tramite azione manuale e può essere estesa con una sincronizzazione periodica più robusta. Il backend usa email e password, mentre autenticazioni esterne come Google Login non sono incluse nella versione finale. Le migration Room sono semplificate per il contesto universitario; in produzione sarebbe opportuno definire migration esplicite per ogni variazione dello schema.
