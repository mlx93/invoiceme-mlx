# Test Execution Status

**Date**: 2025-01-27  
**Status**: ⚠️ **READY FOR EXECUTION** - Prerequisites Check Required

---

## Prerequisites Check

### ✅ Completed
- ✅ Test scripts created (`qa/scripts/test-backend-apis.sh`, `qa/scripts/test-performance.sh`)
- ✅ Test reports created (6 comprehensive reports)
- ✅ QA execution guide created (`QA_EXECUTION_GUIDE.md`)
- ✅ Node.js available (v22.18.0)
- ✅ npm available (v9.8.1)

### ⚠️ Required Before Execution
- ⚠️ **Maven not found** - Need to install Maven or use Maven wrapper
- ⚠️ **Backend not running** - Need to start backend on port 8080
- ⚠️ **Frontend not running** - Need to start frontend on port 3000
- ⚠️ **Database connection** - Need PostgreSQL database accessible
- ⚠️ **Test users** - Need to create test user accounts

---

## Installation Instructions

### Install Maven

**macOS**:
```bash
brew install maven
```

**Linux**:
```bash
sudo apt-get update
sudo apt-get install maven
```

**Verify Installation**:
```bash
mvn --version
```

### Alternative: Use Maven Wrapper

If Maven wrapper exists in backend directory:
```bash
cd backend
./mvnw spring-boot:run
```

---

## Execution Steps

### Step 1: Start Backend

**Terminal 1**:
```bash
cd /Users/mylessjs/Desktop/InvoiceMe/backend

# Option A: If Maven installed
mvn spring-boot:run

# Option B: If Maven wrapper exists
./mvnw spring-boot:run

# Option C: If using IDE
# Run InvoiceMeApplication.java main method
```

**Verify Backend Running**:
```bash
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP"}
```

**Expected Output**:
```
Started InvoiceMeApplication in X.XXX seconds
```

---

### Step 2: Start Frontend

**Terminal 2**:
```bash
cd /Users/mylessjs/Desktop/InvoiceMe/frontend

# Install dependencies (if not already done)
npm install

# Start development server
npm run dev
```

**Verify Frontend Running**:
```bash
curl http://localhost:3000
# Should return HTML content
```

**Expected Output**:
```
  ▲ Next.js 14.x.x
  - Local:        http://localhost:3000
  - Ready in X.XXs
```

---

### Step 3: Create Test Users

Before running tests, create test user accounts:

**Register Test Users** (via API or Frontend):
```bash
# SysAdmin
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sysadmin@test.com",
    "password": "password123",
    "firstName": "SysAdmin",
    "lastName": "Test",
    "role": "SYSADMIN"
  }'

# Accountant
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "accountant@test.com",
    "password": "password123",
    "firstName": "Accountant",
    "lastName": "Test",
    "role": "ACCOUNTANT"
  }'

# Sales
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sales@test.com",
    "password": "password123",
    "firstName": "Sales",
    "lastName": "Test",
    "role": "SALES"
  }'

# Customer
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@test.com",
    "password": "password123",
    "firstName": "Customer",
    "lastName": "Test",
    "role": "CUSTOMER"
  }'
```

**Note**: Users may need approval by SysAdmin before they can login.

---

### Step 4: Execute Test Scripts

**Terminal 3**:
```bash
cd /Users/mylessjs/Desktop/InvoiceMe/qa/scripts

# Make scripts executable
chmod +x test-backend-apis.sh test-performance.sh

# Run backend API tests
./test-backend-apis.sh

# Run performance tests
./test-performance.sh
```

---

### Step 5: Execute Integration Tests

**Terminal 4**:
```bash
cd /Users/mylessjs/Desktop/InvoiceMe/backend

# Run all integration tests
mvn test -Dtest=CustomerPaymentFlowTest,PartialPaymentTest,OverpaymentCreditTest

# Or run all tests
mvn test
```

---

### Step 6: Manual E2E Flow Test

Follow the step-by-step guide in `/qa/results/e2e-flow-evidence.md`:

1. Open browser: `http://localhost:3000`
2. Login as SysAdmin
3. Create Customer
4. Create Invoice
5. Mark Invoice as Sent
6. Record Payment
7. Verify status and events

---

### Step 7: RBAC Verification

Follow the test matrix in `/qa/results/rbac-verification.md`:

1. Login as each role (SysAdmin, Accountant, Sales, Customer)
2. Test each endpoint
3. Verify unauthorized access returns 403
4. Document results

---

### Step 8: Domain Events Verification

Follow procedures in `/qa/results/domain-events-verification.md`:

1. Perform actions that trigger events
2. Query `activity_feed` table
3. Check email service logs
4. Document results

---

## Troubleshooting

### Maven Not Found
- **Solution**: Install Maven (`brew install maven` on macOS)
- **Alternative**: Use Maven wrapper if available (`./mvnw`)

### Backend Won't Start
- **Check**: Database connection (DATABASE_URL environment variable)
- **Check**: Port 8080 not in use (`lsof -i :8080`)
- **Check**: Java 17 installed (`java -version`)

### Frontend Won't Start
- **Check**: Node.js 18+ installed (`node --version`)
- **Check**: Dependencies installed (`npm install`)
- **Check**: Port 3000 not in use (`lsof -i :3000`)

### Tests Failing
- **Check**: Backend running (`curl http://localhost:8080/actuator/health`)
- **Check**: Frontend running (`curl http://localhost:3000`)
- **Check**: Database accessible
- **Check**: Test users created and approved

---

## Next Actions

1. **Install Maven** (if not installed)
2. **Start Backend** (Terminal 1)
3. **Start Frontend** (Terminal 2)
4. **Create Test Users** (via API or Frontend)
5. **Execute Test Scripts** (Terminal 3)
6. **Execute Integration Tests** (Terminal 4)
7. **Perform Manual Tests** (Browser)
8. **Update Test Reports** (Fill in actual results)

---

**Status**: ⚠️ **READY FOR EXECUTION** - Follow steps above to complete testing

