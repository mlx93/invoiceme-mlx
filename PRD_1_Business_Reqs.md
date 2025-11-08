# **InvoiceMe - Product Requirements Document**
## **PRD 1: Business Requirements Document**

---

## **1. Executive Summary**

InvoiceMe is a production-quality ERP-style invoicing system demonstrating mastery of Domain-Driven Design (DDD), Command Query Responsibility Segregation (CQRS), and Vertical Slice Architecture (VSA). The system serves field services businesses with complex invoicing needs across three core domains: **Customers**, **Invoices**, and **Payments**.

**Primary Goal**: Build a scalable, maintainable SaaS application proving architectural clarity while leveraging AI-assisted development tools effectively.

**Target Business**: FloodShield Restoration Services - a water damage restoration company in Austin, TX handling emergency response, structural drying, mold remediation, and reconstruction with multi-phase billing.

**Success Criteria**:
- Clean DDD/CQRS/VSA implementation
- API response times < 200ms
- Production-ready code quality
- Complete end-to-end invoice workflow
- Passing integration tests

---

## **2. Business Context: FloodShield Restoration**

**Company Profile:**
- Industry: Commercial & Residential Water Damage Restoration
- Service Area: Austin metro + Central Texas
- Team: 12 technicians, 2 project managers, 3 admin staff
- Customer Mix: 60% insurance-referred, 40% direct commercial

**Services**:
- Emergency Response (24/7)
- Water Extraction & Structural Drying
- Mold Remediation
- Reconstruction (drywall, flooring, painting)
- Contents Storage (monthly recurring fees)

**Business Metrics**:
- 40-60 jobs/month (800-1,000 invoices annually)
- 150-200 active customers
- $3K-$50K invoice values (multi-phase billing)
- Net 30 payment terms standard

---

## **3. User Roles & Permissions**

### **3.1 System Administrator (SysAdmin)**
- **Full system access**: All CRUD operations
- **Key responsibilities**: Approve user accounts, manage settings, delete customers (zero balance), configure system-wide defaults
- **Dashboard access**: Full financial metrics and reports

### **3.2 Accountant**
- **Financial focus**: Record payments, view all financial data, edit customers, create/edit invoices (draft)
- **Cannot**: Delete customers, approve user accounts
- **Dashboard access**: Payment-focused metrics

### **3.3 Sales Representative**
- **Customer-facing**: Create customers, create/edit invoices (draft/sent), view invoices they created
- **Cannot**: Record payments, access financial reports, delete anything
- **Dashboard access**: Limited (own invoices only)

### **3.4 Customer**
- **Self-service**: View own invoices, make payments, download PDFs, view credit balance
- **Cannot**: Access other customers' data, create invoices
- **Dashboard access**: Own account summary only

**RBAC Matrix:**

| Feature | SysAdmin | Accountant | Sales | Customer |
|---------|----------|------------|-------|----------|
| Create Customer | ✅ | ✅ | ✅ | ❌ |
| Edit Customer | ✅ | ✅ | ❌ | ❌ |
| Delete Customer | ✅ | ❌ | ❌ | ❌ |
| Create Invoice | ✅ | ✅ | ✅ | ❌ |
| Edit Invoice (Draft) | ✅ | ✅ | ✅ | ❌ |
| Edit Invoice (Sent) | ✅ | ❌ | ❌ | ❌ |
| Cancel Invoice | ✅ | ❌ | ❌ | ❌ |
| Record Payment | ✅ | ✅ | ❌ | ✅ (own) |
| Issue Refund | ✅ | ❌ | ❌ | ❌ |
| View Dashboard | ✅ | ✅ | ❌ | ✅ (limited) |
| Approve Users | ✅ | ❌ | ❌ | ❌ |

---

## **4. Core Functional Requirements**

### **4.1 Customer Management**

**Entity Attributes:**
- UUID, Company Name, Contact Name, Email (unique), Phone, Billing Address, Customer Type (Residential/Commercial/Insurance), Credit Balance (calculated), Account Status (Active/Inactive/Suspended), Created/Updated timestamps

**Operations:**

**Create Customer** (Command)
- Actors: SysAdmin, Accountant, Sales
- Validation: Unique email, valid format for email/phone
- Generates UUID, sets initial credit = $0, status = Active

**Update Customer** (Command)
- Actors: SysAdmin, Accountant
- Cannot change credit balance directly (calculated from payments)
- Audit log entry created

**Delete Customer** (Command)
- Actors: SysAdmin only
- Validation: Must have zero balance, all invoices paid
- Soft delete (mark Inactive, retain records)

**List Customers** (Query)
- Filters: Type, Status, Search by name/email, Has outstanding balance, Has overdue invoices
- Sort: Name, Outstanding balance, Last invoice date
- Pagination: 20 per page

---

### **4.2 Invoice Management**

**Entity Attributes:**
- UUID, Invoice Number (INV-YYYY-####, auto-increment), Customer ID (FK), Issue Date, Due Date, Status (Draft/Sent/Paid/Overdue/Cancelled), Payment Terms (Net 30/Due on Receipt/Custom), Line Items (1:many), Subtotal, Tax Amount, Discount Amount, Total Amount, Amount Paid, Balance Due, Notes, Sent/Paid timestamps, Version number

**Line Item Attributes:**
- UUID, Description, Quantity (integer, min 1), Unit Price (rounds to cent), Discount Type (None/Percentage/Fixed), Discount Value, Tax Rate (%), Line Total (calculated)

**Line Item Calculation Logic:**
```
Base Amount = Quantity × Unit Price
Discount Amount = (Type == Percentage) ? Base × (Value / 100) : Value
Taxable Amount = Base - Discount
Tax Amount = Taxable × (Tax Rate / 100)
Line Total = Taxable + Tax
```

**Invoice Lifecycle:**
```
Draft → Sent → Paid
  ↓       ↓
Cancelled Cancelled
```

**Business Rules:**
- **Draft**: Fully editable, not visible to customer, can be deleted
- **Sent**: Emailed to customer, visible in portal, line items editable with version tracking, can be cancelled by SysAdmin
- **Paid**: Balance = $0, locked (cannot edit/delete)
- **Overdue**: Auto-flagged when Current Date > Due Date AND Balance > 0, late fees applied monthly
- **Cancelled**: Voided with audit trail, cannot reactivate

**Operations:**

**Create Invoice** (Command)
- Actors: SysAdmin, Accountant, Sales
- Requires: Customer ID, Issue Date, Payment Terms, ≥1 line item
- Auto-generates Invoice Number, sets status = Draft

**Update Invoice** (Command)
- Actors: SysAdmin, Accountant (draft only), Sales (draft only)
- Draft: All editable; Sent: Line items only; Paid: No changes
- Recalculates totals when line items change

**Mark as Sent** (Command)
- Actors: SysAdmin, Accountant, Sales
- Changes Draft → Sent, sets sent_date
- Checks customer credit, auto-applies if available
- Triggers email notification with PDF

**Cancel Invoice** (Command)
- Actors: SysAdmin only
- Validation: Cannot cancel Paid invoices, no payments applied
- Sets status = Cancelled, retains audit trail

**Generate PDF** (Query)
- Actors: All roles (filtered)
- PDF includes: Company header, invoice details, itemized line items, totals, payment info
- "PAID" watermark if fully paid

**List Invoices** (Query)
- Filters: Status, Customer, Date Range (issue/due), Amount Range, Created By
- Search: Invoice number (exact), Customer name (partial), Line item description
- Sort: Number, Date, Amount, Balance
- Pagination: 20 per page

---

### **4.3 Payment Management**

**Entity Attributes:**
- UUID, Invoice ID (FK), Customer ID (FK), Amount (decimal, >0), Payment Method (Credit Card/ACH), Payment Date, Payment Reference (optional), Status (Pending/Completed/Failed/Refunded), Created By (User ID), Created timestamp, Notes

**Operations:**

**Record Payment** (Command)
- Actors: SysAdmin, Accountant, Customer (own invoices only)
- Validation: Amount > 0, Invoice in Sent/Overdue status
- Updates invoice balance atomically
- If payment = balance → Mark invoice Paid
- If payment > balance → Excess to customer credit
- Publishes PaymentRecorded domain event

**List Payments** (Query)
- Filters: Date Range, Payment Method, Customer, Invoice, Amount Range
- Sort: Date, Amount, Customer
- Pagination: 50 per page

---

### **4.4 Refunds**

**Business Context**: SysAdmin can issue refunds on paid invoices (e.g., service dispute, goodwill)

**Operations:**

**Issue Refund** (Command)
- Actors: SysAdmin only
- Requires: Invoice ID (must be Paid), Refund Amount (≤ amount paid), Reason
- Creates negative payment record (status = Refunded)
- Updates invoice: Reduces amount_paid, increases balance_due
- If balance > 0, changes status from Paid → Sent
- Option: Apply refund as customer credit OR process external refund
- Publishes RefundIssuedEvent → Sends notification

**Business Rules:**
- Partial refunds allowed (invoice reopens)
- Full refunds allowed (invoice status = Paid if re-paid, or Sent if not)
- Refund cannot exceed total amount paid
- Multiple refunds allowed on same invoice (tracked separately)

---

### **4.5 Recurring Invoices**

**Business Context**: Monthly billing for ongoing services (e.g., contents storage fees during reconstruction)

**Template Entity Attributes:**
- UUID, Customer ID (FK), Template Name, Frequency (Monthly/Quarterly/Annually), Start Date, End Date (optional), Next Invoice Date (calculated), Status (Active/Paused/Completed), Line Items (predefined), Payment Terms, Auto-Send (boolean), Created By, Created/Updated timestamps

**Operations:**

**Create Recurring Template** (Command)
- Actors: SysAdmin, Accountant
- Requires: Customer ID, Frequency, Start Date, ≥1 line item
- Calculates Next Invoice Date based on frequency
- Status = Active

**Generate Invoice from Template** (Scheduled Command)
- Trigger: Daily scheduled job checks templates where Next Invoice Date ≤ Current Date
- Creates new invoice from template line items, sets status = Sent (if Auto-Send) or Draft
- Applies customer credit if available
- Updates template's Next Invoice Date (+1 period)
- Publishes RecurringInvoiceGeneratedEvent

**Pause/Resume Template** (Command)
- Actors: SysAdmin, Accountant
- Pause: Stops auto-generation, clears Next Invoice Date
- Resume: Recalculates Next Invoice Date from current date

**Complete Template** (Command)
- Actors: SysAdmin, Accountant
- Sets status = Completed, End Date = current date
- Stops all future generation
- Cannot reactivate (read-only)

---

### **4.6 Late Fees**

**Business Rules:**
- Triggered: Scheduled job runs 1st of each month, checks overdue invoices
- Late fee = $125/month per invoice
- Added as line item: "Late Fee - [Month Year]"
- Capped at 3 months ($375 max per invoice)
- Customer receives email when first late fee applied

---

### **4.7 Customer Credit System**

**Credit Generation:**
- Overpayment (payment amount > invoice balance)
- Manual credit by SysAdmin (goodwill, refund)

**Credit Application:**
- **Automatic**: When invoice marked as Sent, system checks credit balance > 0
  - Creates discount line item: "Credit Applied - $[amount]"
  - Reduces invoice total by credit (up to full balance)
  - Updates customer credit balance
- **Manual**: Accountant can apply credits to specific invoices

**Display:**
- Customer profile page: Prominent credit balance display
- Customer portal dashboard: Credit summary card

---

### **4.8 User Management**

**Registration Flow:**
1. User submits registration (email, password, full name, desired role)
2. Account created with Status = Pending
3. SysAdmin receives notification
4. SysAdmin approves/rejects → User receives email

**Authentication:**
- JWT tokens (24-hour expiry)
- HttpOnly cookies
- Failed login lockout: 5 attempts = 1-hour lock

**Password Reset:**
- User requests reset → Email with link (valid 1 hour)
- User clicks link → Enters new password (min 8 chars)
- Password updated, token invalidated

---

### **4.9 Dashboard & Reporting**

**SysAdmin/Accountant Dashboard:**
- **Key Metrics**: Total Revenue (MTD), Outstanding Invoices (count + amount), Overdue Invoices (count + amount), Active Customers
- **Visualizations**: Revenue trend (12 months bar chart), Invoice status breakdown (pie chart), Top 10 customers by revenue, Aging report (0-30, 31-60, 61-90, 90+ days)
- **Recent Activity Feed**: Last 20 transactions (payments, invoices sent, etc.)
- **Quick Actions**: Record Payment, Create Invoice buttons

**Customer Portal Dashboard:**
- **Summary**: Total Outstanding Balance, # Unpaid Invoices, Credit Balance, Last Payment
- **Invoice List**: Filter by status, sort by due date
- **Payment History**: Last 10 payments

---

### **4.10 Notifications (AWS SES)**

**Email Types:**

1. **Invoice Sent** → Customer
   - Subject: "Invoice [Number] from FloodShield Restoration"
   - Body: Invoice summary, due date, link to portal, PDF attached

2. **Payment Received** → SysAdmin, Accountant
   - Subject: "Payment Received - Invoice [Number]"
   - Body: Payment details, customer, remaining balance

3. **Payment Overdue** → Customer (Day 1, 7, 14, 30 after due)
   - Subject: "Payment Reminder - Invoice [Number] Overdue"
   - Body: Days overdue, balance, late fee notice, link to pay

4. **Account Approved** → New User
   - Subject: "Your InvoiceMe Account Approved"
   - Body: Welcome, role confirmation, login link

5. **Password Reset** → User
   - Subject: "Password Reset Request"
   - Body: Reset link (1-hour expiry), security notice

6. **Recurring Invoice Generated** → Customer, SysAdmin
   - Subject: "New Recurring Invoice [Number]"
   - Body: Same as Invoice Sent

---

## **5. User Stories (Key Examples)**

### **US-101: Create Customer**
**As a** Sales Rep  
**I want to** create a new customer record  
**So that** I can issue invoices for completed jobs

**Acceptance Criteria:**
- Form with required fields (company, contact, email, phone, address, type)
- Email/phone validation
- Unique UUID generated
- Initial credit = $0, status = Active
- Success message with customer ID
- Redirect to customer detail

---

### **US-201: Create Multi-Line Invoice**
**As a** Sales Rep  
**I want to** create invoice with multiple line items  
**So that** I can bill for complex restoration jobs

**Acceptance Criteria:**
- Select customer, set issue date, payment terms
- Add line items: description, qty, price, discount, tax
- Auto-calculate line totals and invoice summary
- Auto-generate invoice number (INV-YYYY-####)
- Status = Draft, can save and edit later
- Min 1 line item required

---

### **US-202: Mark Invoice as Sent**
**As a** Sales Rep  
**I want to** send draft invoice to customer  
**So that** they receive bill and it becomes payable

**Acceptance Criteria:**
- Button enabled for Draft invoices only
- Validates ≥1 line item, valid customer email
- Status Draft → Sent, sets sent_date
- Checks customer credit, auto-applies if available
- Sends email with PDF to customer
- Invoice visible in customer portal
- Cannot revert to Draft

---

### **US-301: Customer Self-Service Payment**
**As a** Customer  
**I want to** pay invoices online  
**So that** I can settle account without calling

**Acceptance Criteria:**
- Portal shows unpaid invoices with "Pay Now" button
- Payment form: amount (pre-filled with balance), method (Credit Card/ACH)
- Simulated payment (instant success, no real transaction)
- If overpayment → excess to credit balance with message
- Invoice balance updated immediately
- Payment confirmation email sent
- If balance = 0 → Invoice status = Paid

---

### **US-401: Create Recurring Invoice Template**
**As an** Accountant  
**I want to** create monthly billing template  
**So that** storage fees are automatically invoiced

**Acceptance Criteria:**
- Form: customer, template name, frequency, start date, line items, auto-send toggle
- Validates start date ≥ today
- Calculates Next Invoice Date
- Status = Active
- Success message with first generation date

---

### **US-402: Auto-Generate Recurring Invoice**
**As the** System  
**I want to** generate invoices from templates  
**So that** customers billed on schedule automatically

**Acceptance Criteria:**
- Scheduled job runs daily at 12:00 AM
- Checks active templates where Next Invoice Date ≤ today
- Creates invoice from template line items
- If Auto-Send → status = Sent, email sent
- If not Auto-Send → status = Draft
- Applies customer credit if available
- Updates template Next Invoice Date (+1 period)
- SysAdmin receives daily summary

---

### **US-501: View Financial Dashboard**
**As a** SysAdmin  
**I want to** see key metrics at a glance  
**So that** I can monitor business health

**Acceptance Criteria:**
- Top metrics: Revenue (MTD), Outstanding (count/amount), Overdue (count/amount), Active Customers
- Charts: Revenue trend (12 months), Invoice status pie, Top 10 customers bar
- Aging report table (0-30, 31-60, 61-90, 90+)
- Recent activity feed (last 20 transactions)
- Quick action buttons (Record Payment, Create Invoice)
- Export to CSV button
- Dashboard loads in <2 seconds

---

### **US-601: Issue Refund**
**As a** SysAdmin  
**I want to** refund a paid invoice  
**So that** I can resolve disputes or provide goodwill credits

**Acceptance Criteria:**
- "Issue Refund" button on paid invoices
- Form: refund amount (≤ amount paid), reason, apply as credit checkbox
- Validates amount ≤ total paid
- Creates negative payment record (status = Refunded)
- Updates invoice: reduces amount_paid, increases balance_due
- If balance > 0 → status Paid → Sent
- If "apply as credit" → adds to customer credit balance
- Customer receives refund notification email
- Audit log entry created

---

## **6. Non-Functional Requirements**

### **Performance**
- API response time: <200ms (standard CRUD)
- Page load time: <2 seconds
- Dashboard load: <2 seconds
- PDF generation: <3 seconds
- Database queries: <500ms (complex queries)

### **Security**
- JWT authentication (24-hour expiry)
- RBAC enforced at API and UI layers
- Bcrypt password hashing (10+ rounds)
- Parameterized queries (SQL injection prevention)
- Input sanitization (XSS prevention)
- HTTPS only in production
- Account lockout: 5 failed logins = 1-hour lock

### **Scalability**
- Support 10,000+ customers, 100,000+ invoices
- Horizontal scaling capability
- Database connection pooling (HikariCP)
- Pagination on all list views

### **Reliability**
- 99.5% uptime target
- Daily automated backups (Supabase managed)
- ACID transaction compliance
- Graceful error handling with user-friendly messages

### **Maintainability**
- 70%+ code coverage (business logic)
- Inline documentation for complex logic
- Consistent naming conventions
- Modular vertical slice architecture

### **Usability**
- Mobile-responsive customer portal
- Browser support: Chrome, Firefox, Safari, Edge (latest 2 versions)
- WCAG 2.1 Level AA compliance
- Clear, actionable error messages

---

## **7. Testing Requirements**

### **Integration Tests (Mandatory)**

**Test Scenarios:**

1. **Customer Payment Flow**
   - Create customer → Create invoice → Mark sent → Record payment → Verify paid
   - Assert: Status = Paid, Payment recorded, Email sent, Audit log

2. **Partial Payment Flow**
   - Create invoice ($5K) → Record payment ($2K) → Verify balance = $3K
   - Assert: Status = Sent, Balance updated

3. **Overpayment & Credit**
   - Invoice ($1K) → Payment ($1.2K) → Verify credit = $200
   - Create 2nd invoice ($500) → Mark sent → Verify credit applied, total = $300
   - Assert: Credit balance = $0

4. **Late Fee Calculation**
   - Create invoice (due 30 days ago) → Run scheduled job
   - Assert: Late fee line item added, total +$125, Email sent

5. **Recurring Invoice**
   - Create template (monthly) → Run scheduled job
   - Assert: Invoice created, status = Sent, Next date updated

6. **Refund Flow**
   - Create invoice ($1K) → Payment ($1K) → Issue refund ($300)
   - Assert: Status Paid → Sent, Balance = $300, Refund recorded

---

## **8. Success Criteria**

✅ All CRUD operations implemented (Customers, Invoices, Payments, Recurring Templates, Refunds)  
✅ Invoice lifecycle (Draft → Sent → Paid) functioning  
✅ Payment processing and credit system working  
✅ Recurring invoices auto-generating  
✅ Late fees applying correctly  
✅ Refunds processing properly  
✅ Email notifications sending  
✅ Dashboard showing accurate metrics  
✅ DDD principles evident (rich domain models)  
✅ CQRS separation (command/query handlers)  
✅ Vertical slice architecture (feature-based folders)  
✅ Domain events with transactional consistency  
✅ Integration tests passing  
✅ API response times <200ms  
✅ Code is modular, documented, production-ready  

---

## **9. Out of Scope**

- Multi-currency support
- Real payment gateway integration (Stripe/PayPal)
- Inventory management
- Multi-tenancy (multiple companies)
- Advanced reporting (P&L, tax liability)
- Document attachments (photos, insurance docs)
- Cryptocurrency payments (USDC)
- Mobile native apps

---

**End of PRD 1: Business Requirements Document**
