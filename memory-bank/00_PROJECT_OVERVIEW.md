# InvoiceMe Project - Memory Bank Overview

**Project**: InvoiceMe ERP Invoicing System  
**Type**: Full-Stack ERP Assessment Project  
**Architecture**: Domain-Driven Design (DDD), CQRS, Vertical Slice Architecture  
**Status**: ✅ M2 Complete, M3 In Progress  
**Last Updated**: 2025-01-27

---

## Project Goal

Build a production-quality ERP-style invoicing system demonstrating:
- Mastery of modern software architecture principles (DDD, CQRS, VSA)
- Intelligent use of AI-assisted development tools
- Enterprise-level code quality and scalability

---

## Core Requirements

### Business Domains
- **Customers**: Create, Update, Delete, Retrieve, List
- **Invoices**: Create (Draft), Update, Mark as Sent, Record Payment, Retrieve, List
- **Payments**: Record Payment, Retrieve, List
- **Invoice Lifecycle**: Draft → Sent → Paid
- **Balance Calculation**: Robust logic for invoice balance and payment application

### Technical Stack
- **Backend**: Java 17, Spring Boot 3.2.0, PostgreSQL (Supabase), Flyway
- **Frontend**: Next.js 14.x, React 18.x, TypeScript, Tailwind CSS, shadcn/ui
- **Architecture**: DDD, CQRS, Vertical Slice Architecture, Clean Architecture
- **Deployment**: AWS Elastic Beanstalk (Backend), AWS Amplify (Frontend)
- **Database**: PostgreSQL 15 (Supabase managed)
- **Authentication**: JWT (24-hour expiry, no refresh tokens)
- **RBAC**: SysAdmin, Accountant, Sales, Customer roles

---

## Key Documents

### Requirements
- `InvoiceMe.md` - Main assessment requirements (root)
- `PRD_1_Business_Reqs.md` - Business requirements (root)
- `PRD_2_Tech_Spec.md` - Technical specification (root)

### Architecture
- `docs/agents/ORCHESTRATOR_OUTPUT.md` - Complete orchestrator output with all agent prompts
- `backend/docs/domain-aggregates.md` - Domain model documentation
- `backend/docs/api/openapi.yaml` - API specification (35+ endpoints)
- `backend/docs/events.md` - Domain events documentation

### Status & Progress
- `docs/milestones/CURRENT_STATUS.md` - Current project status
- `docs/milestones/M2_*.md` - M2 milestone reports
- `docs/milestones/M3_*.md` - M3 milestone reports
- `qa/results/MASTER_AGENT_SUMMARY.md` - Master agent summary

---

## Project Structure

```
InvoiceMe/
├── backend/              # Spring Boot application
│   ├── src/main/java/   # Java source code (DDD, CQRS, VSA)
│   ├── src/main/resources/db/migration/  # Flyway migrations
│   └── docs/            # Backend documentation
├── frontend/            # Next.js application
│   ├── src/app/        # Next.js pages (App Router)
│   ├── src/hooks/      # ViewModels (MVVM pattern)
│   └── src/types/      # TypeScript interfaces
├── docs/                # Project documentation
│   ├── milestones/     # Milestone status reports
│   ├── deployment/     # Deployment guides
│   ├── agents/         # Agent reports
│   └── execution/      # Execution plans
├── qa/                  # QA testing
│   ├── results/        # Test results and reports
│   └── scripts/        # Test scripts
├── AGENT_PROMPTS/       # All agent prompts
├── memory-bank/         # This memory bank
└── scripts/            # Deployment scripts
```

---

## Milestone Summary

### M1: Foundation (Complete ✅)
- Data/DB Agent: Database schema, ERD, migrations
- Backend Agent M1: Domain aggregates, OpenAPI spec
- **Status**: Complete

### M2: Core Implementation (Complete ✅)
- Backend Agent M2: RESTful APIs, domain layer, infrastructure, vertical slices
- Frontend Agent: Next.js pages, MVVM pattern, RBAC, mobile responsive
- **Status**: Complete - 25+ endpoints, 12 pages, all core features

### M3: Non-Functional Targets (In Progress 🚧)
- Testing Agent: Test infrastructure complete, execution pending
- DevOps Agent: Deployment configs complete, AWS deployment pending
- **Status**: Configuration complete, deployment ready

---

## Current Status

**Build Status**: ✅ **SUCCESSFUL**
- Backend: Compiles and runs successfully
- Frontend: Builds successfully (`npm run build` passes)
- ✅ **All 25+ endpoints operational** (revenue-trend fixed via Criteria API)
- ✅ **All pages working** (dashboard, list pages, login)

**Deployment Status**: ⏳ **READY**
- Backend: Ready for Elastic Beanstalk deployment
- Frontend: Ready for Amplify deployment
- **Action Required**: Update JWT_SECRET in AWS Elastic Beanstalk

**Testing Status**: ⏳ **READY FOR EXECUTION**
- Test scripts created
- Test procedures documented
- System stabilized and ready for testing

---

## Key Decisions

1. **Full PRD Scope**: Implement all core and extended features in one build (no MVP)
2. **Domain Events**: In-memory events (Spring ApplicationEventPublisher)
3. **Testing**: Local testing + AWS deployment testing
4. **JWT**: 24-hour expiry, no refresh tokens (simpler)
5. **Timezone**: Central Time (America/Chicago) for scheduled jobs
6. **Money Rounding**: Banker's rounding (HALF_UP) to 2 decimal places
7. **Error Format**: RFC 7807 Problem Details
8. **Pagination**: Spring Data JPA Page<T> format

---

## Critical Configuration

### JWT Secret
- **Algorithm**: HS512
- **Key Length**: 64 characters (512 bits) minimum
- **Current Secret**: `3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8`
- **Status**: ✅ Local updated, ⚠️ AWS needs update

### Database
- **Provider**: Supabase (PostgreSQL 15)
- **Migrations**: Flyway (10+ migration files)
- **Connection**: Environment variable `DATABASE_URL`

### AWS Services
- **Backend**: Elastic Beanstalk (Java 17, Corretto)
- **Frontend**: Amplify (Next.js)
- **Email**: SES (verified email: mylesethan93@gmail.com)
- **Storage**: S3 (bucket: invoiceme-pdfs-mlx)
- **Monitoring**: CloudWatch

---

## Next Steps

1. Update JWT_SECRET in AWS Elastic Beanstalk
2. Restart backend environment (applies dashboard fixes)
3. Deploy frontend to Amplify
4. Execute test suite
5. Verify deployment (health checks, API endpoints)

---

**Memory Bank Location**: `/memory-bank/`  
**Last Updated**: 2025-01-27

