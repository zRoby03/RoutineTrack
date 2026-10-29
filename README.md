<p align="center">
  <img src="app/src/main/res/drawable-nodpi/icon_light.png" width="112" alt="RoutineTrack app icon">
</p>

# RoutineTrack

RoutineTrack is an Android app for creating habits, recording progress and checking statistics. It works locally with Room and can synchronize data through a Flask backend.

[![CI](https://github.com/zRoby03/RoutineTrack/actions/workflows/ci.yml/badge.svg)](https://github.com/zRoby03/RoutineTrack/actions/workflows/ci.yml)
[![Android](https://img.shields.io/badge/Android-26%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)

## Main features

- Boolean and numeric habits with custom frequency and targets.
- Daily progress, streaks, calendar and statistics.
- Local storage with Room and reactive state with Flow.
- Reminders through Android notifications.
- Manual synchronization with a TiDB/MySQL database.
- Light and dark themes.

## Stack

- Kotlin, Jetpack Compose and Material 3.
- ViewModel, coroutines, Flow and StateFlow.
- Room, Retrofit, Moshi and OkHttp.
- Python, Flask, Gunicorn and PyMySQL.

## Architecture

```mermaid
flowchart LR
    UI[Compose UI] --> VM[ViewModel]
    VM --> REPO[Repository]
    REPO --> ROOM[(Room)]
    REPO --> API[Retrofit]
    API --> BACKEND[Flask API]
    BACKEND --> DB[(TiDB)]
    ROOM --> VM
    VM --> UI
```

The project follows an MVVM structure. Room is the local source of data; the backend receives pending changes and returns the synchronized state.

## Requirements

- Android Studio, Android SDK 36 and JDK 17.
- Python 3.11 or newer.
- A TiDB or MySQL-compatible database.

## Backend setup

```bash
cd backend_tidb
python -m venv .venv
pip install -r requirements.txt
```

Copy `.env.example` to `.env`, fill in the database settings and generate `APP_SECRET_KEY`:

```bash
python -c "import secrets; print(secrets.token_urlsafe(48))"
```

Then start the API:

```bash
python app.py
```

The default port is `5000`. The root `render.yaml` can be used to deploy the backend on Render.

## Android setup

Pass the HTTPS API URL as a Gradle property:

```bash
./gradlew :app:assembleDebug -PROUTINETRACK_API_BASE_URL=https://api.example.com/
```

On Windows use `gradlew.bat`. You can also save the property in your user-level Gradle configuration, outside this repository.

## Checks

```bash
pip install -r backend_tidb/requirements-dev.txt
pytest -q
python -m compileall -q backend_tidb app.py
./gradlew :app:assembleDebug
```

The same checks run on GitHub Actions.

## Notes

- Synchronization is currently manual.
- Conflicts use a timestamp-based last-write-wins rule.
- WorkManager integration exists but is not scheduled automatically.
- Room still needs explicit migrations before a production release.
- Security problems should be reported as described in [SECURITY.md](SECURITY.md).

## License

No open-source license has been selected yet.
