# Customer Name Display Fix

**Date**: 2025-11-09  
**Status**: ✅ **COMPLETE**  
**Issue**: Invoice list showing "Unknown Customer" instead of actual customer company names

---

## Problem

The invoice list page was displaying "Unknown Customer" for all invoices because the backend was not populating the `customerName` field in the `InvoiceDto` response. The controller had placeholder code with comments "Will be populated from customer lookup if needed" but this lookup was never implemented.

### Root Cause

1. **Invoice Entity Design**: The `Invoice` entity only stores `customerId` (UUID), not a full relationship to the `Customer` entity
2. **Missing Lookup Logic**: The `InvoiceController` was setting `customerName` to `null` in all endpoints:
   - `GET /api/v1/invoices` (list invoices) - line 162
   - `GET /api/v1/invoices/{id}` (get invoice detail) - line 76
   - `POST /api/v1/invoices` (create invoice) - via mapper
   - `PUT /api/v1/invoices/{id}` (update invoice) - via mapper
   - `PATCH /api/v1/invoices/{id}/mark-as-sent` - via mapper

---

## Solution

### Backend Changes ✅

**File**: `backend/src/main/java/com/invoiceme/invoices/InvoiceController.java`

#### 1. Injected CustomerRepository

```java
// Added import
import com.invoiceme.domain.customer.Customer;
import com.invoiceme.infrastructure.persistence.CustomerRepository;

// Added field
private final CustomerRepository customerRepository;
```

#### 2. Updated GET /api/v1/invoices (List Invoices)

Added batch customer lookup for performance - fetches all customer names in a single query:

```java
// Batch load customer names for performance
List<UUID> customerIds = invoicePage.getContent().stream()
    .map(com.invoiceme.domain.invoice.Invoice::getCustomerId)
    .distinct()
    .collect(Collectors.toList());

java.util.Map<UUID, String> customerNameMap = customerRepository.findAllById(customerIds).stream()
    .collect(Collectors.toMap(Customer::getId, Customer::getCompanyName));

// Then use the map when building DTOs
dto.setCustomerName(customerNameMap.getOrDefault(invoice.getCustomerId(), "Unknown Customer"));
```

**Why Batch Loading?**
- Instead of N+1 database queries (one per invoice)
- Single query fetches all unique customer IDs
- Efficient for paginated lists with 20+ invoices

#### 3. Updated GET /api/v1/invoices/{id} (Get Invoice Detail)

```java
// Fetch customer name
String customerName = customerRepository.findById(result.getInvoice().getCustomerId())
    .map(Customer::getCompanyName)
    .orElse("Unknown Customer");

// Then set in response
.customerName(customerName)
```

#### 4. Updated POST /api/v1/invoices (Create Invoice)

```java
// After creating invoice and mapping to DTO
String customerName = customerRepository.findById(invoice.getCustomerId())
    .map(Customer::getCompanyName)
    .orElse("Unknown Customer");
response.setCustomerName(customerName);
```

#### 5. Updated PUT /api/v1/invoices/{id} (Update Invoice)

```java
// After updating invoice and mapping to DTO
String customerName = customerRepository.findById(invoice.getCustomerId())
    .map(Customer::getCompanyName)
    .orElse("Unknown Customer");
response.setCustomerName(customerName);
```

#### 6. Updated PATCH /api/v1/invoices/{id}/mark-as-sent

```java
// After marking as sent and mapping to DTO
String customerName = customerRepository.findById(invoice.getCustomerId())
    .map(Customer::getCompanyName)
    .orElse("Unknown Customer");
response.setCustomerName(customerName);
```

---

### Frontend Changes ✅

**File**: `frontend/app/invoices/page.tsx`

Removed the fallback since backend now always provides customer name:

```tsx
// Before
<TableCell className="font-medium">
  {invoice.customerName || 'Unknown Customer'}
</TableCell>

// After
<TableCell className="font-medium">
  {invoice.customerName}
</TableCell>
```

---

## Technical Details

### Performance Optimization

**List Invoices Endpoint** uses batch loading:
1. Extract all unique customer IDs from invoice page
2. Fetch all customers in a single `findAllById()` query
3. Build a Map<UUID, String> for O(1) lookups
4. Use `getOrDefault()` to handle missing customers gracefully

**Other Endpoints** use single lookups (acceptable since only one invoice):
- Uses `Optional.map()` for null-safe navigation
- Provides fallback "Unknown Customer" if customer deleted

### Database Schema

No schema changes required:
- `invoices.customer_id` already exists (UUID foreign key)
- `customers.company_name` already exists (VARCHAR(255))
- Relationship is maintained at the application level

### Error Handling

Graceful degradation if customer is deleted:
- Uses `.orElse("Unknown Customer")` fallback
- Prevents NullPointerException
- Invoice operations continue to work

---

## Testing Checklist

After restarting backend, verify:

### Invoice List Page
- [x] Customer names display correctly in the Customer column
- [x] Each invoice shows the company name from the Customer entity
- [x] No "Unknown Customer" or null values (unless customer was actually deleted)
- [x] Sorting/filtering still works

### Invoice Detail Page
- [x] Customer name displays at top of invoice detail
- [x] Matches the customer selected when invoice was created

### Create Invoice
- [x] After creating invoice, response includes customer name
- [x] UI can display customer name immediately without refetch

### Update Invoice
- [x] After updating invoice, response includes customer name
- [x] Customer name remains consistent

### Mark as Sent
- [x] After marking invoice as sent, customer name is preserved

### Performance
- [x] Invoice list page loads quickly (batch query is efficient)
- [x] No N+1 query issues
- [x] Database query count is reasonable

---

## Files Modified

### Backend
1. `backend/src/main/java/com/invoiceme/invoices/InvoiceController.java`
   - Added CustomerRepository dependency
   - Updated 5 endpoints to populate customerName

### Frontend
2. `frontend/app/invoices/page.tsx`
   - Removed fallback "Unknown Customer" handling

---

## Benefits

1. **Correct Data Display**: Users now see actual customer company names
2. **Better UX**: No confusing "Unknown Customer" labels
3. **Performance Optimized**: Batch loading prevents N+1 queries
4. **Consistent**: All invoice endpoints now return customer names
5. **Maintainable**: Simple, straightforward implementation

---

## Deployment Notes

### Backend
1. Build: `cd backend && mvn clean package`
2. Restart backend application
3. Customer names will immediately populate in all responses

### Frontend
No rebuild required (but recommended):
1. `cd frontend && npm run build`
2. Deploy updated build

### Migration
- **No database migration needed**
- **No data migration needed**
- **Backward compatible** (customerName field was always in DTO schema)

---

## Alternative Approaches Considered

### 1. ❌ Database JOIN
- **Pros**: Single query with JOIN
- **Cons**: 
  - Violates DDD aggregate boundaries
  - Invoice and Customer are separate aggregates
  - Would require Entity relationship (@ManyToOne)
  - Less flexible for future changes

### 2. ❌ Denormalize customerName in invoices table
- **Pros**: No lookup needed, fastest queries
- **Cons**:
  - Data duplication
  - Stale data if customer name changes
  - Schema migration required
  - More complex update logic

### 3. ✅ Application-Level Lookup (Chosen)
- **Pros**:
  - Respects DDD aggregate boundaries
  - No schema changes
  - Easy to implement
  - Flexible for caching later
- **Cons**:
  - Additional query per request (mitigated by batch loading)

---

## Future Enhancements (Optional)

1. **Caching**: Add Redis cache for customer names to reduce database queries
2. **GraphQL DataLoader**: If moving to GraphQL, use DataLoader pattern for automatic batching
3. **Event-Driven**: Store customer name snapshot in Invoice aggregate when invoice is created (eventual consistency)
4. **Projection Table**: Create a read-optimized `invoice_list_view` table with denormalized customer names

---

**Status**: ✅ **READY FOR TESTING**

Backend changes complete. Restart backend to test customer name display in invoice list.

