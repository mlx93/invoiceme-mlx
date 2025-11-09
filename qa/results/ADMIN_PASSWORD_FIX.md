# Admin Password Fix

## Issue
Admin login is failing with "Invalid email or password" error.

## Possible Causes
1. **Password hash mismatch** - The stored hash doesn't match "Admin123!"
2. **Enum read issue** - Hibernate might be failing to read the enum fields, causing user lookup to fail
3. **User not found** - The `findByEmail` might be returning empty due to enum conversion issues

## Current Admin User
- Email: `admin@invoiceme.com`
- Stored Hash: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
- Role: `SYSADMIN`
- Status: `ACTIVE`

## Testing
Try these passwords:
- `password123!` (most likely - matches the hash pattern)
- `Admin123!`
- `admin`
- `Admin123`

## Solution
If `password123!` works, update the documentation. If not, we need to:
1. Generate a proper BCrypt hash for "Admin123!"
2. Update the database with the correct hash
3. Or fix the enum read issue if that's preventing user lookup

