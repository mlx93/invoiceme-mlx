# DevOps Agent Prompt

**[AGENT]: DevOps**

**GOAL**: Configure AWS deployment (Elastic Beanstalk for backend, Amplify for frontend), set up CI/CD pipeline (GitHub Actions), and configure monitoring (CloudWatch) for M3 milestone completion.

---

## Context

**M2 Status**: ✅ **COMPLETE**
- Backend: Spring Boot application ready for deployment
- Frontend: Next.js application ready for deployment
- Both applications tested locally and ready for cloud deployment

**M3 Milestone**: Non-Functional Targets Validation
- Deploy backend to AWS Elastic Beanstalk
- Deploy frontend to AWS Amplify
- Configure CI/CD pipeline (GitHub Actions)
- Configure monitoring (CloudWatch logs and metrics)
- Test deployment (verify applications accessible, APIs working)

**Infrastructure Already Set Up** (from Setup Instructions):
- AWS Account with IAM user (`invoiceme-deploy-mlx`)
- Supabase database (PostgreSQL)
- AWS SES email verified
- AWS S3 bucket created
- GitHub repository with secrets configured

---

## Inputs

**Required Documents**:
- `PRD_2_Tech_Spec.md` - Deployment architecture (Section 9), CI/CD (Section 10)
- `ORCHESTRATOR_OUTPUT.md` - Project scope, architecture decisions
- `docs/SETUP_COMPLETION_REPORT.md` - Infrastructure setup status
- `backend/pom.xml` - Backend build configuration
- `frontend/package.json` - Frontend build configuration
- `backend/src/main/resources/application.yml` - Backend configuration
- `frontend/.env.local` (if exists) - Frontend environment variables

**AWS Services Required**:
- **AWS Elastic Beanstalk** - Backend hosting (Spring Boot JAR)
- **AWS Amplify** - Frontend hosting (Next.js)
- **AWS CloudWatch** - Logging and monitoring
- **Supabase** - Database (already configured)
- **AWS SES** - Email (already configured)
- **AWS S3** - PDF storage (already configured)

---

## Deliverables

### 1. Backend Deployment Configuration (`/backend/.ebextensions/`)
**Purpose**: Elastic Beanstalk configuration files for Spring Boot application

**Files**:
- `01-environment.config` - Environment variables configuration
- `02-nginx.config` - Nginx reverse proxy configuration (if needed)
- `03-healthcheck.config` - Health check endpoint configuration

**Configuration Required**:
- Environment variables: `DATABASE_URL`, `JWT_SECRET`, `AWS_REGION`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_SES_FROM_EMAIL`, `AWS_S3_BUCKET_NAME`
- Java version: 17
- Health check path: `/actuator/health` (or `/api/v1/health`)

### 2. Frontend Deployment Configuration (`/amplify.yml`)
**Purpose**: AWS Amplify build and deployment configuration

**Configuration Required**:
- Build commands: `npm install`, `npm run build`
- Environment variables: `NEXT_PUBLIC_API_URL` (pointing to Elastic Beanstalk backend URL)
- Output directory: `.next`
- Node.js version: 18.x

### 3. GitHub Actions CI/CD Pipeline (`.github/workflows/deploy.yml`)
**Purpose**: Automated deployment on push to main branch

**Workflow Steps**:
1. **Backend Tests**: Run `mvn test` (if tests exist)
2. **Backend Build**: Build JAR with `mvn clean package -DskipTests`
3. **Backend Deploy**: Deploy JAR to Elastic Beanstalk using AWS CLI or EB CLI
4. **Frontend Tests**: Run `npm test` (if tests exist)
5. **Frontend Build**: Build Next.js app (handled by Amplify)
6. **Frontend Deploy**: Trigger Amplify deployment (or Amplify auto-deploys from GitHub)

**Secrets Required** (already configured in GitHub):
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `DATABASE_URL`
- `JWT_SECRET`
- `AWS_SES_FROM_EMAIL`
- `AWS_S3_BUCKET_NAME`

### 4. Deployment Documentation (`/docs/deployment.md`)
**Format**: Markdown document with:
- **Local Deployment**:
  - How to run backend locally (`mvn spring-boot:run`)
  - How to run frontend locally (`npm run dev`)
  - How to connect frontend to backend
- **AWS Deployment**:
  - Elastic Beanstalk deployment steps (manual and CI/CD)
  - Amplify deployment steps (manual and CI/CD)
  - Environment variables configuration
  - Database connection (Supabase)
  - Troubleshooting guide
- **Post-Deployment Verification**:
  - How to verify backend is accessible
  - How to verify frontend is accessible
  - How to test API endpoints
  - How to check CloudWatch logs

### 5. Monitoring Configuration (`/docs/monitoring.md`)
**Format**: Markdown document with:
- **CloudWatch Logs**:
  - Log groups configuration
  - Log retention policy (7 days for dev, 30 days for prod)
  - How to view logs in AWS Console
- **Custom Metrics** (optional):
  - API latency (p50, p95, p99)
  - Error rate
  - Request count
- **Alarms** (optional):
  - API error rate > 5%
  - Database connection pool exhausted
  - Application health check failures

### 6. Deployment Verification Script (`/scripts/verify-deployment.sh`)
**Purpose**: Script to verify deployment is working

**Checks**:
- Backend health check endpoint accessible
- Frontend accessible
- API endpoints responding (GET /api/v1/customers)
- Database connection working
- Environment variables configured correctly

---

## Deployment Procedures

### Phase 1: Backend Deployment (Elastic Beanstalk)

1. **Create Elastic Beanstalk Application**:
   - AWS Console → Elastic Beanstalk → Create Application
   - Application name: `invoiceme-backend`
   - Platform: Java (Corretto 17)
   - Platform branch: Java 17 running on 64bit Amazon Linux 2023
   - Application code: Upload JAR or use GitHub integration

2. **Create Environment**:
   - Environment name: `invoiceme-backend-prod` (or `-dev`)
   - Domain: Auto-generated or custom domain
   - Capacity: Single instance (for dev) or load balanced (for prod)
   - Configuration: Set environment variables (DATABASE_URL, JWT_SECRET, etc.)

3. **Configure Environment Variables**:
   - Go to Configuration → Software → Environment properties
   - Add all required environment variables:
     - `DATABASE_URL` (from Supabase)
     - `JWT_SECRET` (generate secure random string)
     - `AWS_REGION` (us-east-1)
     - `AWS_ACCESS_KEY_ID` (from IAM user)
     - `AWS_SECRET_ACCESS_KEY` (from IAM user)
     - `AWS_SES_FROM_EMAIL` (verified email)
     - `AWS_S3_BUCKET_NAME` (bucket name)

4. **Deploy Application**:
   - Upload JAR file or configure GitHub Actions for auto-deployment
   - Wait for deployment to complete (5-10 minutes)
   - Verify health check passes (green status)

5. **Get Backend URL**:
   - Copy the environment URL (e.g., `http://invoiceme-backend-prod.us-east-1.elasticbeanstalk.com`)
   - This will be used for `NEXT_PUBLIC_API_URL` in frontend

### Phase 2: Frontend Deployment (Amplify)

1. **Create Amplify App**:
   - AWS Console → Amplify → New App → Host web app
   - Repository: Connect GitHub repository (`mlx93/invoiceme-mlx`)
   - Branch: `main`
   - Build settings: Use `amplify.yml` configuration file

2. **Configure Environment Variables**:
   - Go to App settings → Environment variables
   - Add: `NEXT_PUBLIC_API_URL` = Backend Elastic Beanstalk URL (from Phase 1)

3. **Deploy Application**:
   - Amplify will automatically build and deploy from GitHub
   - Wait for build to complete (5-10 minutes)
   - Verify deployment successful (green status)

4. **Get Frontend URL**:
   - Copy the app URL (e.g., `https://main.d1234567890.amplifyapp.com`)
   - This is your public frontend URL

### Phase 3: CI/CD Pipeline Setup

1. **Create GitHub Actions Workflow**:
   - Create `.github/workflows/deploy.yml`
   - Configure workflow to trigger on push to `main` branch
   - Add steps for backend and frontend deployment

2. **Backend Deployment Step**:
   - Install AWS CLI or EB CLI
   - Build JAR: `mvn clean package -DskipTests`
   - Deploy to Elastic Beanstalk using AWS CLI or EB CLI
   - Use GitHub secrets for AWS credentials

3. **Frontend Deployment Step**:
   - Amplify auto-deploys from GitHub (no action needed)
   - Or trigger Amplify build via API if needed

4. **Test CI/CD**:
   - Make a small change to code
   - Push to `main` branch
   - Verify GitHub Actions workflow runs
   - Verify deployment succeeds

### Phase 4: Monitoring Setup

1. **CloudWatch Logs**:
   - Elastic Beanstalk automatically streams logs to CloudWatch
   - View logs: AWS Console → CloudWatch → Log groups → `/aws/elasticbeanstalk/invoiceme-backend-prod/var/log/eb-engine.log`
   - Amplify logs: AWS Console → Amplify → App → Deployments → View logs

2. **Health Checks**:
   - Elastic Beanstalk: Configure health check endpoint (`/actuator/health` or `/api/v1/health`)
   - Amplify: Built-in health checks

3. **Custom Metrics** (optional):
   - Add CloudWatch custom metrics in application code
   - Create CloudWatch dashboards for monitoring

---

## Success Criteria

**M3 DevOps Complete When**:
- ✅ Backend deployed to Elastic Beanstalk and accessible
- ✅ Frontend deployed to Amplify and accessible
- ✅ Frontend can connect to backend APIs
- ✅ CI/CD pipeline configured and working (GitHub Actions)
- ✅ Environment variables configured correctly
- ✅ CloudWatch logs accessible
- ✅ Deployment documentation complete
- ✅ Post-deployment verification successful

---

## Report Format

**REPORT BACK WITH**:
- **Summary** (≤15 bullets):
  - Backend deployment status (Elastic Beanstalk URL)
  - Frontend deployment status (Amplify URL)
  - CI/CD pipeline status (GitHub Actions workflow working/not working)
  - Environment variables configured (list)
  - CloudWatch logs accessible (yes/no)
  - Post-deployment verification results (endpoints tested, issues found)
  - Deployment documentation location
  - Monitoring configuration status
- **Artifacts paths**:
  - Deployment config: `/backend/.ebextensions/`, `/amplify.yml`
  - CI/CD pipeline: `.github/workflows/deploy.yml`
  - Documentation: `/docs/deployment.md`, `/docs/monitoring.md`
  - Verification script: `/scripts/verify-deployment.sh`
- **Evidence**:
  - Screenshots of Elastic Beanstalk environment (green status)
  - Screenshots of Amplify app (deployed successfully)
  - Screenshots of GitHub Actions workflow (successful run)
  - Backend URL (accessible)
  - Frontend URL (accessible)
  - API test results (curl commands or Postman)

---

## DO NOT

- Skip environment variables configuration (critical for application to work)
- Skip health check configuration (Elastic Beanstalk needs health check endpoint)
- Skip CI/CD pipeline (mandatory for M3)
- Deploy without testing locally first
- Skip monitoring setup (CloudWatch logs are essential for debugging)

---

## Notes

- **Elastic Beanstalk**: Free tier eligible (first 750 hours/month free)
- **Amplify**: Free tier eligible (first 1000 build minutes/month free)
- **CloudWatch**: Free tier eligible (first 5GB logs/month free)
- **Database**: Supabase free tier (500MB database, 2GB bandwidth)
- **SES**: Free tier eligible (first 62,000 emails/month free in sandbox mode)

---

**Status**: Ready to begin M3 DevOps deployment

