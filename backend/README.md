# RoutineTrack Backend

Backend REST minimale per l'app Android RoutineTrack.

## Avvio

```bash
cd backend
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

Questo backend e solo locale. La versione finale dell'app usa il backend online in `backend_tidb`
tramite `ROUTINETRACK_API_BASE_URL` in `gradle.properties`.

## Endpoint

- `POST /auth/register`
- `POST /auth/login`
- `GET /habits?userId=1`
- `POST /habits`
- `PUT /habits/<id>`
- `DELETE /habits/<id>`
- `GET /completions?userId=1`
- `POST /completions`
- `PUT /completions/<id>`

Questo backend rimane una versione locale dimostrativa. Per account email/password e sync cloud usare `backend_tidb`.
