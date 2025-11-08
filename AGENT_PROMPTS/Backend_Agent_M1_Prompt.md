# Backend Agent Prompt - M1 Phase (Domain & API Freeze)

**[AGENT]: Backend**

**GOAL**: Freeze domain model and API contracts for InvoiceMe system. **M1 MILESTONE**: Define DDD aggregates, CQRS command/query separation, and create OpenAPI specification. **FULL PRD SCOPE**: All features (27 core + 8 extended). Domains: Customers, Invoices, Payments, Recurring Invoices, Refunds, Users, Dashboard.

**INPUTS**:
- InvoiceMe.md (assessment source of truth)
- PRD_1_Business_Reqs.md (business rules, RBAC, entity attributes)
- PRD_2_Tech_Spec.md (technical architecture, API specs, domain model Section 3.2)
- ORCHESTRATOR_OUTPUT.md (scope decisions, decision log, all technical decisions)
- REMAINING_QUESTIONS.md (all decisions resolved - Central Time, RFC 7807, Spring Data JPA, etc.)
- Database schema (completed by Data/DB Agent):
  - `/backend/src/main/resources/db/migration/V1__*.sql` through `V10__*.sql`
  - `/backend/docs/database-schema.md`
  - `/backend/docs/erd.png` or `erd.md`
- Setup completion: `/docs/SETUP_COMPLETION_REPORT.md` (environment variables, database connection)

**M1 DELIVERABLES** (Domain & API Freeze Only):
- `/backend/docs/domain-aggregates.md` — DDD boundaries documentation:
  - Customer Aggregate: Properties, behavior methods, invariants, domain events
  - Invoice Aggregate: Properties, behavior methods (addLineItem, markAsSent, recordPayment, etc.), invariants, domain events
  - Payment Aggregate: Properties, behavior methods (static factory record()), invariants, domain events
  - RecurringInvoiceTemplate Aggregate: Properties, behavior methods, invariants, domain events
  - Value Objects: Money (BigDecimal, HALF_UP rounding), Email, InvoiceNumber, Address
  - Aggregate boundaries and relationships
- `/backend/docs/api/openapi.yaml` — OpenAPI 3.0 specification:
  - All endpoints documented (Customers, Invoices, Payments, Recurring Invoices, Refunds, Users, Dashboard)
  - Commands vs. Queries clearly labeled
  - Request/Response DTOs defined
  - Authentication (JWT Bearer token) specified
  - Error responses (RFC 7807 Problem Details format)
  - Pagination (Spring Data JPA Page<T> format)
- `/backend/docs/events.md` — Domain events list:
  - PaymentRecordedEvent → Published by Payment.record(), consumed by email listener, audit log listener
  - InvoiceSentEvent → Published by Invoice.markAsSent(), consumed by email listener, dashboard cache listener
  - InvoiceFullyPaidEvent → Published by Invoice.recordPayment() when balance = 0, consumed by notification listener
  - LateFeeAppliedEvent → Published by scheduled job, consumed by email listener
  - RecurringInvoiceGeneratedEvent → Published by scheduled job, consumed by email listener
  - RefundIssuedEvent → Published by Refund.issue(), consumed by email listener
  - CreditAppliedEvent → Published by Customer.applyCredit(), consumed by audit log listener
  - CreditDeductedEvent → Published by Customer.deductCredit(), consumed by audit log listener
  - CustomerDeactivatedEvent → Published by Customer.markAsInactive(), consumed by audit log listener
  - InvoiceCancelledEvent → Published by Invoice.cancel(), consumed by email listener, audit log listener
  - Event producers and consumers mapped
  - Transactional consistency pattern documented (@TransactionalEventListener(AFTER_COMMIT))

**DONE CRITERIA**:
1. ✅ DDD aggregates explicitly documented:
   - Customer Aggregate: Rich domain model with behavior (not anemic)
     - Properties: UUID, company name, contact name, email (value object), phone, address (value object), customer type, credit balance (Money value object), status
     - Behavior: `applyCredit(Money)`, `deductCredit(Money)`, `canBeDeleted()`, `markAsInactive()`
     - Invariants: Cannot delete if balance > 0, email must be unique
     - Domain Events: CreditAppliedEvent, CreditDeductedEvent, CustomerDeactivatedEvent
   - Invoice Aggregate: Rich domain model with behavior
     - Properties: UUID, invoice number (value object), customer (reference), issue/due dates, status, payment terms, line items (list of entities), totals (Money), notes, timestamps, version (for optimistic locking)
     - Line Item Entity: description, quantity, unit price (Money), discount type/value, tax rate, calculated line total
     - Behavior: `addLineItem()`, `removeLineItem()`, `markAsSent()`, `recordPayment(Money)`, `applyCreditDiscount(Money)`, `addLateFee(Money)`, `cancel()`, `isOverdue()`
     - Invariants: Cannot mark as sent if no line items, cannot record payment if status is Draft, balance = total - amount paid
     - Domain Events: InvoiceSentEvent, InvoiceFullyPaidEvent, LateFeeAppliedEvent, InvoiceCancelledEvent
   - Payment Aggregate: Rich domain model with behavior
     - Properties: UUID, invoice (reference), customer (reference), amount (Money), method, date, reference, status, created by, notes
     - Behavior: Static factory `record()` - validates amount, updates invoice, handles overpayment to credit
     - Invariants: Amount must be > 0, invoice must be in Sent/Overdue status
     - Domain Events: PaymentRecordedEvent
   - RecurringInvoiceTemplate Aggregate: Rich domain model with behavior
     - Properties: UUID, customer, template name, frequency, start/end dates, next invoice date, status, line items, payment terms, auto-send flag
     - Behavior: `generateInvoice()`, `pause()`, `resume()`, `complete()`, `calculateNextDate()`
     - Domain Events: RecurringInvoiceGeneratedEvent
   - Value Objects: Immutable, no identity
     - Money: Amount + currency, supports add/subtract/compare operations, immutable, BigDecimal precision, HALF_UP rounding to 2 decimals
     - Email: Validates format, immutable
     - InvoiceNumber: Format INV-YYYY-####, auto-generated, immutable
     - Address: Street, city, state, zip, country, immutable
2. ✅ CQRS separation documented:
   - Commands (Write Operations) clearly identified:
     - Customer: CreateCustomerCommand, UpdateCustomerCommand, DeleteCustomerCommand
     - Invoice: CreateInvoiceCommand, UpdateInvoiceCommand, MarkAsSentCommand, CancelInvoiceCommand
     - Payment: RecordPaymentCommand
     - Recurring Invoice: CreateTemplateCommand, PauseTemplateCommand, ResumeTemplateCommand, CompleteTemplateCommand
     - Refund: IssueRefundCommand
     - User: RegisterUserCommand, ApproveUserCommand, RejectUserCommand
   - Queries (Read Operations) clearly identified:
     - Customer: GetCustomerQuery, ListCustomersQuery
     - Invoice: GetInvoiceQuery, ListInvoicesQuery, GeneratePdfQuery
     - Payment: GetPaymentQuery, ListPaymentsQuery
     - Recurring Invoice: GetTemplateQuery, ListTemplatesQuery
     - Dashboard: GetMetricsQuery, GetRevenueTrendQuery, GetInvoiceStatusQuery, GetAgingReportQuery
   - Same database, separate models (write = rich entities, read = flat DTOs)
3. ✅ OpenAPI specification complete:
   - Base URL: `/api/v1`
   - Authentication: JWT in `Authorization: Bearer <token>` header
   - All endpoints documented with:
     - HTTP method, path, description
     - Auth roles required
     - Request body schema (for commands)
     - Response schema (200, 201, 400, 401, 403, 404, 500)
     - Error responses use RFC 7807 Problem Details format
     - Pagination responses use Spring Data JPA Page<T> format
   - Commands vs. Queries labeled in endpoint descriptions
   - DTOs defined: CreateCustomerRequest, CustomerResponse, InvoiceDetailResponse, RecordPaymentRequest, etc.
4. ✅ Domain events mapped:
   - All events listed with producers (which aggregate method publishes) and consumers (which listeners handle)
   - Transactional consistency pattern: Events published after transaction commit (@TransactionalEventListener(AFTER_COMMIT))
   - Event payloads documented (what data each event contains)

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Domain aggregates documented (Customer, Invoice, Payment, RecurringInvoiceTemplate)
  - Value objects defined (Money, Email, InvoiceNumber, Address)
  - CQRS commands/queries separated (X commands, Y queries)
  - OpenAPI spec created (X endpoints documented)
  - Domain events mapped (X events with producers/consumers)
  - API contracts frozen (ready for Frontend Agent)
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Domain aggregates: `/backend/docs/domain-aggregates.md`
  - OpenAPI spec: `/backend/docs/api/openapi.yaml`
  - Domain events: `/backend/docs/events.md`
- **Evidence**:
  - Domain aggregates documentation (showing rich behavior, not anemic models)
  - OpenAPI spec snippet (showing commands vs. queries)
  - Domain events mapping (showing producers/consumers)

**DO NOT**:
- Implement actual code yet (this is M1 - design phase only)
- Change API contracts after freeze (version API if needed: `/api/v1`)
- Use anemic domain models (entities with only getters/setters - must have behavior)
- Skip Money value object with proper rounding (HALF_UP to 2 decimals, BigDecimal precision)
- Use UTC timezone for scheduled jobs (must use Central Time - America/Chicago)
- Implement refresh token endpoint (no refresh tokens - user re-logs in after 24 hours)
- Use custom error format (must use RFC 7807 Problem Details)
- Use custom pagination format (must use Spring Data JPA Page<T>)

**IMPORTANT NOTES**:
- **Money Rounding**: All monetary calculations use Banker's rounding (HALF_UP) to 2 decimal places. Money value object uses BigDecimal for precision.
- **Timezone**: Scheduled jobs run in Central Time (America/Chicago). Use `@Scheduled(cron = "0 0 * * *", zone = "America/Chicago")` for daily recurring invoices, `@Scheduled(cron = "0 0 1 * *", zone = "America/Chicago")` for monthly late fees.
- **Error Format**: Use RFC 7807 Problem Details format. Spring 6+ provides `ProblemDetail` class. Example:
  ```json
  {
    "type": "https://invoiceme.com/errors/validation-error",
    "title": "Validation Error",
    "status": 400,
    "detail": "Email is required",
    "instance": "/api/v1/customers",
    "errors": [
      {
        "field": "email",
        "message": "Email is required"
      }
    ]
  }
  ```
- **Pagination Format**: Use Spring Data JPA `Page<T>` format:
  ```json
  {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false
  }
  ```
- **JWT Authentication**: No refresh tokens. Token expiry: 24 hours. Frontend handles token expiry by redirecting to login on 401.
- **Database Schema**: Use the schema created by Data/DB Agent. Map JPA entities to existing tables. Use Flyway migrations (already created V1-V10).

**REFERENCE**:
- PRD_2_Tech_Spec.md Section 3.2 for domain model structure
- PRD_2_Tech_Spec.md Section 4 for API endpoint specifications
- PRD_1_Business_Reqs.md Section 4 for business rules and entity attributes
- ORCHESTRATOR_OUTPUT.md Section 7 (Decision Log) for all technical decisions
- Database schema: `/backend/docs/database-schema.md` and migration files

---

**Status**: Ready to execute (M1 Phase - Design Only)  
**Dependencies**: Data/DB Agent complete ✅  
**Next**: After M1 completion, proceed to M2 (Backend implementation) + Frontend Agent can start in parallel

