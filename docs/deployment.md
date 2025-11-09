# InvoiceMe Deployment Guide

**Purpose**: Complete guide for deploying InvoiceMe backend and frontend to AWS, including local development setup, cloud deployment, and troubleshooting.

---

## Table of Contents

1. [Local Deployment](#local-deployment)
2. [AWS Deployment](#aws-deployment)
3. [Environment Variables](#environment-variables)
4. [Post-Deployment Verification](#post-deployment-verification)
5. [Troubleshooting](#troubleshooting)

---

## Local Deployment

### Prerequisites

- Java 17+ installed
- Node.js 18+ installed
- PostgreSQL running (via Docker Compose or local installation)
- AWS credentials configured (for SES and S3)

### Backend Local Setup

1. **Navigate to backend directory**:
   ```bash
   cd backend
   ```

2. **Set environment variables** (create `.env` file or export):
   ```bash
   export DATABASE_URL=jdbc:postgresql://localhost:5432/invoiceme
   export DB_USERNAME=postgres
   export DB_PASSWORD=postgres
   export JWT_SECRET=your-secret-key-here
   export AWS_REGION=us-east-1
   export AWS_ACCESS_KEY_ID=your-access-key
   export AWS_SECRET_ACCESS_KEY=your-secret-key
   export AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
   export AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
   ```

3. **Start PostgreSQL** (if using Docker Compose):
   ```bash
   cd ..
   docker-compose up -d postgres
   ```

4. **Run Flyway migrations** (if needed):
   ```bash
   cd backend
   mvn flyway:migrate
   ```

5. **Start Spring Boot application**:
   ```bash
   mvn spring-boot:run
   ```

6. **Verify backend is running**:
   ```bash
   curl http://localhost:8080/actuator/health
   ```
   Expected response: `{"status":"UP"}`

### Frontend Local Setup

1. **Navigate to frontend directory**:
   ```bash
   cd frontend
   ```

2. **Install dependencies**:
   ```bash
   npm install
   ```

3. **Set environment variables**:
   ```bash
   export NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
   ```
   
   Or create `.env.local`:
   ```
   NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
   ```

4. **Start development server**:
   ```bash
   npm run dev
   ```

5. **Verify frontend is running**:
   - Open browser: `http://localhost:3000`
   - You should see the login page

### Connecting Frontend to Backend

The frontend automatically connects to the backend using the `NEXT_PUBLIC_API_URL` environment variable. Make sure:

1. Backend is running on `http://localhost:8080`
2. Frontend has `NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1` set
3. CORS is properly configured (backend allows requests from `http://localhost:3000`)

---

## AWS Deployment

### Phase 1: Backend Deployment (Elastic Beanstalk)

#### Step 1: Create Elastic Beanstalk Application

1. **Go to AWS Console** → Elastic Beanstalk
2. **Click "Create Application"**
3. **Application details**:
   - Application name: `invoiceme-backend`
   - Description: `InvoiceMe Backend API`
   - Platform: **Java**
   - Platform branch: **Corretto 17**
   - Platform version: **Java 17 running on 64bit Amazon Linux 2023**
   - Application code: **Upload your code**

#### Step 2: Create Environment

1. **Environment details**:
   - Environment name: `invoiceme-backend-prod` (or `-dev` for development)
   - Domain: Auto-generated (or use custom domain)
   - Description: `InvoiceMe Backend Production Environment`

2. **Configure more options**:
   - **Capacity**: 
     - Environment type: Single instance (for dev) or Load balanced (for prod)
     - Instance type: `t3.small` (minimum recommended)
   - **Load balancer**: Application Load Balancer (if using load balanced)
   - **Rolling updates**: Enable rolling updates for zero downtime

3. **Upload application code**:
   - Build JAR locally: `cd backend && mvn clean package -DskipTests`
   - Upload: `target/invoiceme-backend-1.0.0-SNAPSHOT.jar`

4. **Click "Create environment"** (takes 5-10 minutes)

#### Step 3: Configure Environment Variables

1. **Go to Configuration** → **Software** → **Environment properties**
2. **Click "Edit"** and add:
   ```
   DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=[YOUR_PASSWORD]
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

3. **Click "Apply"** (environment will restart)

#### Step 4: Configure Health Check

1. **Go to Configuration** → **Load balancer** (if using load balanced)
2. **Health check path**: `/actuator/health`
3. **Health check interval**: 30 seconds
4. **Unhealthy threshold**: 5
5. **Healthy threshold**: 3

#### Step 5: Get Backend URL

1. **Wait for deployment to complete** (green status)
2. **Copy the environment URL** (e.g., `http://invoiceme-backend-prod.us-east-1.elasticbeanstalk.com`)
3. **Test health endpoint**:
   ```bash
   curl http://[backend-url]/actuator/health
   ```

**Note**: Elastic Beanstalk uses port 5000 internally. The `.ebextensions` configuration files handle this automatically.

---

### Phase 2: Frontend Deployment (Amplify)

#### Step 1: Create Amplify App

1. **Go to AWS Console** → **Amplify**
2. **Click "New app"** → **"Host web app"**
3. **Repository**:
   - Source: **GitHub**
   - Click "Authorize" and connect your GitHub account
   - Repository: `mlx93/invoiceme-mlx`
   - Branch: `main`
4. **Build settings**:
   - **Use existing build settings** (Amplify will detect `amplify.yml`)
   - Or manually configure:
     - Build image: `Amazon Linux 2023`
     - Build commands: See `amplify.yml` in repository

#### Step 2: Configure Environment Variables

1. **Go to App settings** → **Environment variables**
2. **Add variable**:
   - Key: `NEXT_PUBLIC_API_URL`
   - Value: `http://[backend-elastic-beanstalk-url]/api/v1`
   - Example: `http://invoiceme-backend-prod.us-east-1.elasticbeanstalk.com/api/v1`

3. **Save** (Amplify will automatically rebuild)

#### Step 3: Deploy Application

1. **Amplify automatically deploys** when you connect the repository
2. **Monitor build progress** in the Amplify console
3. **Wait for deployment** (5-10 minutes)
4. **Get frontend URL**:
   - Copy the app URL (e.g., `https://main.d1234567890.amplifyapp.com`)

#### Step 4: Custom Domain (Optional)

1. **Go to App settings** → **Domain management**
2. **Add custom domain**:
   - Domain: `invoiceme.com` (or your domain)
   - Subdomain: `app` (results in `app.invoiceme.com`)
3. **Configure DNS**:
   - Add CNAME record pointing to Amplify domain
   - Wait for SSL certificate provisioning (automatic)

---

### Phase 3: CI/CD Pipeline Setup

The GitHub Actions workflow (`.github/workflows/deploy.yml`) automatically deploys on push to `main` branch.

#### Workflow Steps

1. **Backend Deployment**:
   - Runs tests (optional, continues on failure)
   - Builds JAR: `mvn clean package -DskipTests`
   - Deploys to Elastic Beanstalk using `beanstalk-deploy` action
   - Waits for environment update

2. **Frontend Deployment**:
   - Runs tests (optional, continues on failure)
   - Builds Next.js app: `npm run build`
   - Amplify auto-deploys from GitHub (no action needed)

#### Manual Trigger

You can manually trigger deployment:
1. Go to GitHub → Actions → "Deploy to AWS"
2. Click "Run workflow" → Select branch → "Run workflow"

#### Required GitHub Secrets

Ensure these secrets are configured in GitHub (Settings → Secrets and variables → Actions):

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `DATABASE_URL`
- `JWT_SECRET`
- `AWS_SES_FROM_EMAIL`
- `AWS_S3_BUCKET_NAME`
- `AWS_REGION`
- `NEXT_PUBLIC_API_URL` (optional, can be set in Amplify console)

---

## Environment Variables

### Backend Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL connection string | `jdbc:postgresql://host:5432/db?user=user&password=pass` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `[password]` |
| `JWT_SECRET` | JWT signing secret (32+ chars) | `[random-string]` |
| `AWS_REGION` | AWS region | `us-east-1` |
| `AWS_ACCESS_KEY_ID` | AWS access key | `[access-key]` |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | `[secret-key]` |
| `AWS_SES_FROM_EMAIL` | Verified SES email | `mylesethan93@gmail.com` |
| `AWS_S3_BUCKET_NAME` | S3 bucket for PDFs | `invoiceme-pdfs-mlx` |
| `SERVER_PORT` | Server port (EB uses 5000) | `5000` |
| `SPRING_PROFILES_ACTIVE` | Spring profile | `production` |

### Frontend Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `NEXT_PUBLIC_API_URL` | Backend API base URL | `http://backend-url/api/v1` |

**Note**: `NEXT_PUBLIC_*` prefix is required for Next.js to expose variables to the browser.

---

## Post-Deployment Verification

### Backend Verification

1. **Health Check**:
   ```bash
   curl http://[backend-url]/actuator/health
   ```
   Expected: `{"status":"UP"}`

2. **API Endpoint Test**:
   ```bash
   curl http://[backend-url]/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"Test123!","fullName":"Test User"}'
   ```
   Expected: `201 Created`

3. **Database Connection**:
   - Check CloudWatch logs for database connection errors
   - Verify Flyway migrations ran successfully

### Frontend Verification

1. **Access Frontend**:
   - Open browser: `https://[frontend-url]`
   - Should see login page

2. **Test API Connection**:
   - Open browser DevTools → Network tab
   - Try to register/login
   - Verify API calls go to correct backend URL

3. **Check Build Logs**:
   - Go to Amplify → App → Deployments
   - View build logs for errors

### Full Flow Test

1. **Register User**:
   - Go to `/register`
   - Create account
   - Verify email (if SES configured)

2. **Login**:
   - Go to `/login`
   - Login with credentials
   - Verify JWT token stored

3. **Create Customer**:
   - Navigate to Customers → New
   - Create customer
   - Verify saved

4. **Create Invoice**:
   - Navigate to Invoices → New
   - Create invoice
   - Verify PDF generation (check S3 bucket)

---

## Troubleshooting

### Backend Issues

#### Application Won't Start

**Symptoms**: Environment health is "Severe" or "Degraded"

**Solutions**:
1. **Check CloudWatch Logs**:
   - Go to Elastic Beanstalk → Environment → Logs
   - Download recent logs
   - Look for startup errors

2. **Common Issues**:
   - **Database connection failed**: Verify `DATABASE_URL` is correct
   - **Port conflict**: Ensure `SERVER_PORT=5000` (EB requirement)
   - **Memory issues**: Increase instance size (t3.small → t3.medium)

3. **Check Environment Variables**:
   - Go to Configuration → Software → Environment properties
   - Verify all required variables are set

#### Health Check Failing

**Symptoms**: Health check returns 404 or 500

**Solutions**:
1. **Verify Actuator Endpoint**:
   ```bash
   curl http://[backend-url]/actuator/health
   ```

2. **Check Security Configuration**:
   - Ensure `/actuator/**` is permitted in `SecurityConfig.java`
   - Verify actuator dependency in `pom.xml`

3. **Check Health Check Path**:
   - Go to Configuration → Load balancer
   - Health check path: `/actuator/health`

#### Database Connection Issues

**Symptoms**: Application starts but database queries fail

**Solutions**:
1. **Verify Supabase Connection**:
   - Test connection string locally
   - Check Supabase dashboard for connection limits

2. **Check Connection Pool**:
   - Review HikariCP settings in `application.yml`
   - Increase pool size if needed

### Frontend Issues

#### Build Fails

**Symptoms**: Amplify build fails with errors

**Solutions**:
1. **Check Build Logs**:
   - Go to Amplify → App → Deployments → Build logs
   - Look for npm/node errors

2. **Common Issues**:
   - **Missing dependencies**: Run `npm install` locally
   - **TypeScript errors**: Fix type errors
   - **Environment variables**: Verify `NEXT_PUBLIC_API_URL` is set

#### API Calls Failing

**Symptoms**: Frontend can't connect to backend

**Solutions**:
1. **Verify Backend URL**:
   - Check `NEXT_PUBLIC_API_URL` in Amplify environment variables
   - Ensure URL includes `/api/v1` suffix

2. **Check CORS**:
   - Verify backend allows requests from Amplify domain
   - Check browser console for CORS errors

3. **Test Backend Directly**:
   ```bash
   curl https://[frontend-url]/api/v1/actuator/health
   ```
   (This should fail, but confirms frontend is trying to proxy)

### CI/CD Issues

#### GitHub Actions Fails

**Symptoms**: Workflow fails during deployment

**Solutions**:
1. **Check Workflow Logs**:
   - Go to GitHub → Actions → Failed workflow → View logs

2. **Common Issues**:
   - **AWS credentials**: Verify secrets are set correctly
   - **Elastic Beanstalk app not found**: Create app manually first
   - **Build failures**: Check Maven/npm build errors

3. **Manual Deployment**:
   - Deploy manually via AWS Console as fallback
   - Fix issues, then retry CI/CD

---

## Monitoring

### CloudWatch Logs

**Backend Logs**:
- Log group: `/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/eb-engine.log`
- View: AWS Console → CloudWatch → Log groups

**Frontend Logs**:
- View: AWS Console → Amplify → App → Deployments → Build logs

### Health Checks

**Backend**:
- Endpoint: `/actuator/health`
- Check: Elastic Beanstalk → Environment → Health

**Frontend**:
- Built-in Amplify health checks
- View: Amplify → App → Monitoring

---

## Rollback Procedures

### Backend Rollback

1. **Via Elastic Beanstalk Console**:
   - Go to Environment → Application versions
   - Select previous version
   - Click "Deploy"

2. **Via GitHub Actions**:
   - Revert commit
   - Push to `main`
   - Workflow will deploy previous version

### Frontend Rollback

1. **Via Amplify Console**:
   - Go to App → Deployments
   - Select previous deployment
   - Click "Redeploy this version"

---

## Security Best Practices

1. **Environment Variables**:
   - Never commit secrets to Git
   - Use GitHub Secrets for CI/CD
   - Use AWS Systems Manager Parameter Store for production secrets

2. **Database**:
   - Use connection pooling (Supabase)
   - Enable SSL for database connections
   - Rotate passwords regularly

3. **AWS Credentials**:
   - Use IAM roles instead of access keys when possible
   - Rotate access keys regularly
   - Limit IAM permissions (principle of least privilege)

4. **HTTPS**:
   - Always use HTTPS in production
   - Amplify provides SSL automatically
   - Elastic Beanstalk supports SSL via Load Balancer

---

## Cost Optimization

### Free Tier Eligibility

- **Elastic Beanstalk**: First 750 hours/month free (t2.micro/t3.micro)
- **Amplify**: First 1000 build minutes/month free
- **CloudWatch**: First 5GB logs/month free
- **Supabase**: Free tier (500MB database, 2GB bandwidth)
- **SES**: First 62,000 emails/month free (sandbox mode)

### Cost Monitoring

- Set up AWS Cost Explorer alerts
- Monitor CloudWatch log storage
- Review Amplify build minutes usage

---

## Next Steps

After successful deployment:

1. **Set up custom domain** (optional)
2. **Configure SSL certificates** (automatic with Amplify)
3. **Set up monitoring alerts** (CloudWatch alarms)
4. **Configure backup strategy** (Supabase automatic backups)
5. **Set up staging environment** (duplicate setup for dev/staging)

---

**Last Updated**: 2025-01-27  
**Maintained By**: DevOps Team

