-- V2: Create invoices table
-- This migration creates the invoices table with all required fields, constraints, and foreign keys

-- Create ENUM types for invoices table
CREATE TYPE invoice_status_enum AS ENUM ('DRAFT', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED');
CREATE TYPE payment_terms_enum AS ENUM ('NET_30', 'DUE_ON_RECEIPT', 'CUSTOM');

-- Create invoices table
CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status invoice_status_enum NOT NULL DEFAULT 'DRAFT',
    payment_terms payment_terms_enum NOT NULL DEFAULT 'NET_30',
    subtotal DECIMAL(19,2) NOT NULL DEFAULT 0 CHECK (subtotal >= 0),
    tax_amount DECIMAL(19,2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    discount_amount DECIMAL(19,2) NOT NULL DEFAULT 0 CHECK (discount_amount >= 0),
    total_amount DECIMAL(19,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    amount_paid DECIMAL(19,2) NOT NULL DEFAULT 0 CHECK (amount_paid >= 0),
    balance_due DECIMAL(19,2) NOT NULL DEFAULT 0 CHECK (balance_due >= 0),
    notes TEXT,
    sent_date TIMESTAMP,
    paid_date TIMESTAMP,
    version INT NOT NULL DEFAULT 1 CHECK (version >= 1),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT
);

-- Create index on invoice_number for fast lookups
CREATE INDEX idx_invoices_invoice_number ON invoices(invoice_number);

-- Create index on customer_id for foreign key queries
CREATE INDEX idx_invoices_customer_id ON invoices(customer_id);

-- Create index on status for filtering
CREATE INDEX idx_invoices_status ON invoices(status);

-- Create index on due_date for overdue queries
CREATE INDEX idx_invoices_due_date ON invoices(due_date);

-- Create index on issue_date for date range queries
CREATE INDEX idx_invoices_issue_date ON invoices(issue_date);

-- Create composite index for common query: customer_id + status
CREATE INDEX idx_invoices_customer_status ON invoices(customer_id, status);

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_invoices_updated_at
    BEFORE UPDATE ON invoices
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

