# Password Fix Instructions

## Issue
Login failing with 400 "Invalid email or password" error.

## Root Cause
The stored BCrypt hash doesn't match "Admin123!".

## Solution

### Step 1: Generate BCrypt Hash
Use online tool: https://bcrypt-generator.com/
- Enter password: `Admin123!`
- Set rounds: **10** (to match Spring Boot's default)
- Click "Generate Hash"
- Copy the hash

### Step 2: Update Database
```sql
UPDATE users 
SET password_hash = '<GENERATED_HASH_HERE>',
    updated_at = CURRENT_TIMESTAMP
WHERE email = 'admin@invoiceme.com';
```

### Step 3: Verify Update
```sql
SELECT email, LEFT(password_hash, 40) as hash_start 
FROM users 
WHERE email = 'admin@invoiceme.com';
```

### Step 4: Test Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@invoiceme.com","password":"Admin123!"}'
```

## Important Notes

1. **BCrypt is One-Way**: Cannot reverse hash to get password (this is correct for security)
2. **Cost Factor**: Use cost factor 10 to match Spring Boot's BCryptPasswordEncoder default
3. **Hash Changes**: Each time you generate a hash, it will be different (due to salt), but all will verify correctly

## Debug Logging

I've added debug logging to `LoginHandler.java`. Check `/tmp/backend.log` to see:
- If user is found
- User role and status
- Password match result

## Alternative: Use Hash from Image

If you generated a hash with cost factor 12 (like in the image), you can use that too - BCryptPasswordEncoder can verify hashes with different cost factors. Just update the database with that hash.

