# InvoiceMe Deployment Guide

**Version**: 1.0  
**Last Updated**: January 2025

---

## Table of Contents

1. [Overview](#overview)
2. [Backend Deployment (AWS Elastic Beanstalk)](#backend-deployment-aws-elastic-beanstalk)
3. [Frontend Deployment (Vercel)](#frontend-deployment-vercel)
4. [Database Configuration](#database-configuration)
5. [Environment Variables](#environment-variables)
6. [CI/CD Pipeline](#cicd-pipeline)
7. [Verification](#verification)
8. [Troubleshooting](#troubleshooting)

---

## Overview

InvoiceMe is deployed across multiple cloud services:

- **Backend**: AWS Elastic Beanstalk (Java 17 Spring Boot)
- **Frontend**: Vercel (Next.js SSR)
- **Database**: Supabase PostgreSQL (Connection Pooler)
- **Email**: AWS SES
- **Storage**: AWS S3 (PDF invoices)

---

## Backend Deployment (AWS Elastic Beanstalk)

### Prerequisites

- AWS Account with Elastic Beanstalk access
- AWS CLI configured (optional, for CLI deployment)
- Backend JAR file built (`mvn clean package`)

### Step 1: Build Backend JAR

```bash
cd backend
mvn clean package -DskipTests
```

**Output**: `target/invoiceme-backend-2.0.0.jar`

### Step 2: Create Elastic Beanstalk Application

1. **Navigate to AWS Console** → Elastic Beanstalk
2. **Click "Create Application"**
3. **Application Details**:
   - Application name: `invoiceme-backend`
   - Description: `InvoiceMe Backend API`
4. **Platform**:
   - Platform: **Java**
   - Platform branch: **Corretto 17**
   - Platform version: **Java 17 running on 64bit Amazon Linux 2023**
5. **Application Code**:
   - **Upload your code** → Choose file → Select `backend/target/invoiceme-backend-2.0.0.jar`
6. **Click "Create Application"**

**Wait Time**: 5-10 minutes for environment creation

### Step 3: Configure Environment Variables

1. **Navigate to Configuration** → Software → Environment properties
2. **Click "Edit"**
3. **Add Environment Variables**:

```
DATABASE_URL=jdbc:postgresql://[supabase-pooler-host]:6543/postgres?user=postgres&password=[password]&sslmode=require
JWT_SECRET=[64-character-secret-key]
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=[your-access-key]
AWS_SECRET_ACCESS_KEY=[your-secret-key]
AWS_SES_FROM_EMAIL=your-email@example.com
AWS_S3_BUCKET_NAME=invoiceme-pdfs-production
SPRING_FLYWAY_ENABLED=false
SERVER_PORT=5000
```

**Important**:
- Use Supabase **Connection Pooler** URL (port 6543), not direct connection
- Generate secure JWT secret (64 characters minimum)
- Set `SPRING_FLYWAY_ENABLED=false` (migrations run manually in production)

4. **Click "Apply"**

**Wait Time**: 2-5 minutes for configuration update

### Step 4: Configure Health Check

1. **Navigate to Configuration** → Load balancer
2. **Health check path**: `/api/v1/health`
3. **Click "Apply"**

### Step 5: Get Backend URL

After deployment completes:
- **Environment URL**: `http://invoiceme-backend-env.eba-xxxxx.us-east-1.elasticbeanstalk.com`
- **Copy this URL** for frontend configuration

### Step 6: Verify Backend Deployment

```bash
curl http://[backend-url]/api/v1/health
# Should return: {"status":"UP"}
```

---

## Frontend Deployment (Vercel)

### Prerequisites

- Vercel account (free tier available)
- GitHub repository connected
- Backend URL from Elastic Beanstalk

### Step 1: Connect GitHub Repository

1. **Navigate to Vercel Dashboard** → https://vercel.com
2. **Click "Add New Project"**
3. **Import Git Repository**:
   - Select your GitHub repository
   - Authorize Vercel to access repository (if first time)
4. **Click "Import"**

### Step 2: Configure Build Settings

Vercel auto-detects Next.js projects. Verify settings:

- **Framework Preset**: Next.js
- **Root Directory**: `frontend` (if frontend is in subdirectory)
- **Build Command**: `npm run build` (or `cd frontend && npm run build`)
- **Output Directory**: `.next` (auto-detected)

### Step 3: Configure Environment Variables

1. **Navigate to Project Settings** → Environment Variables
2. **Add Environment Variable**:

```
NEXT_PUBLIC_API_URL=https://[backend-url]/api/v1
```

**Important**: Use HTTPS URL (Vercel uses HTTPS by default)

3. **Click "Save"**

### Step 4: Deploy

1. **Click "Deploy"**
2. **Wait for Build** (5-10 minutes)

**Build Process**:
- Installs dependencies (`npm install`)
- Builds Next.js application (`npm run build`)
- Deploys to Vercel edge network

### Step 5: Get Frontend URL

After deployment:
- **Production URL**: `https://invoiceme.vercel.app` (or custom domain)
- **Copy this URL** for testing

### Step 6: Configure Custom Domain (Optional)

1. **Navigate to Project Settings** → Domains
2. **Add Domain**: Enter your domain (e.g., `app.invoiceme.com`)
3. **Follow DNS Instructions**: Add CNAME record to your DNS provider
4. **Wait for SSL Certificate** (automatic, 1-5 minutes)

---

## Database Configuration

### Supabase Setup

1. **Create Supabase Project**: https://supabase.com
2. **Get Connection String**:
   - Project Settings → Database
   - **Connection Pooler** (use this for production)
   - Copy connection string (port 6543)

### Connection Pooler Configuration

**Why**: Connection Pooler is required for AWS Elastic Beanstalk external connections.

**Connection String Format**:
```
jdbc:postgresql://[pooler-host]:6543/postgres?user=postgres&password=[password]&sslmode=require
```

**Important**:
- Use port **6543** (Connection Pooler), not 5432 (direct connection)
- Include `sslmode=require` for secure connection
- Connection Pooler handles connection limits and pooling

### Run Migrations Manually

**In Production**: Flyway is disabled (`SPRING_FLYWAY_ENABLED=false`). Run migrations manually:

1. **Connect to Supabase**:
   ```bash
   psql "postgresql://postgres:[password]@[supabase-host]:5432/postgres?sslmode=require"
   ```

2. **Run Migrations**:
   ```bash
   cd backend
   mvn flyway:migrate -Dflyway.url=jdbc:postgresql://[supabase-host]:5432/postgres -Dflyway.user=postgres -Dflyway.password=[password]
   ```

**Or**: Use Supabase SQL Editor to run migration files manually.

---

## Environment Variables

### Backend (Elastic Beanstalk)

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | Supabase Connection Pooler URL | `jdbc:postgresql://...` |
| `JWT_SECRET` | JWT signing secret (64 chars) | `[generated-secret]` |
| `AWS_REGION` | AWS region | `us-east-1` |
| `AWS_ACCESS_KEY_ID` | AWS access key | `AKIA...` |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | `[secret]` |
| `AWS_SES_FROM_EMAIL` | SES sender email | `noreply@invoiceme.com` |
| `AWS_S3_BUCKET_NAME` | S3 bucket for PDFs | `invoiceme-pdfs-prod` |
| `SPRING_FLYWAY_ENABLED` | Enable Flyway migrations | `false` |
| `SERVER_PORT` | Server port | `5000` |

### Frontend (Vercel)

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend API URL | `https://api.invoiceme.com/api/v1` |

---

## CI/CD Pipeline

### GitHub Actions Workflow

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  deploy-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'corretto'
      - name: Build with Maven
        run: |
          cd backend
          mvn clean package -DskipTests
      - name: Deploy to Elastic Beanstalk
        uses: einaregilsson/beanstalk-deploy@v22
        with:
          aws_access_key: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws_secret_key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          application_name: invoiceme-backend
          environment_name: invoiceme-backend-prod
          version_label: ${{ github.sha }}
          region: us-east-1
          deployment_package: backend/target/invoiceme-backend-2.0.0.jar

  deploy-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          vercel-args: '--prod'
```

**GitHub Secrets**:
- `AWS_ACCESS_KEY_ID`: AWS access key
- `AWS_SECRET_ACCESS_KEY`: AWS secret key
- `VERCEL_TOKEN`: Vercel API token
- `VERCEL_ORG_ID`: Vercel organization ID
- `VERCEL_PROJECT_ID`: Vercel project ID

---

## Verification

### Backend Verification

1. **Health Check**:
   ```bash
   curl https://[backend-url]/api/v1/health
   # Expected: {"status":"UP"}
   ```

2. **API Endpoint Test**:
   ```bash
   curl https://[backend-url]/api/v1/customers \
     -H "Authorization: Bearer [token]"
   # Expected: Paginated customer list or 401 Unauthorized
   ```

3. **Check Logs**:
   - AWS Console → Elastic Beanstalk → Logs
   - Verify no errors in application logs

### Frontend Verification

1. **Load Application**:
   - Open frontend URL in browser
   - Verify login page loads

2. **Test Login**:
   - Register new user or use existing credentials
   - Verify login succeeds

3. **Test API Integration**:
   - After login, verify dashboard loads
   - Check browser console for API errors

### Database Verification

1. **Connect to Database**:
   ```bash
   psql "postgresql://postgres:[password]@[supabase-host]:5432/postgres?sslmode=require"
   ```

2. **Verify Tables**:
   ```sql
   \dt
   -- Should show: customers, invoices, line_items, payments, users, etc.
   ```

3. **Check Migration History**:
   ```sql
   SELECT * FROM flyway_schema_history ORDER BY installed_rank;
   ```

---

## Troubleshooting

### Backend Issues

**Issue**: `502 Bad Gateway`  
**Solution**:
- Check Elastic Beanstalk logs for errors
- Verify environment variables are set correctly
- Check health check path (`/api/v1/health`)

**Issue**: `Database connection failed`  
**Solution**:
- Verify Connection Pooler URL (port 6543)
- Check `sslmode=require` in connection string
- Verify Supabase firewall allows AWS IPs

**Issue**: `JWT authentication fails`  
**Solution**:
- Verify `JWT_SECRET` is set (64 characters minimum)
- Check token expiration (24 hours)
- Verify token format in Authorization header

### Frontend Issues

**Issue**: `API calls fail (CORS error)`  
**Solution**:
- Verify `NEXT_PUBLIC_API_URL` is set correctly
- Check backend CORS configuration
- Use Next.js rewrites (configured in `next.config.ts`)

**Issue**: `Build fails`  
**Solution**:
- Check Vercel build logs for errors
- Verify all dependencies in `package.json`
- Check TypeScript errors

### Database Issues

**Issue**: `Connection limit exceeded`  
**Solution**:
- Use Connection Pooler (port 6543) instead of direct connection
- Check connection pool settings in `application.yml`

**Issue**: `Migration errors`  
**Solution**:
- Run migrations manually using Supabase SQL Editor
- Verify migration files are correct
- Check Flyway history table

---

## Rollback Procedures

### Backend Rollback

1. **Navigate to Elastic Beanstalk** → Application Versions
2. **Select Previous Version**
3. **Click "Deploy"**
4. **Select Environment** → Click "Deploy"

### Frontend Rollback

1. **Navigate to Vercel Dashboard** → Deployments
2. **Select Previous Deployment**
3. **Click "Promote to Production"**

---

## Monitoring

### AWS CloudWatch

- **Logs**: Elastic Beanstalk → Logs → Request Logs
- **Metrics**: CloudWatch → Metrics → Elastic Beanstalk
- **Alarms**: Set up alarms for error rates, latency

### Vercel Analytics

- **Analytics**: Vercel Dashboard → Analytics
- **Logs**: Vercel Dashboard → Logs
- **Performance**: Vercel Dashboard → Speed Insights

---

**Document Version**: 1.0  
**Last Updated**: January 2025

