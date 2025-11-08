# Database Schema Documentation

**Last Updated**: 2025-01-27  
**Database**: PostgreSQL 15.x  
**Migration Tool**: Flyway 9.x

---

## Overview

The InvoiceMe database schema supports a production-quality ERP-style invoicing system with full support for Customers, Invoices, Payments, Recurring Invoices, Refunds, Users, and Activity Feed. The schema follows PostgreSQL best practices with proper constraints, indexes, and foreign key relationships.

**Total Tables**: 9  
**Total Migrations**: 10  
**Total Indexes**: 40+ (including composite indexes)

---

## Table Descriptions

### 1. customers

**Purpose**: Stores customer information including billing details, credit balance, and account status.

**Key Fields**:
- `id` (UUID, PK): Unique customer identifier
- `email` (VARCHAR(255), UNIQUE): Customer email address (unique constraint)
- `company_name` (VARCHAR(255)): Company or individual name
- `credit_balance` (DECIMAL(19,2)): Available credit (calculated from overpayments)
- `customer_type` (ENUM): RESIDENTIAL, COMMERCIAL, or INSURANCE
- `status` (ENUM): ACTIVE, INACTIVE, or SUSPENDED

**Constraints**:
- `email` UNIQUE (enforced at database level)
- `credit_balance >= 0` CHECK constraint
- `updated_at` automatically updated via trigger

**Indexes**:
- `idx_customers_email` (email) - Fast email lookups
- `idx_customers_status` (status) - Filter by status
- `idx_customers_customer_type` (customer_type) - Filter by type
- `idx_customers_outstanding` (status, credit_balance) - Customers with outstanding balance

**Relationships**:
- One-to-Many with `invoices` (customer_id FK)
- One-to-Many with `recurring_invoice_templates` (customer_id FK)
- One-to-Many with `payments` (customer_id FK)
- One-to-Many with `users` (customer_id FK, nullable for customer portal users)

---

### 2. invoices

**Purpose**: Stores invoice records with line items, payment tracking, and lifecycle status.

**Key Fields**:
- `id` (UUID, PK): Unique invoice identifier
- `invoice_number` (VARCHAR(20), UNIQUE): Auto-generated format INV-YYYY-####
- `customer_id` (UUID, FK): Reference to customers table
- `status` (ENUM): DRAFT, SENT, PAID, OVERDUE, or CANCELLED
- `total_amount` (DECIMAL(19,2)): Calculated total including tax
- `amount_paid` (DECIMAL(19,2)): Sum of all payments
- `balance_due` (DECIMAL(19,2)): Remaining balance (total_amount - amount_paid)
- `version` (INT): Optimistic locking version number

**Constraints**:
- `invoice_number` UNIQUE (enforced at database level)
- All monetary fields >= 0 CHECK constraints
- `version >= 1` CHECK constraint
- `updated_at` automatically updated via trigger

**Indexes**:
- `idx_invoices_invoice_number` (invoice_number) - Fast invoice number lookups
- `idx_invoices_customer_id` (customer_id) - Foreign key index
- `idx_invoices_status` (status) - Filter by status
- `idx_invoices_due_date` (due_date) - Overdue invoice queries
- `idx_invoices_issue_date` (issue_date) - Date range queries
- `idx_invoices_customer_status` (customer_id, status) - Composite for common query
- `idx_invoices_customer_issue_date` (customer_id, issue_date DESC) - Customer invoice history
- `idx_invoices_overdue` (status, due_date) - Overdue invoice filtering

**Relationships**:
- Many-to-One with `customers` (customer_id FK, RESTRICT DELETE)
- One-to-Many with `line_items` (invoice_id FK, CASCADE DELETE)
- One-to-Many with `payments` (invoice_id FK, RESTRICT DELETE)

**Lifecycle States**:
- **DRAFT**: Fully editable, not visible to customer
- **SENT**: Emailed to customer, line items editable with version tracking
- **PAID**: Balance = $0, locked (cannot edit/delete)
- **OVERDUE**: Auto-flagged when Current Date > Due Date AND Balance > 0
- **CANCELLED**: Voided with audit trail, cannot reactivate

---

### 3. line_items

**Purpose**: Stores individual line items for invoices (services/products, quantity, pricing, discounts, tax).

**Key Fields**:
- `id` (UUID, PK): Unique line item identifier
- `invoice_id` (UUID, FK): Reference to invoices table
- `description` (VARCHAR(500)): Line item description
- `quantity` (INT): Quantity (minimum 1)
- `unit_price` (DECIMAL(19,2)): Price per unit
- `discount_type` (ENUM): NONE, PERCENTAGE, or FIXED
- `discount_value` (DECIMAL(10,2)): Discount amount or percentage
- `tax_rate` (DECIMAL(5,2)): Tax rate percentage (0-100)
- `sort_order` (INT): Display order

**Constraints**:
- `quantity >= 1` CHECK constraint
- `unit_price >= 0` CHECK constraint
- `discount_value >= 0` CHECK constraint
- `tax_rate >= 0 AND tax_rate <= 100` CHECK constraint
- CASCADE DELETE: Deleting invoice deletes all line items

**Indexes**:
- `idx_line_items_invoice_id` (invoice_id) - Foreign key index
- `idx_line_items_sort_order` (invoice_id, sort_order) - Ordering line items

**Relationships**:
- Many-to-One with `invoices` (invoice_id FK, CASCADE DELETE)

**Calculation Logic**:
```
Base Amount = Quantity × Unit Price
Discount Amount = (Type == Percentage) ? Base × (Value / 100) : Value
Taxable Amount = Base - Discount
Tax Amount = Taxable × (Tax Rate / 100)
Line Total = Taxable + Tax
```

---

### 4. payments

**Purpose**: Stores payment records applied to invoices, tracking payment method, date, and status.

**Key Fields**:
- `id` (UUID, PK): Unique payment identifier
- `invoice_id` (UUID, FK): Reference to invoices table
- `customer_id` (UUID, FK): Reference to customers table (denormalized for performance)
- `amount` (DECIMAL(19,2)): Payment amount (must be > 0)
- `payment_method` (ENUM): CREDIT_CARD or ACH
- `payment_date` (DATE): Date payment was received
- `payment_reference` (VARCHAR(100)): Optional reference (e.g., "VISA-4532")
- `status` (ENUM): PENDING, COMPLETED, FAILED, or REFUNDED
- `created_by_user_id` (UUID, FK): User who recorded the payment

**Constraints**:
- `amount > 0` CHECK constraint (strictly positive)
- Foreign keys to invoices and customers (RESTRICT DELETE)
- Foreign key to users (SET NULL on delete)

**Indexes**:
- `idx_payments_invoice_id` (invoice_id) - Foreign key index
- `idx_payments_customer_id` (customer_id) - Foreign key index
- `idx_payments_payment_date` (payment_date) - Date range queries
- `idx_payments_payment_method` (payment_method) - Filter by method
- `idx_payments_customer_date` (customer_id, payment_date DESC) - Customer payment history
- `idx_payments_customer_status` (customer_id, status, payment_date DESC) - Customer payment history with status
- `idx_payments_invoice_status` (invoice_id, status, payment_date DESC) - Invoice payment history

**Relationships**:
- Many-to-One with `invoices` (invoice_id FK, RESTRICT DELETE)
- Many-to-One with `customers` (customer_id FK, RESTRICT DELETE)
- Many-to-One with `users` (created_by_user_id FK, SET NULL on delete)

**Business Rules**:
- Payment amount > 0 (enforced by CHECK constraint)
- If payment > invoice balance, excess goes to customer credit balance
- If payment = invoice balance, invoice status changes to PAID
- Refunds create negative payment records with status = REFUNDED

---

### 5. users

**Purpose**: Stores user accounts for authentication and authorization (SysAdmin, Accountant, Sales, Customer).

**Key Fields**:
- `id` (UUID, PK): Unique user identifier
- `email` (VARCHAR(255), UNIQUE): User email (unique constraint)
- `password_hash` (VARCHAR(255)): Bcrypt hashed password
- `full_name` (VARCHAR(255)): User's full name
- `role` (ENUM): SYSADMIN, ACCOUNTANT, SALES, or CUSTOMER
- `customer_id` (UUID, FK, nullable): Reference to customers table (for CUSTOMER role)
- `status` (ENUM): PENDING, ACTIVE, INACTIVE, or LOCKED
- `failed_login_count` (INT): Failed login attempts (0-5)
- `locked_until` (TIMESTAMP): Account lockout expiration

**Constraints**:
- `email` UNIQUE (enforced at database level)
- `failed_login_count >= 0` CHECK constraint
- `customer_id` nullable (only required for CUSTOMER role)
- `updated_at` automatically updated via trigger

**Indexes**:
- `idx_users_email` (email) - Fast email lookups (authentication)
- `idx_users_role` (role) - Role-based queries
- `idx_users_status` (status) - Filter by status
- `idx_users_role_status` (role, status) - Composite for common query
- `idx_users_customer_portal` (role, customer_id) - Customer portal users
- `idx_users_pending_approval` (status, created_at) - Pending user approval queries

**Relationships**:
- Many-to-One with `customers` (customer_id FK, nullable, SET NULL on delete)
- One-to-Many with `payments` (created_by_user_id FK)
- One-to-Many with `recurring_invoice_templates` (created_by_user_id FK)
- One-to-Many with `activity_feed` (user_id FK)
- One-to-Many with `password_reset_tokens` (user_id FK)

**Authentication Flow**:
1. User registers → Status = PENDING
2. SysAdmin approves → Status = ACTIVE
3. Failed login attempts tracked → After 5 attempts, account locked for 1 hour
4. Password reset tokens stored in `password_reset_tokens` table

---

### 6. recurring_invoice_templates

**Purpose**: Stores templates for recurring invoices (monthly, quarterly, annually) with scheduled generation.

**Key Fields**:
- `id` (UUID, PK): Unique template identifier
- `customer_id` (UUID, FK): Reference to customers table
- `template_name` (VARCHAR(255)): Template name/description
- `frequency` (ENUM): MONTHLY, QUARTERLY, or ANNUALLY
- `start_date` (DATE): First invoice generation date
- `end_date` (DATE, nullable): Optional end date
- `next_invoice_date` (DATE): Calculated next generation date
- `status` (ENUM): ACTIVE, PAUSED, or COMPLETED
- `auto_send` (BOOLEAN): If true, generated invoices are automatically marked as SENT
- `created_by_user_id` (UUID, FK): User who created the template

**Constraints**:
- `end_date >= start_date` CHECK constraint (if end_date is not NULL)
- `next_invoice_date >= start_date` CHECK constraint
- Foreign keys to customers and users (RESTRICT DELETE)
- `updated_at` automatically updated via trigger

**Indexes**:
- `idx_templates_customer_id` (customer_id) - Foreign key index
- `idx_templates_next_invoice_date` (next_invoice_date) - Scheduled job queries
- `idx_templates_status` (status) - Filter by status
- `idx_templates_status_next_date` (status, next_invoice_date) - Composite for scheduled job
- `idx_templates_scheduled_job` (status, next_invoice_date) - Optimized for scheduled job WHERE status = 'ACTIVE'

**Relationships**:
- Many-to-One with `customers` (customer_id FK, RESTRICT DELETE)
- Many-to-One with `users` (created_by_user_id FK, RESTRICT DELETE)
- One-to-Many with `template_line_items` (template_id FK, CASCADE DELETE)

**Scheduled Job Logic**:
- Daily job runs at midnight Central Time
- Checks templates where `status = 'ACTIVE'` AND `next_invoice_date <= CURRENT_DATE`
- Generates invoice from template line items
- Updates `next_invoice_date` by adding frequency period
- If `auto_send = TRUE`, invoice status = SENT, email sent
- If `auto_send = FALSE`, invoice status = DRAFT

---

### 7. template_line_items

**Purpose**: Stores line items for recurring invoice templates (predefined items copied to generated invoices).

**Key Fields**:
- `id` (UUID, PK): Unique template line item identifier
- `template_id` (UUID, FK): Reference to recurring_invoice_templates table
- `description` (VARCHAR(500)): Line item description
- `quantity` (INT): Quantity (minimum 1)
- `unit_price` (DECIMAL(19,2)): Price per unit
- `discount_type` (ENUM): NONE, PERCENTAGE, or FIXED
- `discount_value` (DECIMAL(10,2)): Discount amount or percentage
- `tax_rate` (DECIMAL(5,2)): Tax rate percentage (0-100)
- `sort_order` (INT): Display order

**Constraints**:
- `quantity >= 1` CHECK constraint
- `unit_price >= 0` CHECK constraint
- `discount_value >= 0` CHECK constraint
- `tax_rate >= 0 AND tax_rate <= 100` CHECK constraint
- CASCADE DELETE: Deleting template deletes all template line items

**Indexes**:
- `idx_template_line_items_template_id` (template_id) - Foreign key index
- `idx_template_line_items_sort_order` (template_id, sort_order) - Ordering line items

**Relationships**:
- Many-to-One with `recurring_invoice_templates` (template_id FK, CASCADE DELETE)

**Usage**:
- When scheduled job generates invoice from template, copies all template_line_items to line_items table
- Line items maintain same structure (description, quantity, unit_price, discount, tax_rate)

---

### 8. activity_feed

**Purpose**: Stores audit trail and activity log for domain events (InvoiceSent, PaymentRecorded, etc.).

**Key Fields**:
- `id` (UUID, PK): Unique activity record identifier
- `aggregate_id` (UUID): ID of the aggregate root (invoice_id, customer_id, payment_id, etc.)
- `event_type` (VARCHAR(100)): Event type (e.g., "InvoiceSent", "PaymentRecorded")
- `description` (TEXT): Human-readable description of the event
- `occurred_at` (TIMESTAMP): When the event occurred (defaults to CURRENT_TIMESTAMP)
- `user_id` (UUID, FK, nullable): User who triggered the event (nullable for system events)

**Constraints**:
- `user_id` nullable (system events don't have a user)
- Foreign key to users (SET NULL on delete)

**Indexes**:
- `idx_activity_feed_occurred_at` (occurred_at DESC) - Recent activity queries (most recent first)
- `idx_activity_feed_aggregate_id` (aggregate_id) - Filter by entity
- `idx_activity_feed_aggregate_date` (aggregate_id, occurred_at DESC) - Entity activity history
- `idx_activity_feed_event_type` (event_type) - Filter by event type
- `idx_activity_feed_user_date` (user_id, occurred_at DESC) - User activity history
- `idx_activity_feed_event_type_date` (event_type, occurred_at DESC) - Event type filtering with date

**Relationships**:
- Many-to-One with `users` (user_id FK, nullable, SET NULL on delete)

**Event Types** (examples):
- `InvoiceCreated`, `InvoiceSent`, `InvoicePaid`, `InvoiceCancelled`
- `PaymentRecorded`, `RefundIssued`
- `CustomerCreated`, `CustomerUpdated`, `CustomerDeactivated`
- `RecurringInvoiceGenerated`, `LateFeeApplied`

**Usage**:
- Dashboard "Recent Activity" feed queries this table
- Audit trail for compliance and debugging
- User activity tracking for security

---

### 9. password_reset_tokens

**Purpose**: Stores password reset tokens for secure password reset flow.

**Key Fields**:
- `id` (UUID, PK): Unique token record identifier
- `user_id` (UUID, FK): Reference to users table
- `token` (VARCHAR(255), UNIQUE): Unique reset token (random UUID or hash)
- `expires_at` (TIMESTAMP): Token expiration time (typically 1 hour)
- `used` (BOOLEAN): Whether token has been used (defaults to FALSE)
- `created_at` (TIMESTAMP): When token was created

**Constraints**:
- `token` UNIQUE (enforced at database level)
- Foreign key to users (CASCADE DELETE - tokens deleted when user deleted)

**Indexes**:
- `idx_password_reset_tokens_token` (token) - Fast token lookups (authentication)
- `idx_password_reset_tokens_user_id` (user_id) - User-specific queries
- `idx_password_reset_tokens_validation` (token, used, expires_at) - Token validation query

**Relationships**:
- Many-to-One with `users` (user_id FK, CASCADE DELETE)

**Password Reset Flow**:
1. User requests password reset → Token generated, expires_at = CURRENT_TIMESTAMP + 1 hour
2. User clicks email link with token → System validates token (not used, not expired)
3. User sets new password → Token marked as `used = TRUE`
4. Token cannot be reused (enforced by application logic)

---

## Foreign Key Relationships

### Relationship Diagram

```
customers (1) ────< (N) invoices (1) ────< (N) line_items
    │                      │
    │                      └────< (N) payments
    │
    └────< (N) recurring_invoice_templates (1) ────< (N) template_line_items

users (N) ──> (1) customers (optional FK for customer role)
    │
    ├───< (N) payments (created_by_user_id)
    ├───< (N) recurring_invoice_templates (created_by_user_id)
    ├───< (N) activity_feed (user_id, nullable)
    └───< (N) password_reset_tokens (user_id)
```

### Foreign Key Details

| Parent Table | Child Table | Foreign Key Column | Delete Action | Notes |
|-------------|-------------|-------------------|---------------|-------|
| customers | invoices | customer_id | RESTRICT | Cannot delete customer with invoices |
| customers | payments | customer_id | RESTRICT | Cannot delete customer with payments |
| customers | recurring_invoice_templates | customer_id | RESTRICT | Cannot delete customer with templates |
| customers | users | customer_id | SET NULL | Customer portal users, nullable |
| invoices | line_items | invoice_id | CASCADE | Deleting invoice deletes line items |
| invoices | payments | invoice_id | RESTRICT | Cannot delete invoice with payments |
| users | payments | created_by_user_id | SET NULL | Payment creator, nullable |
| users | recurring_invoice_templates | created_by_user_id | RESTRICT | Cannot delete user who created template |
| users | activity_feed | user_id | SET NULL | Activity user, nullable for system events |
| users | password_reset_tokens | user_id | CASCADE | Tokens deleted when user deleted |
| recurring_invoice_templates | template_line_items | template_id | CASCADE | Deleting template deletes line items |

---

## Indexes and Performance

### Index Strategy

**Primary Indexes** (on all tables):
- Primary key indexes (UUID) - Automatic in PostgreSQL

**Foreign Key Indexes** (performance critical):
- All foreign key columns indexed for join performance
- Examples: `customer_id`, `invoice_id`, `user_id`, `template_id`

**Unique Indexes**:
- `customers.email` - Fast email lookups, unique constraint
- `invoices.invoice_number` - Fast invoice number lookups, unique constraint
- `users.email` - Fast email lookups, unique constraint
- `password_reset_tokens.token` - Fast token validation, unique constraint

**Filtering Indexes**:
- `status` columns on all tables - Common filtering pattern
- `customer_type`, `role`, `payment_method` - Enum filtering

**Date Range Indexes**:
- `due_date`, `issue_date`, `payment_date`, `next_invoice_date`, `occurred_at` - Date range queries

**Composite Indexes** (common query patterns):
- `(customer_id, status)` - Customer invoices by status
- `(customer_id, payment_date DESC)` - Customer payment history
- `(status, next_invoice_date)` - Scheduled job queries
- `(aggregate_id, occurred_at DESC)` - Entity activity history

**Partial Indexes** (optimized for specific queries):
- `idx_invoices_overdue` - Only indexes SENT/OVERDUE invoices
- `idx_templates_scheduled_job` - Only indexes ACTIVE templates
- `idx_users_customer_portal` - Only indexes CUSTOMER role users
- `idx_users_pending_approval` - Only indexes PENDING users

### Performance Targets

- **API Response Time**: <200ms for CRUD operations (p95)
- **Database Query Time**: <500ms for complex queries
- **Index Coverage**: All foreign keys and frequently queried fields indexed

---

## Constraints

### CHECK Constraints

| Table | Column | Constraint | Purpose |
|-------|--------|------------|---------|
| customers | credit_balance | `>= 0` | Credit cannot be negative |
| invoices | subtotal, tax_amount, discount_amount, total_amount, amount_paid, balance_due | `>= 0` | Monetary values cannot be negative |
| invoices | version | `>= 1` | Optimistic locking version |
| line_items | quantity | `>= 1` | Minimum 1 item required |
| line_items | unit_price | `>= 0` | Price cannot be negative |
| line_items | discount_value | `>= 0` | Discount cannot be negative |
| line_items | tax_rate | `>= 0 AND <= 100` | Tax rate percentage range |
| payments | amount | `> 0` | Payment must be positive (strict) |
| users | failed_login_count | `>= 0` | Login attempts cannot be negative |
| template_line_items | quantity | `>= 1` | Minimum 1 item required |
| template_line_items | unit_price | `>= 0` | Price cannot be negative |
| template_line_items | discount_value | `>= 0` | Discount cannot be negative |
| template_line_items | tax_rate | `>= 0 AND <= 100` | Tax rate percentage range |
| recurring_invoice_templates | end_date | `>= start_date` (if not NULL) | End date must be after start date |
| recurring_invoice_templates | next_invoice_date | `>= start_date` | Next date must be after start date |

### UNIQUE Constraints

| Table | Column(s) | Purpose |
|-------|-----------|---------|
| customers | email | Email addresses must be unique |
| invoices | invoice_number | Invoice numbers must be unique |
| users | email | User emails must be unique |
| password_reset_tokens | token | Reset tokens must be unique |

### NOT NULL Constraints

All primary keys, foreign keys, and critical business fields are NOT NULL:
- All `id` columns (PKs)
- All foreign key columns (except `users.customer_id` and `activity_feed.user_id` which are nullable)
- All `status` columns (default values provided)
- All `created_at` columns (defaults to CURRENT_TIMESTAMP)
- Monetary fields (defaults to 0)
- Email, password_hash, full_name, role (users table)

---

## Data Types

### UUID
- Used for all primary keys and foreign keys
- Generated using `gen_random_uuid()` function
- Provides globally unique identifiers

### DECIMAL(19,2)
- Used for all monetary values (amounts, prices, balances)
- Precision: 19 digits total, 2 decimal places
- Supports values up to 999,999,999,999,999,999.99
- Ensures precision for financial calculations

### ENUM Types
- PostgreSQL ENUM types for type safety
- Prevents invalid values at database level
- Examples: `customer_type_enum`, `invoice_status_enum`, `user_role_enum`

### TIMESTAMP
- Used for all date/time fields
- Defaults to `CURRENT_TIMESTAMP` for `created_at` columns
- `updated_at` columns updated via trigger

### VARCHAR Lengths
- Email: 255 characters (standard email length)
- Names: 255 characters (company_name, contact_name, full_name)
- Descriptions: 500 characters (line item descriptions)
- Notes: TEXT (unlimited length)
- References: 100 characters (payment_reference)

---

## Triggers

### Automatic Timestamp Updates

**Function**: `update_updated_at_column()`
- Updates `updated_at` column to `CURRENT_TIMESTAMP` on UPDATE
- Applied to: `customers`, `invoices`, `users`, `recurring_invoice_templates`

**Usage**:
```sql
CREATE TRIGGER update_customers_updated_at
    BEFORE UPDATE ON customers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

---

## Database Optimization Strategies

### 1. Indexing Strategy
- **Foreign Keys**: All foreign key columns indexed (required for join performance)
- **Frequently Queried Fields**: Email, status, dates, invoice numbers
- **Composite Indexes**: Common query patterns (customer_id + status, etc.)
- **Partial Indexes**: Optimized for specific WHERE clauses

### 2. Query Optimization
- Use EXPLAIN ANALYZE to identify slow queries
- Leverage composite indexes for multi-column WHERE clauses
- Use partial indexes for filtered queries (e.g., ACTIVE templates only)

### 3. Connection Pooling
- Use HikariCP connection pool (max 10 connections)
- Configure pool size based on expected load

### 4. Pagination
- All list endpoints use pagination (20-50 items per page)
- Use LIMIT/OFFSET or cursor-based pagination for large datasets

### 5. Monetary Precision
- Use DECIMAL(19,2) for all monetary values
- Round calculations to 2 decimal places (Banker's rounding)
- Store in Money value object (BigDecimal) in application layer

---

## Migration Strategy

See `/backend/docs/migrations.md` for detailed migration strategy documentation.

---

## References

- **PRD 2 Section 5**: Database Schema Specification
- **PRD 1 Section 4**: Business Rules and Entity Attributes
- **ORCHESTRATOR_OUTPUT.md**: Technical Decisions and Scope

---

**End of Database Schema Documentation**

