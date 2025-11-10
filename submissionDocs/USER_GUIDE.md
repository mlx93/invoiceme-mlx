# InvoiceMe User Guide

**Version**: 1.0  
**Last Updated**: January 2025

---

## Table of Contents

1. [Getting Started](#getting-started)
2. [Customer Management](#customer-management)
3. [Invoice Management](#invoice-management)
4. [Payment Processing](#payment-processing)
5. [Refunds](#refunds)
6. [Dashboard & Reports](#dashboard--reports)
7. [Customer Portal](#customer-portal)

---

## Getting Started

### Login

1. Navigate to the InvoiceMe application URL
2. Enter your email address and password
3. Click **Login**

**Note**: New users must be approved by a System Administrator before they can log in.

### Role-Based Access

InvoiceMe supports four user roles with different permissions:

- **SysAdmin**: Full access to all features
- **Accountant**: Access to customers, invoices, payments, dashboard, and refunds
- **Sales**: Access to customers and invoices (read-only for payments)
- **Customer**: Access to own invoices and payments only

### Navigation

The main navigation menu includes:
- **Dashboard**: Overview of key metrics and charts
- **Customers**: Customer management
- **Invoices**: Invoice management
- **Payments**: Payment history
- **Refunds**: Refund management (SysAdmin/Accountant only)
- **Users**: User approval (SysAdmin only)

---

## Customer Management

### Creating Customers

**Roles**: SysAdmin, Accountant, Sales

1. Navigate to **Customers** → Click **Create Customer**
2. Fill in the customer information:
   - **Company Name** (required)
   - **Contact Name** (optional)
   - **Email** (required, must be unique)
   - **Phone** (optional)
   - **Address** (optional): Street, City, State, ZIP Code, Country
   - **Customer Type**: Residential, Commercial, or Insurance
3. Click **Create Customer**

**Note**: When a customer is created, a user account is automatically created with:
- Email: Same as customer email
- Password: `test1234` (customer should change on first login)
- Status: ACTIVE
- Role: CUSTOMER

### Updating Customer Information

**Roles**: SysAdmin, Accountant

1. Navigate to **Customers** → Click on a customer
2. Click **Edit Customer**
3. Update the fields you want to change
4. Click **Save Changes**

**Note**: Credit balance cannot be updated directly (it's calculated from payments and refunds).

### Viewing Customer Details

**Roles**: All (filtered by ownership for Customer role)

1. Navigate to **Customers** → Click on a customer
2. View customer details including:
   - Contact information
   - Credit balance
   - Invoice history
   - Payment history

### Customer Credit Balance

The credit balance shows available credit from:
- Overpayments (when payment > invoice balance)
- Refunds applied as credit
- Manual credit adjustments

Credit is automatically applied when invoices are marked as sent (if customer has available credit).

---

## Invoice Management

### Creating Invoices

**Roles**: SysAdmin, Accountant, Sales

1. Navigate to **Invoices** → Click **Create Invoice**
2. Select a **Customer** from the dropdown
3. Enter invoice details:
   - **Issue Date** (defaults to today)
   - **Payment Terms**: NET_30, DUE_ON_RECEIPT, or CUSTOM
   - **Due Date** (auto-calculated for NET_30, required for CUSTOM)
4. Add **Line Items**:
   - Click **Add Line Item**
   - Enter **Description** (e.g., "Water Extraction Service")
   - Enter **Quantity** (minimum 1)
   - Enter **Unit Price** (e.g., 500.00)
   - Select **Discount Type**: NONE, PERCENTAGE, or FIXED
   - Enter **Discount Value** (if applicable)
   - Enter **Tax Rate** (percentage, e.g., 8.25 for 8.25%)
   - Click **Save Line Item**
5. Add additional line items as needed
6. Optionally add **Notes**
7. Click **Create Invoice**

**Invoice Status**: New invoices are created with status **DRAFT** (not visible to customers).

### Editing Draft Invoices

**Roles**: SysAdmin, Accountant, Sales

1. Navigate to **Invoices** → Click on a **DRAFT** invoice
2. Click **Edit Invoice**
3. Modify line items, dates, or notes
4. Click **Save Changes**

**Note**: You can add, remove, or modify line items in DRAFT invoices.

### Marking Invoices as Sent

**Roles**: SysAdmin, Accountant, Sales

1. Navigate to **Invoices** → Click on a **DRAFT** invoice
2. Click **Mark as Sent**
3. Confirm the action

**What Happens**:
- Invoice status changes to **SENT**
- Invoice becomes visible to the customer in their portal
- If customer has credit balance, it's automatically applied as a discount line item
- An email notification is sent to the customer (if email is configured)

**Note**: Once an invoice is marked as SENT, you can still edit line items, but changes are tracked with version numbers for audit purposes.

### Viewing Invoice Details

**Roles**: All (filtered by ownership for Customer role)

1. Navigate to **Invoices** → Click on an invoice
2. View invoice details including:
   - Invoice number (format: INV-YYYY-####)
   - Customer information
   - Line items with calculations
   - Totals (subtotal, tax, discount, total amount)
   - Payment history
   - Balance due

### Canceling Invoices

**Roles**: SysAdmin only

1. Navigate to **Invoices** → Click on a **DRAFT** or **SENT** invoice
2. Click **Cancel Invoice**
3. Confirm the action

**Business Rules**:
- Cannot cancel PAID invoices (must issue refund instead)
- Cannot cancel if payments have been applied (must issue refund first)
- Cancelled invoices cannot be reactivated

---

## Payment Processing

### Recording Payments (Admin)

**Roles**: SysAdmin, Accountant

1. Navigate to **Payments** → Click **Record Payment**
2. Select an **Invoice** (only SENT or OVERDUE invoices are available)
3. Enter payment details:
   - **Amount** (must be > 0, cannot exceed invoice balance)
   - **Payment Method**: Credit Card or ACH
   - **Payment Date** (defaults to today)
   - **Payment Reference** (optional, e.g., "VISA-4532")
   - **Notes** (optional)
4. Click **Record Payment**

**What Happens**:
- Payment is recorded and applied to the invoice
- Invoice balance is updated (balanceDue = totalAmount - amountPaid)
- If payment = invoice balance, invoice status changes to **PAID**
- If payment > invoice balance, excess goes to customer credit balance
- An email confirmation is sent to the customer (if email is configured)

### Paying Invoices (Customer Portal)

**Roles**: Customer

1. Log in to the Customer Portal
2. Navigate to **My Invoices**
3. Click on an unpaid invoice (status: SENT or OVERDUE)
4. Click **Pay Invoice**
5. Enter payment details:
   - **Amount** (defaults to balance due, can be adjusted for partial payment)
   - **Payment Method**: Credit Card or ACH
   - **Payment Reference** (optional)
6. Click **Submit Payment**

**Note**: Customers can make partial payments. The invoice remains open until fully paid.

### Viewing Payment History

**Roles**: SysAdmin, Accountant, Customer (own payments only)

1. Navigate to **Payments** (or **My Payments** for customers)
2. View payment list with:
   - Invoice number
   - Customer name (admin view)
   - Payment amount
   - Payment date
   - Payment method
   - Status

### Partial Payments

Partial payments are supported:
- Invoice remains open (status: SENT or OVERDUE) until fully paid
- Multiple payments can be recorded against the same invoice
- Balance due is calculated as: totalAmount - amountPaid

### Overpayments and Credit

If a payment exceeds the invoice balance:
- Excess amount is automatically added to customer credit balance
- Credit can be applied to future invoices automatically
- Credit balance is visible in customer details

---

## Refunds

### Issuing Refunds

**Roles**: SysAdmin, Accountant

1. Navigate to **Refunds** → Click **Issue Refund**
2. Select a **Paid Invoice** (only PAID invoices are available)
3. Enter refund details:
   - **Amount** (cannot exceed total amount paid)
   - **Reason** (required, e.g., "Service dispute")
   - **Apply as Credit**: Check if refund should be added to customer credit balance
4. Click **Issue Refund**

**What Happens**:
- Refund is recorded
- If partial refund, invoice status changes from PAID → SENT
- If `applyAsCredit = true`, refund is added to customer credit balance
- An email notification is sent to the customer (if email is configured)

**Business Rules**:
- Refunds can only be issued on PAID invoices
- Refund amount cannot exceed total amount paid
- If partial refund, invoice balance is recalculated

### Viewing Refund History

**Roles**: SysAdmin, Accountant

1. Navigate to **Refunds**
2. View refund list with:
   - Invoice number
   - Customer name
   - Refund amount
   - Reason
   - Date issued
   - Status

---

## Dashboard & Reports

### Dashboard Overview

**Roles**: SysAdmin, Accountant

The dashboard provides an overview of key metrics:

- **Revenue Month-to-Date**: Total revenue for the current month
- **Outstanding Invoices**: Total amount of unpaid invoices
- **Active Customers**: Number of active customers
- **Invoices Sent This Month**: Count of invoices sent
- **Invoices Paid This Month**: Count of invoices paid
- **Average Days to Pay**: Average payment time

### Revenue Trend Chart

**Roles**: SysAdmin, Accountant

1. Navigate to **Dashboard**
2. View the **Revenue Trend** chart showing monthly revenue over time
3. Adjust the time period using the dropdown (default: 12 months)

### Invoice Status Breakdown

**Roles**: SysAdmin, Accountant

1. Navigate to **Dashboard**
2. View the **Invoice Status** breakdown showing:
   - Count and total amount by status (DRAFT, SENT, PAID, OVERDUE, CANCELLED)

### Aging Report

**Roles**: SysAdmin, Accountant

1. Navigate to **Dashboard**
2. View the **Aging Report** showing outstanding invoices grouped by age:
   - **Current**: Not yet due
   - **1-30 Days**: 1-30 days overdue
   - **31-60 Days**: 31-60 days overdue
   - **61-90 Days**: 61-90 days overdue
   - **Over 90 Days**: More than 90 days overdue

---

## Customer Portal

### Accessing the Customer Portal

**Roles**: Customer

1. Log in with your customer account credentials
2. You'll be redirected to the Customer Portal dashboard

**Note**: Customer accounts are automatically created when a customer is added by an admin.

### Viewing Your Invoices

**Roles**: Customer

1. Navigate to **My Invoices**
2. View list of your invoices with:
   - Invoice number
   - Issue date
   - Due date
   - Status (SENT, PAID, OVERDUE)
   - Total amount
   - Balance due

3. Click on an invoice to view details:
   - Line items
   - Totals (subtotal, tax, discount, total)
   - Payment history
   - Download PDF (if available)

### Paying Your Invoices

**Roles**: Customer

1. Navigate to **My Invoices**
2. Click on an unpaid invoice (status: SENT or OVERDUE)
3. Click **Pay Invoice**
4. Enter payment details and submit

**Note**: You can make partial payments. The invoice remains open until fully paid.

### Viewing Your Credit Balance

**Roles**: Customer

1. Navigate to **My Account** or **Dashboard**
2. View your **Credit Balance** (available credit from overpayments or refunds)

**Note**: Credit is automatically applied when invoices are marked as sent.

### Account Summary

**Roles**: Customer

The Customer Portal dashboard shows:
- Total outstanding balance
- Number of unpaid invoices
- Credit balance
- Recent invoices
- Recent payments

---

## Troubleshooting

### Common Issues

**Issue**: Cannot log in  
**Solution**: Ensure your account has been approved by a System Administrator. Check your email and password.

**Issue**: Cannot see invoices  
**Solution**: Ensure invoices have been marked as SENT (DRAFT invoices are not visible to customers).

**Issue**: Cannot record payment  
**Solution**: Ensure invoice status is SENT or OVERDUE (cannot record payment on DRAFT or PAID invoices).

**Issue**: Cannot cancel invoice  
**Solution**: Only DRAFT or SENT invoices can be cancelled. PAID invoices require a refund instead.

**Issue**: Cannot delete customer  
**Solution**: Customers can only be deleted if all invoices are paid and credit balance = $0.

---

## Support

For technical support or questions, contact your System Administrator or Accountant.

---

**Document Version**: 1.0  
**Last Updated**: January 2025

