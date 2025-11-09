#!/bin/bash

# Performance Testing Script
# Measures API latency (p50, p95, p99) for key endpoints

set -e

BASE_URL="${API_URL:-http://localhost:8080/api/v1}"
TOKEN=""
REQUESTS=100

echo "=========================================="
echo "InvoiceMe Performance Testing"
echo "=========================================="
echo "Base URL: $BASE_URL"
echo "Requests per endpoint: $REQUESTS"
echo ""

# Check if ab (Apache Bench) is installed
if ! command -v ab &> /dev/null; then
    echo "Error: 'ab' (Apache Bench) is not installed."
    echo "Install with: brew install httpd (macOS) or apt-get install apache2-utils (Linux)"
    exit 1
fi

# Get authentication token
echo "Getting authentication token..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{
        "email": "test@example.com",
        "password": "password123"
    }')
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo "Error: Failed to get token. Please ensure backend is running and test user exists."
    exit 1
fi

echo "Token obtained."
echo ""

# Create test data
echo "Creating test data..."
CUSTOMER_RESPONSE=$(curl -s -X POST "$BASE_URL/customers" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "companyName": "Performance Test Company",
        "contactName": "Test User",
        "email": "perf@example.com",
        "phone": "555-0000",
        "customerType": "COMMERCIAL"
    }')
CUSTOMER_ID=$(echo "$CUSTOMER_RESPONSE" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

INVOICE_RESPONSE=$(curl -s -X POST "$BASE_URL/invoices" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
        \"customerId\": \"$CUSTOMER_ID\",
        \"issueDate\": \"$(date +%Y-%m-%d)\",
        \"paymentTerms\": \"NET_30\",
        \"lineItems\": [
            {
                \"description\": \"Test Item\",
                \"quantity\": 1,
                \"unitPrice\": 100.00,
                \"discountType\": \"NONE\",
                \"discountValue\": 0,
                \"taxRate\": 0.10
            }
        ]
    }")
INVOICE_ID=$(echo "$INVOICE_RESPONSE" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

echo "Test data created (Customer: $CUSTOMER_ID, Invoice: $INVOICE_ID)"
echo ""

# Function to run performance test
run_perf_test() {
    local name=$1
    local method=$2
    local endpoint=$3
    local data_file=$4
    
    echo "Testing $name..."
    
    # Create temporary file for ab output
    local temp_file=$(mktemp)
    
    if [ "$method" = "GET" ]; then
        ab -n $REQUESTS -c 10 \
            -H "Authorization: Bearer $TOKEN" \
            "$BASE_URL$endpoint" > "$temp_file" 2>&1
    elif [ "$method" = "POST" ]; then
        if [ -n "$data_file" ]; then
            ab -n $REQUESTS -c 10 \
                -p "$data_file" \
                -T "application/json" \
                -H "Authorization: Bearer $TOKEN" \
                "$BASE_URL$endpoint" > "$temp_file" 2>&1
        else
            echo "Error: POST request requires data file"
            return 1
        fi
    fi
    
    # Extract metrics
    local mean=$(grep "Time per request" "$temp_file" | head -1 | awk '{print $4}')
    local p50=$(grep "50%" "$temp_file" | awk '{print $2}')
    local p95=$(grep "95%" "$temp_file" | awk '{print $2}')
    local p99=$(grep "99%" "$temp_file" | awk '{print $2}')
    
    echo "  Mean: ${mean}ms"
    echo "  p50: ${p50}ms"
    echo "  p95: ${p95}ms"
    echo "  p99: ${p99}ms"
    echo ""
    
    # Cleanup
    rm -f "$temp_file"
    
    # Return p95 for comparison
    echo "$p95"
}

# Create data files for POST requests
POST_CUSTOMER_DATA=$(mktemp)
cat > "$POST_CUSTOMER_DATA" <<EOF
{
    "companyName": "Perf Test",
    "contactName": "Test",
    "email": "perf$(date +%s)@example.com",
    "phone": "555-0000",
    "customerType": "COMMERCIAL"
}
EOF

POST_PAYMENT_DATA=$(mktemp)
cat > "$POST_PAYMENT_DATA" <<EOF
{
    "invoiceId": "$INVOICE_ID",
    "amount": 110.00,
    "paymentMethod": "CREDIT_CARD",
    "paymentDate": "$(date +%Y-%m-%d)"
}
EOF

# Run performance tests
echo "=== Performance Test Results ==="
echo ""

run_perf_test "POST /customers" "POST" "/customers" "$POST_CUSTOMER_DATA"
run_perf_test "GET /customers/{id}" "GET" "/customers/$CUSTOMER_ID" ""
run_perf_test "GET /customers" "GET" "/customers?page=0&size=20" ""
run_perf_test "POST /invoices" "POST" "/invoices" ""
run_perf_test "GET /invoices/{id}" "GET" "/invoices/$INVOICE_ID" ""
run_perf_test "GET /invoices" "GET" "/invoices?page=0&size=20" ""
run_perf_test "POST /payments" "POST" "/payments" "$POST_PAYMENT_DATA"

# Cleanup
rm -f "$POST_CUSTOMER_DATA" "$POST_PAYMENT_DATA"

echo "Performance testing complete!"
echo ""
echo "Target: p95 < 200ms for CRUD operations"

