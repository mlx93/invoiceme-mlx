# Backend Agent Prompt - M2 Phase (Core Implementation)

**[AGENT]: Backend**

**GOAL**: Implement Spring Boot REST API with DDD aggregates, CQRS command/query separation, and Vertical Slice Architecture. **M2 MILESTONE**: Core flows working (Customer → Invoice → Payment E2E). **FULL PRD SCOPE**: All features (27 core + 8 extended). Domains: Customers, Invoices, Payments, Recurring Invoices, Refunds, Users, Dashboard.

**INPUTS**:
- InvoiceMe.md (assessment source of truth)
- PRD_1_Business_Reqs.md (business rules, RBAC)
- PRD_2_Tech_Spec.md (technical architecture, API specs, database schema)
- ORCHESTRATOR_OUTPUT.md (scope decisions, decision log, all technical decisions)
- REMAINING_QUESTIONS.md (all decisions resolved)
- **M1 Deliverables** (completed):
  - `/backend/docs/domain-aggregates.md` — DDD boundaries (use as reference)
  - `/backend/docs/api/openapi.yaml` — API contracts (implement these endpoints)
  - `/backend/docs/events.md` — Domain events (implement these events)
- Database schema (completed by Data/DB Agent):
  - `/backend/src/main/resources/db/migration/V1__*.sql` through `V10__*.sql`
  - `/backend/docs/database-schema.md`
- Setup completion: `/docs/SETUP_COMPLETION_REPORT.md` (environment variables, database connection)

**M2 DELIVERABLES** (Implementation Phase):
- `/backend/src/main/java/com/invoiceme/` — Spring Boot application code:
  - **Vertical slice structure**: `customers/createcustomer/`, `invoices/createinvoice/`, `payments/recordpayment/`, etc.
  - **Domain layer**: 
    - `Customer.java` (aggregate root with rich behavior)
    - `Invoice.java` (aggregate root with rich behavior)
    - `Payment.java` (aggregate root with rich behavior)
    - `RecurringInvoiceTemplate.java` (aggregate root)
    - Value objects: `Money.java`, `Email.java`, `InvoiceNumber.java`, `Address.java`
  - **Application layer** (Vertical slices):
    - Command handlers: `CreateCustomerHandler`, `CreateInvoiceHandler`, `MarkAsSentHandler`, `RecordPaymentHandler`, etc.
    - Query handlers: `GetCustomerHandler`, `ListCustomersHandler`, `GetInvoiceHandler`, etc.
    - Controllers: REST endpoints matching OpenAPI spec
  - **Infrastructure layer**:
    - JPA repositories: `CustomerRepository`, `InvoiceRepository`, `PaymentRepository`, etc.
    - Domain event publisher: `DomainEventPublisher` (Spring ApplicationEventPublisher)
    - Email service: `EmailService` interface, `AwsSesEmailService` implementation
    - PDF service: `PdfService` interface, `iTextPdfService` implementation
  - **Domain events**: `InvoiceSentEvent`, `PaymentRecordedEvent`, `InvoiceFullyPaidEvent`, etc.
  - **Event listeners**: `@TransactionalEventListener(AFTER_COMMIT)` for email notifications, audit logging
- `/backend/src/test/java/` — Integration tests:
  - `CustomerPaymentFlowTest.java` — E2E flow (create customer → create invoice → mark sent → record payment → verify paid)
  - `PartialPaymentTest.java` — Partial payment flow
  - `OverpaymentCreditTest.java` — Overpayment → credit flow
- Spring Boot application configuration:
  - `application.yml` or `application.properties` (database connection, AWS config)
  - `@EnableScheduling` for scheduled jobs (recurring invoices, late fees)
  - Spring Security configuration (JWT authentication, RBAC)

**DONE CRITERIA**:
1. ✅ All InvoiceMe.md mandatory requirements implemented (27 core features):
   - Customer CRUD (Create, Update, Delete, Get, List) — working RESTful APIs
   - Invoice CRUD (Create Draft, Update, Mark as Sent, Get, List) — working RESTful APIs
   - Payment (Record Payment, Get, List) — working RESTful APIs
   - Invoice lifecycle: Draft → Sent → Paid transitions (working)
   - Balance calculation: Total - Amount Paid = Balance Due (correct)
   - Overpayment → Credit: Excess payment adds to customer credit balance (working)
2. ✅ DDD principles evident:
   - Rich domain models with behavior (not anemic data models)
   - Aggregate roots enforce invariants (`Customer.canBeDeleted()`, `Invoice.markAsSent()`)
   - Value objects (Money, Email, InvoiceNumber) are immutable
   - Domain logic in aggregates, not in services
3. ✅ CQRS separation enforced:
   - Commands (CreateCustomerCommand, CreateInvoiceCommand, RecordPaymentCommand) handled by command handlers
   - Queries (GetCustomerQuery, ListCustomersQuery, GetInvoiceQuery) handled by query handlers
   - Same database, separate models (write = rich entities, read = flat DTOs via MapStruct)
4. ✅ Vertical Slice Architecture:
   - Code organized by feature (`createcustomer/`, `markassent/`, `recordpayment/`)
   - Each slice contains: Command/Query, Handler, Validator, Controller, Tests
   - Minimal shared infrastructure (domain base classes, event publisher only)
5. ✅ Domain events implemented:
   - Events published after transaction commit (`@TransactionalEventListener(AFTER_COMMIT)`)
   - `InvoiceSentEvent` → Email notification listener
   - `PaymentRecordedEvent` → Payment confirmation listener
   - `InvoiceFullyPaidEvent` → Completion notification listener
   - All events from `/backend/docs/events.md` implemented
6. ✅ Scheduled jobs implemented:
   - Recurring Invoices: `@Scheduled(cron = "0 0 * * *", zone = "America/Chicago")` (daily at midnight Central Time)
   - Late Fees: `@Scheduled(cron = "0 0 1 * *", zone = "America/Chicago")` (1st of month at midnight Central Time)
   - Spring `@EnableScheduling` enabled on main application class
7. ✅ API error handling:
   - RFC 7807 Problem Details format (use Spring's `ProblemDetail` class)
   - Global exception handler (`@ControllerAdvice`)
   - Field-level validation errors included
8. ✅ Pagination:
   - Spring Data JPA `Page<T>` format
   - Include total count for UI pagination controls
9. ✅ JWT authentication:
   - No refresh tokens (user re-logs in after 24 hours)
   - Token expiry: 24 hours
   - Spring Security JWT filter chain
   - RBAC enforced (`@PreAuthorize` annotations)
10. ✅ Extended features implemented (8 features):
    - Recurring Invoices (template creation, scheduled generation)
    - Late Fees (scheduled job, line item addition)
    - Refunds (refund command, invoice reopening, credit application)
    - Dashboard & Reporting (metrics API, visualizations)
    - Email Notifications (AWS SES integration)
    - PDF Generation (iText 7)
    - User Approval (registration → approval workflow)
    - Customer Portal APIs (self-service endpoints)
11. ✅ Money rounding strategy:
    - Banker's rounding (HALF_UP) to 2 decimal places
    - Money value object with BigDecimal precision
    - All monetary calculations use Money value object
12. ✅ Integration tests passing:
    - Customer → Invoice → Payment E2E flow
    - Partial payment flow
    - Overpayment → Credit flow
    - All tests green
13. ✅ API latency <200ms (p95) for CRUD operations (measured locally, 100 requests)
    - Note: Performance validation happens in M3, but optimize during M2

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Domain aggregates implemented (Customer, Invoice, Payment, RecurringInvoiceTemplate)
  - CQRS commands/queries separated (X commands, Y queries)
  - Vertical slices created (list features)
  - Domain events implemented (list events)
  - API endpoints functional (X endpoints, list key ones)
  - Integration tests passing (X/Y scenarios)
  - Scheduled jobs implemented (recurring invoices, late fees)
  - Extended features implemented (list)
  - API latency measured (p95 = X ms) - if tested
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Code: `/backend/src/main/java/com/invoiceme/`
  - Tests: `/backend/src/test/java/`
  - Configuration: `/backend/src/main/resources/`
  - Docs: Reference M1 docs (`/backend/docs/domain-aggregates.md`, `/backend/docs/api/openapi.yaml`, `/backend/docs/events.md`)
- **Evidence**:
  - Integration test logs (screenshots or logs)
  - API endpoint examples (curl commands or Postman collection)
  - Application startup successful (logs showing Spring Boot started)

**DO NOT**:
- Change API contracts from M1 OpenAPI spec (must match exactly)
- Use anemic domain models (entities with only getters/setters - must have behavior)
- Skip Money value object with proper rounding (HALF_UP to 2 decimals, BigDecimal precision)
- Use UTC timezone for scheduled jobs (must use Central Time - America/Chicago)
- Implement refresh token endpoint (no refresh tokens - user re-logs in after 24 hours)
- Use custom error format (must use RFC 7807 Problem Details)
- Use custom pagination format (must use Spring Data JPA Page<T>)
- Skip integration tests (mandatory for M2)

**IMPORTANT NOTES**:
- **Database Connection**: Use `DATABASE_URL` from `.env` file (local PostgreSQL via Docker) or Supabase connection string
- **Money Rounding**: All monetary calculations use Banker's rounding (HALF_UP) to 2 decimal places. Money value object uses BigDecimal for precision.
- **Timezone**: Scheduled jobs run in Central Time (America/Chicago). Use `@Scheduled(cron = "0 0 * * *", zone = "America/Chicago")` for daily recurring invoices, `@Scheduled(cron = "0 0 1 * *", zone = "America/Chicago")` for monthly late fees.
- **Error Format**: Use RFC 7807 Problem Details format. Spring 6+ provides `ProblemDetail` class.
- **Pagination Format**: Use Spring Data JPA `Page<T>` format.
- **JWT Authentication**: No refresh tokens. Token expiry: 24 hours. Use Spring Security JWT filter.
- **Domain Events**: Use Spring's `ApplicationEventPublisher`. Events published after transaction commit using `@TransactionalEventListener(AFTER_COMMIT)`.
- **Vertical Slice Architecture**: Each feature in its own folder. Example structure:
  ```
  customers/
    createcustomer/
      CreateCustomerCommand.java
      CreateCustomerHandler.java
      CreateCustomerValidator.java
      CreateCustomerController.java
    getcustomer/
      GetCustomerQuery.java
      GetCustomerHandler.java
      GetCustomerController.java
  ```
- **AWS SES**: Use AWS SDK for Java. Configure with `AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` from environment variables.
- **iText 7**: Use for PDF generation. Generate PDFs on-demand, cache in S3.

**REFERENCE**:
- M1 Deliverables: `/backend/docs/domain-aggregates.md`, `/backend/docs/api/openapi.yaml`, `/backend/docs/events.md`
- Database schema: `/backend/docs/database-schema.md` and migration files
- PRD_2_Tech_Spec.md Section 3.1 for vertical slice structure
- PRD_2_Tech_Spec.md Section 4 for API endpoint specifications
- PRD_1_Business_Reqs.md Section 4 for business rules
- ORCHESTRATOR_OUTPUT.md Section 7 (Decision Log) for all technical decisions

---

**Status**: Ready to execute (M2 Phase - Implementation)  
**Dependencies**: M1 complete ✅, Data/DB Agent complete ✅  
**Parallel**: Can work in parallel with Frontend Agent (Frontend uses OpenAPI spec)  
**Next**: After M2 completion, proceed to M3 (QA testing + DevOps AWS deployment)

