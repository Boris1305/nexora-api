ALTER TABLE payments
DROP CONSTRAINT chk_payments_status;

ALTER TABLE payments
ADD CONSTRAINT chk_payments_status
CHECK (status IN (
    'PENDING',
    'SUBMITTED',
    'APPROVED',
    'REJECTED',
    'EXPIRED',
    'CANCELLED'
));

ALTER TABLE appointments
ADD COLUMN cancelled_at TIMESTAMP WITH TIME ZONE;