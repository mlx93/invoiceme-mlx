# Entity Relationship Diagram (ERD)

**Last Updated**: 2025-01-27  
**Database**: PostgreSQL 15.x

---

## Visual ERD (Text-Based)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           InvoiceMe Database Schema                           │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────┐
│     customers       │
├─────────────────────┤
│ id (PK, UUID)       │
│ company_name        │
│ contact_name        │
│ email (UNIQUE)      │◄──────────┐
│ phone               │            │
│ street              │            │
│ city                │            │
│ state               │            │
│ zip_code            │            │
│ country             │            │
│ customer_type       │            │
│ credit_balance      │            │
│ status              │            │
│ created_at          │            │
│ updated_at          │            │
└─────────────────────┘            │
         │                          │
         │ 1:N                      │
         │                          │
         ▼                          │
┌─────────────────────┐            │
│     invoices        │            │
├─────────────────────┤            │
│ id (PK, UUID)       │            │
│ invoice_number      │            │
│ customer_id (FK)    │────────────┘
│ issue_date          │
│ due_date            │
│ status              │
│ payment_terms       │
│ subtotal            │
│ tax_amount          │
│ discount_amount     │
│ total_amount        │
│ amount_paid         │
│ balance_due         │
│ notes               │
│ sent_date           │
│ paid_date           │
│ version             │
│ created_at          │
│ updated_at          │
└─────────────────────┘
         │
         │ 1:N
         │
         ├──────────────────┐
         │                  │
         ▼                  ▼
┌─────────────────────┐  ┌─────────────────────┐
│    line_items      │  │      payments       │
├─────────────────────┤  ├─────────────────────┤
│ id (PK, UUID)       │  │ id (PK, UUID)       │
│ invoice_id (FK)     │  │ invoice_id (FK)     │
│ description         │  │ customer_id (FK)    │◄──┐
│ quantity            │  │ amount              │   │
│ unit_price          │  │ payment_method      │   │
│ discount_type       │  │ payment_date        │   │
│ discount_value      │  │ payment_reference   │   │
│ tax_rate            │  │ status              │   │
│ sort_order          │  │ created_by_user_id  │───┼──┐
│ created_at          │  │ notes               │   │  │
└─────────────────────┘  │ created_at          │   │  │
                         └─────────────────────┘   │  │
                                                    │  │
┌─────────────────────┐                            │  │
│ recurring_invoice_  │                            │  │
│    templates        │                            │  │
├─────────────────────┤                            │  │
│ id (PK, UUID)       │                            │  │
│ customer_id (FK)    │────────────────────────────┘  │
│ template_name       │                               │
│ frequency           │                               │
│ start_date          │                               │
│ end_date            │                               │
│ next_invoice_date   │                               │
│ status              │                               │
│ payment_terms      │                               │
│ auto_send           │                               │
│ created_by_user_id  │──────────────────────────────┘
│ created_at          │
│ updated_at          │
└─────────────────────┘
         │
         │ 1:N
         │
         ▼
┌─────────────────────┐
│ template_line_items │
├─────────────────────┤
│ id (PK, UUID)       │
│ template_id (FK)    │
│ description         │
│ quantity            │
│ unit_price          │
│ discount_type       │
│ discount_value      │
│ tax_rate            │
│ sort_order          │
└─────────────────────┘

┌─────────────────────┐
│       users         │
├─────────────────────┤
│ id (PK, UUID)       │
│ email (UNIQUE)      │
│ password_hash       │
│ full_name           │
│ role                │
│ customer_id (FK)    │◄──┐
│ status              │   │
│ failed_login_count  │   │
│ locked_until        │   │
│ created_at          │   │
│ updated_at          │   │
└─────────────────────┘   │
         │                 │
         │ 1:N             │
         │                 │
         ├─────────────────┼──────────────────┐
         │                 │                  │
         ▼                 ▼                  ▼
┌─────────────────────┐  ┌─────────────────────┐  ┌─────────────────────┐
│   activity_feed     │  │ password_reset_     │  │      payments       │
├─────────────────────┤  │     tokens          │  │ (created_by_user)  │
│ id (PK, UUID)       │  ├─────────────────────┤  │ (already shown)    │
│ aggregate_id        │  │ id (PK, UUID)       │  └─────────────────────┘
│ event_type          │  │ user_id (FK)        │
│ description         │  │ token (UNIQUE)      │
│ occurred_at         │  │ expires_at           │
│ user_id (FK)        │  │ used                │
└─────────────────────┘  │ created_at          │
                         └─────────────────────┘
```

---

## Relationship Summary

### One-to-Many Relationships

| Parent Table | Child Table | Foreign Key | Delete Action |
|--------------|-------------|-------------|---------------|
| customers | invoices | customer_id | RESTRICT |
| customers | payments | customer_id | RESTRICT |
| customers | recurring_invoice_templates | customer_id | RESTRICT |
| customers | users | customer_id | SET NULL |
| invoices | line_items | invoice_id | CASCADE |
| invoices | payments | invoice_id | RESTRICT |
| users | payments | created_by_user_id | SET NULL |
| users | recurring_invoice_templates | created_by_user_id | RESTRICT |
| users | activity_feed | user_id | SET NULL |
| users | password_reset_tokens | user_id | CASCADE |
| recurring_invoice_templates | template_line_items | template_id | CASCADE |

### Key Relationships

1. **customers → invoices → line_items**
   - Customer has many invoices
   - Invoice has many line items
   - Deleting invoice cascades to line items

2. **customers → invoices → payments**
   - Customer has many invoices
   - Invoice has many payments
   - Payments also reference customer (denormalized for performance)

3. **customers → recurring_invoice_templates → template_line_items**
   - Customer has many recurring invoice templates
   - Template has many template line items
   - Deleting template cascades to template line items

4. **users → customers** (optional, for customer portal)
   - User can be linked to customer (nullable FK)
   - Used for customer portal authentication
   - SET NULL on customer delete (preserves user account)

5. **users → activity_feed**
   - User can have many activity feed entries
   - user_id is nullable (system events don't have user)
   - SET NULL on user delete (preserves audit trail)

---

## Cardinality Notation

- **1:N** = One-to-Many (one parent, many children)
- **N:1** = Many-to-One (many children, one parent)
- **PK** = Primary Key
- **FK** = Foreign Key
- **CASCADE** = Cascade delete (deleting parent deletes children)
- **RESTRICT** = Restrict delete (cannot delete parent if children exist)
- **SET NULL** = Set foreign key to NULL on parent delete

---

## Visual ERD Generation

This text-based ERD can be converted to a visual PNG diagram using:

1. **dbdiagram.io**: Import SQL schema, generate ERD
2. **pgAdmin**: Use ERD tool to visualize schema
3. **DBeaver**: Generate ERD from database connection
4. **PlantUML**: Convert text diagram to PNG
5. **Lucidchart/Draw.io**: Manual diagram creation

**Recommended Tool**: dbdiagram.io
- Import SQL migrations
- Auto-generate ERD
- Export as PNG
- Supports PostgreSQL syntax

---

## ERD Generation Instructions

### Using dbdiagram.io

1. Go to https://dbdiagram.io/
2. Create new diagram
3. Import SQL migrations (V1-V9) or manually create tables
4. Auto-generate relationships
5. Export as PNG: `File → Export → PNG`

### Using pgAdmin

1. Connect to PostgreSQL database
2. Right-click database → `ERD Tool`
3. Add tables (customers, invoices, line_items, payments, users, etc.)
4. Auto-generate relationships
5. Export as PNG: `File → Export → PNG`

### Using DBeaver

1. Connect to PostgreSQL database
2. Right-click database → `View Diagram`
3. Select all tables
4. Generate ERD
5. Export as PNG: `File → Export → Image`

---

## ERD Notes

- **Denormalization**: `payments.customer_id` is denormalized (also references `invoices.customer_id`) for performance
- **Nullable Foreign Keys**: `users.customer_id` and `activity_feed.user_id` are nullable
- **Cascade Deletes**: `line_items` and `template_line_items` cascade delete with parent
- **Version Column**: `invoices.version` is for optimistic locking (not shown in ERD)

---

**End of ERD Documentation**

