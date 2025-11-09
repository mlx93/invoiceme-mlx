-- V14: Fix admin user password hash
-- Updates the admin user with the correct BCrypt hash for password: Admin123!
-- This fixes production databases where the admin user was created with the wrong hash

UPDATE users 
SET password_hash = '$2a$10$miz.BCFqLEJcWJuLz.vhNOfP5A9aaxt1PRY8bsHSfKk20RQI4/Iuy',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'admin@invoiceme.com';

