# InvoiceMe Changelog

## [Unreleased] - 2025-01-27

### Critical Bug Fixes

#### Money Class Currency Initialization (Backend)
- **File**: `backend/src/main/java/com/invoiceme/domain/common/Money.java`
- **Problem**: Currency field was null for newly created Money objects, causing Jackson serialization errors with "Cannot invoke Object.hashCode() because key is null"
- **Fix**: Initialize currency field to `DEFAULT_CURRENCY.getCurrencyCode()` at declaration instead of relying on `@PostLoad`
- **Impact**: Fixed 500 errors when creating invoices

#### CreateInvoiceMapper Improvements (Backend)
- **File**: `backend/src/main/java/com/invoiceme/invoices/createinvoice/CreateInvoiceMapper.java`
- **Fix**: Added explicit `@Mapping` annotations to properly handle `invoiceNumber` (using custom converter) and `customerName` (ignore, not in entity)
- **Impact**: Fixed serialization issues after invoice creation

### Features

#### Invoice Creation Success Modal (Frontend)
- **Files**: `frontend/app/invoices/new/page.tsx`, `frontend/src/app/invoices/new/page.tsx`
- **Added**: Success dialog after invoice creation with three options:
  - **Send to Customer** - Marks as sent, triggers AWS SES email
  - **View Invoice** - Navigate to detail page
  - **Create Another** - Refresh form
- **Implementation**: Added `useMarkInvoiceAsSent` hook integration, state management for modal and created invoice ID, handler functions for all three actions
- **Impact**: Users can now send invoices immediately after creation instead of navigating to detail page

#### Invoice Edit Page (Frontend)
- **File**: `frontend/app/invoices/[id]/edit/page.tsx` (new file)
- **Features**:
  - Full invoice editing form (pre-populated with existing data)
  - Only accessible for DRAFT invoices
  - Same structure as create form (line items, calculations, etc.)
  - Success modal with Send/View/Continue Editing options
  - Permission checks using `canEditInvoice()`
- **Route**: `/invoices/[id]/edit`
- **Integration**: Uses `useInvoice()` to fetch existing data, `useUpdateInvoice()` to save changes, `useMarkInvoiceAsSent()` for sending

### Bug Fixes

#### Select Component Controlled/Uncontrolled Warnings

**Customer Edit Page**
- **Files**: `frontend/app/customers/[id]/edit/page.tsx`, `frontend/src/app/customers/[id]/edit/page.tsx`
- **Problem**: `customerType` Select was switching from uncontrolled to controlled during data loading
- **Fix**: Changed `value={customerType}` to `value={customerType || 'COMMERCIAL'}` to always have a defined value
- **Impact**: Eliminated React warning in console

**Invoice Edit Page**
- **File**: `frontend/app/invoices/[id]/edit/page.tsx`
- **Problem**: Similar controlled/uncontrolled warnings for `customerId` Select
- **Fix**: Added check to not render form until invoice data is loaded (`!invoice` in loading check), used `customerId || invoice.customerId` as fallback to ensure always-defined value, added `customerId` to watched form values
- **Impact**: Eliminated React warnings on edit page

### Documentation

#### AWS SES Email Setup Guide
- **File**: `docs/deployment/AWS_SES_EMAIL_SETUP.md`
- **Contents**:
  - Step-by-step IAM credentials creation
  - Email verification process for sandbox mode
  - Environment variable configuration (local, Elastic Beanstalk, Vercel)
  - Testing procedures
  - Moving out of sandbox mode (production access)
  - Email templates documentation
  - Cost estimates and monitoring
  - Security best practices
- **Impact**: Complete reference for setting up email functionality (currently not configured)

### Code Cleanup

- Removed redundant file copies
- Deleted redundant `frontend/src/app/invoices/new/page.tsx` that was accidentally copied
- Clarified that `frontend/app/` is the primary directory, not `frontend/src/app/`

### Git Commits

- `490ebce` - "fix: Prevent Select component from switching between controlled/uncontrolled state" (customer edit)
- `29421fc` - "feat: Add invoice creation modal, edit functionality, and email setup docs"
- `b3abb09` - "fix: Prevent Select controlled/uncontrolled warning on edit invoice page" (first attempt)
- `779ad89` - "fix: Ensure invoice data is fully loaded before rendering edit form" (complete fix)
- `eeadb84` - "feat: Add success modal to edit invoice page"

---

## Previous Changes

See individual milestone and status documents in `docs/milestones/` and `qa/results/` for historical changes.

