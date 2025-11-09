#!/bin/bash

# InvoiceMe Deployment Verification Script
# Purpose: Verify that backend and frontend deployments are working correctly

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration (set via environment variables or defaults)
BACKEND_URL="${BACKEND_URL:-http://localhost:8080}"
FRONTEND_URL="${FRONTEND_URL:-http://localhost:3000}"
API_BASE_URL="${API_BASE_URL:-${BACKEND_URL}/api/v1}"

# Counters
PASSED=0
FAILED=0

# Helper functions
print_header() {
    echo ""
    echo "=========================================="
    echo "$1"
    echo "=========================================="
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
    ((PASSED++))
}

print_error() {
    echo -e "${RED}✗${NC} $1"
    ((FAILED++))
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

# Test function
test_endpoint() {
    local name=$1
    local url=$2
    local expected_status=${3:-200}
    local auth_header=${4:-""}
    
    if [ -n "$auth_header" ]; then
        response=$(curl -s -w "\n%{http_code}" -H "Authorization: Bearer $auth_header" "$url" || echo -e "\n000")
    else
        response=$(curl -s -w "\n%{http_code}" "$url" || echo -e "\n000")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq "$expected_status" ]; then
        print_success "$name (HTTP $http_code)"
        return 0
    else
        print_error "$name (HTTP $http_code, expected $expected_status)"
        if [ -n "$body" ]; then
            echo "  Response: $body"
        fi
        return 1
    fi
}

# Main verification
main() {
    print_header "InvoiceMe Deployment Verification"
    echo "Backend URL: $BACKEND_URL"
    echo "Frontend URL: $FRONTEND_URL"
    echo "API Base URL: $API_BASE_URL"
    echo ""
    
    # Backend Health Check
    print_header "Backend Health Checks"
    
    if test_endpoint "Backend Health Endpoint" "$BACKEND_URL/actuator/health" 200; then
        # Parse health response
        health_status=$(curl -s "$BACKEND_URL/actuator/health" | grep -o '"status":"[^"]*"' | cut -d'"' -f4 || echo "UNKNOWN")
        if [ "$health_status" = "UP" ]; then
            print_success "Backend status is UP"
        else
            print_error "Backend status is $health_status (expected UP)"
        fi
    fi
    
    # Backend API Endpoints
    print_header "Backend API Endpoints"
    
    # Test public endpoints (no auth required)
    # Note: Register endpoint may return 201 (created) or 400 (validation error)
    register_response=$(curl -s -w "\n%{http_code}" -X POST "$API_BASE_URL/auth/register" \
        -H "Content-Type: application/json" \
        -d "{\"email\":\"test-$(date +%s)@example.com\",\"password\":\"Test123!\",\"fullName\":\"Test User\"}" \
        || echo -e "\n000")
    register_code=$(echo "$register_response" | tail -n1)
    if [ "$register_code" -eq 201 ] || [ "$register_code" -eq 400 ]; then
        print_success "Register Endpoint is accessible (HTTP $register_code)"
    else
        print_error "Register Endpoint failed (HTTP $register_code)"
    fi
    
    # Test protected endpoints (will fail without auth, but should return 401, not 404)
    test_endpoint "Customers List Endpoint (requires auth)" "$API_BASE_URL/customers" 401
    
    # Frontend Accessibility
    print_header "Frontend Accessibility"
    
    if curl -s -o /dev/null -w "%{http_code}" "$FRONTEND_URL" | grep -q "200\|301\|302"; then
        print_success "Frontend is accessible"
    else
        print_error "Frontend is not accessible"
    fi
    
    # Database Connection (via backend)
    print_header "Database Connection"
    
    # Try to get a simple endpoint that requires DB
    db_test=$(curl -s -w "\n%{http_code}" "$API_BASE_URL/auth/register" \
        -H "Content-Type: application/json" \
        -d '{"email":"db-test-'$(date +%s)'@example.com","password":"Test123!","fullName":"DB Test"}' \
        | tail -n1)
    
    if [ "$db_test" -eq 201 ] || [ "$db_test" -eq 400 ]; then
        # 201 = success, 400 = validation error (DB is working)
        print_success "Database connection is working"
    else
        print_error "Database connection may be failing (HTTP $db_test)"
    fi
    
    # Environment Variables Check (backend)
    print_header "Environment Variables"
    
    # Check if backend is using production config
    if curl -s "$BACKEND_URL/actuator/info" | grep -q "production\|prod"; then
        print_success "Backend appears to be in production mode"
    else
        print_warning "Backend may not be in production mode (check SPRING_PROFILES_ACTIVE)"
    fi
    
    # Summary
    print_header "Verification Summary"
    echo "Passed: $PASSED"
    echo "Failed: $FAILED"
    echo ""
    
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}All checks passed! Deployment is working correctly.${NC}"
        exit 0
    else
        echo -e "${RED}Some checks failed. Please review the errors above.${NC}"
        exit 1
    fi
}

# Run main function
main

