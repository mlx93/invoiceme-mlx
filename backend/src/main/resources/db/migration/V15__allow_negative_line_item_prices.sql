-- V15: Restore original unit price constraint
-- This migration restores the original CHECK constraint that enforces non-negative unit prices.

-- Drop the existing constraint (try multiple possible names)
ALTER TABLE line_items DROP CONSTRAINT IF EXISTS line_items_unit_price_check;

-- Alternative: if the constraint is still there, drop it by name pattern
DO $$ 
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT constraint_name 
        FROM information_schema.check_constraints 
        WHERE constraint_schema = 'public' 
        AND constraint_name LIKE '%unit_price%'
    ) LOOP
        EXECUTE 'ALTER TABLE line_items DROP CONSTRAINT IF EXISTS ' || quote_ident(r.constraint_name);
    END LOOP;
END $$;

-- Add back the original constraint that requires non-negative prices
ALTER TABLE line_items ADD CONSTRAINT line_items_unit_price_bounds 
    CHECK (unit_price >= 0 AND unit_price <= 999999999999999.99);
