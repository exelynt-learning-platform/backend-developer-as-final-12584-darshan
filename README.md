# Resource Booking System

A RESTful backend API for managing bookable resources (rooms, vehicles, equipment) and reservations, built with Spring Boot and secured with JWT-based stateless authentication and role-based access control.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Environment Variables](#environment-variables)
- [Database Setup](#database-setup)
- [Running the Application](#running-the-application)
- [Seed Users](#seed-users)
- [API Documentation](#api-documentation)
- [API Endpoints](#api-endpoints)
- [Filtering, Pagination and Sorting](#filtering-pagination-and-sorting)
- [Error Responses](#error-responses)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)
- [Security Notes](#security-notes)

---

## Features

- JWT-based stateless authentication with BCrypt password hashing
- Role-based access control with `ADMIN` and `USER` roles
- Full CRUD on resources and reservations for `ADMIN`
- Read-only resource access for `USER`
- Reservation ownership enforcement — a `USER` can only access their own reservations
- User identity always resolved from the JWT, never from the request body
- Reservation statuses: `PENDING`, `CONFIRMED`, `CANCELLED`
- Reservation price stored as a decimal value and calculated from resource rate and booking duration
- Reservation filtering by status, minimum price and maximum price
- Pagination and optional sorting on all list endpoints
- Bean validation with consistent JSON error responses
- Interactive API documentation via Swagger / OpenAPI
- User registration with automatic `USER` role assignment
- USER can confirm their own reservations
- USER can cancel their own reservations
- Global exception handling
- Custom `401 Unauthorized` and `403 Forbidden` responses
- 43 unit and integration tests focused on security and authorization

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot |
| Security | Spring Security + JJWT |
| Persistence | Spring Data JPA / Hibernate |
| Database | MySQL |
| Test Database | H2 in-memory |
| Build Tool | Maven |
| Documentation | springdoc-openapi / Swagger UI |
| Testing | JUnit 5, Mockito, MockMvc |

---

## Prerequisites

- JDK 17 or higher
- MySQL 8.0 or higher
- Maven 3.8+ or use the bundled Maven Wrapper
- IntelliJ IDEA or another Java IDE

---

## Environment Variables

All sensitive configuration is externalized.

Set the following environment variables before running the application.

| Variable | Required | Default | Description |
|---|---|---|---|
| `DB_URL` | No | `jdbc:mysql://localhost:3306/resource_booking_db` | JDBC connection URL |
| `DB_USERNAME` | No | `root` | MySQL username |
| `DB_PASSWORD` | Yes | — | MySQL password |
| `JWT_SECRET` | Yes | — | Secret key used to sign JWTs |
| `JWT_EXPIRATION` | No | `86400000` | Token validity in milliseconds (24 hours) |

### Windows PowerShell

```powershell
$env:DB_PASSWORD = "your_mysql_password"
$env:JWT_SECRET = "your_strong_jwt_secret_key"
```

### macOS / Linux

```bash
export DB_PASSWORD="your_mysql_password"
export JWT_SECRET="your_strong_jwt_secret_key"
```

> Do not commit database passwords or JWT secrets to GitHub.

---

## Database Setup

Create the database in MySQL:

```sql
CREATE DATABASE resource_booking_db;
```

The application uses Hibernate with `ddl-auto=update`, so the required tables are created and updated automatically when the application starts.

No manual schema creation or seed SQL is required.

---

## Running the Application

From the project root:

### Windows

```powershell
.\mvnw.cmd spring-boot:run
```

### macOS / Linux

```bash
./mvnw spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

---

## Seed Users

Two accounts are seeded automatically when the application starts.

Seeding is idempotent, so existing users are not overwritten.

| Role | Username | Password |
|---|---|---|
| ADMIN | `admin` | `admin123` |
| USER | `user` | `user123` |

> These credentials are intended for local development and testing. Change them for production use.

---

## API Documentation

Interactive Swagger UI is available once the application is running:

```text
http://localhost:8080/swagger-ui/index.html
```

### Testing Protected Endpoints

1. Call `POST /auth/login`.
2. Copy the returned JWT token.
3. Click the **Authorize** button in Swagger UI.
4. Enter:

```text
Bearer <your-jwt-token>
```

5. Click **Authorize**.
6. Protected API requests will now include the JWT token.

The raw OpenAPI specification is available at:

```text
http://localhost:8080/v3/api-docs
```

---

# API Endpoints

## Authentication

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/auth/login` | Public | Authenticate and receive a JWT |
| `POST` | `/auth/register` | Public | Register a new account |

### Register Request

```json
{
  "username": "john",
  "email": "john@gmail.com",
  "password": "john123"
}
```

Newly registered users are automatically assigned the `USER` role.

### Login Request

```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Login Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

Use the returned token in protected requests:

```text
Authorization: Bearer <token>
```

---

## Resources

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/resources` | Authenticated | List resources with pagination |
| `GET` | `/resources/{id}` | Authenticated | Get a single resource |
| `POST` | `/resources` | ADMIN | Create a resource |
| `PUT` | `/resources/{id}` | ADMIN | Update a resource |
| `DELETE` | `/resources/{id}` | ADMIN | Delete a resource |

### Create Resource Request

```json
{
  "name": "Conference Room A",
  "type": "ROOM",
  "description": "Seats 12, projector included",
  "pricePerUnit": 250.00,
  "available": true
}
```

---

## Reservations

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/reservations` | Authenticated | Create reservation for logged-in user |
| `GET` | `/reservations` | Authenticated | List logged-in user's reservations |
| `GET` | `/reservations/{id}` | Owner / ADMIN | Get a reservation |
| `GET` | `/reservations/all` | ADMIN | List all reservations |
| `PUT` | `/reservations/{id}` | ADMIN | Update a reservation |
| `PUT` | `/reservations/{id}/confirm` | Owner / ADMIN | Confirm a reservation |
| `PUT` | `/reservations/{id}/cancel` | Owner / ADMIN | Cancel a reservation |
| `DELETE` | `/reservations/{id}` | ADMIN | Delete a reservation |

### Create Reservation Request

```json
{
  "resourceId": 1,
  "startTime": "2026-12-01T10:00:00",
  "endTime": "2026-12-01T14:00:00"
}
```

The reservation owner is always taken from the authenticated JWT.

There is no `userId` field in the request body, so ownership cannot be spoofed.

New reservations are created with status:

```text
PENDING
```

The `totalPrice` is calculated from the resource rate and booking duration.

### Reservation Response

```json
{
  "id": 1,
  "userId": 2,
  "username": "user",
  "resourceId": 1,
  "resourceName": "Conference Room A",
  "startTime": "2026-12-01T10:00:00",
  "endTime": "2026-12-01T14:00:00",
  "status": "PENDING",
  "totalPrice": 1000.00
}
```

---

# Filtering, Pagination and Sorting

## Reservation Filtering

Reservation list endpoints support the following optional query parameters:

| Parameter | Type | Example | Description |
|---|---|---|---|
| `status` | enum | `PENDING` | Filter by reservation status |
| `minPrice` | decimal | `100.00` | Minimum reservation price |
| `maxPrice` | decimal | `500.00` | Maximum reservation price |
| `page` | integer | `0` | Zero-based page number |
| `size` | integer | `10` | Number of items per page |
| `sort` | string | `totalPrice,desc` | Sort field and direction |

### Filter by Status

```http
GET /reservations/all?status=CONFIRMED
```

### Filter by Price

```http
GET /reservations/all?minPrice=1000&maxPrice=2000
```

### Combine Filters

```http
GET /reservations/all?status=CONFIRMED&minPrice=1000&maxPrice=2000
```

For a `USER`, reservation queries are always scoped to that user's own reservations.

---

## Reservation Pagination

Example:

```http
GET /reservations/all?page=0&size=10
```

Pages are zero-based:

```text
page=0 → first page
page=1 → second page
```

---

## Reservation Sorting

Sorting uses:

```text
sort=property,direction
```

Example:

```http
GET /reservations/all?sort=totalPrice,desc
```

Multiple sorting criteria can be supplied when required.

---

## Resource Pagination

The `GET /resources` endpoint also supports pagination and sorting.

Example:

```http
GET /resources?page=0&size=10
```

Optional sorting:

```http
GET /resources?page=0&size=10&sort=name,asc
```

Pages are zero-based.

---

# Reservation Status

Reservations support three statuses:

```text
PENDING
CONFIRMED
CANCELLED
```

A newly created reservation starts with:

```text
PENDING
```

A reservation owner can:

- Confirm their own reservation
- Cancel their own reservation

An ADMIN can manage reservations according to their role permissions.

---

# Reservation Price Calculation

Reservation price is calculated using:

```text
Reservation Duration × Resource Price Per Unit
```

Duration is calculated using:

```text
startTime
endTime
```

The calculated price is stored using `BigDecimal`.

### Example

```text
Resource price = 600 per hour
Duration       = 2 hours

Total price    = 1200
```

---

# Authorization

## ADMIN

ADMIN users can:

- Create resources
- View resources
- Update resources
- Delete resources
- View all reservations
- Filter reservations
- Paginate reservations
- Sort reservations
- Update reservations
- Delete reservations
- Cancel reservations

## USER

USER users can:

- Register their own account
- Login
- View resources
- Create reservations
- View only their own reservations
- View their own reservation by ID
- Confirm their own reservations
- Cancel their own reservations

USER users cannot:

- Create resources
- Update resources
- Delete resources
- View another user's reservation
- Update reservations through the ADMIN update endpoint
- Delete reservations

---

# Validation

Request validation is implemented using Jakarta Bean Validation.

Validation includes:

- Required username
- Required email
- Valid email format
- Required password
- Required resource ID
- Required start time
- Required end time
- Future reservation times
- Positive resource price
- Required reservation status

Invalid requests return:

```text
400 Bad Request
```

---

# Error Responses

All errors are handled centrally using global exception handling.

Common HTTP responses:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
500 Internal Server Error
```

### Example Error Response

```json
{
  "timestamp": "2026-09-15T10:30:00",
  "status": 403,
  "error": "Forbidden",
  "message": "You are not allowed to access this reservation",
  "path": "/reservations/5"
}
```

| Status | When it occurs |
|---|---|
| `400` | Validation failure, malformed JSON, invalid parameter type, or business rule violation |
| `401` | Missing, invalid or expired JWT, or incorrect login credentials |
| `403` | Authenticated but not authorized to access the requested resource |
| `404` | Requested user, resource or reservation does not exist |
| `500` | Unexpected server error |

---

# Security

The application uses:

- Spring Security
- JWT authentication
- BCrypt password hashing
- Stateless session management
- Role-based authorization
- Method-level authorization using `@PreAuthorize`
- Custom authentication entry point
- Custom access denied handler

JWT authentication is required for protected API endpoints.

### Authentication Failure

```text
401 Unauthorized
```

### Authorization Failure

```text
403 Forbidden
```

### Ownership Protection

A USER can only access their own reservations.

The authenticated user's identity is obtained from the JWT rather than from the request body.

---

# Running Tests

The project contains automated unit, integration, controller, validation, and security tests.

Tests use an in-memory H2 database and do not require MySQL.

### Windows

```powershell
.\mvnw.cmd clean test
```

### macOS / Linux

```bash
./mvnw clean test
```

### Current Test Result

```text
Tests run: 43
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

---

## Test Coverage

The test suite covers:

- Authentication and login
- User registration
- JWT security
- ADMIN authorization
- USER authorization
- Resource RBAC
- Reservation ownership
- Reservation cancellation
- Reservation confirmation
- Reservation validation
- Reservation filtering
- Reservation pagination
- Reservation sorting
- Controller security
- Service logic
- Unauthenticated access

Security and authorization are a primary focus of the test suite.

---

# Security Test Scenarios

The application verifies scenarios including:

- Valid JWT authentication → `200`
- Missing JWT → `401 Unauthorized`
- Invalid JWT → `401 Unauthorized`
- USER accessing another user's reservation → `403 Forbidden`
- USER attempting resource creation → `403 Forbidden`
- USER attempting resource update → `403 Forbidden`
- USER attempting resource deletion → `403 Forbidden`
- USER attempting reservation update → `403 Forbidden`
- USER attempting reservation deletion → `403 Forbidden`
- ADMIN accessing all reservations → `200`
- USER accessing own reservations → `200`
- Invalid reservation request → `400 Bad Request`
- Past reservation start time → `400 Bad Request`

---

# Project Structure

```text
src/main/java/com/example/resource_booking_system/
├── config/
│   ├── SecurityConfig
│   ├── OpenApiConfig
│   ├── PasswordConfig
│   └── DataSeeder
│
├── controller/
│   ├── AuthController
│   ├── ResourceController
│   └── ReservationController
│
├── dto/
│   ├── auth/
│   │   ├── LoginRequest
│   │   ├── LoginResponse
│   │   └── RegisterRequest
│   │
│   ├── resource/
│   │   ├── ResourceRequest
│   │   └── ResourceResponse
│   │
│   └── reservation/
│       ├── ReservationRequest
│       ├── ReservationUpdateRequest
│       └── ReservationResponse
│
├── entity/
│   ├── User
│   ├── Resource
│   └── Reservation
│
├── enums/
│   ├── Role
│   └── ReservationStatus
│
├── exception/
│   ├── GlobalExceptionHandler
│   └── Custom Exceptions
│
├── repository/
│   ├── UserRepository
│   ├── ResourceRepository
│   ├── ReservationRepository
│   └── ReservationSpecification
│
├── security/
│   ├── JwtService
│   ├── JwtAuthenticationFilter
│   ├── RestAuthenticationEntryPoint
│   └── RestAccessDeniedHandler
│
└── service/
    ├── AuthService
    ├── ResourceService
    ├── ReservationService
    └── CustomUserDetailsService
```

The application follows a layered architecture:

- **Controllers** handle HTTP requests and responses.
- **Services** contain business logic and authorization checks.
- **Repositories** handle database access.
- **DTOs** define the API request and response contracts.
- **Entities** represent database records.
- **Security** handles JWT authentication and authorization.
- **Exceptions** provide centralized error handling.

---

# Application Endpoints Summary

## Authentication

```text
POST /auth/register
POST /auth/login
```

## Resources

```text
GET    /resources
GET    /resources/{id}
POST   /resources
PUT    /resources/{id}
DELETE /resources/{id}
```

## Reservations

```text
POST   /reservations
GET    /reservations
GET    /reservations/{id}
GET    /reservations/all
PUT    /reservations/{id}
PUT    /reservations/{id}/confirm
PUT    /reservations/{id}/cancel
DELETE /reservations/{id}
```

---

# Security Notes

- Passwords are hashed using BCrypt.
- Passwords are never returned in API responses.
- Authentication is stateless.
- No server-side HTTP session is used for authentication.
- Database credentials are supplied through environment variables.
- The JWT signing secret is supplied through an environment variable.
- Secrets should not be committed to the repository.
- JWT authentication protects all non-public API endpoints.
- Role-based authorization prevents unauthorized ADMIN operations.
- Reservation ownership is enforced using the authenticated user's identity.
- USER reservation queries are scoped to the authenticated user.
- Request validation prevents invalid reservation and resource data.

---

# License

This project was developed as part of a backend development assignment.