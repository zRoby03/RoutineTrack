# Security

Do not report security problems in a public issue. Use GitHub's private vulnerability reporting feature and include clear reproduction steps.

Never include passwords, access tokens, database contents or certificates in a report.

Only the latest version on `main` is supported.

For deployments:

- keep `.env`, `APP_SECRET_KEY` and database credentials outside Git;
- use HTTPS;
- rotate any exposed credential immediately;
- give the database user only the permissions it needs.
