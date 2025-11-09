# Update Existing Environment with New JAR

**Environment**: `Invoiceme-mlx-backend-env-1`  
**Action**: Upload new JAR file to existing environment

---

## Step 1: Rebuild JAR File

```bash
cd backend
mvn clean package -DskipTests
```

**Output**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`

---

## Step 2: Upload and Deploy to Existing Environment

### Method 1: Using "Upload and deploy" Button (Easiest)

1. **Go to Elastic Beanstalk** → **Environments**
2. **Select your environment**: `Invoiceme-mlx-backend-env-1`
3. **Click "Upload and deploy"** button (top right, orange button)
4. **Select "Local file"**
5. **Click "Choose file"**
6. **Navigate to**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
7. **Version label**: `1.0.0-$(date +%Y%m%d-%H%M%S)` or just `1.0.0-latest`
   - Example: `1.0.0-20251108-1509`
8. **Click "Deploy"**
9. **Wait 5-10 minutes** for deployment to complete

### Method 2: Using Application Versions

1. **Go to Application**: `invoiceme-mlx-backend`
2. **Click "Application versions"** (left sidebar)
3. **Click "Upload"** button
4. **Select "Local file"**
5. **Choose**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
6. **Version label**: `1.0.0-latest` (or any name)
7. **Click "Upload"**
8. **Go back to Environment**: `Invoiceme-mlx-backend-env-1`
9. **Click "Upload and deploy"** → Select the version you just uploaded → **Deploy**

---

## Step 3: Monitor Deployment

1. **Go to "Events" tab** (left sidebar)
2. **Watch for**:
   - "Deploying new version to instances"
   - "Successfully deployed new version"
   - Any error messages

3. **Check "Health" status**:
   - Should remain "Healthy" (green) during deployment
   - If it goes yellow/red, check Events for errors

---

## Step 4: Verify Deployment

After deployment completes:

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

## Important Notes

### Environment Variables Are Preserved

✅ **Good news**: When you upload a new JAR, **all environment variables are preserved**. You don't need to re-enter them!

### Zero Downtime Deployment

- Elastic Beanstalk uses **rolling updates** by default
- Your application stays available during deployment
- New version is deployed gradually

### Version Labels

- Use descriptive version labels: `1.0.0-20251108-1509`
- Helps track which version is deployed
- Can rollback to previous versions if needed

---

## Troubleshooting

### Deployment Fails

1. **Check Events tab** for error messages
2. **Check Logs tab** → Request logs → Look for startup errors
3. **Verify JAR file** is valid (not corrupted)

### Application Won't Start

1. **Check environment variables** are still set:
   - Configuration → Software → Environment properties
2. **Check database connection** (DATABASE_URL)
3. **Check logs** for startup errors

### Rollback to Previous Version

If new version has issues:

1. **Go to Application versions**
2. **Select previous version**
3. **Deploy** that version to your environment

---

## Quick Command Reference

### Rebuild JAR
```bash
cd backend
mvn clean package -DskipTests
```

### Test Health
```bash
curl http://[your-backend-url]/actuator/health
```

### Test API
```bash
curl -X POST http://[your-backend-url]/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!","fullName":"Test User"}'
```

---

## Summary

1. ✅ **Rebuild JAR**: `mvn clean package -DskipTests`
2. ✅ **Upload**: Environment → "Upload and deploy" → Choose JAR file
3. ✅ **Deploy**: Wait for deployment to complete
4. ✅ **Verify**: Test health endpoint

**Environment variables are automatically preserved!** 🎯

