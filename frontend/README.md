# InvoiceMe Frontend

Next.js 14.x frontend application for InvoiceMe ERP invoicing system.

## Tech Stack

- **Next.js 14.x** - React framework with App Router
- **React 18.x** - UI library
- **TypeScript 5.x** - Type safety
- **Tailwind CSS 3.x** - Utility-first CSS
- **shadcn/ui** - Accessible component library
- **Axios** - HTTP client
- **React Hook Form** - Form management
- **Zod** - Schema validation
- **Recharts** - Dashboard visualizations

## Architecture

### MVVM Pattern

- **Models**: TypeScript interfaces in `/src/types/` matching backend DTOs
- **Views**: React components in `/src/components/` and pages in `/src/app/`
- **ViewModels**: Custom hooks in `/src/hooks/` managing state and API calls

### Project Structure

```
src/
├── app/              # Next.js App Router pages
├── components/       # React components
├── hooks/           # ViewModels (custom hooks)
├── lib/             # Utilities (API client, auth, RBAC)
├── types/           # TypeScript interfaces (Models)
└── contexts/        # React contexts (AuthContext)
```

## Getting Started

### Prerequisites

- Node.js 18+ installed
- Backend API running at `http://localhost:8080/api/v1` (or configure `NEXT_PUBLIC_API_URL`)

### Installation

```bash
npm install
```

### Environment Variables

Create `.env.local`:

```
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

### Development

```bash
npm run dev
```

Open [http://localhost:3000](http://localhost:3000) in your browser.

### Build

```bash
npm run build
npm start
```

## Features

### ✅ Implemented

- Authentication (Login, Register)
- Customer Management (List, Detail, Create)
- Invoice Management (List, Detail, Create)
- Payment Management (List, Record)
- Dashboard (Metrics, Charts, Aging Report)
- RBAC Enforcement
- Responsive Design

### 🚧 In Progress

- Invoice Detail Page (full implementation)
- Create Invoice Page (multi-line item form)
- Recurring Invoices Pages
- Refunds UI
- User Management (pending users)

## Authentication

- JWT tokens stored in localStorage
- Automatic token injection via Axios interceptors
- 401 responses redirect to login
- Token expiry: 24 hours (no refresh tokens)

## RBAC

Role-based access control enforced via:
- Conditional rendering based on user role
- Helper functions in `/src/lib/rbac.ts`
- Backend API authorization

**Roles:**
- `SYSADMIN` - Full access
- `ACCOUNTANT` - Financial operations
- `SALES` - Customer and invoice creation
- `CUSTOMER` - Own data only

## API Integration

- Base URL: `process.env.NEXT_PUBLIC_API_URL`
- Error Format: RFC 7807 Problem Details
- Pagination: Spring Data JPA `Page<T>` format

## Form Validation

- React Hook Form for form state
- Zod for schema validation
- Client-side and server-side validation
- Clear error messages

## Performance

- Page load target: <2s (First Contentful Paint)
- Dashboard load target: <2s
- Code splitting via Next.js App Router
- Loading states and error boundaries

## Mobile Responsiveness

- Tailwind CSS responsive utilities
- Mobile-friendly customer portal
- Responsive tables and forms

## License

See LICENSE file for details.
