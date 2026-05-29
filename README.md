# Gadget Room Backend

> Production-ready REST API for an online phone store: catalog, cart, orders, admin dashboard, JWT authentication,
> image storage, email notifications and CI quality gates.

## Core Features

- [x] **Authentication:** registration, login, access tokens, refresh tokens and password reset flow.
- [x] **Catalog:** phone listing, product details, filtering, sorting, pagination and brand discovery.
- [x] **Cart:** authenticated user cart with add, remove, clear and total recalculation operations.
- [x] **Orders:** order creation, customer order history, delivery details and payment details validation.
- [x] **Admin Panel API:** product, order, customer and dashboard endpoints protected by admin role.
- [x] **Image Storage:** product image upload, metadata retrieval and binary image serving through MinIO.
- [x] **Email Notifications:** order confirmation emails with server-side templates.
- [x] **API Documentation:** OpenAPI documentation with JWT bearer authentication support.

## Engineering Highlights

- [x] **Layered Architecture:** controllers, services, repositories, mappers, validators and DTO contracts are separated.
- [x] **Database Versioning:** Flyway migrations keep schema changes explicit and reproducible.
- [x] **Security:** stateless JWT authentication, BCrypt password hashing, role-based authorization and configurable CORS.
- [x] **Object Storage:** MinIO integration isolates image files from relational data.
- [x] **Validation:** custom validators cover order delivery, payment details, cart rules and filter constraints.
- [x] **Testing:** unit and integration tests cover controllers, services, storage and validation logic.
- [x] **Quality Gate:** JaCoCo enforces a 70% minimum line coverage threshold in CI.
- [x] **Containerization:** Docker and Docker Compose provide reproducible local and production environments.

## Tech Stack

`Java 21` | `Spring Boot 3.5` | `Spring Security` | `Spring Data JPA` | `PostgreSQL` | `Flyway` | `MinIO` | `JWT` |
`MapStruct` | `Lombok` | `Docker` | `JUnit 5` | `Mockito` | `Testcontainers` | `JaCoCo` | `OpenAPI`

## Architecture

```text
Client
  -> REST Controllers
  -> Services
  -> Validators / Mappers
  -> Repositories / Storage Adapters
  -> PostgreSQL / MinIO / SMTP
```

## Main API Areas

| Area | Base path |
| --- | --- |
| Authentication | `/api/auth` |
| Users | `/api/v1/users` |
| Phones | `/api/v1/phones` |
| Filtering | `/api/v1/filter` |
| Images | `/api/v1/images` |
| Cart | `/api/v1/me/cart` |
| Orders | `/api/v1/orders` |
| Admin | `/api/v1/admin/**` |

## API Documentation

- Production Swagger UI: `https://gadget-room.up.railway.app/docs`
- Production OpenAPI JSON: `https://gadget-room.up.railway.app/docs/api-docs`
- Local Swagger UI: `http://localhost:8080/docs`

## Run

```bash
docker compose --env-file test.env up --build
```

```bash
docker compose down
```

## Local Development

```bash
docker compose -f docker-compose-dev.yml up -d
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Tests

```bash
mvn clean verify -Dspring.profiles.active=dev
```

Coverage report:

```text
target/site/jacoco/index.html
```

## CI

GitHub Actions runs on pull requests to `develop` and `main`:

- build
- tests
- JaCoCo coverage check
- coverage report artifact

## Repository

```text
https://github.com/Team-4185/phone-shop
```