# Auto-Create Customer User Account Feature

## Overview

When an admin or accountant creates a customer, a user account is automatically created for that customer with:
- **Status**: ACTIVE (auto-approved, no approval needed)
- **Password**: `test1234` (hashed using bcrypt)
- **Role**: CUSTOMER
- **Email**: Customer's email address
- **Full Name**: Customer's contact name (if provided) or company name
- **Customer ID**: Linked to the created customer

## Implementation Details

### Changes Made

1. **User Entity** (`backend/src/main/java/com/invoiceme/infrastructure/persistence/User.java`)
   - Added `createActive()` factory method for creating active users directly
   - Method signature: `createActive(String email, String passwordHash, String fullName, UserRole role, UUID customerId)`

2. **CreateCustomerHandler** (`backend/src/main/java/com/invoiceme/customers/createcustomer/CreateCustomerHandler.java`)
   - Added `UserRepository` and `PasswordEncoder` dependencies
   - After saving customer, automatically creates user account if:
     - Customer has an email address
     - User with that email doesn't already exist
   - Uses customer's contact name (if available) or company name as full name
   - Sets password to "test1234" (hashed)
   - Links user to customer via `customerId`

## Usage

### Creating a Customer (Admin/Accountant)

When an admin or accountant creates a customer via:
- **API**: `POST /api/v1/customers`
- **Frontend**: `/customers/new` page

The system will:
1. Create the customer entity
2. Automatically create a user account for that customer
3. User account is immediately active (no approval needed)

### Customer Login

The customer can immediately log in with:
- **Email**: The email address used when creating the customer
- **Password**: `test1234`

**Example**:
```
Email: john@riversideapts.com
Password: test1234
```

## Business Logic

### User Account Creation Rules

1. **Only if email provided**: User account is only created if the customer has an email address
2. **Skip if exists**: If a user with that email already exists, no new account is created (prevents duplicates)
3. **Full name priority**: 
   - Uses `contactName` if provided and not empty
   - Falls back to `companyName` if contact name is not available
4. **Always active**: User account is created with `ACTIVE` status (no approval workflow)

### Transaction Safety

The user account creation happens within the same transaction as customer creation:
- If customer creation fails, user account is not created
- If user account creation fails, customer creation is rolled back
- Ensures data consistency

## Security Considerations

⚠️ **Default Password**: The default password `test1234` is intended for initial setup. In production:
- Customers should be encouraged to change their password after first login
- Consider implementing a password reset flow
- Consider requiring password change on first login

## Testing

### Test Scenario 1: Create Customer with Email
```bash
POST /api/v1/customers
{
  "companyName": "Riverside Apartments LLC",
  "contactName": "John Doe",
  "email": "john@riversideapts.com",
  "phone": "555-1234",
  "customerType": "COMMERCIAL"
}
```

**Expected Result**:
- Customer created successfully
- User account created with:
  - Email: `john@riversideapts.com`
  - Password: `test1234` (hashed)
  - Full Name: `John Doe`
  - Status: `ACTIVE`
  - Role: `CUSTOMER`
  - Customer ID: Linked to created customer

### Test Scenario 2: Create Customer without Email
```bash
POST /api/v1/customers
{
  "companyName": "ABC Corp",
  "customerType": "COMMERCIAL"
  // No email provided
}
```

**Expected Result**:
- Customer created successfully
- No user account created (no email provided)

### Test Scenario 3: Create Customer with Existing Email
```bash
# First request creates customer and user
POST /api/v1/customers
{
  "companyName": "Existing Corp",
  "email": "existing@example.com",
  "customerType": "COMMERCIAL"
}

# Second request with same email
POST /api/v1/customers
{
  "companyName": "New Corp",
  "email": "existing@example.com",  // Same email
  "customerType": "COMMERCIAL"
}
```

**Expected Result**:
- First request: Customer and user created successfully
- Second request: Fails with "Customer with email already exists" error (customer email uniqueness constraint)

## Code References

- **User Entity**: `backend/src/main/java/com/invoiceme/infrastructure/persistence/User.java`
- **CreateCustomerHandler**: `backend/src/main/java/com/invoiceme/customers/createcustomer/CreateCustomerHandler.java`
- **UserRepository**: `backend/src/main/java/com/invoiceme/infrastructure/persistence/UserRepository.java`

## Future Enhancements

1. **Password Reset Flow**: Implement password reset so customers can change their default password
2. **Email Notification**: Send welcome email to customer with login credentials
3. **Password Policy**: Enforce password change on first login
4. **Configurable Default Password**: Make default password configurable via environment variable
5. **Optional User Creation**: Add flag to optionally skip user account creation

