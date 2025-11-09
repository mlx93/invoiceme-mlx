# Health Check Configuration & 502 Error Diagnosis

## Health Check Configuration

### ✅ Automatic Configuration (Already Done)

Your health check is **automatically configured** via `.ebextensions/03-healthcheck.config`:

```yaml
HealthCheckURL: /actuator/health
HealthCheckInterval: 30
SystemType: enhanced
```

This is deployed with your JAR file, so it's already configured.

---

### 🔍 How to Verify/Override in Console

If you want to verify or override the health check in the AWS Console:

1. **Go to your environment**: `invoiceme-mlx-back-env`
2. **Click "Configuration"** (left sidebar)
3. **Find "Load balancer"** section (if load-balanced) OR **"Instance traffic and scaling"** (if single instance)
4. **Click "Edit"**
5. **Look for "Health check path"** field
   - Should be: `/actuator/health`
   - If different, change it to `/actuator/health`
6. **Click "Apply"**

**Note**: For single-instance environments, health check might be under:
- **Configuration** → **Instance traffic and scaling** → **Health check path**

---

## 502 Bad Gateway Error - Diagnosis

**What 502 Means**: Nginx (web server) is running but **cannot connect** to your Spring Boot application.

**Possible Causes**:
1. ❌ Application not starting (crashes on startup)
2. ❌ Application running on wrong port (not 5000)
3. ❌ Missing environment variables (DATABASE_URL, etc.)
4. ❌ Database connection failing
5. ❌ Application listening on wrong interface

---

## Step-by-Step Diagnosis

### Step 1: Check Environment Variables

**Critical variables that MUST be set**:

1. **Go to**: Configuration → Software → Environment properties
2. **Verify these are present**:
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
   ```

3. **If any are missing**, add them and **restart the environment**

---

### Step 2: Check Application Logs

**Most Important**: Check why the app isn't starting.

1. **Go to**: Logs (left sidebar)
2. **Click "Request logs"**
3. **Select**:
   - ✅ `/var/log/web.stdout.log` (application output)
   - ✅ `/var/log/eb-engine.log` (deployment logs)
   - ✅ `/var/log/nginx/error.log` (Nginx errors)
4. **Select**: "Last 100 lines"
5. **Click "Request"**

**Look for**:
- ❌ `ERROR` messages
- ❌ `Exception` or `Exception in thread "main"`
- ❌ `Connection refused` (database)
- ❌ `Port already in use` (port conflict)
- ❌ `Failed to start` messages

---

### Step 3: Check Nginx Error Logs

**Nginx error log** will show connection failures:

1. **Go to**: Logs → Request logs
2. **Select**: `/var/log/nginx/error.log`
3. **Look for**: `connect() failed (111: Connection refused)` or `upstream timed out`

---

### Step 4: Verify Application is Running

**Check if Spring Boot process is running**:

1. **Go to**: Logs → Request logs
2. **Select**: `/var/log/web.stdout.log`
3. **Look for**: 
   - ✅ `Started InvoiceMeApplication` (success)
   - ✅ `Tomcat started on port(s): 5000` (correct port)
   - ❌ No startup messages (app crashed)

---

## Common Fixes

### Fix 1: Missing Environment Variables

**Symptom**: App crashes immediately on startup

**Solution**:
1. Add all environment variables (see Step 1 above)
2. **Restart environment**: Actions → Restart app server

---

### Fix 2: Database Connection Failed

**Symptom**: Logs show `Connection refused` or `Connection timeout`

**Solution**:
1. **Verify DATABASE_URL format**:
   ```
   DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
   ```
   **NOT**:
   ```
   DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=...
   ```
2. **Separate username/password**:
   ```
   DB_USERNAME=postgres
   DB_PASSWORD=invoicemesupa
   ```
3. **Restart**: Actions → Restart app server

---

### Fix 3: Wrong Port

**Symptom**: App starts but Nginx can't connect

**Solution**:
1. **Verify SERVER_PORT=5000** is set
2. **Check logs** for: `Tomcat started on port(s): 5000`
3. **If different port**, fix environment variable and restart

---

### Fix 4: Application Crashes on Startup

**Symptom**: Logs show `Exception in thread "main"` or `Failed to start`

**Solution**:
1. **Check logs** for specific error
2. **Common causes**:
   - Missing environment variable
   - Database connection failed
   - Invalid JWT_SECRET format
   - AWS credentials incorrect
3. **Fix the specific error** and restart

---

## Quick Test Commands

**Test health endpoint directly** (from your local machine):

```bash
curl http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/actuator/health
```

**Expected**: `{"status":"UP"}`  
**If 502**: Application not running or not listening on port 5000

---

## Next Steps

1. ✅ **Check environment variables** (Step 1)
2. ✅ **Check application logs** (Step 2) - **MOST IMPORTANT**
3. ✅ **Share log errors** if you find any
4. ✅ **Restart environment** after fixing issues

---

**Action**: Check the logs first (`/var/log/web.stdout.log`) and share any errors you find!

