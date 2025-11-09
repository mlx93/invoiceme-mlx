# Remove Recurring Invoices Module - Agent Prompt

**Date**: 2025-01-27  
**Agent Type**: Backend/Frontend Cleanup Agent  
**Priority**: Medium  
**Estimated Effort**: 2-3 hours

---

## Agent Introduction

The InvoiceMe assessment document (`InvoiceMe.md`) only requires Customer, Invoice, and Payment CRUD operations—recurring invoices were added as an extended feature but are not required for the core assessment. We've decided to remove the entire recurring invoices module to simplify the codebase and focus on core requirements, as it's an isolated module with minimal dependencies on core functionality. Your task is to completely remove all recurring invoice code from backend and frontend, create a database migration to drop the tables, and verify that core features (Customer → Invoice → Payment flow) still work correctly after removal.

---

## Context & Background

The InvoiceMe assessment document (`InvoiceMe.md`) only requires **Customer, Invoice, and Payment** CRUD operations. Recurring invoices were added as an extended feature but are **NOT required** for the core assessment. The recurring invoice module adds unnecessary complexity and maintenance burden without contributing to the core requirements.

**Decision**: Remove the entire recurring invoices module to simplify the codebase and focus on core requirements.

---

## Removal Goal

Completely remove all recurring invoice functionality from both backend and frontend, including:
- Domain aggregates and entities
- Repositories and database tables
- API endpoints and handlers
- Frontend pages and components
- Scheduled jobs related to recurring invoices
- Domain events related to recurring invoices

**Result**: Cleaner codebase focused on core requirements (Customer, Invoice, Payment CRUD).

---

## Execution Instructions

### Phase 1: Backend Removal

**Domain Layer**:
- ❌ Delete `backend/src/main/java/com/invoiceme/domain/recurring/RecurringInvoiceTemplate.java`
- ❌ Delete `backend/src/main/java/com/invoiceme/domain/recurring/TemplateLineItem.java`
- ❌ Remove `RecurringInvoiceGeneratedEvent` from domain events (if exists)

**Infrastructure Layer**:
- ❌ Delete `backend/src/main/java/com/invoiceme/infrastructure/persistence/RecurringInvoiceTemplateRepository.java`
- ❌ Delete `backend/src/main/java/com/invoiceme/infrastructure/persistence/RecurringInvoiceTemplateRepositoryCustom.java`
- ❌ Delete `backend/src/main/java/com/invoiceme/infrastructure/persistence/RecurringInvoiceTemplateRepositoryImpl.java`
- ❌ Remove recurring invoice scheduled job: `backend/src/main/java/com/invoiceme/infrastructure/scheduled/RecurringInvoiceScheduledJob.java`
- ❌ Remove any references to recurring invoices in other scheduled jobs

**Application Layer**:
- ❌ Delete entire `backend/src/main/java/com/invoiceme/recurring/` package (all handlers, controllers, DTOs)
- ❌ Remove any imports/references to recurring invoice classes in other handlers

**Database**:
- ❌ Create migration `V13__drop_recurring_invoice_tables.sql`:
  ```sql
  -- Drop foreign key constraints first
  ALTER TABLE template_line_items DROP CONSTRAINT IF EXISTS fk_template_line_items_template;
  
  -- Drop tables
  DROP TABLE IF EXISTS template_line_items;
  DROP TABLE IF EXISTS recurring_invoice_templates;
  
  -- Drop enum types (if not used elsewhere)
  DROP TYPE IF EXISTS frequency_enum;
  DROP TYPE IF EXISTS template_status_enum;
  ```

**Configuration**:
- ❌ Remove any recurring invoice references from `application.yml`
- ❌ Remove recurring invoice endpoints from OpenAPI spec (`backend/docs/api/openapi.yaml`)

### Phase 2: Frontend Removal

**Pages**:
- ❌ Delete `frontend/src/app/recurring-invoices/` directory (entire directory)
  - `page.tsx` (list page)
  - `[id]/page.tsx` (detail page)
  - `new/page.tsx` (create page)

**Hooks/ViewModels**:
- ❌ Delete `frontend/src/hooks/useRecurringInvoices.ts`

**Types**:
- ❌ Delete `frontend/src/types/recurring.ts` (or remove recurring types from it)

**Navigation**:
- ❌ Remove recurring invoices links from navigation components
- ❌ Remove recurring invoices menu items

**Components**:
- ❌ Remove any recurring invoice-specific components

### Phase 3: Documentation & Cleanup

**Documentation**:
- ❌ Remove recurring invoice references from:
  - `backend/docs/domain-aggregates.md`
  - `backend/docs/api/openapi.yaml`
  - `backend/docs/events.md`
  - `docs/FEATURES.md` (update to show removed)
  - Any other documentation files

**Tests**:
- ❌ Remove recurring invoice test cases
- ❌ Update integration tests to remove recurring invoice scenarios

**Migration**:
- ✅ Create Flyway migration to drop tables (V13)
- ✅ Test migration locally before committing

---

## Files to Remove (Complete List)

### Backend
```
backend/src/main/java/com/invoiceme/domain/recurring/
backend/src/main/java/com/invoiceme/infrastructure/persistence/RecurringInvoiceTemplateRepository*.java
backend/src/main/java/com/invoiceme/infrastructure/scheduled/RecurringInvoiceScheduledJob.java
backend/src/main/java/com/invoiceme/recurring/
backend/src/main/java/com/invoiceme/domain/events/RecurringInvoiceGeneratedEvent.java (if exists)
```

### Frontend
```
frontend/src/app/recurring-invoices/
frontend/src/hooks/useRecurringInvoices.ts
frontend/src/types/recurring.ts
```

### Database
```
Migration: V13__drop_recurring_invoice_tables.sql
```

---

## Verification Steps

After removal, verify:

1. **Backend Compiles**: `cd backend && mvn clean compile` - should succeed
2. **Frontend Builds**: `cd frontend && npm run build` - should succeed
3. **No Broken Imports**: Search codebase for "RecurringInvoice" - should find no references
4. **Database Migration**: Test migration locally, verify tables dropped
5. **Core Features Work**: Test Customer → Invoice → Payment flow still works
6. **No Navigation Errors**: Frontend navigation should not reference recurring invoices

---

## Risk Assessment

**Risk Level**: 🟢 **LOW**

**Why Low Risk**:
- Recurring invoices are isolated module (separate aggregate, repository, handlers)
- Core features (Customer, Invoice, Payment) are independent
- No shared code dependencies (recurring uses its own tables, entities, endpoints)
- Easy to verify removal (compile/build should catch any broken references)

**Mitigation**:
- Test compilation/build after each phase
- Verify core Customer → Invoice → Payment flow still works
- Check for any lingering references in codebase

---

## Success Criteria

- ✅ All recurring invoice code removed
- ✅ Database tables dropped via migration
- ✅ Backend compiles successfully
- ✅ Frontend builds successfully
- ✅ Core features (Customer, Invoice, Payment) still work
- ✅ No broken imports or references
- ✅ Documentation updated
- ✅ Migration tested and ready

---

**Status**: ✅ **READY FOR EXECUTION** - Low risk, isolated module, straightforward removal

