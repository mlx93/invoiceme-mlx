# Domain Aggregates Documentation

**Last Updated**: 2025-01-27  
**Status**: M1 Phase - Domain & API Freeze  
**Architecture**: Domain-Driven Design (DDD) with Rich Domain Models

---

## Overview

This document defines the Domain-Driven Design (DDD) aggregates for the InvoiceMe system. Each aggregate is a rich domain model with behavior (not anemic data models), enforcing business invariants and publishing domain events.

**Key Principles**:
- **Rich Domain Models**: Aggregates contain business logic and behavior methods, not just getters/setters
- **Aggregate Roots**: Customer, Invoice, Payment are aggregate roots that enforce invariants
- **Value Objects**: Immutable objects (Money, Email, InvoiceNumber, Address) with no identity
- **Domain Events**: Business-significant occurrences published after transaction commit
- **Invariants**: Business rules enforced at aggregate level (cannot be violated)

---

## 1. Customer Aggregate

### 1.1 Properties

| Property | Type | Description | Constraints |
|----------|------|-------------|-------------|
| `id` | UUID | Unique customer identifier | Primary key, immutable |
| `companyName` | String | Company or individual name | Required, max 255 chars |
| `contactName` | String | Primary contact name | Optional, max 255 chars |
| `email` | Email (Value Object) | Customer email address | Required, unique, immutable |
| `phone` | String | Contact phone number | Optional, max 50 chars |
| `address` | Address (Value Object) | Billing address | Optional, immutable |
| `customerType` | CustomerType (Enum) | RESIDENTIAL, COMMERCIAL, or INSURANCE | Required, default COMMERCIAL |
| `creditBalance` | Money (Value Object) | Available credit from overpayments | Required, default $0.00, >= $0.00 |
| `status` | CustomerStatus (Enum) | ACTIVE, INACTIVE, or SUSPENDED | Required, default ACTIVE |
| `createdAt` | Instant | Creation timestamp | Auto-generated, immutable |
| `updatedAt` | Instant | Last update timestamp | Auto-updated on changes |

### 1.2 Behavior Methods

#### `applyCredit(Money amount)`
**Purpose**: Add credit to customer's balance (from overpayment or manual credit)  
**Parameters**: `amount` (Money) - Credit amount to add (must be > $0.00)  
**Returns**: `void`  
**Side Effects**: 
- Updates `creditBalance` (adds amount)
- Publishes `CreditAppliedEvent` (after transaction commit)
- Logs to activity feed

**Business Rules**:
- Amount must be > $0.00
- Credit balance cannot exceed reasonable limit (e.g., $1,000,000) - configurable

**Example**:
```java
customer.applyCredit(Money.of(100.00, Currency.USD));
// creditBalance increases by $100.00
```

#### `deductCredit(Money amount)`
**Purpose**: Deduct credit from customer's balance (when credit applied to invoice)  
**Parameters**: `amount` (Money) - Credit amount to deduct (must be > $0.00)  
**Returns**: `void`  
**Side Effects**:
- Updates `creditBalance` (subtracts amount)
- Publishes `CreditDeductedEvent` (after transaction commit)
- Logs to activity feed

**Business Rules**:
- Amount must be > $0.00
- Credit balance cannot go negative (enforced by invariant)

**Example**:
```java
customer.deductCredit(Money.of(50.00, Currency.USD));
// creditBalance decreases by $50.00
```

#### `canBeDeleted()`
**Purpose**: Check if customer can be safely deleted  
**Returns**: `boolean` - `true` if customer can be deleted, `false` otherwise  
**Business Rules**:
- Customer can be deleted only if:
  - All invoices are paid (balance = $0) OR cancelled
  - Credit balance = $0.00

**Example**:
```java
if (customer.canBeDeleted()) {
    customer.markAsInactive(); // Soft delete
}
```

#### `markAsInactive()`
**Purpose**: Soft delete customer (mark as INACTIVE, retain records)  
**Returns**: `void`  
**Side Effects**:
- Updates `status` to INACTIVE
- Publishes `CustomerDeactivatedEvent` (after transaction commit)
- Logs to activity feed

**Business Rules**:
- Cannot mark inactive if `canBeDeleted()` returns `false`
- Cannot reactivate (INACTIVE is terminal state)

**Example**:
```java
if (customer.canBeDeleted()) {
    customer.markAsInactive();
}
```

### 1.3 Invariants

1. **Email Uniqueness**: Email address must be unique across all customers (enforced at database level)
2. **Credit Balance Non-Negative**: `creditBalance >= $0.00` (enforced by CHECK constraint and domain logic)
3. **Cannot Delete with Outstanding Balance**: Customer cannot be deleted if any invoice has balance > $0

### 1.4 Domain Events

| Event | Published By | Payload | Consumers |
|-------|--------------|---------|-----------|
| `CreditAppliedEvent` | `applyCredit()` | `customerId`, `amount`, `newBalance` | Audit log listener, Activity feed listener |
| `CreditDeductedEvent` | `deductCredit()` | `customerId`, `amount`, `newBalance` | Audit log listener, Activity feed listener |
| `CustomerDeactivatedEvent` | `markAsInactive()` | `customerId`, `reason` | Audit log listener, Activity feed listener |

### 1.5 Aggregate Boundaries

**Customer Aggregate Root**:
- Owns: Customer entity (self)
- References: None (no child entities)
- Relationships: 
  - One-to-Many with Invoices (reference only, Invoice is separate aggregate)
  - One-to-Many with Payments (reference only, Payment is separate aggregate)

**Consistency Boundary**:
- Customer aggregate ensures credit balance consistency
- Invoice aggregate ensures invoice balance consistency
- Cross-aggregate consistency handled via domain events (eventual consistency)

---

## 2. Invoice Aggregate

### 2.1 Properties

| Property | Type | Description | Constraints |
|----------|------|-------------|-------------|
| `id` | UUID | Unique invoice identifier | Primary key, immutable |
| `invoiceNumber` | InvoiceNumber (Value Object) | Auto-generated format INV-YYYY-#### | Required, unique, immutable |
| `customerId` | UUID | Reference to Customer aggregate | Required, foreign key |
| `issueDate` | LocalDate | Invoice issue date | Required |
| `dueDate` | LocalDate | Invoice due date | Required, must be >= issueDate |
| `status` | InvoiceStatus (Enum) | DRAFT, SENT, PAID, OVERDUE, or CANCELLED | Required, default DRAFT |
| `paymentTerms` | PaymentTerms (Enum) | NET_30, DUE_ON_RECEIPT, or CUSTOM | Required, default NET_30 |
| `lineItems` | List<LineItem> | Line items (entities within aggregate) | Required, min 1 item, ordered by sortOrder |
| `subtotal` | Money | Sum of line item totals before tax/discount | Calculated, >= $0.00 |
| `taxAmount` | Money | Total tax amount | Calculated, >= $0.00 |
| `discountAmount` | Money | Total discount amount | Calculated, >= $0.00 |
| `totalAmount` | Money | Final invoice total (subtotal + tax - discount) | Calculated, >= $0.00 |
| `amountPaid` | Money | Sum of all payments applied | Calculated, >= $0.00 |
| `balanceDue` | Money | Remaining balance (totalAmount - amountPaid) | Calculated, >= $0.00 |
| `notes` | String | Optional invoice notes | Optional, max TEXT length |
| `sentDate` | Instant | When invoice was marked as sent | Optional, set on markAsSent() |
| `paidDate` | Instant | When invoice was fully paid | Optional, set when balance = $0 |
| `version` | Integer | Optimistic locking version | Required, default 1, >= 1 |
| `createdAt` | Instant | Creation timestamp | Auto-generated, immutable |
| `updatedAt` | Instant | Last update timestamp | Auto-updated on changes |

### 2.2 Line Item Entity (Within Invoice Aggregate)

| Property | Type | Description | Constraints |
|----------|------|-------------|-------------|
| `id` | UUID | Unique line item identifier | Primary key |
| `description` | String | Line item description | Required, max 500 chars |
| `quantity` | Integer | Quantity | Required, >= 1 |
| `unitPrice` | Money | Price per unit | Required, >= $0.00 |
| `discountType` | DiscountType (Enum) | NONE, PERCENTAGE, or FIXED | Required, default NONE |
| `discountValue` | Money | Discount amount or percentage | Required, >= $0.00 |
| `taxRate` | BigDecimal | Tax rate percentage (0-100) | Required, default 0, 0-100 |
| `sortOrder` | Integer | Display order | Required, default 0 |
| `lineTotal` | Money | Calculated total (read-only) | Calculated: (Base - Discount) * (1 + TaxRate) |

**Line Item Calculation Logic**:
```
Base Amount = Quantity × Unit Price
Discount Amount = (Type == PERCENTAGE) ? Base × (Value / 100) : Value
Taxable Amount = Base - Discount
Tax Amount = Taxable × (Tax Rate / 100)
Line Total = Taxable + Tax
```

**Rounding**: All monetary calculations use Banker's rounding (HALF_UP) to 2 decimal places.

### 2.3 Behavior Methods

#### `addLineItem(LineItem lineItem)`
**Purpose**: Add a line item to the invoice  
**Parameters**: `lineItem` (LineItem) - Line item to add  
**Returns**: `void`  
**Side Effects**:
- Adds line item to `lineItems` list
- Recalculates invoice totals (subtotal, taxAmount, totalAmount, balanceDue)
- Increments `version` for optimistic locking

**Business Rules**:
- Can only add line items if status is DRAFT or SENT
- Cannot add line items if status is PAID or CANCELLED
- If status is SENT, line item addition triggers version increment (for audit)

**Example**:
```java
LineItem item = LineItem.builder()
    .description("Water Extraction Service")
    .quantity(1)
    .unitPrice(Money.of(500.00, Currency.USD))
    .discountType(DiscountType.NONE)
    .taxRate(BigDecimal.valueOf(8.25))
    .build();
invoice.addLineItem(item);
```

#### `removeLineItem(UUID lineItemId)`
**Purpose**: Remove a line item from the invoice  
**Parameters**: `lineItemId` (UUID) - ID of line item to remove  
**Returns**: `void`  
**Side Effects**:
- Removes line item from `lineItems` list
- Recalculates invoice totals
- Increments `version` for optimistic locking

**Business Rules**:
- Can only remove line items if status is DRAFT or SENT
- Cannot remove line items if status is PAID or CANCELLED
- Must have at least 1 line item after removal (enforced by invariant)

**Example**:
```java
invoice.removeLineItem(lineItemId);
```

#### `markAsSent()`
**Purpose**: Mark invoice as sent to customer (DRAFT → SENT)  
**Returns**: `void`  
**Side Effects**:
- Updates `status` to SENT
- Sets `sentDate` to current timestamp
- Checks customer credit balance, auto-applies if available
- Publishes `InvoiceSentEvent` (after transaction commit)
- Logs to activity feed

**Business Rules**:
- Can only mark as sent if status is DRAFT
- Must have at least 1 line item (enforced by invariant)
- Customer email must be valid (for email notification)
- If customer has credit balance > $0, auto-applies credit (creates discount line item)

**Example**:
```java
invoice.markAsSent();
// Status changes to SENT, sentDate set, credit auto-applied if available
```

#### `recordPayment(Money amount)`
**Purpose**: Record a payment against the invoice  
**Parameters**: `amount` (Money) - Payment amount (must be > $0.00)  
**Returns**: `void`  
**Side Effects**:
- Updates `amountPaid` (adds amount)
- Recalculates `balanceDue` (totalAmount - amountPaid)
- If balanceDue = $0, updates `status` to PAID and sets `paidDate`
- If amount > balanceDue, excess goes to customer credit balance
- Publishes `PaymentRecordedEvent` (after transaction commit)
- If balance = $0, publishes `InvoiceFullyPaidEvent` (after transaction commit)
- Logs to activity feed

**Business Rules**:
- Can only record payment if status is SENT or OVERDUE
- Cannot record payment if status is DRAFT, PAID, or CANCELLED
- Amount must be > $0.00
- If payment > balanceDue, excess goes to customer credit (via domain event)

**Example**:
```java
invoice.recordPayment(Money.of(1000.00, Currency.USD));
// amountPaid increases, balanceDue decreases
// If balanceDue = 0, status changes to PAID
```

#### `applyCreditDiscount(Money creditAmount)`
**Purpose**: Apply customer credit as discount line item  
**Parameters**: `creditAmount` (Money) - Credit amount to apply (must be > $0.00)  
**Returns**: `void`  
**Side Effects**:
- Creates discount line item: "Credit Applied - $[amount]"
- Updates `discountAmount` (adds creditAmount)
- Recalculates `totalAmount` and `balanceDue`
- Deducts credit from customer (via domain event)

**Business Rules**:
- Can only apply credit if status is DRAFT or SENT
- Credit amount cannot exceed customer credit balance
- Credit amount cannot exceed invoice totalAmount

**Example**:
```java
invoice.applyCreditDiscount(Money.of(50.00, Currency.USD));
// Discount line item added, totalAmount reduced
```

#### `addLateFee(Money lateFeeAmount)`
**Purpose**: Add late fee as line item (called by scheduled job)  
**Parameters**: `lateFeeAmount` (Money) - Late fee amount (typically $125.00)  
**Returns**: `void`  
**Side Effects**:
- Creates line item: "Late Fee - [Month Year]"
- Updates `totalAmount` and `balanceDue`
- Publishes `LateFeeAppliedEvent` (after transaction commit)
- Logs to activity feed

**Business Rules**:
- Can only add late fee if status is SENT or OVERDUE
- Late fee capped at 3 months ($375 max per invoice)
- Late fee line item must not already exist for current month

**Example**:
```java
invoice.addLateFee(Money.of(125.00, Currency.USD));
// Late fee line item added, totalAmount increased
```

#### `cancel()`
**Purpose**: Cancel invoice (DRAFT/SENT → CANCELLED)  
**Returns**: `void`  
**Side Effects**:
- Updates `status` to CANCELLED
- Publishes `InvoiceCancelledEvent` (after transaction commit)
- Logs to activity feed

**Business Rules**:
- Can only cancel if status is DRAFT or SENT
- Cannot cancel if status is PAID (must issue refund instead)
- Cannot cancel if payments have been applied (must issue refund first)
- Cannot reactivate (CANCELLED is terminal state)

**Example**:
```java
invoice.cancel();
// Status changes to CANCELLED
```

#### `isOverdue()`
**Purpose**: Check if invoice is overdue  
**Returns**: `boolean` - `true` if overdue, `false` otherwise  
**Business Rules**:
- Invoice is overdue if:
  - Current date > dueDate
  - Status is SENT or OVERDUE
  - balanceDue > $0.00

**Example**:
```java
if (invoice.isOverdue()) {
    // Schedule late fee application
}
```

### 2.4 Invariants

1. **At Least One Line Item**: Invoice must have at least 1 line item (enforced before markAsSent())
2. **Balance Calculation**: `balanceDue = totalAmount - amountPaid` (always consistent)
3. **Cannot Mark Sent Without Line Items**: Cannot mark as sent if lineItems.isEmpty()
4. **Cannot Record Payment on Draft**: Cannot record payment if status is DRAFT
5. **Cannot Record Payment on Paid**: Cannot record payment if status is PAID
6. **Version Consistency**: Optimistic locking prevents concurrent modifications

### 2.5 Domain Events

| Event | Published By | Payload | Consumers |
|-------|--------------|---------|-----------|
| `InvoiceSentEvent` | `markAsSent()` | `invoiceId`, `invoiceNumber`, `customerId`, `totalAmount`, `dueDate` | Email listener (sends invoice email), Dashboard cache listener (invalidates cache), Activity feed listener |
| `InvoiceFullyPaidEvent` | `recordPayment()` (when balance = 0) | `invoiceId`, `invoiceNumber`, `customerId`, `paidDate` | Email listener (sends payment confirmation), Notification listener, Activity feed listener |
| `LateFeeAppliedEvent` | `addLateFee()` | `invoiceId`, `invoiceNumber`, `customerId`, `lateFeeAmount`, `newBalance` | Email listener (sends overdue reminder), Activity feed listener |
| `InvoiceCancelledEvent` | `cancel()` | `invoiceId`, `invoiceNumber`, `customerId`, `reason` | Email listener (sends cancellation notice), Audit log listener, Activity feed listener |

### 2.6 Aggregate Boundaries

**Invoice Aggregate Root**:
- Owns: Invoice entity (self), LineItem entities (child entities)
- References: Customer aggregate (via customerId UUID)
- Relationships:
  - Many-to-One with Customer (reference only, Customer is separate aggregate)
  - One-to-Many with Payments (reference only, Payment is separate aggregate)

**Consistency Boundary**:
- Invoice aggregate ensures invoice balance consistency
- Line items are part of invoice aggregate (deleted when invoice deleted)
- Payment aggregate updates invoice balance via domain events (eventual consistency)

---

## 3. Payment Aggregate

### 3.1 Properties

| Property | Type | Description | Constraints |
|----------|------|-------------|-------------|
| `id` | UUID | Unique payment identifier | Primary key, immutable |
| `invoiceId` | UUID | Reference to Invoice aggregate | Required, foreign key |
| `customerId` | UUID | Reference to Customer aggregate | Required, foreign key (denormalized) |
| `amount` | Money | Payment amount | Required, > $0.00 |
| `paymentMethod` | PaymentMethod (Enum) | CREDIT_CARD or ACH | Required |
| `paymentDate` | LocalDate | Date payment was received | Required |
| `paymentReference` | String | Optional reference (e.g., "VISA-4532") | Optional, max 100 chars |
| `status` | PaymentStatus (Enum) | PENDING, COMPLETED, FAILED, or REFUNDED | Required, default COMPLETED |
| `createdByUserId` | UUID | User who recorded the payment | Optional, foreign key |
| `notes` | String | Optional payment notes | Optional, max TEXT length |
| `createdAt` | Instant | Creation timestamp | Auto-generated, immutable |

### 3.2 Behavior Methods

#### `static Payment record(Invoice invoice, Customer customer, Money amount, PaymentMethod method, LocalDate paymentDate, UUID createdByUserId)`
**Purpose**: Static factory method to record a payment (validates and updates invoice)  
**Parameters**:
- `invoice` (Invoice) - Invoice to apply payment to
- `customer` (Customer) - Customer making payment
- `amount` (Money) - Payment amount (must be > $0.00)
- `method` (PaymentMethod) - Payment method
- `paymentDate` (LocalDate) - Payment date
- `createdByUserId` (UUID) - User recording payment

**Returns**: `Payment` - Created payment entity  
**Side Effects**:
- Creates Payment entity
- Calls `invoice.recordPayment(amount)` (updates invoice balance)
- If payment > invoice balance, excess goes to customer credit (via domain event)
- Publishes `PaymentRecordedEvent` (after transaction commit)
- Logs to activity feed

**Business Rules**:
- Amount must be > $0.00
- Invoice must be in SENT or OVERDUE status
- Cannot record payment if invoice status is DRAFT, PAID, or CANCELLED
- If payment > invoice balance, excess goes to customer credit balance

**Example**:
```java
Payment payment = Payment.record(
    invoice,
    customer,
    Money.of(1000.00, Currency.USD),
    PaymentMethod.CREDIT_CARD,
    LocalDate.now(),
    currentUserId
);
// Payment created, invoice balance updated, credit applied if overpayment
```

### 3.3 Invariants

1. **Positive Amount**: `amount > $0.00` (strictly positive, enforced by CHECK constraint)
2. **Valid Invoice Status**: Invoice must be in SENT or OVERDUE status (enforced by domain logic)
3. **Invoice Reference**: Invoice must exist (enforced by foreign key)

### 3.4 Domain Events

| Event | Published By | Payload | Consumers |
|-------|--------------|---------|-----------|
| `PaymentRecordedEvent` | `Payment.record()` | `paymentId`, `invoiceId`, `customerId`, `amount`, `paymentMethod`, `paymentDate`, `remainingBalance` | Email listener (sends payment confirmation), Dashboard cache listener (invalidates cache), Activity feed listener |

### 3.5 Aggregate Boundaries

**Payment Aggregate Root**:
- Owns: Payment entity (self)
- References: Invoice aggregate (via invoiceId UUID), Customer aggregate (via customerId UUID)
- Relationships:
  - Many-to-One with Invoice (reference only, Invoice is separate aggregate)
  - Many-to-One with Customer (reference only, Customer is separate aggregate)

**Consistency Boundary**:
- Payment aggregate ensures payment amount consistency
- Invoice aggregate updates balance via `recordPayment()` method call (immediate consistency)
- Customer aggregate updates credit balance via domain events (eventual consistency)

---

## 4. Value Objects

### 4.1 Money Value Object

**Purpose**: Immutable monetary value with currency and precision  
**Properties**:
- `amount` (BigDecimal) - Monetary amount (precision: 19 digits, 2 decimal places)
- `currency` (Currency) - Currency code (default: USD)

**Behavior**:
- `add(Money other)` - Add two Money values (returns new Money)
- `subtract(Money other)` - Subtract two Money values (returns new Money)
- `multiply(BigDecimal multiplier)` - Multiply Money by scalar (returns new Money)
- `compareTo(Money other)` - Compare two Money values
- `isPositive()` - Check if amount > $0.00
- `isNegative()` - Check if amount < $0.00
- `isZero()` - Check if amount = $0.00

**Rounding Strategy**:
- All calculations use Banker's rounding (HALF_UP) to 2 decimal places
- BigDecimal precision: 19 digits total, 2 decimal places
- Example: `$10.125` rounds to `$10.13`, `$10.124` rounds to `$10.12`

**Immutability**: Money objects are immutable (cannot be modified after creation)

**Example**:
```java
Money price = Money.of(100.50, Currency.USD);
Money tax = price.multiply(BigDecimal.valueOf(0.0825)); // 8.25% tax
Money total = price.add(tax); // $108.79 (rounded)
```

### 4.2 Email Value Object

**Purpose**: Immutable email address with validation  
**Properties**:
- `value` (String) - Email address (validated format)

**Behavior**:
- `validate(String email)` - Validates email format (throws exception if invalid)
- `equals(Object other)` - Value equality (case-insensitive)

**Validation Rules**:
- Must match RFC 5322 email format
- Must contain @ symbol
- Must have valid domain

**Immutability**: Email objects are immutable (cannot be modified after creation)

**Example**:
```java
Email email = Email.of("customer@example.com");
// Validates format, throws exception if invalid
```

### 4.3 InvoiceNumber Value Object

**Purpose**: Immutable invoice number with format validation  
**Properties**:
- `value` (String) - Invoice number (format: INV-YYYY-####)

**Behavior**:
- `generate(int sequenceNumber)` - Static factory to generate invoice number
- `validate(String invoiceNumber)` - Validates format (throws exception if invalid)
- `getYear()` - Extract year from invoice number
- `getSequence()` - Extract sequence number from invoice number

**Format**: `INV-YYYY-####` (e.g., `INV-2025-0001`)

**Immutability**: InvoiceNumber objects are immutable (cannot be modified after creation)

**Example**:
```java
InvoiceNumber number = InvoiceNumber.generate(1); // INV-2025-0001
InvoiceNumber parsed = InvoiceNumber.of("INV-2025-0001");
```

### 4.4 Address Value Object

**Purpose**: Immutable address with validation  
**Properties**:
- `street` (String) - Street address
- `city` (String) - City
- `state` (String) - State/province
- `zipCode` (String) - ZIP/postal code
- `country` (String) - Country (default: "USA")

**Behavior**:
- `validate()` - Validates address components (throws exception if invalid)
- `toFormattedString()` - Returns formatted address string

**Validation Rules**:
- Street, city, state, zipCode are required
- Country defaults to "USA" if not provided

**Immutability**: Address objects are immutable (cannot be modified after creation)

**Example**:
```java
Address address = Address.builder()
    .street("123 Main St")
    .city("Austin")
    .state("TX")
    .zipCode("78701")
    .country("USA")
    .build();
```

---

## 5. Aggregate Relationships

### 5.1 Relationship Diagram

```
Customer (Aggregate Root)
    │
    ├───< Invoices (Aggregate Root) ────< LineItems (Entity within Invoice)
    │                    │
    │                    └───< Payments (Aggregate Root)
```

### 5.2 Consistency Patterns

**Immediate Consistency** (within aggregate):
- Invoice aggregate: Line items updated immediately when added/removed

**Eventual Consistency** (cross-aggregate):
- Payment → Invoice: Payment updates invoice balance via `recordPayment()` method call (immediate)
- Payment → Customer: Overpayment credit applied via domain event (eventual)
- Invoice → Customer: Credit application via domain event (eventual)

**Domain Events** (after transaction commit):
- All domain events published after successful transaction commit
- Event listeners handle side effects (email, audit log, cache invalidation)
- Pattern: `@TransactionalEventListener(AFTER_COMMIT)`

---

## 6. Design Decisions

### 6.1 Why Rich Domain Models?

**Decision**: Use rich domain models with behavior methods instead of anemic data models  
**Rationale**:
- Business logic encapsulated in domain objects (not in services)
- Invariants enforced at aggregate level (cannot be violated)
- Domain events published by aggregates (not by services)
- Easier to test (business logic in domain objects)

### 6.2 Why Separate Aggregates?

**Decision**: Customer, Invoice, and Payment are separate aggregates  
**Rationale**:
- Each aggregate has its own consistency boundary
- Aggregates can be modified independently (better scalability)
- Cross-aggregate consistency handled via domain events (eventual consistency)
- Follows DDD best practices (aggregate boundaries)

### 6.3 Why Value Objects?

**Decision**: Use value objects (Money, Email, InvoiceNumber, Address) instead of primitives  
**Rationale**:
- Type safety (prevents mixing Money with BigDecimal)
- Validation encapsulated (email format, invoice number format)
- Immutability (prevents accidental modification)
- Business logic encapsulated (Money calculations, rounding)

### 6.4 Why Domain Events?

**Decision**: Use domain events for cross-aggregate side effects  
**Rationale**:
- Decouples aggregates (Invoice doesn't know about email service)
- Enables eventual consistency (credit applied asynchronously)
- Supports audit logging (all events logged to activity feed)
- Enables dashboard cache invalidation (events trigger cache refresh)

---

## 7. Implementation Notes

### 7.1 Money Rounding

**Strategy**: Banker's rounding (HALF_UP) to 2 decimal places  
**Implementation**:
```java
BigDecimal amount = new BigDecimal("100.125");
amount = amount.setScale(2, RoundingMode.HALF_UP); // 100.13
```

**Applied To**:
- Line item calculations (lineTotal)
- Invoice totals (subtotal, taxAmount, totalAmount)
- Payment amounts
- Credit balances

### 7.2 Optimistic Locking

**Strategy**: Version field on Invoice aggregate  
**Implementation**:
- `version` field incremented on each update
- JPA `@Version` annotation for automatic version management
- Prevents concurrent modification conflicts

### 7.3 Domain Event Publishing

**Pattern**: `@TransactionalEventListener(AFTER_COMMIT)`  
**Implementation**:
- Events collected in aggregate during transaction
- Events published after successful transaction commit
- Event listeners execute asynchronously (no transaction rollback if listener fails)

---

**End of Domain Aggregates Documentation**

