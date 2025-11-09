# Admin Login Instructions

## Quick Answer: No Backend Restart Needed! ✅

The admin user was created **directly in the database**, so it's available immediately. The running backend can see it right away.

---

## Admin Credentials

- **Email**: `admin@invoiceme.com`
- **Password**: `Admin123!`
- **Role**: `SYSADMIN`
- **Status**: `ACTIVE` (ready to login)

---

## Steps to Login

1. **Make sure backend is running** (port 8080)
   ```bash
   curl http://localhost:8080/actuator/health
   # Should return: {"status":"UP"}
   ```

2. **Make sure frontend is running** (port 3000)
   - Open http://localhost:3000 in your browser

3. **Go to login page**
   - Navigate to http://localhost:3000/login

4. **Enter credentials**
   - Email: `admin@invoiceme.com`
   - Password: `Admin123!`

5. **Click "Sign in"**

---

## After Login

Once logged in as admin, you can:

1. **Approve pending users**:
   - Go to http://localhost:3000/users/pending
   - Click "Approve" next to any pending user

2. **Access admin features**:
   - Dashboard
   - User management
   - All system features (SYSADMIN has full access)

---

## Verify Admin User Exists

If you want to verify the admin user was created:

```bash
docker exec invoiceme-postgres psql -U postgres -d invoiceme -c \
  "SELECT email, role, status FROM users WHERE email = 'admin@invoiceme.com';"
```

Should return:
```
        email        |   role   | status 
---------------------+----------+--------
 admin@invoiceme.com | SYSADMIN | ACTIVE
```

---

## Troubleshooting

### Can't login?
- Verify backend is running: `curl http://localhost:8080/actuator/health`
- Check admin user exists (see command above)
- Verify status is `ACTIVE` (not `PENDING`)
- Check browser console for errors

### Backend not running?
Start it:
```bash
cd /Users/mylessjs/Desktop/InvoiceMe/backend
mvn spring-boot:run
```

---

**Note**: The admin user is already in the database and ready to use. No restart needed!

