CREATE DATABASE IF NOT EXISTS RoutineTrack;
USE RoutineTrack;

CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    display_name VARCHAR(255),
    password_hash TEXT,
    auth_provider VARCHAR(32) NOT NULL DEFAULT 'email',
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS habits (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    emoji VARCHAR(32),
    color VARCHAR(32),
    target_value INT NOT NULL DEFAULT 1,
    unit VARCHAR(64),
    active_days VARCHAR(64),
    reminder_time VARCHAR(16),
    start_date VARCHAR(16),
    end_date VARCHAR(16),
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    INDEX idx_habits_user_id (user_id),
    CONSTRAINT fk_habits_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS habit_completions (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    habit_id VARCHAR(64) NOT NULL,
    date VARCHAR(16) NOT NULL,
    value INT NOT NULL DEFAULT 0,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    UNIQUE KEY uq_completion_user_habit_date (user_id, habit_id, date),
    INDEX idx_completions_user_id (user_id),
    INDEX idx_completions_habit_id (habit_id),

    CONSTRAINT fk_completions_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_completions_habit
        FOREIGN KEY (habit_id) REFERENCES habits(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS sync_state (
    user_id VARCHAR(64) PRIMARY KEY,
    last_sync BIGINT NOT NULL,

    CONSTRAINT fk_sync_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);
