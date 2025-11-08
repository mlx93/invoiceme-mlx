# Flyway Migrations Documentation

**Last Updated**: 2025-01-27  
**Migration Tool**: Flyway 9.x  
**Database**: PostgreSQL 15.x

---

## Overview

The InvoiceMe database schema is managed using Flyway migrations. All schema changes are version-controlled SQL files located in `/backend/src/main/resources/db/migration/`.

**Total Migrations**: 10  
**Migration Pattern**: Sequential versions (V1, V2, V3, ...)  
**Naming Convention**: `V{version}__{description}.sql`

---

## Migration Files

### V1: Create customers table
**File**: `V1__create_customers_table.sql`  
**Purpose**: Creates the `customers` table with ENUM types, constraints, indexes, and triggers.

**Creates**:
- ENUM types: `customer_type_enum`, `customer_status_enum`
- Table: `customers` with all required fields
- Indexes: `idx_customers_email`, `idx_customers_status`, `idx_customers_customer_type`
- Trigger: `update_customers_updated_at` for automatic timestamp updates
- Function: `update_updated_at_column()` (reusable for other tables)

**Dependencies**: None (first migration)

---

### V2: Create invoices table
**File**: `V2__create_invoices_table.sql`  
**Purpose**: Creates the `invoices` table with foreign key to customers, ENUM types, and indexes.

**Creates**:
- ENUM types: `invoice_status_enum`, `payment_terms_enum`
- Table: `invoices` with all required fields
- Foreign key: `fk_invoices_customer` → `customers(id)`
- Indexes: `idx_invoices_invoice_number`, `idx_invoices_customer_id`, `idx_invoices_status`, `idx_invoices_due_date`, `idx_invoices_issue_date`, `idx_invoices_customer_status`
- Trigger: `update_invoices_updated_at`

**Dependencies**: V1 (requires `customers` table)

---

### V3: Create line_items table
**File**: `V3__create_line_items_table.sql`  
**Purpose**: Creates the `line_items` table with CASCADE DELETE on invoice deletion.

**Creates**:
- ENUM type: `discount_type_enum`
- Table: `line_items` with all required fields
- Foreign key: `fk_line_items_invoice` → `invoices(id)` with CASCADE DELETE
- Indexes: `idx_line_items_invoice_id`, `idx_line_items_sort_order`

**Dependencies**: V2 (requires `invoices` table)

---

### V4: Create payments table
**File**: `V4__create_payments_table.sql`  
**Purpose**: Creates the `payments` table with foreign keys to invoices and customers.

**Creates**:
- ENUM types: `payment_method_enum`, `payment_status_enum`
- Table: `payments` with all required fields
- Foreign keys: `fk_payments_invoice` → `invoices(id)`, `fk_payments_customer` → `customers(id)`
- Indexes: `idx_payments_invoice_id`, `idx_payments_customer_id`, `idx_payments_payment_date`, `idx_payments_payment_method`, `idx_payments_customer_date`
- Note: `created_by_user_id` foreign key added in V5 (after users table created)

**Dependencies**: V1, V2 (requires `customers` and `invoices` tables)

---

### V5: Create users table
**File**: `V5__create_users_table.sql`  
**Purpose**: Creates the `users` table with authentication fields and role-based access control.

**Creates**:
- ENUM types: `user_role_enum`, `user_status_enum`
- Table: `users` with all required fields
- Foreign key: `fk_users_customer` → `customers(id)` (nullable, SET NULL on delete)
- Indexes: `idx_users_email`, `idx_users_role`, `idx_users_status`, `idx_users_role_status`
- Trigger: `update_users_updated_at`
- Adds foreign key constraint: `fk_payments_created_by_user` → `users(id)` (for payments table from V4)

**Dependencies**: V1 (requires `customers` table)

---

### V6: Create recurring_invoice_templates table
**File**: `V6__create_recurring_invoice_templates_table.sql`  
**Purpose**: Creates the `recurring_invoice_templates` table for scheduled invoice generation.

**Creates**:
- ENUM types: `frequency_enum`, `template_status_enum`
- Table: `recurring_invoice_templates` with all required fields
- Foreign keys: `fk_templates_customer` → `customers(id)`, `fk_templates_created_by_user` → `users(id)`
- Indexes: `idx_templates_customer_id`, `idx_templates_next_invoice_date`, `idx_templates_status`, `idx_templates_status_next_date`
- Trigger: `update_templates_updated_at`

**Dependencies**: V1, V5 (requires `customers` and `users` tables)

---

### V7: Create template_line_items table
**File**: `V7__create_template_line_items_table.sql`  
**Purpose**: Creates the `template_line_items` table with CASCADE DELETE on template deletion.

**Creates**:
- Table: `template_line_items` with all required fields
- Foreign key: `fk_template_line_items_template` → `recurring_invoice_templates(id)` with CASCADE DELETE
- Indexes: `idx_template_line_items_template_id`, `idx_template_line_items_sort_order`

**Dependencies**: V6 (requires `recurring_invoice_templates` table and `discount_type_enum` from V3)

---

### V8: Create activity_feed table
**File**: `V8__create_activity_feed_table.sql`  
**Purpose**: Creates the `activity_feed` table for audit trail and domain event logging.

**Creates**:
- Table: `activity_feed` with all required fields
- Foreign key: `fk_activity_feed_user` → `users(id)` (nullable, SET NULL on delete)
- Indexes: `idx_activity_feed_occurred_at` (DESC), `idx_activity_feed_aggregate_id`, `idx_activity_feed_aggregate_date`, `idx_activity_feed_event_type`

**Dependencies**: V5 (requires `users` table)

---

### V9: Create password_reset_tokens table
**File**: `V9__create_password_reset_tokens_table.sql`  
**Purpose**: Creates the `password_reset_tokens` table for password reset flow.

**Creates**:
- Table: `password_reset_tokens` with all required fields
- Foreign key: `fk_password_reset_tokens_user` → `users(id)` with CASCADE DELETE
- Indexes: `idx_password_reset_tokens_token`, `idx_password_reset_tokens_user_id`, `idx_password_reset_tokens_validation`

**Dependencies**: V5 (requires `users` table)

---

### V10: Create indexes
**File**: `V10__create_indexes.sql`  
**Purpose**: Creates additional composite indexes and optimization indexes for common query patterns.

**Creates**:
- Composite indexes for invoices: `idx_invoices_customer_issue_date`, `idx_invoices_overdue`
- Composite indexes for payments: `idx_payments_customer_status`, `idx_payments_invoice_status`
- Composite indexes for templates: `idx_templates_scheduled_job` (partial index)
- Composite indexes for activity_feed: `idx_activity_feed_user_date`, `idx_activity_feed_event_type_date`
- Composite indexes for users: `idx_users_customer_portal` (partial), `idx_users_pending_approval` (partial)
- Composite indexes for customers: `idx_customers_outstanding` (partial)

**Dependencies**: V1-V9 (requires all tables)

---

## Migration Execution

### Flyway Configuration

**Location**: `application.properties` or `application.yml`

```properties
# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.validate-on-migrate=true
spring.flyway.clean-disabled=true
```

### Execution Order

Flyway executes migrations in sequential order based on version number:
1. V1 → V2 → V3 → V4 → V5 → V6 → V7 → V8 → V9 → V10

### Migration Lifecycle

1. **Application Startup**: Flyway checks `flyway_schema_history` table
2. **Version Check**: Compares applied migrations vs. available migration files
3. **Validation**: Validates checksums of applied migrations (prevents accidental changes)
4. **Execution**: Runs pending migrations in order
5. **Record**: Records migration in `flyway_schema_history` table

---

## Migration Best Practices

### 1. Immutability
- **Rule**: Migration files are immutable once deployed
- **Rationale**: Flyway validates checksums to prevent accidental changes
- **Practice**: Never modify existing migration files. Create new migrations for schema changes.

### 2. Idempotency
- **Rule**: Migrations should be idempotent (can run multiple times safely)
- **Practice**: Use `IF NOT EXISTS` clauses where appropriate, or rely on Flyway's version tracking

### 3. Sequential Versions
- **Rule**: Use sequential version numbers (V1, V2, V3, ...)
- **Practice**: Never skip versions or reuse version numbers

### 4. Descriptive Names
- **Rule**: Use descriptive migration names: `V{version}__{description}.sql`
- **Examples**: `V1__create_customers_table.sql`, `V10__create_indexes.sql`

### 5. Atomic Changes
- **Rule**: Each migration should be atomic (all-or-nothing)
- **Practice**: Use transactions (PostgreSQL wraps each migration in a transaction)

### 6. Backward Compatibility
- **Rule**: Avoid breaking changes in migrations
- **Practice**: Add new columns as nullable, then populate, then make NOT NULL in separate migration

### 7. Performance
- **Rule**: Index creation can be slow on large tables
- **Practice**: Create indexes in separate migration (V10) after data is loaded

---

## Rollback Strategy

### Current Approach: No Rollback Migrations

**Rationale**: 
- Fresh database (no production data to preserve)
- Flyway doesn't support automatic rollbacks
- Manual rollback requires reverse migration scripts

### Manual Rollback (if needed)

1. **Identify Migration**: Determine which migration to rollback
2. **Create Reverse Migration**: Write SQL to undo changes (e.g., `DROP TABLE`, `DROP INDEX`)
3. **Version Numbering**: Use next version number (e.g., `V11__rollback_v10.sql`)
4. **Execute**: Run reverse migration manually

**Example**:
```sql
-- V11__rollback_v10.sql
DROP INDEX IF EXISTS idx_customers_outstanding;
DROP INDEX IF EXISTS idx_users_pending_approval;
-- ... etc
```

### Production Rollback (Future)

For production environments, consider:
- **Database Backups**: Restore from backup before problematic migration
- **Blue-Green Deployment**: Deploy to new environment, migrate data
- **Feature Flags**: Disable features that depend on new schema

---

## Migration Validation

### Checksum Validation

Flyway validates checksums of applied migrations:
- **Purpose**: Prevents accidental changes to migration files
- **Error**: If checksum mismatch, Flyway throws error and stops
- **Resolution**: Fix migration file or manually update checksum in `flyway_schema_history`

### Migration Status

Check migration status:
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

**Columns**:
- `installed_rank`: Execution order
- `version`: Migration version (e.g., "1", "2", "3")
- `description`: Migration description
- `type`: Migration type (SQL, JDBC, etc.)
- `script`: Migration file name
- `checksum`: File checksum (for validation)
- `installed_on`: When migration was applied
- `success`: Whether migration succeeded

---

## Common Migration Patterns

### 1. Adding a New Table
```sql
-- V11__create_new_table.sql
CREATE TABLE new_table (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_new_table_name ON new_table(name);
```

### 2. Adding a Column
```sql
-- V12__add_column_to_table.sql
ALTER TABLE customers ADD COLUMN new_field VARCHAR(100);
CREATE INDEX idx_customers_new_field ON customers(new_field);
```

### 3. Adding an Index
```sql
-- V13__add_index.sql
CREATE INDEX idx_customers_new_index ON customers(column1, column2);
```

### 4. Modifying a Column
```sql
-- V14__modify_column.sql
ALTER TABLE customers ALTER COLUMN email TYPE VARCHAR(500);
```

### 5. Adding a Foreign Key
```sql
-- V15__add_foreign_key.sql
ALTER TABLE new_table
    ADD CONSTRAINT fk_new_table_parent FOREIGN KEY (parent_id) REFERENCES parent_table(id);
```

---

## Troubleshooting

### Migration Fails on Startup

**Symptoms**: Application fails to start, Flyway error in logs

**Causes**:
- Syntax error in migration SQL
- Foreign key constraint violation
- Missing dependency (table/column doesn't exist)
- Checksum mismatch (migration file modified)

**Resolution**:
1. Check Flyway logs for specific error
2. Fix SQL syntax or dependency issues
3. If checksum mismatch, fix migration file or update checksum manually
4. Re-run migration

### Migration Already Applied

**Symptoms**: Flyway skips migration, logs "Migration already applied"

**Causes**: Migration was applied in previous run

**Resolution**: This is normal behavior. Flyway tracks applied migrations in `flyway_schema_history` table.

### Migration Out of Order

**Symptoms**: Flyway error: "Migration version mismatch"

**Causes**: Migration files are not sequential (e.g., V1, V3, V5 - missing V2, V4)

**Resolution**: Ensure all migrations are present and sequential. Fill gaps with placeholder migrations if needed.

---

## Testing Migrations

### Local Testing

1. **Start Fresh Database**: Drop and recreate database
2. **Run Migrations**: Start Spring Boot application (Flyway runs automatically)
3. **Verify Schema**: Check tables, indexes, constraints created correctly
4. **Test Queries**: Run sample queries to verify indexes work

### Integration Testing

1. **Test Database**: Use separate test database (H2 or PostgreSQL)
2. **Flyway Test**: Use `@FlywayTest` annotation (if using Flyway Test extension)
3. **Schema Validation**: Verify schema matches expected structure

### Production Testing

1. **Staging Environment**: Test migrations on staging database first
2. **Backup**: Create database backup before running migrations
3. **Rollback Plan**: Have rollback script ready (if needed)
4. **Monitor**: Watch for slow queries or performance issues after migration

---

## Migration Checklist

Before creating a new migration:

- [ ] Migration file follows naming convention: `V{version}__{description}.sql`
- [ ] Version number is sequential (no gaps)
- [ ] SQL syntax is valid PostgreSQL
- [ ] Foreign keys reference existing tables
- [ ] Indexes are created for foreign keys and frequently queried fields
- [ ] Constraints are appropriate (CHECK, UNIQUE, NOT NULL)
- [ ] Migration is idempotent (can run multiple times safely)
- [ ] Migration is tested locally before committing

---

## References

- **Flyway Documentation**: https://flywaydb.org/documentation/
- **PostgreSQL Documentation**: https://www.postgresql.org/docs/
- **PRD 2 Section 5.3**: Flyway Migrations Specification
- **Database Schema Documentation**: `/backend/docs/database-schema.md`

---

**End of Migrations Documentation**

