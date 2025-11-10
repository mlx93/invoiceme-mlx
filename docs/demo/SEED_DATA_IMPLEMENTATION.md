# Seed Data Implementation Guide

**Purpose**: Complete guide for generating and verifying demo seed data for InvoiceMe

---

## Overview

This guide provides instructions for populating the InvoiceMe database with comprehensive seed data that matches the exact scenario described in `Demo_script_UPDATED.md`. The seed data enables a flawless 5-minute demo execution without manual data entry delays.

---

## Files

1. **`docs/demo/seed_data.sql`** - Main SQL script with all seed data
2. **`scripts/verify-seed-data.sh`** - Verification script to check data integrity
3. **`scripts/generate-bcrypt-hash.sh`** - Utility to generate bcrypt password hashes

---

## Prerequisites

1. **PostgreSQL Database**: Running and accessible
2. **Database Credentials**: Know your database connection details
3. **psql**: PostgreSQL command-line client installed
4. **Permissions**: Database user with INSERT, UPDATE, DELETE permissions

---

## Step 1: Generate BCrypt Password Hash

Before running the seed script, you need to generate a proper bcrypt hash for the customer password (`customer123`).

### Option A: Using Python (Recommended)

```bash
pip install bcrypt
python3 -c "import bcrypt; print(bcrypt.hashpw(b'customer123', bcrypt.gensalt()).decode('utf-8'))"
```

### Option B: Using Node.js

```bash
npm install bcrypt
node -e "const bcrypt = require('bcrypt'); bcrypt.hash('customer123', 10).then(hash => console.log(hash));"
```

### Option C: Using Online Tool

Visit https://bcrypt-generator.com/ and generate hash for password `customer123` with cost factor 10.

### Option D: Using Java/Spring Boot

If you have the backend running, you can use Spring's BCryptPasswordEncoder:

```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
String hash = encoder.encode("customer123");
System.out.println(hash);
```

**Important**: Copy the generated hash and update it in `seed_data.sql` at line ~47 (replace the placeholder hash).

---

## Step 2: Review Seed Data Script

Open `docs/demo/seed_data.sql` and review:

1. **Database Connection**: The script uses standard PostgreSQL syntax
2. **UUIDs**: All entities use fixed UUIDs for idempotency
3. **Dates**: Uses `CURRENT_DATE` and intervals for relative dates
4. **Monetary Values**: All amounts use `DECIMAL(19,2)` with 2 decimal precision

---

## Step 3: Run Seed Data Script

### Method 1: Using psql (Recommended)

```bash
# Set environment variables (adjust as needed)
export PGHOST=localhost
export PGPORT=5432
export PGDATABASE=invoiceme
export PGUSER=postgres

# Run the script
psql -f docs/demo/seed_data.sql
```

### Method 2: Using psql with connection string

```bash
psql "postgresql://username:password@localhost:5432/invoiceme" -f docs/demo/seed_data.sql
```

### Method 3: Copy-paste into psql

```bash
psql -h localhost -U postgres -d invoiceme
# Then copy-paste contents of seed_data.sql
```

---

## Step 4: Verify Seed Data

Run the verification script to ensure all data was inserted correctly:

```bash
# Make script executable
chmod +x scripts/verify-seed-data.sh

# Set database connection parameters (if different from defaults)
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=invoiceme
export DB_USER=postgres

# Run verification
./scripts/verify-seed-data.sh
```

The script will check:
- ✅ User counts and details
- ✅ Customer information
- ✅ Invoice counts and totals
- ✅ Line item counts
- ✅ Payment records
- ✅ Credit balances
- ✅ Activity feed entries

---

## Expected Seed Data Summary

After running the seed script, you should have:

### Users (1 new + 1 existing)
- **SysAdmin**: `admin@invoiceme.com` / `Admin123!` (existing, created by migration V12)
- **Customer**: `john@riversideapts.com` / `customer123` (created by seed script)

### Customer (1)
- **Riverside Apartments LLC**
- Email: `john@riversideapts.com`
- Credit Balance: `$0.00` (credit was applied to Invoice #2)

### Invoices (3)

#### Invoice #1: INV-2025-0001
- **Status**: `PAID`
- **Total**: `$4,600.63`
- **Amount Paid**: `$4,600.63`
- **Balance Due**: `$0.00`
- **Line Items**: 4 (Emergency Response, Water Extraction, Equipment Setup, Anti-microbial Treatment)
- **Payment**: Full payment of `$4,600.63` via Credit Card

#### Invoice #2: INV-2025-0002
- **Status**: `SENT` (partial payment)
- **Total**: `$2,362.69` (after $100 credit applied)
- **Amount Paid**: `$1,500.00`
- **Balance Due**: `$862.69`
- **Line Items**: 3 (Drying Monitoring, Equipment Rental, Credit Applied)
- **Payment**: Partial payment of `$1,500.00` via Credit Card

#### Invoice #3: INV-2025-0003
- **Status**: `SENT` (overdue)
- **Total**: `$1,625.00` (includes $125 late fee)
- **Amount Paid**: `$0.00`
- **Balance Due**: `$1,625.00`
- **Line Items**: 2 (Service Charge, Late Fee)
- **Payment**: None

### Payments (2)
1. Invoice #1: `$4,600.63` (COMPLETED)
2. Invoice #2: `$1,500.00` (COMPLETED)

### Activity Feed
- Invoice sent events
- Payment recorded events
- Credit applied event

---

## Demo Execution Flow

With seed data in place, the demo can proceed as follows:

1. **Login as SysAdmin** (`admin@invoiceme.com` / `Admin123!`)
2. **Show Invoice #1**: Already paid, ready for refund demo
3. **Issue Refund**: $500 refund on Invoice #1 → adds $500 credit
4. **Show Invoice #2**: Already has $100 credit applied, partial payment recorded
5. **Show Invoice #3**: Overdue invoice with late fee
6. **Login as Customer** (`john@riversideapts.com` / `customer123`)
7. **Show Customer Portal**: View invoices, make payments

---

## Troubleshooting

### Issue: "Password hash incorrect"

**Solution**: Generate a new bcrypt hash using one of the methods in Step 1 and update `seed_data.sql`.

### Issue: "Duplicate key violation"

**Solution**: The script uses `ON CONFLICT DO NOTHING` / `ON CONFLICT DO UPDATE`, so it's safe to run multiple times. If you get duplicate key errors, check that you're using the correct UUIDs.

### Issue: "Foreign key constraint violation"

**Solution**: Ensure the script runs in the correct order:
1. Users (SysAdmin first, then Customer)
2. Customer entity
3. Link Customer user to Customer entity
4. Invoices
5. Line items
6. Payments
7. Activity feed

### Issue: "Invoice sequence not found"

**Solution**: The script creates the invoice sequence. If you get this error, ensure the `invoice_sequences` table exists (created by migration V11).

### Issue: "Tax calculations don't match"

**Solution**: Verify tax rate is `8.25%` (0.0825). The script uses:
- Line item tax: `unit_price * quantity * tax_rate`
- Total tax: Sum of all line item taxes
- Rounding: Banker's rounding (HALF_UP) to 2 decimal places

---

## Resetting Seed Data

To start fresh, you can either:

### Option 1: Delete Demo Data Only

```sql
-- Delete only demo-related data
DELETE FROM activity_feed WHERE aggregate_id IN (
    SELECT id FROM invoices WHERE customer_id IN (
        SELECT id FROM customers WHERE email = 'john@riversideapts.com'
    )
);
DELETE FROM payments WHERE customer_id IN (
    SELECT id FROM customers WHERE email = 'john@riversideapts.com'
);
DELETE FROM line_items WHERE invoice_id IN (
    SELECT id FROM invoices WHERE customer_id IN (
        SELECT id FROM customers WHERE email = 'john@riversideapts.com'
    )
);
DELETE FROM invoices WHERE customer_id IN (
    SELECT id FROM customers WHERE email = 'john@riversideapts.com'
);
DELETE FROM users WHERE email = 'john@riversideapts.com';
-- NOTE: Do NOT delete admin@invoiceme.com - it's the production admin account
DELETE FROM customers WHERE email = 'john@riversideapts.com';
```

### Option 2: Truncate All Tables (⚠️ DANGEROUS)

```sql
-- WARNING: This deletes ALL data!
TRUNCATE TABLE activity_feed CASCADE;
TRUNCATE TABLE payments CASCADE;
TRUNCATE TABLE line_items CASCADE;
TRUNCATE TABLE invoices CASCADE;
TRUNCATE TABLE customers CASCADE;
TRUNCATE TABLE users CASCADE;
```

---

## Important Notes

1. **Idempotency**: The script is designed to be safe to run multiple times using `ON CONFLICT` clauses.

2. **Production Safety**: The script includes commented-out DELETE statements. Uncomment only if you want to clear existing data.

3. **Password Security**: The passwords (`admin123`, `customer123`) are for demo purposes only. **Never use these in production**.

4. **Date Handling**: The script uses `CURRENT_DATE` and intervals, so dates are relative to when you run the script. Adjust if you need specific dates.

5. **Credit Application**: Invoice #2 has credit already applied. In a real scenario, credit would be applied automatically when marking the invoice as SENT.

6. **Refund Flow**: The demo script shows issuing a refund on Invoice #1. The seed data sets Invoice #1 as PAID, ready for the refund demo.

---

## Next Steps

After seed data is loaded:

1. ✅ Verify data using `verify-seed-data.sh`
2. ✅ Test login with both users
3. ✅ Verify invoices appear correctly in admin portal
4. ✅ Test customer portal login and invoice viewing
5. ✅ Practice demo flow with seed data

---

## Support

If you encounter issues:

1. Check the verification script output
2. Review PostgreSQL logs for detailed error messages
3. Verify database schema matches migrations
4. Ensure all ENUM types exist (created by migrations)

---

**Status**: ✅ **READY** - Seed data script complete and ready for use

