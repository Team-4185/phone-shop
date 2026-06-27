# QA Guide

> This guide explains how to run Gadget Room Backend locally and test the API without touching production data.

## Requirements

- Docker Desktop is installed and running.
- Git is installed.
- A valid `test.env` file is placed in the project root or in the `qa` folder.
- Port values from `test.env` are free on your machine.

## Start The Application

From the `qa` folder, run one of the setup scripts:

```bash
./setup.sh
```

```bat
setup.bat
```

The script starts the backend and its dependencies with Docker Compose.

## Reset The Local Environment

Use reset when migrations changed, seed data is stale or containers fail because of old volumes:

```bash
./setup.sh reset
```

```bat
setup.bat reset
```

This removes Docker volumes for the `gadget-room-backend` compose project and starts a clean environment.

## What Runs Locally

| Container | Purpose |
| --- | --- |
| Backend | Spring Boot REST API under test. |
| PostgreSQL | Business data: users, phones, carts, orders, reviews and migrations. |
| Redis | Revoked access and refresh tokens. |
| MinIO | Product image binaries. |

## Useful URLs

| Tool | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| MinIO API | `http://localhost:9000` |
| MinIO Console | `http://localhost:9001` |
| Health | `http://localhost:8080/actuator/health` |

If ports are different in your `test.env`, use those values instead.

## Database Connection

Use any PostgreSQL client, for example DBeaver, pgAdmin or DataGrip.

| Parameter | Value |
| --- | --- |
| Host | `localhost` |
| Port | `DB_PORT` from `test.env` |
| Database | `DB_NAME` from `test.env` |
| Username | `DB_USERNAME` from `test.env` |
| Password | `DB_PASSWORD` from `test.env` |

The database is available only while the Docker environment is running.

## MinIO Access

Use the MinIO console at `http://localhost:9001`.

| Parameter | Value |
| --- | --- |
| Username | `MINIO_USERNAME` from `test.env` |
| Password | `MINIO_PASSWORD` from `test.env` |

The `images` bucket is created after the first successful product image upload.

## Suggested Smoke Test

1. Open Swagger UI.
2. Register or log in through `/api/auth/**`.
3. Authorize Swagger with the returned bearer token.
4. Request `/api/v1/phones` and check that catalog data is returned.
5. Add a product to `/api/v1/me/cart/put`.
6. Create an order through `/api/v1/orders/checkout`.
7. Check `/actuator/health`.

For admin-only endpoints, use a user with the admin role from the seed data or create one directly for the test run.

## Troubleshooting

- **Swagger does not open:** check that the backend container is running and port `PORT` from `test.env` is free.
- **Database migration error:** run the reset command instead of deleting containers manually.
- **Cannot connect to PostgreSQL:** verify `DB_PORT` and make sure Docker Desktop is running.
- **Cannot upload images:** verify MinIO credentials and check that `MINIO_STORAGE_PORT` is free.
