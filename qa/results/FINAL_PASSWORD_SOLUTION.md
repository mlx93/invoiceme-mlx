# Final Password Solution

## Current Status
- ❌ Login still failing
- Hash in database: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`
- UPDATE commands not taking effect (possibly transaction/caching issue)

## Solution: Use Cost Factor 10 Hash

The hash you generated has cost factor **12**, but Spring Boot's `BCryptPasswordEncoder` uses cost factor **10** by default. While it SHOULD work, let's use cost factor 10 to match exactly.

### Step 1: Generate Hash with Cost Factor 10
1. Go to: https://bcrypt-generator.com/
2. Enter password: `Admin123!`
3. **Set rounds to 10** (not 12)
4. Click "Generate Hash"
5. Copy the hash

### Step 2: Update Database
```sql
UPDATE users 
SET password_hash = '<HASH_WITH_COST_10>'
WHERE email = 'admin@invoiceme.com';
```

### Step 3: Verify and Test
```bash
# Verify hash was updated
docker exec invoiceme-postgres psql -U postgres -d invoiceme -c \
  "SELECT email, password_hash FROM users WHERE email = 'admin@invoiceme.com';"

# Test login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@invoiceme.com","password":"Admin123!"}'
```

## Alternative: Use Backend to Generate Hash

Since UPDATE isn't working, we can create a test endpoint to generate the hash, then manually update the database.

## Why BCrypt is One-Way

**BCrypt is intentionally one-way** - this protects passwords. We cannot reverse a hash, but we can:
1. Generate a new hash for a known password
2. Verify if a password matches a stored hash
3. Update the database with a correct hash

This is the **correct and secure** approach.

