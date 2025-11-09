# PostgreSQL Enum Conversion Fix - Complete Solution

## Problem
All dashboard endpoints and protected endpoints were returning 500 errors after login due to PostgreSQL enum type mismatches. Hibernate was treating PostgreSQL native enum types (e.g., `invoice_status_enum`, `customer_status_enum`) as `character varying` (String), causing SQL errors like:

```
ERROR: operator does not exist: invoice_status_enum = character varying
Hint: No operator matches the given name and argument types. You might need to add explicit type casts.
```

## Root Cause
JPA's `@Enumerated(EnumType.STRING)` annotation works for VARCHAR columns but **not** for PostgreSQL native enum types. PostgreSQL requires explicit type casting (`::`text) when comparing enum columns with String parameters.

## Solution Applied

### 1. Created Custom JPA Enum Converters
Created converters in `/backend/src/main/java/com/invoiceme/infrastructure/persistence/`:

- ✅ `UserRoleConverter.java`
- ✅ `UserStatusConverter.java`
- ✅ `InvoiceStatusConverter.java`
- ✅ `PaymentTermsConverter.java`
- ✅ `CustomerTypeConverter.java`
- ✅ `CustomerStatusConverter.java`
- ✅ `PaymentMethodConverter.java`
- ✅ `PaymentStatusConverter.java`
- ✅ `DiscountTypeConverter.java`
- ✅ `FrequencyConverter.java`
- ✅ `TemplateStatusConverter.java`

Each converter implements `AttributeConverter<EnumType, String>` to handle conversion between Java enums and database String representation.

### 2. Updated All Entities with `@Convert` + `@ColumnTransformer`

Replaced `@Enumerated(EnumType.STRING)` with `@Convert` and `@ColumnTransformer` in:

#### ✅ User Entity
```java
@Convert(converter = UserRoleConverter.class)
@Column(name = "role", nullable = false, columnDefinition = "user_role_enum")
@org.hibernate.annotations.ColumnTransformer(
    read = "role::text",
    write = "?::user_role_enum"
)
private UserRole role;
```

#### ✅ Invoice Entity
- `status` → `InvoiceStatusConverter` + `invoice_status_enum`
- `paymentTerms` → `PaymentTermsConverter` + `payment_terms_enum`

#### ✅ Customer Entity
- `customerType` → `CustomerTypeConverter` + `customer_type_enum`
- `status` → `CustomerStatusConverter` + `customer_status_enum`

#### ✅ Payment Entity
- `paymentMethod` → `PaymentMethodConverter` + `payment_method_enum`
- `status` → `PaymentStatusConverter` + `payment_status_enum`

#### ✅ LineItem Entity
- `discountType` → `DiscountTypeConverter` + `discount_type_enum`

#### ✅ RecurringInvoiceTemplate Entity
- `frequency` → `FrequencyConverter` + `frequency_enum`
- `status` → `TemplateStatusConverter` + `template_status_enum`
- `paymentTerms` → `PaymentTermsConverter` + `payment_terms_enum`

#### ✅ TemplateLineItem Entity
- `discountType` → `DiscountTypeConverter` + `discount_type_enum`

### 3. Fixed Repository Query Parameter Mismatch

**InvoiceRepository.java** - Fixed parameter name inconsistency:
```java
// BEFORE (BROKEN):
@Query("SELECT i FROM Invoice i WHERE (:status IS NULL OR i.status IN :statusList) ...")
Page<Invoice> findByFilters(@Param("status") List<InvoiceStatus> statusList, ...)

// AFTER (FIXED):
@Query("SELECT i FROM Invoice i WHERE (:statusList IS NULL OR i.status IN :statusList) ...")
Page<Invoice> findByFilters(@Param("statusList") List<InvoiceStatus> statusList, ...)
```

## Why This Fix Works

### @ColumnTransformer Annotations
```java
@org.hibernate.annotations.ColumnTransformer(
    read = "role::text",     // Cast PostgreSQL enum → text when reading
    write = "?::user_role_enum"  // Cast text → PostgreSQL enum when writing
)
```

- **READ**: When Hibernate queries, it casts `role::text` so comparisons work
- **WRITE**: When Hibernate inserts/updates, it casts `?::user_role_enum` to match column type

### Custom Converters
```java
@Converter(autoApply = false)
public class UserRoleConverter implements AttributeConverter<UserRole, String> {
    @Override
    public String convertToDatabaseColumn(UserRole attribute) {
        return attribute != null ? attribute.name() : null;
    }
    
    @Override
    public UserRole convertToEntityAttribute(String dbData) {
        return dbData != null ? UserRole.valueOf(dbData) : null;
    }
}
```

Handles the Java enum ↔ String conversion for JPA while `@ColumnTransformer` handles the PostgreSQL enum casting.

## Verification

After these changes, all SQL queries now include proper casting:

```sql
-- BEFORE (BROKEN):
WHERE i1_0.status = 'PAID'  -- Type mismatch error

-- AFTER (FIXED):
WHERE i1_0.status::text = 'PAID'  -- Works correctly
```

## Files Modified

### New Files (11 Converters)
- `backend/src/main/java/com/invoiceme/infrastructure/persistence/*Converter.java` (11 files)

### Modified Entities (7 Entities)
- `User.java` - role, status
- `Invoice.java` - status, paymentTerms
- `Customer.java` - customerType, status
- `Payment.java` - paymentMethod, status
- `LineItem.java` - discountType
- `RecurringInvoiceTemplate.java` - frequency, status, paymentTerms
- `TemplateLineItem.java` - discountType

### Modified Repositories (1 Repository)
- `InvoiceRepository.java` - Fixed `:status` → `:statusList` parameter name

## Result
✅ Login works
✅ Dashboard metrics endpoint works
✅ Revenue trend endpoint works
✅ Invoice status endpoint works
✅ Aging report endpoint works
✅ All protected endpoints work
✅ All PostgreSQL enum columns properly cast

## Key Takeaway
**ALWAYS use `@Convert` + `@ColumnTransformer` for PostgreSQL native enum types**, not `@Enumerated(EnumType.STRING)`. This ensures proper type casting in all SQL queries and prevents runtime errors.

