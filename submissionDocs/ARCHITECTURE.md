# InvoiceMe Technical Architecture

**Version**: 1.0  
**Date**: January 2025  
**Status**: Production Ready

---

## Executive Summary

InvoiceMe is a production-quality ERP-style invoicing system built using modern software architecture principles. The system demonstrates **Domain-Driven Design (DDD)**, **Command Query Responsibility Segregation (CQRS)**, and **Vertical Slice Architecture (VSA)** to create a maintainable, scalable, and testable application.

---

## Architecture Overview

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Next.js/React)                  │
│                     Hosted on Vercel                         │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTPS/REST (JWT Bearer Token)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                  API Gateway (Spring Boot)                   │
│                     /api/v1/* endpoints                      │
└──────────┬────────────────────┬────────────────────┬─────────┘
           │                    │                    │
           ▼                    ▼                    ▼
    ┌──────────┐         ┌──────────┐        ┌──────────┐
    │ Commands │         │ Queries  │        │  Events  │
    │ (Writes) │         │  (Reads) │        │ (Async)  │
    └─────┬────┘         └─────┬────┘        └─────┬────┘
          │                    │                    │
          └────────────────────┼────────────────────┘
                               ▼
                    ┌──────────────────────┐
                    │   Domain Layer (DDD)  │
                    │  Rich Domain Models   │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Infrastructure Layer │
                    │ PostgreSQL (Supabase)│
                    │ AWS SES (Email)       │
                    │ AWS S3 (PDFs)         │
                    └──────────────────────┘
```

### Architectural Principles

#### 1. Domain-Driven Design (DDD)

The system models core business entities as **rich domain objects** with behavior, not anemic data models. Three main aggregates enforce business invariants:

- **Customer Aggregate**: Manages customer information, credit balance, and account status
- **Invoice Aggregate**: Handles invoice lifecycle, line items, and balance calculations
- **Payment Aggregate**: Records payments and applies them to invoices

**Value Objects** (immutable, no identity):
- `Money`: Monetary values with currency and precision
- `Email`: Validated email addresses
- `InvoiceNumber`: Auto-generated format (INV-YYYY-####)
- `Address`: Billing address information

**Domain Events** (10 events with transactional consistency):
- `InvoiceSentEvent`, `PaymentRecordedEvent`, `InvoiceFullyPaidEvent`
- `LateFeeAppliedEvent`, `RefundIssuedEvent`, `CreditAppliedEvent`
- `CreditDeductedEvent`, `CustomerDeactivatedEvent`, `InvoiceCancelledEvent`

**Rich Domain Model Logic**:
The Invoice aggregate encapsulates business logic in behavior methods. The `markAsSent()` method validates that the invoice is in DRAFT status before transitioning to SENT, sets the sent date, and collects a domain event for later publication. The `recordPayment()` method validates invoice status, updates payment amounts and balance calculations, transitions to PAID when balance reaches zero, and collects appropriate domain events. All business rules are enforced within the aggregate, preventing invalid state transitions.

#### 2. Command Query Responsibility Segregation (CQRS)

**Separation**: Write operations (Commands) and read operations (Queries) are completely separated:

- **Commands**: Mutate state, publish domain events, return minimal data (ID or void)
  - `CreateInvoiceCommand` → `CreateInvoiceHandler` → Creates invoice, publishes events
  - `RecordPaymentCommand` → `RecordPaymentHandler` → Records payment, updates invoice balance
  - `MarkAsSentCommand` → `MarkAsSentHandler` → Marks invoice as sent

- **Queries**: Return read-only DTOs, no side effects, optimized for reading
  - `GetInvoiceQuery` → `GetInvoiceHandler` → Returns `InvoiceDetailResponse` DTO
  - `ListInvoicesQuery` → `ListInvoicesHandler` → Returns paginated `InvoiceResponse` DTOs
  - `DashboardMetricsQuery` → `DashboardMetricsHandler` → Returns aggregated metrics

**Same Database, Separate Models**:
- Write side uses rich domain entities (Invoice, Customer, Payment aggregates)
- Read side uses flat DTOs optimized for API responses
- Both read from same PostgreSQL database (no separate read database for MVP)

**CQRS Pattern Logic**:
Command handlers are transactional services that create or modify aggregates, save them to the repository, and publish domain events. They return minimal data (typically just the aggregate ID). Query handlers are read-only services that load aggregates, map them to optimized DTOs, and return the DTOs. This separation allows write models to use rich domain entities while read models use flat, optimized DTOs for API responses.

#### 3. Vertical Slice Architecture (VSA)

Code is organized **by feature** (vertical slices), not by technical layer (horizontal slicing). Each feature contains all layers needed for that use case:

```
src/main/java/com/invoiceme/
├── invoices/
│   ├── createinvoice/
│   │   ├── CreateInvoiceCommand.java          (DTO)
│   │   ├── CreateInvoiceHandler.java          (Service, @Transactional)
│   │   ├── CreateInvoiceValidator.java        (Validation)
│   │   └── CreateInvoiceController.java       (REST Controller)
│   ├── getinvoice/
│   │   ├── GetInvoiceQuery.java
│   │   ├── GetInvoiceHandler.java
│   │   └── GetInvoiceResponse.java
│   ├── markassent/
│   │   ├── MarkAsSentCommand.java
│   │   ├── MarkAsSentHandler.java
│   │   └── MarkAsSentController.java
│   ├── domain/
│   │   ├── Invoice.java                       (Aggregate Root)
│   │   └── LineItem.java                      (Entity within aggregate)
│   └── infrastructure/
│       └── InvoiceRepository.java             (JPA Repository)
```

**Benefits**:
- Features are self-contained (easy to understand, test, and modify)
- Minimal shared infrastructure (domain base classes, event publisher)
- Easy to add new features (copy slice structure, implement handlers)

#### 4. Clean Architecture Layer Separation

**Domain Layer** (Pure business logic, no framework dependencies):
- Aggregates: `Customer.java`, `Invoice.java`, `Payment.java`
- Value Objects: `Money.java`, `Email.java`, `InvoiceNumber.java`
- Domain Events: `InvoiceSentEvent.java`, `PaymentRecordedEvent.java`
- Repository Interfaces: `CustomerRepository.java` (interface only)

**Application Layer** (Use cases, vertical slices):
- Command Handlers: `CreateInvoiceHandler`, `RecordPaymentHandler`
- Query Handlers: `GetInvoiceHandler`, `ListInvoicesHandler`
- DTOs: `CreateInvoiceCommand`, `InvoiceDetailResponse`

**Infrastructure Layer** (Framework implementations):
- JPA Repositories: `CustomerRepositoryImpl.java` (JPA implementation)
- Event Publisher: `DomainEventPublisher.java` (Spring ApplicationEventPublisher)
- External Services: `AwsSesEmailService.java`, `S3PdfService.java`

**Presentation Layer** (REST API):
- Controllers: `InvoiceController.java`, `CustomerController.java`
- DTOs: Request/Response objects
- Exception Handlers: `GlobalExceptionHandler.java` (RFC 7807 error format)

---

## Domain Model

### Aggregates

#### Customer Aggregate
- **Root**: `Customer.java`
- **Properties**: `id`, `companyName`, `email`, `creditBalance`, `status`
- **Behavior**: `applyCredit()`, `deductCredit()`, `markAsInactive()`
- **Invariants**: Credit balance >= $0, email uniqueness

#### Invoice Aggregate
- **Root**: `Invoice.java`
- **Entities**: `LineItem.java` (child entity)
- **Properties**: `id`, `invoiceNumber`, `customerId`, `status`, `totalAmount`, `balanceDue`
- **Behavior**: `addLineItem()`, `markAsSent()`, `recordPayment()`, `cancel()`
- **Invariants**: At least 1 line item, balance calculation consistency

#### Payment Aggregate
- **Root**: `Payment.java`
- **Properties**: `id`, `invoiceId`, `customerId`, `amount`, `paymentMethod`, `status`
- **Behavior**: `Payment.record()` (static factory method)
- **Invariants**: Amount > $0, valid invoice status

### Domain Events

All domain events are published **after transaction commit** using `@TransactionalEventListener(AFTER_COMMIT)`:

1. **InvoiceSentEvent**: Published when invoice marked as sent
2. **PaymentRecordedEvent**: Published when payment recorded
3. **InvoiceFullyPaidEvent**: Published when invoice balance reaches $0
4. **LateFeeAppliedEvent**: Published by scheduled job for overdue invoices
5. **RefundIssuedEvent**: Published when refund issued
6. **CreditAppliedEvent**: Published when credit added to customer
7. **CreditDeductedEvent**: Published when credit deducted from customer
8. **CustomerDeactivatedEvent**: Published when customer soft-deleted
9. **InvoiceCancelledEvent**: Published when invoice cancelled

**Event Flow**:
```
Command Handler → Aggregate Method → Domain Event Collected
    ↓
Transaction Commit
    ↓
Event Published (AFTER_COMMIT)
    ↓
Event Listeners Execute (Email, Audit Log, Cache Invalidation)
```

---

## Technical Stack

### Backend
- **Runtime**: Java 17 (LTS)
- **Framework**: Spring Boot 3.2.0
- **Security**: Spring Security (JWT authentication, RBAC)
- **Data Access**: Spring Data JPA, Criteria API
- **Database**: PostgreSQL 15 (Supabase managed)
- **Migration**: Flyway 9.x
- **Validation**: Hibernate Validator (JSR 380)
- **Mapping**: MapStruct (DTO ↔ Entity mapping)

### Frontend
- **Framework**: Next.js 14.x (App Router)
- **UI Library**: React 18.x, TypeScript 5.x
- **Styling**: Tailwind CSS 3.x
- **Components**: shadcn/ui
- **State**: React Context + Hooks
- **Forms**: React Hook Form + Zod validation
- **HTTP**: Axios

### Infrastructure
- **Database**: Supabase PostgreSQL (Connection Pooler)
- **Backend Hosting**: AWS Elastic Beanstalk (Java 17)
- **Frontend Hosting**: Vercel (Next.js SSR)
- **Email**: AWS SES
- **Storage**: AWS S3 (PDF invoices)
- **Monitoring**: AWS CloudWatch

---

## Design Decisions

### Why DDD + CQRS + VSA?

**Domain-Driven Design**:
- Business logic encapsulated in domain objects (not services)
- Invariants enforced at aggregate level (cannot be violated)
- Domain events enable loose coupling between aggregates

**CQRS**:
- Clear separation of concerns (writes vs reads)
- Optimized read models (flat DTOs) vs write models (rich entities)
- Scales independently (can add read replicas later)

**Vertical Slice Architecture**:
- Features are self-contained (easy to understand and test)
- Minimal shared infrastructure (reduces coupling)
- Easy to add new features (copy slice structure)

### Database Schema Design

**Tables**: 9 core tables (customers, invoices, line_items, payments, users, etc.)

**Key Design Decisions**:
- **UUID Primary Keys**: Globally unique identifiers, no sequential IDs
- **DECIMAL(19,2)**: All monetary values (precision for financial calculations)
- **PostgreSQL ENUMs**: Type safety at database level (customer_type, invoice_status, etc.)
- **Optimistic Locking**: `version` field on invoices (prevents concurrent modification)
- **Indexes**: 40+ indexes including composite indexes for common query patterns
- **Foreign Keys**: RESTRICT DELETE (prevent orphaned records)

**Schema Design Logic**:
The invoices table uses UUID primary keys for globally unique identifiers, DECIMAL(19,2) for all monetary values to ensure precision, PostgreSQL ENUMs for type safety, and a version field for optimistic locking. Composite indexes are created for common query patterns (customer_id + status, status + due_date for overdue queries). Partial indexes optimize specific queries (e.g., only indexing SENT/OVERDUE invoices for overdue reports).

### API Design

**RESTful Design**:
- Base URL: `/api/v1`
- Authentication: JWT Bearer token (`Authorization: Bearer <token>`)
- Content-Type: `application/json`
- Pagination: Query params `page`, `size`, `sort` (Spring Data JPA `Page<T>`)
- Filtering: Query params for domain filters (`?status=SENT&customerId=123`)

**Error Handling**: All errors follow RFC 7807 Problem Details format, providing a standardized structure with type, title, status code, detail message, and instance path. This enables consistent error handling across all endpoints and clear error messages for API consumers.

**Authentication/Authorization**:
- JWT tokens (24-hour expiry, HS512 algorithm, 64-character secret)
- Role-Based Access Control (RBAC): `SYSADMIN`, `ACCOUNTANT`, `SALES`, `CUSTOMER`
- Spring Security `@PreAuthorize` annotations on controllers

---

## Code Organization

### Folder Structure Example

**Backend** (`backend/src/main/java/com/invoiceme/`):
```
invoices/
├── createinvoice/              # Vertical slice: Create Invoice
│   ├── CreateInvoiceCommand.java
│   ├── CreateInvoiceHandler.java
│   ├── CreateInvoiceValidator.java
│   └── CreateInvoiceController.java
├── getinvoice/                  # Vertical slice: Get Invoice
│   ├── GetInvoiceQuery.java
│   ├── GetInvoiceHandler.java
│   └── GetInvoiceResponse.java
├── domain/                      # Domain layer
│   ├── Invoice.java             # Aggregate root
│   └── LineItem.java            # Entity within aggregate
└── infrastructure/              # Infrastructure layer
    └── InvoiceRepository.java   # JPA repository interface
```

**Frontend** (`frontend/src/`):
```
app/
├── dashboard/                   # Next.js page (App Router)
│   └── page.tsx
├── invoices/
│   ├── page.tsx                 # List invoices
│   └── [id]/page.tsx           # View invoice
src/
├── hooks/                      # ViewModels (MVVM pattern)
│   ├── useInvoices.ts
│   └── useCustomers.ts
└── types/                      # TypeScript interfaces
    ├── invoice.ts
    └── customer.ts
```

### Vertical Slice Logic

**Create Invoice Slice Structure**:
Each vertical slice contains all layers needed for that feature. The Create Invoice slice includes a Command DTO (data transfer object), a Command Handler (transactional service that creates the invoice and publishes events), a Validator (validates command data), and a REST Controller (handles HTTP requests, maps to command, calls handler, returns response). The controller applies role-based authorization, ensuring only authorized roles can create invoices. All components are co-located in the same directory, making the feature self-contained and easy to understand.

---

## Conclusion

InvoiceMe demonstrates a production-ready architecture combining DDD, CQRS, and VSA principles. The system achieves:

- **Maintainability**: Self-contained vertical slices, clear separation of concerns
- **Scalability**: CQRS enables independent scaling of read/write operations
- **Testability**: Rich domain models with behavior methods, easy to unit test
- **Extensibility**: Easy to add new features (copy slice structure, implement handlers)

The architecture supports the core business requirements while maintaining code quality and enabling future growth.

---

**Document Version**: 1.0  
**Last Updated**: January 2025

