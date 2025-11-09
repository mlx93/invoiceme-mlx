# Fix 502 Bad Gateway Error

**Error**: `502 Bad Gateway` from nginx  
**Meaning**: Nginx is running, but Spring Boot application is not responding  
**Root Cause**: Application likely failed to start

---

## Step 1: Check Application Logs (CRITICAL)

The logs will show exactly why the application isn't starting:

1. **Go to Elastic Beanstalk** → Environment: `invoiceme-mlx-backend-env-1`
2. **Click "Logs" tab** (left sidebar)
3. **Click "Request logs"** button (top right)
4. **Select**: "Last 100 lines" or "Full logs"
5. **Click "Request"**
6. **Wait 1-2 minutes** for logs to generate
7. **Click "Download"** when ready
8. **Open the log file** and look for:
   - ❌ Application startup errors
   - ❌ Database connection failures
   - ❌ Port binding errors
   - ❌ Missing environment variables
   - ❌ Flyway migration failures
   - ❌ Java exceptions

---

## Step 2: Common Causes of 502 Bad Gateway

### Cause 1: Application Failed to Start

**Symptoms**: Logs show startup errors

**Common Errors**:
- Database connection failed
- Missing environment variable
- Port already in use
- Flyway migration failed

**Solution**: Fix the error shown in logs

### Cause 2: Wrong Port Configuration

**Symptoms**: Application starts but on wrong port

**Check**:
- `SERVER_PORT=5000` must be set
- Application should listen on port 5000 (Elastic Beanstalk requirement)

**Solution**: Verify `SERVER_PORT=5000` is set in environment variables

### Cause 3: Database Connection Failed

**Symptoms**: Logs show "Connection refused" or "Authentication failed"

**Check**:
- `DATABASE_URL` is correct
- Supabase database is accessible
- Password is correct

**Solution**: Verify `DATABASE_URL` value

### Cause 4: Application Crashed After Start

**Symptoms**: Application starts then crashes

**Check logs for**:
- OutOfMemoryError
- NullPointerException
- Configuration errors

---

## Step 3: Verify Environment Variables

1. **Go to Configuration** → **Software** → **Environment properties**
2. **Verify these are set**:
   ```
   DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa
   SERVER_PORT=5000
   SPRING_PROFILES_ACTIVE=production
   JWT_SECRET=lJ5Rz6L8EjrhNazBCeOcu+XCwQmgo+OvkDxafIwDCz8=
   AWS_REGION=us-east-1
   AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY_ID
   AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_ACCESS_KEY
   AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
   AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
   ```

---

## Step 4: Check Events Tab

1. **Go to "Events" tab** (left sidebar)
2. **Look for**:
   - ❌ Red error messages
   - "Failed to deploy"
   - "Application failed to start"

---

## Step 5: Restart After Fixing Issues

Once you've identified and fixed the issue:

1. **Restart app server**:
   - Actions → Restart app server(s)
   - Wait 5-10 minutes

2. **Or redeploy**:
   - Upload and deploy → Select JAR file
   - Wait for deployment

---

## Most Likely Issues

Based on 502 error:

1. **Database connection failed** - Check `DATABASE_URL`
2. **Application startup error** - Check logs for Java exceptions
3. **Port configuration** - Verify `SERVER_PORT=5000`
4. **Missing environment variable** - Check all vars are set

---

## Quick Diagnostic

**502 Bad Gateway** = Nginx can't reach Spring Boot app

**This means**:
- ✅ Nginx is running (that's why you get 502, not connection refused)
- ❌ Spring Boot app is NOT running or NOT listening on port 5000

**Next Step**: **Check logs** - they will show exactly why the app isn't starting!

---

**Action Required**: Request logs from the Logs tab and share the error messages. That will tell us exactly what's wrong!

