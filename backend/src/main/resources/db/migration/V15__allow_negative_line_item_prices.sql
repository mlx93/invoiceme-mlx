-- V15: Allow negative unit prices in line_items for credit applications
-- This migration removes the CHECK constraint that prevented negative unit prices.
-- Negative unit prices are needed when applying account credits to invoices.

-- Drop the constraint by finding it dynamically
DO $$ 
DECLARE
    constraint_name TEXT;
BEGIN
    -- Find the constraint name that checks unit_price
    SELECT conname INTO constraint_name
    FROM pg_constraint
    WHERE conrelid = 'line_items'::regclass
      AND contype = 'c'
      AND pg_get_constraintdef(oid) LIKE '%unit_price >= 0%';
    
    -- Drop it if found
    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE line_items DROP CONSTRAINT %I', constraint_name);
        RAISE NOTICE 'Dropped constraint: %', constraint_name;
    END IF;
END $$;

-- Now add a new constraint that allows negative values for credit line items
-- This allows reasonable negative values while preventing extreme values
ALTER TABLE line_items ADD CONSTRAINT line_items_unit_price_bounds 
    CHECK (unit_price >= -999999999999999.99 AND unit_price <= 999999999999999.99);

COMMENT ON CONSTRAINT line_items_unit_price_bounds ON line_items IS 
    'Allows negative unit prices for credit line items while preventing extreme values';
