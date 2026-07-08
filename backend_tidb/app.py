from __future__ import annotations

import os
import secrets
import smtplib
import ssl
import time
import uuid
from email.message import EmailMessage
from email.utils import formataddr
from pathlib import Path
from typing import Any

import pymysql
from dotenv import load_dotenv
from flask import Flask, jsonify, request
from flask_cors import CORS
from werkzeug.security import check_password_hash, generate_password_hash


BASE_DIR = Path(__file__).resolve().parent
SCHEMA_PATH = BASE_DIR / "schema.sql"

load_dotenv(BASE_DIR / ".env")

app = Flask(__name__)
CORS(app)

RESET_CODE_TTL_MS = 15 * 60 * 1000
RESET_CODE_MAX_ATTEMPTS = 5


def now_ms() -> int:
    return int(time.time() * 1000)


def normalize_email(value: str | None) -> str:
    return (value or "").strip().lower()


def json_body() -> dict[str, Any]:
    data = request.get_json(silent=True)
    return data if isinstance(data, dict) else {}


def connection(database: str | None = None) -> pymysql.connections.Connection:
    ssl_config = tidb_ssl_config()
    kwargs: dict[str, Any] = dict(
        host=os.environ["TIDB_HOST"],
        port=int(os.getenv("TIDB_PORT", "4000")),
        user=os.environ["TIDB_USER"],
        password=os.environ["TIDB_PASSWORD"],
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
        autocommit=False,
        ssl=ssl_config,
    )
    if database != "":
        kwargs["database"] = database if database is not None else os.getenv("TIDB_DATABASE", "RoutineTrack")
    return pymysql.connect(**kwargs)


def tidb_ssl_config() -> dict[str, str] | None:
    ssl_ca = os.getenv("TIDB_SSL_CA")
    ssl_ca_content = os.getenv("TIDB_SSL_CA_CONTENT")
    if ssl_ca and Path(ssl_ca).exists():
        return {"ca": ssl_ca}
    if ssl_ca_content or ssl_ca:
        pem_content = (ssl_ca_content or ssl_ca or "").replace("\\n", "\n")
        if "BEGIN CERTIFICATE" in pem_content:
            cert_path = BASE_DIR / "tidb_ca_runtime.pem"
            cert_path.write_text(pem_content, encoding="utf-8")
            return {"ca": str(cert_path)}
    return None


def init_schema() -> None:
    statements = [
        statement.strip()
        for statement in SCHEMA_PATH.read_text(encoding="utf-8").split(";")
        if statement.strip()
    ]
    with connection(database="") as db:
        with db.cursor() as cursor:
            for statement in statements:
                cursor.execute(statement)
            ensure_column(cursor, "habits", "start_date", "VARCHAR(16)")
            ensure_column(cursor, "habits", "end_date", "VARCHAR(16)")
        db.commit()


def ensure_column(cursor: pymysql.cursors.DictCursor, table: str, column: str, definition: str) -> None:
    cursor.execute(f"SHOW COLUMNS FROM {table} LIKE %s", (column,))
    if cursor.fetchone() is None:
        cursor.execute(f"ALTER TABLE {table} ADD COLUMN {column} {definition}")


def habit_to_dto(row: dict[str, Any], local_ids: dict[str, int] | None = None) -> dict[str, Any]:
    return {
        "localId": (local_ids or {}).get(row["id"]),
        "remoteId": row["id"],
        "userId": row["user_id"],
        "title": row["title"],
        "description": row.get("description"),
        "emoji": row.get("emoji"),
        "color": row.get("color"),
        "targetValue": int(row.get("target_value") or 1),
        "unit": row.get("unit"),
        "activeDays": row.get("active_days"),
        "reminderTime": row.get("reminder_time"),
        "startDate": row.get("start_date"),
        "endDate": row.get("end_date"),
        "createdAt": int(row["created_at"]),
        "updatedAt": int(row["updated_at"]),
        "isDeleted": bool(row["is_deleted"]),
    }


def completion_to_dto(
    row: dict[str, Any],
    local_ids: dict[str, int] | None = None,
    habit_local_ids: dict[str, int] | None = None,
) -> dict[str, Any]:
    return {
        "localId": (local_ids or {}).get(row["id"]),
        "remoteId": row["id"],
        "userId": row["user_id"],
        "habitLocalId": (habit_local_ids or {}).get(row["habit_id"]),
        "habitRemoteId": row["habit_id"],
        "date": row["date"],
        "value": int(row.get("value") or 0),
        "isCompleted": bool(row["is_completed"]),
        "createdAt": int(row["created_at"]),
        "updatedAt": int(row["updated_at"]),
        "isDeleted": bool(row["is_deleted"]),
    }


def email_sender_configured() -> bool:
    host = os.getenv("SMTP_HOST")
    sender = os.getenv("SMTP_FROM") or os.getenv("SMTP_USERNAME") or os.getenv("SMTP_USER")
    return bool(host and sender)


def send_password_reset_email(to_email: str, code: str) -> None:
    host = os.environ["SMTP_HOST"]
    port = int(os.getenv("SMTP_PORT", "587"))
    username = os.getenv("SMTP_USERNAME") or os.getenv("SMTP_USER")
    password = os.getenv("SMTP_PASSWORD")
    sender = os.getenv("SMTP_FROM") or username
    sender_name = os.getenv("SMTP_FROM_NAME", "RoutineTrack")
    use_ssl = os.getenv("SMTP_USE_SSL", "false").lower() == "true"
    use_tls = os.getenv("SMTP_USE_TLS", "true").lower() == "true"

    if not sender:
        raise RuntimeError("SMTP_FROM or SMTP_USERNAME is required")

    message = EmailMessage()
    message["Subject"] = "RoutineTrack - Codice recupero password"
    message["From"] = formataddr((sender_name, sender))
    message["To"] = to_email
    message.set_content(
        "\n".join(
            [
                "Ciao,",
                "",
                f"Il tuo codice per aggiornare la password RoutineTrack e: {code}",
                "",
                "Il codice scade tra 15 minuti.",
                "Se non hai richiesto tu il codice, puoi ignorare questa email.",
                "",
                "RoutineTrack",
            ]
        )
    )

    context = ssl.create_default_context()
    if use_ssl:
        with smtplib.SMTP_SSL(host, port, timeout=10, context=context) as server:
            if username and password:
                server.login(username, password)
            server.send_message(message)
        return

    with smtplib.SMTP(host, port, timeout=10) as server:
        if use_tls:
            server.starttls(context=context)
        if username and password:
            server.login(username, password)
        server.send_message(message)


@app.get("/health")
def health():
    return jsonify({"status": "ok"})


@app.get("/")
def index():
    return jsonify(
        {
            "name": "RoutineTrack API",
            "status": "ok",
            "endpoints": [
                "/health",
                "/auth/register",
                "/auth/login",
                "/auth/request-password-reset",
                "/auth/reset-password",
                "/sync/<user_id>",
            ],
        }
    )


@app.post("/auth/register")
def register():
    data = json_body()
    email = normalize_email(data.get("email"))
    password = data.get("password") or ""
    display_name = data.get("displayName")
    if not email or not password:
        return jsonify({"error": "email and password are required"}), 400

    user_id = uuid.uuid4().hex
    timestamp = now_ms()
    try:
        with connection() as db:
            with db.cursor() as cursor:
                cursor.execute(
                    """
                    INSERT INTO users(
                        id, email, display_name, password_hash, auth_provider, created_at, updated_at
                    ) VALUES (%s, %s, %s, %s, 'email', %s, %s)
                    """,
                    (user_id, email, display_name, generate_password_hash(password), timestamp, timestamp),
                )
            db.commit()
    except pymysql.err.IntegrityError:
        return jsonify({"error": "email already registered"}), 409

    return jsonify({"userId": user_id, "email": email, "displayName": display_name, "token": None}), 201


@app.post("/auth/login")
def login():
    data = json_body()
    email = normalize_email(data.get("email"))
    password = data.get("password") or ""
    with connection() as db:
        with db.cursor() as cursor:
            cursor.execute("SELECT * FROM users WHERE email = %s", (email,))
            user = cursor.fetchone()
    if user is None or not check_password_hash(user["password_hash"], password):
        return jsonify({"error": "invalid credentials"}), 401

    return jsonify(
        {
            "userId": user["id"],
            "email": user["email"],
            "displayName": user.get("display_name"),
            "token": None,
        }
    )


@app.post("/auth/request-password-reset")
def request_password_reset():
    data = json_body()
    email = normalize_email(data.get("email"))
    if not email:
        return jsonify({"error": "email is required"}), 400

    if not email_sender_configured():
        return jsonify({"error": "password reset is temporarily unavailable"}), 503

    generic_response = {"message": "Se l'email e registrata, riceverai un codice."}
    timestamp = now_ms()
    with connection() as db:
        with db.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE email = %s", (email,))
            user = cursor.fetchone()
            if user is None:
                db.commit()
                return jsonify(generic_response)

            code = f"{secrets.randbelow(1_000_000):06d}"
            reset_id = uuid.uuid4().hex
            cursor.execute(
                "UPDATE password_reset_codes SET used = TRUE WHERE email = %s AND used = FALSE",
                (email,),
            )
            cursor.execute(
                """
                INSERT INTO password_reset_codes(
                    id, email, code_hash, expires_at, used, attempts, created_at
                ) VALUES (%s, %s, %s, %s, FALSE, 0, %s)
                """,
                (
                    reset_id,
                    email,
                    generate_password_hash(code),
                    timestamp + RESET_CODE_TTL_MS,
                    timestamp,
                ),
            )
            try:
                send_password_reset_email(email, code)
            except Exception:
                db.rollback()
                app.logger.exception("Unable to send password reset email")
                return jsonify({"error": "password reset is temporarily unavailable"}), 503
        db.commit()

    return jsonify(generic_response)


@app.post("/auth/reset-password")
def reset_password():
    data = json_body()
    email = normalize_email(data.get("email"))
    code = (data.get("code") or "").strip()
    new_password = data.get("newPassword") or ""
    if not email or not code or not new_password:
        return jsonify({"error": "email, code and newPassword are required"}), 400
    if len(new_password) < 8:
        return jsonify({"error": "password is too short"}), 400

    timestamp = now_ms()
    generic_invalid = {"error": "invalid or expired code"}
    with connection() as db:
        with db.cursor() as cursor:
            cursor.execute("SELECT id FROM users WHERE email = %s", (email,))
            user = cursor.fetchone()
            if user is None:
                return jsonify(generic_invalid), 400

            cursor.execute(
                """
                SELECT * FROM password_reset_codes
                WHERE email = %s AND used = FALSE
                ORDER BY created_at DESC
                LIMIT 1
                """,
                (email,),
            )
            reset_code = cursor.fetchone()
            if reset_code is None or int(reset_code["expires_at"]) < timestamp:
                return jsonify(generic_invalid), 400

            attempts = int(reset_code.get("attempts") or 0)
            if attempts >= RESET_CODE_MAX_ATTEMPTS:
                cursor.execute(
                    "UPDATE password_reset_codes SET used = TRUE WHERE id = %s",
                    (reset_code["id"],),
                )
                db.commit()
                return jsonify(generic_invalid), 400

            if not check_password_hash(reset_code["code_hash"], code):
                cursor.execute(
                    "UPDATE password_reset_codes SET attempts = attempts + 1 WHERE id = %s",
                    (reset_code["id"],),
                )
                db.commit()
                return jsonify(generic_invalid), 400

            cursor.execute(
                "UPDATE users SET password_hash = %s, updated_at = %s WHERE id = %s",
                (generate_password_hash(new_password), timestamp, user["id"]),
            )
            cursor.execute(
                "UPDATE password_reset_codes SET used = TRUE WHERE id = %s",
                (reset_code["id"],),
            )
        db.commit()

    return jsonify({"message": "password updated"})


@app.get("/sync/<user_id>")
def get_sync_data(user_id: str):
    with connection() as db:
        with db.cursor() as cursor:
            cursor.execute("SELECT * FROM habits WHERE user_id = %s", (user_id,))
            habits = cursor.fetchall()
            cursor.execute("SELECT * FROM habit_completions WHERE user_id = %s", (user_id,))
            completions = cursor.fetchall()
            cursor.execute("SELECT last_sync FROM sync_state WHERE user_id = %s", (user_id,))
            sync_row = cursor.fetchone()
    return jsonify(
        {
            "habits": [habit_to_dto(row) for row in habits],
            "completions": [completion_to_dto(row) for row in completions],
            "lastSync": int(sync_row["last_sync"]) if sync_row else 0,
        }
    )


@app.post("/sync/<user_id>")
def sync_data(user_id: str):
    data = request.get_json(force=True)
    habit_local_ids: dict[str, int] = {}
    completion_local_ids: dict[str, int] = {}
    local_habit_to_remote: dict[int, str] = {}

    timestamp = now_ms()
    with connection() as db:
        with db.cursor() as cursor:
            for habit in data.get("habits", []):
                remote_id = habit.get("remoteId") or uuid.uuid4().hex
                local_id = habit.get("localId")
                if local_id is not None:
                    habit_local_ids[remote_id] = local_id
                    local_habit_to_remote[local_id] = remote_id

                cursor.execute("SELECT updated_at FROM habits WHERE id = %s", (remote_id,))
                existing = cursor.fetchone()
                incoming_updated_at = int(habit.get("updatedAt") or timestamp)
                if existing and int(existing["updated_at"]) > incoming_updated_at:
                    continue

                cursor.execute(
                    """
                    INSERT INTO habits(
                        id, user_id, title, description, emoji, color, target_value, unit,
                        active_days, reminder_time, start_date, end_date, created_at, updated_at, is_deleted
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON DUPLICATE KEY UPDATE
                        title = VALUES(title),
                        description = VALUES(description),
                        emoji = VALUES(emoji),
                        color = VALUES(color),
                        target_value = VALUES(target_value),
                        unit = VALUES(unit),
                        active_days = VALUES(active_days),
                        reminder_time = VALUES(reminder_time),
                        start_date = VALUES(start_date),
                        end_date = VALUES(end_date),
                        updated_at = VALUES(updated_at),
                        is_deleted = VALUES(is_deleted)
                    """,
                    (
                        remote_id,
                        user_id,
                        habit.get("title"),
                        habit.get("description"),
                        habit.get("emoji"),
                        habit.get("color"),
                        int(habit.get("targetValue") or 1),
                        habit.get("unit"),
                        habit.get("activeDays"),
                        habit.get("reminderTime"),
                        habit.get("startDate"),
                        habit.get("endDate"),
                        int(habit.get("createdAt") or timestamp),
                        incoming_updated_at,
                        bool(habit.get("isDeleted", False)),
                    ),
                )

            for completion in data.get("completions", []):
                remote_id = completion.get("remoteId") or uuid.uuid4().hex
                habit_remote_id = completion.get("habitRemoteId")
                habit_local_id = completion.get("habitLocalId")
                if not habit_remote_id and habit_local_id is not None:
                    habit_remote_id = local_habit_to_remote.get(habit_local_id)
                if not habit_remote_id:
                    continue

                local_id = completion.get("localId")
                if local_id is not None:
                    completion_local_ids[remote_id] = local_id

                cursor.execute("SELECT updated_at FROM habit_completions WHERE id = %s", (remote_id,))
                existing = cursor.fetchone()
                incoming_updated_at = int(completion.get("updatedAt") or timestamp)
                if existing and int(existing["updated_at"]) > incoming_updated_at:
                    continue

                cursor.execute(
                    """
                    INSERT INTO habit_completions(
                        id, user_id, habit_id, date, value, is_completed,
                        created_at, updated_at, is_deleted
                    ) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                    ON DUPLICATE KEY UPDATE
                        value = VALUES(value),
                        is_completed = VALUES(is_completed),
                        updated_at = VALUES(updated_at),
                        is_deleted = VALUES(is_deleted)
                    """,
                    (
                        remote_id,
                        user_id,
                        habit_remote_id,
                        completion.get("date"),
                        int(completion.get("value") or 0),
                        bool(completion.get("isCompleted", False)),
                        int(completion.get("createdAt") or timestamp),
                        incoming_updated_at,
                        bool(completion.get("isDeleted", False)),
                    ),
                )

            cursor.execute(
                """
                INSERT INTO sync_state(user_id, last_sync) VALUES (%s, %s)
                ON DUPLICATE KEY UPDATE last_sync = VALUES(last_sync)
                """,
                (user_id, timestamp),
            )
            cursor.execute("SELECT * FROM habits WHERE user_id = %s", (user_id,))
            habits = cursor.fetchall()
            cursor.execute("SELECT * FROM habit_completions WHERE user_id = %s", (user_id,))
            completions = cursor.fetchall()
        db.commit()

    return jsonify(
        {
            "habits": [habit_to_dto(row, habit_local_ids) for row in habits],
            "completions": [
                completion_to_dto(row, completion_local_ids, habit_local_ids) for row in completions
            ],
            "lastSync": timestamp,
        }
    )


if os.getenv("INIT_SCHEMA_ON_START", "true").lower() == "true":
    init_schema()


if __name__ == "__main__":
    port = int(os.getenv("PORT", os.getenv("FLASK_PORT", "5000")))
    debug = os.getenv("FLASK_DEBUG", "false").lower() == "true"
    app.run(host="0.0.0.0", port=port, debug=debug)
