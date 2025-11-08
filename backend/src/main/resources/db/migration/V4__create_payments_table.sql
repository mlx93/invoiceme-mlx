-- V4: Create payments table
-- This migration creates the payments table with foreign keys to invoices, customers, and users

-- Create ENUM types for payments table
CREATE TYPE payment_method_enum AS ENUM ('CREDIT_CARD', 'ACH');
CREATE TYPE payment_status_enum AS ENUM ('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED');

-- Create payments table
CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount DECIMAL(19,2) NOT NULL CHECK (amount > 0),
    payment_method payment_method_enum NOT NULL,
    payment_date DATE NOT NULL,
    payment_reference VARCHAR(100),
    status payment_status_enum NOT NULL DEFAULT 'COMPLETED',
    created_by_user_id UUID,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE RESTRICT,
    CONSTRAINT fk_payments_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT
);

-- Create index on invoice_id for foreign key queries
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);

-- Create index on customer_id for foreign key queries
CREATE INDEX idx_payments_customer_id ON payments(customer_id);

-- Create index on payment_date for date range queries
CREATE INDEX idx_payments_payment_date ON payments(payment_date);

-- Create index on payment_method for filtering
CREATE INDEX idx_payments_payment_method ON payments(payment_method);

-- Create composite index for common query: customer_id + payment_date
CREATE INDEX idx_payments_customer_date ON payments(customer_id, payment_date DESC);

-- Note: created_by_user_id foreign key will be added in V5 after users table is created

