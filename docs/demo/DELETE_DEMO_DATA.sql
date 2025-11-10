-- ============================================================================
-- Delete Demo Data: 2 Customers and 2 Invoices
-- 
-- This script safely deletes test customers and invoices while preserving
-- the admin user account.
--
-- Deletion Order (respecting foreign key constraints):
-- 1. Payments (ON DELETE RESTRICT - must delete first)
-- 2. Invoices (line items cascade automatically)
-- 3. Customers (users.customer_id will be set to NULL automatically)
-- ============================================================================

-- Step 1: Delete payments associated with the invoices/customers
-- This will delete payments for invoices belonging to the 2 customers
DELETE FROM payments
WHERE invoice_id IN (
    SELECT id FROM invoices 
    WHERE customer_id IN (
        SELECT id FROM customers 
        WHERE email = 'john@riversideapts.com' 
           OR company_name LIKE '%Riverside%'
    )
);

-- Alternative: If you know the specific invoice IDs, use this instead:
-- DELETE FROM payments WHERE invoice_id IN (
--     SELECT id FROM invoices WHERE invoice_number IN ('INV-2025-0001', 'INV-2025-0002')
-- );

-- Step 2: Delete invoices (line items will cascade delete automatically)
-- This deletes all invoices for the 2 customers
DELETE FROM invoices
WHERE customer_id IN (
    SELECT id FROM customers 
    WHERE email = 'john@riversideapts.com' 
       OR company_name LIKE '%Riverside%'
);

-- Alternative: If you know the specific invoice numbers, use this instead:
-- DELETE FROM invoices WHERE invoice_number IN ('INV-2025-0001', 'INV-2025-0002');

-- Step 3: Delete customers
-- This will automatically set users.customer_id to NULL (ON DELETE SET NULL)
DELETE FROM customers
WHERE email = 'john@riversideapts.com' 
   OR company_name LIKE '%Riverside%';

-- ============================================================================
-- Verification Queries (run these to confirm deletion)
-- ============================================================================

-- Check remaining customers (should not include Riverside)
SELECT id, company_name, email FROM customers;

-- Check remaining invoices (should not include INV-2025-0001 or INV-2025-0002)
SELECT id, invoice_number, customer_id FROM invoices;

-- Check remaining payments (should be empty or not reference deleted invoices)
SELECT id, invoice_id, customer_id, amount FROM payments;

-- Check users (admin user should still exist, customer users may have customer_id = NULL)
SELECT id, email, role, customer_id FROM users WHERE role != 'SYSADMIN';

-- ============================================================================
-- More Specific Queries (if you know exact IDs or want to be more precise)
-- ============================================================================

-- Option A: Delete by specific customer emails/names
/*
DELETE FROM payments
WHERE customer_id IN (
    SELECT id FROM customers 
    WHERE email IN ('john@riversideapts.com', 'another@customer.com')
);

DELETE FROM invoices
WHERE customer_id IN (
    SELECT id FROM customers 
    WHERE email IN ('john@riversideapts.com', 'another@customer.com')
);

DELETE FROM customers
WHERE email IN ('john@riversideapts.com', 'another@customer.com');
*/

-- Option B: Delete by specific invoice numbers
/*
-- First, get customer IDs from invoices
DELETE FROM payments
WHERE invoice_id IN (
    SELECT id FROM invoices 
    WHERE invoice_number IN ('INV-2025-0001', 'INV-2025-0002')
);

DELETE FROM invoices
WHERE invoice_number IN ('INV-2025-0001', 'INV-2025-0002');

-- Then delete customers (only if no other invoices exist)
DELETE FROM customers
WHERE id NOT IN (SELECT DISTINCT customer_id FROM invoices);
*/

-- Option C: Delete all non-admin data (nuclear option - use with caution!)
/*
-- WARNING: This deletes ALL customers, invoices, payments except admin user
DELETE FROM payments;
DELETE FROM invoices;  -- line_items cascade automatically
DELETE FROM customers; -- users.customer_id set to NULL automatically
-- Admin user remains because we're not deleting users
*/

