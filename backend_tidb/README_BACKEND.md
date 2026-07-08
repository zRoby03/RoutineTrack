# RoutineTrack TiDB Backend

Backend Flask per account email/password e sincronizzazione cloud su TiDB.

## Setup

```bash
cd backend_tidb
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
copy .env.example .env
```

Compila `.env` con le credenziali TiDB. Non salvare credenziali reali nel repository.

## Avvio

```bash
python app.py
```

Endpoint principali:

- `GET /health`
- `POST /auth/register`
- `POST /auth/login`
- `GET /sync/<user_id>`
- `POST /sync/<user_id>`

## Deploy Render

Imposta su Render un Web Service con:

```text
Root Directory: backend_tidb
Build Command: pip install -r requirements.txt
Start Command: gunicorn app:app
```

Se nei log Render vedi `routine start:app`, lo Start Command nel pannello Render e errato.
Apri il servizio su Render, vai in Settings e sostituiscilo con:

```text
gunicorn app:app
```

Se Render dice `No such file or directory: requirements.txt`, il servizio sta buildando dalla root
del repository. Puoi risolvere in uno di questi due modi:

1. Imposta `Root Directory` a `backend_tidb`.
2. Oppure lascia la root e usa il `requirements.txt` e `app.py` presenti nella root del progetto.

Variabili ambiente richieste:

```text
TIDB_HOST
TIDB_PORT
TIDB_USER
TIDB_PASSWORD
TIDB_DATABASE
TIDB_SSL_CA_CONTENT
INIT_SCHEMA_ON_START=true
```

`TIDB_SSL_CA_CONTENT` deve contenere il contenuto PEM del certificato, non un path Windows locale.

## URL Android finale

L'app Android legge la URL da `ROUTINETRACK_API_BASE_URL` in `gradle.properties`, oppure da una variabile ambiente/Gradle property con lo stesso nome.
