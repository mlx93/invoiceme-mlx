# Recurring Invoices Refactoring - Work Assessment

**Date**: 2025-01-27  
**Question**: Do we need a separate RecurringInvoiceTemplate module, or should we use a flag on regular invoices?

---

## Current Architecture Analysis

### What Exists

**Backend**:
- `RecurringInvoiceTemplate` aggregate (separate entity)
- `TemplateLineItem` entity (separate from `LineItem`)
- `RecurringInvoiceTemplateRepository` (separate repository)
- Vertical slices: Create, Get, List, Pause, Resume, Complete handlers/controllers
- Scheduled job: `RecurringInvoiceScheduledJob` finds templates and generates invoices

**Frontend**:
- 3 pages: List, Detail, Create
- Hook: `useRecurringInvoices` with pause/resume/complete actions
- Types: `recurring.ts` with template types

**Database**:
- `recurring_invoice_templates` table
- `template_line_items` table
- Foreign keys and indexes

**API Endpoints**: 6 endpoints for recurring templates

---

## Proposed Architecture

### Simplified Approach

**Add to Invoice entity**:
- `recurringFrequency` (MONTHLY, QUARTERLY, ANNUALLY, null = not recurring)
- `recurringNextDate` (next generation date)
- `recurringEndDate` (optional end date)
- `recurringAutoSend` (boolean)
- `recurringTemplateName` (optional name)

**Logic**:
- When `recurringFrequency` is null → Regular invoice
- When `recurringFrequency` is set → Recurring invoice (template)

**Scheduled Job**:
- Finds invoices where `recurringFrequency IS NOT NULL AND recurringNextDate <= today`
- Generates new invoice from recurring invoice
- Updates `recurringNextDate` on template invoice

---

## Work Assessment

### Effort: **Medium-High** (8-12 hours)

### Breakdown

#### 1. Database Migration (2-3 hours)
**Tasks**:
- Create migration to add 5 columns to `invoices` table
- Migrate existing template data to invoices
- Migrate template line items to invoice line items
- Drop `recurring_invoice_templates` table
- Drop `template_line_items` table
- Create index for scheduled job query

**Complexity**: Medium (data migration requires careful mapping)

#### 2. Backend Domain Layer (1-2 hours)
**Tasks**:
- Add recurring fields to `Invoice` entity
- Add behavior methods: `pauseRecurring()`, `resumeRecurring()`, `stopRecurring()`, `isRecurring()`, `calculateNextRecurringDate()`, `generateNextRecurringInvoice()`
- Remove `RecurringInvoiceTemplate` class
- Remove `TemplateLineItem` class

**Complexity**: Low-Medium (straightforward additions)

#### 3. Backend Infrastructure (1 hour)
**Tasks**:
- Add `findRecurringInvoicesReadyForGeneration()` query to `InvoiceRepository`
- Remove `RecurringInvoiceTemplateRepository`

**Complexity**: Low (simple query addition)

#### 4. Backend Application Layer (2-3 hours)
**Tasks**:
- Refactor `RecurringInvoiceScheduledJob` to use `InvoiceRepository`
- Remove all recurring template handlers (5+ handlers)
- Remove recurring template controllers
- Update `CreateInvoiceHandler` to accept recurring fields
- Add pause/resume/stop recurring handlers to `InvoiceController`

**Complexity**: Medium (refactoring scheduled job, removing code)

#### 5. Frontend (2-3 hours)
**Tasks**:
- Remove 3 recurring invoice pages
- Add recurring schedule section to create invoice form
- Add recurring indicator/badge to invoice list
- Add recurring filter to invoice list
- Update `useInvoices` hook
- Remove `useRecurringInvoices` hook
- Update types (remove `recurring.ts`, add to `invoice.ts`)

**Complexity**: Medium (UI changes, hook updates)

#### 6. Testing & Cleanup (1 hour)
**Tasks**:
- Test recurring invoice creation
- Test scheduled job
- Test pause/resume/stop
- Remove unused code
- Update documentation

**Complexity**: Low-Medium

---

## Code Reduction Estimate

### Backend Code Removed
- ~500 lines (RecurringInvoiceTemplate aggregate)
- ~200 lines (TemplateLineItem entity)
- ~300 lines (Repository)
- ~800 lines (Handlers/Controllers)
- **Total**: ~1,800 lines removed

### Backend Code Added
- ~100 lines (recurring fields in Invoice)
- ~150 lines (behavior methods in Invoice)
- ~50 lines (repository query)
- ~100 lines (scheduled job refactor)
- ~200 lines (pause/resume/stop handlers)
- **Total**: ~600 lines added

**Net Reduction**: ~1,200 lines of code

### Frontend Code Removed
- ~900 lines (3 pages)
- ~300 lines (useRecurringInvoices hook)
- ~100 lines (recurring types)
- **Total**: ~1,300 lines removed

### Frontend Code Added
- ~200 lines (recurring section in create form)
- ~100 lines (recurring indicator in list)
- ~50 lines (recurring filter)
- **Total**: ~350 lines added

**Net Reduction**: ~950 lines of code

**Total Net Reduction**: ~2,150 lines of code

---

## Benefits

### Architectural
- ✅ Simpler domain model (one aggregate instead of two)
- ✅ Less code duplication
- ✅ Unified invoice management
- ✅ Easier to understand and maintain

### Functional
- ✅ Same functionality (pause, resume, stop, generate)
- ✅ Recurring invoices visible in regular invoice list
- ✅ Single source of truth for invoices

### Maintenance
- ✅ Less code to maintain
- ✅ Changes to invoice structure only need one place
- ✅ Fewer tests to write/maintain

---

## Risks

### Migration Risk
- **Risk**: Data loss during migration
- **Mitigation**: Test migration on copy, create backup, verify data

### Breaking Changes
- **Risk**: Existing recurring invoices break
- **Mitigation**: Careful migration, test scheduled job, monitor

### Timeline
- **Risk**: 8-12 hours of work
- **Mitigation**: Can be done incrementally, defer if timeline tight

---

## Recommendation

**Proceed with refactoring** if:
- ✅ Time permits (8-12 hours available)
- ✅ System is stable (no critical bugs)
- ✅ Long-term simplicity is valued

**Defer refactoring** if:
- ⚠️ Timeline is tight (M3 deadline approaching)
- ⚠️ System has critical issues
- ⚠️ Risk tolerance is low

**Alternative**: Defer to post-M3 if timeline is tight, but document as technical debt to address later.

---

## PRD Location

**Full PRD**: `/docs/refactoring/RECURRING_INVOICES_REFACTOR_PRD.md`

**Contains**:
- Detailed requirements
- Database schema changes
- Code changes
- Migration plan
- Success criteria

---

**Assessment Status**: ✅ **COMPLETE** - Ready for decision and execution

