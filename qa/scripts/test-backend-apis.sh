#!/bin/bash

# Backend API Testing Script
# Tests all endpoints from TESTING_GUIDE.md

set -e

BASE_URL="${API_URL:-http://localhost:8080/api/v1}"
TOKEN=""
CUSTOMER_ID=""
INVOICE_ID=""
PAYMENT_ID=""

echo "=========================================="
echo "InvoiceMe Backend API Testing"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
PASSED=0
FAILED=0

test_endpoint() {
    local name=$1
    local method=$2
    local endpoint=$3
    local data=$4
    local expected_status=$5
    
    echo -n "Testing $name... "
    
    if [ "$method" = "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" -X GET "$BASE_URL$endpoint" \
            ${TOKEN:+-H "Authorization: Bearer $TOKEN"})
    elif [ "$method" = "POST" ]; then
        response=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            ${TOKEN:+-H "Authorization: Bearer $TOKEN"} \
            ${data:+-d "$data"})
    elif [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PUT "$BASE_URL$endpoint" \
            -H "Content-Type: application/json" \
            ${TOKEN:+-H "Authorization: Bearer $TOKEN"} \
            ${data:+-d "$data"})
    elif [ "$method" = "PATCH" ]; then
        response=$(curl -s -w "\n%{http_code}" -X PATCH "$BASE_URL$endpoint" \
            ${TOKEN:+-H "Authorization: Bearer $TOKEN"})
    elif [ "$method" = "DELETE" ]; then
        response=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL$endpoint" \
            ${TOKEN:+-H "Authorization: Bearer $TOKEN"})
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASS${NC} (HTTP $http_code)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAIL${NC} (Expected HTTP $expected_status, got $http_code)"
        echo "Response: $body"
        ((FAILED++))
        return 1
    fi
}

# 1. Authentication Tests
echo "=== Authentication Tests ==="
echo "Registering test user..."
# Generate unique email with timestamp
TEST_EMAIL="test$(date +%s)@example.com"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
        \"email\": \"$TEST_EMAIL\",
        \"password\": \"password123\",
        \"firstName\": \"Test\",
        \"lastName\": \"User\"
    }")
if [ "$HTTP_CODE" = "201" ]; then
    echo -e "${GREEN}✓ Registration successful (HTTP $HTTP_CODE)${NC}"
    ((PASSED++))
    
    # Get user ID from database to approve
    USER_ID=$(docker exec invoiceme-postgres psql -U postgres -d invoiceme -t -c "SELECT id FROM users WHERE email = '$TEST_EMAIL' ORDER BY created_at DESC LIMIT 1;" 2>/dev/null | tr -d ' ')
    
    if [ -n "$USER_ID" ]; then
        echo "Approving user $USER_ID..."
        # Note: This requires an admin token. For now, we'll manually approve via direct DB update
        docker exec invoiceme-postgres psql -U postgres -d invoiceme -c "UPDATE users SET status = 'ACTIVE' WHERE id = '$USER_ID';" > /dev/null 2>&1
        echo "User approved."
        
        echo "Logging in..."
        LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
            -H "Content-Type: application/json" \
            -d "{
                \"email\": \"$TEST_EMAIL\",
                \"password\": \"password123\"
            }")
        TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
        
        if [ -n "$TOKEN" ]; then
            echo -e "${GREEN}✓ Login successful${NC}"
            ((PASSED++))
        else
            echo -e "${YELLOW}⚠ Login failed, but continuing without token${NC}"
            TOKEN=""
            ((FAILED++))
        fi
    else
        echo -e "${YELLOW}⚠ Could not find user ID, skipping approval${NC}"
        TOKEN=""
    fi
else
    echo -e "${RED}✗ Registration failed (HTTP $HTTP_CODE)${NC}"
    ((FAILED++))
    TOKEN=""
fi
echo ""

if [ -n "$TOKEN" ]; then
    echo "Token obtained: ${TOKEN:0:20}..."
else
    echo -e "${YELLOW}⚠ No token available. Some tests may fail.${NC}"
fi
echo ""

# 2. Customer CRUD Tests
echo "=== Customer CRUD Tests ==="
test_endpoint "Create Customer" "POST" "/customers" \
    '{
        "companyName": "Test Company",
        "contactName": "John Doe",
        "email": "customer@example.com",
        "phone": "555-1234",
        "customerType": "COMMERCIAL"
    }' "201"

CUSTOMER_ID=$(echo "$body" | grep -o '"id":"[^"]*' | cut -d'"' -f4 || echo "")
echo "Customer ID: $CUSTOMER_ID"
echo ""

test_endpoint "Get Customer" "GET" "/customers/$CUSTOMER_ID" "" "200"
test_endpoint "List Customers" "GET" "/customers?page=0&size=20" "" "200"
test_endpoint "Update Customer" "PUT" "/customers/$CUSTOMER_ID" \
    '{
        "companyName": "Updated Company",
        "contactName": "Jane Doe",
        "email": "updated@example.com",
        "phone": "555-5678"
    }' "200"
echo ""

# 3. Invoice CRUD Tests
echo "=== Invoice CRUD Tests ==="
test_endpoint "Create Invoice" "POST" "/invoices" \
    "{
        \"customerId\": \"$CUSTOMER_ID\",
        \"issueDate\": \"$(date +%Y-%m-%d)\",
        \"paymentTerms\": \"NET_30\",
        \"lineItems\": [
            {
                \"description\": \"Test Item\",
                \"quantity\": 2,
                \"unitPrice\": 100.00,
                \"discountType\": \"NONE\",
                \"discountValue\": 0,
                \"taxRate\": 0.10
            }
        ]
    }" "201"

INVOICE_ID=$(echo "$body" | grep -o '"id":"[^"]*' | cut -d'"' -f4 || echo "")
echo "Invoice ID: $INVOICE_ID"
echo ""

test_endpoint "Get Invoice" "GET" "/invoices/$INVOICE_ID" "" "200"
test_endpoint "List Invoices" "GET" "/invoices?page=0&size=20" "" "200"
test_endpoint "Mark Invoice as Sent" "PATCH" "/invoices/$INVOICE_ID/mark-as-sent" "" "200"
echo ""

# 4. Payment Tests
echo "=== Payment Tests ==="
test_endpoint "Record Payment" "POST" "/payments" \
    "{
        \"invoiceId\": \"$INVOICE_ID\",
        \"amount\": 220.00,
        \"paymentMethod\": \"CREDIT_CARD\",
        \"paymentDate\": \"$(date +%Y-%m-%d)\"
    }" "201"

PAYMENT_ID=$(echo "$body" | grep -o '"id":"[^"]*' | cut -d'"' -f4 || echo "")
echo "Payment ID: $PAYMENT_ID"
echo ""

test_endpoint "Get Payment" "GET" "/payments/$PAYMENT_ID" "" "200"
test_endpoint "List Payments" "GET" "/payments?page=0&size=20" "" "200"
echo ""

# 5. Dashboard Tests
echo "=== Dashboard Tests ==="
test_endpoint "Get Metrics" "GET" "/dashboard/metrics" "" "200"
test_endpoint "Get Revenue Trend" "GET" "/dashboard/revenue-trend?period=MONTHLY" "" "200"
test_endpoint "Get Invoice Status" "GET" "/dashboard/invoice-status" "" "200"
test_endpoint "Get Aging Report" "GET" "/dashboard/aging-report" "" "200"
echo ""

# Summary
echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "${GREEN}Passed: $PASSED${NC}"
echo -e "${RED}Failed: $FAILED${NC}"
echo "Total: $((PASSED + FAILED))"
echo ""

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi

