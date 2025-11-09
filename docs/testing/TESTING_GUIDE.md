# M2 Testing Guide

**Date**: 2025-01-27  
**Purpose**: Comprehensive testing guide for M2 implementation

---

## Prerequisites

1. **Start the application**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **Database**: Ensure PostgreSQL is running and migrations are applied

3. **JWT Secret**: Set `jwt.secret` in `application.yml`

---

## Test Scripts

### 1. Authentication

#### Register User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

#### Login (Get JWT Token)
```bash
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }' | jq -r '.token')

echo "Token: $TOKEN"
```

---

### 2. Customer CRUD Tests

#### Create Customer (Requires: SYSADMIN, ACCOUNTANT, or SALES)
```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "companyName": "Test Company",
    "contactName": "John Doe",
    "email": "customer@example.com",
    "phone": "555-1234",
    "customerType": "COMMERCIAL"
  }'
```

**Expected**: 201 Created with CustomerDto

#### Get Customer
```bash
CUSTOMER_ID="<customer-id-from-create>"
curl -X GET http://localhost:8080/api/v1/customers/$CUSTOMER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with CustomerDetailResponse

#### List Customers
```bash
curl -X GET "http://localhost:8080/api/v1/customers?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with PagedCustomerResponse

#### Update Customer (Requires: SYSADMIN, ACCOUNTANT, or SALES)
```bash
curl -X PUT http://localhost:8080/api/v1/customers/$CUSTOMER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "companyName": "Updated Company",
    "contactName": "Jane Doe",
    "email": "updated@example.com",
    "phone": "555-5678"
  }'
```

**Expected**: 200 OK with CustomerDto

#### Delete Customer (Requires: SYSADMIN)
```bash
curl -X DELETE http://localhost:8080/api/v1/customers/$CUSTOMER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 204 No Content

---

### 3. Invoice CRUD Tests

#### Create Invoice (Requires: SYSADMIN, ACCOUNTANT, or SALES)
```bash
curl -X POST http://localhost:8080/api/v1/invoices \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "customerId": "'$CUSTOMER_ID'",
    "issueDate": "2025-01-27",
    "paymentTerms": "NET_30",
    "lineItems": [
      {
        "description": "Test Item",
        "quantity": 2,
        "unitPrice": 100.00,
        "discountType": "NONE",
        "discountValue": 0,
        "taxRate": 0.10
      }
    ]
  }'
```

**Expected**: 201 Created with InvoiceDto

#### Get Invoice
```bash
INVOICE_ID="<invoice-id-from-create>"
curl -X GET http://localhost:8080/api/v1/invoices/$INVOICE_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with InvoiceDetailResponse

#### Mark Invoice as Sent (Requires: SYSADMIN, ACCOUNTANT, or SALES)
```bash
curl -X PATCH http://localhost:8080/api/v1/invoices/$INVOICE_ID/mark-as-sent \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with InvoiceDto  
**Verify**: Check logs for InvoiceSentEvent and email service calls

---

### 4. Payment Tests

#### Record Payment (Requires: SYSADMIN, ACCOUNTANT, or CUSTOMER with ownership)
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "invoiceId": "'$INVOICE_ID'",
    "amount": 220.00,
    "paymentMethod": "CREDIT_CARD",
    "paymentDate": "2025-01-27"
  }'
```

**Expected**: 201 Created with PaymentDto  
**Verify**: 
- Invoice status changes to PAID
- PaymentRecordedEvent published
- InvoiceFullyPaidEvent published (if full payment)
- Check activity_feed table for events

#### Test Overpayment → Credit
```bash
curl -X POST http://localhost:8080/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "invoiceId": "'$INVOICE_ID'",
    "amount": 250.00,
    "paymentMethod": "CREDIT_CARD",
    "paymentDate": "2025-01-27"
  }'
```

**Expected**: 
- Payment recorded
- Invoice fully paid
- Customer credit balance increased by $30

---

### 5. Refund Tests

#### Issue Refund (Requires: SYSADMIN)
```bash
curl -X POST http://localhost:8080/api/v1/refunds \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "invoiceId": "'$INVOICE_ID'",
    "amount": 50.00,
    "reason": "Service dispute",
    "applyAsCredit": false
  }'
```

**Expected**: 201 Created with PaymentDto  
**Verify**:
- Invoice amountPaid reduced
- Invoice status changes from PAID → SENT (if partial refund)
- RefundIssuedEvent published

---

### 6. Dashboard Tests

#### Get Metrics (Requires: SYSADMIN, ACCOUNTANT, or SALES)
```bash
curl -X GET http://localhost:8080/api/v1/dashboard/metrics \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with DashboardMetricsResponse

#### Get Revenue Trend
```bash
curl -X GET "http://localhost:8080/api/v1/dashboard/revenue-trend?period=MONTHLY" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with RevenueTrendResponse

#### Get Invoice Status
```bash
curl -X GET http://localhost:8080/api/v1/dashboard/invoice-status \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with InvoiceStatusResponse

#### Get Aging Report
```bash
curl -X GET http://localhost:8080/api/v1/dashboard/aging-report \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with AgingReportResponse

---

### 7. User Approval Tests

#### Get Pending Users (Requires: SYSADMIN or ACCOUNTANT)
```bash
curl -X GET http://localhost:8080/api/v1/users/pending \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 200 OK with List<PendingUserDto>

#### Approve User (Requires: SYSADMIN or ACCOUNTANT)
```bash
USER_ID="<pending-user-id>"
curl -X POST http://localhost:8080/api/v1/users/$USER_ID/approve \
  -H "Authorization: Bearer $TOKEN"
```

**Expected**: 204 No Content  
**Verify**: User status changes to ACTIVE

#### Reject User (Requires: SYSADMIN or ACCOUNTANT)
```bash
curl -X POST http://localhost:8080/api/v1/users/$USER_ID/reject \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '"Reason for rejection"'
```

**Expected**: 204 No Content  
**Verify**: User status changes to INACTIVE

---

## RBAC Verification Tests

### Test Unauthorized Access

#### Try to Delete Customer as SALES (Should Fail)
```bash
# Login as SALES user
SALES_TOKEN="<sales-user-token>"

curl -X DELETE http://localhost:8080/api/v1/customers/$CUSTOMER_ID \
  -H "Authorization: Bearer $SALES_TOKEN"
```

**Expected**: 403 Forbidden

#### Try to Cancel Invoice as SALES (Should Fail)
```bash
curl -X DELETE http://localhost:8080/api/v1/invoices/$INVOICE_ID \
  -H "Authorization: Bearer $SALES_TOKEN"
```

**Expected**: 403 Forbidden

#### Try to Issue Refund as ACCOUNTANT (Should Fail)
```bash
ACCOUNTANT_TOKEN="<accountant-user-token>"

curl -X POST http://localhost:8080/api/v1/refunds \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ACCOUNTANT_TOKEN" \
  -d '{
    "invoiceId": "'$INVOICE_ID'",
    "amount": 50.00,
    "reason": "Test"
  }'
```

**Expected**: 403 Forbidden (Only SYSADMIN can issue refunds)

---

## Domain Event Verification

### Check Activity Feed
```sql
SELECT * FROM activity_feed ORDER BY occurred_at DESC LIMIT 20;
```

**Expected**: Events logged for:
- InvoiceSentEvent
- PaymentRecordedEvent
- InvoiceFullyPaidEvent
- RefundIssuedEvent

### Check Email Service Logs
Look for log messages:
- "Invoice email sent to..."
- "Payment confirmation email sent to..."
- "Payment completion email sent to..."

---

## Scheduled Jobs Testing

### Test Recurring Invoice Job
1. Create a RecurringInvoiceTemplate with `nextInvoiceDate` = today
2. Manually trigger the job or wait for scheduled time
3. Verify invoice is generated
4. Verify `nextInvoiceDate` is updated

### Test Late Fee Job
1. Create an overdue invoice (dueDate < today, status = SENT)
2. Wait for 1st of month at midnight CT or manually trigger
3. Verify late fee line item added
4. Verify LateFeeAppliedEvent published

---

## Test Results Template

### Endpoint Status
| Endpoint | Method | Status | Notes |
|----------|--------|--------|-------|
| POST /api/v1/customers | POST | ✅/❌ | |
| GET /api/v1/customers/{id} | GET | ✅/❌ | |
| ... | ... | ... | ... |

### RBAC Verification
| Endpoint | Role | Expected | Actual | Status |
|----------|------|----------|--------|--------|
| DELETE /customers/{id} | SALES | 403 | | ✅/❌ |
| POST /refunds | ACCOUNTANT | 403 | | ✅/❌ |

### Domain Events
| Event | Triggered | Listener Executed | Status |
|-------|-----------|-------------------|--------|
| InvoiceSentEvent | ✅/❌ | ✅/❌ | ✅/❌ |
| PaymentRecordedEvent | ✅/❌ | ✅/❌ | ✅/❌ |

### Issues Found
1. [Issue description]
2. [Issue description]

---

## Notes

- All endpoints require JWT authentication (except `/auth/**`)
- RBAC is enforced via `@PreAuthorize` annotations
- Domain events are published after transaction commit
- Email service failures don't break transactions
- Scheduled jobs run in Central Time (America/Chicago)

