# InvoiceMe Project — Orchestrator Output Package

**Date**: 2025-01-27  
**Status**: Planning Phase — Awaiting Clarifications  
**Orchestrator Role**: Requirements Verification, Gap Analysis, Execution Planning

---

## 1. Executive Snapshot

- **What We're Building**: Production-quality ERP-style invoicing system demonstrating DDD, CQRS, and VSA architecture patterns. Core domains: Customers, Invoices, Payments with full CRUD operations, invoice lifecycle (Draft → Sent → Paid), payment processing, and authentication.

- **Success Definition**: 
  - All InvoiceMe.md mandatory requirements implemented (27 core features)
  - API latency <200ms for CRUD operations
  - Integration tests passing for Customer → Invoice → Payment E2E flow
  - Clean architecture with DDD aggregates, CQRS separation, VSA structure
  - Complete deliverables: code repo, demo video, technical writeup, AI documentation

- **Biggest Risks**:
  - **Scope Creep**: PRDs include extended features (Recurring Invoices, Late Fees, Refunds, Customer Portal) not explicitly in InvoiceMe.md — need confirmation on MVP vs. Extended scope
  - **Architecture Complexity**: Balancing DDD/CQRS/VSA while maintaining <200ms performance may require careful query optimization
  - **Time Constraint**: 5-7 day timeline is tight for full-stack implementation with integration tests

- **Immediate Next Steps**:
  1. Resolve clarifying questions (Section 4) — **BLOCKING**
  2. Confirm MVP scope (core InvoiceMe.md vs. extended PRD features)
  3. Freeze domain model and API contracts (M1 milestone)
  4. Begin backend implementation with DDD aggregates

---

## 2. Traceability Matrix

| InvoiceMe.md Requirement | PRD Section(s) | Status | Notes |
|---------------------------|----------------|--------|-------|
| **Architecture Requirements** |
| Domain-Driven Design (DDD) | PRD 2, Section 1.2, 3.2 | ✅ Covered | Rich domain models with behavior documented (Customer, Invoice, Payment aggregates) |
| CQRS Pattern | PRD 2, Section 1.2, 3.3 | ✅ Covered | Commands/Queries separation with handlers documented |
| Vertical Slice Architecture (VSA) | PRD 2, Section 3.1 | ✅ Covered | Feature-based folder structure (`createinvoice/`, `recordpayment/`) |
| Clean Architecture Layers | PRD 2, Section 1.2 | ✅ Covered | Domain → Application → Infrastructure separation |
| **Customer Domain** |
| Create Customer (Command) | PRD 1, Section 4.1 | ✅ Covered | Full spec with validation, UUID generation |
| Update Customer (Command) | PRD 1, Section 4.1 | ✅ Covered | Audit logging included |
| Delete Customer (Command) | PRD 1, Section 4.1 | ✅ Covered | Soft delete with zero balance validation |
| Retrieve Customer by ID (Query) | PRD 1, Section 4.1 | ✅ Covered | CustomerDetailResponse DTO |
| List Customers (Query) | PRD 1, Section 4.1 | ✅ Covered | Filters, pagination (20/page) |
| **Invoice Domain** |
| Create Invoice (Draft) (Command) | PRD 1, Section 4.2 | ✅ Covered | Auto-generates invoice number, requires ≥1 line item |
| Update Invoice (Command) | PRD 1, Section 4.2 | ✅ Covered | Draft fully editable, Sent line items only, Paid locked |
| Mark as Sent (Command) | PRD 1, Section 4.2 | ✅ Covered | Status transition, email notification, credit auto-apply |
| Record Payment (Command) | PRD 1, Section 4.3 | ✅ Covered | Updates balance, handles overpayment → credit |
| Retrieve Invoice by ID (Query) | PRD 1, Section 4.2 | ✅ Covered | InvoiceDetailResponse with line items, payments |
| List Invoices (Query) | PRD 1, Section 4.2 | ✅ Covered | Filters by status/customer/date, pagination |
| Multiple Line Items | PRD 1, Section 4.2 | ✅ Covered | LineItem entity with description, qty, unit price |
| Line Item Discounts | PRD 1, Section 4.2 | ✅ Covered | Percentage or Fixed discount types |
| Tax Calculations | PRD 1, Section 4.2 | ✅ Covered | Tax rate per line item, calculated line total |
| Invoice Lifecycle (Draft → Sent → Paid) | PRD 1, Section 4.2 | ✅ Covered | State transitions with business rules |
| Balance Calculation | PRD 1, Section 4.2 | ✅ Covered | Total - Amount Paid = Balance Due |
| Invoice Numbering (INV-YYYY-####) | PRD 1, Section 4.2 | ✅ Covered | Auto-increment format specified |
| **Payment Domain** |
| Record Payment (Command) | PRD 1, Section 4.3 | ✅ Covered | Validates amount > 0, updates invoice balance atomically |
| Retrieve Payment by ID (Query) | PRD 1, Section 4.3 | ✅ Covered | PaymentDetailResponse DTO |
| List Payments (Query) | PRD 1, Section 4.3 | ✅ Covered | Filters by date/customer/invoice, pagination |
| Payment Methods (Credit Card/ACH) | PRD 1, Section 4.3 | ✅ Covered | Enum with simulated processing |
| Partial Payments | PRD 1, Section 4.3 | ✅ Covered | Invoice remains Sent if balance > 0 |
| **User Management** |
| Authentication (Login) | PRD 1, Section 4.8 | ✅ Covered | JWT tokens, HttpOnly cookies, 24-hour expiry |
| Role-Based Access Control | PRD 1, Section 3 | ✅ Covered | 4 roles (SysAdmin, Accountant, Sales, Customer) with RBAC matrix |
| **Technical Stack** |
| Java + Spring Boot (Backend) | PRD 2, Section 2.1 | ✅ Covered | Spring Boot 3.2.x, Java 17 LTS |
| TypeScript + React/Next.js (Frontend) | PRD 2, Section 2.2 | ✅ Covered | Next.js 14.x, React 18.x, MVVM pattern |
| PostgreSQL Database | PRD 2, Section 2.1, 5 | ✅ Covered | Supabase managed PostgreSQL 15.x |
| RESTful APIs | PRD 2, Section 4 | ✅ Covered | `/api/v1/*` endpoints documented |
| Cloud Deployment (AWS/Azure) | PRD 2, Section 9 | ✅ Covered | AWS Amplify (frontend), Elastic Beanstalk (backend) |
| **Performance** |
| API Latency <200ms | PRD 1, Section 6 | ✅ Covered | Target specified, optimization strategies documented |
| UI Responsiveness | PRD 1, Section 6 | ✅ Covered | Page load <2s, dashboard <2s |
| **Code Quality** |
| Modular, Readable Code | PRD 2, Section 11.2 | ✅ Covered | Development guidelines provided |
| DTOs for Boundary Crossing | PRD 2, Section 4.9 | ✅ Covered | Request/Response DTOs documented |
| Domain Events (Optional) | PRD 2, Section 3.4 | ✅ Covered | Event architecture with @TransactionalEventListener |
| **Testing** |
| Integration Tests (Mandatory) | PRD 1, Section 7 | ✅ Covered | 6 test scenarios documented (Customer Payment Flow, Partial Payment, Overpayment, etc.) |
| **Deliverables** |
| Code Repository | PRD 2, Section 11.1 | ✅ Covered | GitHub preferred, local setup documented |
| Demo Video | Demo_script.md | ✅ Covered | 5-minute script with core flows |
| Technical Writeup (1-2 pages) | FEATURES.md, Section 3.1 | ✅ Covered | Architecture diagram, DDD boundaries, CQRS implementation |
| AI Tool Documentation | FEATURES.md, Section 3.2 | ✅ Covered | Tools used, example prompts, acceleration metrics |
| **Extended Features (Not in InvoiceMe.md)** |
| Recurring Invoices | PRD 1, Section 4.5 | ⚠️ Extended | Template creation, scheduled generation — **NOT in InvoiceMe.md** |
| Late Fees | PRD 1, Section 4.6 | ⚠️ Extended | Monthly late fee calculation — **NOT in InvoiceMe.md** |
| Refunds | PRD 1, Section 4.4 | ⚠️ Extended | Refund processing with credit application — **NOT in InvoiceMe.md** |
| Customer Portal | PRD 1, Section 4.9 | ⚠️ Extended | Self-service dashboard — **NOT in InvoiceMe.md** |
| Dashboard & Reporting | PRD 1, Section 4.9 | ⚠️ Extended | Financial metrics visualization — **NOT in InvoiceMe.md** |
| Email Notifications | PRD 1, Section 4.10 | ⚠️ Extended | AWS SES integration — **NOT in InvoiceMe.md** (Domain Events encouraged) |
| PDF Generation | PRD 1, Section 4.2 | ⚠️ Extended | Invoice PDF download — **NOT in InvoiceMe.md** |
| User Account Approval | PRD 1, Section 4.8 | ⚠️ Extended | Registration → Approval flow — **NOT in InvoiceMe.md** |
| Password Reset | PRD 1, Section 4.8 | ⚠️ Extended | Forgot password flow — **NOT in InvoiceMe.md** |

**Legend**: ✅ Covered = Fully specified in PRDs | ⚠️ Extended = Beyond InvoiceMe.md scope | ⚠️ Partial = Needs clarification

---

## 3. Gap Intake Summary

**Note**: User has not yet provided a "Gaps List". The following gaps are identified from PRD analysis:

### Critical Gaps

**GAP-001: MVP Scope Ambiguity**
- **Issue**: PRDs include 8 extended features (Recurring Invoices, Late Fees, Refunds, Customer Portal, Dashboard, Email, PDF, User Approval) not explicitly required by InvoiceMe.md. InvoiceMe.md only requires "basic authentication" and "core CRUD operations."
- **Impact**: Risk of over-scoping and missing 5-7 day timeline if all features implemented.
- **Resolution Proposal**:
  - **Option A (MVP-First)**: Implement only InvoiceMe.md mandatory requirements (27 core features). Defer extended features to post-MVP if time permits.
  - **Option B (Full PRD)**: Implement all PRD features, accepting potential timeline risk. Prioritize core flows first, extended features second.
- **Acceptance Test**: Clear decision on MVP scope documented in Decision Log. If Option A, extended features marked as "Post-MVP" in backlog.

**GAP-002: Domain Events Implementation Detail**
- **Issue**: InvoiceMe.md encourages domain events but doesn't specify implementation pattern. PRD 2 Section 3.4 documents @TransactionalEventListener(AFTER_COMMIT) but doesn't clarify event store vs. in-memory events.
- **Impact**: Backend agent may implement events inconsistently.
- **Resolution Proposal**: Use Spring ApplicationEventPublisher with in-memory events (no event store required for MVP). Events published after transaction commit for side effects (email, audit log).
- **Acceptance Test**: Domain events listed in `/backend/docs/events.md` with producer/consumer mapping. Integration test verifies event triggers email notification.

### Major Gaps

**GAP-003: Performance Measurement Methodology**
- **Issue**: InvoiceMe.md requires <200ms API latency but doesn't specify measurement conditions (local vs. cloud, sample size, p50/p95/p99).
- **Impact**: QA agent may measure incorrectly, leading to false pass/fail.
- **Resolution Proposal**: Measure p95 latency for CRUD endpoints (Create Customer, Get Invoice, List Invoices) in local environment with 100 requests. Document methodology in `/qa/results/performance.md`.
- **Acceptance Test**: Performance report includes latency table with sample size, p50/p95/p99 percentiles, and environment details.

**GAP-004: Integration Test Scope**
- **Issue**: InvoiceMe.md requires "Customer Payment flow" integration test but PRD 1 Section 7 lists 6 test scenarios including extended features (Late Fees, Recurring Invoices, Refunds).
- **Impact**: QA agent may over-test extended features not in MVP scope.
- **Resolution Proposal**: If MVP scope (Option A), implement only 3 core tests: (1) Customer → Invoice → Payment E2E, (2) Partial Payment, (3) Overpayment → Credit. Defer extended feature tests to post-MVP.
- **Acceptance Test**: Test plan in `/qa/test-plan.md` clearly marks MVP vs. Extended tests. MVP tests pass before extended tests run.

### Minor Gaps

**GAP-005: Database Migration Strategy**
- **Issue**: PRD 2 mentions Flyway but doesn't specify migration file naming convention or rollback strategy.
- **Resolution Proposal**: Use Flyway naming `V{version}__{description}.sql`. No rollback migrations required for MVP (fresh database). Document in `/backend/docs/migrations.md`.
- **Acceptance Test**: Migration files exist in `/backend/src/main/resources/db/migration/` with sequential versions.

**GAP-006: Frontend MVVM Pattern Clarification**
- **Issue**: PRD 2 Section 6.1 describes MVVM with custom hooks as ViewModels, but doesn't clarify if state management library (Redux/Zustand) is needed.
- **Resolution Proposal**: Use React Context + Hooks only (no Redux). Custom hooks (`useInvoiceDetail`, `useCustomers`) act as ViewModels. Document pattern in `/frontend/docs/mvvm-pattern.md`.
- **Acceptance Test**: Frontend code structure matches PRD 2 Section 6.2 with hooks in `/hooks/` directory.

---

## 4. Clarifying Questions (MUST ANSWER BEFORE EXECUTION)

### Blocking Questions

**Q1: MVP Scope Confirmation** ⚠️ **BLOCKING**
- **Source**: InvoiceMe.md Section 2 (core requirements) vs. PRD 1 Sections 4.4-4.10 (extended features)
- **Question**: Should we implement only InvoiceMe.md mandatory requirements (27 core features) for MVP, or include all PRD features (Recurring Invoices, Late Fees, Refunds, Customer Portal, Dashboard, Email, PDF, User Approval)?
- **Rationale**: InvoiceMe.md doesn't mention extended features. PRDs add significant scope. Need clear decision to avoid over-scoping.
- **Options**:
  - **A**: MVP-only (InvoiceMe.md requirements) — faster delivery, lower risk
  - **B**: Full PRD scope — more complete system, higher timeline risk
  - **C**: MVP + 2-3 extended features (specify which)
- **Impact**: If unanswered, Backend/Frontend agents may implement unnecessary features, wasting time.

**Q2: Domain Events Implementation Pattern** ⚠️ **BLOCKING**
- **Source**: InvoiceMe.md Section 4.1 (encourages events) vs. PRD 2 Section 3.4 (specifies @TransactionalEventListener)
- **Question**: Should domain events be implemented using Spring ApplicationEventPublisher with in-memory events (no event store), or is an event store (e.g., EventStore, Kafka) required?
- **Rationale**: PRD 2 documents in-memory pattern but doesn't explicitly rule out event store. Backend agent needs clarity.
- **Options**:
  - **A**: In-memory events (Spring ApplicationEventPublisher) — simpler, sufficient for MVP
  - **B**: Event store (EventStore/Kafka) — more complex, better for production scalability
- **Impact**: If unanswered, Backend agent may choose wrong pattern, requiring refactor.

**Q3: Performance Measurement Environment** ⚠️ **BLOCKING**
- **Source**: InvoiceMe.md Section 3.3 (requires <200ms) vs. PRD 1 Section 6 (specifies p95)
- **Question**: Should performance be measured in local environment (localhost) or cloud (AWS)? What percentile (p50/p95/p99) and sample size?
- **Rationale**: InvoiceMe.md says "local testing environment" but doesn't specify methodology. PRD 1 mentions p95 but not sample size.
- **Options**:
  - **A**: Local environment, p95 latency, 100 requests per endpoint
  - **B**: Cloud environment (AWS), p95 latency, 1000 requests per endpoint
  - **C**: Other (specify)
- **Impact**: If unanswered, QA agent may measure incorrectly, leading to false pass/fail.

**Q4: Integration Test Scope** ⚠️ **BLOCKING**
- **Source**: InvoiceMe.md Section 4.2 (requires Customer Payment flow) vs. PRD 1 Section 7 (lists 6 scenarios including extended features)
- **Question**: Should integration tests cover only MVP flows (Customer → Invoice → Payment, Partial Payment, Overpayment) or all PRD scenarios (including Late Fees, Recurring Invoices, Refunds)?
- **Rationale**: Depends on MVP scope decision (Q1). If MVP-only, extended feature tests should be deferred.
- **Options**:
  - **A**: MVP tests only (3 scenarios) — aligns with InvoiceMe.md
  - **B**: All PRD tests (6 scenarios) — more comprehensive, requires extended features
- **Impact**: If unanswered, QA agent may write tests for features not in scope.

### Non-Blocking Questions

**Q5: Frontend Framework Choice**
- **Source**: InvoiceMe.md Section 3.2 (React.js or Next.js) vs. PRD 2 Section 2.2 (specifies Next.js 14.x)
- **Question**: PRD 2 specifies Next.js 14.x. Should we use Next.js (SSR) or plain React (CSR)? Any preference?
- **Rationale**: Next.js adds SSR complexity but better SEO/performance. Plain React is simpler.
- **Recommendation**: Use Next.js 14.x as specified in PRD 2 (App Router) for production readiness.
- **Impact**: Low — PRD 2 is clear, but confirming user preference.

**Q6: Database for Local Development**
- **Source**: InvoiceMe.md Section 3.2 (PostgreSQL preferred, H2/SQLite permitted for testing)
- **Question**: Should local development use PostgreSQL (via Docker) or H2 in-memory database?
- **Rationale**: PostgreSQL is production-ready but requires Docker setup. H2 is faster for local dev but may hide PostgreSQL-specific issues.
- **Recommendation**: Use PostgreSQL via Docker Compose for consistency with production (Supabase).
- **Impact**: Low — can switch if needed.

**Q7: Authentication Implementation Detail**
- **Source**: InvoiceMe.md Section 2.4 (basic authentication) vs. PRD 1 Section 4.8 (JWT, account approval, password reset)
- **Question**: For MVP, should we implement only basic login (email/password → JWT) or full auth flow (registration → approval → password reset)?
- **Rationale**: InvoiceMe.md says "basic authentication" but PRD 1 includes full user management.
- **Recommendation**: If MVP scope (Q1-A), implement basic login only. Defer registration/approval/reset to post-MVP.
- **Impact**: Low — depends on MVP scope decision.

---

## 5. Delivery Plan & Milestones

### Milestone Overview

| Milestone | Goal | Deliverables | Acceptance Criteria | Dependencies |
|-----------|------|-------------|---------------------|--------------|
| **M0** | Planning Completed | ORCHESTRATOR_OUTPUT.md approved, questions answered | All blocking questions resolved, scope confirmed | None |
| **M1** | Domain & API Contract Frozen | Domain aggregates documented, OpenAPI spec, event list | DDD aggregates defined, CQRS commands/queries separated, API contracts validated | M0 |
| **M2** | Core Flows Working | Customer → Invoice → Payment E2E, integration tests passing | All MVP CRUD operations functional, integration tests green, <200ms API latency | M1 |
| **M3** | Non-Functional Targets Validated | Performance report, UI responsiveness verified | API <200ms (p95), dashboard <2s, error handling graceful | M2 |
| **M4** | Extended Features (if approved) | Recurring Invoices, Late Fees, Refunds, Customer Portal | Extended features functional, tests passing | M2 |
| **M5** | Demo + Writeups | Demo video, TECHNICAL_WRITEUP.md, AI_TOOL_DOCUMENTATION.md | Video follows Demo_script.md, writeups complete | M2 (or M4 if extended) |

### Detailed Milestone Breakdown

#### M0: Planning Completed (Current Phase)
**Duration**: 1 day  
**Owner**: Orchestrator  
**Tasks**:
- [x] Load and analyze InvoiceMe.md, PRD 1, PRD 2, FEATURES.md
- [x] Create traceability matrix
- [x] Identify gaps and propose resolutions
- [x] Generate clarifying questions
- [ ] **WAIT FOR USER**: Answer blocking questions (Q1-Q4)
- [ ] Update Decision Log with scope confirmation
- [ ] Issue sub-agent prompts

**Deliverables**:
- ORCHESTRATOR_OUTPUT.md (this document)
- Decision Log (Section 7)

**Acceptance Criteria**:
- All blocking questions answered
- MVP scope confirmed (MVP-only vs. Full PRD)
- Sub-agent prompts ready for execution

---

#### M1: Domain & API Contract Frozen
**Duration**: 1 day  
**Owner**: Backend Agent (with Data/DB Agent support)  
**Tasks**:
- Define DDD aggregates (Customer, Invoice, Payment) with rich behavior
- Document aggregate invariants and domain events
- Create OpenAPI spec (`/backend/docs/api/openapi.yaml`) with commands vs. queries labeled
- Design database schema (ERD) and Flyway migrations
- List domain events with producers/consumers

**Deliverables**:
- `/backend/docs/domain-aggregates.md` — DDD boundaries, aggregate roots, value objects
- `/backend/docs/api/openapi.yaml` — REST API specification (OpenAPI 3.0)
- `/backend/docs/events.md` — Domain events list (PaymentRecorded, InvoiceSent, etc.)
- `/backend/docs/erd.png` — Entity Relationship Diagram
- `/backend/src/main/resources/db/migration/V1__*.sql` — Initial Flyway migrations

**Acceptance Criteria**:
- All InvoiceMe.md must-haves modeled in domain aggregates
- Commands/Queries clearly separated in API spec
- Domain events defined with transactional consistency pattern
- Database schema supports all MVP operations
- **Review**: Orchestrator validates against InvoiceMe.md requirements

**Risks & Mitigations**:
- **Risk**: Domain model too complex → Mitigation: Start with InvoiceMe.md requirements only, defer extended features
- **Risk**: API contracts change later → Mitigation: Freeze contracts before implementation, version API (`/api/v1`)

---

#### M2: Core Flows Working
**Duration**: 2-3 days  
**Owner**: Backend Agent, Frontend Agent, QA Agent  
**Tasks**:
- **Backend**: Implement command handlers (CreateCustomer, CreateInvoice, MarkAsSent, RecordPayment)
- **Backend**: Implement query handlers (GetCustomer, ListCustomers, GetInvoice, ListInvoices, GetPayment, ListPayments)
- **Backend**: Implement domain events (InvoiceSentEvent, PaymentRecordedEvent) with email listeners
- **Frontend**: Implement MVVM pattern with React hooks (useCustomers, useInvoiceDetail, usePayments)
- **Frontend**: Build UI components (Customer list/detail, Invoice list/detail, Payment form)
- **QA**: Write integration tests (Customer → Invoice → Payment E2E, Partial Payment, Overpayment → Credit)

**Deliverables**:
- Backend: Spring Boot application with REST APIs functional
- Frontend: Next.js application with core UI pages functional
- QA: Integration test suite passing (`/qa/integration-tests/`)
- QA: Test results report (`/qa/results/summary.md`)

**Acceptance Criteria**:
- ✅ Customer CRUD: Create, Update, Delete, Get, List — all working
- ✅ Invoice CRUD: Create (Draft), Update, Mark as Sent, Get, List — all working
- ✅ Payment: Record Payment, Get, List — all working
- ✅ Invoice lifecycle: Draft → Sent → Paid transitions working
- ✅ Balance calculation: Total - Amount Paid = Balance Due (correct)
- ✅ Overpayment → Credit: Excess payment adds to customer credit balance
- ✅ Integration tests: All MVP tests pass (3 scenarios minimum)
- ✅ API latency: <200ms for CRUD operations (p95, local environment)

**Risks & Mitigations**:
- **Risk**: Performance target missed → Mitigation: Optimize queries early (indexes, DTO projections), measure incrementally
- **Risk**: Integration tests fail → Mitigation: Write tests alongside implementation, not after
- **Risk**: Frontend/Backend integration issues → Mitigation: Use OpenAPI spec for contract, mock API during frontend dev

---

#### M3: Non-Functional Targets Validated
**Duration**: 0.5 day  
**Owner**: QA Agent, DevOps Agent  
**Tasks**:
- Measure API latency (p50/p95/p99) for CRUD endpoints with 100 requests
- Verify UI responsiveness (page load <2s, dashboard <2s)
- Test error handling (404, 400, 500 responses)
- Validate authentication (JWT tokens, RBAC)

**Deliverables**:
- `/qa/results/performance.md` — Latency table with percentiles, sample sizes
- `/qa/results/ui-performance.md` — Page load times, Lighthouse scores
- Screenshots/logs of performance tests

**Acceptance Criteria**:
- API latency <200ms (p95) for Create Customer, Get Invoice, List Invoices
- Dashboard loads in <2s (First Contentful Paint)
- Error handling returns user-friendly messages
- Authentication works (login → JWT → authorized requests)

**Risks & Mitigations**:
- **Risk**: Performance target missed → Mitigation: Optimize slow queries, add database indexes, use DTO projections

---

#### M4: Extended Features (Conditional — Only if Q1 answered "B" or "C")
**Duration**: 1-2 days  
**Owner**: Backend Agent, Frontend Agent  
**Tasks**:
- Implement Recurring Invoices (template creation, scheduled generation)
- Implement Late Fees (scheduled job, line item addition)
- Implement Refunds (refund command, invoice reopening, credit application)
- Implement Customer Portal (self-service dashboard, payment form)
- Implement Dashboard & Reporting (metrics API, visualizations)
- Implement Email Notifications (AWS SES integration)
- Implement PDF Generation (iText 7)

**Deliverables**:
- Extended feature implementations
- Extended feature integration tests

**Acceptance Criteria**:
- All extended features functional (if scope approved)
- Extended feature tests passing

**Cut Line**: If timeline exceeds 7 days, defer extended features to post-MVP.

---

#### M5: Demo + Writeups
**Duration**: 1 day  
**Owner**: Docs Agent (with support from all agents)  
**Tasks**:
- Record 5-minute demo video following Demo_script.md
- Write TECHNICAL_WRITEUP.md (architecture diagram, DDD boundaries, CQRS implementation, database schema, design decisions)
- Write AI_TOOL_DOCUMENTATION.md (tools used, example prompts, acceleration metrics, limitations)

**Deliverables**:
- Demo video (YouTube/Vimeo link in README.md)
- `/TECHNICAL_WRITEUP.md` — 1-2 pages architecture documentation
- `/AI_TOOL_DOCUMENTATION.md` — AI tool usage documentation

**Acceptance Criteria**:
- Demo video shows core InvoiceMe.md flows (Customer → Invoice → Payment)
- Demo video highlights architecture (DDD/CQRS/VSA)
- Technical writeup includes architecture diagram, DDD boundaries, CQRS flow, database schema
- AI documentation includes 5-10 example prompts, acceleration metrics, limitations

---

### Timeline Summary

| Phase | Duration | Cumulative |
|-------|----------|------------|
| M0: Planning | 1 day | Day 1 |
| M1: Domain & API | 1 day | Day 2 |
| M2: Core Flows | 2-3 days | Day 3-5 |
| M3: Non-Functional | 0.5 day | Day 5.5 |
| M4: Extended (if approved) | 1-2 days | Day 6-7 |
| M5: Demo + Writeups | 1 day | Day 7-8 |

**Total MVP Timeline**: 5.5 days (M0-M3)  
**Total Full PRD Timeline**: 7-8 days (M0-M5 with M4)

---

## 6. Sub-Agent Prompts

### A) Backend Agent Prompt

**[AGENT]: Backend**

**GOAL**: Implement Spring Boot REST API with DDD aggregates, CQRS command/query separation, and Vertical Slice Architecture. **FULL PRD SCOPE**: All features (27 core + 8 extended). Domains: Customers, Invoices, Payments, Recurring Invoices, Refunds, Users, Dashboard.

**INPUTS**:
- InvoiceMe.md (assessment source of truth)
- PRD_1_Business_Reqs.md (business rules, RBAC)
- PRD_2_Tech_Spec.md (technical architecture, API specs, database schema)
- ORCHESTRATOR_OUTPUT.md (this document — scope decisions, gaps resolved)
- Decision Log (Section 7) — MVP scope confirmation

**DELIVERABLES**:
- `/backend/src/main/java/com/invoiceme/` — Spring Boot application code
  - Vertical slice structure: `customers/createcustomer/`, `invoices/createinvoice/`, `payments/recordpayment/`, etc.
  - Domain layer: `Customer.java` (aggregate root), `Invoice.java` (aggregate root), `Payment.java` (aggregate root)
  - Domain events: `InvoiceSentEvent`, `PaymentRecordedEvent`, `InvoiceFullyPaidEvent`
- `/backend/docs/domain-aggregates.md` — DDD boundaries, aggregate invariants, value objects (Money, Email, InvoiceNumber)
- `/backend/docs/api/openapi.yaml` — OpenAPI 3.0 specification with commands vs. queries labeled
- `/backend/docs/events.md` — Domain events list with producers/consumers, transactional consistency pattern
- `/backend/src/main/resources/db/migration/V1__*.sql` — Flyway migrations (customers, invoices, line_items, payments, users tables)
- `/backend/src/test/java/` — Integration tests (Customer → Invoice → Payment E2E, Partial Payment, Overpayment → Credit)

**DONE CRITERIA**:
1. ✅ All InvoiceMe.md mandatory requirements implemented (27 core features):
   - Customer CRUD (Create, Update, Delete, Get, List)
   - Invoice CRUD (Create Draft, Update, Mark as Sent, Get, List)
   - Payment (Record Payment, Get, List)
   - Invoice lifecycle: Draft → Sent → Paid transitions
   - Balance calculation: Total - Amount Paid = Balance Due
   - Overpayment → Credit: Excess payment adds to customer credit balance
2. ✅ DDD principles evident:
   - Rich domain models with behavior (not anemic data models)
   - Aggregate roots enforce invariants (Customer.canBeDeleted(), Invoice.markAsSent())
   - Value objects (Money, Email, InvoiceNumber) are immutable
3. ✅ CQRS separation enforced:
   - Commands (CreateCustomerCommand, CreateInvoiceCommand, RecordPaymentCommand) handled by command handlers
   - Queries (GetCustomerQuery, ListCustomersQuery, GetInvoiceQuery) handled by query handlers
   - Same database, separate models (write = rich entities, read = flat DTOs)
4. ✅ Vertical Slice Architecture:
   - Code organized by feature (`createcustomer/`, `markassent/`, `recordpayment/`)
   - Each slice contains: Command/Query, Handler, Validator, Controller, Tests
5. ✅ Domain events implemented:
   - Events published after transaction commit (@TransactionalEventListener(AFTER_COMMIT))
   - InvoiceSentEvent → Email notification listener
   - PaymentRecordedEvent → Payment confirmation listener
6. ✅ Scheduled jobs implemented:
   - Recurring Invoices: `@Scheduled(cron = "0 0 * * *", zone = "America/Chicago")` (daily at midnight Central Time)
   - Late Fees: `@Scheduled(cron = "0 0 1 * *", zone = "America/Chicago")` (1st of month at midnight Central Time)
   - Spring `@EnableScheduling` enabled on main application class
7. ✅ API error handling:
   - RFC 7807 Problem Details format (use Spring's `ProblemDetail` class)
   - Standard format: `{ "type": "uri", "title": "...", "status": 400, "detail": "...", "instance": "..." }`
   - Field-level errors: `{ "errors": [{ "field": "email", "message": "..." }] }`
8. ✅ Pagination:
   - Spring Data JPA `Page<T>` format: `{ "content": [...], "page": 0, "size": 20, "totalElements": 150, "totalPages": 8, "first": true, "last": false }`
   - Include total count for UI pagination controls
9. ✅ JWT authentication:
   - No refresh tokens (user re-logs in after 24 hours)
   - Token expiry: 24 hours
   - Frontend handles token expiry by redirecting to login on 401
10. ✅ API latency <200ms (p95) for CRUD operations (measured locally, 100 requests)
11. ✅ Integration tests passing:
   - Customer → Invoice → Payment E2E flow
   - Partial payment flow
   - Overpayment → Credit flow
12. ✅ Extended features implemented (8 features):
   - Recurring Invoices (template creation, scheduled generation)
   - Late Fees (scheduled job, line item addition)
   - Refunds (refund command, invoice reopening, credit application)
   - Customer Portal (self-service dashboard, payment form)
   - Dashboard & Reporting (metrics API, visualizations)
   - Email Notifications (AWS SES integration)
   - PDF Generation (iText 7)
   - User Approval (registration → approval workflow)
13. ✅ Code quality:
   - Modular, readable, well-documented
   - DTOs for boundary crossing (MapStruct mappers)
   - Consistent naming conventions
14. ✅ Money rounding strategy:
    - Banker's rounding (HALF_UP) to 2 decimal places
    - Money value object with BigDecimal precision
    - Documented in `/backend/docs/domain-aggregates.md`

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Domain aggregates implemented (Customer, Invoice, Payment)
  - CQRS commands/queries separated (X commands, Y queries)
  - Vertical slices created (list features)
  - Domain events implemented (list events)
  - API endpoints functional (list endpoints)
  - Integration tests passing (X/Y scenarios)
  - API latency measured (p95 = X ms)
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Code: `/backend/src/main/java/com/invoiceme/`
  - Docs: `/backend/docs/domain-aggregates.md`, `/backend/docs/api/openapi.yaml`, `/backend/docs/events.md`
  - Tests: `/backend/src/test/java/`
  - Migrations: `/backend/src/main/resources/db/migration/`
- **Evidence**:
  - Integration test logs (screenshots or logs)
  - Performance test results (latency table)
  - API endpoint examples (curl commands or Postman collection)

**DO NOT**:
- Introduce libraries without justification and tradeoffs documented
- Change API contracts after M1 freeze without Orchestrator approval
- Use anemic domain models (entities with only getters/setters)
- Skip Money value object with proper rounding (mandatory for balance calculations)
- Use UTC timezone for scheduled jobs (must use Central Time - America/Chicago)
- Implement refresh token endpoint (no refresh tokens - user re-logs in after 24 hours)
- Use custom error format (must use RFC 7807 Problem Details)
- Use custom pagination format (must use Spring Data JPA Page<T>)

---

### B) Frontend Agent Prompt

**[AGENT]: Frontend**

**GOAL**: Implement Next.js 14.x frontend with React 18.x, TypeScript, and MVVM pattern. **FULL PRD SCOPE**: All UI features (Customers, Invoices, Payments, Recurring Invoices, Refunds, Customer Portal, Dashboard, User Management).

**INPUTS**:
- InvoiceMe.md (assessment source of truth)
- PRD_1_Business_Reqs.md (user roles, RBAC matrix, UI requirements)
- PRD_2_Tech_Spec.md (frontend architecture, MVVM pattern, component structure, API DTOs)
- ORCHESTRATOR_OUTPUT.md (this document — scope decisions)
- Backend OpenAPI spec (`/backend/docs/api/openapi.yaml`) — API contracts

**DELIVERABLES**:
- `/frontend/src/app/` — Next.js App Router pages:
  - `/customers/page.tsx` (Customer list)
  - `/customers/[id]/page.tsx` (Customer detail)
  - `/customers/new/page.tsx` (Create customer)
  - `/invoices/page.tsx` (Invoice list)
  - `/invoices/[id]/page.tsx` (Invoice detail)
  - `/invoices/new/page.tsx` (Create invoice)
  - `/payments/page.tsx` (Payment list)
  - `/login/page.tsx` (Login page)
- `/frontend/src/components/` — React components:
  - `/ui/` — shadcn/ui primitives (Button, Input, Table, etc.)
  - `/customers/` — Customer-specific components
  - `/invoices/` — Invoice-specific components
  - `/payments/` — Payment-specific components
  - `/layout/` — Header, Sidebar, Footer
- `/frontend/src/hooks/` — Custom hooks (ViewModels):
  - `useCustomers.ts` — Customer list/detail logic
  - `useInvoiceDetail.ts` — Invoice detail logic
  - `usePayments.ts` — Payment recording logic
  - `useAuth.ts` — Authentication logic
- `/frontend/src/lib/api.ts` — Axios instance with JWT interceptors
- `/frontend/src/types/` — TypeScript interfaces matching backend DTOs

**DONE CRITERIA**:
1. ✅ MVVM pattern implemented:
   - Models: TypeScript interfaces matching backend DTOs
   - Views: React components (presentational, receive props, emit events)
   - ViewModels: Custom hooks managing state, API calls, business logic
2. ✅ Core UI pages functional:
   - Customer list (with filters, pagination)
   - Customer detail (with edit/delete actions)
   - Create customer form (with validation)
   - Invoice list (with filters, pagination)
   - Invoice detail (with Mark as Sent, Record Payment actions)
   - Create invoice form (with multiple line items, discount, tax)
   - Payment list (with filters)
   - Login page (email/password → JWT)
3. ✅ RBAC enforced in UI:
   - Conditional rendering based on user role (SysAdmin, Accountant, Sales, Customer)
   - "Delete Customer" button only visible to SysAdmin
   - "Record Payment" button only visible to SysAdmin, Accountant, Customer (own invoices)
4. ✅ UI responsiveness:
   - Page load <2s (First Contentful Paint)
   - Dashboard <2s (if implemented)
   - Smooth interactions without lag
5. ✅ Form validation:
   - React Hook Form for form state
   - Client-side validation (email format, required fields, number ranges)
   - Error messages displayed clearly
6. ✅ API integration:
   - Axios instance configured with base URL (`process.env.NEXT_PUBLIC_API_URL`)
   - JWT token added to Authorization header (from localStorage)
   - Error handling (401 → redirect to login, 400 → show error message)
7. ✅ Styling:
   - Tailwind CSS utility classes
   - shadcn/ui components for consistent design
   - Mobile-responsive (customer portal at minimum)
8. ✅ Extended features UI implemented:
   - Recurring Invoices UI (template creation, list, pause/resume)
   - Refunds UI (issue refund form, refund history)
   - Customer Portal (self-service dashboard, payment form, invoice list)
   - Dashboard (metrics, charts, aging report)
   - User Management (registration, pending users list, approval/rejection)

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Pages implemented (list pages)
  - Components created (count)
  - Custom hooks (ViewModels) created (list)
  - RBAC enforced (roles supported)
  - API integration working (endpoints connected)
  - UI performance (page load times)
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Code: `/frontend/src/`
  - Components: `/frontend/src/components/`
  - Hooks: `/frontend/src/hooks/`
  - Types: `/frontend/src/types/`
- **Evidence**:
  - Screenshots of UI pages
  - Lighthouse performance scores (if available)
  - Browser console logs (no errors)

**DO NOT**:
- Use Redux/Zustand for state management (use React Context + Hooks only)
- Write custom CSS (use Tailwind utility classes)
- Hardcode API URLs (use environment variables)
- Skip mobile responsiveness (customer portal must be mobile-friendly)

---

### C) Data/DB Agent Prompt

**[AGENT]: Data/DB**

**GOAL**: Design PostgreSQL database schema with Flyway migrations, ERD diagram, and database optimization strategies. **FULL PRD SCOPE**: Support all operations (Customers, Invoices, Payments, Recurring Invoices, Refunds, Users, Activity Feed) with proper indexes and constraints.

**INPUTS**:
- InvoiceMe.md (assessment source of truth)
- PRD_2_Tech_Spec.md (database schema Section 5, Flyway migrations)
- Backend domain aggregates (`/backend/docs/domain-aggregates.md`) — entity relationships
- ORCHESTRATOR_OUTPUT.md (this document — scope decisions)

**DELIVERABLES**:
- `/backend/src/main/resources/db/migration/` — Flyway migration files:
  - `V1__create_customers_table.sql`
  - `V2__create_invoices_table.sql`
  - `V3__create_line_items_table.sql`
  - `V4__create_payments_table.sql`
  - `V5__create_users_table.sql`
  - `V6__create_recurring_invoice_templates_table.sql`
  - `V7__create_template_line_items_table.sql`
  - `V8__create_activity_feed_table.sql`
  - `V9__create_password_reset_tokens_table.sql`
  - `V10__create_indexes.sql`
- `/backend/docs/erd.png` — Entity Relationship Diagram (customers → invoices → line_items, customers → invoices → payments)
- `/backend/docs/database-schema.md` — Schema documentation:
  - Table descriptions
  - Foreign key relationships
  - Indexes and their purposes
  - Constraints (CHECK, UNIQUE, NOT NULL)
- `/backend/docs/migrations.md` — Migration strategy documentation

**DONE CRITERIA**:
1. ✅ Database schema supports all PRD operations (core + extended):
   - Customers table (id, company_name, email, credit_balance, status, etc.)
   - Invoices table (id, invoice_number, customer_id, status, totals, balance_due, etc.)
   - Line items table (id, invoice_id, description, quantity, unit_price, discount, tax_rate, etc.)
   - Payments table (id, invoice_id, customer_id, amount, payment_method, status, etc.)
   - Users table (id, email, password_hash, role, customer_id FK, status, etc.)
   - Recurring invoice templates table (id, customer_id, frequency, next_invoice_date, status, etc.)
   - Template line items table (id, template_id, description, quantity, unit_price, etc.)
   - Activity feed table (id, aggregate_id, event_type, description, occurred_at, user_id, etc.)
   - Password reset tokens table (id, user_id, token, expires_at, used, etc.)
2. ✅ Proper constraints:
   - Foreign keys with CASCADE DELETE where appropriate (line_items → invoices)
   - UNIQUE constraints (email, invoice_number)
   - CHECK constraints (quantity >= 1, amount > 0)
   - NOT NULL constraints on required fields
3. ✅ Indexes for performance:
   - Foreign keys indexed (customer_id, invoice_id)
   - Frequently queried fields indexed (email, status, due_date, invoice_number)
   - Composite indexes for common queries (customer_id + status)
4. ✅ Flyway migrations:
   - Sequential versions (V1, V2, V3, ...)
   - Immutable (no changes after deployment)
   - Idempotent (can run multiple times safely)
5. ✅ ERD diagram:
   - Visual representation of relationships
   - Cardinality shown (1:N, N:1)
   - Key fields labeled

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Tables created (list tables)
  - Foreign keys defined (list relationships)
  - Indexes created (list indexes and purposes)
  - Constraints enforced (list constraints)
  - Migration files (count)
  - ERD diagram created
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Migrations: `/backend/src/main/resources/db/migration/`
  - ERD: `/backend/docs/erd.png`
  - Schema doc: `/backend/docs/database-schema.md`
- **Evidence**:
  - ERD diagram (PNG image)
  - Migration file examples (snippets)
  - Database schema validation (tables created successfully)

**DO NOT**:
- Use database-specific features that aren't portable (use standard SQL)
- Skip indexes on foreign keys (performance critical)
- Skip tables for extended features (all PRD tables required: recurring_invoice_templates, template_line_items, activity_feed, password_reset_tokens)

---

### D) QA Agent Prompt

**[AGENT]: QA**

**GOAL**: Author integration tests proving Customer → Invoice → Payment E2E flow plus overpayment → credit. Measure API performance (<200ms target). Generate test evidence (logs, screenshots, timings).

**INPUTS**:
- InvoiceMe.md (assessment source of truth — integration tests mandatory)
- PRD_1_Business_Reqs.md (test scenarios Section 7)
- PRD_2_Tech_Spec.md (testing requirements)
- ORCHESTRATOR_OUTPUT.md (this document — MVP scope, performance methodology)
- Backend API (`/backend/docs/api/openapi.yaml`) — API contracts

**DELIVERABLES**:
- `/qa/test-plan.md` — Test plan document:
  - All 6 PRD test scenarios (Customer Payment Flow, Partial Payment, Overpayment & Credit, Late Fee Calculation, Recurring Invoice, Refund Flow)
  - Performance test scenarios (API latency measurement - local + AWS)
- `/qa/integration-tests/` — Integration test code:
  - `CustomerPaymentFlowTest.java` — E2E flow test
  - `PartialPaymentTest.java` — Partial payment test
  - `OverpaymentCreditTest.java` — Overpayment → credit test
  - `LateFeeCalculationTest.java` — Late fee scheduled job test
  - `RecurringInvoiceTest.java` — Recurring invoice generation test
  - `RefundFlowTest.java` — Refund processing test
  - `PerformanceTest.java` — API latency measurement (local + AWS)
- `/qa/results/summary.md` — Test results report:
  - Pass/fail table for all tests
  - Performance results (latency table with p50/p95/p99, sample sizes)
  - Screenshots/logs of test executions
- `/qa/results/performance.md` — Detailed performance report:
  - Latency table (endpoint, p50, p95, p99, sample size)
  - Environment details (local vs. cloud, database type)
  - Optimization recommendations (if targets missed)

**DONE CRITERIA**:
1. ✅ Integration tests implemented (all 6 PRD scenarios):
   - **Customer Payment Flow**: Create customer → Create invoice → Mark sent → Record payment → Verify paid
   - **Partial Payment Flow**: Create invoice ($5K) → Record payment ($2K) → Verify balance = $3K, status = Sent
   - **Overpayment & Credit**: Invoice ($1K) → Payment ($1.2K) → Verify credit = $200 → Create 2nd invoice ($500) → Mark sent → Verify credit applied, total = $300
   - **Late Fee Calculation**: Create invoice (due 30 days ago) → Run scheduled job → Verify late fee line item added ($125), email sent
   - **Recurring Invoice**: Create template (monthly) → Run scheduled job → Verify invoice created, status = Sent, Next date updated
   - **Refund Flow**: Create invoice ($1K) → Payment ($1K) → Issue refund ($300) → Verify status Paid → Sent, Balance = $300, Refund recorded
2. ✅ Performance tests implemented:
   - API latency measurement for CRUD endpoints (Create Customer, Get Invoice, List Invoices)
   - **Local environment**: p95 latency <200ms (100 requests per endpoint)
   - **AWS environment**: p95 latency <200ms (1000 requests per endpoint, after deployment)
   - Latency table with p50/p95/p99 percentiles for both environments
3. ✅ Test evidence captured:
   - Test execution logs (screenshots or text logs)
   - Performance test results (latency table)
   - Test pass/fail status clearly documented
4. ✅ Test methodology documented:
   - Performance measurement conditions (local vs. cloud, sample size, percentile)
   - Test data setup/teardown procedures
   - Environment details (database, Spring Boot version, etc.)

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Integration tests implemented (list scenarios)
  - Performance tests implemented (list endpoints measured)
  - Test results (X/Y tests passing)
  - API latency results (p95 = X ms for each endpoint)
  - Performance targets met (yes/no for each endpoint)
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Test plan: `/qa/test-plan.md`
  - Test code: `/qa/integration-tests/`
  - Results: `/qa/results/summary.md`, `/qa/results/performance.md`
- **Evidence**:
  - Test execution logs (screenshots or logs)
  - Performance test results (latency table)
  - Test pass/fail table

**DO NOT**:
- Skip performance measurement (mandatory requirement)
- Measure only in one environment (must test both local and AWS)
- Skip integration tests for extended features (all 6 PRD test scenarios required)

---

### E) DevOps Agent Prompt

**[AGENT]: DevOps**

**GOAL**: Set up local development environment (Docker Compose for PostgreSQL, Spring Boot, Next.js) and one cloud deployment target (AWS). Configure CI/CD pipeline (GitHub Actions) and monitoring (CloudWatch logs).

**INPUTS**:
- PRD_2_Tech_Spec.md (deployment architecture Section 9, CI/CD Section 10)
- ORCHESTRATOR_OUTPUT.md (this document — scope decisions)
- Backend application (Spring Boot JAR)
- Frontend application (Next.js build)

**DELIVERABLES**:
- `/docker-compose.yml` — Local development environment:
  - PostgreSQL 15 container
  - Spring Boot application (optional, can run locally)
  - Environment variables (.env file)
- `/backend/.github/workflows/deploy.yml` — GitHub Actions CI/CD pipeline:
  - Backend tests → Build JAR → Deploy to AWS Elastic Beanstalk
  - Frontend tests → Build → Deploy to AWS Amplify
- `/docs/deployment.md` — Deployment documentation:
  - Local setup instructions
  - AWS deployment instructions (Elastic Beanstalk, Amplify)
  - Environment variables list
  - Troubleshooting guide
- `/docs/monitoring.md` — Monitoring setup:
  - CloudWatch logs configuration
  - Custom metrics (API latency, error rate)
  - Alarms (error rate > 5%, database pool exhausted)

**DONE CRITERIA**:
1. ✅ Local environment runs successfully:
   - Docker Compose starts PostgreSQL
   - Spring Boot connects to database
   - Next.js connects to backend API
   - All services accessible (backend on :8080, frontend on :3000)
2. ✅ Cloud deployment configured:
   - AWS Elastic Beanstalk application created (backend)
   - AWS Amplify app created (frontend)
   - Environment variables configured (DATABASE_URL, JWT_SECRET, AWS_SES_REGION, etc.)
   - Deployment successful (backend and frontend accessible)
3. ✅ CI/CD pipeline functional:
   - GitHub Actions workflow runs on push to main
   - Backend tests → Build → Deploy (if tests pass)
   - Frontend tests → Build → Deploy (if tests pass)
   - Deployment notifications (Slack or email)
4. ✅ Monitoring configured:
   - CloudWatch logs streaming from Elastic Beanstalk
   - Custom metrics (API latency p95, error rate)
   - Alarms configured (error rate > 5%)

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Local environment setup (Docker Compose working)
  - Cloud deployment (AWS Elastic Beanstalk + Amplify URLs)
  - CI/CD pipeline (GitHub Actions workflow status)
  - Monitoring (CloudWatch logs/metrics/alarms configured)
  - Environment variables documented (list)
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Docker Compose: `/docker-compose.yml`
  - CI/CD: `/.github/workflows/deploy.yml`
  - Docs: `/docs/deployment.md`, `/docs/monitoring.md`
- **Evidence**:
  - Local environment screenshot (services running)
  - Cloud deployment URLs (backend API, frontend app)
  - CI/CD pipeline run logs (GitHub Actions)
  - CloudWatch dashboard screenshot (if available)

**DO NOT**:
- Deploy extended features (Recurring Invoices, Late Fees, etc.) unless MVP scope decision (Q1) answered "B" or "C"
- Skip local environment setup (required for development)
- Use production credentials in code (use environment variables)

---

### F) Docs Agent Prompt

**[AGENT]: Docs**

**GOAL**: Create post-development documentation: TECHNICAL_WRITEUP.md (architecture, DDD boundaries, CQRS implementation, database schema), AI_TOOL_DOCUMENTATION.md (AI tools used, example prompts, acceleration metrics), and coordinate demo video recording.

**INPUTS**:
- InvoiceMe.md (assessment source of truth — deliverables Section 5.2)
- FEATURES.md (documentation requirements Section 3)
- Demo_script.md (5-minute demo script)
- Backend code (`/backend/src/main/java/`) — architecture examples
- Frontend code (`/frontend/src/`) — MVVM pattern examples
- Backend docs (`/backend/docs/domain-aggregates.md`, `/backend/docs/api/openapi.yaml`, `/backend/docs/erd.png`)
- QA results (`/qa/results/summary.md`, `/qa/results/performance.md`)

**DELIVERABLES**:
- `/TECHNICAL_WRITEUP.md` — Technical documentation (1-2 pages):
  - Architecture diagram (DDD bounded contexts, CQRS flow, VSA structure)
  - DDD boundaries (Customer, Invoice, Payment aggregates and responsibilities)
  - CQRS implementation (command handlers vs. query handlers, event flow)
  - Database schema (ERD with explanation of key relationships)
  - Design decisions & trade-offs (same database for CQRS vs. separate, Supabase vs. AWS RDS, domain event implementation)
- `/AI_TOOL_DOCUMENTATION.md` — AI tool usage documentation:
  - Tools used (Cursor, Claude, GitHub Copilot, etc.) with version/model info
  - Example effective prompts (5-10 prompts that generated high-quality code)
  - Acceleration metrics (quantify how AI sped up development, e.g., "Domain models generated in 30 minutes vs. estimated 3 hours manually")
  - AI limitations (areas where AI struggled and required human guidance)
  - Justification (explain how AI maintained architectural quality while accelerating delivery)
- Demo video coordination:
  - Record 5-minute video following Demo_script.md
  - Upload to YouTube/Vimeo
  - Include link in README.md

**DONE CRITERIA**:
1. ✅ Technical writeup complete:
   - Architecture diagram included (visual representation)
   - DDD boundaries explained (Customer, Invoice, Payment aggregates)
   - CQRS implementation explained (commands vs. queries, event flow)
   - Database schema documented (ERD with relationships)
   - Design decisions justified (trade-offs documented)
2. ✅ AI documentation complete:
   - Tools listed with versions/models
   - 5-10 example prompts included (with code snippets showing results)
   - Acceleration metrics quantified (time saved, quality maintained)
   - Limitations documented (where AI struggled)
   - Justification clear (how AI helped without compromising architecture)
3. ✅ Demo video recorded:
   - Follows Demo_script.md (5-minute duration)
   - Shows core InvoiceMe.md flows (Customer → Invoice → Payment)
   - Highlights architecture (DDD/CQRS/VSA)
   - Link included in README.md

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Technical writeup created (architecture diagram, DDD boundaries, CQRS flow, database schema)
  - AI documentation created (tools used, example prompts, acceleration metrics, limitations)
  - Demo video recorded (link provided)
  - Documentation reviewed (all requirements met)
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Technical writeup: `/TECHNICAL_WRITEUP.md`
  - AI documentation: `/AI_TOOL_DOCUMENTATION.md`
  - Demo video: Link in README.md
- **Evidence**:
  - Technical writeup (PDF or Markdown)
  - AI documentation (Markdown)
  - Demo video link (YouTube/Vimeo)

**DO NOT**:
- Exceed 1-2 pages for technical writeup (keep concise)
- Include code snippets longer than 10 lines in technical writeup (reference files instead)
- Skip AI documentation (mandatory deliverable)

---

## 7. Decision Log

**Purpose**: Track all architectural and scope decisions made during planning and execution.

| Decision ID | Date | Question | Decision | Rationale | Impact |
|-------------|------|----------|----------|-----------|--------|
| **DEC-001** | 2025-01-27 | MVP Scope (Q1) | **FULL PRD SCOPE** — Implement all features (core + extended) | User confirmed: "No MVP Scope - implement everything" | ✅ **RESOLVED** — All 35 features (27 core + 8 extended) in scope |
| **DEC-002** | 2025-01-27 | Domain Events Pattern (Q2) | **IN-MEMORY EVENTS** — Spring ApplicationEventPublisher | User confirmed: "in memory events should be fine" | ✅ **RESOLVED** — No event store required |
| **DEC-003** | 2025-01-27 | Performance Measurement (Q3) | **LOCAL + AWS** — Test locally first, then on AWS after deployment | User confirmed: "test locally but then also once deployed on AWS" | ✅ **RESOLVED** — Dual environment testing |
| **DEC-004** | 2025-01-27 | Integration Test Scope (Q4) | **ALL PRD TESTS** — 6 scenarios (including extended features) | User confirmed: "all PRD tests for entire build" | ✅ **RESOLVED** — Full test coverage |
| **DEC-005** | 2025-01-27 | Frontend Framework (Q5) | **RECOMMENDED: Next.js 14.x** | PRD 2 specifies Next.js, production-ready SSR | Low — PRD 2 is clear |
| **DEC-006** | 2025-01-27 | Local Database (Q6) | **RECOMMENDED: PostgreSQL via Docker** | Consistency with production (Supabase) | Low — can switch if needed |
| **DEC-007** | 2025-01-27 | Authentication Scope (Q7) | **FULL AUTH FLOW** — Registration → Approval → Login → Password Reset | Full PRD scope includes user management | ✅ **RESOLVED** — All auth features in scope |
| **DEC-008** | 2025-01-27 | Scheduled Jobs (GAP-007) | **SPRING @SCHEDULED** — Cron expressions, Central Time | User confirmed: "Use spring scheduled (simpler)" | ✅ **RESOLVED** — Daily at midnight CT, monthly on 1st |
| **DEC-009** | 2025-01-27 | API Error Format (GAP-008) | **RFC 7807 PROBLEM DETAILS** — Standard REST error format | User confirmed: "API error use RFC 7807 Problem Details" | ✅ **RESOLVED** — Production-ready error responses |
| **DEC-010** | 2025-01-27 | Pagination Format (GAP-009) | **SPRING DATA JPA PAGE<T>** — Standard Spring Boot format | User confirmed: "Yes, use Spring Data JPA format" | ✅ **RESOLVED** — Standard pagination response |
| **DEC-011** | 2025-01-27 | JWT Refresh (GAP-010) | **NO REFRESH TOKENS** — User re-logs in after 24 hours | User confirmed: "No refresh tokens (simpler)" | ✅ **RESOLVED** — 24-hour expiry, no refresh endpoint |
| **DEC-012** | 2025-01-27 | Timezone (GAP-011) | **CENTRAL TIME (CST/CDT)** — America/Chicago timezone | User confirmed: "Central time" | ✅ **RESOLVED** — Scheduled jobs in Central Time |
| **DEC-013** | 2025-01-27 | Search/Filter Format (GAP-012) | **SIMPLE QUERY PARAMS** — `?status=SENT&customerId=123` | User confirmed: "good with simple query params" | ✅ **RESOLVED** — Standard query param format |
| **DEC-014** | 2025-01-27 | PDF Storage (GAP-013) | **ON-DEMAND + S3 CACHE** — Generate on click, cache in S3 | User confirmed: "good with your recs on PDFs" | ✅ **RESOLVED** — Signed URLs, 1-hour expiry |
| **DEC-015** | 2025-01-27 | Customer Portal Auth (GAP-014) | **AUTO-LINK BY EMAIL** — Match Customer entity email | User confirmed: "good with your rec" | ✅ **RESOLVED** — Auto-link on registration |

**Assumptions**:
- **ASSUMPTION-001**: User wants production-ready code quality (not prototype)
- **ASSUMPTION-002**: 5-7 day timeline is flexible (can extend if needed for quality)
- **ASSUMPTION-003**: AWS deployment preferred (PRD 2 specifies AWS Amplify + Elastic Beanstalk)
- **ASSUMPTION-004**: PostgreSQL via Supabase for production (managed database)

---

## 8. Review Checklist (For Future Iterations)

Use this checklist to validate deliverables before milestone completion:

### DDD Validation
- [ ] Domain aggregates explicitly documented (Customer, Invoice, Payment)
- [ ] Aggregate invariants enforced in code (e.g., `Customer.canBeDeleted()` checks zero balance)
- [ ] Rich domain models with behavior (not anemic data models with only getters/setters)
- [ ] Value objects immutable (Money, Email, InvoiceNumber)
- [ ] Domain events listed in `/backend/docs/events.md` with producers/consumers

### CQRS Validation
- [ ] Commands/Queries separation enforced in code structure (separate handlers)
- [ ] Command handlers modify state and publish events
- [ ] Query handlers return read-only data (no side effects)
- [ ] API endpoints labeled as Commands vs. Queries in OpenAPI spec

### VSA Validation
- [ ] Code organized by feature (vertical slices: `createinvoice/`, `recordpayment/`)
- [ ] Each slice contains: Command/Query, Handler, Validator, Controller, Tests
- [ ] Minimal shared infrastructure (domain base classes, event publisher only)

### Performance Validation
- [ ] API latency <200ms (p95) for CRUD operations
- [ ] Performance evidence attached (latency table with sample sizes, percentiles)
- [ ] Dashboard loads in <2s (if implemented)
- [ ] Database indexes created on foreign keys and frequently queried fields

### Integration Tests Validation
- [ ] Integration tests green (all MVP scenarios passing)
- [ ] Test evidence attached (screenshots/logs)
- [ ] Test plan documents MVP vs. Extended scope clearly

### Demo Validation
- [ ] Demo video follows Demo_script.md (5-minute duration)
- [ ] Core InvoiceMe.md flows demonstrated (Customer → Invoice → Payment)
- [ ] Architecture highlighted (DDD/CQRS/VSA)
- [ ] Video link included in README.md

### Documentation Validation
- [ ] TECHNICAL_WRITEUP.md includes architecture diagram, DDD boundaries, CQRS flow, database schema
- [ ] AI_TOOL_DOCUMENTATION.md includes tools used, example prompts, acceleration metrics, limitations
- [ ] All deliverables present (code repo, demo video, writeups)

---

## 9. Next Steps

1. ✅ **COMPLETED**: User answered all blocking questions (Q1-Q4).
2. ✅ **COMPLETED**: Decision Log (Section 7) updated with confirmed decisions.
3. **NEXT**: Review setup instructions (Section 10) and create required accounts/credentials.
4. **NEXT**: Issue sub-agent prompts (Section 6) to Backend, Frontend, Data/DB, QA, DevOps, Docs agents.
5. **DURING EXECUTION**: Monitor milestone progress (M1-M5), review deliverables against checklist (Section 8).

---

## 10. Setup Instructions & Account Creation

### 10.1 User Approval Feature Clarification

**What is "User Approval"?**

User Approval is the registration → approval workflow for new user accounts:

1. **Registration**: New user submits registration form (email, password, full name, desired role: SysAdmin/Accountant/Sales/Customer)
2. **Pending Status**: Account created with `Status = PENDING` (cannot login yet)
3. **SysAdmin Notification**: SysAdmin receives notification of pending registration
4. **Approval/Rejection**: SysAdmin reviews and approves/rejects the account
5. **Email Notification**: User receives email confirming approval (or rejection)

**Why it's Extended**: InvoiceMe.md only requires "basic authentication" (login screen). User Approval adds the registration → approval flow, which is a common SaaS security practice but not explicitly required.

**Implementation**: 
- Endpoints: `POST /auth/register`, `GET /users/pending`, `PATCH /users/{id}/approve`, `PATCH /users/{id}/reject`
- UI: Registration page, Admin "Pending Users" list with approve/reject buttons
- Email: Account approval/rejection notifications via AWS SES

---

### 10.2 Tech Stack Setup Instructions

**Purpose**: Create accounts and obtain credentials for all required services before development begins.

#### A) AWS Account Setup (Required for Deployment)

**Services Needed**:
1. **AWS Amplify** (Frontend hosting)
2. **AWS Elastic Beanstalk** (Backend hosting)
3. **AWS SES** (Email notifications)
4. **AWS S3** (PDF storage - optional for MVP, can generate on-demand)
5. **AWS CloudWatch** (Logging & monitoring)

**Steps**:
1. **Create AWS Account** (if you don't have one):
   - Go to https://aws.amazon.com/
   - Sign up for free tier (12 months free for eligible services)
   - Provide credit card (won't be charged unless you exceed free tier)

2. **Create IAM User** (for programmatic access):
   - AWS Console → IAM → Users → Create User
   - User name: `invoiceme-deploy`
   - Access type: Programmatic access (API keys)
   - Permissions: Attach policies:
     - `AWSElasticBeanstalkFullAccess`
     - `AWSAmplifyFullAccess`
     - `AmazonSESFullAccess`
     - `AmazonS3FullAccess`
     - `CloudWatchFullAccess`
   - Save Access Key ID and Secret Access Key (you'll need these for CI/CD)

3. **Verify SES Email Domain** (for sending emails):
   - AWS Console → SES → Verified identities → Create identity
   - Choose "Domain" or "Email address"
   - For testing: Verify your email address (check inbox for verification link)
   - For production: Verify domain (add DNS records)
   - **Note**: SES starts in "Sandbox" mode (can only send to verified emails). Request production access if needed.

4. **Create S3 Bucket** (for PDF storage - optional):
   - AWS Console → S3 → Create bucket
   - Bucket name: `invoiceme-pdfs-{your-unique-id}` (must be globally unique)
   - Region: `us-east-1` (or your preferred region)
   - Block public access: Uncheck (or configure bucket policy for signed URLs)
   - Save bucket name

**Environment Variables to Save**:
```
AWS_ACCESS_KEY_ID=<from IAM user>
AWS_SECRET_ACCESS_KEY=<from IAM user>
AWS_REGION=us-east-1
AWS_SES_FROM_EMAIL=noreply@yourdomain.com (or verified email)
AWS_S3_BUCKET_NAME=invoiceme-pdfs-{your-unique-id}
```

---

#### B) Supabase Account Setup (Database)

**Steps**:
1. **Create Supabase Account**:
   - Go to https://supabase.com/
   - Sign up (free tier available)
   - Create new project

2. **Create Project**:
   - Project name: `invoiceme`
   - Database password: Generate strong password (save it!)
   - Region: Choose closest to you (or `us-east-1` for AWS compatibility)
   - Wait for project provisioning (~2 minutes)

3. **Get Connection String**:
   - Project Settings → Database → Connection string
   - Copy "Connection string" (URI format: `postgresql://postgres:[password]@[host]:5432/postgres`)
   - Save as `DATABASE_URL` environment variable

4. **Enable Connection Pooling** (optional, for production):
   - Project Settings → Database → Connection pooling
   - Use "Session" mode for development, "Transaction" mode for production
   - Save pooled connection string as `DATABASE_POOL_URL`

**Environment Variables to Save**:
```
DATABASE_URL=postgresql://postgres:[password]@[host]:5432/postgres
DATABASE_POOL_URL=postgresql://postgres:[password]@[host]:6543/postgres (if using pooling)
```

---

#### C) GitHub Account Setup (Code Repository & CI/CD)

**Steps**:
1. **Create GitHub Account** (if you don't have one):
   - Go to https://github.com/
   - Sign up for free account

2. **Create Repository**:
   - New repository → Name: `InvoiceMe`
   - Visibility: Private (or Public if you want)
   - Initialize: Don't add README (we'll create one)

3. **Set Up GitHub Actions Secrets** (for CI/CD):
   - Repository → Settings → Secrets and variables → Actions
   - Add secrets:
     - `AWS_ACCESS_KEY_ID` (from AWS IAM user)
     - `AWS_SECRET_ACCESS_KEY` (from AWS IAM user)
     - `DATABASE_URL` (from Supabase)
     - `JWT_SECRET` (generate: `openssl rand -base64 32`)
     - `AWS_SES_FROM_EMAIL` (from AWS SES)
     - `AWS_S3_BUCKET_NAME` (from AWS S3)

**Environment Variables to Save**:
```
JWT_SECRET=<generate with: openssl rand -base64 32>
```

---

#### D) Local Development Setup

**Prerequisites**:
- Java 17 (LTS) installed
- Node.js 18+ installed
- Docker Desktop installed (for PostgreSQL)
- Git installed

**Steps**:
1. **Install Java 17**:
   - macOS: `brew install openjdk@17`
   - Windows: Download from https://adoptium.net/
   - Verify: `java -version` (should show 17.x)

2. **Install Node.js 18+**:
   - macOS: `brew install node@18`
   - Windows: Download from https://nodejs.org/
   - Verify: `node -v` (should show 18.x or higher)

3. **Install Docker Desktop**:
   - macOS: Download from https://www.docker.com/products/docker-desktop
   - Windows: Download from https://www.docker.com/products/docker-desktop
   - Verify: `docker --version`

4. **Clone Repository** (after GitHub repo created):
   ```bash
   git clone https://github.com/yourusername/InvoiceMe.git
   cd InvoiceMe
   ```

5. **Set Up Local Environment Variables**:
   - Create `.env` file in project root:
     ```bash
     # Database
     DATABASE_URL=postgresql://postgres:postgres@localhost:5432/invoiceme
     
     # JWT
     JWT_SECRET=<generate with: openssl rand -base64 32>
     
     # AWS (use test values for local dev)
     AWS_REGION=us-east-1
     AWS_SES_FROM_EMAIL=test@example.com
     AWS_S3_BUCKET_NAME=invoiceme-pdfs-local
     
     # Frontend API URL
     NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
     ```

---

### 10.3 Account Creation Checklist

Before starting development, ensure you have:

- [ ] **AWS Account** created
- [ ] **AWS IAM User** created with access keys saved
- [ ] **AWS SES** email verified (or domain verified)
- [ ] **AWS S3 Bucket** created (optional)
- [ ] **Supabase Account** created
- [ ] **Supabase Project** created with connection string saved
- [ ] **GitHub Account** created
- [ ] **GitHub Repository** created
- [ ] **GitHub Actions Secrets** configured (AWS keys, DATABASE_URL, JWT_SECRET)
- [ ] **Local Environment** set up (Java 17, Node.js 18+, Docker Desktop)
- [ ] **Local .env file** created with all environment variables

---

### 10.4 Quick Start Command Reference

**Start Local PostgreSQL** (Docker):
```bash
docker-compose up -d postgres
```

**Run Backend** (Spring Boot):
```bash
cd backend
./mvnw spring-boot:run
```

**Run Frontend** (Next.js):
```bash
cd frontend
npm install
npm run dev
```

**Run Integration Tests**:
```bash
cd backend
./mvnw test
```

---

## 11. Minor Gaps Addressed

### Gap: Integration Test Evidence Collection

**Issue**: InvoiceMe.md expects "integration test evidence" but PRD doesn't specify how results are collected/documentated.

**Resolution**: QA Agent will deliver:
- `/qa/results/summary.md` — Test results report with pass/fail table
- `/qa/results/performance.md` — Performance test results with latency tables
- Screenshots/logs of test executions (stored in `/qa/results/screenshots/` or `/qa/results/logs/`)
- CI/CD logs (GitHub Actions) showing test execution

**Updated Requirement**: QA Agent prompt (Section 6.D) now explicitly requires evidence capture (screenshots, logs, timings).

---

### Gap: Domain Events Mapping

**Issue**: Domain events mentioned but not explicitly mapped to entities.

**Resolution**: Backend Agent will deliver `/backend/docs/events.md` with:
- **PaymentRecordedEvent** → Published by `Payment.record()`, consumed by email listener, audit log listener
- **InvoiceSentEvent** → Published by `Invoice.markAsSent()`, consumed by email listener, dashboard cache listener
- **InvoiceFullyPaidEvent** → Published by `Invoice.recordPayment()` when balance = 0, consumed by notification listener
- **LateFeeAppliedEvent** → Published by scheduled job, consumed by email listener
- **RecurringInvoiceGeneratedEvent** → Published by scheduled job, consumed by email listener
- **RefundIssuedEvent** → Published by `Refund.issue()`, consumed by email listener

**Updated Requirement**: Backend Agent prompt (Section 6.A) now requires explicit event mapping in `/backend/docs/events.md`.

---

### Gap: Vertical Slice Architecture Folder Structure

**Issue**: VSA concept described but not shown in folder-level structure.

**Resolution**: PRD 2 Section 3.1 already includes folder structure, but Backend Agent will ensure:
```
src/main/java/com/invoiceme/
├── customers/
│   ├── createcustomer/          ← Vertical slice
│   │   ├── CreateCustomerCommand.java
│   │   ├── CreateCustomerHandler.java
│   │   ├── CreateCustomerValidator.java
│   │   └── CreateCustomerController.java
│   ├── updatecustomer/          ← Vertical slice
│   ├── getcustomer/             ← Vertical slice (query)
│   └── listcustomers/           ← Vertical slice (query)
```

**Updated Requirement**: Backend Agent prompt (Section 6.A) now explicitly requires vertical slice structure.

---

### Gap: Cloud Deployment Clarification

**Issue**: AWS/Azure mentioned but minimal deployment requirements omitted.

**Resolution**: DevOps Agent will deliver:
- **Backend**: Spring Boot JAR deployed to AWS Elastic Beanstalk (Java platform, Java 17, single instance for MVP)
- **Frontend**: Next.js build deployed to AWS Amplify (automatic builds from GitHub)
- **Database**: Supabase PostgreSQL (managed, no deployment needed)
- **Containerization**: Optional Dockerfile for backend (not required for Elastic Beanstalk, but good practice)

**Updated Requirement**: DevOps Agent prompt (Section 6.E) now specifies AWS Elastic Beanstalk + Amplify deployment targets.

---

### Gap: Line Item Rounding Logic

**Issue**: InvoiceMe.md expects robust balance calculations and rounding consistency.

**Resolution**: Backend Agent will implement:
- **Rounding Strategy**: Banker's rounding (round half to even) to 2 decimal places for all monetary calculations
- **Money Value Object**: Immutable `Money` class with `BigDecimal` for precision, `setScale(2, RoundingMode.HALF_UP)` for display
- **Line Item Calculation**: `LineTotal = (Base - Discount) * (1 + TaxRate)` rounded to 2 decimals
- **Invoice Totals**: Sum of line items rounded to 2 decimals
- **Balance Calculation**: `BalanceDue = TotalAmount - AmountPaid` (both rounded to 2 decimals)

**Updated Requirement**: Backend Agent prompt (Section 6.A) now requires `Money` value object with explicit rounding strategy documented in `/backend/docs/domain-aggregates.md`.

---

**End of Orchestrator Output Package**

**Status**: ✅ **FULLY APPROVED** — All blocking questions answered, all critical decisions resolved. Ready to proceed with execution.

**Decisions Summary**:
- ✅ Full PRD scope (35 features)
- ✅ In-memory domain events
- ✅ Dual environment testing (local + AWS)
- ✅ All 6 PRD test scenarios
- ✅ Spring @Scheduled (Central Time)
- ✅ RFC 7807 Problem Details
- ✅ Spring Data JPA pagination
- ✅ No refresh tokens
- ✅ Central Time timezone
- ✅ Simple query params
- ✅ On-demand PDF + S3 cache
- ✅ Auto-link Customer by email

**Note**: See `REMAINING_QUESTIONS.md` for complete decision log.

**Contact**: Orchestrator ready to proceed once questions resolved.

