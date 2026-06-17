# Online Bookshop API

A RESTful back-end for an online bookshop, built with Java and Spring Boot as part of a university course. The system manages books, authors, genres, orders, and reviews, and exposes a fully documented API testable via Swagger UI.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot |
| Database | PostgreSQL |
| Security | Spring Security + JWT |
| API Docs | Swagger UI (OpenAPI 3.1) |
| Testing | JUnit 5, Mockito, MockMvc |

## Getting Started

### Prerequisites
- Java 17+
- PostgreSQL running locally
- Maven

### Run

```bash
# Clone the repo
git clone https://github.com/tpwrzz/online-bookshop.git
cd online-bookshop

# Configure DB credentials in
# src/main/resources/application.properties

# Build and run
./mvnw spring-boot:run
```

Swagger UI will be available at:
[localhost](http://localhost:8080/swagger-ui/index.html)

## Project Structure

```
src/main/java/com/online/bookshop/
├── api/
│   └── controller/        # REST controllers
├── application/
│   └── service/           # Business logic
├── domain/
│   ├── model/             # Domain models
│   └── repository/        # Repository interfaces
└── infrastructure/
    ├── mapper/            # Entity ↔ Domain mappers
    ├── persistence/       # JPA entities
    └── repository/        # Repository implementations
```

## API Overview

| Resource | Endpoints |
|---|---|
| Auth | `POST /api/auth/register`, `/login`, `/refresh`, `/logout` |
| Books | `GET /books`, `/books/{id}`, `/books/search`, `/books/byAuthor`, `/books/byGenre` |
| Genres | `GET /genres`, `/genres/{id}`, `/genres/search` |
| Persons | `GET /persons`, `/persons/{id}`, `/persons/search` |
| Orders | `GET /orders`, `/orders/{id}`, `/orders/byUser/{userId}` |
| Order Items | `GET /order-items`, `/order-items/order/{orderId}` |
| Reviews | `GET /reviews`, `/reviews/byBook/{bookId}`, `/reviews/byUser/{userId}` |
| Users | `GET /users/me`, `/users/{id}` |

Full request/response schemas are available in Swagger UI.

## Domain Model

**Book** — title, author, genre, language, price, currency, availability, page count, average rating, reviews

**User** — username, email, status, registration date, linked to a Person

**Person** — first name, middle name, last name, address, phone, birth date

**Order** — ship address, date, status (`NEW → COORDINATED → INDELIVERY → DELIVERED`), user, items

**OrderItem** — book, quantity, unit price, linked to an order

**Review** — user, book, message, rating (1–5)

## Testing

The project has unit and integration tests covering the service and controller layers for all main resources.

| Layer | Tool | Coverage |
|---|---|---|
| Service | JUnit 5 + Mockito | Books, Genres, Persons, Orders, OrderItems, Reviews, Auth, JWT |
| Controller | MockMvc + Mockito | Books, Genres, Persons, Orders, OrderItems, Reviews, Users |

Controller tests verify HTTP status codes (200, 201, 204, 400, 401, 403, 404, 409) and response body structure. Service tests mock the repository layer and cover happy paths, not-found cases, and business rule violations (duplicate entries, invalid input).

```bash
./mvnw test
```

## Status

> **In active development** — core CRUD and auth are implemented; cart and advanced filtering are planned.

## License

MIT
