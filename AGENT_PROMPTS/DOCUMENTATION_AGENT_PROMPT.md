# Documentation Agent Prompt - InvoiceMe

**[AGENT]: Documentation**

**GOAL**: Generate comprehensive, production-ready documentation for the InvoiceMe ERP system covering architecture, API reference, user guides, deployment, and technical specifications.

---

## Context

**Project Status**: ✅ **PRODUCTION READY**
- Backend: Spring Boot application deployed on AWS Elastic Beanstalk
- Frontend: Next.js application deployed on Vercel
- Database: PostgreSQL (Supabase)
- All core features implemented and tested
- Integration tests passing (12 tests, 100% pass rate)

**Documentation Needs**:
- Technical architecture documentation (for assessment submission)
- API reference documentation
- User guides and tutorials
- Deployment and operations guides
- Developer setup instructions
- Project overview and feature documentation

---

## Inputs

**Core Project Documents**:
- `InvoiceMe.md` - Original project specification and requirements
- `PRD_1_Business_Reqs.md` - Business requirements and functional specs
- `PRD_2_Tech_Spec.md` - Technical architecture and implementation details
- `docs/FEATURES.md` - Feature list and status

**Codebase References**:
- `backend/src/main/java/com/invoiceme/` - Backend source code (DDD, CQRS, VSA)
- `frontend/app/` and `frontend/src/` - Frontend source code (Next.js, React, TypeScript)
- `backend/src/main/resources/db/migration/` - Database migrations
- `backend/src/test/java/com/invoiceme/integration/` - Integration tests

**Existing Documentation** (to review and enhance):
- `docs/README.md` - Documentation index
- `docs/deployment/` - Deployment guides
- `docs/setup/` - Setup instructions
- `docs/testing/` - Testing documentation
- `docs/demo/` - Demo scripts and seed data
- `memory-bank/` - Project context and progress

---

## Deliverables

### 1. Technical Architecture Documentation (`docs/ARCHITECTURE.md`)

**Purpose**: Comprehensive technical writeup for assessment submission (1-2 pages as required by InvoiceMe.md)

**Sections**:
- **Architecture Overview**
  - Domain-Driven Design (DDD) boundaries and aggregates
  - Command Query Responsibility Segregation (CQRS) implementation
  - Vertical Slice Architecture (VSA) structure
  - Clean Architecture layer separation (Domain/Application/Infrastructure)
  
- **Domain Model**
  - Aggregates: Customer, Invoice, Payment
  - Value Objects: Money, Email, InvoiceNumber, Address
  - Domain Events: 10 events with transactional consistency
  - Business Rules: Invoice lifecycle, payment application, credit system
  
- **Technical Stack**
  - Backend: Java 17, Spring Boot, Spring Data JPA, Spring Security
  - Frontend: Next.js 14, React, TypeScript, Tailwind CSS
  - Database: PostgreSQL (Supabase)
  - Cloud: AWS (Elastic Beanstalk, Vercel, SES, S3)
  
- **Design Decisions**
  - Why DDD + CQRS + VSA?
  - Database schema design (tables, indexes, constraints)
  - API design (RESTful, RFC 7807 error handling)
  - Authentication/Authorization (JWT, RBAC)
  
- **Code Organization**
  - Folder structure examples
  - Vertical slice examples (`/invoices/createinvoice/`, `/payments/recordpayment/`)
  - Domain layer examples (rich behavior methods)

### 2. API Reference Documentation (`docs/API_REFERENCE.md`)

**Purpose**: Complete API endpoint documentation for developers

**Sections**:
- **Authentication**
  - Login endpoint (`POST /api/v1/auth/login`)
  - Register endpoint (`POST /api/v1/auth/register`)
  - JWT token format and usage
  - Role-based access control (RBAC)
  
- **Customer Endpoints**
  - `POST /api/v1/customers` - Create customer
  - `GET /api/v1/customers/{id}` - Get customer
  - `GET /api/v1/customers` - List customers (with pagination, filtering)
  - `PUT /api/v1/customers/{id}` - Update customer
  - `DELETE /api/v1/customers/{id}` - Delete customer
  
- **Invoice Endpoints**
  - `POST /api/v1/invoices` - Create invoice (DRAFT)
  - `GET /api/v1/invoices/{id}` - Get invoice
  - `GET /api/v1/invoices` - List invoices (with pagination, filtering, search)
  - `PUT /api/v1/invoices/{id}` - Update invoice
  - `PATCH /api/v1/invoices/{id}/mark-as-sent` - Mark as sent
  - `PATCH /api/v1/invoices/{id}/cancel` - Cancel invoice
  
- **Payment Endpoints**
  - `POST /api/v1/payments` - Record payment
  - `GET /api/v1/payments/{id}` - Get payment
  - `GET /api/v1/payments` - List payments (with pagination, filtering)
  
- **Dashboard Endpoints**
  - `GET /api/v1/dashboard/metrics` - Revenue MTD, outstanding invoices, active customers
  - `GET /api/v1/dashboard/revenue-trend` - Revenue trends over time
  - `GET /api/v1/dashboard/invoice-status` - Invoice status breakdown
  - `GET /api/v1/dashboard/aging-report` - Aging report
  
- **Refund Endpoints**
  - `POST /api/v1/refunds` - Issue refund
  
- **User Management Endpoints**
  - `GET /api/v1/users/pending` - List pending users
  - `PATCH /api/v1/users/{id}/approve` - Approve user
  - `PATCH /api/v1/users/{id}/reject` - Reject user

**For Each Endpoint**:
- HTTP method and path
- Authentication requirements (roles)
- Request body schema (with examples)
- Response schema (200, 201, 400, 401, 403, 404, 500)
- Error response format (RFC 7807 Problem Details)
- Pagination format (Spring Data JPA Page<T>)

### 3. User Guide (`docs/USER_GUIDE.md`)

**Purpose**: End-user documentation for SysAdmin, Accountant, Sales, and Customer roles

**Sections**:
- **Getting Started**
  - Login instructions
  - Role-based dashboard overview
  - Navigation guide
  
- **Customer Management** (SysAdmin, Accountant, Sales)
  - Creating customers
  - Updating customer information
  - Viewing customer details
  - Customer credit balance
  
- **Invoice Management** (SysAdmin, Accountant, Sales)
  - Creating invoices with line items
  - Editing draft invoices
  - Marking invoices as sent
  - Viewing invoice details
  - Canceling invoices
  
- **Payment Processing** (SysAdmin, Accountant, Sales, Customer)
  - Recording payments (admin)
  - Paying invoices (customer portal)
  - Viewing payment history
  - Partial payments
  - Overpayments and credit application
  
- **Refunds** (SysAdmin, Accountant)
  - Issuing refunds
  - Applying refunds as credit
  - Refund history
  
- **Dashboard & Reports** (SysAdmin, Accountant, Sales)
  - Revenue metrics
  - Invoice status breakdown
  - Aging report
  - Revenue trend charts
  
- **Customer Portal** (Customer role)
  - Viewing own invoices
  - Paying invoices
  - Viewing credit balance
  - Account summary

### 4. Developer Setup Guide (`docs/DEVELOPER_SETUP.md`)

**Purpose**: Step-by-step instructions for developers to set up local environment

**Sections**:
- **Prerequisites**
  - Java 17 JDK
  - Node.js 20+
  - PostgreSQL (or Docker)
  - Maven 3.8+
  - Git
  
- **Backend Setup**
  - Clone repository
  - Configure `application.yml` (database, JWT secret)
  - Run Flyway migrations
  - Start Spring Boot application
  - Verify health endpoint
  
- **Frontend Setup**
  - Install dependencies (`npm install`)
  - Configure `.env.local` (API URL)
  - Start development server (`npm run dev`)
  - Verify frontend accessible
  
- **Database Setup**
  - Local PostgreSQL setup (Docker Compose)
  - Supabase setup (for production)
  - Connection pooler configuration
  
- **Testing**
  - Running integration tests (`mvn test`)
  - Running frontend tests
  - Manual testing checklist

### 5. Deployment Guide (`docs/DEPLOYMENT_GUIDE.md`)

**Purpose**: Production deployment instructions (enhance existing deployment docs)

**Sections**:
- **Backend Deployment (AWS Elastic Beanstalk)**
  - Build JAR file
  - Create EB application and environment
  - Configure environment variables
  - Deploy and verify
  
- **Frontend Deployment (Vercel)**
  - Connect GitHub repository
  - Configure build settings
  - Set environment variables
  - Deploy and verify
  
- **Database Configuration**
  - Supabase Connection Pooler setup
  - Environment variables
  - Migration strategy (Flyway disabled in production)
  
- **CI/CD Pipeline**
  - GitHub Actions workflow
  - Automated deployment triggers
  - Rollback procedures

### 6. Project Overview (`docs/PROJECT_OVERVIEW.md`)

**Purpose**: High-level project summary and feature list

**Sections**:
- **Project Description**
  - What is InvoiceMe?
  - Core functionality
  - Target users
  
- **Core Features** (from InvoiceMe.md requirements)
  - Customer management (CRUD)
  - Invoice management (CRUD, lifecycle)
  - Payment processing
  - Multi-line item support
  - Balance calculations
  
- **Extended Features** (bonus features)
  - Customer credit system
  - Refunds
  - Dashboard analytics
  - Late fee automation
  - User approval workflow
  - Customer portal
  
- **Architecture Highlights**
  - DDD + CQRS + VSA
  - Domain events
  - Clean Architecture
  
- **Technology Stack**
  - Backend, Frontend, Database, Cloud services

### 7. AI Tool Documentation (`docs/AI_TOOL_USAGE.md`)

**Purpose**: Document AI tools used and how they accelerated development (required by InvoiceMe.md)

**Sections**:
- **Tools Used**
  - Cursor (primary IDE with AI assistance)
  - GitHub Copilot (code completion)
  - Other tools (if any)
  
- **Example Prompts**
  - Domain model design prompts
  - API endpoint implementation prompts
  - Test generation prompts
  - Bug fixing prompts
  
- **How AI Accelerated Development**
  - Code generation for boilerplate
  - Test generation
  - Documentation generation
  - Architecture pattern implementation
  
- **Maintaining Architectural Quality**
  - How AI was guided to follow DDD principles
  - Code review process
  - Ensuring CQRS separation
  - Maintaining VSA structure

---

## Documentation Standards

### Format
- **Markdown** format for all documentation
- Use clear headings and subheadings
- Code blocks with syntax highlighting
- Tables for structured data
- Diagrams (Mermaid or ASCII) for architecture

### Style
- **Clear and concise** language
- **Examples** for all concepts
- **Step-by-step** instructions where applicable
- **Cross-references** between documents
- **Consistent** terminology throughout

### Code Examples
- Use actual code from the codebase (with file paths)
- Include both backend (Java) and frontend (TypeScript) examples
- Show request/response examples for APIs
- Include SQL examples for database operations

---

## Success Criteria

✅ **Technical Architecture Documentation**:
- 1-2 pages as required by InvoiceMe.md
- Clearly explains DDD boundaries, CQRS implementation, VSA structure
- Includes database schema overview
- Shows code organization examples

✅ **API Reference**:
- All 25+ endpoints documented
- Request/response examples provided
- Error handling documented (RFC 7807)
- Authentication/authorization clearly explained

✅ **User Guide**:
- Covers all 4 user roles
- Step-by-step instructions with screenshots (or descriptions)
- Common workflows documented
- Troubleshooting section

✅ **Developer Setup**:
- Complete setup instructions
- All prerequisites listed
- Troubleshooting guide included
- Verification steps provided

✅ **Deployment Guide**:
- Production deployment steps
- Environment variable reference
- Troubleshooting common issues
- Rollback procedures

✅ **AI Tool Documentation**:
- Documents specific tools and prompts used
- Shows how AI accelerated development
- Explains how architectural quality was maintained

---

## Important Notes

- **Review existing documentation** in `docs/` folder and enhance/consolidate where needed
- **Use actual code** from the codebase for examples (don't make up examples)
- **Reference InvoiceMe.md requirements** to ensure all deliverables are covered
- **Maintain consistency** with existing documentation style
- **Update `docs/README.md`** with links to all new documentation

---

## Expected Outcome

After running this agent, you should have:
1. ✅ Complete technical architecture documentation (ready for assessment submission)
2. ✅ Comprehensive API reference for all endpoints
3. ✅ User guides for all roles
4. ✅ Developer setup instructions
5. ✅ Deployment guides (enhanced)
6. ✅ AI tool usage documentation
7. ✅ Updated documentation index

All documentation should be **production-ready**, **well-organized**, and **ready for submission** as part of the InvoiceMe assessment deliverables.

