# Admin Credentials - IMPORTANT

## ⚠️ Current Issue
Admin login is **NOT working**. The password hash in the database may not match "Admin123!".

## Admin User Info
- **Email**: `admin@invoiceme.com`
- **Role**: `SYSADMIN`
- **Status**: `ACTIVE`
- **Stored Hash**: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

## Possible Passwords to Try
The stored hash might match one of these:
1. `password123!` (most common test password)
2. `Admin123!` (what we want it to be)
3. `admin` (simple password)
4. `Admin123` (without exclamation)

## Solution Options

### Option 1: Find the Correct Password
Use an online BCrypt checker (https://bcrypt-generator.com/) to verify which password matches the hash.

### Option 2: Update Password Hash
Generate a new BCrypt hash for "Admin123!" and update the database:
```sql
UPDATE users 
SET password_hash = '<NEW_HASH_FOR_Admin123!>'
WHERE email = 'admin@invoiceme.com';
```

### Option 3: Create New Admin User
Delete and recreate the admin user with a known password.

## Next Steps
1. Test with `password123!` first
2. If that works, update documentation
3. If not, generate new hash and update database

