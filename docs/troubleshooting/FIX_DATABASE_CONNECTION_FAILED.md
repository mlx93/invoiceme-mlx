# Fix: Database Connection Failed

## Problem Identified ✅

**Error**: `Unable to obtain connection from database: The connection attempt failed.`

**Root Cause**: Spring Boot cannot connect to your Supabase PostgreSQL database during Flyway migration initialization.

---

## Solution: Fix Database Connection Configuration

### Step 1: Verify Environment Variables Format

**The DATABASE_URL must NOT include username/password** - they must be separate variables.

**✅ CORRECT Format**:

```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
```

**❌ WRONG Format** (will fail):

```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa
```

---

### Step 2: Update Environment Variables in Elastic Beanstalk

1. **Go to**: Elastic Beanstalk Console → Your Environment (`invoiceme-mlx-back-env`)
2. **Click**: **Configuration** (left sidebar)
3. **Click**: **Software** → **Edit**
4. **Scroll to**: **Environment properties**

5. **Verify/Set these EXACT values**:

```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
```

**Important**:
- ✅ **DATABASE_URL**: NO `?user=` or `&password=` parameters
- ✅ **DB_USERNAME**: Separate variable
- ✅ **DB_PASSWORD**: Separate variable

6. **Also verify these are set**:

```
SERVER_PORT=5000
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

7. **Click**: **Apply** (bottom right)
8. **Wait**: 2-3 minutes for configuration to apply

---

### Step 3: Restart Application Server

**After updating environment variables**:

1. **Go to**: **Actions** → **Restart app server**
2. **Wait**: 2-3 minutes for restart
3. **Check logs**: Logs → Request logs → `/var/log/web.stdout.log`
4. **Look for**: `Started InvoiceMeApplication` (success)

---

## Why This Happens

**Spring Boot expects separate variables**:

Your `application.yml` uses:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://localhost:5432/invoiceme}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
```

If `DATABASE_URL` contains `?user=...&password=...`, Spring Boot will:
1. Use the URL with embedded credentials
2. Also try to use `DB_USERNAME` and `DB_PASSWORD`
3. This causes conflicts and connection failures

**Solution**: Keep URL clean, use separate username/password variables.

---

## Verify Connection After Fix

**After restart, check logs for**:

✅ **Success**:
```
Flyway migration successful
Started InvoiceMeApplication
Tomcat started on port(s): 5000
```

❌ **Still failing**:
- Check if DATABASE_URL format is correct (no `?user=` or `&password=`)
- Verify DB_USERNAME and DB_PASSWORD are set
- Check Supabase database is accessible (not paused/stopped)

---

## Test Health Endpoint

**After restart, test**:

```bash
curl http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/actuator/health
```

**Expected**: `{"status":"UP"}`

---

## Quick Checklist

- [ ] DATABASE_URL has NO `?user=` or `&password=` parameters
- [ ] DB_USERNAME is set separately
- [ ] DB_PASSWORD is set separately
- [ ] All environment variables are set
- [ ] Clicked "Apply" after updating
- [ ] Restarted app server
- [ ] Checked logs for "Started InvoiceMeApplication"

---

**Action**: Update the DATABASE_URL format (remove username/password if present) and ensure DB_USERNAME/DB_PASSWORD are set separately, then restart!

