# Seed Data Guide for Demo

**Purpose**: Create test data for demo video recording

---

## Required Seed Data

### 1. SysAdmin User (Pre-created)

**Via SQL** (recommended for demo):
```sql
-- Insert SysAdmin user (password: admin123)
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'admin@floodshield.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- bcrypt hash for "admin123"
  'System',
  'Administrator',
  'SYSADMIN',
  'ACTIVE',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
```

**Or via API** (if backend running):
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@floodshield.com",
    "password": "admin123",
    "firstName": "System",
    "lastName": "Administrator",
    "role": "SYSADMIN"
  }'

# Then approve via SysAdmin (if approval workflow enabled)
```

### 2. Customer User (For Customer Portal Demo)

**Via SQL**:
```sql
-- Insert Customer user (password: customer123)
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'john@riversideapts.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', -- bcrypt hash for "customer123"
  'John',
  'Doe',
  'CUSTOMER',
  'ACTIVE',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
```

**Note**: Customer user should be linked to a Customer entity (see below).

---

## Demo Data Creation Flow

### Option A: Manual Creation During Demo (Recommended)

**Why**: Shows the actual functionality, more authentic

**Steps**:
1. Start with empty database (only SysAdmin user)
2. During demo, create customer "Riverside Apartments LLC"
3. Create invoices as shown in demo script
4. Create customer user account if needed for portal demo

**Pros**: 
- Shows real functionality
- No seed data cleanup needed
- More authentic demo

**Cons**:
- Takes longer (but still fits in 5 minutes)
- Risk of typos/errors during recording

### Option B: Pre-seed Data (Faster Demo)

**Why**: Faster demo, less risk of errors

**SQL Script** (run before demo):
```sql
-- 1. Create Customer
INSERT INTO customers (id, company_name, contact_name, email, phone, customer_type, status, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'Riverside Apartments LLC',
  'John Doe',
  'john@riversideapts.com',
  '555-1234',
  'COMMERCIAL',
  'ACTIVE',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);

-- 2. Create Customer User (linked to customer email)
-- (Use SQL from above)

-- 3. Optional: Pre-create one invoice for faster demo
-- (But better to create during demo to show functionality)
```

**Pros**:
- Faster demo
- Less risk of errors
- Can focus on showcasing features

**Cons**:
- Less authentic (doesn't show creation flow)
- Need to clean up after demo

---

## Recommended Approach

**Use Option A (Manual Creation)** for demo because:
1. Shows actual CRUD functionality (core requirement)
2. More authentic demonstration
3. Fits in 5-minute timeframe
4. No cleanup needed

**Only seed**: SysAdmin user account (so you can log in immediately)

---

## Quick Seed Script

**Minimal seed data** (SysAdmin only):

```sql
-- Create SysAdmin user
INSERT INTO users (id, email, password_hash, first_name, last_name, role, status, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'admin@floodshield.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'System',
  'Administrator',
  'SYSADMIN',
  'ACTIVE',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
);
```

**Password**: `admin123` (for demo purposes)

---

## Database Reset (If Needed)

**To start fresh**:
```sql
-- WARNING: This deletes all data!
TRUNCATE TABLE payments CASCADE;
TRUNCATE TABLE line_items CASCADE;
TRUNCATE TABLE invoices CASCADE;
TRUNCATE TABLE customers CASCADE;
TRUNCATE TABLE users CASCADE;
TRUNCATE TABLE activity_feed CASCADE;

-- Then re-run seed script above
```

---

**Status**: ✅ **READY** - Minimal seed data (SysAdmin only) recommended

