# Supabase Database Setup Instructions

**Last Updated**: 2025-01-27

---

## Quick Setup (Recommended)

### Method 1: Supabase SQL Editor (Easiest)

1. **Log into Supabase**:
   - Go to https://supabase.com/
   - Navigate to your project

2. **Open SQL Editor**:
   - Click "SQL Editor" in the left sidebar
   - Click "New Query"

3. **Run Combined Migration**:
   - Open `/backend/src/main/resources/db/migration/ALL_MIGRATIONS_COMBINED.sql`
   - Copy entire contents
   - Paste into Supabase SQL Editor
   - Click "Run" (or press Cmd/Ctrl + Enter)

4. **Verify Tables Created**:
   - Go to "Table Editor" in left sidebar
   - You should see all 9 tables:
     - customers
     - invoices
     - line_items
     - payments
     - users
     - recurring_invoice_templates
     - template_line_items
     - activity_feed
     - password_reset_tokens

---

### Method 2: psql Command Line

1. **Get Connection String**:
   - Supabase Dashboard → Settings → Database
   - Copy "Connection string" (URI format)

2. **Run Combined Migration**:
   ```bash
   psql "postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres" -f backend/src/main/resources/db/migration/ALL_MIGRATIONS_COMBINED.sql
   ```

3. **Or Run Individual Migrations**:
   ```bash
   for file in backend/src/main/resources/db/migration/V*.sql; do
     psql "postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres" -f "$file"
   done
   ```

---

### Method 3: Using Flyway (If Backend is Set Up)

If you have Spring Boot backend configured with Flyway:

1. **Set DATABASE_URL**:
   ```bash
   export DATABASE_URL="postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres"
   ```

2. **Run Migrations**:
   ```bash
   cd backend
   ./mvnw flyway:migrate
   ```

---

## Verification

After running migrations, verify schema:

```sql
-- Check all tables exist
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
ORDER BY table_name;

-- Should return:
-- activity_feed
-- customers
-- invoices
-- line_items
-- password_reset_tokens
-- payments
-- recurring_invoice_templates
-- template_line_items
-- users

-- Check indexes
SELECT tablename, indexname 
FROM pg_indexes 
WHERE schemaname = 'public' 
ORDER BY tablename, indexname;

-- Check foreign keys
SELECT
    tc.table_name, 
    kcu.column_name, 
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE constraint_type = 'FOREIGN KEY'
ORDER BY tc.table_name;
```

---

## Troubleshooting

### Error: "type already exists"
- **Cause**: ENUM types already created
- **Solution**: Remove `CREATE TYPE` statements or use `CREATE TYPE IF NOT EXISTS` (PostgreSQL 9.5+)

### Error: "relation already exists"
- **Cause**: Tables already created
- **Solution**: Drop existing tables first or use `CREATE TABLE IF NOT EXISTS`

### Error: "permission denied"
- **Cause**: Insufficient permissions
- **Solution**: Ensure you're using the postgres superuser or have CREATE privileges

### Error: "function already exists"
- **Cause**: `update_updated_at_column()` function already exists
- **Solution**: Function is idempotent, safe to ignore or use `CREATE OR REPLACE FUNCTION`

---

## Next Steps

After schema is created:

1. ✅ **Backend Integration**: Backend Agent can now create JPA entities
2. ✅ **Test Data**: Add sample data for testing
3. ✅ **API Development**: Start building REST API endpoints

---

**Note**: The combined migration file (`ALL_MIGRATIONS_COMBINED.sql`) is provided for convenience. For production, use Flyway migrations (V1-V10) which are version-controlled and immutable.

