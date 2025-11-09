# Dashboard 500 Error - Root Cause and Fix

## Problem Summary

**Login works ✅** but **all authenticated endpoints return 500 errors** including:
- `/api/v1/dashboard/metrics`
- `/api/v1/dashboard/revenue-trend` 
- `/api/v1/dashboard/invoice-status`
- `/api/v1/dashboard/aging-report`
- `/api/v1/customers`

##Root Cause

The backend process (PID 22385) was started at 3:47 PM but may have runtime errors preventing endpoint execution. All compilation is successful, JWT authentication works, but something is failing during request processing.

## Investigation Results

### ✅ What Works
- Admin login endpoint (`/api/v1/auth/login`) - returns 200 OK with valid JWT token
- JWT token generation and validation
- Database connection (admin user verified)
- Code compilation (mvn compile succeeds)

### ❌ What Fails
- **ALL authenticated endpoints return HTTP 500**
- Dashboard metrics endpoint
- Dashboard revenue trend endpoint  
- Dashboard invoice status endpoint
- Dashboard aging report endpoint
- Customers endpoint

### Database State
- 0 customers
- 0 invoices
- 1 admin user (verified)

## Likely Issues

### 1. Runtime Exception in Handlers
The dashboard handlers should work with empty data (they use `Money.zero()`), but there might be:
- NullPointerException in some handler
- Issue with Spring Security context
- Problem extracting user from JWT
- Database query failing

### 2. Backend Not Restarted
The backend process started at 3:47 PM but code changes may have been made after that time. The running process might:
- Have old code loaded  
- Be throwing exceptions on every request
- Have cached bad state

## Immediate Fix

**RESTART THE BACKEND**

```bash
# Find and kill the current process
ps aux | grep "spring-boot:run" | grep -v grep
kill 22385

# Navigate to backend directory
cd /Users/mylessjs/Desktop/InvoiceMe/backend

# Start backend
mvn spring-boot:run
```

## Watch for Errors

After restarting, monitor the console output for:

1. **Stack traces** during requests
2. **NullPointerException** or **IllegalArgumentException**
3. **SQL errors** from repository methods
4. **Spring Security** errors
5. **JSON serialization** errors (Jackson)

## Test After Restart

```bash
# Get fresh JWT token
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@invoiceme.com","password":"Admin123!"}' \
  | jq -r '.token')

# Test dashboard endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/dashboard/metrics | jq '.'

# Test customers endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/customers | jq '.'
```

## Expected Behavior

With empty database, endpoints should return:

### Dashboard Metrics
```json
{
  "revenueMTD": {"amount": 0.00, "currency": "USD"},
  "outstandingInvoicesCount": 0,
  "outstandingInvoicesAmount": {"amount": 0.00, "currency": "USD"},
  "overdueInvoicesCount": 0,
  "overdueInvoicesAmount": {"amount": 0.00, "currency": "USD"},
  "activeCustomersCount": 0,
  "asOfDate": "2025-11-08"
}
```

### Customers
```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0
}
```

## If Problem Persists

Look in backend console for specific exceptions. Common causes:
1. **MoneyConverter issue** - Currency parsing
2. **Enum conversion** - Role/Status mapping  
3. **@PreAuthorize** - Security expression evaluation
4. **Repository query** - SQL/HQL syntax
5. **DTO mapping** - Missing fields or constructors

---

**Status**: ⚠️ NEEDS BACKEND RESTART  
**Last Updated**: Nov 8, 2025, 3:56 PM  
**Next Action**: Restart backend and observe console output during API requests

