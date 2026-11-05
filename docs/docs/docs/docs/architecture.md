# Nexora API — Technical Architecture

## 1. Architecture overview

Nexora will use a layered architecture based on Spring Boot.

The goal is to separate responsibilities, improve maintainability and make the business logic easier to test.

## 2. Application layers

### Controller Layer

Responsible for receiving HTTP requests and returning HTTP responses.

Responsibilities:
- Define REST endpoints.
- Receive request DTOs.
- Validate incoming data.
- Delegate operations to the Service Layer.
- Return appropriate HTTP status codes.

### Service Layer

Contains the application's business logic.

Responsibilities:
- Apply business rules.
- Coordinate operations between repositories.
- Validate appointment availability.
- Handle cancellations and rescheduling.
- Manage payment workflows.
- Control transactional operations.

### Repository Layer

Responsible for communication with the database.

Responsibilities:
- Persist entities.
- Retrieve records.
- Update information.
- Execute database queries.

Technology: Spring Data JPA.

### Entity Layer

Contains the domain entities mapped to database tables.

Main entities:
- Professional
- Client
- Service
- Appointment
- Payment

### DTO Layer

Data Transfer Objects define the information exchanged through the API.

They separate external request and response structures from internal database entities.

### Exception Layer

Centralizes error handling.

Responsibilities:
- Handle business exceptions.
- Handle resource-not-found errors.
- Handle validation errors.
- Return consistent error responses.

## 3. Request flow

A typical request follows this sequence:

1. The client sends an HTTP request.
2. The Controller receives and validates the request.
3. The Controller calls the appropriate Service.
4. The Service applies business rules.
5. The Repository accesses the database when necessary.
6. The Service returns the result.
7. The Controller generates the HTTP response.

## 4. Proposed project structure

```text
src/
└── main/
    ├── java/
    │   └── com/nexora/api/
    │       ├── controller/
    │       ├── service/
    │       ├── repository/
    │       ├── entity/
    │       ├── dto/
    │       ├── exception/
    │       ├── enums/
    │       └── config/
    │
    └── resources/
        ├── application.properties
        └── db/
            └── migration/

src/test/
└── java/
    └── com/nexora/api/
```

## 5. Technology stack

- Java (LTS version)
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven
- Bean Validation
- JUnit
- Mockito
- Swagger / OpenAPI
- Flyway for database migrations

Exact versions will be selected when the application is initialized.

## 6. Testing strategy

The project will include:

- Unit tests for business rules.
- Service-layer tests.
- Repository tests.
- Integration tests with PostgreSQL.
- API endpoint tests.

Priority scenarios:
- Prevent overlapping appointments.
- Enforce the 48-hour payment deadline.
- Validate appointment status transitions.
- Preserve payment history during rescheduling.

## 7. Development principles

- Separation of concerns.
- Clear naming conventions.
- Reusable and testable business logic.
- Data integrity.
- Consistent error handling.
- Small, meaningful Git commits.

## 8. Current status

Proposed technical architecture. Implementation will begin after the initial design decisions have been reviewed.
