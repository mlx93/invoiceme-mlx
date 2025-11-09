# Recurring Invoices Refactoring - Readiness Checklist

**Date**: 2025-01-27  
**Status**: ✅ **READY FOR EXECUTION**  
**Decision**: Proceed with refactoring

---

## ✅ Pre-Refactoring Status

### Documentation Ready
- ✅ **PRD**: `/docs/refactoring/RECURRING_INVOICES_REFACTOR_PRD.md` - Complete with detailed requirements, migration plan, and success criteria
- ✅ **Work Assessment**: `/docs/refactoring/RECURRING_REFACTOR_ASSESSMENT.md` - Effort estimates and code reduction analysis
- ✅ **Agent Prompt**: `/docs/refactoring/REFACTORING_AGENT_PROMPT.md` - 3-paragraph intro with execution instructions

### Current Implementation Status
- ✅ **Backend**: Recurring templates fully implemented with:
  - `RecurringInvoiceTemplate` aggregate
  - `TemplateLineItem` entity
  - `RecurringInvoiceTemplateRepository` + `RecurringInvoiceTemplateRepositoryCustom/Impl` (Criteria-backed)
  - Vertical slices: Create, Get, List, Pause, Resume, Complete handlers
  - `GET /api/v1/recurring-invoices` endpoint (recently added)
  - Scheduled job: `RecurringInvoiceScheduledJob`
- ✅ **Frontend**: 3 pages for recurring invoices (list, detail, create)
- ✅ **Database**: `recurring_invoice_templates` and `template_line_items` tables (V6 migration)

### Infrastructure Ready
- ✅ **Flyway**: Configured and working (`spring.flyway.enabled=true`)
- ✅ **Database**: PostgreSQL with Supabase connection
- ✅ **Migration Pattern**: Sequential versions (currently at V10, next will be V13)

---

## 📋 Refactoring Scope

### Code to Remove (~2,150 lines)
- **Backend Domain**: `RecurringInvoiceTemplate.java`, `TemplateLineItem.java`
- **Backend Infrastructure**: `RecurringInvoiceTemplateRepository.java`, `RecurringInvoiceTemplateRepositoryCustom.java`, `RecurringInvoiceTemplateRepositoryImpl.java`
- **Backend Application**: All handlers in `recurringinvoices/` package (including `ListRecurringTemplatesQuery/Handler/Mapper`)
- **Backend Controllers**: `RecurringInvoiceTemplateController` and all recurring endpoints
- **Backend DTOs**: `RecurringTemplateDto`, `PagedRecurringTemplateResponse`
- **Frontend Pages**: `/recurring-invoices/` directory (3 pages)
- **Frontend Hooks**: `useRecurringInvoices.ts`
- **Frontend Types**: `recurring.ts`

### Code to Add (~600 lines)
- **Backend Domain**: 5 recurring fields + behavior methods in `Invoice.java`
- **Backend Infrastructure**: `findRecurringInvoicesReadyForGeneration()` query in `InvoiceRepository`
- **Backend Application**: Pause/resume/stop recurring handlers in `InvoiceController`
- **Backend Scheduled Job**: Refactor to use `InvoiceRepository` instead of template repository
- **Frontend**: Recurring section in create invoice form, recurring indicator in list, recurring filter

### Database Changes
- **Add to `invoices` table**: 5 new columns (recurring_frequency, recurring_next_date, recurring_end_date, recurring_auto_send, recurring_template_name)
- **Migrate data**: From `recurring_invoice_templates` → `invoices`, from `template_line_items` → `invoice_line_items`
- **Drop tables**: `recurring_invoice_templates`, `template_line_items`
- **Create index**: `idx_invoices_recurring_next_date` for scheduled job query

---

## 🎯 Execution Plan (4 Phases)

### Phase 1: Database Migration
1. Create `V13__add_recurring_fields_to_invoices.sql` migration
2. Add recurring columns to `invoices` table (nullable)
3. Migrate existing template data to invoices
4. Migrate template line items to invoice line items
5. Create index for scheduled job query
6. **Test**: Verify data migration, check for data loss

### Phase 2: Backend Refactoring
1. Add recurring fields to `Invoice` entity
2. Add behavior methods: `pauseRecurring()`, `resumeRecurring()`, `stopRecurring()`, `isRecurring()`, `calculateNextRecurringDate()`, `generateNextRecurringInvoice()`
3. Update `InvoiceRepository` with `findRecurringInvoicesReadyForGeneration()` query
4. Refactor `RecurringInvoiceScheduledJob` to use `InvoiceRepository`
5. Update `CreateInvoiceHandler` to accept recurring fields
6. Add pause/resume/stop recurring handlers to `InvoiceController`
7. Remove template aggregate, repository, handlers, controllers, DTOs
8. **Test**: Verify scheduled job works, test pause/resume/stop, verify API endpoints

### Phase 3: Frontend Refactoring
1. Remove recurring invoice pages (`/recurring-invoices/`)
2. Add recurring schedule section to create invoice form
3. Add recurring indicator/badge to invoice list
4. Add recurring filter to invoice list (`?recurring=true`)
5. Update `useInvoices` hook
6. Remove `useRecurringInvoices` hook and `recurring.ts` types
7. **Test**: Verify UI works, test recurring invoice creation, test filters

### Phase 4: Cleanup
1. Drop `recurring_invoice_templates` table (in migration or separate)
2. Drop `template_line_items` table
3. Remove unused code
4. Update documentation
5. **Test**: Full E2E test, verify no regressions

---

## ⚠️ Risk Mitigation

### Data Loss Prevention
- ✅ Test migration on copy of production data first
- ✅ Create database backup before migration
- ✅ Verify all template data migrated correctly

### Breaking Changes Prevention
- ✅ Deploy backend first, then frontend
- ✅ Keep old endpoints temporarily (deprecated) if needed
- ✅ Test thoroughly before removing old code

### Scheduled Job Continuity
- ✅ Ensure scheduled job continues working during migration
- ✅ Test scheduled job after migration
- ✅ Monitor for any missed invoice generations

---

## ✅ Success Criteria

- [ ] No separate template aggregate (simplified architecture)
- [ ] Recurring invoices created as regular invoices with recurring fields
- [ ] Scheduled job generates invoices from recurring invoices
- [ ] Frontend shows recurring invoices in regular invoice list
- [ ] All existing functionality preserved (pause, resume, stop)
- [ ] No data loss during migration
- [ ] All tests passing
- [ ] ~2,150 lines of code removed (net reduction)

---

## 📝 Notes for Refactoring Agent

1. **Recent Work**: A debugging agent recently (2025-01-27) added `RecurringInvoiceTemplateRepositoryCustom/Impl`, `ListRecurringTemplatesQuery/Handler/Mapper`, and `GET /api/v1/recurring-invoices` endpoint. This code will be removed as part of the refactoring.

2. **Migration Version**: Check current Flyway migrations to determine next version number (likely V13, but verify).

3. **Testing**: Test incrementally at each phase. Don't proceed to next phase until current phase is fully tested and verified.

4. **Documentation**: Update any documentation that references recurring templates after refactoring is complete.

---

**Status**: ✅ **ALL SYSTEMS GO** - Ready for refactoring agent execution

