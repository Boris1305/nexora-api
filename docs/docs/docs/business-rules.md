# Nexora API — Business Rules

## 1. Appointment scheduling

### BR-01 — No overlapping appointments

A professional cannot have two active appointments that overlap in time.

The system must validate that the requested time slot does not conflict with another active appointment belonging to the same professional.

Overlap condition:

`startA < endB && endA > startB`

### BR-02 — Valid appointment duration

An appointment must have a duration greater than zero.

The appointment duration and price must be preserved as historical values at booking time.

### BR-03 — Valid client and service

An appointment can only be created when:

- The client exists and is active.
- The service exists and is active.
- Both belong to the same professional.

### BR-04 — Future appointments

New appointments and rescheduled appointments must have a future date and time.

## 2. Payment rules

### BR-05 — Payment deadline

An appointment has a payment deadline of 48 hours from its creation.

### BR-06 — Payment receipt

A payment cannot be approved without a receipt.

### BR-07 — Payment review

A payment can be approved or rejected by the professional.

A rejected payment must include a rejection reason.

### BR-08 — Payment history

Payment records must be preserved.

The system must not overwrite or delete payment history when an appointment is cancelled or rescheduled.

## 3. Appointment status rules

The appointment statuses are:

- PENDING_PAYMENT
- CONFIRMED
- COMPLETED
- CANCELLED
- RESCHEDULED
- EXPIRED

Only PENDING_PAYMENT and CONFIRMED appointments block a professional's schedule.

COMPLETED, CANCELLED, RESCHEDULED and EXPIRED appointments do not block new bookings.

## 4. Cancellation and rescheduling

### BR-09 — Cancellation

A cancelled appointment cannot be modified or completed.

### BR-10 — Rescheduling

Rescheduling must preserve the relationship between the original appointment and the new appointment.

The original appointment must be marked as RESCHEDULED only if the new appointment is successfully created.

The operation must be transactional.

### BR-11 — Payment preservation during rescheduling

If the original appointment has an approved payment, the system must preserve that payment and correctly account for it when handling the new appointment.

The payment must never be duplicated or lost.

The exact technical mechanism for transferring or applying an existing payment will be defined before implementation.

## 5. Client and service management

### BR-12 — Soft deletion

Clients and services must be deactivated instead of physically deleted.

Historical appointments must remain available.

## 6. Data validation

- Monetary values must be nonnegative.
- Service duration must be greater than zero.
- Email addresses must have a valid format.
- Required fields cannot be empty.
- Each professional must have a unique email address.

## 7. Technical consistency

- Use BigDecimal for monetary values.
- Use UUID identifiers.
- Use transactional operations for rescheduling.
- Return consistent HTTP error responses.
- Preserve historical information.

## 8. Current status

Initial business rules documented. These rules will guide the Java implementation and automated tests.
