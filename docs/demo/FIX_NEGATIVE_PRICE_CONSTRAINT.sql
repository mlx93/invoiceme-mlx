-- ============================================================================
-- Fix: Allow Negative Unit Prices for Credit Line Items
-- 
-- This script removes the constraint preventing negative unit_price values
-- in the line_items table, which is needed for credit application line items.
--
-- Run this directly in Supabase SQL Editor
-- ============================================================================

-- Step 1: Drop the existing constraint that prevents negative values
ALTER TABLE line_items DROP CONSTRAINT IF EXISTS line_items_unit_price_check;

-- Step 2: Find and drop any other constraints on unit_price
DO $$ 
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT constraint_name 
        FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'line_items'
        AND constraint_type = 'CHECK'
        AND constraint_name LIKE '%unit_price%'
    ) LOOP
        EXECUTE 'ALTER TABLE line_items DROP CONSTRAINT IF EXISTS ' || quote_ident(r.constraint_name);
    END LOOP;
END $$;

-- Step 3: Add a new constraint that allows negative values (for credits)
-- This allows values from -999,999,999,999,999.99 to +999,999,999,999,999.99
ALTER TABLE line_items ADD CONSTRAINT line_items_unit_price_bounds 
    CHECK (unit_price >= -999999999999999.99 AND unit_price <= 999999999999999.99);

-- Step 4: Verify the constraint was updated
SELECT 
    constraint_name,
    check_clause
FROM information_schema.check_constraints
WHERE constraint_schema = 'public'
AND constraint_name = 'line_items_unit_price_bounds';

-- Expected result should show:
-- constraint_name: line_items_unit_price_bounds
-- check_clause: (unit_price >= -999999999999999.99 AND unit_price <= 999999999999999.99)

