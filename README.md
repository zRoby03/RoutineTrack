# RoutineTrack

RoutineTrack è un'app Android/Kotlin per il tracciamento personale delle abitudini. Il progetto usa Jetpack Compose, Material 3, Room, Retrofit, WorkManager e un backend Flask collegabile a TiDB.

## Funzionalita principali

- Registrazione, login e logout con email/password.
- Ogni account vede solo le proprie routine grazie al filtro `userId`.
- Creazione e modifica di abitudini booleane o numeriche.
- Frequenza giornaliera o per giorni specifici.
- Reminder con WorkManager e notifiche Android.
- Home pulita con calendario settimanale e lista abitudini del giorno.
- Statistiche con calendario mensile, progress ring e report.
- Account/Settings con stato sync, ultimo sync, `Sync Now`, `Restore from Cloud` e tema light/dark.
- Sync offline-first: Room prima, TiDB tramite backend Flask quando richiesto.

## Architettura

L'app usa MVVM:

- `ui/screens`: schermate Compose e ViewModel.
- `domain/model`: modelli puri dell'app.
- `domain/usecase`: calcolo statistiche e streak.
- `data/local`: Room database, entity e DAO.
- `data/remote`: Retrofit, ApiService e DTO.
- `data/repository`: repository per auth, habit, stats e sync.
- `data/session`: sessione utente su SharedPreferences.
- `notification`: reminder con WorkManager.
- `sync`: worker e manager per sincronizzazione.

Flusso dati:

```text
UI Compose -> ViewModel -> Repository -> DAO -> Room Database
```

La UI non chiama direttamente DAO o Retrofit. Le modifiche locali impostano `pendingSync = true`; la sincronizzazione invia al backend solo i dati pendenti dell'utente loggato.

## Backend TiDB

Il backend principale si trova in `backend_tidb/`.

```bash
cd backend_tidb
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
python app.py
```

Configura `.env` con host, porta, utente, password e database TiDB. Non inserire credenziali reali nel codice.

Endpoint:

- `GET /health`
- `POST /auth/register`
- `POST /auth/login`
- `GET /sync/<user_id>`
- `POST /sync/<user_id>`

## Backend online Android

Per la versione finale non usare IP locali come `192.168.x.x` o `10.0.2.2`.

L'app legge la URL stabile del backend da `ROUTINETRACK_API_BASE_URL` in `gradle.properties`.
Esempio:

```properties
ROUTINETRACK_API_BASE_URL=https://routinetrack-backend.onrender.com/
```

Se Render genera una URL diversa, cambia solo quella riga e ricompila l'app.

## Render

Se il servizio Render è creato manualmente dalla root della repository, usa:

```text
Build Command: pip install -r requirements.txt
Start Command: gunicorn app:app
```

La root contiene un piccolo `app.py` che espone il backend reale in `backend_tidb/app.py`.

Se invece usi `backend_tidb` come Root Directory, usa:

```text
Root Directory: backend_tidb
Build Command: pip install -r requirements.txt
Start Command: gunicorn app:app
```

## Avviare l'app

Da Android Studio apri il progetto e lancia il modulo `app`.

Da terminale:

```bash
.\gradlew.bat :app:assembleDebug
```

## Test consigliato

1. Avvia il backend TiDB.
2. Avvia l'app.
3. Registra Account A.
4. Crea la habit `Workout`.
5. Premi `Sync Now`.
6. Logout.
7. Registra Account B.
8. Verifica che Account B non veda `Workout`.
9. Crea `Study`, sincronizza e fai logout.
10. Login Account A.
11. Verifica che Account A veda `Workout` ma non `Study`.

## Note per l'esame

- Compose gestisce UI dichiarativa e stato osservabile.
- Room garantisce persistenza locale e uso offline.
- Retrofit separa la comunicazione HTTP dal resto dell'app.
- Repository mantiene la logica dati fuori dai composable.
- WorkManager gestisce reminder e sync fuori dal main thread.
- TiDB fornisce storage cloud SQL compatibile MySQL.

Frase pronta per relazione:

> Nel progetto è stata rimossa la precedente sezione Coach, ritenuta non necessaria rispetto allo scopo principale dell'applicazione. Al suo posto è stata introdotta una gestione account con sincronizzazione cloud. Ogni utente può registrarsi tramite email e password, creare le proprie routine e sincronizzarle con un backend Flask collegato a un database TiDB. L'app utilizza Room come database locale offline-first e Retrofit per comunicare con il web service remoto.
