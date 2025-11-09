# Create New Environment: Invoiceme-mlx-backend-env-1

**Environment Name**: `Invoiceme-mlx-backend-env-1`  
**Status**: Ready to create

---

## Step 1: Build JAR File

```bash
cd backend
mvn clean package -DskipTests
```

**Output**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`

---

## Step 2: Release Unused Elastic IPs (If Not Done)

1. **Go to EC2** → **Elastic IPs** (in `us-east-1`)
2. **Release the 3 unused ones** (ones with no associated instance ID)
3. **Keep 2 free slots** for Elastic Beanstalk

---

## Step 3: Terminate Old Environment (If Still Exists)

1. **Go to Elastic Beanstalk** → **Environments**
2. **Select**: `Invoiceme-mlx-backend-env` (the failed one)
3. **Actions** → **Terminate environment**
4. **Wait 5-10 minutes** for termination

---

## Step 4: Create New Environment

### 4.1 Navigate to Application

1. **Go to Elastic Beanstalk** → **Applications**
2. **Click**: `invoiceme-mlx-backend`
3. **Click "Create environment"** (top right)

### 4.2 Configure Environment

**Step 1: Configure environment**
- **Environment name**: `Invoiceme-mlx-backend-env-1`
- **Domain**: Leave blank (auto-generated)
- **Description**: "InvoiceMe Backend Production Environment" (optional)

**Step 2: Configure service access**
- **Service role**: `aws-elasticbeanstalk-service-role` (default - keep)
- **EC2 instance profile**: `aws-elasticbeanstalk-ec2-role` (default - keep)
- **EC2 key pair**: Leave as "Choose a key pair" (optional)
- **Click "Next"**

**Step 3: Set up networking, database, and tags**
- **VPC**: Leave default (or configure if needed)
- **Database**: **Skip** (using Supabase)
- **Tags**: Optional
- **Click "Next"** or **"Skip to review"**

**Step 4: Configure instance traffic and scaling**
- **Environment type**: **Single instance** ⚠️ **IMPORTANT** (not Load balanced)
- **Instance type**: `t3.small` (or `t3.micro` for free tier)
- **Click "Next"** or **"Skip to review"**

**Step 5: Configure updates, monitoring, and logging**
- **Deployment policy**: Rolling updates (default)
- **Monitoring**: Enhanced health reporting (default)
- **Click "Next"** or **"Skip to review"**

**Step 6: Review**
- **Platform**: Java (Corretto 17) - **VERIFY**
- **Region**: **VERIFY THIS IS `us-east-1`** ⚠️ **CRITICAL**
- **Application code**: Upload your code → Select `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
- **Review all settings**
- **Click "Create environment"**

---

## Step 5: Wait for Environment Creation

1. **Monitor "Events" tab** for progress
2. **Wait 5-10 minutes** for environment to become healthy
3. **Status should turn green** (Healthy)

---

## Step 6: Add Environment Variables

Once environment is **healthy (green)**:

1. **Go to Configuration** → **Software** → **Environment properties** → **Edit**
2. **Add all variables** from `ELASTIC_BEANSTALK_ENV_VALUES.txt`:

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

3. **Click "Apply"** (environment will restart)

---

## Step 7: Verify Deployment

1. **Get Backend URL**:
   - Overview tab → Copy "Domain" or "Environment URL"

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

---

## Important Reminders

- ✅ **Region**: Must be `us-east-1` (check before creating!)
- ✅ **Environment type**: **Single instance** (not Load balanced)
- ✅ **JAR file**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
- ✅ **Environment variables**: Copy from `ELASTIC_BEANSTALK_ENV_VALUES.txt`

---

## Quick Checklist

- [ ] JAR file built: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
- [ ] Unused Elastic IPs released (3 of them)
- [ ] Old environment terminated (if exists)
- [ ] New environment created in `us-east-1`
- [ ] Environment type: **Single instance**
- [ ] Environment is healthy (green)
- [ ] Environment variables added
- [ ] Health check tested: `/actuator/health`

---

**Ready to create!** 🚀

