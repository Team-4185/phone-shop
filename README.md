# Gadget room

## How to get api docs

Application uses an auto-generated api docs.\
To get docs UI: `[base-url]/docs`\
To get docs api:`[base-url]/docs/api-docs`

## How to Launch the Application

### Prerequisites

Set the following environment variables before starting the application:

| Variable      | Description             |
|---------------|-------------------------|
| `PORT`        | Application port        |
| `DB_HOST`     | PostgreSQL host address |
| `DB_NAME`     | Database name           |
| `DB_USERNAME` | Database username       |
| `DB_PASSWORD` | Database password       |

### Launch

```bash
export PORT=8080
export DB_HOST=localhost:5544
export DB_NAME=gadgetroom
export DB_USERNAME=postgres
export DB_PASSWORD=pass1234

./mvnw spring-boot:run
```