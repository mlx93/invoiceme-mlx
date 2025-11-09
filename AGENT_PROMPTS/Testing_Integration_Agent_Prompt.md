# Testing and Integration Agent Prompt

**[AGENT]: Testing & Integration**

**GOAL**: Execute comprehensive testing of the InvoiceMe ERP system, validate Frontend-Backend integration, measure performance targets, and generate test evidence for M3 milestone completion.

---

## Context

**M2 Status**: ✅ **COMPLETE**
- Backend: 25+ REST endpoints, DDD/CQRS/VSA architecture, event listeners, scheduled jobs, JWT auth, RBAC enforcement
- Frontend: 12 pages, MVVM pattern, RBAC tested, mobile responsive
- Both systems ready for integration testing

**M3 Milestone**: Non-Functional Targets Validation
- API latency <200ms (p95) for CRUD operations
- UI page load <2s (First Contentful Paint)
- E2E flow working (Customer → Invoice → Payment)
- RBAC enforcement verified
- Domain events firing correctly

---

## Inputs

**Required Documents**:
- `InvoiceMe.md` - Assessment requirements (integration tests mandatory)
- `PRD_1_Business_Reqs.md` - Business rules and test scenarios (Section 7)
- `PRD_2_Tech_Spec.md` - Technical requirements and performance targets
- `ORCHESTRATOR_OUTPUT.md` - Project scope, architecture decisions, M3 acceptance criteria
- `/backend/TESTING_GUIDE.md` - Backend testing guide with curl commands
- `/backend/docs/api/openapi.yaml` - API contracts
- `/backend/M2_COMPLETE.md` - Backend implementation status
- `/frontend/FRONTEND_AGENT_REPORT.md` - Frontend implementation status
- `/frontend/TESTING_REPORT.md` - Frontend RBAC and mobile testing results

**Codebase**:
- `/backend/` - Spring Boot application (ready to run)
- `/frontend/` - Next.js application (ready to run)
- `/backend/src/test/java/com/invoiceme/integration/` - Existing integration tests

---

## Deliverables

### 1. Test Execution Report (`/qa/results/test-execution-report.md`)
**Format**: Markdown document with:
- Test execution summary (date, environment, test duration)
- Backend API test results (all endpoints from TESTING_GUIDE.md)
- Frontend-Backend integration test results
- E2E flow test results (Customer → Invoice → Payment)
- RBAC enforcement verification results
- Domain events verification results
- Scheduled jobs verification results
- Issues found (critical, major, minor) with reproduction steps
- Test evidence (screenshots, logs, curl command outputs)

### 2. Performance Test Report (`/qa/results/performance-report.md`)
**Format**: Markdown document with:
- API latency measurements (local environment):
  - Endpoint, p50, p95, p99, sample size (minimum 100 requests per endpoint)
  - Key endpoints: POST /customers, GET /customers/{id}, GET /customers, POST /invoices, GET /invoices/{id}, GET /invoices, POST /payments
  - Target: p95 <200ms for CRUD operations
- UI performance measurements:
  - Page load times (First Contentful Paint, Time to Interactive)
  - Key pages: Dashboard, Customer List, Invoice List, Invoice Detail
  - Target: <2s page load
- Performance optimization recommendations (if targets missed)

### 3. Integration Test Results (`/qa/results/integration-test-results.md`)
**Format**: Markdown document with:
- Pass/fail table for all integration tests:
  - CustomerPaymentFlowTest (E2E flow)
  - PartialPaymentTest
  - OverpaymentCreditTest
  - LateFeeCalculationTest (if scheduled job testable)
  - RecurringInvoiceTest (if scheduled job testable)
  - RefundFlowTest
- Test execution logs
- Screenshots of test results
- Any test failures with root cause analysis

### 4. E2E Flow Test Evidence (`/qa/results/e2e-flow-evidence.md`)
**Format**: Markdown document with:
- Step-by-step E2E flow test (Customer → Invoice → Payment):
  1. Create customer via Frontend
  2. Create invoice via Frontend
  3. Mark invoice as sent via Frontend
  4. Record payment via Frontend
  5. Verify invoice status changed to PAID
  6. Verify domain events fired (check activity_feed table)
  7. Verify email notifications sent (check logs)
- Screenshots of each step
- API request/response logs
- Database state verification (screenshots of relevant tables)

### 5. RBAC Verification Report (`/qa/results/rbac-verification.md`)
**Format**: Markdown document with:
- RBAC test matrix (role × endpoint × expected result):
  - SysAdmin: Full access to all endpoints
  - Accountant: Financial operations, customer management
  - Sales: Customer and invoice creation
  - Customer: Own invoices and payments only
- Test results for each role/endpoint combination
- Unauthorized access attempts (should return 403)
- Screenshots of RBAC enforcement in action

### 6. Domain Events Verification (`/qa/results/domain-events-verification.md`)
**Format**: Markdown document with:
- Domain events test results:
  - InvoiceSentEvent (verify published when invoice marked as sent)
  - PaymentRecordedEvent (verify published when payment recorded)
  - InvoiceFullyPaidEvent (verify published when invoice fully paid)
  - CreditAppliedEvent (verify published when overpayment occurs)
  - RefundIssuedEvent (verify published when refund issued)
- Event listener verification:
  - Email listeners triggered (check email service logs)
  - Activity feed listeners triggered (check activity_feed table)
  - Cache invalidation listeners triggered (check cache logs)
- Screenshots of activity_feed table entries
- Email service logs (if available)

### 7. Test Execution Scripts (`/qa/scripts/`)
**Optional but helpful**:
- `test-backend-apis.sh` - Automated script to test all backend endpoints
- `test-performance.sh` - Automated script for performance testing
- `test-e2e-flow.sh` - Automated script for E2E flow testing

---

## Testing Procedures

### Phase 1: Backend API Testing
1. **Start Backend Application**:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   Verify application starts successfully on `http://localhost:8080`

2. **Execute Backend Tests**:
   - Follow `/backend/TESTING_GUIDE.md` for curl commands
   - Test all 25+ endpoints:
     - Customer CRUD (5 endpoints)
     - Invoice CRUD (6 endpoints)
     - Payment CRUD (3 endpoints)
     - Refunds (1 endpoint)
     - Dashboard (4 endpoints)
     - User Approval (3 endpoints)
     - Authentication (2 endpoints)
   - Document results in test-execution-report.md

3. **Verify RBAC Enforcement**:
   - Test each endpoint with different user roles
   - Verify unauthorized access returns 403
   - Document results in rbac-verification.md

4. **Verify Domain Events**:
   - Execute actions that trigger domain events
   - Check activity_feed table for event logs
   - Check email service logs for email notifications
   - Document results in domain-events-verification.md

### Phase 2: Frontend-Backend Integration Testing
1. **Start Both Applications**:
   ```bash
   # Terminal 1: Backend
   cd backend
   mvn spring-boot:run
   
   # Terminal 2: Frontend
   cd frontend
   npm install
   npm run dev
   ```
   Verify:
   - Backend running on `http://localhost:8080`
   - Frontend running on `http://localhost:3000`

2. **Configure Frontend API URL**:
   - Ensure `frontend/.env.local` has: `NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1`

3. **Test Frontend Pages with Backend APIs**:
   - Login page (POST /auth/login)
   - Customer pages (CRUD operations)
   - Invoice pages (CRUD operations)
   - Payment pages (record payment)
   - Dashboard (metrics endpoints)
   - Document any integration issues

### Phase 3: E2E Flow Testing
1. **Execute Customer → Invoice → Payment Flow**:
   - Create customer via Frontend UI
   - Create invoice via Frontend UI (add line items)
   - Mark invoice as sent via Frontend UI
   - Record payment via Frontend UI
   - Verify invoice status changed to PAID
   - Verify customer credit balance updated (if overpayment)
   - Document step-by-step with screenshots

2. **Verify Business Rules**:
   - Overpayment → Credit: Record payment > invoice balance, verify credit applied
   - Partial Payment: Record payment < invoice balance, verify invoice remains SENT
   - Invoice Cancellation: Cancel invoice, verify status changed to CANCELLED

### Phase 4: Performance Testing
1. **API Latency Measurement**:
   - Use tool like `ab` (Apache Bench) or `wrk` or custom script
   - Test key endpoints with 100+ requests:
     - POST /api/v1/customers
     - GET /api/v1/customers/{id}
     - GET /api/v1/customers (with pagination)
     - POST /api/v1/invoices
     - GET /api/v1/invoices/{id}
     - GET /api/v1/invoices
     - POST /api/v1/payments
   - Measure p50, p95, p99 latencies
   - Target: p95 <200ms

2. **UI Performance Measurement**:
   - Use Chrome DevTools Lighthouse or similar
   - Test key pages:
     - Dashboard
     - Customer List
     - Invoice List
     - Invoice Detail
   - Measure First Contentful Paint, Time to Interactive
   - Target: <2s page load

### Phase 5: Scheduled Jobs Testing
1. **Test Recurring Invoice Generation**:
   - Create recurring invoice template
   - Manually trigger scheduled job or wait for scheduled time
   - Verify invoice generated from template
   - Document results

2. **Test Late Fee Application**:
   - Create overdue invoice
   - Manually trigger scheduled job or wait for scheduled time
   - Verify late fee applied
   - Document results

---

## Success Criteria

**M3 Milestone Complete When**:
- ✅ All backend endpoints tested and working
- ✅ Frontend-Backend integration verified
- ✅ E2E flow (Customer → Invoice → Payment) working end-to-end
- ✅ RBAC enforcement verified for all roles
- ✅ Domain events firing correctly (verified via activity_feed table)
- ✅ API latency <200ms (p95) for CRUD operations (local environment)
- ✅ UI page load <2s (First Contentful Paint)
- ✅ Integration tests passing (3+ tests)
- ✅ Test evidence documented (screenshots, logs, reports)

---

## Report Format

**REPORT BACK WITH**:
- **Summary** (≤15 bullets):
  - Backend API test results (X/Y endpoints passing)
  - Frontend-Backend integration status (working/blocked)
  - E2E flow test status (pass/fail)
  - RBAC verification status (all roles verified/partial)
  - Domain events verification status (all events firing/partial)
  - Performance test results (API latency p95, UI page load times)
  - Integration test results (X/Y tests passing)
  - Critical issues found (count)
  - Major issues found (count)
  - Minor issues found (count)
  - Scheduled jobs status (tested/not tested)
  - Test evidence location (paths to reports)
- **Artifacts paths**:
  - Test execution report: `/qa/results/test-execution-report.md`
  - Performance report: `/qa/results/performance-report.md`
  - Integration test results: `/qa/results/integration-test-results.md`
  - E2E flow evidence: `/qa/results/e2e-flow-evidence.md`
  - RBAC verification: `/qa/results/rbac-verification.md`
  - Domain events verification: `/qa/results/domain-events-verification.md`
- **Evidence**:
  - Screenshots of test executions
  - API request/response logs
  - Performance test results (latency tables)
  - UI performance results (Lighthouse scores)
  - Database state verification (screenshots)

---

## DO NOT

- Skip performance testing (mandatory for M3)
- Skip RBAC verification (critical security requirement)
- Skip domain events verification (critical for event-driven architecture)
- Skip E2E flow testing (core business requirement)
- Report "all tests passing" without evidence (screenshots, logs required)

---

## Notes

- **Database**: Ensure PostgreSQL is running (local or Supabase)
- **Environment Variables**: Backend needs DATABASE_URL, JWT_SECRET, AWS credentials (optional for local)
- **Frontend Environment**: Needs NEXT_PUBLIC_API_URL configured
- **Test Data**: Create test data as needed (customers, invoices, payments)
- **Scheduled Jobs**: May need manual triggering for testing (can modify cron expressions temporarily)

---

**Status**: Ready to begin M3 testing and integration validation

