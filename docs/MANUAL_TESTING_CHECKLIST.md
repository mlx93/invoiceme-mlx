# Manual Testing Checklist - Post Recurring Invoices Removal

**Date**: 2025-01-27  
**Purpose**: Verify recurring invoices module successfully removed and core features still work

---

## Pre-Testing Setup

### 1. Start Backend
```bash
cd backend
mvn spring-boot:run
```
**Verify**: Backend starts without errors, no references to recurring invoices

### 2. Start Frontend
```bash
cd frontend
npm run dev
```
**Verify**: Frontend builds and runs without errors

### 3. Database Migration
**Verify**: Migration V13 has been applied (tables dropped)
```sql
-- Check tables don't exist
SELECT table_name FROM information_schema.tables 
WHERE table_name IN ('recurring_invoice_templates', 'template_line_items');
-- Should return 0 rows
```

---

## Core Feature Testing (InvoiceMe.md Requirements)

### ✅ Test 1: Customer CRUD

**1.1 Create Customer**
- [ ] Navigate to `/customers/new`
- [ ] Fill form: Company Name, Contact Name, Email, Phone
- [ ] Submit
- [ ] **Verify**: Customer created successfully, redirects to customer list

**1.2 List Customers**
- [ ] Navigate to `/customers`
- [ ] **Verify**: Customer list displays correctly
- [ ] **Verify**: No errors in console
- [ ] **Verify**: Pagination works (if multiple customers)

**1.3 Get Customer Detail**
- [ ] Click on a customer from list
- [ ] **Verify**: Customer detail page loads
- [ ] **Verify**: All customer information displays correctly

**1.4 Update Customer**
- [ ] Click "Edit" on customer detail page
- [ ] Modify contact name or email
- [ ] Submit
- [ ] **Verify**: Changes saved, customer updated

**1.5 Delete Customer** (if zero balance)
- [ ] Click "Delete" on customer with zero balance
- [ ] Confirm deletion
- [ ] **Verify**: Customer deleted, removed from list

---

### ✅ Test 2: Invoice CRUD & Lifecycle

**2.1 Create Invoice (Draft)**
- [ ] Navigate to `/invoices/new`
- [ ] Select customer
- [ ] Add **multiple line items** (at least 2):
  - Line item 1: Description, Quantity, Unit Price
  - Line item 2: Description, Quantity, Unit Price, Discount
- [ ] **Verify**: Subtotal, tax, total calculate correctly
- [ ] Save as Draft
- [ ] **Verify**: Invoice created with status "Draft"
- [ ] **Verify**: Invoice number format: `INV-YYYY-####`

**2.2 Update Invoice**
- [ ] Navigate to draft invoice
- [ ] Click "Edit"
- [ ] Add another line item
- [ ] **Verify**: Totals recalculate
- [ ] Save
- [ ] **Verify**: Invoice updated

**2.3 Mark Invoice as Sent**
- [ ] Navigate to draft invoice
- [ ] Click "Mark as Sent"
- [ ] **Verify**: Status changes to "Sent"
- [ ] **Verify**: Email notification sent (check toast/console)
- [ ] **Verify**: Invoice cannot be edited (read-only)

**2.4 List Invoices**
- [ ] Navigate to `/invoices`
- [ ] **Verify**: Invoice list displays
- [ ] **Verify**: Filters work (status, customer)
- [ ] **Verify**: No errors

**2.5 Get Invoice Detail**
- [ ] Click on invoice from list
- [ ] **Verify**: Invoice detail page loads
- [ ] **Verify**: All line items display
- [ ] **Verify**: Payment history section visible (empty initially)

---

### ✅ Test 3: Payment Recording

**3.1 Record Payment (Full Payment)**
- [ ] Navigate to sent invoice
- [ ] Click "Record Payment" (or use customer portal)
- [ ] Enter payment amount = invoice balance
- [ ] Select payment method (Credit Card or ACH)
- [ ] Submit
- [ ] **Verify**: Invoice status changes to "Paid"
- [ ] **Verify**: Balance Due = $0.00
- [ ] **Verify**: Payment appears in payment history

**3.2 Record Payment (Partial Payment)**
- [ ] Create new sent invoice
- [ ] Record payment < invoice balance
- [ ] **Verify**: Invoice status remains "Sent" (not fully paid)
- [ ] **Verify**: Balance Due = Total - Amount Paid
- [ ] **Verify**: Amount Paid displays correctly

**3.3 Record Payment (Overpayment → Credit)**
- [ ] Create new sent invoice (total: $1,000)
- [ ] Record payment: $1,100 (overpay by $100)
- [ ] **Verify**: Invoice status = "Paid"
- [ ] **Verify**: Customer credit balance = $100.00
- [ ] **Verify**: Success message shows credit applied

**3.4 List Payments**
- [ ] Navigate to `/payments`
- [ ] **Verify**: Payment list displays
- [ ] **Verify**: All recorded payments visible
- [ ] **Verify**: Filters work

---

### ✅ Test 4: Credit Application

**4.1 Automatic Credit Application**
- [ ] Ensure customer has credit balance > 0
- [ ] Create new invoice for that customer
- [ ] Mark invoice as Sent
- [ ] **Verify**: Credit line item auto-added
- [ ] **Verify**: Invoice total reduced by credit amount
- [ ] **Verify**: Customer credit balance reduced

---

## Extended Features Testing

### ✅ Test 5: Refunds

**5.1 Issue Refund**
- [ ] Navigate to paid invoice
- [ ] Click "Issue Refund"
- [ ] Enter refund amount (partial)
- [ ] Select "Apply as Credit"
- [ ] Submit
- [ ] **Verify**: Invoice status changes: Paid → Sent
- [ ] **Verify**: Balance Due = refund amount
- [ ] **Verify**: Customer credit balance increased
- [ ] **Verify**: Refund appears in payment history (negative entry)

---

### ✅ Test 6: Dashboard

**6.1 Dashboard Metrics**
- [ ] Navigate to `/dashboard`
- [ ] **Verify**: Dashboard loads without errors
- [ ] **Verify**: Metrics display:
  - Total Revenue MTD
  - Outstanding Invoices (count + amount)
  - Overdue Invoices (count + amount)
  - Active Customers

**6.2 Dashboard Charts**
- [ ] **Verify**: Revenue trend chart displays
- [ ] **Verify**: Invoice status pie chart displays
- [ ] **Verify**: Aging report displays (if implemented)

---

### ✅ Test 7: User Management

**7.1 User Registration**
- [ ] Navigate to `/register`
- [ ] Fill registration form
- [ ] Submit
- [ ] **Verify**: User created with status "Pending"

**7.2 User Approval**
- [ ] Log in as SysAdmin
- [ ] Navigate to `/users/pending`
- [ ] **Verify**: Pending users list displays
- [ ] Click "Approve" on a pending user
- [ ] **Verify**: User status changes to "Active"
- [ ] **Verify**: User can now log in

---

## Negative Testing (Verify Recurring Invoices Removed)

### ❌ Test 8: Recurring Invoices Should Not Exist

**8.1 Navigation**
- [ ] **Verify**: No "Recurring Invoices" link in navigation menu
- [ ] **Verify**: No recurring invoices page accessible via URL (`/recurring-invoices`)

**8.2 API Endpoints**
- [ ] Try: `GET /api/v1/recurring-invoices`
- [ ] **Verify**: Returns 404 Not Found

**8.3 Database**
- [ ] **Verify**: `recurring_invoice_templates` table does not exist
- [ ] **Verify**: `template_line_items` table does not exist

**8.4 Code References**
- [ ] Search codebase for "RecurringInvoice"
- [ ] **Verify**: No references found (except in removal summary/docs)

---

## Integration Testing

### ✅ Test 9: End-to-End Flow

**9.1 Complete Customer → Invoice → Payment Flow**
- [ ] Create customer
- [ ] Create invoice with multiple line items
- [ ] Mark invoice as sent
- [ ] Record payment (full payment)
- [ ] **Verify**: Invoice status = Paid
- [ ] **Verify**: All domain events fired (check activity feed)

**9.2 Overpayment → Credit → Next Invoice Flow**
- [ ] Create customer
- [ ] Create invoice #1, mark as sent
- [ ] Overpay invoice #1 (creates credit)
- [ ] Create invoice #2, mark as sent
- [ ] **Verify**: Credit auto-applied to invoice #2
- [ ] **Verify**: Customer credit balance = $0

---

## Performance Testing

### ✅ Test 10: API Latency

**10.1 Standard CRUD Operations**
- [ ] Measure response time for:
  - `GET /api/v1/customers` (list)
  - `GET /api/v1/customers/{id}` (get)
  - `POST /api/v1/customers` (create)
  - `GET /api/v1/invoices` (list)
  - `POST /api/v1/invoices` (create)
- [ ] **Verify**: All responses < 200ms (InvoiceMe.md requirement)

---

## Build & Compilation Verification

### ✅ Test 11: Build Verification

**11.1 Backend Compilation**
- [ ] Run: `cd backend && mvn clean compile`
- [ ] **Verify**: Compiles without errors
- [ ] **Verify**: No references to recurring invoice classes

**11.2 Frontend Build**
- [ ] Run: `cd frontend && npm run build`
- [ ] **Verify**: Builds successfully
- [ ] **Verify**: No errors related to recurring invoices

**11.3 Type Checking**
- [ ] Run: `cd frontend && npm run type-check` (if available)
- [ ] **Verify**: No type errors

---

## Checklist Summary

### Core Requirements (InvoiceMe.md)
- [ ] Customer CRUD (5 operations)
- [ ] Invoice CRUD (6 operations)
- [ ] Payment CRUD (3 operations)
- [ ] Invoice lifecycle (Draft → Sent → Paid)
- [ ] Multi-line items
- [ ] Balance calculation
- [ ] API latency < 200ms

### Extended Features
- [ ] Refunds
- [ ] Customer credit system
- [ ] Dashboard metrics
- [ ] User approval workflow

### Removal Verification
- [ ] No recurring invoice navigation
- [ ] No recurring invoice API endpoints
- [ ] No recurring invoice database tables
- [ ] No code references to recurring invoices

---

## Issues Found

**Document any issues here**:
- Issue 1: [Description]
- Issue 2: [Description]

---

**Status**: ⏳ **READY FOR TESTING** - Follow checklist systematically

