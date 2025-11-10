# Seed Data Generation Agent Prompt - InvoiceMe Demo

## Mission
Generate comprehensive seed data for the InvoiceMe demo video that matches the exact scenario described in `docs/demo/Demo_script_UPDATED.md`. Create SQL scripts and/or API call scripts that will populate the production/demo database with all required data to execute the 5-minute demo flawlessly.

## Reference Documents
- **Demo Script**: `docs/demo/Demo_script_UPDATED.md` - Contains exact scenario, line items, amounts, and flow
- **Seed Data Guide**: `docs/demo/SEED_DATA_GUIDE.md` - Current seed data approach (minimal)
- **Database Schema**: Review `backend/src/main/resources/db/migration/` for table structures

## Demo Scenario Overview

**Company**: FloodShield Restoration  
**Customer**: Riverside Apartments LLC  
**Scenario**: Emergency water damage job with follow-up monitoring

## Required Seed Data

### 1. Users (2 users)

#### SysAdmin User
- **Email**: `admin@floodshield.com`
- **Password**: `admin123` (bcrypt hash: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`)
- **Name**: System Administrator
- **Role**: `SYSADMIN`
- **Status**: `ACTIVE`
- **Purpose**: Admin portal login for demo

#### Customer User
- **Email**: `john@riversideapts.com`
- **Password**: `customer123` (generate bcrypt hash)
- **Name**: John Doe
- **Role**: `CUSTOMER`
- **Status**: `ACTIVE`
- **Customer ID**: Must link to Riverside Apartments customer entity
- **Purpose**: Customer portal login for payment demo

### 2. Customer Entity

**Riverside Apartments LLC**
- **Company Name**: `Riverside Apartments LLC`
- **Contact Name**: `John Doe`
- **Email**: `john@riversideapts.com`
- **Phone**: `555-1234`
- **Customer Type**: `COMMERCIAL`
- **Status**: `ACTIVE`
- **Credit Balance**: `$0.00` (initially, will change during demo)
- **Address**: Optional (can be null or minimal)

### 3. Invoice #1: Emergency Response (INV-2025-0001)

**Invoice Details**:
- **Customer**: Riverside Apartments LLC
- **Invoice Number**: `INV-2025-0001` (or auto-generated sequence)
- **Invoice Date**: Current date (or 1 week ago for demo)
- **Due Date**: 30 days from invoice date
- **Payment Terms**: `NET_30`
- **Status**: `SENT` (already sent, ready for payment demo)
- **Sent Date**: 1 day ago

**Line Items** (4 items):
1. **Emergency Response Fee**
   - Description: `Emergency Response Fee`
   - Quantity: `1`
   - Unit Price: `$450.00`
   - Discount Type: `NONE`
   - Discount Amount: `$0.00`
   - Tax Rate: `8.25%` (0.0825)
   - Line Total: `$487.13` (450 + 37.13 tax)

2. **Water Extraction**
   - Description: `Water Extraction (6 hours)`
   - Quantity: `1`
   - Unit Price: `$750.00`
   - Discount Type: `NONE`
   - Discount Amount: `$0.00`
   - Tax Rate: `8.25%`
   - Line Total: `$811.88` (750 + 61.88 tax)

3. **Equipment Setup**
   - Description: `Equipment Setup`
   - Quantity: `1`
   - Unit Price: `$2,100.00`
   - Discount Type: `NONE`
   - Discount Amount: `$0.00`
   - Tax Rate: `8.25%`
   - Line Total: `$2,273.25` (2100 + 173.25 tax)

4. **Anti-microbial Treatment**
   - Description: `Anti-microbial Treatment`
   - Quantity: `1`
   - Unit Price: `$950.00`
   - Discount Type: `NONE`
   - Discount Amount: `$0.00`
   - Tax Rate: `8.25%`
   - Line Total: `$1,028.38` (950 + 78.38 tax)

**Invoice Totals**:
- **Subtotal**: `$4,250.00`
- **Tax Amount**: `$350.63` (8.25% of $4,250)
- **Total Amount**: `$4,600.63`
- **Amount Paid**: `$0.00` (initially)
- **Balance Due**: `$4,600.63`

**Payment** (to be created):
- **Amount**: `$4,700.63` (overpayment by $100)
- **Payment Method**: `CREDIT_CARD`
- **Payment Date**: Current date
- **Status**: `COMPLETED`
- **Result**: Invoice status → `PAID`, Customer credit balance → `$100.00`

### 4. Invoice #2: Monitoring Phase (INV-2025-0002)

**Invoice Details**:
- **Customer**: Riverside Apartments LLC
- **Invoice Number**: `INV-2025-0002`
- **Invoice Date**: Current date (or today)
- **Due Date**: 30 days from invoice date
- **Payment Terms**: `NET_30`
- **Status**: `SENT` (ready for credit application demo)
- **Sent Date**: Today

**Line Items** (2 items + 1 credit line item):
1. **Drying Monitoring**
   - Description: `Drying Monitoring (7 days)`
   - Quantity: `1`
   - Unit Price: `$1,050.00`
   - Discount Type: `NONE`
   - Discount Amount: `$0.00`
   - Tax Rate: `8.25%`
   - Line Total: `$1,136.63` (1050 + 86.63 tax)

2. **Equipment Rental**
   - Description: `Equipment Rental`
   - Quantity: `1`
   - Unit Price: `$1,225.00`
   - Discount Type: `NONE`
   - Discount Amount: `$0.00`
   - Tax Rate: `8.25%`
   - Line Total: `$1,326.06` (1225 + 101.06 tax)

3. **Credit Applied** (auto-added when invoice marked as sent)
   - Description: `Credit Applied`
   - Quantity: `1`
   - Unit Price: `-$100.00` (negative)
   - Discount Type: `FIXED_AMOUNT`
   - Discount Amount: `$100.00`
   - Tax Rate: `0%` (credits don't have tax)
   - Line Total: `-$100.00`

**Invoice Totals** (after credit):
- **Subtotal**: `$2,275.00`
- **Tax Amount**: `$187.69` (8.25% of $2,275)
- **Discount**: `$100.00` (credit applied)
- **Total Amount**: `$2,362.69`
- **Amount Paid**: `$0.00` (initially)
- **Balance Due**: `$2,362.69`

**Partial Payment** (to be created during demo):
- **Amount**: `$1,500.00`
- **Payment Method**: `CREDIT_CARD`
- **Payment Date**: Current date
- **Status**: `COMPLETED`
- **Result**: Invoice status remains `SENT`, Balance due → `$862.69`

### 5. Refund Data (for Invoice #1)

**Refund Details**:
- **Invoice**: INV-2025-0001 (already paid)
- **Refund Amount**: `$500.00`
- **Reason**: `Quality issue resolved`
- **Apply as Credit**: `true`
- **Refund Date**: Current date
- **Result**: 
  - Invoice status: `PAID` → `SENT`
  - Invoice balance due: `$0.00` → `$500.00`
  - Customer credit balance: `$0.00` → `$500.00`
  - Payment history shows refund entry

### 6. Overdue Invoice (for Late Fee Demo)

**Invoice #3: Overdue Invoice**
- **Customer**: Riverside Apartments LLC
- **Invoice Number**: `INV-2025-0003`
- **Invoice Date**: 35 days ago
- **Due Date**: 31 days ago (overdue by 4 days)
- **Status**: `SENT`
- **Total Amount**: `$1,500.00`
- **Balance Due**: `$1,500.00`
- **Late Fee Line Item**: 
  - Description: `Late Fee (31+ days overdue)`
  - Amount: `$125.00`
  - Added by scheduled job (or manually for demo)

## Implementation Requirements

### Option 1: SQL Script (Recommended for Production)

Create a comprehensive SQL script (`docs/demo/seed_data.sql`) that:

1. **Clears existing data** (optional, with warning):
   ```sql
   -- WARNING: This deletes all data!
   TRUNCATE TABLE payments CASCADE;
   TRUNCATE TABLE line_items CASCADE;
   TRUNCATE TABLE invoices CASCADE;
   TRUNCATE TABLE customers CASCADE;
   TRUNCATE TABLE users CASCADE;
   TRUNCATE TABLE activity_feed CASCADE;
   ```

2. **Inserts users** with proper UUIDs and bcrypt password hashes

3. **Inserts customer** with proper UUID

4. **Links customer user** to customer entity via `customer_id` foreign key

5. **Inserts invoices** with:
   - Proper invoice numbers (using sequence or manual)
   - Correct dates (invoice date, due date, sent date)
   - Proper status (`SENT` or `PAID`)

6. **Inserts line items** with:
   - Correct calculations (subtotal, tax, line total)
   - Proper ordering (sequence numbers)
   - Tax rates (8.25% = 0.0825)

7. **Inserts payments** with:
   - Proper amounts
   - Payment methods
   - Links to invoices and customers

8. **Updates customer credit balance** after overpayment

9. **Inserts refunds** (if refund table exists) or creates negative payment entries

10. **Inserts activity feed entries** for domain events (optional but nice for demo)

### Option 2: API Call Script (Alternative)

Create a bash script (`scripts/seed-demo-data.sh`) that uses curl to:

1. Register/login as SysAdmin
2. Create customer via API
3. Create invoices via API
4. Record payments via API
5. Issue refunds via API

**Pros**: Tests actual API endpoints  
**Cons**: More complex, requires backend running

### Option 3: Spring Boot Data Initializer

Create a `@Component` class (`backend/src/main/java/com/invoiceme/infrastructure/data/DemoDataInitializer.java`) that:

1. Runs only in `demo` or `test` profile
2. Checks if data exists (idempotent)
3. Creates all entities programmatically
4. Uses repositories to persist

**Pros**: Type-safe, uses domain models  
**Cons**: Requires code changes, runs on startup

## Key Considerations

### 1. Invoice Number Generation
- Use `invoice_sequences` table if available
- Or manually set invoice numbers: `INV-2025-0001`, `INV-2025-0002`, `INV-2025-0003`
- Ensure sequence is updated after manual inserts

### 2. Money Precision
- Use `BigDecimal` with scale 2 (2 decimal places)
- Round using `HALF_UP` (banker's rounding)
- Example: `350.625` → `350.63`

### 3. Tax Calculations
- Tax rate: `8.25%` = `0.0825`
- Line item tax: `unit_price * quantity * tax_rate`
- Total tax: Sum of all line item taxes
- Round to 2 decimal places

### 4. Credit Application Logic
- When Invoice #2 is marked as `SENT`, system should:
  1. Check customer credit balance (`$100.00`)
  2. Create discount line item (`-$100.00`)
  3. Update invoice totals
  4. Deduct credit from customer balance
  5. Publish `CreditAppliedEvent`

### 5. State Transitions
- Invoice #1: `SENT` → `PAID` (after payment)
- Invoice #1: `PAID` → `SENT` (after refund)
- Invoice #2: `SENT` (remains `SENT` after partial payment)
- Invoice #3: `SENT` (overdue, ready for late fee demo)

### 6. Domain Events
- `InvoiceSentEvent` for Invoice #1 and #2
- `PaymentRecordedEvent` for payments
- `RefundIssuedEvent` for refund
- `CreditAppliedEvent` for credit application
- Events should be in `activity_feed` table (if exists)

### 7. Dates for Demo
- **Invoice #1**: Created 1 week ago, sent 1 day ago
- **Invoice #2**: Created today, sent today
- **Invoice #3**: Created 35 days ago, due 31 days ago (overdue)
- **Payments**: Today's date
- **Refund**: Today's date

## Deliverables

### 1. SQL Script (`docs/demo/seed_data.sql`)
- Complete SQL script with all INSERT statements
- Proper UUID generation
- Correct foreign key relationships
- Comments explaining each section
- Idempotent (can run multiple times safely)

### 2. Verification Script (`scripts/verify-seed-data.sh`)
- SQL queries to verify data was inserted correctly
- Checks:
  - User counts
  - Customer exists
  - Invoice counts and totals
  - Payment amounts
  - Credit balances
  - Line item counts

### 3. Documentation (`docs/demo/SEED_DATA_IMPLEMENTATION.md`)
- Instructions for running seed script
- Database connection details
- Troubleshooting guide
- Data verification steps

### 4. Alternative: API Script (`scripts/seed-demo-data-api.sh`)
- Bash script with curl commands
- JWT token handling
- Error handling
- Step-by-step execution

## Success Criteria

✅ **All data matches demo script exactly**:
- Same customer name, email, amounts
- Same line items with correct calculations
- Same invoice numbers (or sequential)
- Same payment amounts and methods
- Same credit balances after operations

✅ **Database integrity maintained**:
- All foreign keys valid
- All UUIDs properly generated
- All dates in correct format
- All money amounts with 2 decimal precision

✅ **Demo can run smoothly**:
- Admin can log in immediately
- Customer can log in immediately
- Invoices appear in correct state
- Payments can be recorded
- Refunds can be issued
- Credit application works

✅ **Scripts are production-ready**:
- Well-commented
- Idempotent (safe to run multiple times)
- Error handling included
- Verification steps provided

## Testing the Seed Data

After generating seed data, verify:

1. **Login Test**:
   - Admin: `admin@floodshield.com` / `admin123` ✅
   - Customer: `john@riversideapts.com` / `customer123` ✅

2. **Invoice #1 Verification**:
   - Status: `PAID` ✅
   - Total: `$4,600.63` ✅
   - Payment recorded: `$4,700.63` ✅
   - Credit balance: `$0.00` (after refund demo) ✅

3. **Invoice #2 Verification**:
   - Status: `SENT` ✅
   - Total: `$2,362.69` (after credit) ✅
   - Credit line item exists: `-$100.00` ✅
   - Balance due: `$862.69` (after partial payment) ✅

4. **Customer Verification**:
   - Credit balance: `$500.00` (after refund) ✅
   - 3 invoices linked ✅
   - User account linked ✅

5. **Dashboard Verification**:
   - Total Revenue MTD: Matches sum of paid invoices ✅
   - Outstanding Invoices: 2 invoices ✅
   - Active Customers: 1 ✅

## Important Notes

- **Do NOT modify production code** unless creating a data initializer component
- **Use SQL scripts** for production/demo database seeding
- **Test seed script** on local database first
- **Document all assumptions** (tax rates, dates, etc.)
- **Make script idempotent** (safe to re-run)

## Expected Outcome

After running this agent, you should have:
1. ✅ Complete SQL seed script ready for production
2. ✅ Verification script to check data integrity
3. ✅ Documentation for running seed data
4. ✅ Demo can execute flawlessly with pre-seeded data
5. ✅ All amounts, dates, and relationships match demo script exactly

This will enable a smooth, professional demo without manual data entry delays.

