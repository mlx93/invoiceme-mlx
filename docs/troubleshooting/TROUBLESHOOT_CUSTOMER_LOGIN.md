# Troubleshoot Customer Login Issue

## Problem
Customer account `myles@gmail.com` exists but cannot log in.

## Diagnostic Steps

### Step 1: Check if User Account Exists

Run this SQL query in Supabase SQL Editor:

```sql
SELECT 
    id,
    email,
    full_name,
    role,
    status,
    customer_id,
    created_at
FROM users
WHERE email = 'myles@gmail.com';
```

**Expected Result**: Should return one row with:
- `email`: `myles@gmail.com`
- `role`: `CUSTOMER`
- `status`: `ACTIVE`
- `customer_id`: UUID linking to customer

**If no rows returned**: User account was not created. See "Fix: Create Missing User Account" below.

---

### Step 2: Check User Status

If user exists, check the status:

```sql
SELECT email, status, role, customer_id
FROM users
WHERE email = 'myles@gmail.com';
```

**Possible Statuses**:
- `ACTIVE` ✅ - Should work
- `PENDING` ❌ - Needs approval
- `INACTIVE` ❌ - Account disabled
- `LOCKED` ❌ - Account locked

**If status is not ACTIVE**: See "Fix: Activate User Account" below.

---

### Step 3: Check Customer Link

Verify the customer exists and is linked:

```sql
SELECT 
    u.email as user_email,
    u.status as user_status,
    u.customer_id,
    c.id as customer_id,
    c.company_name,
    c.email as customer_email
FROM users u
LEFT JOIN customers c ON u.customer_id = c.id
WHERE u.email = 'myles@gmail.com';
```

**Expected Result**: Should show customer linked via `customer_id`.

---

### Step 4: Verify Password Hash

Check if password hash exists:

```sql
SELECT 
    email,
    password_hash,
    LENGTH(password_hash) as hash_length
FROM users
WHERE email = 'myles@gmail.com';
```

**Expected Result**:
- `password_hash` should be a bcrypt hash (starts with `$2a$10$...`)
- `hash_length` should be 60 characters

**If password_hash is NULL or empty**: See "Fix: Set Password" below.

---

## Fixes

### Fix 1: Create Missing User Account

If user account doesn't exist, create it:

```sql
-- First, find the customer ID
SELECT id, email, company_name, contact_name
FROM customers
WHERE email = 'myles@gmail.com';

-- Then create the user account (replace CUSTOMER_UUID with actual customer ID from above)
INSERT INTO users (
    id,
    email,
    password_hash,
    full_name,
    role,
    status,
    customer_id,
    created_at,
    updated_at
)
VALUES (
    gen_random_uuid(),
    'myles@gmail.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- bcrypt hash for "test1234"
    (SELECT COALESCE(contact_name, company_name) FROM customers WHERE email = 'myles@gmail.com'),
    'CUSTOMER',
    'ACTIVE',
    (SELECT id FROM customers WHERE email = 'myles@gmail.com'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
```

---

### Fix 2: Activate User Account

If user exists but status is not ACTIVE:

```sql
UPDATE users
SET status = 'ACTIVE',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'myles@gmail.com';
```

---

### Fix 3: Set/Reset Password

If password hash is missing or incorrect:

```sql
UPDATE users
SET password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- bcrypt hash for "test1234"
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'myles@gmail.com';
```

**Password**: `test1234`

---

### Fix 4: Complete Fix (All-in-One)

Run this complete fix that handles all scenarios:

```sql
-- Step 1: Ensure customer exists
DO $$
DECLARE
    v_customer_id UUID;
    v_full_name TEXT;
BEGIN
    -- Get customer ID and full name
    SELECT id, COALESCE(contact_name, company_name) INTO v_customer_id, v_full_name
    FROM customers
    WHERE email = 'myles@gmail.com';
    
    IF v_customer_id IS NULL THEN
        RAISE EXCEPTION 'Customer with email myles@gmail.com does not exist';
    END IF;
    
    -- Step 2: Check if user exists, if not create it
    IF NOT EXISTS (SELECT 1 FROM users WHERE email = 'myles@gmail.com') THEN
        INSERT INTO users (
            id,
            email,
            password_hash,
            full_name,
            role,
            status,
            customer_id,
            created_at,
            updated_at
        )
        VALUES (
            gen_random_uuid(),
            'myles@gmail.com',
            '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
            v_full_name,
            'CUSTOMER',
            'ACTIVE',
            v_customer_id,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP
        );
        RAISE NOTICE 'User account created for myles@gmail.com';
    ELSE
        -- Step 3: Update existing user to ensure it's active and has correct password
        UPDATE users
        SET status = 'ACTIVE',
            password_hash = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
            customer_id = v_customer_id,
            updated_at = CURRENT_TIMESTAMP
        WHERE email = 'myles@gmail.com';
        RAISE NOTICE 'User account updated for myles@gmail.com';
    END IF;
END $$;

-- Step 4: Verify the fix
SELECT 
    u.email,
    u.full_name,
    u.role,
    u.status,
    u.customer_id,
    c.company_name,
    CASE 
        WHEN u.password_hash IS NULL THEN 'MISSING'
        WHEN LENGTH(u.password_hash) = 60 THEN 'OK'
        ELSE 'INVALID'
    END as password_status
FROM users u
LEFT JOIN customers c ON u.customer_id = c.id
WHERE u.email = 'myles@gmail.com';
```

---

## Test Login

After applying fixes, test login:

**Login Credentials**:
- **Email**: `myles@gmail.com`
- **Password**: `test1234`

**Via Frontend**:
1. Go to login page: `http://localhost:3000/login` (or your deployed URL)
2. Enter email and password
3. Should redirect to customer portal

**Via API** (for testing):
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "myles@gmail.com",
    "password": "test1234"
  }'
```

**Expected Response**:
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "user": {
    "id": "...",
    "email": "myles@gmail.com",
    "fullName": "...",
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "customerId": "..."
  }
}
```

---

## Common Issues

### Issue 1: "User account is not active. Status: PENDING"
**Solution**: Run Fix 2 (Activate User Account)

### Issue 2: "Invalid email or password"
**Possible Causes**:
- Password hash is incorrect → Run Fix 3 (Set Password)
- User doesn't exist → Run Fix 1 (Create Missing User Account)
- Email case mismatch → Check exact email case in database

### Issue 3: User exists but customer_id is NULL
**Solution**: Link user to customer:
```sql
UPDATE users
SET customer_id = (SELECT id FROM customers WHERE email = 'myles@gmail.com')
WHERE email = 'myles@gmail.com';
```

---

## Prevention

To prevent this issue in the future:
1. **Always create customers via the API/Frontend** - The auto-create feature will create user accounts automatically
2. **Check backend logs** - Look for errors during customer creation
3. **Verify after creation** - Run diagnostic queries after creating customers

---

## Quick Reference

**Default Password**: `test1234`  
**Bcrypt Hash**: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`  
**Required Status**: `ACTIVE`  
**Required Role**: `CUSTOMER`

