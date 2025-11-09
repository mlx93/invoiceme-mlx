-- V12: Create initial admin user
-- This migration creates a default SYSADMIN user for initial system access
-- Password: Admin123! (BCrypt hash)
-- IMPORTANT: Change this password immediately after first login!

INSERT INTO users (id, email, password_hash, full_name, role, status, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'admin@invoiceme.com',
    '$2a$10$miz.BCFqLEJcWJuLz.vhNOfP5A9aaxt1PRY8bsHSfKk20RQI4/Iuy', -- Password: Admin123!
    'System Administrator',
    'SYSADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (email) DO NOTHING;

