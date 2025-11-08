-- V5: Create users table
-- This migration creates the users table with authentication fields and role-based access control

-- Create ENUM types for users table
CREATE TYPE user_role_enum AS ENUM ('SYSADMIN', 'ACCOUNTANT', 'SALES', 'CUSTOMER');
CREATE TYPE user_status_enum AS ENUM ('PENDING', 'ACTIVE', 'INACTIVE', 'LOCKED');

-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    role user_role_enum NOT NULL,
    customer_id UUID,
    status user_status_enum NOT NULL DEFAULT 'PENDING',
    failed_login_count INT NOT NULL DEFAULT 0 CHECK (failed_login_count >= 0),
    locked_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL
);

-- Create index on email for fast lookups (authentication)
CREATE INDEX idx_users_email ON users(email);

-- Create index on role for role-based queries
CREATE INDEX idx_users_role ON users(role);

-- Create index on status for filtering active/pending users
CREATE INDEX idx_users_status ON users(status);

-- Create composite index for common query: role + status
CREATE INDEX idx_users_role_status ON users(role, status);

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Add foreign key constraint for payments.created_by_user_id
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_created_by_user FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL;

