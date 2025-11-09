# QA Testing Execution Guide

**Status**: ✅ Testing Infrastructure Complete - Ready for Execution  
**Date**: 2025-01-27

---

## Quick Start: Execute All Tests

### Prerequisites
- Backend running on `http://localhost:8080`
- Frontend running on `http://localhost:3000`
- Database (PostgreSQL) accessible
- Test user accounts created (SysAdmin, Accountant, Sales, Customer roles)

---

## Step 1: Start Applications

### Terminal 1: Backend
```bash
cd backend
mvn spring-boot:run
```
**Verify**: Backend accessible at `http://localhost:8080/api/v1`

### Terminal 2: Frontend
```bash
cd frontend
npm run dev
```
**Verify**: Frontend accessible at `http://localhost:3000`

---

## Step 2: Execute Automated Tests

### Backend API Tests
```bash
cd qa/scripts
chmod +x test-backend-apis.sh
./test-backend-apis.sh
```
**Output**: Test results logged to console and `/qa/results/test-execution-report.md`

### Performance Tests
```bash
cd qa/scripts
chmod +x test-performance.sh
./test-performance.sh
```
**Output**: Performance metrics logged to `/qa/results/performance-report.md`

### Integration Tests
```bash
cd backend
mvn test
```
**Output**: Test results in console, update `/qa/results/integration-test-results.md`

---

## Step 3: Manual E2E Flow Test

Follow the step-by-step guide in `/qa/results/e2e-flow-evidence.md`:

1. **Create Customer** (via Frontend UI)
   - Navigate to Customers → New Customer
   - Fill form and submit
   - Verify customer created

2. **Create Invoice** (via Frontend UI)
   - Navigate to Invoices → New Invoice
   - Select customer, add line items
   - Submit and verify invoice created (status: DRAFT)

3. **Mark Invoice as Sent** (via Frontend UI)
   - Open invoice detail page
   - Click "Mark as Sent"
   - Verify status changed to SENT
   - Verify InvoiceSentEvent fired (check activity_feed table)

4. **Record Payment** (via Frontend UI)
   - Open invoice detail page
   - Click "Record Payment"
   - Enter payment amount (full or partial)
   - Submit and verify payment recorded
   - Verify PaymentRecordedEvent fired (check activity_feed table)

5. **Verify Invoice Status** (if full payment)
   - Check invoice status changed to PAID
   - Verify InvoiceFullyPaidEvent fired (check activity_feed table)

6. **Verify Overpayment → Credit** (if payment > invoice balance)
   - Record payment greater than invoice balance
   - Verify customer credit balance increased
   - Verify CreditAppliedEvent fired (check activity_feed table)

7. **Take Screenshots**
   - Screenshot each step
   - Save to `/qa/results/screenshots/` directory
   - Update `/qa/results/e2e-flow-evidence.md` with screenshot paths

---

## Step 4: RBAC Verification

Follow the test matrix in `/qa/results/rbac-verification.md`:

### Test Each Role × Endpoint Combination

1. **SysAdmin Role**:
   - Login as SysAdmin
   - Test all endpoints (should have full access)
   - Document results

2. **Accountant Role**:
   - Login as Accountant
   - Test endpoints (should have financial operations access)
   - Verify cannot delete customers (should return 403)
   - Document results

3. **Sales Role**:
   - Login as Sales
   - Test endpoints (should have customer/invoice creation access)
   - Verify cannot access user management (should return 403)
   - Document results

4. **Customer Role**:
   - Login as Customer
   - Test endpoints (should only see own invoices/payments)
   - Verify cannot create customers (should return 403)
   - Document results

**Update**: `/qa/results/rbac-verification.md` with actual test results

---

## Step 5: Domain Events Verification

Follow procedures in `/qa/results/domain-events-verification.md`:

### Verify Each Domain Event

1. **InvoiceSentEvent**:
   - Mark invoice as sent
   - Check `activity_feed` table: `SELECT * FROM activity_feed WHERE event_type = 'InvoiceSentEvent' ORDER BY created_at DESC LIMIT 1;`
   - Check email service logs (if available)
   - Document results

2. **PaymentRecordedEvent**:
   - Record payment
   - Check `activity_feed` table: `SELECT * FROM activity_feed WHERE event_type = 'PaymentRecordedEvent' ORDER BY created_at DESC LIMIT 1;`
   - Check email service logs
   - Document results

3. **InvoiceFullyPaidEvent**:
   - Record full payment
   - Check `activity_feed` table: `SELECT * FROM activity_feed WHERE event_type = 'InvoiceFullyPaidEvent' ORDER BY created_at DESC LIMIT 1;`
   - Check email service logs
   - Document results

4. **CreditAppliedEvent**:
   - Record overpayment
   - Check `activity_feed` table: `SELECT * FROM activity_feed WHERE event_type = 'CreditAppliedEvent' ORDER BY created_at DESC LIMIT 1;`
   - Verify customer credit balance updated
   - Document results

**Repeat for all 10 domain events** (see `/qa/results/domain-events-verification.md`)

**Update**: `/qa/results/domain-events-verification.md` with actual verification results

---

## Step 6: Scheduled Jobs Testing

### Test Recurring Invoice Generation

1. **Create Recurring Invoice Template**:
   - Via API or Frontend UI
   - Set frequency (MONTHLY, QUARTERLY, ANNUALLY)
   - Set next generation date to today or past date

2. **Trigger Scheduled Job** (or wait for scheduled time):
   - Option A: Wait for scheduled time (daily at midnight Central Time)
   - Option B: Manually trigger (modify cron expression temporarily to run immediately)
   - Option C: Call scheduled job endpoint (if exposed)

3. **Verify Invoice Generated**:
   - Check invoices table: `SELECT * FROM invoices WHERE invoice_number LIKE 'INV-%' ORDER BY created_at DESC LIMIT 1;`
   - Verify invoice created from template
   - Verify RecurringInvoiceGeneratedEvent fired

### Test Late Fee Application

1. **Create Overdue Invoice**:
   - Create invoice with due date in the past
   - Mark invoice as SENT
   - Wait for late fee scheduled job (1st of month at midnight Central Time)

2. **Trigger Scheduled Job** (or wait for scheduled time):
   - Option A: Wait for scheduled time
   - Option B: Manually trigger (modify cron expression temporarily)
   - Option C: Call scheduled job endpoint (if exposed)

3. **Verify Late Fee Applied**:
   - Check invoice: Late fee line item added
   - Verify LateFeeAppliedEvent fired
   - Check activity_feed table

**Update**: `/qa/results/test-execution-report.md` with scheduled job test results

---

## Step 7: Update Test Reports

After executing all tests, update the following reports with actual results:

1. **`/qa/results/test-execution-report.md`**:
   - Fill in actual test results (pass/fail)
   - Add screenshots/logs
   - Document any issues found

2. **`/qa/results/performance-report.md`**:
   - Fill in actual latency measurements (p50, p95, p99)
   - Fill in UI performance metrics (FCP, TTI)
   - Document if targets met (<200ms API, <2s UI)

3. **`/qa/results/integration-test-results.md`**:
   - Update with actual test execution results
   - Document any test failures

4. **`/qa/results/e2e-flow-evidence.md`**:
   - Add screenshots to each step
   - Document actual API request/response logs
   - Document database state verification

5. **`/qa/results/rbac-verification.md`**:
   - Fill in test matrix with actual results (pass/fail)
   - Document unauthorized access attempts

6. **`/qa/results/domain-events-verification.md`**:
   - Document actual event firing verification
   - Add activity_feed table screenshots
   - Document email service log verification

---

## Step 8: Generate M3 Testing Summary

Create `/qa/results/M3_TESTING_SUMMARY.md` with:

- **Test Execution Summary**:
  - Total tests executed: X
  - Tests passing: Y
  - Tests failing: Z
  - Pass rate: Y/X %

- **Performance Results**:
  - API latency (p95): X ms (target: <200ms) ✅/❌
  - UI page load (FCP): X s (target: <2s) ✅/❌

- **Issues Found**:
  - Critical: X
  - Major: Y
  - Minor: Z

- **M3 Milestone Status**: ✅ Complete / ❌ Incomplete

---

## Troubleshooting

### Backend Not Starting
- Check database connection (DATABASE_URL)
- Check port 8080 not in use
- Check logs: `backend/logs/application.log`

### Frontend Not Starting
- Check Node.js version (18+)
- Check `npm install` completed
- Check port 3000 not in use

### Tests Failing
- Verify backend/frontend are running
- Check API endpoints accessible
- Check database connection
- Check environment variables configured

### Performance Tests Failing
- Ensure backend has no other load
- Run tests multiple times for accuracy
- Check network latency

---

## Expected Timeline

- **Backend API Tests**: 15-30 minutes
- **Performance Tests**: 30-45 minutes
- **E2E Flow Test**: 30-45 minutes
- **RBAC Verification**: 30-45 minutes
- **Domain Events Verification**: 30-45 minutes
- **Scheduled Jobs Testing**: 15-30 minutes (if manually triggered)
- **Report Updates**: 30-60 minutes

**Total Estimated Time**: 3-5 hours for complete test execution and documentation

---

## Success Criteria

M3 Testing Complete When:
- ✅ All backend API endpoints tested and documented
- ✅ Performance targets met (API <200ms p95, UI <2s FCP)
- ✅ E2E flow working end-to-end
- ✅ RBAC verified for all roles
- ✅ Domain events firing correctly
- ✅ Integration tests passing
- ✅ All test reports updated with actual results
- ✅ M3 Testing Summary generated

---

**Status**: Ready to execute tests - Follow steps above to complete M3 testing

