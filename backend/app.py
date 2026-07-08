from __future__ import annotations

import sqlite3
import uuid
from pathlib import Path
from typing import Any

from flask import Flask, jsonify, request
from werkzeug.security import check_password_hash, generate_password_hash


BASE_DIR = Path(__file__).resolve().parent
DB_PATH = BASE_DIR / "routinetrack.db"

app = Flask(__name__)


@app.get("/")
def index():
    return jsonify(
        {
            "name": "RoutineTrack local legacy backend",
            "status": "ok",
            "message": "Per la versione finale usa backend_tidb.",
        }
    )


@app.get("/health")
def health():
    return jsonify({"status": "ok", "backend": "local-legacy"})


def get_db() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db() -> None:
    with get_db() as db:
        db.execute(
            """
            CREATE TABLE IF NOT EXISTS users (
                id TEXT PRIMARY KEY,
                email TEXT NOT NULL UNIQUE,
                display_name TEXT,
                password_hash TEXT NOT NULL,
                token TEXT
            )
            """
        )
        db.execute(
            """
            CREATE TABLE IF NOT EXISTS habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                category TEXT NOT NULL,
                color TEXT NOT NULL,
                type TEXT NOT NULL,
                target_value REAL,
                unit TEXT,
                frequency TEXT NOT NULL,
                reminder_enabled INTEGER NOT NULL DEFAULT 0,
                reminder_time TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                is_deleted INTEGER NOT NULL DEFAULT 0
            )
            """
        )
        db.execute(
            """
            CREATE TABLE IF NOT EXISTS completions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                habit_id INTEGER NOT NULL,
                local_habit_id INTEGER,
                user_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                value REAL NOT NULL DEFAULT 1,
                completed INTEGER NOT NULL DEFAULT 1,
                created_at INTEGER NOT NULL,
                UNIQUE(user_id, habit_id, date)
            )
            """
        )


def row_to_habit(row: sqlite3.Row) -> dict[str, Any]:
    return {
        "id": row["id"],
        "user_id": row["user_id"],
        "title": row["title"],
        "description": row["description"] or "",
        "category": row["category"],
        "color": row["color"],
        "type": row["type"],
        "target_value": row["target_value"],
        "unit": row["unit"],
        "frequency": row["frequency"],
        "reminder_enabled": bool(row["reminder_enabled"]),
        "reminder_time": row["reminder_time"],
        "created_at": row["created_at"],
        "updated_at": row["updated_at"],
        "is_deleted": bool(row["is_deleted"]),
    }


def row_to_completion(row: sqlite3.Row) -> dict[str, Any]:
    return {
        "id": row["id"],
        "habit_id": row["habit_id"],
        "local_habit_id": row["local_habit_id"],
        "user_id": row["user_id"],
        "date": row["date"],
        "value": row["value"],
        "completed": bool(row["completed"]),
        "created_at": row["created_at"],
    }


@app.post("/auth/register")
def register():
    data = request.get_json(force=True)
    email = (data.get("email") or "").strip().lower()
    password = data.get("password") or ""
    display_name = data.get("displayName")
    if not email or not password:
        return jsonify({"error": "email and password are required"}), 400

    user_id = uuid.uuid4().hex
    try:
        with get_db() as db:
            db.execute(
                "INSERT INTO users(id, email, display_name, password_hash, token) VALUES (?, ?, ?, ?, ?)",
                (user_id, email, display_name, generate_password_hash(password), None),
            )
    except sqlite3.IntegrityError:
        return jsonify({"error": "email already registered"}), 409

    return jsonify({"userId": user_id, "email": email, "displayName": display_name, "token": None}), 201


@app.post("/auth/login")
def login():
    data = request.get_json(force=True)
    email = (data.get("email") or "").strip().lower()
    password = data.get("password") or ""
    with get_db() as db:
        user = db.execute("SELECT * FROM users WHERE email = ?", (email,)).fetchone()
        if user is None or not check_password_hash(user["password_hash"], password):
            return jsonify({"error": "invalid credentials"}), 401
        return jsonify({
            "userId": user["id"],
            "email": user["email"],
            "displayName": user["display_name"],
            "token": user["token"],
        })


@app.get("/habits")
def get_habits():
    user_id = request.args.get("userId")
    if user_id is None:
        return jsonify({"error": "userId query parameter is required"}), 400
    with get_db() as db:
        rows = db.execute(
            "SELECT * FROM habits WHERE user_id = ? AND is_deleted = 0 ORDER BY created_at DESC",
            (user_id,),
        ).fetchall()
    return jsonify([row_to_habit(row) for row in rows])


@app.post("/habits")
def create_habit():
    data = request.get_json(force=True)
    with get_db() as db:
        cursor = db.execute(
            """
            INSERT INTO habits(
                user_id, title, description, category, color, type, target_value, unit,
                frequency, reminder_enabled, reminder_time, created_at, updated_at, is_deleted
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """,
            (
                data["user_id"],
                data["title"],
                data.get("description", ""),
                data.get("category", "OTHER"),
                data.get("color", "#F1F3F4"),
                data.get("type", "BOOLEAN"),
                data.get("target_value"),
                data.get("unit"),
                data.get("frequency", "DAILY"),
                int(bool(data.get("reminder_enabled", False))),
                data.get("reminder_time"),
                data.get("created_at", 0),
                data.get("updated_at", 0),
                int(bool(data.get("is_deleted", False))),
            ),
        )
        row = db.execute("SELECT * FROM habits WHERE id = ?", (cursor.lastrowid,)).fetchone()
    return jsonify(row_to_habit(row)), 201


@app.put("/habits/<int:habit_id>")
def update_habit(habit_id: int):
    data = request.get_json(force=True)
    with get_db() as db:
        db.execute(
            """
            UPDATE habits SET
                title = ?, description = ?, category = ?, color = ?, type = ?,
                target_value = ?, unit = ?, frequency = ?, reminder_enabled = ?,
                reminder_time = ?, updated_at = ?, is_deleted = ?
            WHERE id = ?
            """,
            (
                data["title"],
                data.get("description", ""),
                data.get("category", "OTHER"),
                data.get("color", "#F1F3F4"),
                data.get("type", "BOOLEAN"),
                data.get("target_value"),
                data.get("unit"),
                data.get("frequency", "DAILY"),
                int(bool(data.get("reminder_enabled", False))),
                data.get("reminder_time"),
                data.get("updated_at", 0),
                int(bool(data.get("is_deleted", False))),
                habit_id,
            ),
        )
        row = db.execute("SELECT * FROM habits WHERE id = ?", (habit_id,)).fetchone()
    if row is None:
        return jsonify({"error": "habit not found"}), 404
    return jsonify(row_to_habit(row))


@app.delete("/habits/<int:habit_id>")
def delete_habit(habit_id: int):
    with get_db() as db:
        db.execute("UPDATE habits SET is_deleted = 1 WHERE id = ?", (habit_id,))
    return "", 204


@app.get("/completions")
def get_completions():
    user_id = request.args.get("userId")
    if user_id is None:
        return jsonify({"error": "userId query parameter is required"}), 400
    with get_db() as db:
        rows = db.execute(
            "SELECT * FROM completions WHERE user_id = ? ORDER BY date DESC",
            (user_id,),
        ).fetchall()
    return jsonify([row_to_completion(row) for row in rows])


@app.post("/completions")
def create_completion():
    data = request.get_json(force=True)
    with get_db() as db:
        existing = db.execute(
            "SELECT id FROM completions WHERE user_id = ? AND habit_id = ? AND date = ?",
            (data["user_id"], data["habit_id"], data["date"]),
        ).fetchone()
        if existing:
            completion_id = existing["id"]
            db.execute(
                """
                UPDATE completions SET value = ?, completed = ?, created_at = ?, local_habit_id = ?
                WHERE id = ?
                """,
                (
                    data.get("value", 1),
                    int(bool(data.get("completed", True))),
                    data.get("created_at", 0),
                    data.get("local_habit_id"),
                    completion_id,
                ),
            )
        else:
            cursor = db.execute(
                """
                INSERT INTO completions(habit_id, local_habit_id, user_id, date, value, completed, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    data["habit_id"],
                    data.get("local_habit_id"),
                    data["user_id"],
                    data["date"],
                    data.get("value", 1),
                    int(bool(data.get("completed", True))),
                    data.get("created_at", 0),
                ),
            )
            completion_id = cursor.lastrowid
        row = db.execute("SELECT * FROM completions WHERE id = ?", (completion_id,)).fetchone()
    return jsonify(row_to_completion(row)), 201


@app.put("/completions/<int:completion_id>")
def update_completion(completion_id: int):
    data = request.get_json(force=True)
    with get_db() as db:
        db.execute(
            """
            UPDATE completions SET habit_id = ?, local_habit_id = ?, user_id = ?, date = ?,
                value = ?, completed = ?, created_at = ?
            WHERE id = ?
            """,
            (
                data["habit_id"],
                data.get("local_habit_id"),
                data["user_id"],
                data["date"],
                data.get("value", 1),
                int(bool(data.get("completed", True))),
                data.get("created_at", 0),
                completion_id,
            ),
        )
        row = db.execute("SELECT * FROM completions WHERE id = ?", (completion_id,)).fetchone()
    if row is None:
        return jsonify({"error": "completion not found"}), 404
    return jsonify(row_to_completion(row))


if __name__ == "__main__":
    init_db()
    app.run(host="0.0.0.0", port=5000, debug=True)
