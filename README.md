# Resource Booking System

A RESTful Resource Booking System built using Spring Boot, Spring Security, JWT authentication, Spring Data JPA, Hibernate, and MySQL.

The system provides role-based access control with two roles:

- `ADMIN`
- `USER`

Users can register their own accounts, while ADMIN accounts are controlled by the system.

---

## Features

- JWT-based authentication
- User registration
- Role-based authorization
- ADMIN and USER roles
- Resource management
- Reservation management
- Reservation ownership
- Reservation status management
- USER can confirm their own reservations
- USER can cancel their own reservations
- ADMIN can manage all reservations
- Reservation price calculation
- Reservation filtering by status and price
- Pagination
- Sorting
- Request validation
- Global exception handling
- Custom `401 Unauthorized` JSON responses
- Custom `403 Forbidden` JSON responses
- MySQL database integration
- Automatic database table creation using Hibernate
- Swagger/OpenAPI documentation
- Seeded ADMIN and USER accounts
- Automated security, controller, and service tests

---

## Technologies Used

- Java 17+
- Spring Boot 4.1.1
- Spring Security 7.1.1
- Spring Data JPA
- Hibernate
- MySQL
- JWT
- JJWT 0.12.6
- Springdoc OpenAPI
- Maven Wrapper
- JUnit
- Mockito
- MockMvc
- Lombok

---

## Prerequisites

Make sure the following are installed:

- Java 17 or higher
- MySQL
- IntelliJ IDEA or another Java IDE

The project includes the Maven Wrapper, so Maven does not need to be installed separately.

---

## Database Setup

The application uses MySQL.

Create the database in MySQL:

```sql
CREATE DATABASE resource_booking_db;
```

The application will automatically create and update the required tables using Hibernate.

---

## Configuration

The application supports environment variables for database and JWT configuration.

### Environment Variables

| Variable | Description | Default |
|---|---|---|
| `DB_URL` | MySQL database URL | `jdbc:mysql://localhost:3306/resource_booking_db` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | Configure according to your local MySQL setup |
| `JWT_SECRET` | Secret key used to sign JWTs | Application default |
| `JWT_EXPIRATION` | JWT expiration time in milliseconds | `86400000` |

For production environments, it is recommended to provide these values through environment variables instead of storing credentials directly in configuration files.

---

## How to Run

### 1. Open the project

Open the project in IntelliJ IDEA or another Java IDE.

### 2. Start MySQL

Make sure MySQL is running.

### 3. Create the database

```sql
CREATE DATABASE resource_booking_db;
```

### 4. Configure database credentials

Update the environment variables or application configuration according to your local MySQL setup.

### 5. Run the application

#### Windows

```bash
.\mvnw.cmd spring-boot:run
```

#### Linux/macOS

```bash
./mvnw spring-boot:run
```

The application will start at:

```text
http://localhost:8080
```

---

## Swagger / OpenAPI

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui/index.html
```

Open Swagger and click **Authorize**.

Enter:

```text
Bearer <your-jwt-token>
```

The JWT token is obtained from the login endpoint.

---

# Authentication

## Register a User

### Endpoint

```http
POST /auth/register
```

### Request

```json
{
  "username": "john",
  "email": "john@gmail.com",
  "password": "john123"
}
```

### Response

```text
User registered successfully
```

Newly registered users are automatically assigned the `USER` role.

---

## Login

### Endpoint

```http
POST /auth/login
```

### Request

```json
{
  "username": "john",
  "password": "john123"
}
```

### Response

```json
{
  "token": "<JWT_TOKEN>",
  "username": "john",
  "role": "USER"
}
```

Use the returned JWT token for protected API requests.

---

# Default Seeded Accounts

The application creates default accounts when the application starts.

## ADMIN

```text
Username: admin
Password: admin123
Role: ADMIN
```

## USER

```text
Username: user
Password: user123
Role: USER
```

---

# Authorization and Roles

## ADMIN Permissions

ADMIN users can:

- Create resources
- View resources
- Update resources
- Delete resources
- View all reservations
- Filter all reservations
- Paginate reservations
- Sort reservations
- Update reservations
- Delete reservations
- Cancel reservations

---

## USER Permissions

USER users can:

- View resources
- Register their own account
- Login
- Create reservations
- View only their own reservations
- View their own reservation by ID
- Confirm their own reservations
- Cancel their own reservations

USER users cannot:

- Create resources
- Update resources
- Delete resources
- View other users' reservations
- Update reservations through the ADMIN update endpoint
- Delete reservations

---

# Resource API

| Method | Endpoint | Access |
|---|---|---|
| `GET` | `/resources` | Authenticated users |
| `GET` | `/resources/{id}` | Authenticated users |
| `POST` | `/resources` | ADMIN |
| `PUT` | `/resources/{id}` | ADMIN |
| `DELETE` | `/resources/{id}` | ADMIN |

---

# Reservation API

| Method | Endpoint | Access |
|---|---|---|
| `POST` | `/reservations` | USER / ADMIN |
| `GET` | `/reservations` | USER / ADMIN - own reservations |
| `GET` | `/reservations/{id}` | Owner / ADMIN |
| `GET` | `/reservations/all` | ADMIN |
| `PUT` | `/reservations/{id}` | ADMIN |
| `PUT` | `/reservations/{id}/confirm` | Reservation owner |
| `PUT` | `/reservations/{id}/cancel` | Reservation owner / ADMIN |
| `DELETE` | `/reservations/{id}` | ADMIN |

---

# Reservation Status

Reservations support the following statuses:

```text
PENDING
CONFIRMED
CANCELLED
```

A newly created reservation starts with:

```text
PENDING
```

A USER can confirm or cancel their own reservation.

---

# Reservation Price Calculation

The reservation price is calculated based on:

```text
Reservation Duration × Resource Price Per Unit
```

The duration is calculated from:

```text
startTime
endTime
```

The calculated price is stored as `BigDecimal`.

### Example

```text
Resource price = 600 per hour
Duration       = 2 hours

Total price    = 1200
```

---

# Reservation Filtering

The reservation APIs support filtering using:

- `status`
- `minPrice`
- `maxPrice`

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

---

# Pagination

Reservation APIs support pagination using:

- `page`
- `size`

### Example

```http
GET /reservations/all?page=0&size=10
```

`page` is zero-based.

```text
page=0 → first page
page=1 → second page
```

---

# Sorting

Reservation APIs support sorting using:

```text
sort=property,direction
```

### Example

```http
GET /reservations/all?sort=totalPrice,desc
```

Multiple sorting criteria are supported.

---

# Validation

Request validation is implemented using Jakarta Bean Validation.

Examples of validation include:

- Required username
- Required email
- Valid email format
- Required password
- Required resource ID
- Required start and end times
- Future reservation times
- Positive resource price
- Required reservation status

Invalid requests return an appropriate HTTP `400 Bad Request` response.

---

# Error Handling

The application provides centralized exception handling.

Common responses include:

```text
400 Bad Request
401 Unauthorized
403 Forbidden
404 Not Found
```

Custom JSON error responses are provided for authentication and authorization failures.

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

Authentication failures return:

```text
401 Unauthorized
```

Authorization failures return:

```text
403 Forbidden
```

---

# Project Structure

```text
src
└── main
    └── java
        └── com.example.resource_booking_system
            ├── config
            ├── controller
            ├── dto
            ├── entity
            ├── enums
            ├── exception
            ├── repository
            ├── security
            └── service
```

---

# Testing

The project contains automated tests covering authentication, authorization, validation, services, controllers, and security scenarios.

Run all tests using:

### Windows

```bash
.\mvnw.cmd clean test
```

### Linux/macOS

```bash
./mvnw clean test
```

### Current Test Result

```text
Tests run: 39
Failures: 0
Errors: 0
Skipped: 0
BUILD SUCCESS
```

---

# Security Test Scenarios

The application has been manually verified for the following scenarios:

- Valid JWT authentication → `200`
- Missing JWT → `401 Unauthorized`
- Invalid JWT → `401 Unauthorized`
- USER accessing another user's reservation → `403 Forbidden`
- USER attempting resource creation → `403 Forbidden`
- USER attempting reservation update → `403 Forbidden`
- USER attempting reservation deletion → `403 Forbidden`
- ADMIN accessing all reservations → `200`
- USER accessing own reservations → `200`

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

# License

This project was developed as part of a backend development assignment.