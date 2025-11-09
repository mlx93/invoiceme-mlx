# Frontend Deployment to AWS Amplify - Step-by-Step Guide

## 📋 Prerequisites

✅ **Backend Deployed**: http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com
✅ **Backend Health Check**: `{"status":"UP"}`
✅ **GitHub Repository**: Code pushed to `main` branch
✅ **AWS Account**: Access to AWS Amplify

---

## 🚀 Step 1: Push Latest Changes to GitHub

**Make sure your code is pushed to GitHub**:

```bash
# Check current status
git status

# Add any uncommitted changes
git add .

# Commit changes
git commit -m "Configure for AWS deployment"

# Push to main branch
git push origin main
```

---

## 🌐 Step 2: Create AWS Amplify App

### Option A: AWS Console (Recommended)

1. **Go to**: https://console.aws.amazon.com/amplify/home?region=us-east-1
2. **Click**: "Create new app" → "Host web app"
3. **Choose**: GitHub
4. **Authenticate**: Connect your GitHub account (if not already)
5. **Select**:
   - Repository: `InvoiceMe` (or your repo name)
   - Branch: `main`
6. **Click**: "Next"

### Configure Build Settings

**App name**: `invoiceme-frontend` (or your preferred name)

**Build settings** - Amplify should auto-detect Next.js. Verify the following:

```yaml
version: 1
frontend:
  phases:
    preBuild:
      commands:
        - cd frontend
        - nvm use 20
        - npm ci
    build:
      commands:
        - npm run build
  artifacts:
    baseDirectory: frontend/.next
    files:
      - '**/*'
  cache:
    paths:
      - frontend/node_modules/**/*
      - frontend/.next/cache/**/*
```

**If Amplify doesn't detect correctly**, you can:
1. Click "Edit" on build settings
2. Paste the above YAML
3. **Important**: Make sure `baseDirectory` points to `frontend/.next`

---

## ⚙️ Step 3: Configure Environment Variables

**CRITICAL**: Set the backend API URL as an environment variable.

1. **Before clicking "Next"**, scroll down to **"Advanced settings"**
2. **Click**: "Add environment variable"
3. **Add**:
   - **Key**: `NEXT_PUBLIC_API_URL`
   - **Value**: `http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1`

**Important Notes**:
- ✅ Must start with `NEXT_PUBLIC_` to be accessible in browser
- ✅ Include `/api/v1` at the end
- ✅ Use `http` (not `https`) unless you've configured SSL

4. **Click**: "Next"

---

## 🔍 Step 4: Review and Deploy

1. **Review** all settings:
   - Repository: Correct
   - Branch: `main`
   - Build settings: Points to `frontend/` directory
   - Environment variable: `NEXT_PUBLIC_API_URL` set

2. **Click**: "Save and deploy"

---

## ⏳ Step 5: Wait for Deployment

Amplify will now:
1. **Provision** resources (~1-2 minutes)
2. **Build** your application (~3-5 minutes)
3. **Deploy** to CDN (~1-2 minutes)

**Total time**: ~5-10 minutes

You'll see phases:
- ✅ Provision
- ✅ Build
- ✅ Deploy
- ✅ Verify

---

## 🎯 Step 6: Access Your Deployed Frontend

Once deployment completes:

1. **Click** on your app name in Amplify console
2. **Copy** the Amplify URL (looks like: `https://main.XXXXX.amplifyapp.com`)
3. **Open** in browser

---

## 🧪 Step 7: Test the Deployment

### Test 1: Frontend Loads

**Open**: `https://main.XXXXX.amplifyapp.com`

**Expected**:
- ✅ Login page loads
- ✅ No console errors
- ✅ Styling looks correct

### Test 2: Backend Connection

1. **Go to**: Login page
2. **Try logging in**:
   - Username: `admin`
   - Password: `admin123`

**Expected**:
- ✅ Login succeeds
- ✅ Redirects to dashboard
- ✅ Dashboard loads with data

### Test 3: API Calls Work

1. **After login**, navigate to:
   - `/customers` - Should load customer list
   - `/invoices` - Should load invoice list
   - `/dashboard` - Should show statistics

**Expected**:
- ✅ All pages load
- ✅ Data displays correctly
- ✅ No CORS errors in console

---

## 🔧 Troubleshooting

### Issue 1: 404 on Page Refresh

**Problem**: Refreshing any page (except home) shows 404

**Fix**: Amplify needs rewrite rules for Next.js

1. **Go to**: Amplify Console → Your App → "Rewrites and redirects"
2. **Add** rewrites (usually auto-configured for Next.js)
3. If missing, add:
   ```
   Source: </^[^.]+$|\.(?!(css|gif|ico|jpg|js|png|txt|svg|woff|ttf|map|json)$)([^.]+$)/>
   Target: /index.html
   Type: 200 (Rewrite)
   ```

### Issue 2: Can't Connect to Backend (CORS Error)

**Problem**: Console shows CORS error when calling API

**Symptoms**:
```
Access to XMLHttpRequest at 'http://...' from origin 'https://...' has been blocked by CORS
```

**Fix**: Update backend CORS configuration

In your Spring Boot `SecurityConfig` or `CorsConfig`:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "https://main.XXXXX.amplifyapp.com"  // Add your Amplify URL
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    // ...
}
```

### Issue 3: Environment Variable Not Working

**Problem**: Frontend still trying to connect to `localhost:8080`

**Symptoms**:
- Network errors in console
- Trying to reach `http://localhost:8080/api/v1`

**Fix**:
1. **Go to**: Amplify Console → Environment variables
2. **Verify**: `NEXT_PUBLIC_API_URL` is set correctly
3. **Redeploy**: Amplify → Redeploy this version
4. **Wait**: 5 minutes for new build

### Issue 4: Build Fails

**Problem**: Build phase fails in Amplify

**Common causes**:
1. **Wrong Node version**: Amplify uses Node 16 by default, but frontend needs Node 20
   - **Fix**: Add to build settings:
     ```yaml
     preBuild:
       commands:
         - cd frontend
         - nvm use 20  # <-- This line
         - npm ci
     ```

2. **TypeScript errors**: Strict type checking fails build
   - **Check**: Local build works: `npm run build`
   - **Fix**: Fix TypeScript errors locally, commit, push

3. **Missing dependencies**: `package-lock.json` out of sync
   - **Fix**: Delete `node_modules` and `package-lock.json`, run `npm install`, commit

---

## 🎨 Step 8: Custom Domain (Optional)

To use your own domain instead of `amplifyapp.com`:

1. **Go to**: Amplify Console → Domain management
2. **Add domain**
3. **Follow** wizard to:
   - Connect your domain
   - Configure DNS
   - Enable SSL (automatic with Amplify)

---

## 📋 Final Configuration Summary

### Frontend URL
- **Amplify URL**: `https://main.XXXXX.amplifyapp.com`
- **Custom Domain**: (if configured)

### Backend URL
- **Elastic Beanstalk**: `http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com`

### Environment Variables
```
NEXT_PUBLIC_API_URL=http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1
```

### Build Configuration
- **Framework**: Next.js 16.0.1
- **Node Version**: 20
- **Build Command**: `npm run build`
- **Output Directory**: `frontend/.next`

---

## 🔄 Continuous Deployment

**Amplify automatically deploys** when you push to the `main` branch:

1. **Make changes** locally
2. **Commit and push** to GitHub:
   ```bash
   git add .
   git commit -m "Update feature"
   git push origin main
   ```
3. **Amplify auto-deploys** (watch in Amplify Console)
4. **Wait ~5 minutes** for build and deployment

---

## ✅ Deployment Checklist

- [ ] Code pushed to GitHub `main` branch
- [ ] Amplify app created and connected to GitHub
- [ ] Build settings configured (points to `frontend/` directory)
- [ ] Environment variable `NEXT_PUBLIC_API_URL` set
- [ ] First deployment succeeded
- [ ] Frontend loads in browser
- [ ] Login works (connects to backend)
- [ ] Dashboard shows data
- [ ] All pages accessible
- [ ] No CORS errors
- [ ] No console errors

---

## 🎉 Success Indicators

**Your deployment is successful when**:
1. ✅ Amplify shows "Deployed" status (green)
2. ✅ Frontend URL opens and shows login page
3. ✅ Can log in with `admin` / `admin123`
4. ✅ Dashboard loads with statistics
5. ✅ Can create/edit customers, invoices, payments
6. ✅ All 12 pages work correctly

---

**Ready to deploy! Follow Step 1 and let's get your frontend live!** 🚀

