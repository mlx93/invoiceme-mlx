# Upload Latest Backend JAR to Elastic Beanstalk

**JAR File**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`  
**Status**: ✅ **BUILT SUCCESSFULLY** (includes latest backend changes)

---

## Quick Upload Steps

### Step 1: Locate the JAR File

**File Path**:
```
/Users/mylessjs/Desktop/InvoiceMe/backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar
```

**Or relative from project root**:
```
backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar
```

### Step 2: Upload to Elastic Beanstalk

#### Option A: During Environment Creation (New Environment)

1. **Go to AWS Console** → **Elastic Beanstalk**
2. **Change region to `us-east-1`** (top-right corner)
3. **Create environment** (or go to your application)
4. **Step 1: Configure environment**
   - **Application code**: Select **"Upload your code"**
   - **Source code origin**: Select **"Local file"**
   - **Click "Choose file"**
   - **Navigate to**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
   - **Select the file**
   - You should see: ✅ `File name: invoiceme-backend-1.0.0-SNAPSHOT.jar`
5. **Continue with environment creation**

#### Option B: Upload to Existing Environment

1. **Go to AWS Console** → **Elastic Beanstalk**
2. **Select your environment**: `Invoiceme-mlx-backend-env`
3. **Click "Upload and deploy"** button (top-right)
4. **Select "Local file"**
5. **Click "Choose file"**
6. **Navigate to**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
7. **Version label**: `1.0.0-$(date +%Y%m%d-%H%M%S)` or just `1.0.0-latest`
8. **Click "Deploy"**
9. **Wait for deployment** (5-10 minutes)

---

## File Details

- **File Name**: `invoiceme-backend-1.0.0-SNAPSHOT.jar`
- **Location**: `backend/target/`
- **Type**: Spring Boot executable JAR (includes all dependencies)
- **Build Date**: Latest build (includes all recent changes)
- **Includes**:
  - ✅ Spring Boot Actuator (health checks)
  - ✅ All backend code changes
  - ✅ All dependencies bundled

---

## Verification

After uploading, verify the deployment:

1. **Check Environment Status**: Should show "Healthy" (green)
2. **Test Health Endpoint**:
   ```bash
   curl http://[your-backend-url]/actuator/health
   ```
   Expected: `{"status":"UP"}`

3. **Check Logs** (if issues):
   - Elastic Beanstalk → Environment → **Logs** → Request logs

---

## Troubleshooting

### File Too Large
- **Max size**: 500 MB
- **Current size**: Check with `ls -lh backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
- If too large, check for unnecessary dependencies in `pom.xml`

### Upload Fails
- **Check file permissions**: File should be readable
- **Check file path**: Make sure you're selecting the correct file
- **Try again**: Sometimes AWS has temporary issues

### Deployment Fails
- **Check CloudWatch Logs**: Look for startup errors
- **Verify environment variables**: Make sure all required vars are set
- **Check health check**: Ensure `/actuator/health` is configured

---

## Next Steps After Upload

1. ✅ **Wait for deployment** (5-10 minutes)
2. ✅ **Verify environment is healthy** (green status)
3. ✅ **Add environment variables** (if not already added)
4. ✅ **Configure health check**: `/actuator/health`
5. ✅ **Test endpoints**

---

**Ready to Upload**: ✅  
**File Location**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`

