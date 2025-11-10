# InvoiceMe API Reference

**Version**: 1.0  
**Base URL**: `https://api.invoiceme.com/api/v1` (Production)  
**Base URL**: `http://localhost:8080/api/v1` (Local Development)

---

## Table of Contents

1. [Authentication](#authentication)
2. [Customer Endpoints](#customer-endpoints)
3. [Invoice Endpoints](#invoice-endpoints)
4. [Payment Endpoints](#payment-endpoints)
5. [Refund Endpoints](#refund-endpoints)
6. [Dashboard Endpoints](#dashboard-endpoints)
7. [User Management Endpoints](#user-management-endpoints)
8. [Error Handling](#error-handling)
9. [Pagination](#pagination)

---

## Authentication

### Login

**Endpoint**: `POST /api/v1/auth/login`

**Description**: Authenticate user and receive JWT token

**Request Body**: `{ "email": "string", "password": "string" }`

**Response** (200 OK): `{ "token": "string", "user": { "id": "uuid", "email": "string", "fullName": "string", "role": "enum" } }`

**Error Responses**: `401 Unauthorized` (invalid credentials), `400 Bad Request` (validation error)

---

### Register

**Endpoint**: `POST /api/v1/auth/register`

**Description**: Register new user (status: PENDING, requires approval)

**Request Body**: `{ "email": "string", "password": "string", "fullName": "string", "role": "enum" }`

**Response** (201 Created): `{ "id": "uuid", "email": "string", "fullName": "string", "role": "enum", "status": "PENDING" }`

**Error Responses**: `400 Bad Request` (validation error), `409 Conflict` (email exists)

---

### Using JWT Tokens

Include JWT token in `Authorization: Bearer <token>` header. Token expires in 24 hours (HS512 algorithm).

**Role-Based Access Control**:
- `SYSADMIN`: Full access
- `ACCOUNTANT`: Customers, invoices, payments, dashboard
- `SALES`: Customers, invoices (read-only payments)
- `CUSTOMER`: Own invoices and payments only

---

## Customer Endpoints

### Create Customer

**Endpoint**: `POST /api/v1/customers`

**Roles**: `SYSADMIN`, `ACCOUNTANT`, `SALES`

**Request Body**: `{ "companyName": "string", "email": "string", "contactName": "string", "phone": "string", "address": {...}, "customerType": "enum" }`

**Response** (201 Created): Customer object with `id`, `companyName`, `email`, `creditBalance`, `status`, `createdAt`

**Error Responses**: `400 Bad Request`, `409 Conflict` (email exists), `401 Unauthorized`, `403 Forbidden`

---

### Get Customer

**Endpoint**: `GET /api/v1/customers/{id}`

**Roles**: All (filtered by ownership for CUSTOMER role)

**Response** (200 OK): Full customer details including address, credit balance, timestamps

**Error Responses**: `404 Not Found`, `401 Unauthorized`, `403 Forbidden`

---

### List Customers

**Endpoint**: `GET /api/v1/customers`

**Roles**: `SYSADMIN`, `ACCOUNTANT`, `SALES`

**Query Parameters**:
- `page` (integer, default: 0): Page number
- `size` (integer, default: 20): Page size (max: 100)
- `sort` (string): Sort field and direction (e.g., "companyName,asc")
- `status` (enum): Filter by status (`ACTIVE`, `INACTIVE`, `SUSPENDED`)
- `customerType` (enum): Filter by type (`RESIDENTIAL`, `COMMERCIAL`, `INSURANCE`)
- `search` (string): Search by company name or email
- `hasOutstandingBalance` (boolean): Filter customers with outstanding balance

**Response** (200 OK): Paginated customer list (Spring Data JPA Page format)

---

### Update Customer

**Endpoint**: `PUT /api/v1/customers/{id}`

**Roles**: `SYSADMIN`, `ACCOUNTANT`

**Request Body**: Same as Create Customer (all fields optional)

**Response** (200 OK): Updated customer object

**Error Responses**: `400 Bad Request`, `404 Not Found`, `401 Unauthorized`, `403 Forbidden`

---

### Delete Customer

**Endpoint**: `DELETE /api/v1/customers/{id}`

**Roles**: `SYSADMIN` only

**Response** (204 No Content): Empty response body

**Error Responses**: `400 Bad Request` (has outstanding balance), `404 Not Found`, `401 Unauthorized`, `403 Forbidden`

---

## Invoice Endpoints

### Create Invoice

**Endpoint**: `POST /api/v1/invoices`

**Roles**: `SYSADMIN`, `ACCOUNTANT`, `SALES`

**Request Body**: `{ "customerId": "uuid", "issueDate": "date", "paymentTerms": "enum", "dueDate": "date" (optional), "lineItems": [{ "description": "string", "quantity": "integer", "unitPrice": "decimal", "discountType": "enum", "discountValue": "decimal", "taxRate": "decimal" }], "notes": "string" (optional) }`

**Response** (201 Created): Invoice object with `id`, `invoiceNumber` (format: INV-YYYY-####), `status` (DRAFT), calculated totals (`subtotal`, `taxAmount`, `discountAmount`, `totalAmount`, `balanceDue`)

**Error Responses**: `400 Bad Request` (validation error), `404 Not Found` (customer not found), `401 Unauthorized`, `403 Forbidden`

---

### Get Invoice

**Endpoint**: `GET /api/v1/invoices/{id}`

**Roles**: All (filtered by ownership for CUSTOMER role)

**Response** (200 OK): Full invoice details including customer info, line items with calculations, payment history, PDF URL

**Error Responses**: `404 Not Found`, `401 Unauthorized`, `403 Forbidden`

---

### List Invoices

**Endpoint**: `GET /api/v1/invoices`

**Roles**: All (filtered by ownership for CUSTOMER role)

**Query Parameters**:
- `page`, `size`, `sort`: Pagination parameters
- `status` (enum): Filter by status (`DRAFT`, `SENT`, `PAID`, `OVERDUE`, `CANCELLED`)
- `customerId` (uuid): Filter by customer
- `search` (string): Search by invoice number or customer name
- `dueDateFrom`, `dueDateTo` (date): Date range filter

**Response** (200 OK): Paginated invoice list

---

### Update Invoice

**Endpoint**: `PUT /api/v1/invoices/{id}`

**Roles**: `SYSADMIN`, `ACCOUNTANT`, `SALES`

**Request Body**: Same as Create Invoice (all fields optional except lineItems)

**Response** (200 OK): Updated invoice object

**Error Responses**: `400 Bad Request` (status is PAID or CANCELLED), `409 Conflict` (optimistic locking), `404 Not Found`

---

### Mark Invoice as Sent

**Endpoint**: `PATCH /api/v1/invoices/{id}/mark-as-sent`

**Roles**: `SYSADMIN`, `ACCOUNTANT`, `SALES`

**Response** (200 OK): Updated invoice with `status` = SENT

**Error Responses**: `400 Bad Request` (status not DRAFT, or no line items), `404 Not Found`

---

### Cancel Invoice

**Endpoint**: `DELETE /api/v1/invoices/{id}`

**Roles**: `SYSADMIN` only

**Response** (204 No Content): Empty response body

**Error Responses**: `400 Bad Request` (status is PAID, or payments applied), `404 Not Found`

---

## Payment Endpoints

### Record Payment

**Endpoint**: `POST /api/v1/payments`

**Roles**: `SYSADMIN`, `ACCOUNTANT`, `CUSTOMER` (own invoices only)

**Request Body**: `{ "invoiceId": "uuid", "amount": "decimal", "paymentMethod": "enum" (CREDIT_CARD or ACH), "paymentDate": "date", "paymentReference": "string" (optional), "notes": "string" (optional) }`

**Response** (201 Created): Payment object with `id`, `invoiceId`, `invoiceNumber`, `customerId`, `customerName`, `amount`, `paymentMethod`, `paymentDate`, `status` (COMPLETED)

**Error Responses**: `400 Bad Request` (invalid invoice status, amount <= 0), `404 Not Found`, `401 Unauthorized`, `403 Forbidden`

**Business Rules**:
- Payment only allowed if invoice status is SENT or OVERDUE
- If payment > invoice balance, excess goes to customer credit balance
- If payment = invoice balance, invoice status changes to PAID

---

### Get Payment

**Endpoint**: `GET /api/v1/payments/{id}`

**Roles**: `SYSADMIN`, `ACCOUNTANT`

**Response** (200 OK): Payment details

---

### List Payments

**Endpoint**: `GET /api/v1/payments`

**Roles**: `SYSADMIN`, `ACCOUNTANT`

**Query Parameters**:
- `page`, `size`, `sort`: Pagination parameters
- `invoiceId` (uuid): Filter by invoice
- `customerId` (uuid): Filter by customer
- `paymentMethod` (enum): Filter by method (`CREDIT_CARD`, `ACH`)
- `status` (enum): Filter by status (`PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`)
- `paymentDateFrom`, `paymentDateTo` (date): Date range filter

**Response** (200 OK): Paginated payment list

---

## Refund Endpoints

### Issue Refund

**Endpoint**: `POST /api/v1/refunds`

**Roles**: `SYSADMIN`, `ACCOUNTANT`

**Request Body**: `{ "invoiceId": "uuid", "amount": "decimal", "reason": "string", "applyAsCredit": "boolean" }`

**Response** (201 Created): Refund object with `id`, `invoiceId`, `invoiceNumber`, `customerId`, `amount`, `reason`, `status` (COMPLETED)

**Error Responses**: `400 Bad Request` (invoice not paid, refund > amount paid), `404 Not Found`

**Business Rules**:
- Refund only allowed on PAID invoices
- Refund amount cannot exceed total amount paid
- If partial refund, invoice status changes from PAID → SENT
- If `applyAsCredit = true`, refund added to customer credit balance

---

## Dashboard Endpoints

### Get Dashboard Metrics

**Endpoint**: `GET /api/v1/dashboard/metrics`

**Roles**: `SYSADMIN`, `ACCOUNTANT`

**Response** (200 OK): `{ "revenueMonthToDate": "decimal", "outstandingInvoices": "decimal", "activeCustomers": "integer", "invoicesSentThisMonth": "integer", "invoicesPaidThisMonth": "integer", "averageDaysToPay": "integer" }`

---

### Get Revenue Trend

**Endpoint**: `GET /api/v1/dashboard/revenue-trend`

**Roles**: `SYSADMIN`, `ACCOUNTANT`

**Query Parameters**: `months` (integer, default: 12)

**Response** (200 OK): `{ "data": [{ "month": "string", "revenue": "decimal", "invoiceCount": "integer" }] }`

---

### Get Invoice Status Breakdown

**Endpoint**: `GET /api/v1/dashboard/invoice-status`

**Roles**: `SYSADMIN`, `ACCOUNTANT`

**Response** (200 OK): `{ "draft": { "count": "integer", "totalAmount": "decimal" }, "sent": {...}, "paid": {...}, "overdue": {...}, "cancelled": {...} }`

---

### Get Aging Report

**Endpoint**: `GET /api/v1/dashboard/aging-report`

**Roles**: `SYSADMIN`, `ACCOUNTANT`

**Response** (200 OK): `{ "current": { "count": "integer", "totalAmount": "decimal" }, "days30": {...}, "days60": {...}, "days90": {...}, "over90": {...} }`

---

## User Management Endpoints

### List Pending Users

**Endpoint**: `GET /api/v1/users/pending`

**Roles**: `SYSADMIN` only

**Response** (200 OK): `{ "users": [{ "id": "uuid", "email": "string", "fullName": "string", "role": "enum", "status": "PENDING", "createdAt": "datetime" }] }`

---

### Approve User

**Endpoint**: `PATCH /api/v1/users/{id}/approve`

**Roles**: `SYSADMIN` only

**Response** (200 OK): User object with `status` = ACTIVE

---

### Reject User

**Endpoint**: `PATCH /api/v1/users/{id}/reject`

**Roles**: `SYSADMIN` only

**Response** (204 No Content): Empty response body

---

## Error Handling

All error responses follow **RFC 7807 Problem Details** format:

```json
{
  "type": "https://invoiceme.com/errors/invalid-request",
  "title": "Invalid Request",
  "status": 400,
  "detail": "Invoice cannot be marked as sent: status is not DRAFT",
  "instance": "/api/v1/invoices/123/mark-as-sent",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

**Common HTTP Status Codes**:
- `200 OK`: Successful GET/PUT/PATCH
- `201 Created`: Successful POST
- `204 No Content`: Successful DELETE
- `400 Bad Request`: Validation error or business rule violation
- `401 Unauthorized`: Invalid or missing JWT token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict (optimistic locking, duplicate email)
- `500 Internal Server Error`: Server error

---

## Pagination

All list endpoints support pagination using Spring Data JPA `Page<T>` format:

**Query Parameters**:
- `page` (integer, default: 0): Page number (0-indexed)
- `size` (integer, default: 20): Page size (max: 100)
- `sort` (string, default: "createdAt,desc"): Sort field and direction (comma-separated)

**Response Format**:
```json
{
  "content": [...],
  "pageable": { "pageNumber": 0, "pageSize": 20, "sort": {...} },
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true,
  "numberOfElements": 20,
  "size": 20,
  "number": 0,
  "empty": false
}
```

---

**Document Version**: 1.0  
**Last Updated**: January 2025
