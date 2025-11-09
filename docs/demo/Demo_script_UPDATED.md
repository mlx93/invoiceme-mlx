# **InvoiceMe - 5-Minute Demo Script (Updated - No Recurring Invoices)**

---

## **Demo Overview**

**Duration**: 5 minutes  
**Scenario**: FloodShield Restoration - Emergency water damage job for Riverside Apartments  
**Demonstrates**: Core InvoiceMe.md requirements + 2 standout features (Credit System, Refunds)

---

## **Minutes 0-1.5: Core Requirements - Customer & Invoice Lifecycle**

### **0:00-0:30 - Create Customer & Multi-Line Invoice**
**Narration**: *"FloodShield responds to an emergency water damage call. Let me create the customer and invoice."*

**Actions**:
- Already logged in as SysAdmin
- Navigate to Customers → Create: `Riverside Apartments LLC`, `john@riversideapts.com`
- Click "Create Customer"
- Navigate to Invoices → Create New
- Select customer: Riverside Apartments
- Payment Terms: Net 30
- **Add 4 line items** (show multi-line requirement):
  1. Emergency Response Fee: $450
  2. Water Extraction (6 hours): $750
  3. Equipment Setup: $2,100
  4. Anti-microbial Treatment: $950
- **Show calculation**: Subtotal $4,250 + Tax $351 = **Total $4,601**
- Save as Draft

### **0:30-1:00 - Invoice Lifecycle: Draft → Sent**
**Narration**: *"Now I'll send the invoice to trigger our domain event system."*

**Actions**:
- Click "Mark as Sent"
- **Show**: Status changes Draft → Sent
- **Show**: Email notification sent (toast/popup)
- **Highlight**: *"Domain event published → Email listener triggered → AWS SES sends"*

### **1:00-1:30 - Payment Recording**
**Narration**: *"The customer pays immediately via our self-service portal."*

**Actions**:
- Switch to Customer Portal (already logged in as john@riversideapts.com)
- **Show**: Invoice appears in customer dashboard
- Click "Pay Now"
- Amount: **$4,701** (overpay by $100)
- Payment Method: Credit Card
- Submit Payment
- **Show**: Success message: *"Payment received. Excess $100 added to credit balance."*
- **Show**: Invoice status changes to Paid
- **Show**: Credit balance: $100.00

---

## **Minutes 1.5-3: Core Requirements - Complex Business Logic**

### **1:30-2:15 - Create Second Invoice**
**Narration**: *"Week later, we invoice for the monitoring phase."*

**Actions**:
- Switch back to Admin Portal
- Create new invoice for Riverside Apartments
- Add 2 line items:
  1. Drying Monitoring (7 days): $1,050
  2. Equipment Rental: $1,225
- **Show**: Subtotal $2,275 + Tax $188 = **Total $2,463**

### **2:15-2:45 - Automatic Credit Application**
**Narration**: *"Watch what happens when I mark this as sent."*

**Actions**:
- Click "Mark as Sent"
- **Show**: System processing: *"Applying customer credit..."*
- **Show**: New line item auto-added: "Credit Applied - $100"
- **Show**: **Updated total: $2,363** (reduced from $2,463)
- **Highlight**: *"Credit balance: $100 → $0. Business logic in domain model."*

### **2:45-3:00 - Partial Payment**
**Narration**: *"Customer makes a partial payment this time."*

**Actions**:
- Customer portal: Pay $1,500 on $2,363 invoice
- **Show**: Balance Due updates to $863
- **Show**: Status remains "Sent" (not fully paid)
- **Highlight**: *"Invoice tracks amount paid vs. balance due - CQRS query side."*

---

## **Minutes 3-4.5: Extra Features (Not in InvoiceMe.md)**

### **3:00-3:45 - Refund Processing** ⭐ **STANDOUT FEATURE 1**
**Narration**: *"Customer disputes part of the work. Let's issue a partial refund."*

**Actions**:
- Navigate to paid invoice (INV-2025-0001)
- Click "Issue Refund"
- Amount: $500
- Reason: "Quality issue resolved"
- Apply as Credit: ✓ Checked
- Submit
- **Show**: Invoice status: Paid → Sent (balance reopened: $500)
- **Show**: Customer credit balance: $0 → $500
- **Show**: Payment history shows refund (negative entry)
- **Highlight**: *"Refund reopens invoice, applies credit. Complex state transitions."*

### **3:45-4:15 - Late Fees** ⭐ **STANDOUT FEATURE 2**
**Narration**: *"Let me show you our automated late fee system."*

**Actions**:
- Navigate to an overdue invoice (or manually set due date to 31 days ago)
- **Show**: Invoice shows "Overdue" badge
- **Show**: Late fee line item added ($125) - *"Automated by scheduled job on 1st of month"*
- **Highlight**: *"Scheduled job runs monthly, adds late fees automatically. Event-driven architecture."*

---

## **Minutes 4.5-5: Architecture Highlight**

### **4:15-4:45 - Dashboard Metrics**
**Narration**: *"Here's the financial dashboard showing real-time metrics."*

**Actions**:
- Navigate to Dashboard
- **Show**:
  - Total Revenue MTD: $6,201
  - Outstanding Invoices: 2 ($1,363)
  - Active Customers: 1
  - Revenue trend chart (last 12 months)
  - Invoice status pie chart
  - Recent activity feed (shows all events)
- **Highlight**: *"Query side pulls from domain events + calculated views. CQRS separation."*

### **4:45-5:00 - Architecture Summary**
**Narration**: *"InvoiceMe demonstrates enterprise architecture at scale."*

**Screen**: Show VS Code with folder structure

**Key Points** (rapid-fire):
- ✅ **Domain-Driven Design**: Rich domain models with behavior (Invoice.markAsSent(), Payment.record())
- ✅ **CQRS**: Separate command handlers (write) and query handlers (read)
- ✅ **Vertical Slice Architecture**: Each feature isolated (createinvoice/, recordpayment/)
- ✅ **Domain Events**: Transactional consistency with @TransactionalEventListener(AFTER_COMMIT)
- ✅ **Clean Architecture**: Domain → Application → Infrastructure layers
- ✅ **Integration Tests**: Complete workflows tested (customer → invoice → payment)

**Closing**: *"Production-ready, scalable, maintainable - built with AI assistance following solid architecture principles."*

---

## **Demo Preparation Checklist**

**Pre-Demo Setup:**
- [ ] Fresh database with seed data (SysAdmin account only)
- [ ] Admin portal logged in (admin@floodshield.com or test SysAdmin)
- [ ] Customer portal open in separate tab (not logged in yet)
- [ ] VS Code open to show folder structure
- [ ] Demo runs in local environment (fast, no network delays)

**Browser Tabs (Order):**
1. Admin Portal - Dashboard
2. Customer Portal - Login page
3. VS Code - Folder structure
4. (Optional) Email preview tool

**Talking Points to Emphasize:**
- **Multi-line items** → InvoiceMe.md requirement
- **Invoice lifecycle (Draft → Sent → Paid)** → InvoiceMe.md requirement
- **Payment with balance calculation** → InvoiceMe.md requirement
- **Domain events** → InvoiceMe.md encouraged feature
- **Customer credit system** → Extra feature (not in requirements)
- **Refunds** → Extra feature (not in requirements)
- **Late fees** → Extra feature (not in requirements)
- **DDD/CQRS/VSA** → Core architecture requirements

---

## **Backup: If Demo Runs Short (<5 min)**

**Show RBAC:**
- Log in as different roles (Accountant, Sales)
- Show different permissions (Sales can't record payment, etc.)

**Show Dashboard Details:**
- Drill into revenue trend chart
- Show aging report
- Show activity feed details

---

## **Technical Q&A Prep**

**If asked: "How does credit application work?"**
- *"When MarkAsSent command executes, it checks customer.creditBalance in the domain model. If > 0, creates a discount line item and calls customer.deductCredit(amount). Domain event published after transaction commit."*

**If asked: "How do you ensure consistency between payment and invoice update?"**
- *"@Transactional annotation on RecordPaymentCommandHandler. Entire operation (payment creation, invoice.recordPayment(), credit application) happens in one database transaction. If any step fails, everything rolls back."*

**If asked: "Where is the invoice PDF stored?"**
- *"PDF generation is stubbed for demo purposes. In production, we'd generate on-demand using iText library and cache in AWS S3 with invoice ID as key."*

---

**End of 5-Minute Demo Script**

