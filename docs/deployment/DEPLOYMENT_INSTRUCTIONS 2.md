# InvoiceMe Deployment Instructions

**Purpose**: Step-by-step guide to deploy InvoiceMe backend and frontend to AWS and visually test the application.

---

## Quick Start: Deploy and Test

### Option 1: Manual Deployment (Recommended for First Time)

#### Step 1: Deploy Backend to Elastic Beanstalk

1. **Build Backend JAR**:
   ```bash
   cd backend
   mvn clean package -DskipTests
   ```
   This creates `target/invoiceme-backend-1.0.0-SNAPSHOT.jar`

2. **Create Elastic Beanstalk Application**:
   - Go to AWS Console → Elastic Beanstalk
   - Click "Create Application"
   - Application name: `invoiceme-backend`
   - Platform: **Java** → **Corretto 17** → **Java 17 running on 64bit Amazon Linux 2023**
   - Application code: **Upload your code** → Choose file → Select `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
   - Click "Create application"

3. **Configure Environment Variables**:
   - Wait for environment to be created (5-10 minutes)
   - Go to Configuration → Software → Environment properties
   - Click "Edit" and add:
     ```
     DATABASE_URL=jdbc:postgresql://[supabase-host]:5432/postgres?user=postgres&password=[password]
     JWT_SECRET=[generate-random-string-32-chars]
     AWS_REGION=us-east-1
     AWS_ACCESS_KEY_ID=[your-aws-access-key]
     AWS_SECRET_ACCESS_KEY=[your-aws-secret-key]
     AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
     AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
     SERVER_PORT=5000
     ```
   - Click "Apply"

4. **Get Backend URL**:
   - After deployment completes, copy the environment URL (e.g., `http://invoiceme-backend-prod.us-east-1.elasticbeanstalk.com`)
   - Test backend: Open `http://[backend-url]/api/v1/health` (or create a simple health endpoint)

#### Step 2: Deploy Frontend to Amplify

1. **Create Amplify App**:
   - Go to AWS Console → Amplify
   - Click "New app" → "Host web app"
   - Repository: **GitHub** → Authorize → Select `mlx93/invoiceme-mlx`
   - Branch: `main`
   - Build settings: **Use existing build settings** (or create `amplify.yml`)

2. **Create `amplify.yml`** (if not exists):
   ```yaml
   version: 1
   frontend:
     phases:
       preBuild:
         commands:
           - npm install
       build:
         commands:
           - npm run build
     artifacts:
       baseDirectory: .next
       files:
         - '**/*'
     cache:
       paths:
         - node_modules/**/*
   ```

3. **Configure Environment Variables**:
   - Go to App settings → Environment variables
   - Add: `NEXT_PUBLIC_API_URL` = `http://[backend-url]/api/v1`
   - Save

4. **Deploy**:
   - Amplify will automatically build and deploy
   - Wait for build to complete (5-10 minutes)
   - Copy the app URL (e.g., `https://main.d1234567890.amplifyapp.com`)

#### Step 3: Test Deployment

1. **Test Backend**:
   ```bash
   curl http://[backend-url]/api/v1/health
   ```

2. **Test Frontend**:
   - Open frontend URL in browser
   - You should see the login page

3. **Test Full Flow**:
   - Register a new user
   - Login
   - Create a customer
   - Create an invoice
   - Record a payment

---

### Option 2: Automated Deployment (CI/CD)

#### Setup GitHub Actions

1. **Create `.github/workflows/deploy.yml`**:
   ```yaml
   name: Deploy to AWS
   
   on:
     push:
       branches: [ main ]
   
   jobs:
     deploy-backend:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v3
         - name: Set up JDK 17
           uses: actions/setup-java@v3
           with:
             java-version: '17'
         - name: Build with Maven
           run: mvn clean package -DskipTests
         - name: Deploy to Elastic Beanstalk
           uses: einaregilsson/beanstalk-deploy@v22
           with:
             aws_access_key: ${{ secrets.AWS_ACCESS_KEY_ID }}
             aws_secret_key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
             application_name: invoiceme-backend
             environment_name: invoiceme-backend-prod
             version_label: ${{ github.sha }}
             region: us-east-1
             deployment_package: target/invoiceme-backend-1.0.0-SNAPSHOT.jar
   
     deploy-frontend:
       runs-on: ubuntu-latest
       steps:
         - uses: actions/checkout@v3
         - name: Trigger Amplify Build
           run: |
             curl -X POST \
               -H "Authorization: Bearer ${{ secrets.AMPLIFY_TOKEN }}" \
               https://amplify.us-east-1.amazonaws.com/apps/[app-id]/branches/main/jobs
   ```

2. **Push to GitHub**:
   ```bash
   git add .github/workflows/deploy.yml
   git commit -m "feat: Add CI/CD pipeline"
   git push origin main
   ```

3. **Verify Deployment**:
   - Check GitHub Actions tab for workflow status
   - Check Elastic Beanstalk for backend deployment
   - Check Amplify for frontend deployment

---

## Visual Testing Guide

### 1. Access Frontend

- Open your Amplify URL in a browser (e.g., `https://main.d1234567890.amplifyapp.com`)
- You should see the InvoiceMe login page

### 2. Register and Login

1. Click "Register" (or go to `/register`)
2. Fill in registration form:
   - Email: `test@example.com`
   - Password: `Test123!`
   - Role: Select `SALES` or `ACCOUNTANT`
3. Click "Register"
4. **Note**: User approval may be required (if User Approval workflow is enabled)
5. Login with your credentials

### 3. Test Core Features

#### Create Customer
1. Navigate to "Customers" → "New Customer"
2. Fill in customer form:
   - Company Name: `Test Company`
   - Email: `customer@example.com`
   - Customer Type: `COMMERCIAL`
   - Address: Fill in address fields
3. Click "Create Customer"
4. Verify customer appears in customer list

#### Create Invoice
1. Navigate to "Invoices" → "New Invoice"
2. Select customer from dropdown
3. Add line items:
   - Description: `Test Service`
   - Quantity: `1`
   - Unit Price: `100.00`
4. Set payment terms: `NET_30`
5. Click "Create Invoice"
6. Verify invoice appears in invoice list with status `DRAFT`

#### Mark Invoice as Sent
1. Open invoice detail page
2. Click "Mark as Sent"
3. Verify status changes to `SENT`
4. Verify invoice sent email notification (check email or logs)

#### Record Payment
1. Open invoice detail page
2. Click "Record Payment"
3. Fill in payment form:
   - Amount: `100.00`
   - Payment Method: `CREDIT_CARD`
   - Transaction ID: `TEST123`
4. Click "Record Payment"
5. Verify invoice status changes to `PAID` (if full payment)
6. Verify payment appears in payments list

### 4. Test Dashboard

1. Navigate to "Dashboard"
2. Verify metrics display:
   - Revenue MTD
   - Outstanding Invoices
   - Overdue Invoices
   - Active Customers
3. Verify charts display:
   - Revenue Trend Chart
   - Invoice Status Pie Chart
   - Aging Report

### 5. Test RBAC

1. Logout and register a new user with `CUSTOMER` role
2. Login as customer
3. Verify:
   - Can only see own invoices
   - Cannot access user management
   - Cannot delete customers
   - Can record payments on own invoices

---

## Troubleshooting

### Backend Not Accessible

1. **Check Elastic Beanstalk Health**:
   - AWS Console → Elastic Beanstalk → Environment → Health
   - Should be "Ok" (green)

2. **Check Logs**:
   - AWS Console → Elastic Beanstalk → Logs → Request Logs
   - Look for errors

3. **Check Environment Variables**:
   - Configuration → Software → Environment properties
   - Verify all variables are set correctly

4. **Check Database Connection**:
   - Verify `DATABASE_URL` is correct
   - Verify Supabase database is accessible

### Frontend Not Loading

1. **Check Amplify Build Logs**:
   - AWS Console → Amplify → App → Deployments → Build logs
   - Look for build errors

2. **Check Environment Variables**:
   - App settings → Environment variables
   - Verify `NEXT_PUBLIC_API_URL` is set correctly

3. **Check Browser Console**:
   - Open browser DevTools → Console
   - Look for JavaScript errors

### API Calls Failing

1. **Check CORS**:
   - Backend needs to allow requests from Amplify domain
   - Add CORS configuration in Spring Boot SecurityConfig

2. **Check API URL**:
   - Verify `NEXT_PUBLIC_API_URL` points to correct backend URL
   - Test backend URL directly: `curl http://[backend-url]/api/v1/customers`

3. **Check Authentication**:
   - Verify JWT token is being sent in requests
   - Check browser Network tab for Authorization header

---

## Quick Reference

### Backend URLs
- **Local**: `http://localhost:8080/api/v1`
- **Elastic Beanstalk**: `http://[environment-url]/api/v1`

### Frontend URLs
- **Local**: `http://localhost:3000`
- **Amplify**: `https://[app-id].amplifyapp.com`

### Environment Variables

**Backend (Elastic Beanstalk)**:
- `DATABASE_URL` - Supabase connection string
- `JWT_SECRET` - Random 32-character string
- `AWS_REGION` - us-east-1
- `AWS_ACCESS_KEY_ID` - From IAM user
- `AWS_SECRET_ACCESS_KEY` - From IAM user
- `AWS_SES_FROM_EMAIL` - Verified email
- `AWS_S3_BUCKET_NAME` - S3 bucket name

**Frontend (Amplify)**:
- `NEXT_PUBLIC_API_URL` - Backend API URL

---

## Next Steps

After successful deployment:
1. ✅ Test all core features (Customer, Invoice, Payment CRUD)
2. ✅ Test RBAC enforcement
3. ✅ Test domain events (check activity feed)
4. ✅ Test performance (API latency, UI page load)
5. ✅ Document any issues found

---

**Status**: Ready for deployment

