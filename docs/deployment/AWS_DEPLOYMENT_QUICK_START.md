# AWS Deployment Quick Start Guide

**Quick reference for manual AWS deployment of InvoiceMe**

---

## Prerequisites Checklist

- [ ] AWS account with IAM user `invoiceme-deploy-mlx` configured
- [ ] Supabase database connection string ready
- [ ] GitHub repository: `mlx93/invoiceme-mlx`
- [ ] Backend JAR built: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`

---

## Step 1: Deploy Backend to Elastic Beanstalk (15-20 min)

### 1.1 Build Backend JAR
```bash
cd backend
mvn clean package -DskipTests
```
**Output**: `target/invoiceme-backend-1.0.0-SNAPSHOT.jar`

### 1.2 Create Elastic Beanstalk Application
1. **AWS Console** → **Elastic Beanstalk** → **Create Application**
2. **Application name**: `invoiceme-backend`
3. **Platform**: **Java** → **Corretto 17** → **Java 17 running on 64bit Amazon Linux 2023**
4. **Application code**: **Upload your code** → Select JAR file
5. Click **Create application** (wait 5-10 minutes)

### 1.3 Create Environment
1. After app created → **Create environment**
2. **Environment name**: `invoiceme-backend-prod`
3. **Domain**: Auto-generated (or custom)
4. **Capacity**: **Single instance** (dev) or **Load balanced** (prod)
5. Click **Create environment** (wait 5-10 minutes)

### 1.4 Configure Environment Variables
1. Go to **Configuration** → **Software** → **Environment properties** → **Edit**
2. Add these variables:

```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=[YOUR_SUPABASE_PASSWORD]
DB_USERNAME=postgres
DB_PASSWORD=[YOUR_SUPABASE_PASSWORD]
JWT_SECRET=[GENERATE_RANDOM_32_CHAR_STRING]
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=[YOUR_AWS_ACCESS_KEY]
AWS_SECRET_ACCESS_KEY=[YOUR_AWS_SECRET_KEY]
AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
SERVER_PORT=5000
SPRING_PROFILES_ACTIVE=production
```

3. Click **Apply** (environment restarts)

### 1.5 Configure Health Check
1. **Configuration** → **Load balancer** → **Edit**
2. **Health check path**: `/actuator/health`
3. **Health check interval**: `30` seconds
4. Click **Apply**

### 1.6 Get Backend URL
1. Wait for **green status** (healthy)
2. Copy **Environment URL** (e.g., `http://invoiceme-backend-prod.us-east-1.elasticbeanstalk.com`)
3. **Test**: `curl http://[backend-url]/actuator/health`
   - Expected: `{"status":"UP"}`

**✅ Backend URL**: `_________________________________`

---

## Step 2: Deploy Frontend to Amplify (10-15 min)

### 2.1 Create Amplify App
1. **AWS Console** → **Amplify** → **New app** → **Host web app**
2. **Repository**: **GitHub** → Authorize → Select `mlx93/invoiceme-mlx`
3. **Branch**: `main`
4. **Build settings**: **Use existing build settings** (detects `amplify.yml`)
5. Click **Create app** (wait 5-10 minutes)

### 2.2 Configure Environment Variable
1. **App settings** → **Environment variables** → **Add variable**
2. **Key**: `NEXT_PUBLIC_API_URL`
3. **Value**: `http://[BACKEND_URL_FROM_STEP_1]/api/v1`
   - Example: `http://invoiceme-backend-prod.us-east-1.elasticbeanstalk.com/api/v1`
4. Click **Save** (Amplify rebuilds automatically)

### 2.3 Get Frontend URL
1. Wait for **green status** (deployed)
2. Copy **App URL** (e.g., `https://main.d1234567890.amplifyapp.com`)
3. **Test**: Open URL in browser → Should see login page

**✅ Frontend URL**: `_________________________________`

---

## Step 3: Verify Deployment (5 min)

### 3.1 Test Backend
```bash
# Health check
curl http://[backend-url]/actuator/health

# API test (should return 201 or 400)
curl -X POST http://[backend-url]/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!","fullName":"Test User"}'
```

### 3.2 Test Frontend
1. Open frontend URL in browser
2. Should see login page
3. Open browser DevTools → Network tab
4. Try to register/login → Verify API calls go to backend

### 3.3 Run Verification Script
```bash
BACKEND_URL=http://[backend-url] \
FRONTEND_URL=https://[frontend-url] \
./scripts/verify-deployment.sh
```

---

## Step 4: Test CI/CD Pipeline (Optional, 5 min)

### 4.1 Trigger Deployment
1. Make a small change (e.g., update a comment)
2. Commit and push to `main` branch:
   ```bash
   git add .
   git commit -m "test: trigger CI/CD"
   git push origin main
   ```

### 4.2 Verify GitHub Actions
1. Go to **GitHub** → **Actions** tab
2. Watch workflow run
3. Verify both backend and frontend deploy successfully

---

## Quick Troubleshooting

### Backend Issues

**Health check failing?**
- Check CloudWatch logs: `/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/eb-engine.log`
- Verify environment variables are set correctly
- Check database connection string

**Application won't start?**
- Verify `SERVER_PORT=5000` is set
- Check JAR file uploaded correctly
- Review CloudWatch logs for errors

### Frontend Issues

**Build fails?**
- Check Amplify build logs
- Verify `NEXT_PUBLIC_API_URL` is set correctly
- Check for TypeScript/build errors

**Can't connect to backend?**
- Verify `NEXT_PUBLIC_API_URL` includes `/api/v1`
- Check backend URL is accessible
- Check browser console for CORS errors

---

## Environment Variables Quick Reference

### Backend (Elastic Beanstalk)
```
DATABASE_URL=jdbc:postgresql://[supabase-host]:5432/postgres?user=postgres&password=[password]
DB_USERNAME=postgres
DB_PASSWORD=[supabase-password]
JWT_SECRET=[32-char-random-string]
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=[from-github-secrets]
AWS_SECRET_ACCESS_KEY=[from-github-secrets]
AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
SERVER_PORT=5000
SPRING_PROFILES_ACTIVE=production
```

### Frontend (Amplify)
```
NEXT_PUBLIC_API_URL=http://[backend-url]/api/v1
```

---

## URLs to Save

- **Backend URL**: `_________________________________`
- **Frontend URL**: `_________________________________`
- **Elastic Beanstalk Console**: https://console.aws.amazon.com/elasticbeanstalk
- **Amplify Console**: https://console.aws.amazon.com/amplify
- **CloudWatch Logs**: https://console.aws.amazon.com/cloudwatch

---

## Next Steps After Deployment

1. **Set up CloudWatch alarms** (see `/docs/monitoring.md`)
2. **Create CloudWatch dashboards** (see `/docs/monitoring.md`)
3. **Configure custom domain** (optional, in Amplify settings)
4. **Set up SNS notifications** for alerts

---

**Total Time**: ~30-40 minutes  
**Status**: Ready to deploy ✅

