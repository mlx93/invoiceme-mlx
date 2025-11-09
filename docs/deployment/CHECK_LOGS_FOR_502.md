# Check Logs to Fix 502 Bad Gateway

**Status**: Still getting 502 Bad Gateway  
**Meaning**: Application is not starting  
**Next Step**: Check logs to find the exact error

---

## Step 1: Request Logs

1. **Go to Elastic Beanstalk** → Environment: `invoiceme-mlx-backend-env-1`
2. **Click "Logs" tab** (left sidebar)
3. **Click "Request logs" button** (top right, blue button)
4. **Select**: "Last 100 lines" (start with this)
5. **Click "Request"**
6. **Wait 1-2 minutes** for logs to generate
7. **Click "Download"** when the button appears
8. **Open the downloaded log file**

---

## Step 2: Look for These Errors

In the log file, search for:

### Database Connection Errors
- `Connection refused`
- `Authentication failed`
- `FATAL: password authentication failed`
- `org.postgresql.util.PSQLException`

### Port Errors
- `Address already in use`
- `BindException`
- `Port 5000`

### Missing Environment Variables
- `null`
- `Required property 'DATABASE_URL' not found`
- Configuration errors

### Application Startup Errors
- `Exception in thread "main"`
- `Caused by:`
- `Failed to start`
- `BeanCreationException`

### Flyway Migration Errors
- `Migration failed`
- `FlywayException`
- SQL errors

---

## Step 3: Common Issues Based on Logs

### If You See Database Connection Errors

**Error**: `Connection refused` or `Authentication failed`

**Fix**:
1. **Verify DATABASE_URL**:
   - Should be: `jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres`
   - **Remove** `?user=postgres&password=...` from URL
   - Keep `DB_USERNAME=postgres` and `DB_PASSWORD=invoicemesupa` separate

2. **Test Supabase connection**:
   - Verify database is accessible
   - Check password is correct

### If You See Port Errors

**Error**: `Address already in use` or port binding failed

**Fix**:
1. **Verify SERVER_PORT=5000** is set
2. **Check** no other process is using port 5000

### If You See Missing Environment Variables

**Error**: `null` or configuration errors

**Fix**:
1. **Go to Configuration** → **Software** → **Environment properties**
2. **Verify all variables are set**:
   - `DATABASE_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `SERVER_PORT=5000`
   - `SPRING_PROFILES_ACTIVE=production`

### If You See Flyway Migration Errors

**Error**: Migration failed

**Fix**:
1. **Check database schema** matches migrations
2. **Verify database is accessible**
3. **Check migration files** are correct

---

## Step 4: Share the Error

After checking logs, **share the error message** you see. Common patterns:

- **Last few lines** of the log file
- **Any lines with "ERROR"** or "Exception"
- **The final error** before the application stops

---

## Quick Test: Check if App Started at All

Look in logs for:
- `Started InvoiceMeApplication` (means app started successfully)
- `Application run failed` (means app failed to start)
- `Tomcat started on port(s): 5000` (means app is listening)

---

**Next Step**: Request logs and share the error messages you see. That will tell us exactly what's wrong!

