# Fix Degraded Health Status

**Problem**: Environment shows "Degraded" / "Severe" - "Following services are not running: web"  
**Meaning**: Spring Boot application is not starting properly

---

## Step 1: Check Application Logs

The logs will tell us exactly what's wrong:

1. **Go to "Logs" tab** (left sidebar)
2. **Click "Request logs"** button (top right)
3. **Select**: "Last 100 lines" or "Full logs"
4. **Click "Request"**
5. **Wait 1-2 minutes** for logs to generate
6. **Click "Download"** when ready
7. **Open the log file** and look for:
   - ❌ Application startup errors
   - ❌ Database connection errors
   - ❌ Port conflicts
   - ❌ Missing environment variables
   - ❌ ClassNotFoundException or other Java errors

---

## Step 2: Common Issues & Solutions

### Issue 1: Database Connection Failed

**Symptoms**: Logs show "Connection refused" or "Authentication failed"

**Solution**:
1. **Verify DATABASE_URL** is correct:
   - Go to Configuration → Software → Environment properties
   - Check `DATABASE_URL` value
   - Should be: `jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa`

2. **Test database connection**:
   - Verify Supabase database is accessible
   - Check if password is correct

### Issue 2: Port Conflict

**Symptoms**: Logs show "Address already in use" or port binding errors

**Solution**:
1. **Verify SERVER_PORT=5000** is set:
   - Configuration → Software → Environment properties
   - Should have: `SERVER_PORT=5000`

### Issue 3: Missing Environment Variables

**Symptoms**: Logs show "null" values or missing configuration

**Solution**:
1. **Verify all required variables are set**:
   - `DATABASE_URL`
   - `JWT_SECRET`
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`
   - `SERVER_PORT=5000`
   - `SPRING_PROFILES_ACTIVE=production`

### Issue 4: Application Startup Error

**Symptoms**: Logs show Java exceptions or Spring Boot errors

**Solution**:
1. **Check for**:
   - Missing dependencies
   - Configuration errors
   - Flyway migration failures
   - Bean creation errors

---

## Step 3: Check Events Tab

1. **Go to "Events" tab** (left sidebar)
2. **Look for**:
   - ❌ Red error messages
   - ⚠️ Yellow warnings
   - "Failed to deploy" messages

---

## Step 4: Verify Environment Variables

1. **Go to Configuration** → **Software** → **Environment properties**
2. **Verify these are set**:
   ```
   DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa
   DB_USERNAME=postgres
   DB_PASSWORD=invoicemesupa
   JWT_SECRET=lJ5Rz6L8EjrhNazBCeOcu+XCwQmgo+OvkDxafIwDCz8=
   AWS_REGION=us-east-1
   AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY_ID
   AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_ACCESS_KEY
   AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
   AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
   SERVER_PORT=5000
   SPRING_PROFILES_ACTIVE=production
   ```

---

## Step 5: Restart Environment (If Needed)

If logs show the application crashed:

1. **Go to "Actions" dropdown** (top right)
2. **Select "Restart app server(s)"**
3. **Wait 5-10 minutes** for restart
4. **Check health status** again

---

## Quick Diagnostic Steps

### 1. Check Logs First
- **Most important**: Download and review application logs
- This will show the exact error

### 2. Verify Environment Variables
- Make sure all are set correctly
- Especially `DATABASE_URL` and `SERVER_PORT`

### 3. Check Events
- Look for deployment or startup errors

### 4. Test Database Connection
- Verify Supabase database is accessible
- Check if password is correct

---

## Most Likely Issues

Based on "web service not running":

1. **Application failed to start** - Check logs for startup errors
2. **Database connection failed** - Verify DATABASE_URL
3. **Port conflict** - Verify SERVER_PORT=5000
4. **Missing environment variable** - Check all vars are set

---

**Next Step**: **Request logs** and share the error messages you see. That will tell us exactly what's wrong!

