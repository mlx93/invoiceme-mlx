# Admin User Setup Guide

## Default Admin Credentials

**Email**: `admin@invoiceme.com`  
**Password**: `Admin123!`  
**Role**: `SYSADMIN`  
**Status**: `ACTIVE` (can login immediately)

---

## How to Approve Users

### Option 1: Via Frontend UI (Recommended)

1. **Login as Admin**:
   - Go to http://localhost:3000/login
   - Email: `admin@invoiceme.com`
   - Password: `Admin123!`

2. **Navigate to Pending Users**:
   - After login, go to http://localhost:3000/users/pending
   - Or click "Pending Users" in the navigation menu (if available)

3. **Approve Users**:
   - You'll see a table of all pending user registrations
   - Click "Approve" button next to the user you want to approve
   - Confirm the approval in the dialog
   - User will be activated and can now login

### Option 2: Via API (Direct)

**Approve a user by email**:
```bash
# First, get the user ID
USER_ID=$(docker exec invoiceme-postgres psql -U postgres -d invoiceme -t -c \
  "SELECT id FROM users WHERE email = 'user@example.com';" | tr -d ' ')

# Then approve via API (requires admin token)
curl -X POST http://localhost:8080/api/v1/users/$USER_ID/approve \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

### Option 3: Via Database (Quick Test)

**Direct database update**:
```bash
docker exec invoiceme-postgres psql -U postgres -d invoiceme -c \
  "UPDATE users SET status = 'ACTIVE' WHERE email = 'user@example.com';"
```

---

## Creating Additional Admin Users

### Via Registration + Manual Approval

1. Register a new user with role `SYSADMIN` via frontend
2. Login as existing admin
3. Approve the new admin user from pending users page

### Via Database Direct Insert

```bash
docker exec invoiceme-postgres psql -U postgres -d invoiceme -c "
INSERT INTO users (id, email, password_hash, full_name, role, status, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'newadmin@example.com',
    '\$2a\$10\$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'New Admin',
    'SYSADMIN',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
"
```

**Note**: The password hash above is for `Admin123!`. To create a different password, you'll need to generate a BCrypt hash.

---

## Roles That Can Approve Users

Based on RBAC configuration:
- **SYSADMIN**: Can approve all users
- **ACCOUNTANT**: Can approve users (check `canApproveUsers` function)

---

## Security Note

⚠️ **IMPORTANT**: Change the default admin password immediately after first login!

The default password `Admin123!` is publicly documented and should not be used in production.

---

## Troubleshooting

### Can't login as admin
- Verify user exists: `docker exec invoiceme-postgres psql -U postgres -d invoiceme -c "SELECT email, role, status FROM users WHERE email = 'admin@invoiceme.com';"`
- Check status is `ACTIVE` (not `PENDING`)
- Verify password hash is correct

### Can't access pending users page
- Verify you're logged in
- Check your role is `SYSADMIN` or `ACCOUNTANT`
- Check RBAC permissions in `frontend/src/lib/rbac.ts`

### Users not showing in pending list
- Verify users have `PENDING` status
- Check API endpoint `/api/v1/users/pending` returns data
- Check browser console for API errors

