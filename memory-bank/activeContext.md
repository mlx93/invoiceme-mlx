# Active Context

## Current Status: Customer Experience Refinements Complete ✅

**Last Updated:** November 9, 2025

## Recently Completed Work

### Payment Table Fix (Latest)
- **Problem:** Invoice Number and Customer columns empty in payments table
- **Root Cause:** Backend PaymentController was setting these fields to `null`
- **Solution:** Modified PaymentController to fetch and populate Invoice and Customer data
- **Files Changed:** `backend/src/main/java/com/invoiceme/payments/PaymentController.java`
- **Status:** Complete - payments table now displays full information

### Customer User Experience Improvements
1. **Auto-Create Customer Users:** Admins/Accountants creating customers now auto-generate ACTIVE user accounts with password "test1234"
2. **Customer Dashboard Redirect:** Fixed React error by moving redirect logic to useEffect
3. **Customer-Specific Invoice View:** Customers only see their own invoices (filtered by customerId)
4. **"Pay Invoice" Terminology:** Changed all "Record Payment" text to "Pay Invoice" for customer role
5. **Address Validation Fix:** Made street address optional by checking all fields before creating Address object

### Update Invoice Button Fix
Fixed 4 critical bugs preventing invoice updates:
- Missing version field for optimistic locking
- NaN validation errors from null handling
- Line items removal order violation
- Null ID comparison crashes

## Active Decisions

### UI Improvements Pending
Created agent prompt for UI enhancements:
- Sleeker header with better active tab indication
- Left-aligned navigation tabs
- Clickable invoice rows (remove Actions column)
- Show company name in Customer column
- Better formatting for Aging Report and Revenue Trend chart

**Agent Prompt:** `AGENT_PROMPTS/UI_IMPROVEMENTS_AGENT_PROMPT.md`

### Email Notifications Deferred
- Decision: Hold off on email implementation
- Action Taken: Removed email notification references from UI
- Files Updated: Invoice sent dialogs, user approval dialogs

### Recurring Invoices Removed
- Decision: Removed recurring invoice module (not core requirement)
- Rationale: Simplifies codebase, reduces complexity
- Status: Complete - all recurring invoice code deleted

## Technical Context

### Deployment Architecture
- **Backend:** AWS Elastic Beanstalk (Java 17 Spring Boot) ✅
- **Frontend:** Vercel (Next.js SSR) ✅  
- **Database:** Supabase PostgreSQL (Connection Pooler) ✅

### Key Integration Patterns
- Next.js rewrites proxy API calls to avoid mixed content issues
- Supabase Connection Pooler required for AWS external connections
- Flyway disabled for production (SPRING_FLYWAY_ENABLED=false)

### Authentication & Authorization
- JWT tokens (24-hour expiry, HS512, 64-character secret)
- RBAC: SysAdmin, Accountant, Sales, Customer
- Customer role auto-created with ACTIVE status and default password

## Next Steps

1. **UI Agent Execution:** Address header, navigation, and chart formatting improvements
2. **Manual Testing:** Follow checklist in `docs/MANUAL_TESTING_CHECKLIST.md`
3. **PDF Generation:** Implement invoice PDF generation (prompt created)
4. **Demo Preparation:** Use updated demo script without recurring invoices

## Known Issues
- None critical - system is stable and operational

## Recent Git Commits
- Auto-create ACTIVE customer users with default password
- Fix customer dashboard redirect (useEffect)
- Filter customer invoices by customerId
- Change "Record Payment" to "Pay Invoice" for customers
- Populate invoice number and customer name in payments table

