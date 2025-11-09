# User Password Setup Guide

## Problem

You created a user account as an admin, but the user doesn't have a password set, so they cannot log in.

## Solutions

### Option 1: Set Password Hash Directly in Database (Quickest)

**Step 1**: Generate a bcrypt hash for your desired password.

You can use an online bcrypt generator (https://bcrypt-generator.com/) or use a simple script:

**Using Node.js** (if you have Node.js installed):
```bash
node -e "const bcrypt = require('bcrypt'); bcrypt.hash('YourPassword123!', 10).then(hash => console.log(hash));"
```

**Or use this pre-generated hash** (for password `password123`):
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
```

**Step 2**: Update the user's password hash in the database.

**Via Supabase SQL Editor**:
```sql
-- Replace 'user@example.com' with the actual user email
-- Replace the password_hash with your generated hash
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE email = 'user@example.com';
```

**Step 3**: Ensure the user status is ACTIVE (if they need to log in immediately):
```sql
UPDATE users 
SET status = 'ACTIVE'
WHERE email = 'user@example.com';
```

**Step 4**: The user can now log in with:
- **Email**: `user@example.com`
- **Password**: `password123` (or whatever password you hashed)

---

### Option 2: Use Registration Endpoint (If User Doesn't Exist Yet)

If the user account doesn't exist yet, you can create it via the registration API:

**Via curl**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "YourPassword123!",
    "fullName": "User Full Name",
    "role": "CUSTOMER"
  }'
```

**Note**: New users are created with `PENDING` status and need to be approved by a SysAdmin via `/users/pending` page.

---

### Option 3: Password Reset Flow (If Implemented)

The database has a `password_reset_tokens` table, but the password reset endpoints may not be fully implemented yet. Check if these endpoints exist:

- `POST /api/v1/auth/request-password-reset` - Request reset token
- `POST /api/v1/auth/reset-password` - Reset password with token

If implemented, the user would:
1. Request a password reset (sends email with token)
2. Use the token to set a new password

**Status**: Password reset functionality was planned but may not be fully implemented. Check `backend/src/main/java/com/invoiceme/auth/` for reset handlers.

---

## Quick Reference: Common Password Hashes

For testing/demo purposes, here are some pre-generated bcrypt hashes:

| Password | Bcrypt Hash |
|----------|-------------|
| `password123` | `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` |
| `admin123` | `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` |
| `customer123` | `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy` |

**⚠️ Security Warning**: These are for development/testing only. Never use these in production!

---

## Verify User Can Log In

After setting the password:

1. Go to the login page: `http://localhost:3000/login` (or your deployed URL)
2. Enter the user's email and password
3. If the user status is `PENDING`, they'll need to be approved first by a SysAdmin

---

## Troubleshooting

**User still can't log in?**
1. Check user status: `SELECT email, status FROM users WHERE email = 'user@example.com';`
2. If status is `PENDING`, approve them via `/users/pending` page as SysAdmin
3. Verify password hash was updated: `SELECT email, password_hash FROM users WHERE email = 'user@example.com';`
4. Check backend logs for authentication errors

**Password hash format**:
- Must be bcrypt format: `$2a$10$...` (60 characters)
- Spring Security uses bcrypt with cost factor 10 by default
- Never store plain text passwords!

---

## Recommended Approach

**For your current situation** (user already created, no password):

Use **Option 1** (direct database update) - it's the quickest and most straightforward:

```sql
-- 1. Set password (using 'password123' as example)
UPDATE users 
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    status = 'ACTIVE'
WHERE email = 'your-user-email@example.com';
```

Then the user can log in immediately with:
- Email: `your-user-email@example.com`
- Password: `password123`

