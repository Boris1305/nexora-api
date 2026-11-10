
# Nexora — Professional Services Management Platform

Nexora is a REST API for independent professionals and small businesses to manage clients, services, in-person and online appointments, and payment tracking with receipt, with real business rules: no overlapping appointments, rescheduling with traceability, and 48-hour payment deadline.

## About the Project

Nexora is a backend application designed to help independent professionals and small businesses organize their daily operations through a centralized system.

The project focuses on managing the relationship between clients, services, appointments, and payments, implementing business rules that reflect real-world professional service workflows.

The main goal is to build a maintainable REST API with clear business logic, data validation, persistence, and traceability.

## Main Features

### Client Management
- Client registration and management.
- Client information and contact details.
- Active and inactive client status.

### Service Management
- Service registration and management.
- Service name and description.
- Price and duration configuration.
- Active and inactive service status.

### Appointment Management
- Appointment creation associated with a client and a service.
- Appointment start and end time calculation.
- Automatic duration and price assignment based on the selected service.
- Appointment status management.
- Appointment cancellation.
- Appointment rescheduling.
- Rescheduling history for traceability.
- Prevention of overlapping appointments.

### Payment Management
- Automatic payment creation for each appointment.
- Payment deadline of 48 hours.
- Receipt reference submission.
- Receipt approval and rejection.
- Receipt resubmission after rejection.
- Payment status tracking.
- Automatic expiration of unpaid appointments and payments.

## Business Rules

Nexora implements business rules designed to maintain consistency between appointments and payments.

### Appointment Rules

- Appointments cannot be created for past dates.
- Inactive clients cannot book appointments.
- Inactive services cannot be booked.
- Overlapping appointments are not allowed.
- Only appointments with `PENDING` or `CONFIRMED` status can be cancelled.
- Appointments can be rescheduled only when their current status allows it.
- Rescheduling retains the original appointment ID.
- Every rescheduling operation is recorded in the appointment history.
- Cancelled appointments release their reserved time slot.

### Payment Rules

- Each appointment has an associated payment.
- Payments have a 48-hour submission deadline.
- Payment receipts can be submitted for professional review.
- Only `PENDING` or `REJECTED` payments can receive a new receipt.
- Rejected receipts can be resubmitted before the payment deadline, provided the appointment remains pending.
- Only submitted receipts can be approved or rejected.
- Approved payments confirm appointments that are still pending.
- Submitted receipts remain available for review even if the appointment is cancelled.
- Cancelling an unpaid appointment also cancels its payment.
- Submitted payments are not automatically expired while awaiting review.

### Cancellation and Rescheduling Rules

The system distinguishes between appointment status and payment status.

**1. Cancellation with an approved payment — at least 48 hours before the appointment**

- The appointment becomes `RESCHEDULE_ELIGIBLE`.
- The payment remains `APPROVED`.
- The client can reschedule without making another payment.

**2. Cancellation with an approved payment — less than 48 hours before the appointment**

- The appointment becomes `LOST`.
- The payment remains `APPROVED`.
- The appointment cannot be rescheduled.

**3. Cancellation with a pending or rejected payment**

- The appointment becomes `CANCELLED`.
- The payment becomes `CANCELLED`.
- The reserved time slot is released.

**4. Cancellation with a submitted receipt**

- The appointment becomes `CANCELLED`.
- The payment remains `SUBMITTED`.
- The receipt is retained for professional review.
- If approved, the appointment becomes `RESCHEDULE_ELIGIBLE` or `LOST`, depending on the cancellation time.
- If rejected, the payment becomes `REJECTED` and the appointment remains cancelled.

## Appointment Statuses

| Status | Description |
|---|---|
| `PENDING` | Appointment created, awaiting payment confirmation. |
| `CONFIRMED` | Payment approved and appointment confirmed. |
| `RESCHEDULE_ELIGIBLE` | Approved payment with cancellation at least 48 hours before the appointment. |
| `CANCELLED` | Appointment cancelled. |
| `LOST` | Approved appointment cancelled less than 48 hours before its scheduled time. |
| `COMPLETED` | Appointment completed. |
| `EXPIRED` | Appointment expired due to an unpaid payment. |

## Payment Statuses

| Status | Description |
|---|---|
| `PENDING` | Payment created, awaiting receipt submission. |
| `SUBMITTED` | Receipt submitted and awaiting review. |
| `APPROVED` | Payment receipt approved. |
| `REJECTED` | Payment receipt rejected. |
| `EXPIRED` | Payment submission deadline exceeded. |
| `CANCELLED` | Payment cancelled because the appointment was cancelled while unpaid. |

## Technology Stack

- **Java 21** — Main programming language.
- **Spring Boot 4.1.2** — Backend application framework.
- **Spring Web** — REST API development.
- **Spring Data JPA** — Database persistence and ORM.
- **PostgreSQL** — Relational database.
- **Hibernate** — ORM implementation.
- **Flyway** — Database versioning and migrations.
- **Maven** — Dependency management and build automation.
- **Jakarta Bean Validation** — Request validation.
- **Postman** — API testing.
- **Git and GitHub** — Version control and project management.

## API Structure

The application follows a layered architecture, separating HTTP handling, business logic, data persistence, and domain entities.

Main components:

- `controller` — REST endpoints and HTTP request handling.
- `service` — Business rules and application logic.
- `repository` — Database access.
- `entity` — Database entities.
- `dto` — Request and response data transfer objects.
- `enums` — Appointment and payment status definitions.

## Main API Endpoints

### Services

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/services` | Retrieve services. |
| `POST` | `/api/services` | Create a service. |

### Appointments

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/appointments` | Retrieve appointments. |
| `GET` | `/api/appointments/{id}` | Retrieve an appointment. |
| `POST` | `/api/appointments` | Create an appointment. |
| `PATCH` | `/api/appointments/{id}/cancel` | Cancel an appointment. |
| `PATCH` | `/api/appointments/{id}/reschedule` | Reschedule an appointment. |
| `GET` | `/api/appointments/{id}/history` | Retrieve rescheduling history. |

### Payments

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/payments/{id}` | Retrieve a payment. |
| `GET` | `/api/payments/appointment/{appointmentId}` | Retrieve payment by appointment. |
| `POST` | `/api/payments/{id}/receipt` | Submit or resubmit a receipt. |
| `PATCH` | `/api/payments/{id}/approve` | Approve a submitted payment. |
| `PATCH` | `/api/payments/{id}/reject` | Reject a submitted payment. |

## Database Management

Nexora uses PostgreSQL for relational data persistence.

Flyway manages database schema changes through versioned SQL migrations.

This approach keeps database changes organized and reproducible across development environments.

## Running the Project Locally

### Requirements

- Java 21 or compatible JDK.
- PostgreSQL.
- Git.
- Maven Wrapper included in the repository.

### Run the Application

Clone the repository:

```bash
git clone <repository-url>
```

Navigate to the project directory and configure the database connection in the application configuration.

On Windows, run:

```powershell
.\mvnw.cmd spring-boot:run
```

On Linux or macOS, run:

```bash
./mvnw spring-boot:run
```

The application starts on port `8080` by default.

### Compile the Project

On Windows:

```powershell
.\mvnw.cmd clean compile
```

On Linux or macOS:

```bash
./mvnw clean compile
```

## Testing and Validation

The API has been manually tested using Postman.

Validated scenarios include:

- Service creation and updates.
- Appointment creation.
- Prevention of overlapping appointments.
- Appointment cancellation.
- Payment receipt submission.
- Payment approval and rejection.
- Receipt resubmission after rejection.
- Automatic appointment confirmation after payment approval.
- Appointment expiration rules.
- Cancellation with approved payments.
- Rescheduling eligibility based on the 48-hour rule.
- Rescheduling history and traceability.
- Receipt review after appointment cancellation.
- Prevention of rescheduling appointments with `LOST` status.

The project compiles successfully with Maven.

Automated unit and integration test coverage is part of the next development stage.

## Development Roadmap

### Version 1 — Core Backend

- [x] Client management.
- [x] Service management.
- [x] Appointment management.
- [x] Appointment cancellation.
- [x] Appointment rescheduling.
- [x] Appointment history.
- [x] Payment management.
- [x] Receipt submission and review.
- [x] Payment deadline and expiration.
- [x] Appointment and payment business rules.
- [x] Database migrations with Flyway.
- [ ] Automated unit tests.
- [ ] Integration tests.
- [ ] API documentation with Swagger / OpenAPI.

### Version 1.1 — Extended Features

- [ ] Dashboard and business metrics.
- [ ] JWT authentication and authorization.
- [ ] Automatic unpaid appointment expiration improvements.
- [ ] Deployment to a production environment.

## Project Goals

Nexora is both a practical backend development project and a portfolio application.

It demonstrates the implementation of:

- Object-Oriented Programming.
- REST API design.
- Layered architecture.
- Relational database modeling.
- Spring Boot application development.
- Data validation and error handling.
- Business rule implementation.
- Transaction management.
- Database migrations.
- Appointment and payment lifecycle management.

The project is being developed incrementally, prioritizing maintainability, consistency, and realistic business requirements.
