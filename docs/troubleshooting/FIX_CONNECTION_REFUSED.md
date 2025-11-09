# Fix: Connection Refused on Port 5000

## Problem

**Nginx Error**: `connect() failed (111: Connection refused) while connecting to upstream: "http://127.0.0.1:5000/"`

**Meaning**: Spring Boot application is **NOT running** or **NOT listening on port 5000**.

---

## Root Cause

The application is either:
1. ❌ **Crashing on startup** (most likely)
2. ❌ **Not starting at all**
3. ❌ **Running on wrong port** (not 5000)
4. ❌ **Missing environment variables** causing startup failure

---

## Step 1: Check Application Logs (CRITICAL)

**This will tell us WHY the app isn't starting:**

1. **Go to**: Elastic Beanstalk Console → Your Environment → **Logs** (left sidebar)
2. **Click**: "Request logs"
3. **Select**:
   - ✅ `/var/log/web.stdout.log` (application output - **MOST IMPORTANT**)
   - ✅ `/var/log/eb-engine.log` (deployment logs)
4. **Select**: "Last 100 lines"
5. **Click**: "Request"

**Look for**:
- ❌ `ERROR` messages
- ❌ `Exception in thread "main"`
- ❌ `Failed to start`
- ❌ `Connection refused` (database)
- ❌ `Port already in use`
- ❌ Any `Exception` or `Error` stack traces

**What to look for**:
- ✅ `Started InvoiceMeApplication` = App started successfully
- ✅ `Tomcat started on port(s): 5000` = App listening on correct port
- ❌ **No startup messages** = App crashed before starting

---

## Step 2: Verify Environment Variables

**Missing environment variables cause startup failures:**

1. **Go to**: Configuration → Software → Environment properties
2. **Verify ALL these are set**:

```
SERVER_PORT=5000
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
JWT_SECRET=lJ5Rz6L8EjrhNazBCeOcu+XCwQmgo+OvkDxafIwDCz8=
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_ACCESS_KEY
AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
SPRING_PROFILES_ACTIVE=production
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_INVOICEME=INFO
```

3. **If any are missing**, add them
4. **After adding**, **restart**: Actions → Restart app server

---

## Step 3: Common Startup Errors & Fixes

### Error 1: Database Connection Failed

**Log shows**: `Connection refused` or `Connection timeout` or `Failed to obtain JDBC Connection`

**Fix**:
1. **Verify DATABASE_URL format** (NO username/password in URL):
   ```
   ✅ CORRECT:
   DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
   
   ❌ WRONG:
   DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=...
   ```

2. **Separate username/password**:
   ```
   DB_USERNAME=postgres
   DB_PASSWORD=invoicemesupa
   ```

3. **Restart**: Actions → Restart app server

---

### Error 2: Missing Environment Variable

**Log shows**: `Could not resolve placeholder 'XXX'` or `Required environment variable 'XXX' is missing`

**Fix**:
1. **Add missing variable** in Configuration → Software → Environment properties
2. **Restart**: Actions → Restart app server

---

### Error 3: Port Already in Use

**Log shows**: `Port 5000 is already in use` or `Address already in use`

**Fix**:
1. **Verify SERVER_PORT=5000** is set
2. **Restart**: Actions → Restart app server (this should kill old process)

---

### Error 4: JAR File Issues

**Log shows**: `no main manifest attribute` or `UnsupportedClassVersionError`

**Fix**:
1. **Verify JAR was built correctly**: `invoiceme-backend-2.0.0.jar`
2. **Rebuild JAR** if needed: `mvn clean package -DskipTests`
3. **Redeploy** the JAR file

---

### Error 5: Application Crashes Immediately

**Log shows**: `Exception in thread "main"` or `Failed to start`

**Fix**:
1. **Check the specific exception** in logs
2. **Common causes**:
   - Database connection failed
   - Missing environment variable
   - Invalid configuration
   - AWS credentials incorrect
3. **Fix the specific error** and restart

---

## Step 4: Restart After Fixes

**After fixing environment variables or configuration**:

1. **Go to**: Actions → **Restart app server**
2. **Wait**: 2-3 minutes for restart
3. **Check logs again**: `/var/log/web.stdout.log`
4. **Look for**: `Started InvoiceMeApplication` (success)

---

## Step 5: Verify Application Started

**Check logs for success messages**:

✅ **Success indicators**:
```
Started InvoiceMeApplication in X.XXX seconds
Tomcat started on port(s): 5000 (http)
```

❌ **Failure indicators**:
```
Exception in thread "main"
Failed to start
Connection refused
```

---

## Quick Test

**After restart, test health endpoint**:

```bash
curl http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/actuator/health
```

**Expected**: `{"status":"UP"}`  
**If still 502**: Check logs again for new errors

---

## Next Steps

1. ✅ **Check `/var/log/web.stdout.log`** - **MOST IMPORTANT**
2. ✅ **Share the error messages** you find
3. ✅ **Fix the specific error** (database, env vars, etc.)
4. ✅ **Restart app server**
5. ✅ **Test health endpoint**

---

**Action**: Check `/var/log/web.stdout.log` and share any ERROR or Exception messages you find. That will tell us exactly why the app isn't starting!

