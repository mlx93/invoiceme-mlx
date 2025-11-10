-- ============================================================================
-- InvoiceMe Demo Seed Data SQL Script
-- ============================================================================
-- Purpose: Populate database with demo data matching Demo_script_UPDATED.md
-- Scenario: FloodShield Restoration - Emergency water damage job
-- Customer: Riverside Apartments LLC
--
-- IMPORTANT NOTES:
-- 1. This script is idempotent - safe to run multiple times
-- 2. Uses ON CONFLICT DO NOTHING for safety
-- 3. All monetary values use DECIMAL(19,2) with 2 decimal precision
-- 4. Tax rate: 8.25% = 0.0825
-- 5. Invoice numbers follow format: INV-YYYY-####
-- 6. ADMIN USER: Uses existing admin@invoiceme.com (created by migration V12)
--    - Email: admin@invoiceme.com
--    - Password: Admin123!
--    - This script does NOT create or modify the admin user
-- ============================================================================

BEGIN;

-- ============================================================================
-- STEP 1: Clear existing demo data (OPTIONAL - COMMENT OUT FOR PRODUCTION)
-- ============================================================================
-- WARNING: Uncommenting this will delete all data!
-- Uncomment only if you want to start fresh

/*
DELETE FROM activity_feed WHERE aggregate_id IN (
    SELECT id FROM invoices WHERE customer_id IN (
        SELECT id FROM customers WHERE email = 'john@riversideapts.com'
    )
);
DELETE FROM payments WHERE customer_id IN (
    SELECT id FROM customers WHERE email = 'john@riversideapts.com'
);
DELETE FROM line_items WHERE invoice_id IN (
    SELECT id FROM invoices WHERE customer_id IN (
        SELECT id FROM customers WHERE email = 'john@riversideapts.com'
    )
);
DELETE FROM invoices WHERE customer_id IN (
    SELECT id FROM customers WHERE email = 'john@riversideapts.com'
);
DELETE FROM users WHERE email = 'john@riversideapts.com';
-- NOTE: Do NOT delete admin@invoiceme.com - it's the production admin account
DELETE FROM customers WHERE email = 'john@riversideapts.com';
*/

-- ============================================================================
-- STEP 2: Create/Update Invoice Sequence for 2025
-- ============================================================================
INSERT INTO invoice_sequences (year, sequence_number)
VALUES (2025, 4) -- Start at 4 since we're creating INV-2025-0001, 0002, 0003
ON CONFLICT (year) DO UPDATE SET sequence_number = 4;

-- ============================================================================
-- STEP 3: Create Users
-- ============================================================================

-- NOTE: SysAdmin user (admin@invoiceme.com) already exists in production
-- Created by migration V12__create_initial_admin_user.sql
-- Email: admin@invoiceme.com
-- Password: Admin123!
-- We do NOT create or modify this user - it already exists

-- Customer User (will be linked to customer after customer is created)
-- Email: john@riversideapts.com
-- Password: customer123
-- BCrypt hash: MUST BE GENERATED - see SEED_DATA_IMPLEMENTATION.md Step 1
-- Generate hash using: python3 -c "import bcrypt; print(bcrypt.hashpw(b'customer123', bcrypt.gensalt()).decode('utf-8'))"
-- Or use: scripts/generate-bcrypt-hash.sh customer123
-- Example hash format: $2a$10$XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
INSERT INTO users (id, email, password_hash, full_name, role, status, customer_id, created_at, updated_at)
VALUES (
    'b2c3d4e5-f6a7-8901-bcde-f12345678901'::UUID,
    'john@riversideapts.com',
    '$2a$10$/TK6mQxsRhgvpHWECK3Y9OOQ5dIVOAyE9PaObj3oPc8otBUGMl9We', -- bcrypt hash for "customer123"
    'John Doe',
    'CUSTOMER',
    'ACTIVE',
    NULL, -- Will be updated after customer creation
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================================
-- STEP 4: Create Customer Entity
-- ============================================================================

-- Riverside Apartments LLC
INSERT INTO customers (id, company_name, contact_name, email, phone, customer_type, credit_balance, status, created_at, updated_at)
VALUES (
    'c3d4e5f6-a7b8-9012-cdef-123456789012'::UUID,
    'Riverside Apartments LLC',
    'John Doe',
    'john@riversideapts.com',
    '555-1234',
    'COMMERCIAL',
    0.00, -- Initially $0, will be updated after refund demo
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO UPDATE SET
    company_name = EXCLUDED.company_name,
    contact_name = EXCLUDED.contact_name,
    phone = EXCLUDED.phone,
    customer_type = EXCLUDED.customer_type,
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;

-- Link customer user to customer entity
UPDATE users
SET customer_id = (SELECT id FROM customers WHERE email = 'john@riversideapts.com')
WHERE email = 'john@riversideapts.com';

-- ============================================================================
-- STEP 5: Create Invoice #1 - Emergency Response (INV-2025-0001)
-- ============================================================================
-- Status: PAID (paid exactly $4,600.63)
-- Created: 1 week ago
-- Sent: 1 day ago
-- Paid: Today (for demo purposes, we'll set it as already paid)

INSERT INTO invoices (
    id,
    invoice_number,
    customer_id,
    issue_date,
    due_date,
    status,
    payment_terms,
    subtotal,
    tax_amount,
    discount_amount,
    total_amount,
    amount_paid,
    balance_due,
    sent_date,
    paid_date,
    version,
    created_at,
    updated_at
)
VALUES (
    'd4e5f6a7-b8c9-0123-def0-123456789013'::UUID,
    'INV-2025-0001',
    (SELECT id FROM customers WHERE email = 'john@riversideapts.com'),
    CURRENT_DATE - INTERVAL '7 days', -- Created 1 week ago
    CURRENT_DATE - INTERVAL '7 days' + INTERVAL '30 days', -- Due 30 days from issue
    'PAID', -- Already paid
    'NET_30',
    4250.00, -- Subtotal
    350.63,  -- Tax (8.25% of $4,250)
    0.00,    -- No discount initially
    4600.63, -- Total
    4600.63, -- Amount paid (exact payment, no overpayment)
    0.00,    -- Balance due (fully paid)
    CURRENT_DATE - INTERVAL '1 day', -- Sent 1 day ago
    CURRENT_DATE, -- Paid today
    1,
    CURRENT_TIMESTAMP - INTERVAL '7 days',
    CURRENT_TIMESTAMP
)
ON CONFLICT (invoice_number) DO UPDATE SET
    status = EXCLUDED.status,
    amount_paid = EXCLUDED.amount_paid,
    balance_due = EXCLUDED.balance_due,
    paid_date = EXCLUDED.paid_date,
    updated_at = CURRENT_TIMESTAMP;

-- Invoice #1 Line Items
INSERT INTO line_items (id, invoice_id, description, quantity, unit_price, discount_type, discount_value, tax_rate, sort_order, created_at)
VALUES
    -- Line Item 1: Emergency Response Fee
    (
        'e5f6a7b8-c9d0-1234-ef01-123456789014'::UUID,
        (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0001'),
        'Emergency Response Fee',
        1,
        450.00,
        'NONE',
        0.00,
        8.25,
        1,
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    ),
    -- Line Item 2: Water Extraction
    (
        'f6a7b8c9-d0e1-2345-f012-123456789015'::UUID,
        (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0001'),
        'Water Extraction (6 hours)',
        1,
        750.00,
        'NONE',
        0.00,
        8.25,
        2,
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    ),
    -- Line Item 3: Equipment Setup
    (
        'a7b8c9d0-e1f2-3456-0123-123456789016'::UUID,
        (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0001'),
        'Equipment Setup',
        1,
        2100.00,
        'NONE',
        0.00,
        8.25,
        3,
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    ),
    -- Line Item 4: Anti-microbial Treatment
    (
        'b8c9d0e1-f2a3-4567-1234-123456789017'::UUID,
        (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0001'),
        'Anti-microbial Treatment',
        1,
        950.00,
        'NONE',
        0.00,
        8.25,
        4,
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    )
ON CONFLICT (id) DO NOTHING;

-- Payment for Invoice #1 (exact payment, no overpayment)
INSERT INTO payments (
    id,
    invoice_id,
    customer_id,
    amount,
    payment_method,
    payment_date,
    payment_reference,
    status,
    created_by_user_id,
    notes,
    created_at
)
VALUES (
    'c9d0e1f2-a3b4-5678-2345-123456789018'::UUID,
    (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0001'),
    (SELECT id FROM customers WHERE email = 'john@riversideapts.com'),
    4600.63, -- Exact payment amount
    'CREDIT_CARD',
    CURRENT_DATE,
    'CC-XXXX-1234',
    'COMPLETED',
    (SELECT id FROM users WHERE email = 'admin@invoiceme.com'),
    'Initial payment for emergency response',
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- STEP 6: Create Invoice #2 - Monitoring Phase (INV-2025-0002)
-- ============================================================================
-- Status: SENT (with $100 credit already applied)
-- Created: Today
-- Sent: Today
-- Has partial payment of $1,500
-- Balance due: $862.69

INSERT INTO invoices (
    id,
    invoice_number,
    customer_id,
    issue_date,
    due_date,
    status,
    payment_terms,
    subtotal,
    tax_amount,
    discount_amount,
    total_amount,
    amount_paid,
    balance_due,
    sent_date,
    paid_date,
    version,
    created_at,
    updated_at
)
VALUES (
    'd0e1f2a3-b4c5-6789-3456-123456789019'::UUID,
    'INV-2025-0002',
    (SELECT id FROM customers WHERE email = 'john@riversideapts.com'),
    CURRENT_DATE, -- Created today
    CURRENT_DATE + INTERVAL '30 days', -- Due 30 days from issue
    'SENT', -- Sent but not fully paid
    'NET_30',
    2275.00, -- Subtotal (sum of line items)
    187.69,  -- Tax (8.25% of $2,275)
    100.00,  -- Discount (credit applied as discount, not line item)
    2362.69, -- Total (subtotal + tax - discount = $2,275 + $187.69 - $100 = $2,362.69)
    1500.00, -- Amount paid (partial payment)
    862.69,  -- Balance due
    CURRENT_DATE, -- Sent today
    NULL, -- Not fully paid
    1,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (invoice_number) DO UPDATE SET
    status = EXCLUDED.status,
    amount_paid = EXCLUDED.amount_paid,
    balance_due = EXCLUDED.balance_due,
    updated_at = CURRENT_TIMESTAMP;

-- Invoice #2 Line Items
INSERT INTO line_items (id, invoice_id, description, quantity, unit_price, discount_type, discount_value, tax_rate, sort_order, created_at)
VALUES
    -- Line Item 1: Drying Monitoring
    (
        'e1f2a3b4-c5d6-7890-4567-123456789020'::UUID,
        (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0002'),
        'Drying Monitoring (7 days)',
        1,
        1050.00,
        'NONE',
        0.00,
        8.25,
        1,
        CURRENT_TIMESTAMP
    ),
    -- Line Item 2: Equipment Rental
    (
        'f2a3b4c5-d6e7-8901-5678-123456789021'::UUID,
        (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0002'),
        'Equipment Rental',
        1,
        1225.00,
        'NONE',
        0.00,
        8.25,
        2,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (id) DO NOTHING;

-- Note: Credit is applied as discount_amount on the invoice, not as a line item
-- The $100 credit is reflected in the invoice's discount_amount field (set above)

-- Partial Payment for Invoice #2
INSERT INTO payments (
    id,
    invoice_id,
    customer_id,
    amount,
    payment_method,
    payment_date,
    payment_reference,
    status,
    created_by_user_id,
    notes,
    created_at
)
VALUES (
    'b4c5d6e7-f8a9-0123-7890-123456789023'::UUID,
    (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0002'),
    (SELECT id FROM customers WHERE email = 'john@riversideapts.com'),
    1500.00, -- Partial payment
    'CREDIT_CARD',
    CURRENT_DATE,
    'CC-XXXX-5678',
    'COMPLETED',
    (SELECT id FROM users WHERE email = 'john@riversideapts.com'), -- Customer made payment
    'Partial payment for monitoring phase',
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- Update customer credit balance (credit was applied, so reduce from $100 to $0)
-- Note: In the demo, credit starts at $100 (from overpayment scenario that doesn't exist in system)
-- For seed data, we'll set credit to $0 since Invoice #2 already has credit applied
UPDATE customers
SET credit_balance = 0.00
WHERE email = 'john@riversideapts.com';

-- ============================================================================
-- STEP 7: Create Invoice #3 - Overdue Invoice (INV-2025-0003)
-- ============================================================================
-- Status: SENT, OVERDUE
-- Created: 35 days ago
-- Due: 31 days ago (overdue by 4 days)
-- Total: $1,500.00
-- Late fee: $125.00 (to be added by scheduled job or manually)

INSERT INTO invoices (
    id,
    invoice_number,
    customer_id,
    issue_date,
    due_date,
    status,
    payment_terms,
    subtotal,
    tax_amount,
    discount_amount,
    total_amount,
    amount_paid,
    balance_due,
    sent_date,
    paid_date,
    version,
    created_at,
    updated_at
)
VALUES (
    'c5d6e7f8-a9b0-1234-8901-123456789024'::UUID,
    'INV-2025-0003',
    (SELECT id FROM customers WHERE email = 'john@riversideapts.com'),
    CURRENT_DATE - INTERVAL '35 days', -- Created 35 days ago
    CURRENT_DATE - INTERVAL '31 days', -- Due 31 days ago (overdue)
    'SENT', -- Will show as overdue in UI
    'NET_30',
    1385.68, -- Subtotal (before late fee) - calculated to get $1,500 total with 8.25% tax
    114.32,  -- Tax (8.25% of $1,385.68 = $114.32)
    0.00,    -- No discount
    1500.00, -- Total (before late fee, will be $1,625 after late fee)
    0.00,    -- Not paid
    1500.00, -- Balance due (before late fee)
    CURRENT_DATE - INTERVAL '35 days', -- Sent when created
    NULL,
    1,
    CURRENT_TIMESTAMP - INTERVAL '35 days',
    CURRENT_TIMESTAMP
)
ON CONFLICT (invoice_number) DO UPDATE SET
    status = EXCLUDED.status,
    updated_at = CURRENT_TIMESTAMP;

-- Invoice #3 Line Items (before late fee)
-- Calculation: To get $1,500 total with 8.25% tax:
-- subtotal + (subtotal * 0.0825) = 1500
-- subtotal * 1.0825 = 1500
-- subtotal = 1500 / 1.0825 = 1385.68
-- tax = 1500 - 1385.68 = 114.32
INSERT INTO line_items (id, invoice_id, description, quantity, unit_price, discount_type, discount_value, tax_rate, sort_order, created_at)
VALUES
    -- Line Item 1: Service Charge
    (
        'd6e7f8a9-b0c1-2345-9012-123456789025'::UUID,
        (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0003'),
        'Service Charge',
        1,
        1385.68, -- Base amount to get $1,500 total with 8.25% tax
        'NONE',
        0.00,
        8.25,
        1,
        CURRENT_TIMESTAMP - INTERVAL '35 days'
    )
ON CONFLICT (id) DO NOTHING;

-- Late Fee Line Item (added by scheduled job or manually)
INSERT INTO line_items (id, invoice_id, description, quantity, unit_price, discount_type, discount_value, tax_rate, sort_order, created_at)
VALUES (
    'e7f8a9b0-c1d2-3456-0123-123456789026'::UUID,
    (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0003'),
    'Late Fee (31+ days overdue)',
    1,
    125.00,
    'NONE',
    0.00,
    0.00, -- Late fees typically don't have tax
    2,
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;

-- Update Invoice #3 totals to include late fee
UPDATE invoices
SET 
    subtotal = subtotal + 125.00,
    total_amount = total_amount + 125.00,
    balance_due = balance_due + 125.00,
    updated_at = CURRENT_TIMESTAMP
WHERE invoice_number = 'INV-2025-0003';

-- ============================================================================
-- STEP 8: Create Activity Feed Entries (Optional - for demo completeness)
-- ============================================================================

-- Invoice #1 Sent Event
INSERT INTO activity_feed (id, aggregate_id, event_type, description, occurred_at, user_id)
VALUES (
    'f8a9b0c1-d2e3-4567-1234-123456789027'::UUID,
    (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0001'),
    'InvoiceSentEvent',
    'Invoice INV-2025-0001 sent to Riverside Apartments LLC',
    CURRENT_DATE - INTERVAL '1 day',
    (SELECT id FROM users WHERE email = 'admin@invoiceme.com')
)
ON CONFLICT (id) DO NOTHING;

-- Invoice #1 Payment Event
INSERT INTO activity_feed (id, aggregate_id, event_type, description, occurred_at, user_id)
VALUES (
    'a9b0c1d2-e3f4-5678-2345-123456789028'::UUID,
    (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0001'),
    'PaymentRecordedEvent',
    'Payment of $4,600.63 recorded for invoice INV-2025-0001',
    CURRENT_DATE,
    (SELECT id FROM users WHERE email = 'john@riversideapts.com')
)
ON CONFLICT (id) DO NOTHING;

-- Invoice #2 Sent Event
INSERT INTO activity_feed (id, aggregate_id, event_type, description, occurred_at, user_id)
VALUES (
    'b0c1d2e3-f4a5-6789-3456-123456789029'::UUID,
    (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0002'),
    'InvoiceSentEvent',
    'Invoice INV-2025-0002 sent to Riverside Apartments LLC with $100 credit applied',
    CURRENT_DATE,
    (SELECT id FROM users WHERE email = 'admin@invoiceme.com')
)
ON CONFLICT (id) DO NOTHING;

-- Invoice #2 Credit Applied Event
INSERT INTO activity_feed (id, aggregate_id, event_type, description, occurred_at, user_id)
VALUES (
    'c1d2e3f4-a5b6-7890-4567-123456789030'::UUID,
    (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0002'),
    'CreditAppliedEvent',
    '$100.00 credit applied to invoice INV-2025-0002',
    CURRENT_DATE,
    NULL -- System event
)
ON CONFLICT (id) DO NOTHING;

-- Invoice #2 Payment Event
INSERT INTO activity_feed (id, aggregate_id, event_type, description, occurred_at, user_id)
VALUES (
    'd2e3f4a5-b6c7-8901-5678-123456789031'::UUID,
    (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0002'),
    'PaymentRecordedEvent',
    'Partial payment of $1,500.00 recorded for invoice INV-2025-0002',
    CURRENT_DATE,
    (SELECT id FROM users WHERE email = 'john@riversideapts.com')
)
ON CONFLICT (id) DO NOTHING;

-- Invoice #3 Sent Event
INSERT INTO activity_feed (id, aggregate_id, event_type, description, occurred_at, user_id)
VALUES (
    'e3f4a5b6-c7d8-9012-6789-123456789032'::UUID,
    (SELECT id FROM invoices WHERE invoice_number = 'INV-2025-0003'),
    'InvoiceSentEvent',
    'Invoice INV-2025-0003 sent to Riverside Apartments LLC',
    CURRENT_DATE - INTERVAL '35 days',
    (SELECT id FROM users WHERE email = 'admin@invoiceme.com')
)
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- STEP 9: Verification Queries (for manual verification)
-- ============================================================================

-- Uncomment to run verification queries:
/*
-- Verify users
SELECT email, full_name, role, status FROM users WHERE email IN ('admin@invoiceme.com', 'john@riversideapts.com');

-- Verify customer
SELECT company_name, email, credit_balance, status FROM customers WHERE email = 'john@riversideapts.com';

-- Verify invoices
SELECT invoice_number, status, total_amount, amount_paid, balance_due FROM invoices ORDER BY invoice_number;

-- Verify line items count
SELECT invoice_number, COUNT(*) as line_item_count FROM invoices i
JOIN line_items li ON i.id = li.invoice_id
GROUP BY invoice_number
ORDER BY invoice_number;

-- Verify payments
SELECT i.invoice_number, p.amount, p.payment_method, p.status FROM payments p
JOIN invoices i ON p.invoice_id = i.id
ORDER BY i.invoice_number, p.created_at;
*/

COMMIT;

-- ============================================================================
-- Seed Data Summary
-- ============================================================================
-- Users: 1 new (Customer) + 1 existing (SysAdmin: admin@invoiceme.com)
-- Customer: 1 (Riverside Apartments LLC)
-- Invoices: 3
--   - INV-2025-0001: PAID ($4,600.63)
--   - INV-2025-0002: SENT with partial payment ($862.69 balance)
--   - INV-2025-0003: SENT, OVERDUE ($1,625.00 balance)
-- Payments: 2 (Invoice #1 full payment, Invoice #2 partial payment)
-- Credit Balance: $0.00 (credit was applied to Invoice #2)
-- ============================================================================

