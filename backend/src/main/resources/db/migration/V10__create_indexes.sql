-- V10: Create additional indexes for performance optimization
-- This migration adds composite indexes and optimization indexes for common query patterns

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

-- Note: All foreign key columns already have indexes from previous migrations
-- This migration adds composite indexes for common query patterns to improve performance

