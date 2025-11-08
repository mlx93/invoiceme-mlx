-- V9: Create password_reset_tokens table
-- This migration creates the password_reset_tokens table for password reset flow

-- Create password_reset_tokens table
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create index on token for fast lookups (authentication)
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);

-- Create index on user_id for user-specific queries
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);

-- Create composite index for validation query: token + used + expires_at
CREATE INDEX idx_password_reset_tokens_validation ON password_reset_tokens(token, used, expires_at);

