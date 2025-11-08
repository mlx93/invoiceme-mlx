# Data/DB Agent Prompt

**[AGENT]: Data/DB**

**GOAL**: Design PostgreSQL database schema with Flyway migrations, ERD diagram, and database optimization strategies. **FULL PRD SCOPE**: Support all operations (Customers, Invoices, Payments, Recurring Invoices, Refunds, Users, Activity Feed) with proper indexes and constraints.

**INPUTS**:
- InvoiceMe.md (assessment source of truth)
- PRD_2_Tech_Spec.md (database schema Section 5, Flyway migrations)
- Backend domain aggregates (`/backend/docs/domain-aggregates.md`) — entity relationships (may not exist yet, but will be created by Backend Agent)
- ORCHESTRATOR_OUTPUT.md (scope decisions, decision log)
- REMAINING_QUESTIONS.md (all technical decisions resolved)
- PRD_1_Business_Reqs.md (business rules, entity attributes)

**DELIVERABLES**:
- `/backend/src/main/resources/db/migration/` — Flyway migration files:
  - `V1__create_customers_table.sql`
  - `V2__create_invoices_table.sql`
  - `V3__create_line_items_table.sql`
  - `V4__create_payments_table.sql`
  - `V5__create_users_table.sql`
  - `V6__create_recurring_invoice_templates_table.sql`
  - `V7__create_template_line_items_table.sql`
  - `V8__create_activity_feed_table.sql`
  - `V9__create_password_reset_tokens_table.sql`
  - `V10__create_indexes.sql`
- `/backend/docs/erd.png` — Entity Relationship Diagram (customers → invoices → line_items, customers → invoices → payments, recurring templates, etc.)
- `/backend/docs/database-schema.md` — Schema documentation:
  - Table descriptions
  - Foreign key relationships
  - Indexes and their purposes
  - Constraints (CHECK, UNIQUE, NOT NULL)
- `/backend/docs/migrations.md` — Migration strategy documentation

**DONE CRITERIA**:
1. ✅ Database schema supports all PRD operations (core + extended):
   - Customers table (id UUID PK, company_name, email UNIQUE, credit_balance DECIMAL(19,2), status ENUM, customer_type ENUM, billing_address fields, timestamps)
   - Invoices table (id UUID PK, invoice_number UNIQUE VARCHAR(20), customer_id FK, issue_date, due_date, status ENUM, payment_terms ENUM, subtotal, tax_amount, discount_amount, total_amount, amount_paid, balance_due, notes, sent_date, paid_date, version INT, timestamps)
   - Line items table (id UUID PK, invoice_id FK CASCADE DELETE, description, quantity INT CHECK >= 1, unit_price DECIMAL(19,2), discount_type ENUM, discount_value DECIMAL(10,2), tax_rate DECIMAL(5,2), sort_order INT, timestamps)
   - Payments table (id UUID PK, invoice_id FK, customer_id FK, amount DECIMAL(19,2) CHECK > 0, payment_method ENUM, payment_date, payment_reference VARCHAR(100), status ENUM, created_by_user_id FK, notes TEXT, timestamps)
   - Users table (id UUID PK, email UNIQUE, password_hash, full_name, role ENUM, customer_id FK nullable, status ENUM, failed_login_count INT DEFAULT 0, locked_until TIMESTAMP, timestamps)
   - Recurring invoice templates table (id UUID PK, customer_id FK, template_name, frequency ENUM, start_date, end_date nullable, next_invoice_date, status ENUM, payment_terms ENUM, auto_send BOOLEAN DEFAULT FALSE, created_by_user_id FK, timestamps)
   - Template line items table (id UUID PK, template_id FK CASCADE DELETE, description, quantity INT, unit_price DECIMAL(19,2), discount_type ENUM, discount_value DECIMAL(10,2), tax_rate DECIMAL(5,2), sort_order INT)
   - Activity feed table (id UUID PK, aggregate_id UUID, event_type VARCHAR(100), description TEXT, occurred_at TIMESTAMP, user_id FK nullable)
   - Password reset tokens table (id UUID PK, user_id FK, token VARCHAR(255) UNIQUE, expires_at TIMESTAMP, used BOOLEAN DEFAULT FALSE, created_at TIMESTAMP)
2. ✅ Proper constraints:
   - Foreign keys with CASCADE DELETE where appropriate (line_items → invoices, template_line_items → recurring_invoice_templates)
   - UNIQUE constraints (email in users, email in customers, invoice_number in invoices, token in password_reset_tokens)
   - CHECK constraints (quantity >= 1 in line_items, amount > 0 in payments)
   - NOT NULL constraints on required fields (all PKs, FKs, critical business fields)
3. ✅ Indexes for performance:
   - Foreign keys indexed (customer_id, invoice_id, user_id, template_id)
   - Frequently queried fields indexed (email, status, due_date, issue_date, invoice_number, next_invoice_date)
   - Composite indexes for common queries (customer_id + status, invoice_id + status)
   - Index on activity_feed.occurred_at DESC for recent activity queries
4. ✅ Flyway migrations:
   - Sequential versions (V1, V2, V3, ...)
   - Immutable (no changes after deployment)
   - Idempotent (can run multiple times safely)
   - Naming convention: `V{version}__{description}.sql`
5. ✅ ERD diagram:
   - Visual representation of relationships
   - Cardinality shown (1:N, N:1)
   - Key fields labeled
   - All tables included (core + extended features)

**REPORT BACK WITH**:
- **Summary** (≤12 bullets):
  - Tables created (list all 9 tables)
  - Foreign keys defined (list relationships)
  - Indexes created (list indexes and purposes)
  - Constraints enforced (list constraints)
  - Migration files (count: 10 files)
  - ERD diagram created
  - Schema tested (migrations run successfully)
  - Open issues/blocked dependencies (if any)
- **Artifacts paths**:
  - Migrations: `/backend/src/main/resources/db/migration/`
  - ERD: `/backend/docs/erd.png`
  - Schema doc: `/backend/docs/database-schema.md`
  - Migrations doc: `/backend/docs/migrations.md`
- **Evidence**:
  - ERD diagram (PNG image)
  - Migration file examples (snippets showing key tables)
  - Database schema validation (migrations run successfully, tables created)

**DO NOT**:
- Use database-specific features that aren't portable (use standard SQL)
- Skip indexes on foreign keys (performance critical)
- Skip tables for extended features (all PRD tables required: recurring_invoice_templates, template_line_items, activity_feed, password_reset_tokens)
- Use VARCHAR without length limits (specify lengths: VARCHAR(255), VARCHAR(500), etc.)
- Forget to include version column in invoices table (for optimistic locking)

**IMPORTANT NOTES**:
- Database connection: Use `DATABASE_URL` from `.env` file (local PostgreSQL via Docker) or Supabase connection string
- All monetary values: Use `DECIMAL(19,2)` for precision (Money value object will use BigDecimal)
- All UUIDs: Use PostgreSQL `UUID` type (not VARCHAR)
- All timestamps: Use `TIMESTAMP` type (not TIMESTAMP WITH TIME ZONE for simplicity, but store in Central Time per decisions)
- ENUM types: Use PostgreSQL ENUM types or VARCHAR with CHECK constraints (prefer ENUM for type safety)
- Credit balance: Stored in customers table but calculated from payments (can be updated via triggers or application logic)

**REFERENCE**:
- PRD_2_Tech_Spec.md Section 5.2 for detailed table specifications
- PRD_1_Business_Reqs.md Section 4 for entity attributes and business rules
- ORCHESTRATOR_OUTPUT.md Section 7 (Decision Log) for technical decisions (Central Time, etc.)

---

**Status**: Ready to execute  
**Dependencies**: None (foundation layer - starts first)  
**Next**: After completion, Backend Agent will use schema for domain model design

