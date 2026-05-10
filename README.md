# Coupon API

REST API for coupon management built with Spring Boot 3 and Java 17. Implements the technical challenge proposed by [Outforce](https://outforce.com.br/).

## Tech Stack

- **Java 17** (Eclipse Temurin)
- **Spring Boot 3.5** — Web, Data JPA, Validation, DevTools
- **H2** in-memory database
- **Maven** build tool
- **JUnit 5 + Mockito + AssertJ** testing
- **springdoc-openapi** for Swagger UI
- **Docker + Docker Compose**

## Architecture

The project follows a pragmatic layered architecture with a clear separation between **business rules (domain)** and **persistence (JPA entity)**:

```
com.brunorusciolelli.coupon
├── domain/entity      # Pure domain — business rules live here, no framework
├── controller         # HTTP layer
├── service            # Use cases / orchestration
├── repository         # JPA entity, Spring Data repository, mapper
├── dto                # Request/Response payloads
├── exception          # Custom exceptions and global handler
└── config             # OpenAPI configuration
```

The `Coupon` domain class encapsulates...

## Business Rules

### Create
- `code`, `description`, `discountValue`, `expirationDate` are required
- `code` must be 6 alphanumeric characters; special characters are accepted on input but stripped before persistence
- `discountValue` minimum is `0.5`, no upper limit
- `expirationDate` cannot be in the past
- A coupon may be created already published

### Delete
- Soft delete only (status changes to `DELETED`, no row is removed)
- Already deleted coupons cannot be deleted again

## Getting Started

### Prerequisites

- Java 17
- Maven 3.9+ (or use the Maven Wrapper bundled with the project)
- Docker + Docker Compose (only if running with Docker)

### Run locally

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

### Run with Docker

```bash
docker compose up --build
```

To stop:

```bash
docker compose down
```

## API Documentation

Once running, Swagger UI is available at:

**http://localhost:8080/swagger-ui/index.html**

### Endpoints

| Method | Path             | Description           | Status codes        |
|--------|------------------|-----------------------|---------------------|
| POST   | `/coupon`        | Create a new coupon   | 201, 400            |
| DELETE | `/coupon/{id}`   | Soft delete a coupon  | 204, 400, 404       |

### Sample request

```bash
curl -X POST http://localhost:8080/coupon \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ABC-123",
    "description": "Black Friday discount",
    "discountValue": 0.8,
    "expirationDate": "2027-01-01T00:00:00Z",
    "published": true
  }'
```

### Sample response

```json
{
  "id": "cef9d1e3-aae5-4ab6-a297-358c6032b1e7",
  "code": "ABC123",
  "description": "Black Friday discount",
  "discountValue": 0.8,
  "expirationDate": "2027-01-01T00:00:00Z",
  "status": "ACTIVE",
  "published": true,
  "redeemed": false
}
```

## Testing

```bash
./mvnw test
```

The suite covers:
- **Unit tests** for the `Coupon` domain (business rules)
- **Unit tests** for `CouponMapper` (round-trip mapping)
- **Unit tests** for `CouponService` (with Mockito)
- **Integration tests** for `CouponController` (full Spring context with H2)

## Author

Bruno Rusciolelli — [GitHub](https://github.com/brunorusciolelli)