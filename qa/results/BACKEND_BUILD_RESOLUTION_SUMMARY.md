# Backend Build Resolution Summary

## Overview
This document summarizes all errors encountered and resolutions applied to successfully build and run the InvoiceMe Spring Boot backend application.

---

## 1. Maven Build Configuration Issues

### Error: Missing Flyway Database Dependency Version
- **Error**: `'dependencies.dependency.version' for org.flywaydb:flyway-database-postgresql:jar is missing`
- **Resolution**: Removed `flyway-database-postgresql` dependency (implicitly included in `flyway-core` for Spring Boot 3.2.0)

### Error: Annotation Processor Order
- **Error**: MapStruct mappers failing to generate implementations
- **Resolution**: Reordered `annotationProcessorPaths` in `pom.xml` to ensure Lombok runs before MapStruct, added `lombok-mapstruct-binding`

---

## 2. Lombok Constructor Conflicts

### Error: Duplicate Constructor Definitions
- **Error**: `constructor GetMetricsQuery() is already defined` (and similar for GetPendingUsersQuery, GetInvoiceStatusQuery, GetAgingReportQuery)
- **Resolution**: Removed `@AllArgsConstructor` from query classes with no fields (kept only `@NoArgsConstructor`)

---

## 3. Missing Imports

### Errors:
- `cannot find symbol class UUID` in UpdateCustomerMapper, ActivityFeedListener
- `cannot find symbol variable BigDecimal` and `RoundingMode` in Invoice.java
- Missing `Email` import in various mappers

### Resolution: Added missing imports:
- `import java.util.UUID;`
- `import java.math.BigDecimal;`
- `import java.math.RoundingMode;`
- `import com.invoiceme.domain.common.Email;`

---

## 4. MapStruct Value Object Mapping

### Error: `Can't map property "Email email" to "String email"`
- **Resolution**: Added default mapping methods to mappers:
  ```java
  default String map(Email email) {
      return email == null ? null : email.getValue();
  }
  ```
- Applied to: GetCustomerMapper, ListCustomersMapper, CreateCustomerMapper, UpdateCustomerMapper, CreateInvoiceMapper, UpdateInvoiceMapper, MarkAsSentMapper, ListInvoicesMapper

### Error: `Can't map property "InvoiceNumber invoiceNumber" to "String invoiceNumber"`
- **Resolution**: Added similar default mapping methods for `InvoiceNumber` value object

---

## 5. Lombok Builder Inheritance

### Error: `cannot find symbol method id(java.util.UUID)` in CustomerController
- **Resolution**: Changed `@Builder` to `@SuperBuilder` in:
  - `CustomerDto` (base class)
  - `CustomerDetailResponse` (extends CustomerDto)

---

## 6. Domain Event Constructor Calls

### Error: `constructor InvoiceFullyPaidEvent cannot be applied to given types`
- **Resolution**: Updated event constructor calls in `Invoice.java` to match updated signatures, providing `null` for fields populated by handlers

### Error: `constructor LateFeeAppliedEvent cannot be applied to given types`
- **Resolution**: Updated constructor call with correct parameters

---

## 7. JWT API Compatibility

### Error: `cannot find symbol method parserBuilder()` in JwtTokenProvider
- **Resolution**: Updated to JJWT 0.12.x API:
  ```java
  Jwts.parser()
      .verifyWith(getSigningKey())
      .build()
      .parseSignedClaims(token)
      .getPayload();
  ```

---

## 8. Entity Encapsulation Issues

### Error: `Payment() has protected access` and private field access in IssueRefundHandler
- **Resolution**: Created static factory method `Payment.createRefund()` for proper domain encapsulation

### Error: Direct field access in User entity (RegisterHandler, ApproveUserHandler, RejectUserHandler)
- **Resolution**: 
  - Added static factory method `User.create()`
  - Added public methods `approve()` and `reject()` for status changes
  - Updated handlers to use these methods

---

## 9. Integration Test Errors

### Error: `method create in class Customer cannot be applied to given types`
- **Resolution**: Updated `Customer.create()` calls in:
  - `PartialPaymentTest.java`
  - `OverpaymentCreditTest.java`
  - `CustomerPaymentFlowTest.java`
- Changed to: `Customer.create(String companyName, Email email, CustomerType customerType)`

---

## 10. Database Connection Issues

### Error: `FATAL: role "postgres" does not exist`
- **Resolution**: 
  - Stopped conflicting local PostgreSQL service: `brew services stop postgresql@14`
  - Recreated Docker PostgreSQL container with clean volume
  - Verified Docker container is running correctly

---

## 11. Scheduled Job Configuration

### Error: `Cron expression must consist of 6 fields (found 5 in "0 0 1 * *")`
- **Resolution**: Updated cron expressions to 6 fields (added seconds):
  - `LateFeeScheduledJob`: `"0 0 1 * * ?"` (runs at 1 AM daily)
  - `RecurringInvoiceScheduledJob`: `"0 0 * * * ?"` (runs hourly)

---

## 12. YAML Configuration

### Error: `found duplicate key jdbc` in application.yml
- **Resolution**: Merged duplicate `jdbc` configuration under `hibernate.properties`

---

## 13. CORS Configuration

### Error: `Access to XMLHttpRequest blocked by CORS policy`
- **Resolution**: Added `CorsConfigurationSource` bean to `SecurityConfig.java`:
  - Allowed origin: `http://localhost:3000`
  - Allowed methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
  - Allowed headers: all
  - Credentials: enabled

---

## 14. Frontend-Backend Field Mismatch

### Error: `Validation failed for argument [0] ... Field error ... on field 'firstName': rejected value [null]`
- **Resolution**: 
  - Added `fullName` field to `RegisterRequest.java`
  - Updated `RegisterHandler.java` to parse `fullName` into `firstName` and `lastName`
  - Made `firstName`/`lastName` optional for backward compatibility

---

## 15. PostgreSQL Enum Handling

### Error: Login endpoint returns 500 Internal Server Error due to enum read compatibility
- **Resolution**: 
  - Created `UserRoleConverter` and `UserStatusConverter` implementing `AttributeConverter<UserRole/String, String>`
  - Applied converters using `@Convert(converter = UserRoleConverter.class)`
  - Added `@ColumnTransformer` for explicit PostgreSQL enum casting:
    ```java
    @org.hibernate.annotations.ColumnTransformer(
        read = "role::text",
        write = "?::user_role_enum"
    )
    ```

---

## Final Status

✅ **Backend Successfully Running**
- Port: 8080
- Health endpoint: `http://localhost:8080/actuator/health`
- Status: `{"status": "UP"}`
- Database: Connected and migrations applied (12 migrations)
- All compilation errors resolved
- Frontend-backend integration working (CORS configured)

---

## Key Files Modified

1. `backend/pom.xml` - Maven configuration
2. `backend/src/main/java/com/invoiceme/infrastructure/persistence/User.java` - Entity encapsulation
3. `backend/src/main/java/com/invoiceme/infrastructure/security/SecurityConfig.java` - CORS configuration
4. `backend/src/main/java/com/invoiceme/infrastructure/persistence/UserRoleConverter.java` - Enum converter (new)
5. `backend/src/main/java/com/invoiceme/infrastructure/persistence/UserStatusConverter.java` - Enum converter (new)
6. Multiple mapper interfaces - Value object mapping
7. `backend/src/main/resources/application.yml` - YAML fixes
8. Scheduled job classes - Cron expression fixes
9. Integration test files - Customer.create() signature fixes

---

## Lessons Learned

1. **Lombok + MapStruct**: Annotation processor order is critical
2. **PostgreSQL Enums**: Require explicit converters and column transformers for Hibernate compatibility
3. **Domain Encapsulation**: Use factory methods and public methods instead of direct field access
4. **Value Objects**: MapStruct needs explicit mapping methods for custom types
5. **Spring Boot 3.x**: Cron expressions require 6 fields (seconds included)

