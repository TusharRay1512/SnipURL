# SnipURL

SnipURL is a REST API for creating and managing short links. It supports user registration and JWT authentication, link redirects, and click analytics backed by PostgreSQL.

## Features

- Register and log in with a username, email, and password.
- Create short URLs and list or soft-delete your links.
- Redirect short codes to their original URLs and record clicks.
- View daily click counts for an individual link or across your links.
- Explore the API through Swagger UI.

## Requirements

- Java 17 or later (Java 21 is used by the Dockerfiles).
- PostgreSQL.
- No separate Maven installation is required when using the included Maven Wrapper.

## Configuration

The application listens on port `8085` by default. Configure the following environment variables before starting it:

| Variable | Description |
| --- | --- |
| `DATABASE_URL` | JDBC URL for the PostgreSQL database, for example `jdbc:postgresql://localhost:5432/snipurl` |
| `DATABASE_USERNAME` | PostgreSQL username |
| `DATABASE_PASSWORD` | PostgreSQL password |
| `DATABASE_DIALECT` | Hibernate dialect, typically `org.hibernate.dialect.PostgreSQLDialect` |
| `JWT_SECRET` | Base64-encoded secret used to sign JWTs; use a securely generated key of at least 32 bytes |
| `FRONTEND_URL` | Frontend origin allowed by the API's CORS configuration |

The project ignores `.env` files, and Spring Boot does not load them automatically. Set the variables in your shell or IDE run configuration, or use a separate environment-file loader. Never commit real credentials or signing keys.

## Run locally

In PowerShell, set the variables for the current terminal session (replace the example values):

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/snipurl"
$env:DATABASE_USERNAME = "postgres"
$env:DATABASE_PASSWORD = "your-local-password"
$env:DATABASE_DIALECT = "org.hibernate.dialect.PostgreSQLDialect"
$env:JWT_SECRET = "BASE64_ENCODED_SECRET"
$env:FRONTEND_URL = "http://localhost:3000"
.\mvnw.cmd spring-boot:run
```

The API is then available at `http://localhost:8085`. Create the PostgreSQL database before starting the application. Hibernate is configured to update the schema at startup.

To build and test:

```powershell
.\mvnw.cmd clean verify
```

## API

All responses and request bodies are JSON unless noted otherwise. The protected endpoints require the JWT returned by login, sent as `Authorization: Bearer <token>`.

### Authentication

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/auth/public/register` | Public | Register a user |
| `POST` | `/api/auth/public/login` | Public | Authenticate and receive a JWT |

Registration request:

```json
{
  "username": "alice",
  "email": "alice@example.com",
  "password": "a-secure-password"
}
```

Login request:

```json
{
  "username": "alice",
  "password": "a-secure-password"
}
```

Login returns a JSON object containing the `token` field. Registration requires a username of at least 3 characters and a password of at least 8 characters.

### Short URLs and analytics

| Method | Endpoint | Access | Description |
| --- | --- | --- | --- |
| `POST` | `/api/urls/shorten` | Authenticated | Create a short URL |
| `GET` | `/api/urls/myurls` | Authenticated | List the current user's active URLs |
| `DELETE` | `/api/urls/{shortUrl}` | Authenticated | Soft-delete one of the current user's URLs |
| `GET` | `/api/urls/analytics/{shortUrl}?startDate=...&endDate=...` | Authenticated | Daily clicks for a URL |
| `GET` | `/api/urls/totalClicks?startDate=...&endDate=...` | Authenticated | Daily clicks across the current user's URLs |
| `GET` | `/{shortUrl}` | Public | Redirect to the original URL and record a click |

Create a URL with:

```json
{
  "originalUrl": "https://example.com/some/long/path"
}
```

The analytics endpoint expects `startDate` and `endDate` in ISO local date-time format, for example `2026-10-01T00:00:00`. The `totalClicks` endpoint expects ISO dates, for example `2026-10-01`. A successful delete returns `204 No Content`; an unknown short code returns `404 Not Found` on redirect.

### API documentation

When the application is running, open Swagger UI at `http://localhost:8085/swagger-ui/index.html`. The OpenAPI document is available at `http://localhost:8085/v3/api-docs`.

## Docker

Build the image from the repository root:

```powershell
docker build -t snipurl .
```

Run it with the required environment variables configured in your environment:

```powershell
docker run --rm -p 8085:8085 `
  -e DATABASE_URL `
  -e DATABASE_USERNAME `
  -e DATABASE_PASSWORD `
  -e DATABASE_DIALECT `
  -e JWT_SECRET `
  -e FRONTEND_URL `
  snipurl
```

The default Dockerfile builds and runs with Java 21. The separate `Dockerfile.vercel` configures the server port from the `PORT` environment variable for that deployment target.
