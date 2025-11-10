#!/bin/bash
# Verification script for InvoiceMe demo seed data
# This script runs SQL queries to verify seed data was inserted correctly

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Database connection parameters (adjust as needed)
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-invoiceme}"
DB_USER="${DB_USER:-postgres}"

echo "=========================================="
echo "InvoiceMe Seed Data Verification"
echo "=========================================="
echo ""

# Function to run SQL query
run_query() {
    local query="$1"
    local description="$2"
    
    echo -e "${YELLOW}Checking: $description${NC}"
    psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "$query" || {
        echo -e "${RED}✗ Failed to run query${NC}"
        return 1
    }
}

# Check users
echo "=== USERS ==="
USER_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT COUNT(*) FROM users WHERE email IN ('admin@invoiceme.com', 'john@riversideapts.com');")
if [ "$USER_COUNT" -ge 2 ]; then
    echo -e "${GREEN}✓ Users: $USER_COUNT found (admin@invoiceme.com + john@riversideapts.com)${NC}"
else
    echo -e "${RED}✗ Users: Expected at least 2 (admin@invoiceme.com + john@riversideapts.com), found $USER_COUNT${NC}"
fi

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT email, full_name, role, status FROM users WHERE email IN ('admin@invoiceme.com', 'john@riversideapts.com') ORDER BY email;"
echo ""

# Check customer
echo "=== CUSTOMER ==="
CUSTOMER_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT COUNT(*) FROM customers WHERE email = 'john@riversideapts.com';")
if [ "$CUSTOMER_COUNT" -eq 1 ]; then
    echo -e "${GREEN}✓ Customer: 1 found${NC}"
else
    echo -e "${RED}✗ Customer: Expected 1, found $CUSTOMER_COUNT${NC}"
fi

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT company_name, email, credit_balance, status FROM customers WHERE email = 'john@riversideapts.com';"
echo ""

# Check invoices
echo "=== INVOICES ==="
INVOICE_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT COUNT(*) FROM invoices WHERE invoice_number LIKE 'INV-2025-%';")
if [ "$INVOICE_COUNT" -eq 3 ]; then
    echo -e "${GREEN}✓ Invoices: 3 found${NC}"
else
    echo -e "${RED}✗ Invoices: Expected 3, found $INVOICE_COUNT${NC}"
fi

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT invoice_number, status, total_amount, amount_paid, balance_due FROM invoices WHERE invoice_number LIKE 'INV-2025-%' ORDER BY invoice_number;"
echo ""

# Check line items
echo "=== LINE ITEMS ==="
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT i.invoice_number, COUNT(li.id) as line_item_count, SUM(li.unit_price * li.quantity) as subtotal FROM invoices i LEFT JOIN line_items li ON i.id = li.invoice_id WHERE i.invoice_number LIKE 'INV-2025-%' GROUP BY i.invoice_number ORDER BY i.invoice_number;"
echo ""

# Check payments
echo "=== PAYMENTS ==="
PAYMENT_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT COUNT(*) FROM payments p JOIN invoices i ON p.invoice_id = i.id WHERE i.invoice_number LIKE 'INV-2025-%';")
if [ "$PAYMENT_COUNT" -eq 2 ]; then
    echo -e "${GREEN}✓ Payments: 2 found${NC}"
else
    echo -e "${RED}✗ Payments: Expected 2, found $PAYMENT_COUNT${NC}"
fi

psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT i.invoice_number, p.amount, p.payment_method, p.status, p.payment_date FROM payments p JOIN invoices i ON p.invoice_id = i.id WHERE i.invoice_number LIKE 'INV-2025-%' ORDER BY i.invoice_number, p.created_at;"
echo ""

# Check credit balance
echo "=== CREDIT BALANCE ==="
CREDIT_BALANCE=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT credit_balance FROM customers WHERE email = 'john@riversideapts.com';")
echo "Customer credit balance: \$$CREDIT_BALANCE"
echo ""

# Check activity feed
echo "=== ACTIVITY FEED ==="
ACTIVITY_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -A -c "SELECT COUNT(*) FROM activity_feed af JOIN invoices i ON af.aggregate_id = i.id WHERE i.invoice_number LIKE 'INV-2025-%';")
echo "Activity feed entries: $ACTIVITY_COUNT"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT af.event_type, af.description, af.occurred_at FROM activity_feed af JOIN invoices i ON af.aggregate_id = i.id WHERE i.invoice_number LIKE 'INV-2025-%' ORDER BY af.occurred_at DESC LIMIT 10;"
echo ""

# Summary
echo "=========================================="
echo "Verification Complete"
echo "=========================================="
echo ""
echo "Expected Summary:"
echo "- Users: 1 new (Customer) + 1 existing (SysAdmin: admin@invoiceme.com)"
echo "- Customer: 1 (Riverside Apartments LLC)"
echo "- Invoices: 3 (INV-2025-0001, 0002, 0003)"
echo "- Payments: 2 (Invoice #1 full, Invoice #2 partial)"
echo "- Credit Balance: \$0.00 (credit applied to Invoice #2)"
echo ""

