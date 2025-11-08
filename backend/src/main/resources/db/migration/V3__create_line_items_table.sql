-- V3: Create line_items table
-- This migration creates the line_items table with CASCADE DELETE on invoice deletion

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

