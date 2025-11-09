# Recurring Invoices Refactoring Agent Prompt

**Date**: 2025-01-27  
**Agent Type**: Backend/Frontend Refactoring Agent  
**Priority**: Medium-High  
**Estimated Effort**: 8-12 hours

---

## Context & Background

The InvoiceMe ERP system currently implements recurring invoices using a separate `RecurringInvoiceTemplate` aggregate with its own `TemplateLineItem` entities, repository, handlers, controllers, and frontend pages. This architecture creates significant code duplication—template line items duplicate invoice line items, template handlers duplicate invoice handlers, and template pages duplicate invoice pages. The system maintains two separate aggregates (`Invoice` and `RecurringInvoiceTemplate`) for essentially the same concept, requiring changes to invoice structure to be made in two places, which increases maintenance burden and allows template and invoice logic to drift apart over time. The current implementation includes a `recurring_invoice_templates` table (created in migration V6), a `template_line_items` table, separate repositories, 6 API endpoints, 3 frontend pages, and a scheduled job that finds templates and generates invoices.

## Refactoring Goal

We want to simplify this architecture by eliminating the separate template aggregate and instead adding recurring schedule fields directly to the `Invoice` entity. When `recurringFrequency` is null, the invoice is a regular one-time invoice; when `recurringFrequency` is set (MONTHLY, QUARTERLY, ANNUALLY), the invoice becomes a recurring template that generates new invoices. This approach will reduce code complexity, eliminate duplication, unify invoice management, and make the system easier to understand and maintain. The refactoring involves adding 5 fields to the `invoices` table (`recurring_frequency`, `recurring_next_date`, `recurring_end_date`, `recurring_auto_send`, `recurring_template_name`), migrating existing template data to invoices, removing the separate template tables and code, and refactoring the scheduled job to work with recurring invoices instead of templates. This will result in a net reduction of approximately 2,150 lines of code while preserving all existing functionality (pause, resume, stop, generate).

## Execution Instructions

Please review the detailed requirements and migration plan in `/docs/refactoring/RECURRING_INVOICES_REFACTOR_PRD.md` and the work assessment in `/docs/refactoring/RECURRING_REFACTOR_ASSESSMENT.md`. The PRD contains complete database schema changes, entity modifications, API endpoint changes, frontend updates, migration strategy, and success criteria. Flyway is already configured in the project (`spring.flyway.enabled=true` in `application.yml`), so you should create a new migration file `V13__add_recurring_fields_to_invoices.sql` (or the next sequential version) in `/backend/src/main/resources/db/migration/` that adds the recurring columns to the `invoices` table, migrates existing template data, creates the necessary index, and then drops the old template tables. Follow the migration plan in the PRD (Phase 1: Database Migration → Phase 2: Backend Refactoring → Phase 3: Frontend Refactoring → Phase 4: Cleanup), test thoroughly at each phase, and ensure all existing functionality is preserved. The refactoring should be done incrementally and carefully to avoid data loss or breaking changes—test the migration on a copy of production data first, verify the scheduled job continues working, and ensure frontend changes don't break existing invoice management workflows.

---

**Reference Documents**:
- `/docs/refactoring/RECURRING_INVOICES_REFACTOR_PRD.md` - Complete PRD with detailed requirements
- `/docs/refactoring/RECURRING_REFACTOR_ASSESSMENT.md` - Work assessment and code reduction estimates

**Key Files to Review**:
- `backend/src/main/java/com/invoiceme/domain/recurring/RecurringInvoiceTemplate.java` - Current template aggregate (to be removed)
- `backend/src/main/java/com/invoiceme/domain/invoice/Invoice.java` - Invoice entity (to be enhanced)
- `backend/src/main/java/com/invoiceme/infrastructure/scheduled/RecurringInvoiceScheduledJob.java` - Scheduled job (to be refactored)
- `backend/src/main/resources/db/migration/V6__create_recurring_invoice_templates_table.sql` - Current template migration (to be reversed)
- `frontend/src/app/recurring-invoices/` - Frontend pages (to be removed/merged)

**Success Criteria**: All criteria listed in the PRD must be met, including no separate template aggregate, recurring invoices created as regular invoices with recurring fields, scheduled job generates invoices from recurring invoices, frontend shows recurring invoices in regular invoice list, all existing functionality preserved, no data loss during migration, and all tests passing.

