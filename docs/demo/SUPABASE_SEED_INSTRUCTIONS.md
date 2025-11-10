# Running Seed Data Script on Supabase

## Prerequisites

1. **Generate BCrypt Hash** (REQUIRED before running):
   
   The customer password hash needs to be generated. Use one of these methods:

   **Option A: Python** (Recommended)
   ```bash
   pip install bcrypt
   python3 -c "import bcrypt; print(bcrypt.hashpw(b'customer123', bcrypt.gensalt()).decode('utf-8'))"
   ```

   **Option B: Online Tool**
   - Visit: https://bcrypt-generator.com/
   - Password: `customer123`
   - Rounds: `10`
   - Copy the generated hash

   **Option C: Node.js**
   ```bash
   npm install bcrypt
   node -e "const bcrypt = require('bcrypt'); bcrypt.hash('customer123', 10).then(hash => console.log(hash));"
   ```

2. **Update seed_data.sql**:
   - Open `docs/demo/seed_data.sql`
   - Find line ~78 (the customer user INSERT statement)
   - Replace the placeholder hash with your generated hash

## Running on Supabase

### Method 1: Supabase SQL Editor (Recommended)

1. **Open Supabase Dashboard** → Your Project → SQL Editor
2. **Copy the entire contents** of `docs/demo/seed_data.sql`
3. **Paste into SQL Editor**
4. **Click "Run"** (or press Cmd/Ctrl + Enter)

**Advantages**:
- ✅ Visual feedback
- ✅ See errors immediately
- ✅ Can run queries step by step
- ✅ Transaction safety (BEGIN/COMMIT)

### Method 2: psql Command Line

```bash
# Connect to Supabase
psql "postgresql://postgres:[YOUR_PASSWORD]@db.[YOUR_PROJECT_REF].supabase.co:5432/postgres" -f docs/demo/seed_data.sql
```

Replace:
- `[YOUR_PASSWORD]` with your Supabase database password
- `[YOUR_PROJECT_REF]` with your Supabase project reference (found in connection string)

### Method 3: Supabase CLI

```bash
# If you have Supabase CLI installed
supabase db execute -f docs/demo/seed_data.sql
```

## Verification

After running the script, verify the data:

### Quick Check in Supabase SQL Editor:

```sql
-- Check users
SELECT email, full_name, role, status 
FROM users 
WHERE email IN ('admin@invoiceme.com', 'john@riversideapts.com')
ORDER BY email;

-- Check customer
SELECT company_name, email, credit_balance, status 
FROM customers 
WHERE email = 'john@riversideapts.com';

-- Check invoices
SELECT invoice_number, status, total_amount, amount_paid, balance_due 
FROM invoices 
WHERE invoice_number LIKE 'INV-2025-%'
ORDER BY invoice_number;

-- Check payments
SELECT i.invoice_number, p.amount, p.payment_method, p.status 
FROM payments p
JOIN invoices i ON p.invoice_id = i.id
WHERE i.invoice_number LIKE 'INV-2025-%'
ORDER BY i.invoice_number;
```

### Or Use Verification Script:

```bash
# Set Supabase connection details
export DB_HOST=db.[YOUR_PROJECT_REF].supabase.co
export DB_PORT=5432
export DB_NAME=postgres
export DB_USER=postgres
export PGPASSWORD=[YOUR_PASSWORD]

# Run verification
./scripts/verify-seed-data.sh
```

## Expected Results

After successful execution, you should have:

- ✅ **1 existing user**: `admin@invoiceme.com` (unchanged)
- ✅ **1 new user**: `john@riversideapts.com` / `customer123`
- ✅ **1 customer**: Riverside Apartments LLC
- ✅ **3 invoices**: 
  - INV-2025-0001: PAID ($4,600.63)
  - INV-2025-0002: SENT with partial payment ($862.69 balance)
  - INV-2025-0003: SENT, OVERDUE ($1,625.00 balance)
- ✅ **2 payments**: Invoice #1 full payment, Invoice #2 partial payment
- ✅ **Activity feed entries**: Domain events logged

## Important Notes

1. **Idempotent**: The script uses `ON CONFLICT DO NOTHING` / `ON CONFLICT DO UPDATE`, so it's safe to run multiple times.

2. **Admin Account**: The script does NOT create or modify `admin@invoiceme.com` - it already exists from migration V12.

3. **Transaction Safety**: The script wraps everything in `BEGIN` / `COMMIT`, so if any error occurs, all changes roll back.

4. **Production Safety**: The cleanup section (STEP 1) is commented out by default. Only uncomment if you want to delete existing demo data first.

5. **Dates**: The script uses `CURRENT_DATE` and intervals, so dates are relative to when you run it. Adjust if you need specific dates.

## Troubleshooting

### Error: "password hash incorrect"
- **Solution**: Make sure you generated a proper bcrypt hash and updated line ~78 in seed_data.sql

### Error: "duplicate key violation"
- **Solution**: The script handles this with `ON CONFLICT`, but if you get this error, check that UUIDs are correct or clear existing data first.

### Error: "foreign key constraint violation"
- **Solution**: Ensure migrations have run (especially V12 for admin user). The script depends on existing admin user.

### Error: "relation does not exist"
- **Solution**: Make sure all Flyway migrations have run successfully. Check Supabase → Database → Migrations.

## Next Steps

After seed data is loaded:

1. ✅ Test login: `admin@invoiceme.com` / `Admin123!`
2. ✅ Test customer login: `john@riversideapts.com` / `customer123`
3. ✅ Verify invoices appear in admin portal
4. ✅ Test customer portal
5. ✅ Practice demo flow

---

**Status**: ✅ Ready to run on Supabase

