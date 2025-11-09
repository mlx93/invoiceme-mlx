# Production CORS Error - Quick Fix Steps

## Problem
Frontend on AWS Elastic Beanstalk is trying to connect to `localhost:8080` instead of the actual backend URL, causing CORS errors.

## Root Cause
`NEXT_PUBLIC_API_URL` environment variable is not set in the frontend Elastic Beanstalk environment.

## Fix (2 Steps)

### Step 1: Set Frontend Environment Variable ⚠️ REQUIRED

**In AWS Elastic Beanstalk Console:**

1. Go to: [Elastic Beanstalk Console](https://console.aws.amazon.com/elasticbeanstalk)
2. Select: Frontend environment (`invoiceme-mlx-env`)
3. Navigate: **Configuration** → **Software** → **Environment properties** → **Edit**
4. Add new property:
   ```
   Key: NEXT_PUBLIC_API_URL
   Value: http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1
   ```
   ⚠️ **Replace with your actual backend URL** (check your backend Elastic Beanstalk environment)
5. Click: **Apply**
6. Wait: Environment will restart (2-3 minutes)

**How to find your backend URL:**
- Go to Elastic Beanstalk → Backend environment
- Copy the environment URL
- Format: `http://YOUR-BACKEND-URL/api/v1`

---

### Step 2: Update Backend CORS Configuration ⚠️ REQUIRED

**File**: `backend/src/main/java/com/invoiceme/infrastructure/security/SecurityConfig.java`

**Already Updated**: ✅ Backend CORS now allows:
- `http://localhost:3000` (dev)
- `http://invoiceme-mlx-env.eba-dpfprff7.us-east-1.elasticbeanstalk.com` (production)
- `https://invoiceme-mlx-env.eba-dpfprff7.us-east-1.elasticbeanstalk.com` (production HTTPS)

**Action Required**: **Redeploy backend** with this change.

---

## Verification

After both steps:

1. **Wait for frontend restart** (2-3 minutes)
2. **Open frontend URL** in browser
3. **Open DevTools** → **Network** tab
4. **Try to login**
5. **Check requests**:
   - ✅ Should see: `http://invoiceme-mlx-back-env.../api/v1/auth/login`
   - ❌ Should NOT see: `http://localhost:8080/...`

---

## Quick Commands

### Set Environment Variable via EB CLI
```bash
cd frontend
eb setenv NEXT_PUBLIC_API_URL=http://YOUR-BACKEND-URL/api/v1
```

### Set Environment Variable via AWS CLI
```bash
aws elasticbeanstalk update-environment \
  --environment-name invoiceme-mlx-env \
  --option-settings \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=NEXT_PUBLIC_API_URL,Value=http://YOUR-BACKEND-URL/api/v1 \
  --region us-east-1
```

---

## Why This Happens

The frontend code (`frontend/src/lib/api.ts`) has:
```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
```

When `NEXT_PUBLIC_API_URL` is not set, it defaults to `localhost:8080`, which:
- ✅ Works in development (backend runs locally)
- ❌ Fails in production (backend is on AWS, not localhost)

---

## Summary

**Two things needed:**
1. ✅ Set `NEXT_PUBLIC_API_URL` in frontend Elastic Beanstalk environment
2. ✅ Redeploy backend with updated CORS configuration (already done in code)

**After both are done, login should work!** 🎉

