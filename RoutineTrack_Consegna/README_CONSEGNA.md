# README Consegna - RoutineTrack

## Descrizione

RoutineTrack è un'app Android/Kotlin per il tracciamento di abitudini personali. L'app usa Jetpack Compose per l'interfaccia, Room per la persistenza locale, Retrofit per la comunicazione HTTP e un backend Flask collegato a TiDB per account e sincronizzazione cloud.

## Struttura della consegna

```text
RoutineTrack_Consegna/
  RoutineTrack_Android/
  backend_tidb/
  docs/
    Relazione_RoutineTrack.docx
    Relazione_RoutineTrack.md
  README_CONSEGNA.md
```

## Aprire il progetto Android

1. Aprire Android Studio.
2. Selezionare `Open`.
3. Aprire la cartella `RoutineTrack_Android`.
4. Attendere il sync Gradle.
5. Eseguire il modulo `app` su emulatore o dispositivo fisico.

Comando alternativo da terminale:

```bash
.\gradlew.bat :app:assembleDebug
```

## Configurare il backend

Il backend finale è nella cartella `backend_tidb`.

```bash
cd backend_tidb
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
python app.py
```

Il file `.env` deve essere compilato con le credenziali TiDB. Il file `.env` reale non è incluso nella consegna.

## Variabili TiDB richieste

```text
TIDB_HOST
TIDB_PORT
TIDB_USER
TIDB_PASSWORD
TIDB_DATABASE
TIDB_SSL_CA_CONTENT
INIT_SCHEMA_ON_START=true
```

Lo schema SQL si trova in `backend_tidb/schema.sql` e crea le tabelle `users`, `habits`, `habit_completions` e `sync_state`.

## Configurare la BASE_URL Android

L'app legge la URL del backend da `ROUTINETRACK_API_BASE_URL` in `gradle.properties`.

```properties
ROUTINETRACK_API_BASE_URL=https://routinetrack.onrender.com/
```

Per emulatore Android con backend locale:

```properties
ROUTINETRACK_API_BASE_URL=http://10.0.2.2:5000/
```

Per telefono fisico con backend locale usare l'IP del PC nella stessa rete. Per una consegna stabile è preferibile usare il backend online Render.

## Endpoint backend

```text
GET  /health
POST /auth/register
POST /auth/login
GET  /sync/<user_id>
POST /sync/<user_id>
```

## Account di test

Non sono incluse credenziali reali nella consegna. È possibile creare un account nuovo dalla schermata di registrazione dell'app.

## Contenuto dello zip

Lo zip di consegna contiene:

- progetto Android Studio completo;
- backend Flask/TiDB;
- relazione finale in formato `.docx` e `.md`;
- README di consegna;
- file `.env.example` senza credenziali reali;
- schema SQL del database.

Non contiene:

- cartelle `build/`;
- cartelle `.gradle/`;
- file `.env` reale;
- `local.properties`;
- database locali `.db`;
- cache Python o Gradle;
- credenziali, keystore o file temporanei.

## Limiti noti

La sincronizzazione cloud è disponibile tramite azione manuale `Sync Now` e ripristino `Restore from Cloud`. Le migration Room sono semplificate per il contesto universitario; per una pubblicazione reale andrebbero definite migration esplicite per ogni cambio schema.
