# Gadget room

## Remote URLs
- application base-url: ```https://gadget-room.up.railway.app```
- minio base-url(secured ui): ```https://minio-ui.up.railway.app```

## How to get api docs

Application uses an auto-generated api docs.\
To get docs UI: `[base-url]/docs`\
To get docs api:`[base-url]/docs/api-docs`

## How to Launch the Application

### Prerequisites

#### Environment Variables

Set the following environment variables before starting the application:

| Variable                      | Description                             | Example               |
|-------------------------------|-----------------------------------------|-----------------------|
| PORT	                         | Application port	                       | 8080                  |
| DB_HOST	                      | PostgreSQL host address	                | localhost:5544        |
| DB_NAME	                      | Database name	                          | gadgetroom            |
| DB_USERNAME	                  | Database username	                      | postgres              |
| DB_PASSWORD	                  | Database password	                      | pass1234              |
| JWT_ACCESS_TOKEN_EXPIRATION	  | Access token expiration time (minutes)	 | 10                    |
| JWT_REFRESH_TOKEN_EXPIRATION	 | Refresh token expiration time (hours)	  | 2                     |
| JWT_PRIVATE_KEY	              | RSA private key for JWT signing	        | secure                |
| JWT_PUBLIC_KEY	               | RSA public key for JWT verification	    | secure                |
| MINIO_URL	                    | MinIO service URL	                      | http://localhost:9000 |
| MINIO_USERNAME	               | MinIO username	                         | minioadmin            |
| MINIO_PASSWORD	               | MinIO password	                         | minioadmin            |

### Launch

#### Local Development

```bash
export PORT=8080
export DB_HOST=localhost:5544
export DB_NAME=gadgetroom
export DB_USERNAME=postgres
export DB_PASSWORD=pass1234
export JWT_ACCESS_TOKEN_EXPIRATION=10
export JWT_REFRESH_TOKEN_EXPIRATION=2
export JWT_PRIVATE_KEY=<your-private-key>
export JWT_PUBLIC_KEY=<your-public-key>
export MINIO_URL=http://localhost:9000
export MINIO_USERNAME=minioadmin
export MINIO_PASSWORD=minioadmin

./mvnw spring-boot:run

```