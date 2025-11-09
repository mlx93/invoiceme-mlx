-- Migration V13: Drop Recurring Invoice Tables
-- Purpose: Remove recurring invoice functionality as it's not part of core requirements

-- Drop foreign key constraints first
ALTER TABLE IF EXISTS template_line_items DROP CONSTRAINT IF EXISTS fk_template_line_items_template;

-- Drop tables
DROP TABLE IF EXISTS template_line_items;
DROP TABLE IF EXISTS recurring_invoice_templates;

-- Drop enum types (if not used elsewhere)
DROP TYPE IF EXISTS frequency_enum;
DROP TYPE IF EXISTS template_status_enum;

