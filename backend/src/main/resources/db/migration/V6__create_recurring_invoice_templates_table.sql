-- V6: Create recurring_invoice_templates table
-- This migration creates the recurring invoice templates table for scheduled invoice generation

-- Create ENUM types for recurring_invoice_templates table
CREATE TYPE frequency_enum AS ENUM ('MONTHLY', 'QUARTERLY', 'ANNUALLY');
CREATE TYPE template_status_enum AS ENUM ('ACTIVE', 'PAUSED', 'COMPLETED');

-- Create recurring_invoice_templates table
CREATE TABLE recurring_invoice_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    frequency frequency_enum NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    next_invoice_date DATE NOT NULL,
    status template_status_enum NOT NULL DEFAULT 'ACTIVE',
    payment_terms payment_terms_enum NOT NULL DEFAULT 'NET_30',
    auto_send BOOLEAN NOT NULL DEFAULT FALSE,
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_templates_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE RESTRICT,
    CONSTRAINT fk_templates_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT chk_template_dates CHECK (end_date IS NULL OR end_date >= start_date),
    CONSTRAINT chk_next_invoice_date CHECK (next_invoice_date >= start_date)
);

-- Create index on customer_id for foreign key queries
CREATE INDEX idx_templates_customer_id ON recurring_invoice_templates(customer_id);

-- Create index on next_invoice_date for scheduled job queries
CREATE INDEX idx_templates_next_invoice_date ON recurring_invoice_templates(next_invoice_date);

-- Create index on status for filtering active templates
CREATE INDEX idx_templates_status ON recurring_invoice_templates(status);

-- Create composite index for scheduled job query: status + next_invoice_date
CREATE INDEX idx_templates_status_next_date ON recurring_invoice_templates(status, next_invoice_date);

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_templates_updated_at
    BEFORE UPDATE ON recurring_invoice_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

