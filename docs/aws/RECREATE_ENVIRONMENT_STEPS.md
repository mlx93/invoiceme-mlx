# Recreate Elastic Beanstalk Environment - Step by Step

**Goal**: Create a new environment in `us-east-1` region

---

## Step 1: Terminate Failed Environment (if still exists)

1. Go to **AWS Console** → **Elastic Beanstalk**
2. Select environment: `Invoiceme-mlx-backend-env`
3. Click **Actions** → **Terminate environment**
4. Confirm termination
5. Wait for termination to complete (2-3 minutes)

---

## Step 2: Create New Environment

### 2.1 Navigate to Application

1. In Elastic Beanstalk console, find application: `invoiceme-mlx-backend`
2. Click on the application name
3. Click **Create environment** button (top right)

### 2.2 Configure Environment

**Step 1: Configure environment**
- **Environment name**: `invoiceme-mlx-backend-env` (or `invoiceme-backend-prod`)
- **Domain**: Leave auto-generated (or enter custom domain)
- **Description**: `InvoiceMe Backend Production Environment` (optional)

**Step 2: Configure service access**
- **Service role**: `aws-elasticbeanstalk-service-role` (default - keep this)
- **EC2 instance profile**: `aws-elasticbeanstalk-ec2-role` (default - keep this)
- **EC2 key pair**: Leave as "Choose a key pair" (optional)
- Click **Next**

**Step 3: Set up networking, database, and tags** (Optional)
- **VPC**: Leave default (or configure if needed)
- **Database**: **Skip** (we're using Supabase)
- **Tags**: Optional
- Click **Next** or **Skip to review**

**Step 4: Configure instance traffic and scaling** (Optional)
- **Capacity**: 
  - **Environment type**: **Single instance** (for dev/testing)
  - Or **Load balanced** (for production)
- **Instance type**: `t3.small` (minimum recommended)
- Click **Next** or **Skip to review**

**Step 5: Configure updates, monitoring, and logging** (Optional)
- **Deployment policy**: Rolling updates (default)
- **Health check**: Will be configured later
- **Monitoring**: Enhanced health reporting (default)
- Click **Next** or **Skip to review**

**Step 6: Review**
- **Platform**: Java (Corretto 17) - **VERIFY THIS**
- **Region**: **VERIFY THIS IS `us-east-1`** ⚠️
- **Application code**: Should show your JAR file
- Review all settings
- Click **Create environment**

---

## Step 3: Wait for Environment Creation

1. **Monitor progress** in the **Events** tab
2. **Wait 5-10 minutes** for environment to become healthy
3. **Status should turn green** (Healthy)

**⚠️ IMPORTANT**: Make sure the region shown is **`us-east-1`** before clicking Create!

---

## Step 4: Configure Environment Variables (After Environment is Healthy)

Once the environment shows **green/healthy status**:

1. Go to **Configuration** → **Software** → **Environment properties** → **Edit**
2. Click **Add environment property** for each variable below:

### Copy these values (from `ELASTIC_BEANSTALK_ENV_VALUES.txt`):

```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
JWT_SECRET=lJ5Rz6L8EjrhNazBCeOcu+XCwQmgo+OvkDxafIwDCz8=
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=YOUR_AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY=YOUR_AWS_SECRET_ACCESS_KEY
AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
SERVER_PORT=5000
SPRING_PROFILES_ACTIVE=production
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_INVOICEME=INFO
```

3. Click **Apply** at the bottom
4. Wait for environment to restart (2-3 minutes)

---

## Step 5: Configure Health Check

1. Go to **Configuration** → **Load balancer** (or **Health**) → **Edit**
2. Find **"Application health check URL"** or **"Health check path"**
3. Set to: `/actuator/health`
4. **Health check interval**: `30` seconds
5. Click **Apply**

---

## Step 6: Verify Deployment

1. **Get Backend URL**:
   - Go to **Overview** tab
   - Copy **Environment URL** (e.g., `http://invoiceme-backend-prod.us-east-1.elasticbeanstalk.com`)

2. **Test Health Endpoint**:
   ```bash
   curl http://[your-backend-url]/actuator/health
   ```
   Expected: `{"status":"UP"}`

3. **Test API Endpoint**:
   ```bash
   curl -X POST http://[your-backend-url]/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"Test123!","fullName":"Test User"}'
   ```
   Expected: `201 Created` or `400 Bad Request` (validation error)

---

## Troubleshooting

### Environment Creation Fails Again

1. **Check CloudFormation Events**:
   - Go to **CloudFormation** → Find stack with your environment name
   - Check **Events** tab for specific error

2. **Common Issues**:
   - **IAM permissions**: Verify service role has correct permissions
   - **VPC issues**: Try creating without VPC configuration
   - **Instance type**: Try `t3.micro` if `t3.small` fails (free tier)

3. **Check Logs**:
   - Elastic Beanstalk → Environment → **Logs** → Request logs

### Environment Variables Not Saving

- Make sure environment is **healthy** before adding variables
- Check for typos in variable names
- Verify no special characters causing issues

---

## Quick Checklist

- [ ] Terminated old environment
- [ ] Created new environment in **us-east-1** region
- [ ] Environment shows **green/healthy** status
- [ ] Added all environment variables
- [ ] Configured health check path: `/actuator/health`
- [ ] Tested health endpoint: `/actuator/health`
- [ ] Tested API endpoint: `/api/v1/auth/register`

---

**Total Time**: ~15-20 minutes  
**Status**: Ready to recreate ✅

