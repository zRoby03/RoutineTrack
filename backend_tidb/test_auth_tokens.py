import os

os.environ.setdefault("APP_SECRET_KEY", "test-only-secret-that-is-long-enough-for-unit-tests")
os.environ["INIT_SCHEMA_ON_START"] = "false"

import pytest

from backend_tidb.app import app, authorize_user, configured_database_name, issue_token


def status_code(response_tuple) -> int:
    return response_tuple[1]


def test_valid_token_authorizes_matching_user():
    token = issue_token("user-1")

    with app.test_request_context(headers={"Authorization": f"Bearer {token}"}):
        assert authorize_user("user-1") is None


def test_missing_token_is_rejected():
    with app.test_request_context():
        assert status_code(authorize_user("user-1")) == 401


def test_invalid_token_is_rejected():
    with app.test_request_context(headers={"Authorization": "Bearer invalid"}):
        assert status_code(authorize_user("user-1")) == 401


def test_token_cannot_access_another_user():
    token = issue_token("user-1")

    with app.test_request_context(headers={"Authorization": f"Bearer {token}"}):
        assert status_code(authorize_user("user-2")) == 403


def test_sync_endpoint_requires_authentication_before_database_access():
    response = app.test_client().get("/sync/user-1")

    assert response.status_code == 401
    assert response.get_json() == {"error": "authentication required"}


def test_database_name_rejects_sql_identifiers(monkeypatch):
    monkeypatch.setenv("TIDB_DATABASE", "RoutineTrack; DROP DATABASE RoutineTrack")

    with pytest.raises(RuntimeError):
        configured_database_name()
