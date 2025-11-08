-- V1: Create customers table
-- This migration creates the customers table with all required fields, constraints, and ENUM types

-- Create ENUM types for customers table
CREATE TYPE customer_type_enum AS ENUM ('RESIDENTIAL', 'COMMERCIAL', 'INSURANCE');
CREATE TYPE customer_status_enum AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');

-- Create customers table
CREATE TABLE customers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_name VARCHAR(255) NOT NULL,
    contact_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(50),
    zip_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'USA',
    customer_type customer_type_enum NOT NULL DEFAULT 'COMMERCIAL',
    credit_balance DECIMAL(19,2) NOT NULL DEFAULT 0 CHECK (credit_balance >= 0),
    status customer_status_enum NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create index on email for fast lookups
CREATE INDEX idx_customers_email ON customers(email);

-- Create index on status for filtering
CREATE INDEX idx_customers_status ON customers(status);

-- Create index on customer_type for filtering
CREATE INDEX idx_customers_customer_type ON customers(customer_type);

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

