# Domain Events Documentation

**Last Updated**: 2025-01-27  
**Status**: M1 Phase - Domain & API Freeze  
**Architecture**: Domain-Driven Design (DDD) with Domain Events

---

## Overview

Domain events represent business-significant occurrences in the InvoiceMe system. Events are published by aggregate roots after successful transaction commit and consumed by event listeners for side effects (email notifications, audit logging, cache invalidation).

**Key Principles**:
- **Event-Driven Architecture**: Aggregates publish events, listeners handle side effects
- **Transactional Consistency**: Events published after transaction commit (`@TransactionalEventListener(AFTER_COMMIT)`)
- **Decoupling**: Aggregates don't know about listeners (loose coupling)
- **Eventual Consistency**: Cross-aggregate consistency handled via events

---

## Event Publishing Pattern

### Implementation Pattern

1. **Event Collection**: Aggregate methods collect events in transient list during transaction
2. **Transaction Commit**: After successful commit, events are published
3. **Event Publishing**: Spring `ApplicationEventPublisher` publishes events
4. **Event Listening**: Listeners annotated with `@TransactionalEventListener(AFTER_COMMIT)` handle events

### Code Example

```java
// Aggregate method collects event
public void markAsSent() {
    // Business logic...
    this.status = InvoiceStatus.SENT;
    this.sentDate = Instant.now();
    
    // Collect event (not published yet)
    addDomainEvent(new InvoiceSentEvent(this.id, this.invoiceNumber, ...));
}

// Command handler publishes events after transaction commit
@Transactional
public void handle(MarkAsSentCommand command) {
    Invoice invoice = invoiceRepository.findById(command.getInvoiceId());
    invoice.markAsSent();
    invoiceRepository.save(invoice);
    
    // Publish events after commit
    eventPublisher.publishEvents(invoice);
}

// Event listener handles event asynchronously
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleInvoiceSent(InvoiceSentEvent event) {
    emailService.sendInvoiceEmail(event.getInvoiceId());
    activityFeedService.logEvent(event);
    dashboardCacheService.invalidateCache();
}
```

### Transactional Consistency

**Pattern**: `@TransactionalEventListener(AFTER_COMMIT)`

**Rationale**:
- Events published only after successful transaction commit
- If transaction rolls back, events are not published
- Event listeners execute asynchronously (don't block transaction)
- If listener fails, transaction is not rolled back (side effects are independent)

**Spring Configuration**:
```java
@Configuration
@EnableAsync
public class EventConfiguration {
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return eventMulticaster;
    }
}
```

---

## Domain Events Catalog

### 1. PaymentRecordedEvent

**Published By**: `Payment.record()` static factory method  
**Aggregate**: Payment  
**When**: Payment is successfully recorded against an invoice

**Payload**:
```java
{
    paymentId: UUID,
    invoiceId: UUID,
    invoiceNumber: String,
    customerId: UUID,
    customerName: String,
    amount: Money,
    paymentMethod: PaymentMethod,
    paymentDate: LocalDate,
    remainingBalance: Money,
    overpaymentAmount: Money (if payment > invoice balance)
}
```

**Consumers**:
1. **Email Listener** (`PaymentRecordedEmailListener`)
   - Sends payment confirmation email to customer
   - Sends payment notification email to SysAdmin/Accountant
   - Email includes: Payment amount, invoice number, remaining balance

2. **Dashboard Cache Listener** (`DashboardCacheInvalidationListener`)
   - Invalidates dashboard metrics cache
   - Triggers cache refresh for revenue metrics

3. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs payment event to activity feed
   - Records: Payment ID, invoice ID, amount, payment date

**Business Rules**:
- If payment > invoice balance, excess goes to customer credit (via `CreditAppliedEvent`)
- If payment = invoice balance, invoice status changes to PAID (via `InvoiceFullyPaidEvent`)

**Example**:
```java
// Payment recorded
Payment payment = Payment.record(invoice, customer, Money.of(1000.00), ...);
// → PaymentRecordedEvent published

// Event listeners execute:
// 1. Email sent to customer
// 2. Dashboard cache invalidated
// 3. Activity feed updated
```

---

### 2. InvoiceSentEvent

**Published By**: `Invoice.markAsSent()` method  
**Aggregate**: Invoice  
**When**: Invoice is marked as sent (DRAFT → SENT)

**Payload**:
```java
{
    invoiceId: UUID,
    invoiceNumber: String,
    customerId: UUID,
    customerName: String,
    customerEmail: String,
    totalAmount: Money,
    dueDate: LocalDate,
    issueDate: LocalDate,
    lineItemsCount: Integer,
    creditApplied: Money (if customer credit was auto-applied)
}
```

**Consumers**:
1. **Email Listener** (`InvoiceSentEmailListener`)
   - Sends invoice email to customer
   - Email includes: Invoice PDF attachment, payment link, due date
   - Subject: "Invoice [Number] from FloodShield Restoration"

2. **Dashboard Cache Listener** (`DashboardCacheInvalidationListener`)
   - Invalidates dashboard metrics cache
   - Triggers cache refresh for invoice status metrics

3. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs invoice sent event to activity feed
   - Records: Invoice ID, invoice number, customer ID, sent date

**Business Rules**:
- Customer credit is auto-applied if available (creates discount line item)
- Invoice PDF is generated and attached to email
- Invoice becomes visible in customer portal

**Example**:
```java
// Invoice marked as sent
invoice.markAsSent();
// → InvoiceSentEvent published

// Event listeners execute:
// 1. Email sent to customer with PDF
// 2. Dashboard cache invalidated
// 3. Activity feed updated
```

---

### 3. InvoiceFullyPaidEvent

**Published By**: `Invoice.recordPayment()` method (when balance = $0)  
**Aggregate**: Invoice  
**When**: Invoice balance reaches $0.00 (fully paid)

**Payload**:
```java
{
    invoiceId: UUID,
    invoiceNumber: String,
    customerId: UUID,
    customerName: String,
    totalAmount: Money,
    paidDate: Instant,
    paymentCount: Integer
}
```

**Consumers**:
1. **Email Listener** (`InvoiceFullyPaidEmailListener`)
   - Sends payment confirmation email to customer
   - Sends completion notification to SysAdmin/Accountant
   - Email includes: Invoice number, total amount, paid date

2. **Notification Listener** (`NotificationListener`)
   - Sends push notification (if implemented)
   - Updates customer portal dashboard

3. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs invoice paid event to activity feed
   - Records: Invoice ID, invoice number, paid date

**Business Rules**:
- Invoice status changes to PAID (enforced by domain logic)
- Invoice becomes read-only (cannot be edited or deleted)

**Example**:
```java
// Payment recorded, balance reaches $0
invoice.recordPayment(Money.of(1000.00));
// → InvoiceFullyPaidEvent published (if balance = 0)

// Event listeners execute:
// 1. Payment confirmation email sent
// 2. Notification sent
// 3. Activity feed updated
```

---

### 4. LateFeeAppliedEvent

**Published By**: Scheduled job (`LateFeeScheduledJob`)  
**Aggregate**: Invoice  
**When**: Late fee is applied to overdue invoice (monthly scheduled job)

**Payload**:
```java
{
    invoiceId: UUID,
    invoiceNumber: String,
    customerId: UUID,
    customerName: String,
    customerEmail: String,
    lateFeeAmount: Money,
    newBalance: Money,
    daysOverdue: Integer,
    month: String (e.g., "January 2025")
}
```

**Consumers**:
1. **Email Listener** (`LateFeeEmailListener`)
   - Sends overdue reminder email to customer
   - Email includes: Late fee amount, new balance, days overdue
   - Subject: "Payment Reminder - Invoice [Number] Overdue"
   - Only sends email on first late fee (not on subsequent months)

2. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs late fee event to activity feed
   - Records: Invoice ID, late fee amount, month

**Business Rules**:
- Late fee = $125.00 per month per invoice
- Capped at 3 months ($375 max per invoice)
- Email sent only on first late fee application

**Scheduled Job**:
```java
@Scheduled(cron = "0 0 1 * *", zone = "America/Chicago")
public void applyLateFees() {
    // Runs 1st of each month at midnight Central Time
    // Checks overdue invoices, applies late fees
    // → LateFeeAppliedEvent published for each invoice
}
```

**Example**:
```java
// Scheduled job runs monthly
lateFeeScheduledJob.applyLateFees();
// → LateFeeAppliedEvent published for each overdue invoice

// Event listeners execute:
// 1. Overdue reminder email sent (first time only)
// 2. Activity feed updated
```

---

### 5. RecurringInvoiceGeneratedEvent

**Published By**: Scheduled job (`RecurringInvoiceScheduledJob`)  
**Aggregate**: RecurringInvoiceTemplate  
**When**: Invoice is generated from recurring template (daily scheduled job)

**Payload**:
```java
{
    templateId: UUID,
    templateName: String,
    invoiceId: UUID,
    invoiceNumber: String,
    customerId: UUID,
    customerName: String,
    customerEmail: String,
    nextInvoiceDate: LocalDate,
    autoSend: Boolean,
    generatedDate: LocalDate
}
```

**Consumers**:
1. **Email Listener** (`RecurringInvoiceEmailListener`)
   - Sends invoice email to customer (if autoSend = true)
   - Sends notification email to SysAdmin (daily summary)
   - Email includes: Invoice PDF attachment, payment link

2. **Dashboard Cache Listener** (`DashboardCacheInvalidationListener`)
   - Invalidates dashboard metrics cache
   - Triggers cache refresh for revenue metrics

3. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs recurring invoice generation event to activity feed
   - Records: Template ID, invoice ID, invoice number

**Business Rules**:
- Invoice generated with status DRAFT (if autoSend = false) or SENT (if autoSend = true)
- Template's nextInvoiceDate updated by frequency period
- If endDate reached, template status changes to COMPLETED

**Scheduled Job**:
```java
@Scheduled(cron = "0 0 * * *", zone = "America/Chicago")
public void generateRecurringInvoices() {
    // Runs daily at midnight Central Time
    // Checks active templates where nextInvoiceDate <= current date
    // Generates invoices from templates
    // → RecurringInvoiceGeneratedEvent published for each invoice
}
```

**Example**:
```java
// Scheduled job runs daily
recurringInvoiceScheduledJob.generateRecurringInvoices();
// → RecurringInvoiceGeneratedEvent published for each generated invoice

// Event listeners execute:
// 1. Invoice email sent (if autoSend = true)
// 2. Dashboard cache invalidated
// 3. Activity feed updated
```

---

### 6. RefundIssuedEvent

**Published By**: `Refund.issue()` method  
**Aggregate**: Refund  
**When**: Refund is issued on a paid invoice

**Payload**:
```java
{
    refundId: UUID,
    invoiceId: UUID,
    invoiceNumber: String,
    customerId: UUID,
    customerName: String,
    customerEmail: String,
    refundAmount: Money,
    reason: String,
    applyAsCredit: Boolean,
    newInvoiceBalance: Money,
    invoiceStatus: InvoiceStatus (PAID → SENT if partial refund)
}
```

**Consumers**:
1. **Email Listener** (`RefundEmailListener`)
   - Sends refund notification email to customer
   - Email includes: Refund amount, reason, invoice number
   - Subject: "Refund Issued - Invoice [Number]"

2. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs refund event to activity feed
   - Records: Refund ID, invoice ID, refund amount, reason

**Business Rules**:
- If partial refund, invoice status changes from PAID → SENT
- If applyAsCredit = true, refund added to customer credit balance (via `CreditAppliedEvent`)
- Refund cannot exceed total amount paid

**Example**:
```java
// Refund issued
Refund refund = Refund.issue(invoice, Money.of(300.00), "Service dispute", false);
// → RefundIssuedEvent published

// Event listeners execute:
// 1. Refund notification email sent
// 2. Activity feed updated
```

---

### 7. CreditAppliedEvent

**Published By**: `Customer.applyCredit()` method  
**Aggregate**: Customer  
**When**: Credit is added to customer's balance (from overpayment or manual credit)

**Payload**:
```java
{
    customerId: UUID,
    customerName: String,
    amount: Money,
    previousBalance: Money,
    newBalance: Money,
    source: String (e.g., "Overpayment", "Manual Credit", "Refund")
}
```

**Consumers**:
1. **Audit Log Listener** (`AuditLogListener`)
   - Logs credit application to audit log
   - Records: Customer ID, amount, source, timestamp

2. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs credit application event to activity feed
   - Records: Customer ID, amount, new balance

**Business Rules**:
- Credit balance cannot go negative (enforced by invariant)
- Credit can be applied from overpayment, manual credit, or refund

**Example**:
```java
// Credit applied to customer
customer.applyCredit(Money.of(100.00));
// → CreditAppliedEvent published

// Event listeners execute:
// 1. Audit log updated
// 2. Activity feed updated
```

---

### 8. CreditDeductedEvent

**Published By**: `Customer.deductCredit()` method  
**Aggregate**: Customer  
**When**: Credit is deducted from customer's balance (when credit applied to invoice)

**Payload**:
```java
{
    customerId: UUID,
    customerName: String,
    amount: Money,
    previousBalance: Money,
    newBalance: Money,
    invoiceId: UUID,
    invoiceNumber: String
}
```

**Consumers**:
1. **Audit Log Listener** (`AuditLogListener`)
   - Logs credit deduction to audit log
   - Records: Customer ID, amount, invoice ID, timestamp

2. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs credit deduction event to activity feed
   - Records: Customer ID, amount, invoice ID, new balance

**Business Rules**:
- Credit balance cannot go negative (enforced by invariant)
- Credit deducted when applied to invoice (via `Invoice.applyCreditDiscount()`)

**Example**:
```java
// Credit deducted from customer
customer.deductCredit(Money.of(50.00));
// → CreditDeductedEvent published

// Event listeners execute:
// 1. Audit log updated
// 2. Activity feed updated
```

---

### 9. CustomerDeactivatedEvent

**Published By**: `Customer.markAsInactive()` method  
**Aggregate**: Customer  
**When**: Customer is soft deleted (marked as INACTIVE)

**Payload**:
```java
{
    customerId: UUID,
    customerName: String,
    reason: String (e.g., "Zero balance, all invoices paid"),
    deactivatedAt: Instant
}
```

**Consumers**:
1. **Audit Log Listener** (`AuditLogListener`)
   - Logs customer deactivation to audit log
   - Records: Customer ID, reason, timestamp

2. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs customer deactivation event to activity feed
   - Records: Customer ID, customer name, reason

**Business Rules**:
- Customer can only be deactivated if `canBeDeleted()` returns true
- Cannot reactivate (INACTIVE is terminal state)

**Example**:
```java
// Customer deactivated
if (customer.canBeDeleted()) {
    customer.markAsInactive();
    // → CustomerDeactivatedEvent published
}

// Event listeners execute:
// 1. Audit log updated
// 2. Activity feed updated
```

---

### 10. InvoiceCancelledEvent

**Published By**: `Invoice.cancel()` method  
**Aggregate**: Invoice  
**When**: Invoice is cancelled (DRAFT/SENT → CANCELLED)

**Payload**:
```java
{
    invoiceId: UUID,
    invoiceNumber: String,
    customerId: UUID,
    customerName: String,
    customerEmail: String,
    reason: String (optional),
    cancelledAt: Instant,
    previousStatus: InvoiceStatus
}
```

**Consumers**:
1. **Email Listener** (`InvoiceCancelledEmailListener`)
   - Sends cancellation notice email to customer
   - Email includes: Invoice number, cancellation reason
   - Subject: "Invoice [Number] Cancelled"

2. **Audit Log Listener** (`AuditLogListener`)
   - Logs invoice cancellation to audit log
   - Records: Invoice ID, reason, timestamp

3. **Activity Feed Listener** (`ActivityFeedListener`)
   - Logs invoice cancellation event to activity feed
   - Records: Invoice ID, invoice number, reason

**Business Rules**:
- Cannot cancel PAID invoices (must issue refund instead)
- Cannot cancel if payments have been applied (must issue refund first)
- Cannot reactivate (CANCELLED is terminal state)

**Example**:
```java
// Invoice cancelled
invoice.cancel();
// → InvoiceCancelledEvent published

// Event listeners execute:
// 1. Cancellation email sent
// 2. Audit log updated
// 3. Activity feed updated
```

---

## Event Producer/Consumer Matrix

| Event | Producer (Aggregate Method) | Consumers | Side Effects |
|-------|----------------------------|-----------|--------------|
| `PaymentRecordedEvent` | `Payment.record()` | Email, Dashboard Cache, Activity Feed | Payment confirmation email, cache invalidation, audit log |
| `InvoiceSentEvent` | `Invoice.markAsSent()` | Email, Dashboard Cache, Activity Feed | Invoice email with PDF, cache invalidation, audit log |
| `InvoiceFullyPaidEvent` | `Invoice.recordPayment()` (when balance = 0) | Email, Notification, Activity Feed | Payment confirmation email, notification, audit log |
| `LateFeeAppliedEvent` | Scheduled job (`LateFeeScheduledJob`) | Email, Activity Feed | Overdue reminder email, audit log |
| `RecurringInvoiceGeneratedEvent` | Scheduled job (`RecurringInvoiceScheduledJob`) | Email, Dashboard Cache, Activity Feed | Invoice email (if autoSend), cache invalidation, audit log |
| `RefundIssuedEvent` | `Refund.issue()` | Email, Activity Feed | Refund notification email, audit log |
| `CreditAppliedEvent` | `Customer.applyCredit()` | Audit Log, Activity Feed | Audit log, activity feed |
| `CreditDeductedEvent` | `Customer.deductCredit()` | Audit Log, Activity Feed | Audit log, activity feed |
| `CustomerDeactivatedEvent` | `Customer.markAsInactive()` | Audit Log, Activity Feed | Audit log, activity feed |
| `InvoiceCancelledEvent` | `Invoice.cancel()` | Email, Audit Log, Activity Feed | Cancellation email, audit log, activity feed |

---

## Event Listener Implementation

### Email Listeners

**Purpose**: Send email notifications for business events  
**Implementation**: AWS SES integration  
**Pattern**: `@TransactionalEventListener(AFTER_COMMIT)`

**Listeners**:
- `PaymentRecordedEmailListener` → Sends payment confirmation
- `InvoiceSentEmailListener` → Sends invoice email with PDF
- `InvoiceFullyPaidEmailListener` → Sends payment completion notification
- `LateFeeEmailListener` → Sends overdue reminder
- `RecurringInvoiceEmailListener` → Sends recurring invoice email
- `RefundEmailListener` → Sends refund notification
- `InvoiceCancelledEmailListener` → Sends cancellation notice

**Example**:
```java
@Component
public class InvoiceSentEmailListener {
    
    @Autowired
    private EmailService emailService;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInvoiceSent(InvoiceSentEvent event) {
        emailService.sendInvoiceEmail(
            event.getCustomerEmail(),
            event.getInvoiceNumber(),
            event.getInvoiceId()
        );
    }
}
```

### Dashboard Cache Listeners

**Purpose**: Invalidate dashboard cache when data changes  
**Implementation**: Spring Cache (Caffeine)  
**Pattern**: `@TransactionalEventListener(AFTER_COMMIT)`

**Listeners**:
- `DashboardCacheInvalidationListener` → Invalidates cache on payment/invoice events

**Example**:
```java
@Component
public class DashboardCacheInvalidationListener {
    
    @Autowired
    private CacheManager cacheManager;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentRecorded(PaymentRecordedEvent event) {
        cacheManager.getCache("dashboardMetrics").clear();
    }
}
```

### Activity Feed Listeners

**Purpose**: Log all domain events to activity feed  
**Implementation**: Activity feed repository  
**Pattern**: `@TransactionalEventListener(AFTER_COMMIT)`

**Listeners**:
- `ActivityFeedListener` → Logs all events to activity feed

**Example**:
```java
@Component
public class ActivityFeedListener {
    
    @Autowired
    private ActivityFeedRepository activityFeedRepository;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentRecorded(PaymentRecordedEvent event) {
        ActivityFeedEntry entry = ActivityFeedEntry.builder()
            .aggregateId(event.getInvoiceId())
            .eventType("PaymentRecorded")
            .description(String.format("Payment of %s recorded for invoice %s", 
                event.getAmount(), event.getInvoiceNumber()))
            .occurredAt(Instant.now())
            .build();
        activityFeedRepository.save(entry);
    }
}
```

### Audit Log Listeners

**Purpose**: Log critical business events for compliance  
**Implementation**: Audit log repository  
**Pattern**: `@TransactionalEventListener(AFTER_COMMIT)`

**Listeners**:
- `AuditLogListener` → Logs credit applications, deductions, customer deactivations, invoice cancellations

**Example**:
```java
@Component
public class AuditLogListener {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreditApplied(CreditAppliedEvent event) {
        AuditLogEntry entry = AuditLogEntry.builder()
            .entityType("Customer")
            .entityId(event.getCustomerId())
            .action("CreditApplied")
            .details(String.format("Credit of %s applied, new balance: %s", 
                event.getAmount(), event.getNewBalance()))
            .timestamp(Instant.now())
            .build();
        auditLogRepository.save(entry);
    }
}
```

---

## Event Flow Diagrams

### Payment Recording Flow

```
Payment.record() (Command Handler)
    ↓
Payment entity created
    ↓
Invoice.recordPayment() called
    ↓
Invoice balance updated
    ↓
Transaction commit
    ↓
PaymentRecordedEvent published
    ↓
Event Listeners (AFTER_COMMIT):
    ├─→ Email Listener → Send payment confirmation
    ├─→ Dashboard Cache Listener → Invalidate cache
    └─→ Activity Feed Listener → Log event

If balance = 0:
    ↓
InvoiceFullyPaidEvent published
    ↓
Event Listeners (AFTER_COMMIT):
    ├─→ Email Listener → Send completion notification
    ├─→ Notification Listener → Send push notification
    └─→ Activity Feed Listener → Log event

If overpayment:
    ↓
CreditAppliedEvent published
    ↓
Event Listeners (AFTER_COMMIT):
    ├─→ Audit Log Listener → Log credit application
    └─→ Activity Feed Listener → Log event
```

### Invoice Sent Flow

```
Invoice.markAsSent() (Command Handler)
    ↓
Invoice status = SENT
    ↓
Customer credit checked
    ↓
If credit available:
    ├─→ Credit applied (discount line item)
    └─→ CreditDeductedEvent published
    ↓
Transaction commit
    ↓
InvoiceSentEvent published
    ↓
Event Listeners (AFTER_COMMIT):
    ├─→ Email Listener → Send invoice email with PDF
    ├─→ Dashboard Cache Listener → Invalidate cache
    └─→ Activity Feed Listener → Log event
```

---

## Design Decisions

### Why Domain Events?

**Decision**: Use domain events for cross-aggregate side effects  
**Rationale**:
- Decouples aggregates (Invoice doesn't know about email service)
- Enables eventual consistency (credit applied asynchronously)
- Supports audit logging (all events logged to activity feed)
- Enables dashboard cache invalidation (events trigger cache refresh)

### Why AFTER_COMMIT?

**Decision**: Publish events after transaction commit  
**Rationale**:
- Events published only if transaction succeeds
- If transaction rolls back, events are not published
- Event listeners execute asynchronously (don't block transaction)
- If listener fails, transaction is not rolled back (side effects are independent)

### Why In-Memory Events?

**Decision**: Use Spring ApplicationEventPublisher (in-memory) instead of event store  
**Rationale**:
- Simpler implementation (no event store required)
- Sufficient for MVP (eventual consistency acceptable)
- Can migrate to event store (Kafka, EventStore) later if needed
- Events logged to activity feed for audit trail

---

## Testing Domain Events

### Unit Testing

**Test Event Publishing**:
```java
@Test
void testInvoiceSentEventPublished() {
    Invoice invoice = Invoice.create(...);
    invoice.markAsSent();
    
    List<DomainEvent> events = invoice.getDomainEvents();
    assertThat(events).hasSize(1);
    assertThat(events.get(0)).isInstanceOf(InvoiceSentEvent.class);
}
```

### Integration Testing

**Test Event Listener**:
```java
@Test
void testInvoiceSentEmailListener() {
    Invoice invoice = createInvoice();
    invoice.markAsSent();
    invoiceRepository.save(invoice);
    
    // Verify email sent
    verify(emailService).sendInvoiceEmail(any(), any(), any());
}
```

---

**End of Domain Events Documentation**

