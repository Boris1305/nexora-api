ALTER TABLE appointments
DROP CONSTRAINT chk_appointments_status;

ALTER TABLE appointments
ADD CONSTRAINT chk_appointments_status
CHECK (status IN (
    'PENDING',
    'CONFIRMED',
    'RESCHEDULE_ELIGIBLE',
    'CANCELLED',
    'LOST',
    'COMPLETED',
    'EXPIRED'
));