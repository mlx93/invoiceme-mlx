-- V11: Create invoice_sequences table
-- This migration creates the invoice_sequences table for tracking invoice number sequences per year

CREATE TABLE invoice_sequences (
    year INT PRIMARY KEY,
    sequence_number INT NOT NULL DEFAULT 1
);

-- Insert initial sequence for current year
INSERT INTO invoice_sequences (year, sequence_number)
VALUES (EXTRACT(YEAR FROM CURRENT_DATE), 1);

