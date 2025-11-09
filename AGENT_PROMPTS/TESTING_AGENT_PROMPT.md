# Testing Agent Prompt - InvoiceMe Integration Tests

## Mission
Execute and enhance the existing integration tests for the InvoiceMe ERP system. Verify that all core business flows (Customer → Invoice → Payment) work correctly at the domain and repository layers.

## Current Test Suite

We have **3 existing integration tests** in `/backend/src/test/java/com/invoiceme/integration/`:

1. **CustomerPaymentFlowTest.java** - Full E2E flow: Create customer → Create invoice with line items → Mark as sent → Record full payment → Verify PAID status
2. **PartialPaymentTest.java** - Partial payments: Record $500 payment on $1000 invoice → Verify balance → Record second $500 payment → Verify PAID
3. **OverpaymentCreditTest.java** - Overpayment handling: Record $1200 payment on $1000 invoice → Verify invoice PAID → Verify credit application logic

## Your Tasks

### Phase 1: Run Existing Tests ✅
1. **Execute the test suite:**
   ```bash
   cd /Users/mylessjs/Desktop/InvoiceMe/backend
   mvn test
   ```

2. **Verify all 3 tests pass:**
   - `CustomerPaymentFlowTest::testCustomerToInvoiceToPaymentE2EFlow`
   - `PartialPaymentTest::testPartialPayment`
   - `OverpaymentCreditTest::testOverpaymentAppliedAsCredit`

3. **Document results:**
   - Note any failures with stack traces
   - Check if test database (H2/PostgreSQL test profile) is configured correctly
   - Verify all domain logic assertions pass

### Phase 2: Enhance Test Coverage (If Time Permits)

**Additional test scenarios to add:**

1. **Invoice with Multiple Line Items:**
   - Create invoice with 3+ line items
   - Verify subtotal, tax, discount calculations
   - Test line item removal/updates

2. **Late Fee Application:**
   - Create overdue invoice (past due date)
   - Trigger late fee scheduled job logic
   - Verify late fee added to invoice

3. **Refund Flow:**
   - Create and pay invoice ($1000)
   - Issue partial refund ($300)
   - Verify invoice status changes from PAID → SENT
   - Verify balance due = $300

4. **Customer Credit Application:**
   - Create customer with credit balance
   - Create new invoice
   - Verify credit auto-applied at payment

5. **Invoice State Transitions:**
   - Test invalid transitions (e.g., PAID → DRAFT)
   - Verify business rule violations throw exceptions
   - Test cancel invoice scenarios

6. **Discount Calculations:**
   - Test PERCENTAGE discount (10% off)
   - Test FIXED_AMOUNT discount ($50 off)
   - Verify discount applied before tax

### Phase 3: Test Configuration Verification

**Check test configuration files:**

1. **application-test.yml** (if exists):
   - Verify test database configuration (H2 or PostgreSQL)
   - Check that Flyway migrations run in test profile
   - Verify Spring profiles are correct

2. **Test Database Setup:**
   - Ensure `@Transactional` rollback works (tests don't pollute DB)
   - Verify `@BeforeEach` setup runs cleanly
   - Check that repositories autowire correctly

3. **Maven Configuration:**
   - Verify `spring-boot-starter-test` dependency exists
   - Check JUnit 5 and AssertJ are available
   - Ensure test isolation works

## Success Criteria

### Minimum (Must Have):
- ✅ All 3 existing tests pass without errors
- ✅ Test execution report generated (pass/fail counts, duration)
- ✅ Any failures documented with root causes and fixes

### Ideal (Nice to Have):
- ✅ Add 2-3 additional test scenarios (refund, late fee, multiple line items)
- ✅ Achieve >80% domain layer test coverage
- ✅ Document test patterns and best practices

## Deliverables

1. **Test Execution Report** (`/qa/results/INTEGRATION_TEST_EXECUTION_REPORT.md`):
   ```markdown
   # Integration Test Execution Report
   
   ## Summary
   - Total Tests: X
   - Passed: X
   - Failed: X
   - Duration: X seconds
   
   ## Test Results
   ### CustomerPaymentFlowTest
   - Status: PASS/FAIL
   - Duration: X ms
   - Notes: ...
   
   ### PartialPaymentTest
   - Status: PASS/FAIL
   - Duration: X ms
   - Notes: ...
   
   ### OverpaymentCreditTest
   - Status: PASS/FAIL
   - Duration: X ms
   - Notes: ...
   
   ## Issues Found
   - Issue 1: Description and fix
   - Issue 2: Description and fix
   
   ## Recommendations
   - Suggestion 1
   - Suggestion 2
   ```

2. **Updated Test Files** (if enhancements made):
   - New test classes in `/backend/src/test/java/com/invoiceme/integration/`
   - Follow existing naming conventions and structure

3. **Test Coverage Report** (optional):
   - Run `mvn test jacoco:report`
   - Generate HTML coverage report in `/target/site/jacoco/`

## Technical Context

### Test Framework Stack:
- **JUnit 5** - Test framework
- **Spring Boot Test** - `@SpringBootTest` for integration tests
- **AssertJ** - Fluent assertions (`assertThat(...)`)
- **Transactional** - Auto-rollback after each test

### Domain Model Reference:
- **Aggregates:** Customer, Invoice, Payment
- **Value Objects:** Money, Email, InvoiceNumber, Address
- **Domain Events:** 10 events (PaymentRecorded, InvoiceSent, InvoicePaid, etc.)
- **Business Rules:** Invoice lifecycle (DRAFT → SENT → PAID), balance calculations, payment application

### Key Business Logic to Test:
1. **Invoice Balance Calculation:**
   - Subtotal = sum(line items)
   - Tax = subtotal × taxRate
   - Total = subtotal + tax - discount
   - Balance Due = total - amountPaid

2. **Payment Application:**
   - Payment reduces invoice balance
   - Invoice status changes to PAID when balance = 0
   - Overpayments create customer credit

3. **State Transitions:**
   - DRAFT → SENT (markAsSent)
   - SENT → PAID (recordPayment when balance = 0)
   - PAID → SENT (issueRefund creates new balance)

## Important Notes

- **Don't modify production code** unless fixing critical bugs found during testing
- **Use `@Transactional`** to ensure test isolation (auto-rollback)
- **Follow existing test patterns** for consistency
- **Document any test failures** with detailed stack traces
- **Git commit** any new tests with clear messages: "test: Add refund flow integration test"

## Expected Outcome

After running this agent, we should have:
1. ✅ Confirmation that all existing tests pass
2. ✅ Test execution report with results and timings
3. ✅ Enhanced test coverage (if time permits)
4. ✅ Confidence that core business flows work correctly

This validates our DDD implementation and ensures the domain layer business logic is correct before final deployment.

