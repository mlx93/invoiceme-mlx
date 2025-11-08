-- Combined Flyway Migrations for Supabase
-- This file combines all migrations (V1-V10) for easy execution on Supabase SQL Editor
-- Execute this entire file in Supabase SQL Editor to create all tables, indexes, and constraints

-- ============================================================================
-- V1: Create customers table
-- ============================================================================

-- Create ENUM types for customers table
CREATE TYPE customer_type_enum AS ENUM ('RESIDENTIAL', 'COMMERCIAL', 'INSURANCE');
CREATE TYPE customer_status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');

-- Create customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(50),
    zip_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'USA',
    customer_type customer_type_enum NOT NULL DEFAULT 'COMMERCIAL',
    credit_balance DECIMAL(19,2) NOT NULL DEFAULT 0 CHECK (credit_balance >= 0),
    status customer_status_enum NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for fast lookups
CREATE INDEX idx_customers_email ON customers(email);

-- Create index on status for filtering
CREATE INDEX idx_customers_status ON customers(status);

-- Create index on customer_type for filtering
CREATE INDEX idx_customers_customer_type ON customers(customer_type);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- V2: Create invoices table
-- ============================================================================

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

-- ============================================================================
-- V3: Create line_items table
-- ============================================================================

-- Create ENUM type for discount_type
CREATE TYPE discount_type_enum AS ENUM ('NONE', 'PERCENTAGE', 'FIXED');

-- Create line_items table
CREATE TABLE line_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    invoice_id UUID NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 1),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
    discount_type discount_type_enum NOT NULL DEFAULT 'NONE',
    discount_value DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (discount_value >= 0),
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0 CHECK (tax_rate >= 0 AND tax_rate <= 100),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_line_items_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE
);

-- Create index on invoice_id for foreign key queries
CREATE INDEX idx_line_items_invoice_id ON line_items(invoice_id);

-- Create index on sort_order for ordering line items
CREATE INDEX idx_line_items_sort_order ON line_items(invoice_id, sort_order);

-- ============================================================================
-- V4: Create payments table
-- ============================================================================

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

-- ============================================================================
-- V5: Create users table
-- ============================================================================

-- Create ENUM types for users table
CREATE TYPE user_role_enum AS ENUM ('SYSADMIN', 'ACCOUNTANT', 'SALES', 'CUSTOMER');
CREATE TYPE user_status_enum AS ENUM ('PENDING', 'ACTIVE', 'INACTIVE', 'LOCKED');

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role user_role_enum NOT NULL,
    customer_id UUID,
    status user_status_enum NOT NULL DEFAULT 'PENDING',
    failed_login_count INT NOT NULL DEFAULT 0 CHECK (failed_login_count >= 0),
    locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL
);

-- Create index on email for fast lookups (authentication)
CREATE INDEX idx_users_email ON users(email);

-- Create index on role for role-based queries
CREATE INDEX idx_users_role ON users(role);

-- Create index on status for filtering active/pending users
CREATE INDEX idx_users_status ON users(status);

-- Create composite index for common query: role + status
CREATE INDEX idx_users_role_status ON users(role, status);

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add foreign key constraint for payments.created_by_user_id
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL;

-- ============================================================================
-- V6: Create recurring_invoice_templates table
-- ============================================================================

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

-- ============================================================================
-- V7: Create template_line_items table
-- ============================================================================

-- Create template_line_items table
CREATE TABLE template_line_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id UUID NOT NULL,
    description VARCHAR(500) NOT NULL,
    quantity INT NOT NULL CHECK (quantity >= 1),
    unit_price DECIMAL(19,2) NOT NULL CHECK (unit_price >= 0),
    discount_type discount_type_enum NOT NULL DEFAULT 'NONE',
    discount_value DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (discount_value >= 0),
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0 CHECK (tax_rate >= 0 AND tax_rate <= 100),
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_template_line_items_template FOREIGN KEY (template_id) REFERENCES recurring_invoice_templates(id) ON DELETE CASCADE
);

-- Create index on template_id for foreign key queries
CREATE INDEX idx_template_line_items_template_id ON template_line_items(template_id);

-- Create index on sort_order for ordering line items
CREATE INDEX idx_template_line_items_sort_order ON template_line_items(template_id, sort_order);

-- ============================================================================
-- V8: Create activity_feed table
-- ============================================================================

-- Create activity_feed table
CREATE TABLE activity_feed (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id UUID,
    CONSTRAINT fk_activity_feed_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Create index on occurred_at DESC for recent activity queries (most recent first)
CREATE INDEX idx_activity_feed_occurred_at ON activity_feed(occurred_at DESC);

-- Create index on aggregate_id for filtering by entity
CREATE INDEX idx_activity_feed_aggregate_id ON activity_feed(aggregate_id);

-- Create composite index for common query: aggregate_id + occurred_at DESC
CREATE INDEX idx_activity_feed_aggregate_date ON activity_feed(aggregate_id, occurred_at DESC);

-- Create index on event_type for filtering by event type
CREATE INDEX idx_activity_feed_event_type ON activity_feed(event_type);

-- ============================================================================
-- V9: Create password_reset_tokens table
-- ============================================================================

-- Create password_reset_tokens table
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index on token for fast lookups (authentication)
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Create index on user_id for user-specific queries
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

-- Create composite index for validation query: token + used + expires_at
CREATE INDEX idx_password_reset_tokens_validation ON password_reset_tokens(token, used, expires_at);

-- ============================================================================
-- V10: Create additional indexes for performance optimization
-- ============================================================================

-- Additional composite indexes for invoices table
-- Index for filtering invoices by customer and date range
CREATE INDEX IF NOT EXISTS idx_invoices_customer_issue_date ON invoices(customer_id, issue_date DESC);

-- Index for overdue invoice queries (status = OVERDUE or (status = SENT AND due_date < current_date))
CREATE INDEX IF NOT EXISTS idx_invoices_overdue ON invoices(status, due_date) WHERE status IN ('SENT', 'OVERDUE');

-- Additional composite indexes for payments table
-- Index for payment history queries by customer
CREATE INDEX IF NOT EXISTS idx_payments_customer_status ON payments(customer_id, status, payment_date DESC);

-- Index for invoice payment history
CREATE INDEX IF NOT EXISTS idx_payments_invoice_status ON payments(invoice_id, status, payment_date DESC);

-- Additional indexes for recurring_invoice_templates table
-- Index for scheduled job: active templates with next_invoice_date <= current_date
CREATE INDEX IF NOT EXISTS idx_templates_scheduled_job ON recurring_invoice_templates(status, next_invoice_date) WHERE status = 'ACTIVE';

-- Additional indexes for activity_feed table
-- Index for user activity queries
CREATE INDEX IF NOT EXISTS idx_activity_feed_user_date ON activity_feed(user_id, occurred_at DESC);

-- Index for event type filtering
CREATE INDEX IF NOT EXISTS idx_activity_feed_event_type_date ON activity_feed(event_type, occurred_at DESC);

-- Additional indexes for users table
-- Index for customer portal users (role = CUSTOMER AND customer_id IS NOT NULL)
CREATE INDEX IF NOT EXISTS idx_users_customer_portal ON users(role, customer_id) WHERE role = 'CUSTOMER' AND customer_id IS NOT NULL;

-- Index for pending user approval queries
CREATE INDEX IF NOT EXISTS idx_users_pending_approval ON users(status, created_at) WHERE status = 'PENDING';

-- Additional indexes for customers table
-- Index for customers with outstanding balance (credit_balance > 0 or has unpaid invoices)
CREATE INDEX IF NOT EXISTS idx_customers_outstanding ON customers(status, credit_balance) WHERE status = 'ACTIVE';

-- ============================================================================
-- Migration Complete!
-- ============================================================================
-- All 9 tables created with proper constraints, indexes, and foreign keys.
-- Schema is ready for Backend Agent integration.

