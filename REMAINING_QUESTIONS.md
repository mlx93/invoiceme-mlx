# Remaining Questions & Information Gaps

**Date**: 2025-01-27  
**Status**: Pre-Execution Review  
**Purpose**: Identify any remaining ambiguities or missing specifications before sub-agents begin implementation.

---

## 1. Setup Instructions Assessment

### Current State
The setup instructions in `ORCHESTRATOR_OUTPUT.md` Section 10.2 are **high-level** and may not be sufficient for manual execution, especially for developers new to AWS/cloud services.

### Recommendation
✅ **YES, create a dedicated Setup Instructions Agent** — The prompt has been created in `SETUP_INSTRUCTIONS_AGENT_PROMPT.md`.

**Why**:
- Current instructions lack platform-specific variations (macOS vs Windows vs Linux)
- No troubleshooting section for common issues
- Missing verification steps (how to test each service connection)
- No screenshot annotations or detailed navigation paths
- User may have follow-up questions that would clutter orchestrator output

**Benefits**:
- User can ask Setup Agent questions without interrupting orchestrator workflow
- More detailed, step-by-step instructions with verification steps
- Platform-specific command variations
- Troubleshooting guide for common issues

---

## 2. Remaining Information Gaps

### Critical Gaps (Need Answers Before Implementation)

#### GAP-007: Scheduled Jobs Implementation Details ⚠️ **CRITICAL**
**Issue**: PRD 1 mentions "daily scheduled job" for Recurring Invoices and Late Fees but doesn't specify:
- How jobs are triggered (Spring `@Scheduled` annotation? Cron expression?)
- Where jobs run (backend instance? separate service?)
- What happens if job fails (retry logic? error handling?)
- Timezone handling (UTC? Server timezone?)

**Current PRD Coverage**:
- PRD 1 Section 4.5: "Daily scheduled job checks templates where Next Invoice Date ≤ Current Date"
- PRD 1 Section 4.6: "Scheduled job runs 1st of each month, checks overdue invoices"

**Missing Details**:
- Cron expression: `0 0 * * *` (daily at midnight UTC)?
- Spring `@Scheduled(cron = "0 0 * * *")` annotation?
- Job runs on single backend instance (no distributed locking needed)?
- Error handling: Log failures? Retry? Alert SysAdmin?

**Recommendation**: 
- **Option A**: Use Spring `@Scheduled` with cron expressions:
  - Recurring Invoices: `@Scheduled(cron = "0 0 * * *")` (daily at midnight UTC)
  - Late Fees: `@Scheduled(cron = "0 0 1 * *")` (1st of month at midnight UTC)
- **Option B**: Use AWS EventBridge (cloud-native, more scalable)
- **Decision Needed**: Which approach? (Recommend Option A for MVP simplicity)

---

#### GAP-008: API Error Response Format ⚠️ **CRITICAL**
**Issue**: PRD 2 mentions error handling but doesn't specify error response structure.

**Current PRD Coverage**:
- PRD 2 Section 4.1: Mentions "Content-Type: application/json"
- PRD 2 Section 7.2: Mentions error handling (401 → redirect, 400 → show error)

**Missing Details**:
- Error response format:
  ```json
  {
    "error": {
      "code": "VALIDATION_ERROR",
      "message": "Email is required",
      "field": "email",
      "timestamp": "2025-01-27T10:00:00Z"
    }
  }
  ```
- HTTP status codes mapping:
  - 400: Validation errors
  - 401: Unauthorized (invalid/missing token)
  - 403: Forbidden (insufficient permissions)
  - 404: Not found
  - 500: Internal server error
- Field-level validation errors format

**Recommendation**: 
- Use RFC 7807 Problem Details format (standard REST error format)
- Or simple format: `{ "error": { "code": "...", "message": "...", "field": "..." } }`
- **Decision Needed**: Which format? (Recommend RFC 7807 for production-ready API)

---

#### GAP-009: Pagination Response Format ⚠️ **CRITICAL**
**Issue**: PRD 2 mentions pagination (`?page=0&size=20&sort=createdAt,desc`) but doesn't specify response format.

**Current PRD Coverage**:
- PRD 2 Section 4.1: "Pagination: Query params `page`, `size`, `sort`"
- PRD 1 Section 4.1: "Pagination: 20 per page"

**Missing Details**:
- Response format:
  ```json
  {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "first": true,
    "last": false
  }
  ```
- Or Spring Data JPA `Page<T>` format?
- Total count included? (Performance consideration for large datasets)

**Recommendation**: 
- Use Spring Data JPA `Page<T>` format (standard Spring Boot pagination)
- Include total count (required for UI pagination controls)
- **Decision Needed**: Confirm Spring Data JPA pagination format? (Recommend yes)

---

#### GAP-010: JWT Token Refresh Mechanism ⚠️ **MAJOR**
**Issue**: PRD 2 specifies 24-hour JWT expiry but doesn't mention refresh tokens or token refresh endpoint.

**Current PRD Coverage**:
- PRD 2 Section 7.1: "Expiry: 24 hours"
- PRD 2 Section 7.1: "Storage: HttpOnly cookie (frontend), localStorage fallback"

**Missing Details**:
- Refresh token endpoint: `POST /auth/refresh`?
- Refresh token stored where? (HttpOnly cookie? Database?)
- Token refresh flow: User stays logged in automatically, or must re-login after 24 hours?
- Frontend auto-refresh logic: Refresh token before expiry (e.g., at 23 hours)?

**Recommendation**: 
- **Option A**: No refresh tokens (simpler) — User re-logs in after 24 hours
- **Option B**: Refresh tokens (better UX) — `POST /auth/refresh` endpoint, refresh token in HttpOnly cookie
- **Decision Needed**: Which approach? (Recommend Option A for MVP simplicity, Option B for production)

---

### Major Gaps (Should Clarify, But Not Blocking)

#### GAP-011: Date/Time Format Standardization
**Issue**: PRD 2 mentions "ISO date" but doesn't specify exact format (ISO 8601 with timezone?).

**Current PRD Coverage**:
- PRD 2 Section 4.9: "ISO date: '2025-02-01'"
- PRD 2 Section 4.9: "ISO datetime" (not specified)

**Missing Details**:
- Date format: `YYYY-MM-DD` (ISO 8601 date)
- DateTime format: `YYYY-MM-DDTHH:mm:ssZ` (ISO 8601 datetime with timezone)?
- Timezone: UTC? Server timezone? User timezone?

**Recommendation**: 
- Dates: `YYYY-MM-DD` (ISO 8601 date)
- DateTimes: `YYYY-MM-DDTHH:mm:ssZ` (ISO 8601 datetime, UTC timezone)
- **Decision Needed**: Confirm UTC timezone? (Recommend yes)

---

#### GAP-012: Search/Filter Query Parameter Format
**Issue**: PRD 2 mentions filtering (`?status=SENT&customerId=123`) but doesn't specify complex filters (date ranges, multiple values).

**Current PRD Coverage**:
- PRD 2 Section 4.1: "Filtering: Query params for domain filters (e.g., `?status=SENT&customerId=123`)"
- PRD 1 Section 4.1: "Filters: Type, Status, Search by name/email, Has outstanding balance"

**Missing Details**:
- Date range filters: `?issueDateFrom=2025-01-01&issueDateTo=2025-01-31`?
- Multiple status filters: `?status=SENT,OVERDUE` (comma-separated)?
- Search query: `?search=john` (searches name/email)?
- Boolean filters: `?hasOutstandingBalance=true`?

**Recommendation**: 
- Use simple query params: `?status=SENT&customerId=123&issueDateFrom=2025-01-01&issueDateTo=2025-01-31`
- Multiple values: Comma-separated `?status=SENT,OVERDUE`
- **Decision Needed**: Confirm query param format? (Recommend simple format above)

---

#### GAP-013: PDF Storage Strategy
**Issue**: PRD 2 mentions AWS S3 for PDF storage but doesn't specify: generate on-demand vs. pre-generate, caching strategy.

**Current PRD Coverage**:
- PRD 2 Section 9.4: "Stores generated invoice PDFs"
- PRD 2 Section 9.4: "Key format: `invoices/{invoiceId}.pdf`"

**Missing Details**:
- Generate on-demand (when user clicks "Download PDF") or pre-generate (when invoice sent)?
- Cache strategy: Generate once, store in S3, serve from S3?
- Signed URLs: Public read or signed URLs (expires in 1 hour)?

**Recommendation**: 
- Generate on-demand (simpler, no storage costs for unused PDFs)
- Cache in S3 after first generation (subsequent requests serve from S3)
- Use signed URLs (expires in 1 hour) for security
- **Decision Needed**: Confirm on-demand generation with S3 caching? (Recommend yes)

---

#### GAP-014: Customer Portal Authentication
**Issue**: PRD 1 mentions Customer role can "view own invoices" but doesn't specify: How does Customer user link to Customer entity?

**Current PRD Coverage**:
- PRD 1 Section 3.4: "Customer role: View own invoices, make payments"
- PRD 2 Section 5.2: "users table: `customer_id` UUID FK (nullable)"

**Missing Details**:
- Customer user registration: Must provide customer email that matches existing Customer entity?
- Or: Customer user can register independently, then SysAdmin links user to Customer entity?
- Customer portal access: User must have `customer_id` set in users table?

**Recommendation**: 
- Customer registration: User provides email, system checks if Customer entity exists with that email
- If match: Auto-link `customer_id`, approve account
- If no match: Account created but `customer_id = null`, SysAdmin must link manually
- **Decision Needed**: Confirm auto-link by email? (Recommend yes)

---

### Minor Gaps (Can Be Decided During Implementation)

#### GAP-015: Activity Feed Implementation
**Issue**: PRD 2 mentions `activity_feed` table but doesn't specify: What events are logged? How is it queried?

**Recommendation**: Log all domain events (InvoiceSent, PaymentRecorded, etc.) to activity_feed. Query via `/dashboard/recent-activity` endpoint.

---

#### GAP-016: Dashboard Cache Strategy
**Issue**: PRD 2 mentions "update dashboard cache" but doesn't specify: What caching mechanism? Redis? In-memory?

**Recommendation**: Use Spring Cache (Caffeine) for in-memory caching. Cache dashboard metrics for 5 minutes, invalidate on PaymentRecordedEvent.

---

## 3. Summary & Recommendations

### Critical Decisions ✅ **ALL RESOLVED**

1. **GAP-007**: Scheduled Jobs — ✅ **DECIDED**: Spring `@Scheduled` with cron expressions
   - Recurring Invoices: `@Scheduled(cron = "0 0 * * *")` (daily at midnight Central Time)
   - Late Fees: `@Scheduled(cron = "0 0 1 * *")` (1st of month at midnight Central Time)
   - Implementation: Spring `@EnableScheduling` on main application class

2. **GAP-008**: API Error Format — ✅ **DECIDED**: RFC 7807 Problem Details format
   - Standard format: `{ "type": "uri", "title": "...", "status": 400, "detail": "...", "instance": "..." }`
   - Field-level errors: `{ "errors": [{ "field": "email", "message": "..." }] }`

3. **GAP-009**: Pagination Format — ✅ **DECIDED**: Spring Data JPA `Page<T>` format
   - Response: `{ "content": [...], "page": 0, "size": 20, "totalElements": 150, "totalPages": 8, "first": true, "last": false }`
   - Include total count for UI pagination controls

4. **GAP-010**: JWT Refresh — ✅ **DECIDED**: No refresh tokens (user re-logs in after 24 hours)
   - Token expiry: 24 hours
   - No refresh endpoint required
   - Frontend handles token expiry by redirecting to login

### Major Decisions ✅ **ALL RESOLVED**

5. **GAP-011**: Date/Time Format — ✅ **DECIDED**: Central Time (CST/CDT)
   - Dates: `YYYY-MM-DD` (ISO 8601 date)
   - DateTimes: `YYYY-MM-DDTHH:mm:ss` (ISO 8601 datetime, Central Time)
   - Scheduled jobs run in Central Time
   - **Note**: Backend should use `America/Chicago` timezone for scheduled jobs

6. **GAP-012**: Search/Filter Format — ✅ **DECIDED**: Simple query params
   - Format: `?status=SENT&customerId=123&issueDateFrom=2025-01-01&issueDateTo=2025-01-31`
   - Multiple values: Comma-separated `?status=SENT,OVERDUE`
   - Search: `?search=john` (searches name/email)

7. **GAP-013**: PDF Storage — ✅ **DECIDED**: On-demand generation with S3 caching
   - Generate PDF when user clicks "Download PDF"
   - Cache in S3 after first generation (`invoices/{invoiceId}.pdf`)
   - Use signed URLs (expires in 1 hour) for security
   - Subsequent requests serve from S3 (no regeneration)

8. **GAP-014**: Customer Portal Auth — ✅ **DECIDED**: Auto-link Customer user by email match
   - Customer registration: User provides email
   - System checks if Customer entity exists with matching email
   - If match: Auto-link `customer_id`, approve account (or set to PENDING for SysAdmin review)
   - If no match: Account created but `customer_id = null`, SysAdmin must link manually

### Minor Decisions (Can Be Decided During Implementation)

9. **GAP-015**: Activity Feed — Log all domain events confirmed?
10. **GAP-016**: Dashboard Cache — Spring Cache (Caffeine) confirmed?

---

## 4. Action Items

1. ✅ **Setup Instructions Agent Prompt Created** — `SETUP_INSTRUCTIONS_AGENT_PROMPT.md`
2. ⚠️ **Critical Decisions Needed** — Answer GAP-007, GAP-008, GAP-009, GAP-010 before M1
3. 📝 **Major Decisions** — Answer GAP-011 through GAP-014 during M1 (or provide defaults)
4. 🔧 **Minor Decisions** — Can be decided during implementation (provide defaults)

---

## 5. Decision Log

**Date**: 2025-01-27  
**Status**: ✅ **ALL DECISIONS RESOLVED**

| Gap ID | Decision | Implementation Notes |
|--------|----------|---------------------|
| GAP-007 | Spring `@Scheduled` | Cron: `0 0 * * *` (daily, Central Time), `0 0 1 * *` (monthly, Central Time) |
| GAP-008 | RFC 7807 Problem Details | Use Spring's `ProblemDetail` class (Spring 6+) |
| GAP-009 | Spring Data JPA `Page<T>` | Standard Spring Boot pagination response |
| GAP-010 | No refresh tokens | 24-hour expiry, frontend redirects to login on 401 |
| GAP-011 | Central Time (CST/CDT) | Use `America/Chicago` timezone, scheduled jobs in Central Time |
| GAP-012 | Simple query params | `?status=SENT&customerId=123&issueDateFrom=...` |
| GAP-013 | On-demand PDF + S3 cache | Generate on click, cache in S3, signed URLs (1-hour expiry) |
| GAP-014 | Auto-link by email | Customer registration matches email to Customer entity |
| GAP-015 | Log all domain events | Activity feed records all domain events |
| GAP-016 | Spring Cache (Caffeine) | Dashboard metrics cached 5 minutes, invalidate on events |

---

**Status**: ✅ **ALL DECISIONS RESOLVED** — Ready for M1 (Domain & API Freeze)

**Next Step**: Issue sub-agent prompts and begin execution.

