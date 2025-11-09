# 🎯 ADMIN LOGIN - QUICK REFERENCE

## ✅ STATUS: WORKING

---

## 📝 Credentials
```
Email:    admin@invoiceme.com
Password: Admin123!
```

---

## 🔗 Endpoints

### Correct URL
```
POST http://localhost:8080/api/v1/auth/login
```

### ❌ Common Mistake
```
POST http://localhost:8080/api/auth/login  ← Missing /v1/
```

---

## 🧪 Test Commands

### Quick Test
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@invoiceme.com","password":"Admin123!"}'
```

### Get Token for API Testing
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@invoiceme.com","password":"Admin123!"}' \
  | jq -r '.token')

echo $TOKEN
```

### Use Token
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/customers
```

---

## 🐛 Troubleshooting

| Issue | HTTP Code | Solution |
|-------|-----------|----------|
| Wrong path | 403 | Add `/v1/` to URL |
| Wrong credentials | 400 | Check email/password |
| Backend old code | 500 | Restart backend |
| Backend not running | Connection refused | Start backend |

---

## 🔄 Restart Backend

```bash
# Find process
ps aux | grep "spring-boot:run" | grep -v grep

# Kill it
kill <PID>

# Start backend
cd /Users/mylessjs/Desktop/InvoiceMe/backend
mvn spring-boot:run
```

---

## ✅ Success Response
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "userId": "0a570c68-3301-413d-9943-88e015d33348",
  "email": "admin@invoiceme.com",
  "role": "SYSADMIN",
  "fullName": "System Administrator"
}
```

---

## 📊 Current Status

- Backend PID: **22385**
- Started: **3:47 PM**
- Code: **Latest** ✅
- Database: **Ready** ✅
- Login: **Working** ✅

---

**Last Tested**: Nov 8, 2025, 3:48 PM  
**Result**: ✅ ALL TESTS PASSING

