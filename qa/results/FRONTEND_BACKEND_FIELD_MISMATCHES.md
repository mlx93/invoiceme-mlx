# Frontend-Backend Field Name Mismatches

## Summary
This document lists all identified field name mismatches between frontend TypeScript types and backend Java response classes.

## Critical Mismatches (Will Cause Runtime Errors)

### 1. RevenueTrendResponse ❌
**Backend** (`RevenueTrendResponse.java`):
- Field: `dataPoints` (List<RevenueDataPoint>)
- Nested fields: `period` (LocalDate), `revenue`, `invoiceCount`

**Frontend** (`dashboard.ts`):
- Field: `data` (RevenueTrendData[])
- Nested fields: `month` (string), `revenue`

**Impact**: Frontend accesses `revenueData.data` but backend returns `dataPoints`. Also, frontend expects `month` but backend returns `period`.

**Location**: `frontend/app/dashboard/page.tsx:111-113`

---

### 2. InvoiceStatusResponse ❌
**Backend** (`InvoiceStatusResponse.java`):
- Field: `breakdown` (List<StatusBreakdown>)
- Nested fields: `status`, `count`, `totalAmount`

**Frontend** (`dashboard.ts`):
- Field: `data` (InvoiceStatusData[])
- Nested fields: `status`, `count`, `amount`

**Impact**: Frontend accesses `statusData.data` but backend returns `breakdown`. Also, frontend expects `amount` but backend returns `totalAmount`.

**Location**: `frontend/app/dashboard/page.tsx:137-150`

---

### 3. AgingReportResponse ❌
**Backend** (`AgingReportResponse.java`):
- Field: `buckets` (List<AgingBucket>)
- Nested fields: `range` (String), `invoiceCount` (Integer), `totalAmount` (Money)

**Frontend** (`dashboard.ts`):
- Field: `data` (AgingReportData[])
- Nested fields: `bucket` ('0-30' | '31-60' | '61-90' | '90+'), `count` (number), `amount` (Money)

**Impact**: Frontend accesses `agingData.data` but backend returns `buckets`. Also:
- Frontend expects `bucket` but backend returns `range`
- Frontend expects `count` but backend returns `invoiceCount`
- Frontend expects `amount` but backend returns `totalAmount`

**Location**: `frontend/app/dashboard/page.tsx:167-190`

---

## Potential Issues (May Work But Inconsistent)

### 4. LoginResponse ⚠️
**Backend** (`LoginResponse.java`):
- Structure: `{ token: String, user: AuthenticatedUser }`
- `AuthenticatedUser` has: `id`, `email`, `fullName`, `role`, `status`, `customerId`, `createdAt`

**Frontend** (`user.ts`):
- Structure: `{ token: string, user: UserResponse }`
- `UserResponse` has: `id`, `email`, `fullName`, `role`, `customerId?`, `status`, `createdAt`

**Status**: ✅ Should work (Jackson maps nested objects correctly), but verify the structure matches exactly.

**Location**: `frontend/src/hooks/useAuth.ts:18`

---

## Verified Matches ✅

### DashboardMetricsResponse
- ✅ Fixed: `revenueMTD` → `totalRevenueMTD`, `activeCustomersCount` → `activeCustomers`

### CustomerDetailResponse
- ✅ Matches: Extends `CustomerDto` correctly, all fields align

### InvoiceDetailResponse
- ✅ Matches: All fields align (dates are handled by Jackson serialization)

### PaymentDetailResponse
- ✅ Matches: All fields align (dates are handled by Jackson serialization)

---

## Recommended Fixes

### Priority 1: Fix Dashboard Responses

1. **RevenueTrendResponse**:
   - Change backend field `dataPoints` → `data`
   - Change nested field `period` → `month` (and format as string)
   - Remove `invoiceCount` if not needed by frontend

2. **InvoiceStatusResponse**:
   - Change backend field `breakdown` → `data`
   - Change nested field `totalAmount` → `amount`

3. **AgingReportResponse**:
   - Change backend field `buckets` → `data`
   - Change nested field `range` → `bucket`
   - Change nested field `invoiceCount` → `count`
   - Change nested field `totalAmount` → `amount`

### Priority 2: Verify LoginResponse
- Test login flow to ensure nested `user` object maps correctly
- Verify all fields are present and correctly typed

---

## Testing Checklist

After fixes, verify:
- [ ] Dashboard loads without errors
- [ ] Revenue trend chart displays data
- [ ] Invoice status pie chart displays data
- [ ] Aging report table displays data
- [ ] Login response structure matches frontend expectations

