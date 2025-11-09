# Dashboard Field Name Mismatch Fix

## Issue
Runtime error on dashboard page after admin login:
```
Cannot read properties of undefined (reading 'amount')
app/dashboard/page.tsx (65:67)
```

## Root Cause
Field name mismatch between backend and frontend:
- **Backend** returned: `revenueMTD` and `activeCustomersCount`
- **Frontend** expected: `totalRevenueMTD` and `activeCustomers`

When the frontend tried to access `metrics.totalRevenueMTD.amount`, it was undefined because the backend was sending `revenueMTD` instead.

## Fixes Applied

### Backend Changes
1. **`DashboardMetricsResponse.java`**:
   - Renamed `revenueMTD` → `totalRevenueMTD`
   - Renamed `activeCustomersCount` → `activeCustomers`

2. **`GetMetricsHandler.java`**:
   - Updated builder calls to use new field names:
     - `.revenueMTD(revenueMTDMoney)` → `.totalRevenueMTD(revenueMTDMoney)`
     - `.activeCustomersCount(...)` → `.activeCustomers(...)`

### Frontend Changes
1. **`app/dashboard/page.tsx`**:
   - Added null-safety checks using optional chaining (`?.`) and nullish coalescing (`??`)
   - Changed `metrics ? formatCurrency(...)` → `metrics?.totalRevenueMTD?.amount ? formatCurrency(...)`
   - Changed `metrics ? metrics.count : 0` → `metrics?.count ?? 0`

## Files Modified
- `backend/src/main/java/com/invoiceme/dashboard/getmetrics/DashboardMetricsResponse.java`
- `backend/src/main/java/com/invoiceme/dashboard/getmetrics/GetMetricsHandler.java`
- `frontend/app/dashboard/page.tsx`

## Next Steps
1. **Restart the backend** to apply the field name changes
2. **Refresh the frontend** (or wait for hot reload)
3. **Test the dashboard** - it should now display metrics without errors

## Verification
After restart, the backend will return:
```json
{
  "totalRevenueMTD": { "amount": 0.00, "currency": "USD" },
  "outstandingInvoicesCount": 0,
  "outstandingInvoicesAmount": { "amount": 0.00, "currency": "USD" },
  "overdueInvoicesCount": 0,
  "overdueInvoicesAmount": { "amount": 0.00, "currency": "USD" },
  "activeCustomers": 0
}
```

The frontend will now correctly access `metrics.totalRevenueMTD.amount` without errors.

