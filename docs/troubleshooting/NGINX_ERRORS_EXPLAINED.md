# Nginx Errors Explained - Backend vs Frontend

## 🔍 Important Clarification

**The nginx errors are from the BACKEND (Elastic Beanstalk), NOT the frontend (Amplify).**

---

## Where the Nginx Errors Come From

### Backend (Elastic Beanstalk) ✅ **This is where your errors are**

- **Location**: Elastic Beanstalk environment (`Invoiceme-mlx-back-env`)
- **Nginx**: Acts as reverse proxy for Spring Boot app
- **Errors**: `502 Bad Gateway`, `Connection refused`
- **Cause**: Spring Boot app not running on port 5000
- **Status**: ❌ **Not working** (database connection issue)

**Your nginx errors**:
```
connect() failed (111: Connection refused) while connecting to upstream: "http://127.0.0.1:5000/"
```

This means:
- ✅ Nginx is running (on Elastic Beanstalk)
- ❌ Spring Boot app is NOT running (can't connect to port 5000)
- ❌ App crashed during startup (database connection failed)

---

### Frontend (Amplify) ❌ **Not deployed yet**

- **Location**: AWS Amplify (not created yet)
- **Status**: ❌ **Not deployed**
- **Reason**: Frontend deployment is **skipped** because backend deployment failed
- **Impact**: **None** - Frontend doesn't affect backend nginx errors

**Why frontend is skipped**:
- GitHub Actions workflow: `needs: deploy-backend`
- If backend fails, frontend step is skipped
- Frontend hasn't been deployed to Amplify yet

---

## The Real Problem

**Your nginx errors are caused by**:

1. ❌ **Spring Boot app not starting** (database connection failed)
2. ❌ **App crashes during Flyway migration** (`Unable to obtain connection from database`)
3. ❌ **Nothing listening on port 5000** (app never started)

**NOT caused by**:
- ✅ Frontend (doesn't exist yet)
- ✅ Amplify (not deployed)
- ✅ Frontend deployment (skipped)

---

## Current Status

### Backend (Elastic Beanstalk)
- ✅ Environment exists: `Invoiceme-mlx-back-env`
- ✅ Nginx running
- ❌ Spring Boot app NOT running (database connection issue)
- ❌ Health check failing (502 Bad Gateway)

### Frontend (Amplify)
- ❌ Not created yet
- ❌ Not deployed
- ⏸️ Deployment skipped (waiting for backend to succeed)

---

## What You Need to Fix

### 1. Fix Backend Database Connection (Priority 1) 🔴

**This is causing your nginx errors**:

1. **Verify environment variables** in Elastic Beanstalk:
   - `DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres`
   - `DB_USERNAME=postgres`
   - `DB_PASSWORD=invoicemesupa`

2. **Restart app server**: Actions → Restart app server

3. **Check logs**: `/var/log/web.stdout.log` for startup success

4. **Test**: `curl http://[backend-url]/actuator/health`

**Once backend is working**, nginx errors will stop.

---

### 2. Deploy Frontend to Amplify (Priority 2) 🟡

**After backend is working**:

1. **Create Amplify App**:
   - AWS Console → Amplify → New App → Host web app
   - Connect GitHub repository: `mlx93/invoiceme-mlx`
   - Branch: `main`
   - Build settings: Use `amplify.yml`

2. **Configure Environment Variables**:
   - `NEXT_PUBLIC_API_URL=http://[backend-url]/api/v1`

3. **Deploy**: Amplify will auto-deploy from GitHub

**Note**: Frontend deployment is separate and doesn't affect backend nginx errors.

---

## Summary

| Component | Status | Impact on Nginx Errors |
|-----------|--------|------------------------|
| **Backend (Elastic Beanstalk)** | ❌ Not working | ✅ **Direct cause** - App not running |
| **Frontend (Amplify)** | ❌ Not deployed | ❌ **No impact** - Separate service |

---

## Action Items

1. ✅ **Fix backend database connection** (this will fix nginx errors)
2. ⏸️ **Deploy frontend to Amplify** (after backend is working)

---

**Bottom Line**: The nginx errors are from your backend (Elastic Beanstalk) because the Spring Boot app isn't starting. The frontend (Amplify) hasn't been deployed yet and doesn't affect these errors.

