# Verify Database Connection Locally

## Problem Found ✅

Your `ELASTIC_BEANSTALK_ENV_VALUES.txt` file has the **WRONG format**:

**❌ WRONG** (line 8):
```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa
```

**✅ CORRECT** (fixed):
```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
```

---

## Test Database Connection Locally

### Option 1: Quick Test with psql (if installed)

```bash
# Test connection
PGPASSWORD=invoicemesupa psql -h db.rhyariaxwllotjiuchhz.supabase.co -p 5432 -U postgres -d postgres -c "SELECT version();"
```

**Expected**: Shows PostgreSQL version

---

### Option 2: Test with Spring Boot App

**Test locally with correct environment variables**:

```bash
cd backend

# Set environment variables (correct format)
export DATABASE_URL="jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres"
export DB_USERNAME="postgres"
export DB_PASSWORD="invoicemesupa"
export SERVER_PORT=8080
export SPRING_PROFILES_ACTIVE=dev

# Run the app (will test DB connection)
mvn spring-boot:run
```

**Look for**:
- ✅ `Started InvoiceMeApplication` = Connection successful
- ❌ `Unable to obtain connection` = Connection failed

---

### Option 3: Use Test Script

```bash
# Run the test script
./test-db-connection.sh
```

This will:
1. Test with psql (if available)
2. Show correct vs wrong format
3. Test with Spring Boot JAR

---

## Correct Environment Variables for Elastic Beanstalk

**Copy these EXACT values** into Elastic Beanstalk:

```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
SERVER_PORT=5000
SPRING_PROFILES_ACTIVE=production
JWT_SECRET=lJ5Rz6L8EjrhNazBCeOcu+XCwQmgo+OvkDxafIwDCz8=
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_ACCESS_KEY
AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_INVOICEME=INFO
```

**⚠️ CRITICAL**: 
- DATABASE_URL has **NO** `?user=` or `&password=` parameters
- DB_USERNAME and DB_PASSWORD are **separate** variables

---

## Why This Matters

**Spring Boot's `application.yml` expects**:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}        # Just the URL, no credentials
    username: ${DB_USERNAME}     # Separate variable
    password: ${DB_PASSWORD}     # Separate variable
```

If DATABASE_URL contains `?user=...&password=...`, Spring Boot will:
1. Try to use the URL with embedded credentials
2. Also try to use DB_USERNAME and DB_PASSWORD
3. This causes conflicts and connection failures

---

## Next Steps

1. ✅ **Test locally** using Option 2 above
2. ✅ **Update Elastic Beanstalk** with correct DATABASE_URL format
3. ✅ **Restart** the environment
4. ✅ **Check logs** for "Started InvoiceMeApplication"

---

**Action**: Test the connection locally first, then update Elastic Beanstalk with the corrected DATABASE_URL format!

