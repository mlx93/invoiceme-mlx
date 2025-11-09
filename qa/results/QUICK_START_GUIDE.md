# Quick Start Guide - Backend & Frontend

## Starting the Applications

### 1. Start Database
```bash
cd /Users/mylessjs/Desktop/InvoiceMe
docker-compose up -d postgres
```

### 2. Start Backend (Terminal 1)
```bash
cd /Users/mylessjs/Desktop/InvoiceMe/backend
mvn spring-boot:run
```

**Wait for**: `Started InvoiceMeApplication in X.XXX seconds`

**Verify**: 
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

### 3. Start Frontend (Terminal 2)
```bash
cd /Users/mylessjs/Desktop/InvoiceMe/frontend
npm run dev
```

**Verify**: Open http://localhost:3000 in browser

---

## Recent Fixes Applied

### ✅ CORS Configuration
- Backend now allows requests from `http://localhost:3000`
- Configured in `SecurityConfig.java`

### ✅ Registration Endpoint
- Now accepts `fullName` field (matches frontend)
- Splits fullName into firstName/lastName internally
- Backward compatible with firstName/lastName

### ✅ PostgreSQL Enum Handling
- Created AttributeConverters for UserRole and UserStatus
- Combined with ColumnTransformer for proper enum read/write
- Should fix login endpoint enum reading issues

---

## Testing Registration

### From Frontend
1. Navigate to http://localhost:3000/register
2. Fill in:
   - Full Name: "John Doe"
   - Email: "test@example.com"
   - Password: "password123"
   - Role: Select a role
3. Click Register
4. Should redirect to login page

### From Command Line
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -H "Origin: http://localhost:3000" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "fullName": "John Doe"
  }'
```

**Expected Response**: HTTP 201 Created (no body)

---

## Testing Login

**Note**: Users are created with `PENDING` status. To test login:

1. Register a user
2. Approve the user (set status to ACTIVE):
```bash
docker exec invoiceme-postgres psql -U postgres -d invoiceme -c \
  "UPDATE users SET status = 'ACTIVE' WHERE email = 'test@example.com';"
```

3. Login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Expected Response**: JSON with `token`, `userId`, `email`, `role`, `fullName`

---

## Troubleshooting

### Backend won't start
- Check if port 8080 is in use: `lsof -i :8080`
- Check PostgreSQL is running: `docker ps | grep invoiceme-postgres`
- Check logs: `tail -f /tmp/backend_*.log` or console output

### CORS errors
- Verify CORS is configured in `SecurityConfig.java`
- Check backend is running on port 8080
- Check frontend is running on port 3000

### Registration fails
- Check backend logs for validation errors
- Verify `fullName` field is being sent (not `firstName`/`lastName`)
- Check email is unique

### Login fails
- Verify user status is ACTIVE (not PENDING)
- Check enum converters are working (check backend logs)
- Verify password matches

---

## Key Files Modified

- `backend/src/main/java/com/invoiceme/infrastructure/security/SecurityConfig.java` - CORS
- `backend/src/main/java/com/invoiceme/auth/register/RegisterRequest.java` - fullName support
- `backend/src/main/java/com/invoiceme/auth/register/RegisterHandler.java` - fullName splitting
- `backend/src/main/java/com/invoiceme/infrastructure/persistence/User.java` - enum converters
- `backend/src/main/java/com/invoiceme/infrastructure/persistence/UserRoleConverter.java` - NEW
- `backend/src/main/java/com/invoiceme/infrastructure/persistence/UserStatusConverter.java` - NEW

---

**Status**: ✅ Ready for testing!

