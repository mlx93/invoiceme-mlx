# E2E Flow Test Evidence

**Date**: 2025-01-27  
**Environment**: Local Development  
**Test Duration**: [To be filled when tests executed]  
**Status**: ⚠️ **PENDING EXECUTION** - Reports generated based on requirements

---

## Executive Summary

This report documents step-by-step evidence of the end-to-end (E2E) flow test: Customer → Invoice → Payment. The test verifies the complete business workflow from customer creation through invoice payment, including domain event firing and email notifications.

**E2E Flow**: Create Customer → Create Invoice → Mark Invoice as Sent → Record Payment → Verify Invoice Paid

---

## 1. E2E Flow Test Steps

### Step 1: Create Customer via Frontend

**Action**: Navigate to `/customers/new` and create a new customer

**Test Data**:
- Company Name: "Test Company E2E"
- Contact Name: "John Doe"
- Email: "teste2e@example.com"
- Phone: "555-1234"
- Customer Type: Commercial

**Expected Result**: Customer created successfully, redirected to customer detail page

**Actual Result**: [Pending execution]

**Screenshot**: [To be added after execution]

**API Request**:
```http
POST /api/v1/customers
Authorization: Bearer <token>
Content-Type: application/json

{
  "companyName": "Test Company E2E",
  "contactName": "John Doe",
  "email": "teste2e@example.com",
  "phone": "555-1234",
  "customerType": "COMMERCIAL"
}
```

**API Response**: [To be added after execution]

**Status**: ⚠️ **PENDING EXECUTION**

---

### Step 2: Create Invoice via Frontend

**Action**: Navigate to `/invoices/new` and create a new invoice for the customer

**Test Data**:
- Customer: Test Company E2E (from Step 1)
- Issue Date: Today
- Payment Terms: NET_30
- Line Items:
  - Description: "Test Service"
  - Quantity: 2
  - Unit Price: $100.00
  - Discount Type: None
  - Tax Rate: 10%

**Expected Result**: 
- Invoice created with status = DRAFT
- Invoice totals calculated:
  - Subtotal: $200.00 (2 × $100.00)
  - Tax: $20.00 (10% of $200.00)
  - Total: $220.00
  - Balance Due: $220.00

**Actual Result**: [Pending execution]

**Screenshot**: [To be added after execution]

**API Request**:
```http
POST /api/v1/invoices
Authorization: Bearer <token>
Content-Type: application/json

{
  "customerId": "<customer-id-from-step-1>",
  "issueDate": "2025-01-27",
  "paymentTerms": "NET_30",
  "lineItems": [
    {
      "description": "Test Service",
      "quantity": 2,
      "unitPrice": 100.00,
      "discountType": "NONE",
      "discountValue": 0,
      "taxRate": 0.10
    }
  ]
}
```

**API Response**: [To be added after execution]

**Status**: ⚠️ **PENDING EXECUTION**

---

### Step 3: Mark Invoice as Sent via Frontend

**Action**: On invoice detail page, click "Mark as Sent" button

**Expected Result**: 
- Invoice status changed from DRAFT → SENT
- Invoice sentDate is set
- InvoiceSentEvent published
- Email notification sent to customer
- Activity feed entry created

**Actual Result**: [Pending execution]

**Screenshot**: [To be added after execution]

**API Request**:
```http
PATCH /api/v1/invoices/<invoice-id>/mark-as-sent
Authorization: Bearer <token>
```

**API Response**: [To be added after execution]

**Database Verification** (activity_feed table):
```sql
SELECT * FROM activity_feed 
WHERE event_type = 'InvoiceSentEvent' 
ORDER BY occurred_at DESC 
LIMIT 1;
```

**Expected**: Entry with event_type = 'InvoiceSentEvent', aggregate_id = invoice ID

**Actual**: [Pending execution]

**Email Service Logs**: [To be added after execution]

**Status**: ⚠️ **PENDING EXECUTION**

---

### Step 4: Record Payment via Frontend

**Action**: On invoice detail page, click "Record Payment" button and submit payment form

**Test Data**:
- Amount: $220.00 (full invoice amount)
- Payment Method: Credit Card
- Payment Date: Today

**Expected Result**: 
- Payment recorded successfully
- Invoice amountPaid = $220.00
- Invoice balanceDue = $0.00
- Invoice status changed from SENT → PAID
- Invoice paidDate is set
- PaymentRecordedEvent published
- InvoiceFullyPaidEvent published (since balance = 0)
- Email notification sent to customer
- Activity feed entries created

**Actual Result**: [Pending execution]

**Screenshot**: [To be added after execution]

**API Request**:
```http
POST /api/v1/payments
Authorization: Bearer <token>
Content-Type: application/json

{
  "invoiceId": "<invoice-id-from-step-2>",
  "amount": 220.00,
  "paymentMethod": "CREDIT_CARD",
  "paymentDate": "2025-01-27"
}
```

**API Response**: [To be added after execution]

**Database Verification** (invoices table):
```sql
SELECT id, status, amount_paid, balance_due, paid_date 
FROM invoices 
WHERE id = '<invoice-id>';
```

**Expected**: 
- status = 'PAID'
- amount_paid = 220.00
- balance_due = 0.00
- paid_date is not null

**Actual**: [Pending execution]

**Status**: ⚠️ **PENDING EXECUTION**

---

### Step 5: Verify Invoice Status Changed to PAID

**Action**: Refresh invoice detail page and verify status

**Expected Result**: 
- Invoice status displayed as "PAID"
- Balance Due displayed as "$0.00"
- Paid Date displayed
- Payment history shows recorded payment

**Actual Result**: [Pending execution]

**Screenshot**: [To be added after execution]

**Status**: ⚠️ **PENDING EXECUTION**

---

### Step 6: Verify Domain Events Fired

**Action**: Check activity_feed table for event entries

**Expected Events**:
1. InvoiceSentEvent (from Step 3)
2. PaymentRecordedEvent (from Step 4)
3. InvoiceFullyPaidEvent (from Step 4)

**Database Query**:
```sql
SELECT * FROM activity_feed 
WHERE aggregate_id = '<invoice-id>' 
ORDER BY occurred_at ASC;
```

**Expected Results**:
- 3 entries total
- Entry 1: event_type = 'InvoiceSentEvent'
- Entry 2: event_type = 'PaymentRecordedEvent'
- Entry 3: event_type = 'InvoiceFullyPaidEvent'

**Actual Results**: [Pending execution]

**Screenshot**: [To be added after execution]

**Status**: ⚠️ **PENDING EXECUTION**

---

### Step 7: Verify Email Notifications Sent

**Action**: Check email service logs or email inbox

**Expected Emails**:
1. Invoice Sent email (from Step 3)
   - To: teste2e@example.com
   - Subject: "Invoice [Number] from InvoiceMe"
   - Body: Invoice summary, due date, link to portal

2. Payment Confirmation email (from Step 4)
   - To: teste2e@example.com
   - Subject: "Payment Received - Invoice [Number]"
   - Body: Payment details, remaining balance

3. Payment Completion email (from Step 4)
   - To: teste2e@example.com
   - Subject: "Invoice [Number] Fully Paid"
   - Body: Invoice fully paid confirmation

**Email Service Logs**: [To be added after execution]

**Screenshot**: [To be added after execution]

**Status**: ⚠️ **PENDING EXECUTION**

---

## 2. Database State Verification

### 2.1 Customers Table

**Query**:
```sql
SELECT id, company_name, email, status, credit_balance 
FROM customers 
WHERE email = 'teste2e@example.com';
```

**Expected**: Customer record with status = 'ACTIVE', credit_balance = 0.00

**Actual**: [Pending execution]

**Screenshot**: [To be added after execution]

---

### 2.2 Invoices Table

**Query**:
```sql
SELECT id, invoice_number, customer_id, status, total_amount, amount_paid, balance_due, sent_date, paid_date 
FROM invoices 
WHERE customer_id = '<customer-id>';
```

**Expected**: Invoice record with:
- status = 'PAID'
- total_amount = 220.00
- amount_paid = 220.00
- balance_due = 0.00
- sent_date is not null
- paid_date is not null

**Actual**: [Pending execution]

**Screenshot**: [To be added after execution]

---

### 2.3 Payments Table

**Query**:
```sql
SELECT id, invoice_id, customer_id, amount, payment_method, payment_date, status 
FROM payments 
WHERE invoice_id = '<invoice-id>';
```

**Expected**: Payment record with:
- amount = 220.00
- payment_method = 'CREDIT_CARD'
- status = 'COMPLETED'

**Actual**: [Pending execution]

**Screenshot**: [To be added after execution]

---

### 2.4 Activity Feed Table

**Query**:
```sql
SELECT id, aggregate_id, event_type, description, occurred_at 
FROM activity_feed 
WHERE aggregate_id IN ('<customer-id>', '<invoice-id>', '<payment-id>') 
ORDER BY occurred_at ASC;
```

**Expected**: Multiple entries for:
- Customer creation (if event exists)
- Invoice sent
- Payment recorded
- Invoice fully paid

**Actual**: [Pending execution]

**Screenshot**: [To be added after execution]

---

## 3. API Request/Response Logs

### 3.1 Complete Request/Response Sequence

[To be added after execution]

**Format**:
```
[2025-01-27 10:00:00] POST /api/v1/customers
Request: {...}
Response: 201 Created {...}

[2025-01-27 10:01:00] POST /api/v1/invoices
Request: {...}
Response: 201 Created {...}

[2025-01-27 10:02:00] PATCH /api/v1/invoices/{id}/mark-as-sent
Request: {...}
Response: 200 OK {...}

[2025-01-27 10:03:00] POST /api/v1/payments
Request: {...}
Response: 201 Created {...}
```

---

## 4. Screenshots

### 4.1 Step-by-Step Screenshots

1. **Customer Creation**: [To be added]
2. **Invoice Creation**: [To be added]
3. **Invoice Marked as Sent**: [To be added]
4. **Payment Recording**: [To be added]
5. **Invoice Status PAID**: [To be added]
6. **Activity Feed**: [To be added]
7. **Email Notifications**: [To be added]

---

## 5. Test Summary

| Step | Action | Expected Result | Actual Result | Status |
|------|--------|-----------------|---------------|--------|
| 1 | Create Customer | Customer created | [Pending] | ⚠️ Pending |
| 2 | Create Invoice | Invoice created (DRAFT) | [Pending] | ⚠️ Pending |
| 3 | Mark Invoice as Sent | Status = SENT, event fired | [Pending] | ⚠️ Pending |
| 4 | Record Payment | Status = PAID, events fired | [Pending] | ⚠️ Pending |
| 5 | Verify Status | Status = PAID confirmed | [Pending] | ⚠️ Pending |
| 6 | Verify Events | 3 events in activity_feed | [Pending] | ⚠️ Pending |
| 7 | Verify Emails | 3 emails sent | [Pending] | ⚠️ Pending |

**Overall Status**: ⚠️ **PENDING EXECUTION**

---

## 6. Next Steps

1. **Start Backend**: `cd backend && mvn spring-boot:run`
2. **Start Frontend**: `cd frontend && npm run dev`
3. **Execute E2E Flow**: Follow steps 1-7 manually via Frontend UI
4. **Capture Screenshots**: Take screenshots at each step
5. **Verify Database**: Run SQL queries to verify database state
6. **Check Logs**: Review API logs and email service logs
7. **Update Report**: Fill in actual results and screenshots

---

**Report Generated**: 2025-01-27  
**Next Update**: After E2E flow test execution

