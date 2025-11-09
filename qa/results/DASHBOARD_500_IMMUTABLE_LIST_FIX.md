# Dashboard 500 Errors - Immutable List Fix

## Issue
All dashboard endpoints were returning 500 Internal Server Error after login:
- `/api/v1/dashboard/metrics` - 500
- `/api/v1/dashboard/invoice-status` - 500  
- `/api/v1/dashboard/aging-report` - 500
- `/api/v1/dashboard/revenue-trend?months=12` - 500

## Root Cause
Spring Data's `Page.getContent()` returns an **immutable list**. When handlers tried to call `.addAll()` on this list to combine results from multiple queries, it threw `UnsupportedOperationException`, causing 500 errors.

### Problematic Code Pattern:
```java
var outstandingInvoices = invoiceRepository.findByStatus(
    InvoiceStatus.SENT,
    PageRequest.of(0, Integer.MAX_VALUE)
).getContent(); // Returns immutable list
outstandingInvoices.addAll(...); // ❌ UnsupportedOperationException
```

## Fix Applied

### Files Fixed:
1. **`backend/src/main/java/com/invoiceme/dashboard/getmetrics/GetMetricsHandler.java`**
2. **`backend/src/main/java/com/invoiceme/dashboard/getagingreport/GetAgingReportHandler.java`**

### Solution:
Wrap the immutable list in a new `ArrayList` before adding to it:

```java
List<Invoice> outstandingInvoices = new ArrayList<>(
    invoiceRepository.findByStatus(
        InvoiceStatus.SENT,
        PageRequest.of(0, Integer.MAX_VALUE)
    ).getContent() // Immutable list
); // Now mutable ArrayList
outstandingInvoices.addAll(...); // ✅ Works correctly
```

### Changes Made:

**GetMetricsHandler.java:**
- Changed `var outstandingInvoices = ...` to `List<Invoice> outstandingInvoices = new ArrayList<>(...)`
- Added imports: `import com.invoiceme.domain.invoice.Invoice;`, `import java.util.ArrayList;`, `import java.util.List;`

**GetAgingReportHandler.java:**
- Changed `var outstandingInvoices = ...` to `List<Invoice> outstandingInvoices = new ArrayList<>(...)`
- No additional imports needed (already had `ArrayList` and `List`)

## Testing Required

After restarting the backend, test these endpoints:
1. `GET /api/v1/dashboard/metrics`
2. `GET /api/v1/dashboard/invoice-status`
3. `GET /api/v1/dashboard/aging-report`
4. `GET /api/v1/dashboard/revenue-trend?months=12`

All should return 200 OK instead of 500 Internal Server Error.

## Next Steps

1. **Restart backend** to apply the fixes
2. **Test dashboard endpoints** after login
3. **Verify dashboard page loads** without errors

## Related Issues

This was the final piece needed to resolve the dashboard 500 errors. Previous fixes included:
- Lambda variable naming (`Invoice` → `invoice`)
- Revenue trend parameter handling (`months` parameter support)

All dashboard endpoints should now work correctly.

