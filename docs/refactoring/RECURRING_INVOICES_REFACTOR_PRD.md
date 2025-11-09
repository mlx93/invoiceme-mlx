# Recurring Invoices Refactoring PRD

**Date**: 2025-01-27  
**Status**: 📋 **PROPOSAL** - Architectural Simplification  
**Priority**: Medium (Simplifies architecture, reduces code complexity)

---

## Executive Summary

**Current Architecture**: Separate `RecurringInvoiceTemplate` aggregate with `TemplateLineItem` entities, requiring separate repository, handlers, controllers, and frontend pages.

**Proposed Architecture**: Add recurring schedule fields directly to `Invoice` entity, eliminating the need for a separate template aggregate.

**Benefit**: Simpler architecture, less code to maintain, unified invoice management.

---

## Problem Statement

### Current Implementation Issues

1. **Code Duplication**: 
   - Template line items duplicate invoice line items
   - Template handlers duplicate invoice handlers
   - Template pages duplicate invoice pages

2. **Complexity**:
   - Two separate aggregates for essentially the same concept
   - Two separate repositories (including recently added `RecurringInvoiceTemplateRepositoryCustom/Impl` with Criteria-backed pagination)
   - Two separate sets of handlers/controllers (including recently added `ListRecurringTemplatesQuery/Handler/Mapper`)
   - Two separate frontend page sets

3. **Maintenance Burden**:
   - Changes to invoice structure require changes in two places
   - Template and invoice logic can drift apart
   - More code to test and maintain

**Note**: A debugging agent recently (2025-01-27) implemented backend support for listing recurring templates with Criteria-backed repository, DTOs (`RecurringTemplateDto`, `PagedRecurringTemplateResponse`), and `GET /api/v1/recurring-invoices` endpoint. This work will be removed as part of this refactoring, as the functionality will be merged into the unified invoice management system.

### Proposed Solution

**Add recurring schedule fields to Invoice entity**:
- `recurringFrequency` (MONTHLY, QUARTERLY, ANNUALLY, null = not recurring)
- `recurringNextDate` (next generation date, null = not recurring)
- `recurringEndDate` (optional end date for recurring schedule)
- `recurringAutoSend` (boolean, auto-send generated invoices)
- `recurringTemplateName` (optional name for recurring invoice series)

**When `recurringFrequency` is null**: Invoice is a regular one-time invoice  
**When `recurringFrequency` is set**: Invoice is a recurring template that generates new invoices

---

## Current Implementation Assessment

### Backend Components to Remove/Refactor

**Domain Layer**:
- ❌ `RecurringInvoiceTemplate.java` (remove aggregate)
- ❌ `TemplateLineItem.java` (remove entity)
- ✅ `Invoice.java` (add recurring fields)

**Infrastructure Layer**:
- ❌ `RecurringInvoiceTemplateRepository.java` (remove)
- ❌ `RecurringInvoiceTemplateRepositoryCustom.java` (remove - Criteria-backed repository interface)
- ❌ `RecurringInvoiceTemplateRepositoryImpl.java` (remove - Criteria implementation)
- ✅ `InvoiceRepository.java` (add query method: `findRecurringInvoicesReadyForGeneration()`)

**Application Layer** (Vertical Slices):
- ❌ All handlers in `recurringinvoices/` package (remove)
  - Includes: `ListRecurringTemplatesQuery/Handler/Mapper` (recently added)
- ❌ All controllers for recurring templates (remove)
  - Includes: `RecurringInvoiceTemplateController` with `GET /api/v1/recurring-invoices` endpoint
- ❌ DTOs: `RecurringTemplateDto`, `PagedRecurringTemplateResponse` (remove)
- ✅ `RecurringInvoiceScheduledJob.java` (refactor to use InvoiceRepository)

**Database**:
- ❌ `recurring_invoice_templates` table (migrate data, then drop)
- ❌ `template_line_items` table (migrate data, then drop)
- ✅ `invoices` table (add 5 new columns)

**Domain Events**:
- ✅ `RecurringInvoiceGeneratedEvent` (keep, update to reference Invoice instead of Template)

### Frontend Components to Remove/Refactor

**Pages**:
- ❌ `/recurring-invoices/page.tsx` (remove)
- ❌ `/recurring-invoices/[id]/page.tsx` (remove)
- ❌ `/recurring-invoices/new/page.tsx` (remove)
- ✅ `/invoices/page.tsx` (add recurring indicator/badge)
- ✅ `/invoices/new/page.tsx` (add recurring schedule section)

**Hooks**:
- ❌ `useRecurringInvoices.ts` (remove)
- ✅ `useInvoices.ts` (add recurring filter support)

**Types**:
- ❌ `recurring.ts` (remove or merge into invoice types)
- ✅ `invoice.ts` (add recurring fields)

**API Endpoints to Remove**:
- ❌ `GET /api/v1/recurring-invoices` (remove - recently added with Criteria-backed pagination)
- ❌ `GET /api/v1/recurring-invoices/{id}` (remove)
- ❌ `POST /api/v1/recurring-invoices` (remove)
- ❌ `PATCH /api/v1/recurring-invoices/{id}/pause` (remove)
- ❌ `PATCH /api/v1/recurring-invoices/{id}/resume` (remove)
- ❌ `PATCH /api/v1/recurring-invoices/{id}/complete` (remove)

**API Endpoints to Add/Modify**:
- ✅ `POST /api/v1/invoices` (add recurring fields to request)
- ✅ `PUT /api/v1/invoices/{id}` (add recurring fields to request)
- ✅ `PATCH /api/v1/invoices/{id}/pause-recurring` (new)
- ✅ `PATCH /api/v1/invoices/{id}/resume-recurring` (new)
- ✅ `PATCH /api/v1/invoices/{id}/stop-recurring` (new)
- ✅ `GET /api/v1/invoices` (add `recurring=true` filter)

---

## Work Assessment

### Effort Estimate: **Medium-High** (8-12 hours)

**Breakdown**:
1. **Database Migration** (2-3 hours):
   - Create migration to add recurring fields to `invoices` table
   - Migrate existing template data to invoices
   - Drop `recurring_invoice_templates` and `template_line_items` tables
   - Update Flyway migration history

2. **Backend Domain Layer** (1-2 hours):
   - Add recurring fields to `Invoice` entity
   - Add behavior methods: `pauseRecurring()`, `resumeRecurring()`, `stopRecurring()`, `isRecurring()`, `calculateNextRecurringDate()`
   - Remove `RecurringInvoiceTemplate` and `TemplateLineItem` classes

3. **Backend Infrastructure** (1 hour):
   - Update `InvoiceRepository` with `findRecurringInvoicesReadyForGeneration()` query
   - Remove `RecurringInvoiceTemplateRepository`

4. **Backend Application Layer** (2-3 hours):
   - Refactor `RecurringInvoiceScheduledJob` to use `InvoiceRepository`
   - Remove all recurring template handlers/controllers
   - Update `CreateInvoiceHandler` to accept recurring fields
   - Add pause/resume/stop recurring handlers to `InvoiceController`

5. **Frontend** (2-3 hours):
   - Remove recurring invoice pages
   - Add recurring schedule section to create invoice form
   - Add recurring indicator/badge to invoice list
   - Add recurring filter to invoice list
   - Update `useInvoices` hook
   - Remove `useRecurringInvoices` hook

6. **Testing** (1 hour):
   - Test recurring invoice creation
   - Test scheduled job generates invoices
   - Test pause/resume/stop recurring
   - Test invoice list with recurring filter

---

## Detailed Requirements

### 1. Database Schema Changes

#### Add to `invoices` table:
```sql
ALTER TABLE invoices ADD COLUMN recurring_frequency frequency_enum NULL;
ALTER TABLE invoices ADD COLUMN recurring_next_date DATE NULL;
ALTER TABLE invoices ADD COLUMN recurring_end_date DATE NULL;
ALTER TABLE invoices ADD COLUMN recurring_auto_send BOOLEAN DEFAULT FALSE;
ALTER TABLE invoices ADD COLUMN recurring_template_name VARCHAR(255) NULL;

-- Index for scheduled job query
CREATE INDEX idx_invoices_recurring_next_date ON invoices(recurring_next_date) 
WHERE recurring_frequency IS NOT NULL AND recurring_next_date IS NOT NULL;
```

#### Migration Strategy:
1. Add new columns to `invoices` table (nullable)
2. Migrate data from `recurring_invoice_templates` to `invoices`:
   - Create invoice records for each template
   - Copy line items from `template_line_items` to `invoice_line_items`
   - Set recurring fields
3. Drop `recurring_invoice_templates` and `template_line_items` tables

### 2. Invoice Entity Changes

**Add Fields**:
```java
@Convert(converter = FrequencyConverter.class)
@Column(name = "recurring_frequency", nullable = true)
private Frequency recurringFrequency;

@Column(name = "recurring_next_date", nullable = true)
private LocalDate recurringNextDate;

@Column(name = "recurring_end_date", nullable = true)
private LocalDate recurringEndDate;

@Column(name = "recurring_auto_send", nullable = false)
private boolean recurringAutoSend = false;

@Column(name = "recurring_template_name", nullable = true, length = 255)
private String recurringTemplateName;
```

**Add Behavior Methods**:
```java
public boolean isRecurring() {
    return recurringFrequency != null;
}

public void pauseRecurring() {
    if (!isRecurring()) {
        throw new IllegalStateException("Invoice is not recurring");
    }
    this.recurringNextDate = null; // Paused
}

public void resumeRecurring() {
    if (!isRecurring() || recurringNextDate != null) {
        throw new IllegalStateException("Invoice recurring is not paused");
    }
    this.recurringNextDate = calculateNextRecurringDate(LocalDate.now());
}

public void stopRecurring() {
    if (!isRecurring()) {
        throw new IllegalStateException("Invoice is not recurring");
    }
    this.recurringFrequency = null;
    this.recurringNextDate = null;
    this.recurringEndDate = null;
}

public LocalDate calculateNextRecurringDate(LocalDate currentDate) {
    if (!isRecurring()) {
        return null;
    }
    if (recurringEndDate != null && currentDate.isAfter(recurringEndDate)) {
        return null; // Recurring ended
    }
    return switch (recurringFrequency) {
        case MONTHLY -> currentDate.plusMonths(1);
        case QUARTERLY -> currentDate.plusMonths(3);
        case ANNUALLY -> currentDate.plusYears(1);
    };
}

public Invoice generateNextRecurringInvoice(InvoiceNumber invoiceNumber, LocalDate issueDate, DomainEventPublisher eventPublisher) {
    if (!isRecurring()) {
        throw new IllegalStateException("Invoice is not recurring");
    }
    if (recurringNextDate == null) {
        throw new IllegalStateException("Recurring invoice is paused");
    }
    if (issueDate.isBefore(recurringNextDate)) {
        throw new IllegalArgumentException("Issue date must be >= recurringNextDate");
    }
    
    // Create new invoice with same line items
    Invoice newInvoice = Invoice.create(
        customerId,
        invoiceNumber,
        issueDate,
        calculateDueDate(issueDate),
        paymentTerms
    );
    
    // Copy line items
    for (LineItem lineItem : lineItems) {
        newInvoice.addLineItem(LineItem.create(
            lineItem.getDescription(),
            lineItem.getQuantity(),
            lineItem.getUnitPrice(),
            lineItem.getDiscountType(),
            lineItem.getDiscountValue(),
            lineItem.getTaxRate(),
            lineItem.getSortOrder()
        ));
    }
    
    // Auto-send if enabled
    if (recurringAutoSend) {
        newInvoice.markAsSent();
    }
    
    // Update next recurring date
    LocalDate nextDate = calculateNextRecurringDate(issueDate);
    this.recurringNextDate = nextDate;
    
    // Check if recurring ended
    if (recurringEndDate != null && nextDate != null && nextDate.isAfter(recurringEndDate)) {
        stopRecurring(); // Auto-stop when end date reached
    }
    
    // Publish event
    addDomainEvent(new RecurringInvoiceGeneratedEvent(
        id,
        recurringTemplateName != null ? recurringTemplateName : invoiceNumber.toString(),
        newInvoice.getId(),
        newInvoice.getInvoiceNumber().toString(),
        customerId,
        null, // customerName - set by listener
        null, // customerEmail - set by listener
        recurringNextDate,
        recurringAutoSend,
        issueDate
    ));
    
    return newInvoice;
}
```

### 3. Scheduled Job Refactoring

**Current**: Finds templates, generates invoices  
**New**: Finds recurring invoices, generates new invoices

```java
@Scheduled(cron = "0 0 * * * ?", zone = "America/Chicago")
@Transactional
public void generateRecurringInvoices() {
    LocalDate today = LocalDate.now();
    
    // Find all recurring invoices ready for generation
    List<Invoice> recurringInvoices = invoiceRepository
        .findRecurringInvoicesReadyForGeneration(today);
    
    for (Invoice templateInvoice : recurringInvoices) {
        try {
            InvoiceNumber invoiceNumber = invoiceNumberGenerator.generateNext();
            Invoice newInvoice = templateInvoice.generateNextRecurringInvoice(
                invoiceNumber, 
                today, 
                eventPublisher
            );
            
            invoiceRepository.save(newInvoice);
            invoiceRepository.save(templateInvoice); // Update next date
            
            eventPublisher.publishEvents(newInvoice);
            eventPublisher.publishEvents(templateInvoice);
        } catch (Exception e) {
            log.error("Failed to generate recurring invoice from {}", templateInvoice.getId(), e);
        }
    }
}
```

### 4. API Changes

**Create Invoice Request** (add optional recurring fields):
```json
{
  "customerId": "uuid",
  "lineItems": [...],
  "paymentTerms": "NET_30",
  "recurringFrequency": "MONTHLY",  // NEW: optional
  "recurringEndDate": "2025-12-31",  // NEW: optional
  "recurringAutoSend": true,          // NEW: optional
  "recurringTemplateName": "Monthly Subscription"  // NEW: optional
}
```

**New Endpoints**:
- `PATCH /api/v1/invoices/{id}/pause-recurring` - Pause recurring schedule
- `PATCH /api/v1/invoices/{id}/resume-recurring` - Resume recurring schedule
- `PATCH /api/v1/invoices/{id}/stop-recurring` - Stop recurring schedule

**List Invoices** (add filter):
- `GET /api/v1/invoices?recurring=true` - Filter to only recurring invoices

### 5. Frontend Changes

**Create Invoice Form** (add recurring section):
- Checkbox: "Make this a recurring invoice"
- If checked, show:
  - Frequency selector (Monthly, Quarterly, Annually)
  - Start date (default: today)
  - End date (optional)
  - Auto-send checkbox
  - Template name (optional)

**Invoice List**:
- Add recurring badge/indicator for recurring invoices
- Add "Recurring" filter option
- Show next generation date for recurring invoices

**Invoice Detail**:
- Show recurring schedule information (if recurring)
- Show pause/resume/stop buttons (if recurring, RBAC enforced)

---

## Migration Plan

### Phase 1: Database Migration
1. Create migration `V13__add_recurring_fields_to_invoices.sql`
2. Add recurring columns to `invoices` table
3. Migrate data from templates to invoices
4. Create index for scheduled job query

### Phase 2: Backend Refactoring
1. Update `Invoice` entity with recurring fields and methods
2. Update `InvoiceRepository` with recurring query
3. Refactor scheduled job
4. Remove template handlers/controllers
5. Update create/update invoice handlers

### Phase 3: Frontend Refactoring
1. Remove recurring invoice pages
2. Add recurring section to create invoice form
3. Add recurring indicator to invoice list
4. Update hooks and types

### Phase 4: Cleanup
1. Drop `recurring_invoice_templates` table
2. Drop `template_line_items` table
3. Remove unused code
4. Update documentation

---

## Risks & Mitigations

### Risk 1: Data Loss During Migration
**Mitigation**: 
- Test migration on copy of production data first
- Create backup before migration
- Verify all template data migrated correctly

### Risk 2: Breaking Existing Recurring Invoices
**Mitigation**:
- Ensure scheduled job continues working during migration
- Test scheduled job after migration
- Monitor for any missed invoice generations

### Risk 3: Frontend Breaking Changes
**Mitigation**:
- Deploy backend first, then frontend
- Keep old endpoints temporarily (deprecated) if needed
- Test thoroughly before removing old code

---

## Success Criteria

- ✅ No separate template aggregate (simplified architecture)
- ✅ Recurring invoices created as regular invoices with recurring fields
- ✅ Scheduled job generates invoices from recurring invoices
- ✅ Frontend shows recurring invoices in regular invoice list
- ✅ All existing functionality preserved (pause, resume, stop)
- ✅ No data loss during migration
- ✅ All tests passing

---

## Alternatives Considered

### Alternative 1: Keep Current Architecture
**Pros**: No refactoring needed  
**Cons**: Code duplication, maintenance burden, complexity

### Alternative 2: Hybrid Approach
**Pros**: Gradual migration  
**Cons**: More complex, two systems to maintain

### Alternative 3: Proposed Solution (Flag-based)
**Pros**: Simpler, unified, less code  
**Cons**: Requires refactoring effort

**Recommendation**: Proceed with Alternative 3 (flag-based) for long-term simplicity.

---

## Decision Required

**Question**: Should we proceed with this refactoring?

**Considerations**:
- Current system is working
- Refactoring requires 8-12 hours of work
- Simplifies architecture long-term
- Reduces maintenance burden
- May introduce bugs during migration

**Recommendation**: Proceed if time permits, or defer to post-M3 if timeline is tight.

---

**PRD Status**: ✅ **READY FOR EXECUTION** - Detailed requirements and migration plan provided

