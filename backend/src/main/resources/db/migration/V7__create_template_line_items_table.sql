-- V7: Create template_line_items table
-- This migration creates the template_line_items table with CASCADE DELETE on template deletion

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

