# **InvoiceMe - Product Requirements Document**
## **PRD 2: Technical Specification Document**

---

## **1. System Architecture Overview**

### **1.1 High-Level Architecture**

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Next.js/React)                  │
│                     Hosted on AWS Amplify                    │
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
                    │   Domain Layer (DDD) │
                    │  Rich Domain Models  │
                    └──────────┬───────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │ Infrastructure Layer │
                    │ PostgreSQL (Supabase)│
                    │ AWS SES (Email)      │
                    │ AWS S3 (PDFs)        │
                    └──────────────────────┘
```

### **1.2 Architectural Principles**

**Domain-Driven Design (DDD):**
- Core entities modeled as rich domain objects with behavior (not anemic data models)
- Aggregate roots enforce business invariants (Customer, Invoice, Payment)
- Value objects for immutable concepts (Money, Email, InvoiceNumber)
- Domain events capture business-significant occurrences

**Command Query Responsibility Segregation (CQRS):**
- Write operations (Commands): CreateInvoice, RecordPayment, MarkAsSent
- Read operations (Queries): GetInvoice, ListCustomers, DashboardMetrics
- Same database, separate models (write = rich entities, read = flat DTOs)

**Vertical Slice Architecture (VSA):**
- Code organized by feature, not technical layer
- Each feature contains: Command/Query, Handler, Validator, Controller, Tests
- Minimal shared infrastructure (domain base classes, event publisher)

**Clean Architecture:**
- Domain Layer: Pure business logic, no framework dependencies
- Application Layer: Use cases (command/query handlers)
- Infrastructure Layer: Database, email, external services
- Presentation Layer: REST controllers, DTOs

---

## **2. Technology Stack**

### **2.1 Backend**

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| Runtime | Java | 17 (LTS) | Application runtime |
| Framework | Spring Boot | 3.2.x | REST API framework |
| Security | Spring Security | 3.2.x | JWT auth, RBAC |
| Data Access | Spring Data JPA | 3.2.x | ORM, repositories |
| Database | PostgreSQL | 15.x | ACID-compliant data store |
| Hosting | Supabase | Latest | Managed PostgreSQL |
| Connection Pool | HikariCP | 5.x | High-performance pooling |
| Migration | Flyway | 9.x | Version-controlled schema |
| Validation | Hibernate Validator | 8.x | Bean validation (JSR 380) |
| Mapping | MapStruct | 1.5.x | DTO ↔ Entity mapping |
| PDF | iText 7 | 8.x | Invoice PDF generation |
| Testing | JUnit 5 + Mockito | 5.x | Unit/integration tests |

### **2.2 Frontend**

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| Framework | Next.js | 14.x | React SSR framework |
| UI Library | React | 18.x | Component-based UI |
| Language | TypeScript | 5.x | Type-safe JavaScript |
| State | React Context + Hooks | 18.x | Global state management |
| HTTP | Axios | 1.x | API client |
| Forms | React Hook Form | 7.x | Form validation |
| Styling | Tailwind CSS | 3.x | Utility-first CSS |
| Components | shadcn/ui | Latest | Accessible components |
| Charts | Recharts | 2.x | Dashboard visualizations |
| Testing | Vitest + RTL | 1.x / 14.x | Component tests |

### **2.3 Infrastructure**

| Component | Technology | Purpose |
|-----------|------------|---------|
| Database | Supabase (PostgreSQL 15) | Managed DB with backups |
| Frontend Hosting | AWS Amplify | Serverless hosting, CI/CD, CDN |
| Backend Hosting | AWS Elastic Beanstalk | Managed Java app hosting |
| Email | AWS SES | Transactional email |
| File Storage | AWS S3 | PDF invoice storage |
| Monitoring | AWS CloudWatch | Logs, metrics, alarms |
| CI/CD | GitHub Actions | Automated build/test/deploy |
| Secrets | AWS Parameter Store | Secure credentials |

---

## **3. Backend Architecture**

### **3.1 Vertical Slice Structure**

```
src/main/java/com/invoiceme/
├── customers/
│   ├── createcustomer/
│   │   ├── CreateCustomerCommand.java
│   │   ├── CreateCustomerHandler.java
│   │   ├── CreateCustomerValidator.java
│   │   └── CreateCustomerController.java
│   ├── updatecustomer/
│   │   ├── UpdateCustomerCommand.java
│   │   ├── UpdateCustomerHandler.java
│   │   └── UpdateCustomerController.java
│   ├── getcustomer/
│   │   ├── GetCustomerQuery.java
│   │   ├── GetCustomerHandler.java
│   │   └── GetCustomerResponse.java
│   ├── listcustomers/
│   │   ├── ListCustomersQuery.java
│   │   ├── ListCustomersHandler.java
│   │   └── ListCustomersResponse.java
│   ├── deletecustomer/
│   │   ├── DeleteCustomerCommand.java
│   │   └── DeleteCustomerHandler.java
│   ├── domain/
│   │   ├── Customer.java (Aggregate Root)
│   │   ├── CustomerRepository.java (Interface)
│   │   └── CustomerStatus.java (Enum)
│   └── infrastructure/
│       └── CustomerRepositoryImpl.java (JPA)
│
├── invoices/
│   ├── createinvoice/
│   ├── updateinvoice/
│   ├── markassent/
│   ├── cancelinvoice/
│   ├── getinvoice/
│   ├── listinvoices/
│   ├── generatepdf/
│   ├── domain/
│   │   ├── Invoice.java (Aggregate Root)
│   │   ├── LineItem.java (Entity)
│   │   ├── InvoiceNumber.java (Value Object)
│   │   ├── Money.java (Value Object)
│   │   └── InvoiceRepository.java
│   └── infrastructure/
│       └── InvoiceRepositoryImpl.java
│
├── payments/
│   ├── recordpayment/
│   ├── getpayment/
│   ├── listpayments/
│   ├── domain/
│   │   ├── Payment.java (Aggregate Root)
│   │   ├── PaymentMethod.java (Enum)
│   │   └── PaymentRepository.java
│   └── infrastructure/
│       └── PaymentRepositoryImpl.java
│
├── refunds/
│   ├── issuerefund/
│   │   ├── IssueRefundCommand.java
│   │   ├── IssueRefundHandler.java
│   │   └── IssueRefundController.java
│   └── domain/
│       └── RefundService.java
│
├── recurringinvoices/
│   ├── createtemplate/
│   ├── generatefromtemplate/
│   ├── pausetemplate/
│   ├── completetemplate/
│   ├── listtemplates/
│   ├── domain/
│   │   ├── RecurringInvoiceTemplate.java
│   │   └── RecurringInvoiceRepository.java
│   └── infrastructure/
│       └── RecurringInvoiceRepositoryImpl.java
│
├── users/
│   ├── register/
│   ├── login/
│   ├── approveaccount/
│   ├── resetpassword/
│   ├── domain/
│   │   ├── User.java
│   │   ├── UserRole.java (Enum)
│   │   └── UserRepository.java
│   └── infrastructure/
│       └── UserRepositoryImpl.java
│
├── shared/
│   ├── domain/
│   │   ├── DomainEvent.java (Interface)
│   │   ├── DomainEventPublisher.java
│   │   └── AggregateRoot.java (Base Class)
│   ├── infrastructure/
│   │   ├── JpaConfig.java
│   │   └── EventPublisherImpl.java
│   └── exceptions/
│       ├── DomainException.java
│       ├── ValidationException.java
│       └── NotFoundException.java
│
└── notifications/
    ├── EmailService.java (Interface)
    ├── AwsSesEmailService.java (Implementation)
    └── templates/
        ├── InvoiceSentTemplate.html
        ├── PaymentReceivedTemplate.html
        └── OverdueReminderTemplate.html
```

### **3.2 Domain Model (High-Level)**

**Customer Aggregate:**
- Properties: UUID, company name, contact name, email (value object), phone, address (value object), customer type, credit balance (Money value object), status
- Behavior: `applyCredit(Money)`, `deductCredit(Money)`, `canBeDeleted()`, `markAsInactive()`
- Domain Events: CreditAppliedEvent, CreditDeductedEvent, CustomerDeactivatedEvent

**Invoice Aggregate:**
- Properties: UUID, invoice number (value object), customer (reference), issue/due dates, status, payment terms, line items (list of entities), totals (Money), notes, timestamps, version
- Line Item Entity: description, quantity, unit price (Money), discount type/value, tax rate, calculated line total
- Behavior: `addLineItem()`, `removeLineItem()`, `markAsSent()`, `recordPayment(Money)`, `applyCreditDiscount(Money)`, `addLateFee(Money)`, `cancel()`, `isOverdue()`
- Domain Events: InvoiceSentEvent, InvoiceFullyPaidEvent, LateFeeAppliedEvent, InvoiceCancelledEvent

**Payment Aggregate:**
- Properties: UUID, invoice (reference), customer (reference), amount (Money), method, date, reference, status, created by, notes
- Behavior: Static factory `record()` - validates amount, updates invoice, handles overpayment to credit
- Domain Events: PaymentRecordedEvent

**RecurringInvoiceTemplate Aggregate:**
- Properties: UUID, customer, template name, frequency, start/end dates, next invoice date, status, line items, payment terms, auto-send flag
- Behavior: `generateInvoice()`, `pause()`, `resume()`, `complete()`, `calculateNextDate()`
- Domain Events: RecurringInvoiceGeneratedEvent

**Value Objects:**
- Money: Amount + currency, supports add/subtract/compare operations, immutable
- Email: Validates format, immutable
- InvoiceNumber: Format INV-YYYY-####, auto-generated, immutable
- Address: Street, city, state, zip, country, immutable

### **3.3 CQRS Implementation**

**Command Pattern:**
- Command = immutable DTO representing intent (e.g., CreateInvoiceCommand contains customerId, issueDate, lineItems)
- CommandHandler = service method that validates, executes business logic, persists, publishes events
- Example flow: Controller receives CreateInvoiceRequest → Maps to CreateInvoiceCommand → CreateInvoiceHandler validates, creates Invoice entity, saves to repository, publishes InvoiceCreatedEvent → Returns UUID

**Query Pattern:**
- Query = immutable DTO representing data request (e.g., GetInvoiceQuery contains invoiceId)
- QueryHandler = service method that retrieves data, maps to response DTO (no business logic)
- Example flow: Controller receives GET /invoices/{id} → Creates GetInvoiceQuery → GetInvoiceHandler retrieves Invoice entity, maps to InvoiceDetailResponse DTO → Returns flattened data

**Key Principle:** Commands modify state and publish events. Queries return read-only data.

### **3.4 Domain Events**

**Architecture:**
- Domain events are objects representing business-significant occurrences (e.g., InvoiceSentEvent)
- Aggregate roots collect events in a transient list during transaction
- After successful transaction commit, DomainEventPublisher publishes events
- Event listeners (annotated with `@TransactionalEventListener(AFTER_COMMIT)`) handle side effects

**Implementation Pattern:**
1. Aggregate method emits event: `addDomainEvent(new InvoiceSentEvent(this.id))`
2. CommandHandler saves aggregate: `invoiceRepository.save(invoice)`
3. CommandHandler publishes events: `eventPublisher.publishEvents(invoice)` (uses Spring's ApplicationEventPublisher)
4. Event listener executes: `@TransactionalEventListener void handleInvoiceSent(InvoiceSentEvent event) { emailService.send(...); }`

**Events:**
- InvoiceSentEvent → Send email notification, log to activity feed
- PaymentRecordedEvent → Send payment confirmation, update dashboard cache
- InvoiceFullyPaidEvent → Send completion notification, mark milestone
- LateFeeAppliedEvent → Send overdue reminder
- RecurringInvoiceGeneratedEvent → Notify admins

---

## **4. API Specifications**

### **4.1 RESTful Design**

- **Base URL**: `/api/v1`
- **Authentication**: JWT in `Authorization: Bearer <token>` header
- **Content-Type**: `application/json`
- **Pagination**: Query params `page`, `size`, `sort` (e.g., `?page=0&size=20&sort=createdAt,desc`)
- **Filtering**: Query params for domain filters (e.g., `?status=SENT&customerId=123`)

### **4.2 Customer Endpoints**

| Method | Endpoint | Description | Auth Roles | Request | Response |
|--------|----------|-------------|------------|---------|----------|
| POST | `/customers` | Create customer | SYSADMIN, ACCOUNTANT, SALES | CreateCustomerRequest | CustomerResponse (201) |
| GET | `/customers/{id}` | Get by ID | All | - | CustomerDetailResponse (200) |
| PUT | `/customers/{id}` | Update customer | SYSADMIN, ACCOUNTANT | UpdateCustomerRequest | CustomerResponse (200) |
| DELETE | `/customers/{id}` | Delete customer | SYSADMIN | - | 204 No Content |
| GET | `/customers` | List with filters | SYSADMIN, ACCOUNTANT, SALES | Query params | PagedCustomerResponse (200) |

### **4.3 Invoice Endpoints**

| Method | Endpoint | Description | Auth Roles | Request | Response |
|--------|----------|-------------|------------|---------|----------|
| POST | `/invoices` | Create invoice | SYSADMIN, ACCOUNTANT, SALES | CreateInvoiceRequest | InvoiceResponse (201) |
| GET | `/invoices/{id}` | Get by ID | All (filtered) | - | InvoiceDetailResponse (200) |
| PUT | `/invoices/{id}` | Update invoice | SYSADMIN, ACCOUNTANT, SALES | UpdateInvoiceRequest | InvoiceResponse (200) |
| PATCH | `/invoices/{id}/mark-as-sent` | Mark sent | SYSADMIN, ACCOUNTANT, SALES | - | InvoiceResponse (200) |
| DELETE | `/invoices/{id}` | Cancel invoice | SYSADMIN | - | 204 No Content |
| GET | `/invoices` | List with filters | All (filtered) | Query params | PagedInvoiceResponse (200) |
| GET | `/invoices/{id}/pdf` | Generate PDF | All (filtered) | - | Binary PDF (200) |

### **4.4 Payment Endpoints**

| Method | Endpoint | Description | Auth Roles | Request | Response |
|--------|----------|-------------|------------|---------|----------|
| POST | `/payments` | Record payment | SYSADMIN, ACCOUNTANT, CUSTOMER | RecordPaymentRequest | PaymentResponse (201) |
| GET | `/payments/{id}` | Get by ID | SYSADMIN, ACCOUNTANT | - | PaymentDetailResponse (200) |
| GET | `/payments` | List with filters | SYSADMIN, ACCOUNTANT | Query params | PagedPaymentResponse (200) |

### **4.5 Refund Endpoints**

| Method | Endpoint | Description | Auth Roles | Request | Response |
|--------|----------|-------------|------------|---------|----------|
| POST | `/refunds` | Issue refund | SYSADMIN | IssueRefundRequest | RefundResponse (201) |
| GET | `/refunds` | List refunds | SYSADMIN | Query params | PagedRefundResponse (200) |

### **4.6 Recurring Invoice Endpoints**

| Method | Endpoint | Description | Auth Roles | Request | Response |
|--------|----------|-------------|------------|---------|----------|
| POST | `/recurring-invoices` | Create template | SYSADMIN, ACCOUNTANT | CreateTemplateRequest | TemplateResponse (201) |
| GET | `/recurring-invoices/{id}` | Get by ID | SYSADMIN, ACCOUNTANT | - | TemplateDetailResponse (200) |
| PATCH | `/recurring-invoices/{id}/pause` | Pause template | SYSADMIN, ACCOUNTANT | - | TemplateResponse (200) |
| PATCH | `/recurring-invoices/{id}/resume` | Resume template | SYSADMIN, ACCOUNTANT | - | TemplateResponse (200) |
| PATCH | `/recurring-invoices/{id}/complete` | Complete template | SYSADMIN, ACCOUNTANT | - | TemplateResponse (200) |
| GET | `/recurring-invoices` | List templates | SYSADMIN, ACCOUNTANT | Query params | PagedTemplateResponse (200) |

### **4.7 User & Auth Endpoints**

| Method | Endpoint | Description | Auth Roles | Request | Response |
|--------|----------|-------------|------------|---------|----------|
| POST | `/auth/register` | Register user | Public | RegisterRequest | RegisterResponse (201) |
| POST | `/auth/login` | Login | Public | LoginRequest | LoginResponse (200) |
| POST | `/auth/logout` | Logout | All | - | 204 No Content |
| POST | `/auth/forgot-password` | Request reset | Public | ForgotPasswordRequest | 204 No Content |
| POST | `/auth/reset-password` | Reset password | Public | ResetPasswordRequest | 204 No Content |
| GET | `/users/pending` | Get pending users | SYSADMIN | - | PendingUserListResponse (200) |
| PATCH | `/users/{id}/approve` | Approve user | SYSADMIN | - | UserResponse (200) |
| PATCH | `/users/{id}/reject` | Reject user | SYSADMIN | - | 204 No Content |

### **4.8 Dashboard Endpoints**

| Method | Endpoint | Description | Auth Roles | Request | Response |
|--------|----------|-------------|------------|---------|----------|
| GET | `/dashboard/metrics` | Key metrics | SYSADMIN, ACCOUNTANT | - | DashboardMetricsResponse (200) |
| GET | `/dashboard/revenue-trend` | Revenue by month | SYSADMIN, ACCOUNTANT | Query params | RevenueTrendResponse (200) |
| GET | `/dashboard/invoice-status` | Status breakdown | SYSADMIN, ACCOUNTANT | - | InvoiceStatusResponse (200) |
| GET | `/dashboard/aging-report` | Aging report | SYSADMIN, ACCOUNTANT | - | AgingReportResponse (200) |

### **4.9 DTO Examples**

The API tables in sections 4.2-4.8 show request/response structure. Here are explicit DTO class definitions for key endpoints:

**CreateInvoiceRequest (Command DTO):**
```typescript
interface CreateInvoiceRequest {
  customerId: string;           // UUID
  issueDate: string;             // ISO date: "2025-02-01"
  paymentTerms: "NET_30" | "DUE_ON_RECEIPT" | "CUSTOM";
  dueDate?: string;              // Optional, for CUSTOM terms
  lineItems: LineItemDTO[];
  notes?: string;
}

interface LineItemDTO {
  description: string;
  quantity: number;              // Integer, min 1
  unitPrice: number;             // Decimal
  discountType: "NONE" | "PERCENTAGE" | "FIXED";
  discountValue: number;         // Percentage or fixed amount
  taxRate: number;               // Percentage (e.g., 8.25 for 8.25%)
}
```

**InvoiceDetailResponse (Query DTO):**
```typescript
interface InvoiceDetailResponse {
  id: string;                    // UUID
  invoiceNumber: string;         // "INV-2025-0001"
  customer: {
    id: string;
    companyName: string;
    email: string;
  };
  issueDate: string;             // ISO date
  dueDate: string;               // ISO date
  status: "DRAFT" | "SENT" | "PAID" | "OVERDUE" | "CANCELLED";
  paymentTerms: string;
  lineItems: LineItemResponse[];
  subtotal: number;
  taxAmount: number;
  discountAmount: number;
  totalAmount: number;
  amountPaid: number;
  balanceDue: number;
  notes?: string;
  sentDate?: string;             // ISO datetime
  paidDate?: string;             // ISO datetime
  payments: PaymentSummary[];
  pdfUrl: string;                // Download link
}

interface LineItemResponse {
  id: string;
  description: string;
  quantity: number;
  unitPrice: number;
  discountType: string;
  discountValue: number;
  taxRate: number;
  lineTotal: number;             // Calculated
}
```

**RecordPaymentRequest (Command DTO):**
```typescript
interface RecordPaymentRequest {
  invoiceId: string;             // UUID
  amount: number;                // Must be > 0
  paymentMethod: "CREDIT_CARD" | "ACH";
  paymentDate: string;           // ISO date
  paymentReference?: string;     // Optional (e.g., "VISA-4532")
  notes?: string;
}
```

**Note**: Java backend uses equivalent classes with annotations (@Valid, @NotNull, etc.). Frontend uses these TypeScript interfaces.

---

## **5. Database Schema**

### **5.1 Entity Relationship Diagram**

```
customers (1) ────< (N) invoices (1) ────< (N) line_items
    │                      │
    │                      │
    │                      └────< (N) payments
    │
    └────< (N) recurring_invoice_templates (1) ────< (N) template_line_items

users (N) ──> (1) customers (optional FK for customer role)

activity_feed (logs domain events)
password_reset_tokens (for password reset flow)
```

### **5.2 Core Tables**

**customers**
- `id` UUID PK, `company_name` VARCHAR(255), `contact_name` VARCHAR(255), `email` VARCHAR(255) UNIQUE, `phone` VARCHAR(50), `street` VARCHAR(255), `city` VARCHAR(100), `state` VARCHAR(50), `zip_code` VARCHAR(20), `country` VARCHAR(100), `customer_type` ENUM('RESIDENTIAL', 'COMMERCIAL', 'INSURANCE'), `credit_balance` DECIMAL(19,2) DEFAULT 0, `status` ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE', `created_at` TIMESTAMP, `updated_at` TIMESTAMP
- Indexes: email, status, customer_type

**invoices**
- `id` UUID PK, `invoice_number` VARCHAR(20) UNIQUE, `customer_id` UUID FK, `issue_date` DATE, `due_date` DATE, `status` ENUM('DRAFT', 'SENT', 'PAID', 'OVERDUE', 'CANCELLED'), `payment_terms` ENUM('NET_30', 'DUE_ON_RECEIPT', 'CUSTOM'), `subtotal` DECIMAL(19,2), `tax_amount` DECIMAL(19,2), `discount_amount` DECIMAL(19,2), `total_amount` DECIMAL(19,2), `amount_paid` DECIMAL(19,2) DEFAULT 0, `balance_due` DECIMAL(19,2), `notes` TEXT, `sent_date` TIMESTAMP, `paid_date` TIMESTAMP, `version` INT DEFAULT 1, `created_at` TIMESTAMP, `updated_at` TIMESTAMP
- Indexes: invoice_number, customer_id, status, due_date, issue_date

**line_items**
- `id` UUID PK, `invoice_id` UUID FK (CASCADE DELETE), `description` VARCHAR(500), `quantity` INT CHECK (quantity >= 1), `unit_price` DECIMAL(19,2), `discount_type` ENUM('NONE', 'PERCENTAGE', 'FIXED'), `discount_value` DECIMAL(10,2), `tax_rate` DECIMAL(5,2) DEFAULT 0, `sort_order` INT, `created_at` TIMESTAMP
- Indexes: invoice_id

**payments**
- `id` UUID PK, `invoice_id` UUID FK, `customer_id` UUID FK, `amount` DECIMAL(19,2) CHECK (amount > 0), `payment_method` ENUM('CREDIT_CARD', 'ACH'), `payment_date` DATE, `payment_reference` VARCHAR(100), `status` ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'), `created_by_user_id` UUID FK, `created_at` TIMESTAMP, `notes` TEXT
- Indexes: invoice_id, customer_id, payment_date, payment_method

**recurring_invoice_templates**
- `id` UUID PK, `customer_id` UUID FK, `template_name` VARCHAR(255), `frequency` ENUM('MONTHLY', 'QUARTERLY', 'ANNUALLY'), `start_date` DATE, `end_date` DATE, `next_invoice_date` DATE, `status` ENUM('ACTIVE', 'PAUSED', 'COMPLETED'), `payment_terms` ENUM('NET_30', 'DUE_ON_RECEIPT', 'CUSTOM'), `auto_send` BOOLEAN DEFAULT FALSE, `created_by_user_id` UUID FK, `created_at` TIMESTAMP, `updated_at` TIMESTAMP
- Indexes: customer_id, next_invoice_date, status

**template_line_items**
- `id` UUID PK, `template_id` UUID FK (CASCADE DELETE), `description` VARCHAR(500), `quantity` INT, `unit_price` DECIMAL(19,2), `discount_type` ENUM, `discount_value` DECIMAL(10,2), `tax_rate` DECIMAL(5,2), `sort_order` INT
- Indexes: template_id

**users**
- `id` UUID PK, `email` VARCHAR(255) UNIQUE, `password_hash` VARCHAR(255), `full_name` VARCHAR(255), `role` ENUM('SYSADMIN', 'ACCOUNTANT', 'SALES', 'CUSTOMER'), `customer_id` UUID FK (nullable), `status` ENUM('PENDING', 'ACTIVE', 'INACTIVE', 'LOCKED'), `failed_login_count` INT DEFAULT 0, `locked_until` TIMESTAMP, `created_at` TIMESTAMP, `updated_at` TIMESTAMP
- Indexes: email, role, status

**activity_feed**
- `id` UUID PK, `aggregate_id` UUID, `event_type` VARCHAR(100), `description` TEXT, `occurred_at` TIMESTAMP, `user_id` UUID FK (nullable)
- Indexes: occurred_at (DESC), aggregate_id

**password_reset_tokens**
- `id` UUID PK, `user_id` UUID FK, `token` VARCHAR(255) UNIQUE, `expires_at` TIMESTAMP, `used` BOOLEAN DEFAULT FALSE, `created_at` TIMESTAMP
- Indexes: token, user_id

### **5.3 Flyway Migrations**

- Version-controlled SQL files: `V1__create_customers_table.sql`, `V2__create_invoices_table.sql`, etc.
- Naming: `V{version}__{description}.sql`
- Execution: Flyway runs migrations in order on application startup
- Checksum validation prevents accidental changes
- Migration files are immutable once deployed

---

## **6. Frontend Architecture**

### **6.1 MVVM Pattern**

- **Model**: TypeScript interfaces matching backend DTOs (Customer, Invoice, Payment)
- **View**: React components (presentational, receive props, emit events)
- **ViewModel**: Custom hooks managing state, API calls, business logic (e.g., `useInvoiceDetail`, `usePayments`)

**Example Flow:**
1. Component calls hook: `const { invoice, loading, markAsSent } = useInvoiceDetail(id)`
2. Hook fetches data from API: `GET /api/v1/invoices/{id}`
3. Hook manages loading/error states, exposes data and actions
4. Component renders data, calls actions on user interaction

### **6.2 Component Structure**

```
src/
├── app/                          (Next.js App Router)
│   ├── layout.tsx                (Root layout with nav)
│   ├── dashboard/page.tsx        (Admin dashboard)
│   ├── customers/
│   │   ├── page.tsx              (Customer list)
│   │   ├── [id]/page.tsx         (Customer detail)
│   │   └── new/page.tsx          (Create customer)
│   ├── invoices/
│   │   ├── page.tsx              (Invoice list)
│   │   ├── [id]/page.tsx         (Invoice detail)
│   │   └── new/page.tsx          (Create invoice)
│   ├── payments/page.tsx         (Payment list)
│   ├── recurring-invoices/
│   │   ├── page.tsx              (Template list)
│   │   └── new/page.tsx          (Create template)
│   ├── login/page.tsx
│   └── register/page.tsx
│
├── components/
│   ├── ui/                       (shadcn/ui primitives)
│   ├── layout/                   (Header, Sidebar, Footer)
│   ├── customers/                (Customer-specific components)
│   ├── invoices/                 (Invoice-specific components)
│   ├── payments/                 (Payment-specific components)
│   ├── dashboard/                (Dashboard widgets)
│   └── shared/                   (Reusable: LoadingSpinner, ErrorMessage, Pagination)
│
├── hooks/                        (Custom hooks - ViewModels)
│   ├── useCustomers.ts
│   ├── useInvoiceDetail.ts
│   ├── usePayments.ts
│   ├── useDashboard.ts
│   └── useAuth.ts
│
├── lib/
│   ├── api.ts                    (Axios instance with interceptors)
│   ├── auth.ts                   (JWT token management)
│   └── utils.ts                  (Utility functions)
│
├── types/                        (TypeScript interfaces - Models)
│   ├── customer.ts
│   ├── invoice.ts
│   ├── payment.ts
│   └── user.ts
│
└── contexts/
    └── AuthContext.tsx           (Global auth state)
```

### **6.3 API Integration**

- Axios instance configured with base URL (`process.env.NEXT_PUBLIC_API_URL`)
- Request interceptor: Adds JWT token from localStorage to `Authorization` header
- Response interceptor: Handles 401 (redirect to login), global error messages
- Custom hooks wrap API calls: Loading states, error handling, data transformation

---

## **7. Authentication & Authorization**

### **7.1 JWT Authentication**

**Token Structure:**
- Payload: `{ sub: userId, email, role, customerId, iat, exp }`
- Signing: HMAC SHA-512 with secret key
- Expiry: 24 hours
- Storage: HttpOnly cookie (frontend), localStorage fallback

**Login Flow:**
1. User submits email/password → POST `/auth/login`
2. Backend validates credentials, generates JWT token
3. Response: `{ token, user: { id, email, role, ... } }`
4. Frontend stores token, sets AuthContext
5. Subsequent requests include token in `Authorization: Bearer <token>` header

**Security Features:**
- Password hashing: Bcrypt with 10+ rounds
- Failed login tracking: 5 attempts → 1-hour account lockout
- Token validation on every request (Spring Security filter chain)
- HTTPS only in production

### **7.2 Role-Based Access Control**

- Enforced at two layers: API (Spring Security `@PreAuthorize`) and UI (conditional rendering)
- API example: `@PreAuthorize("hasAnyRole('SYSADMIN', 'SALES')")` on create invoice endpoint
- UI example: Render "Delete Customer" button only if `user.role === 'SYSADMIN'`
- Customer role: Additional check to verify ownership (e.g., cannot pay other customers' invoices)

---

## **8. Email & Notification System**

**AWS SES Integration:**
- Service: `AwsSesEmailService` implements `EmailService` interface
- Sends HTML emails using Thymeleaf templates
- Methods: `sendInvoiceSentNotification()`, `sendPaymentReceivedNotification()`, `sendOverdueReminderNotification()`, etc.
- Configuration: From email = `noreply@invoiceme.com`, AWS region = us-east-1
- Triggered by domain event listeners (e.g., `@TransactionalEventListener` on InvoiceSentEvent)

---

## **9. Deployment Architecture**

### **9.1 AWS Amplify (Frontend)**

- Hosts Next.js application (SSR + static assets)
- Auto-deploys from GitHub main branch
- Built-in CDN (CloudFront) for global distribution
- HTTPS by default with custom domain support
- Environment variables: `NEXT_PUBLIC_API_URL`

### **9.2 AWS Elastic Beanstalk (Backend)**

- Hosts Spring Boot JAR file
- Auto-scaling: Min 1, Max 3 instances
- Load balancer with health checks
- Rolling updates (zero downtime)
- Environment variables: `DATABASE_URL`, `JWT_SECRET`, `AWS_SES_REGION`, etc.

### **9.3 Supabase (Database)**

- Managed PostgreSQL 15 with connection pooling (pgBouncer)
- Automatic daily backups with point-in-time recovery
- Connection string provided via environment variable
- No VPC configuration needed (vs. AWS RDS)

### **9.4 AWS S3 (PDF Storage)**

- Stores generated invoice PDFs
- Key format: `invoices/{invoiceId}.pdf`
- Public read access (signed URLs for security - optional)
- Lifecycle policy: Retain indefinitely

### **9.5 AWS CloudWatch (Monitoring)**

- Application logs streamed from Elastic Beanstalk
- Custom metrics: API latency (p50, p95, p99), error rate, invoice creation rate
- Alarms: API error rate > 5%, database connection pool exhausted

---

## **10. CI/CD Pipeline**

### **10.1 GitHub Actions Workflow**

**On Push to Main Branch:**
1. **Test Backend**: Run JUnit tests, build JAR
2. **Test Frontend**: Run Vitest tests, build production bundle
3. **Deploy Backend**: Upload JAR to Elastic Beanstalk, rolling update
4. **Deploy Frontend**: Push to AWS Amplify, invalidate CDN cache
5. **Notify**: Slack notification on success/failure

**Workflow File:** `.github/workflows/deploy.yml`

**Total Deployment Time:** ~5-10 minutes

---

## **11. Development Workflow**

### **11.1 Local Setup**

**Backend:**
1. Clone repository
2. Set environment variables: `DATABASE_URL`, `JWT_SECRET`, `AWS_*`
3. Run Flyway migrations: `./mvnw flyway:migrate`
4. Start Spring Boot: `./mvnw spring-boot:run`
5. API available at `http://localhost:8080`

**Frontend:**
1. Navigate to frontend directory
2. Install dependencies: `npm install`
3. Set environment: `NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1`
4. Start dev server: `npm run dev`
5. App available at `http://localhost:3000`

### **11.2 Development Guidelines**

**Backend:**
- Use Vertical Slice Architecture: Each feature in own folder
- Commands for writes, Queries for reads
- Emit domain events for side effects
- Write integration tests for all commands
- Use MapStruct for DTO mapping

**Frontend:**
- Use MVVM: Custom hooks for logic, components for presentation
- TypeScript strict mode (no `any`)
- React Hook Form for forms
- Tailwind for styling (no custom CSS)
- Component tests with Vitest + React Testing Library

**Git Workflow:**
- Feature branch: `feature/create-invoice`
- Commit messages: "feat: Add create invoice handler"
- Pull request with tests passing
- Squash merge to main

### **11.3 Implementation Guidance for AI/Cursor**

**UI Design:**
- Use **shadcn/ui components** for consistent, accessible design (specified in PRD 2, Section 2.2)
- Reference: https://ui.shadcn.com/docs/components
- Color scheme: Tailwind default theme or customize in `tailwind.config.ts`
- Layout: Clean, modern ERP style (similar to Ramp, QuickBooks Online)
- No full mockups provided - use shadcn/ui examples for component patterns

**Email Templates:**
- Create simple HTML templates based on content descriptions in PRD 1, Section 4.10
- Use inline CSS for email client compatibility
- Keep templates under 200 lines each
- Test with Thymeleaf variables (e.g., `${invoice.invoiceNumber}`)

**PDF Generation:**
- Reference **iText 7** documentation for layout: https://kb.itextpdf.com/
- Invoice format specified in PRD 1, Section 4.2
- Include: Header, line items table, totals, payment info
- Add "PAID" watermark for paid invoices using `PdfCanvas` API

**External Documentation References:**
When Cursor needs implementation details beyond PRDs, reference:

1. **Spring Boot** - Dependency injection, annotations
   - `@Service`, `@Transactional`, `@PreAuthorize`
   - Spring Data JPA repository patterns
   - Spring Security configuration

2. **React/Next.js** - Component lifecycle, hooks
   - `useState`, `useEffect`, `useContext`
   - Next.js App Router file structure
   - Server-side rendering patterns

3. **iText 7** - PDF generation library API
   - Document, Paragraph, Table classes
   - Cell styling and borders
   - Watermark overlay

4. **AWS SDK** - Cloud service integration
   - AWS SES for email (SendEmailRequest)
   - AWS S3 for file storage (PutObjectRequest)
   - Credentials via environment variables

**When in Doubt:**
- Business logic → Reference PRD 1
- Architecture patterns → Reference PRD 2, Sections 1-3
- API structure → Reference PRD 2, Section 4
- Database schema → Reference PRD 2, Section 5

---

## **12. Performance & Monitoring**

### **12.1 Performance Targets**

| Metric | Target | Measurement |
|--------|--------|-------------|
| API Response (CRUD) | <200ms | p95 |
| Page Load | <2s | First Contentful Paint |
| Dashboard Load | <2s | Full page load |
| PDF Generation | <3s | Time to download |

### **12.2 Optimizations**

**Backend:**
- Database indexing on foreign keys and frequently queried fields
- HikariCP connection pooling (max 10 connections)
- Pagination on all list endpoints
- DTO projection queries (select only needed columns)

**Frontend:**
- Next.js server-side rendering for initial load
- Code splitting (dynamic imports)
- Memoization (useMemo, useCallback)
- Debounced search inputs

### **12.3 Monitoring**

**CloudWatch Metrics:**
- API latency (p50, p95, p99)
- Error rate (4xx, 5xx)
- Database connection pool utilization

**CloudWatch Alarms:**
- Error rate > 5% → SNS notification
- Database pool exhausted → SNS notification

**Logging:**
- Structured JSON logs
- Levels: ERROR, WARN, INFO, DEBUG
- Sensitive data excluded (passwords, tokens)

---

**End of PRD 2: Technical Specification Document**
