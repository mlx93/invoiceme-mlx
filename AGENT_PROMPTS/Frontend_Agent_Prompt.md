# Frontend Agent Prompt

**[AGENT]: Frontend**

**GOAL**: Implement Next.js 14.x frontend with React 18.x, TypeScript, and MVVM pattern. **FULL PRD SCOPE**: All UI features (Customers, Invoices, Payments, Recurring Invoices, Refunds, Customer Portal, Dashboard, User Management).

**INPUTS**:
- InvoiceMe.md (assessment source of truth)
- PRD_1_Business_Reqs.md (user roles, RBAC matrix, UI requirements)
- PRD_2_Tech_Spec.md (frontend architecture, MVVM pattern, component structure, API DTOs)
- ORCHESTRATOR_OUTPUT.md (scope decisions, decision log)
- **M1 Deliverables** (completed):
  - `/backend/docs/api/openapi.yaml` — API contracts (use to generate TypeScript types and API client)
- Setup completion: `/docs/SETUP_COMPLETION_REPORT.md` (environment variables: `NEXT_PUBLIC_API_URL`)

**DELIVERABLES**:
- `/frontend/src/app/` — Next.js App Router pages:
  - `/customers/page.tsx` (Customer list)
  - `/customers/[id]/page.tsx` (Customer detail)
  - `/customers/new/page.tsx` (Create customer)
  - `/invoices/page.tsx` (Invoice list)
  - `/invoices/[id]/page.tsx` (Invoice detail)
  - `/invoices/new/page.tsx` (Create invoice)
  - `/payments/page.tsx` (Payment list)
  - `/recurring-invoices/page.tsx` (Recurring invoice templates list)
  - `/recurring-invoices/new/page.tsx` (Create template)
  - `/dashboard/page.tsx` (Dashboard with metrics)
  - `/login/page.tsx` (Login page)
  - `/register/page.tsx` (Registration page)
  - `/users/pending/page.tsx` (Pending users list - SysAdmin only)
- `/frontend/src/components/` — React components:
  - `/ui/` — shadcn/ui primitives (Button, Input, Table, Card, Dialog, etc.)
  - `/customers/` — Customer-specific components (CustomerCard, CustomerForm, etc.)
  - `/invoices/` — Invoice-specific components (InvoiceCard, InvoiceForm, LineItemForm, etc.)
  - `/payments/` — Payment-specific components (PaymentForm, PaymentCard, etc.)
  - `/dashboard/` — Dashboard widgets (MetricsCard, RevenueChart, InvoiceStatusChart, etc.)
  - `/layout/` — Header, Sidebar, Footer, Navigation
- `/frontend/src/hooks/` — Custom hooks (ViewModels):
  - `useCustomers.ts` — Customer list/detail logic
  - `useInvoiceDetail.ts` — Invoice detail logic
  - `usePayments.ts` — Payment recording logic
  - `useAuth.ts` — Authentication logic
  - `useDashboard.ts` — Dashboard metrics logic
  - `useRecurringInvoices.ts` — Recurring invoice template logic
- `/frontend/src/lib/api.ts` — Axios instance with JWT interceptors
- `/frontend/src/types/` — TypeScript interfaces matching backend DTOs (can generate from OpenAPI spec)

**DONE CRITERIA**:
1. ✅ MVVM pattern implemented:
   - Models: TypeScript interfaces matching backend DTOs (from OpenAPI spec)
   - Views: React components (presentational, receive props, emit events)
   - ViewModels: Custom hooks managing state, API calls, business logic
2. ✅ Core UI pages functional:
   - Customer list (with filters, pagination, search)
   - Customer detail (with edit/delete actions)
   - Create customer form (with validation)
   - Invoice list (with filters, pagination, search)
   - Invoice detail (with Mark as Sent, Record Payment actions, PDF download)
   - Create invoice form (with multiple line items, discount, tax calculation)
   - Payment list (with filters)
   - Login page (email/password → JWT)
   - Registration page (email, password, name, role → pending approval)
3. ✅ RBAC enforced in UI:
   - Conditional rendering based on user role (SysAdmin, Accountant, Sales, Customer)
   - "Delete Customer" button only visible to SysAdmin
   - "Record Payment" button only visible to SysAdmin, Accountant, Customer (own invoices)
   - "Approve User" button only visible to SysAdmin
   - Customer role: Can only view own invoices, make payments on own invoices
4. ✅ UI responsiveness:
   - Page load <2s (First Contentful Paint)
   - Dashboard <2s (full page load)
   - Smooth interactions without lag
   - Mobile-responsive (customer portal at minimum)
5. ✅ Form validation:
   - React Hook Form for form state
   - Client-side validation (email format, required fields, number ranges)
   - Error messages displayed clearly (RFC 7807 Problem Details format)
6. ✅ API integration:
   - Axios instance configured with base URL (`process.env.NEXT_PUBLIC_API_URL`)
   - JWT token added to Authorization header (from localStorage)
   - Error handling (401 → redirect to login, 400 → show error message)
   - Loading states managed in ViewModels
7. ✅ Styling:
   - Tailwind CSS utility classes
   - shadcn/ui components for consistent design
   - Mobile-responsive (customer portal at minimum)
   - Clean, modern ERP style (similar to Ramp, QuickBooks Online)
8. ✅ Extended features UI implemented:
   - Recurring Invoices UI (template creation, list, pause/resume, complete)
   - Refunds UI (issue refund form, refund history)
   - Customer Portal (self-service dashboard, payment form, invoice list)
   - Dashboard (metrics cards, revenue trend chart, invoice status pie chart, aging report)
   - User Management (registration, pending users list, approval/rejection)

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Pages implemented (list pages)
  - Components created (count)
  - Custom hooks (ViewModels) created (list)
  - RBAC enforced (roles supported)
  - API integration working (endpoints connected)
  - UI performance (page load times)
  - Mobile responsiveness (tested on mobile)
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Code: `/frontend/src/`
  - Components: `/frontend/src/components/`
  - Hooks: `/frontend/src/hooks/`
  - Types: `/frontend/src/types/`
  - Pages: `/frontend/src/app/`
- **Evidence**:
  - Screenshots of UI pages
  - Lighthouse performance scores (if available)
  - Browser console logs (no errors)
  - Mobile screenshots (customer portal)

**DO NOT**:
- Use Redux/Zustand for state management (use React Context + Hooks only)
- Write custom CSS (use Tailwind utility classes)
- Hardcode API URLs (use environment variables: `NEXT_PUBLIC_API_URL`)
- Skip mobile responsiveness (customer portal must be mobile-friendly)
- Implement refresh token logic (no refresh tokens - redirect to login on 401)
- Use custom error format (use RFC 7807 Problem Details format from backend)

**IMPORTANT NOTES**:
- **API Base URL**: Use `process.env.NEXT_PUBLIC_API_URL` (from `.env` file, defaults to `http://localhost:8080/api/v1`)
- **JWT Storage**: Store JWT token in localStorage. Add to `Authorization: Bearer <token>` header via Axios interceptor.
- **Error Handling**: Backend returns RFC 7807 Problem Details format. Parse and display user-friendly error messages.
- **Pagination**: Backend returns Spring Data JPA `Page<T>` format. Use `content`, `totalElements`, `totalPages`, `first`, `last` properties.
- **Authentication Flow**: No refresh tokens. On 401 response, redirect to login page. Token expiry: 24 hours.
- **RBAC**: Check user role from JWT token payload. Conditionally render UI elements based on role.
- **Customer Portal**: Must be mobile-responsive. Customer role can only access own data (filtered by `customer_id` from JWT).
- **shadcn/ui**: Use shadcn/ui components for consistent, accessible design. Reference: https://ui.shadcn.com/docs/components
- **Charts**: Use Recharts library for dashboard visualizations (revenue trend, invoice status pie chart).
- **Form Validation**: Use React Hook Form with validation rules. Display errors clearly.

**REFERENCE**:
- OpenAPI spec: `/backend/docs/api/openapi.yaml` (use to generate TypeScript types)
- PRD_2_Tech_Spec.md Section 6 for frontend architecture and MVVM pattern
- PRD_1_Business_Reqs.md Section 3 for RBAC matrix
- PRD_1_Business_Reqs.md Section 4 for UI requirements
- ORCHESTRATOR_OUTPUT.md Section 7 (Decision Log) for technical decisions

---

**Status**: Ready to execute  
**Dependencies**: M1 complete ✅ (needs OpenAPI spec)  
**Parallel**: Can work in parallel with Backend Agent M2 (Frontend can mock APIs initially, then connect to real APIs)  
**Next**: After completion, proceed to M3 (QA testing + DevOps AWS deployment)

