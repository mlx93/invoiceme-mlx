# Backend Agent M2 - Remaining Work Prioritized

**Date**: 2025-01-27  
**Status**: 🚧 **IN PROGRESS** - Domain Layer ~70% Complete  
**Current State**: Domain aggregates, value objects, and events complete. Need infrastructure and application layers.

---

## ✅ Completed (Domain Layer Foundation)

- ✅ Spring Boot project structure (pom.xml, application.yml)
- ✅ All 4 value objects (Money, Email, InvoiceNumber, Address)
- ✅ All 10 domain events
- ✅ 3 of 4 aggregates (Customer, Invoice, Payment) with rich behavior
- ✅ AggregateRoot base class with domain event collection
- ✅ DomainEventPublisher wrapper

---

## 🎯 Prioritized Remaining Work

### Phase 1: Infrastructure Foundation (Enables Application Layer)
**Priority**: 🔴 **CRITICAL** - Must complete before vertical slices

1. **JPA Repositories** (Required for all vertical slices)
   - `CustomerRepository` extends `JpaRepository<Customer, UUID>`
   - `InvoiceRepository` extends `JpaRepository<Invoice, UUID>`
   - `PaymentRepository` extends `JpaRepository<Payment, UUID>`
   - `RecurringInvoiceTemplateRepository` extends `JpaRepository<RecurringInvoiceTemplate, UUID>`
   - Custom query methods for common queries (findByEmail, findByStatus, etc.)

2. **JPA Entity Mapping** (Map domain aggregates to database tables)
   - Add `@Entity`, `@Table` annotations to aggregates
   - Map value objects with `@Embedded` (Money, Email, InvoiceNumber, Address)
   - Map relationships with `@OneToMany`, `@ManyToOne`
   - Configure `@Version` for optimistic locking (Invoice)

3. **Complete RecurringInvoiceTemplate Aggregate** (Last domain aggregate)
   - Behavior methods: `generateInvoice()`, `pause()`, `resume()`, `complete()`, `calculateNextDate()`
   - Child entity: `TemplateLineItem`
   - Domain event: `RecurringInvoiceGeneratedEvent`

---

### Phase 2: Core Vertical Slices (Customer CRUD First)
**Priority**: 🔴 **CRITICAL** - Enables E2E testing

**Start with Customer CRUD** (simplest, no dependencies):

1. **`customers/createcustomer/`**
   - `CreateCustomerCommand` (DTO)
   - `CreateCustomerHandler` (@Service, @Transactional)
   - `CreateCustomerValidator` (validation logic)
   - `CreateCustomerController` (@RestController, POST /api/v1/customers)
   - MapStruct mapper: `CreateCustomerCommand` → `Customer` entity

2. **`customers/getcustomer/`**
   - `GetCustomerQuery` (DTO)
   - `GetCustomerHandler` (@Service)
   - `GetCustomerController` (@RestController, GET /api/v1/customers/{id})
   - MapStruct mapper: `Customer` entity → `CustomerDetailResponse` DTO

3. **`customers/listcustomers/`**
   - `ListCustomersQuery` (DTO with filters)
   - `ListCustomersHandler` (@Service)
   - `ListCustomersController` (@RestController, GET /api/v1/customers)
   - Returns `Page<CustomerSummaryResponse>`

4. **`customers/updatecustomer/`**
   - `UpdateCustomerCommand` (DTO)
   - `UpdateCustomerHandler` (@Service, @Transactional)
   - `UpdateCustomerValidator`
   - `UpdateCustomerController` (@RestController, PUT /api/v1/customers/{id})

5. **`customers/deletecustomer/`**
   - `DeleteCustomerCommand` (DTO)
   - `DeleteCustomerHandler` (@Service, @Transactional)
   - `DeleteCustomerController` (@RestController, DELETE /api/v1/customers/{id})

**Why Start Here**: Customer CRUD is independent, allows testing domain events, validates infrastructure setup.

---

### Phase 3: Invoice Vertical Slices (Depends on Customer)
**Priority**: 🟡 **HIGH** - Core business logic

1. **`invoices/createinvoice/`** - Create draft invoice
2. **`invoices/getinvoice/`** - Get invoice detail
3. **`invoices/listinvoices/`** - List invoices with filters
4. **`invoices/updateinvoice/`** - Update draft invoice
5. **`invoices/markassent/`** - Mark as sent (triggers InvoiceSentEvent)
6. **`invoices/cancelinvoice/`** - Cancel invoice

---

### Phase 4: Payment Vertical Slices (Depends on Invoice)
**Priority**: 🟡 **HIGH** - Core business logic

1. **`payments/recordpayment/`** - Record payment (triggers PaymentRecordedEvent)
2. **`payments/getpayment/`** - Get payment detail
3. **`payments/listpayments/`** - List payments with filters

**Critical**: Payment.record() handles overpayment → credit logic

---

### Phase 5: Infrastructure Services (Enables Event Listeners)
**Priority**: 🟡 **HIGH** - Required for event listeners

1. **Email Service**
   - `EmailService` interface
   - `AwsSesEmailService` implementation
   - Methods: `sendInvoiceSentNotification()`, `sendPaymentReceivedNotification()`, etc.

2. **PDF Service** (Can be simplified initially)
   - `PdfService` interface
   - `iTextPdfService` implementation (or mock for initial testing)
   - Method: `generateInvoicePdf(Invoice)`

---

### Phase 6: Event Listeners (Depends on Services)
**Priority**: 🟢 **MEDIUM** - Side effects, not blocking

1. **`InvoiceSentEmailListener`** - `@TransactionalEventListener(AFTER_COMMIT)`
2. **`PaymentRecordedEmailListener`** - `@TransactionalEventListener(AFTER_COMMIT)`
3. **`InvoiceFullyPaidEmailListener`** - `@TransactionalEventListener(AFTER_COMMIT)`
4. **`ActivityFeedListener`** - Log all events to activity_feed table
5. **`DashboardCacheInvalidationListener`** - Invalidate cache on events

**Note**: Can implement listeners incrementally as services become available.

---

### Phase 7: Security & Authentication
**Priority**: 🟢 **MEDIUM** - Can be implemented in parallel or after core CRUD

1. **User Entity** (JPA entity mapping)
2. **JWT Token Generation** (`JwtTokenService`)
3. **Spring Security Configuration** (`SecurityConfig` with JWT filter)
4. **Authentication Endpoints** (`/auth/login`, `/auth/register`)
5. **RBAC Enforcement** (`@PreAuthorize` annotations on controllers)

**Note**: Can test core CRUD without authentication initially, add security later.

---

### Phase 8: Error Handling
**Priority**: 🟢 **MEDIUM** - Can be added incrementally

1. **Global Exception Handler** (`@ControllerAdvice`)
2. **RFC 7807 Problem Details** (`ProblemDetail` responses)
3. **Validation Error Handling** (field-level errors)

---

### Phase 9: Scheduled Jobs
**Priority**: 🟢 **MEDIUM** - Extended features

1. **`RecurringInvoiceScheduledJob`** - `@Scheduled(cron = "0 0 * * *", zone = "America/Chicago")`
2. **`LateFeeScheduledJob`** - `@Scheduled(cron = "0 0 1 * *", zone = "America/Chicago")`

**Note**: Can be tested manually initially (trigger via endpoint or test).

---

### Phase 10: Integration Tests
**Priority**: 🔴 **CRITICAL** - Validate E2E flows

1. **`CustomerPaymentFlowTest`** - E2E flow (create customer → create invoice → mark sent → record payment → verify paid)
2. **`PartialPaymentTest`** - Partial payment flow
3. **`OverpaymentCreditTest`** - Overpayment → credit flow

**Note**: Write tests incrementally as features are implemented.

---

## 📋 Recommended Implementation Order

### Week 1: Core Foundation
1. ✅ Complete RecurringInvoiceTemplate aggregate
2. ✅ Create JPA repositories
3. ✅ Map JPA entities (add @Entity, @Table, @Embedded annotations)
4. ✅ Implement Customer CRUD vertical slices (5 slices)
5. ✅ Test Customer CRUD (manual testing via Postman/curl)

### Week 2: Core Business Logic
6. ✅ Implement Invoice CRUD vertical slices (6 slices)
7. ✅ Implement Payment vertical slices (3 slices)
8. ✅ Test Customer → Invoice → Payment E2E flow (manual testing)

### Week 3: Infrastructure & Testing
9. ✅ Implement Email Service (AWS SES)
10. ✅ Implement PDF Service (iText 7)
11. ✅ Implement Event Listeners (6 listeners)
12. ✅ Write Integration Tests (3 tests)
13. ✅ Implement JWT Authentication
14. ✅ Implement Global Exception Handler

### Week 4: Extended Features & Polish
15. ✅ Implement Scheduled Jobs (2 jobs)
16. ✅ Implement Extended Features (Refunds, Recurring Invoices, Dashboard)
17. ✅ Performance Testing (<200ms latency)
18. ✅ Final Integration Testing

---

## 🚀 Quick Start: Minimum Viable Implementation

**To get E2E flow working quickly:**

1. **Infrastructure** (2-3 hours):
   - JPA Repositories (4 repositories)
   - JPA Entity Mapping (add annotations to existing aggregates)
   - Complete RecurringInvoiceTemplate aggregate

2. **Customer CRUD** (3-4 hours):
   - Create, Get, List, Update, Delete vertical slices
   - Test via Postman/curl

3. **Invoice CRUD** (4-5 hours):
   - Create, Get, List, Update, MarkAsSent vertical slices
   - Test via Postman/curl

4. **Payment CRUD** (2-3 hours):
   - RecordPayment vertical slice
   - Test E2E flow: Customer → Invoice → Payment

5. **Event Listeners** (2-3 hours):
   - Basic email listener (can mock AWS SES initially)
   - Activity feed listener

**Total**: ~15-20 hours for working E2E flow (without authentication, can add later)

---

## 📝 Implementation Tips

### Vertical Slice Structure
```
customers/
  createcustomer/
    CreateCustomerCommand.java          (DTO)
    CreateCustomerHandler.java          (@Service, @Transactional)
    CreateCustomerValidator.java        (validation)
    CreateCustomerController.java       (@RestController)
    CreateCustomerMapper.java           (MapStruct)
```

### Command Handler Pattern
```java
@Service
@Transactional
public class CreateCustomerHandler {
    private final CustomerRepository customerRepository;
    private final DomainEventPublisher eventPublisher;
    
    public UUID handle(CreateCustomerCommand command) {
        // Validate
        // Create aggregate
        // Save aggregate
        // Publish events
        // Return ID
    }
}
```

### Query Handler Pattern
```java
@Service
public class GetCustomerHandler {
    private final CustomerRepository customerRepository;
    private final CustomerMapper mapper;
    
    public CustomerDetailResponse handle(GetCustomerQuery query) {
        // Load aggregate
        // Map to DTO
        // Return DTO
    }
}
```

### Event Listener Pattern
```java
@Component
public class InvoiceSentEmailListener {
    private final EmailService emailService;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(InvoiceSentEvent event) {
        // Send email
        // Log to activity feed
    }
}
```

---

## ✅ Success Criteria for M2

M2 is complete when:
- ✅ All RESTful APIs implemented and working (matching OpenAPI spec)
- ✅ Customer → Invoice → Payment E2E flow working
- ✅ Integration tests passing (3 tests minimum)
- ✅ Domain events firing correctly (email listeners working)
- ✅ API latency <200ms (p95) for CRUD operations

---

**Status**: Ready to continue implementation  
**Next Focus**: Infrastructure Foundation (JPA Repositories + Entity Mapping)

