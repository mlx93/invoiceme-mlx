# Test Health Check - It Might Already Be Configured!

**Good News**: Your JAR file includes `.ebextensions/03-healthcheck.config` which automatically configures the health check path to `/actuator/health`.

**For Single Instance Environments**: The health check path is often **not directly editable** in the console - it's configured via `.ebextensions` files (which you already have).

---

## Step 1: Get Your Backend URL

1. **Go to Overview tab** (left sidebar)
2. **Find "Domain"** or **"Environment URL"**
3. **Copy the URL** (e.g., `http://invoiceme-mlx-backend-env.us-east-1.elasticbeanstalk.com`)

**Your Backend URL**: `_________________________________`

---

## Step 2: Test the Health Check Endpoint

### Option A: Test with curl (Terminal)

```bash
curl http://[your-backend-url]/actuator/health
```

**Replace `[your-backend-url]`** with your actual URL.

**Expected Response**:
```json
{"status":"UP"}
```

### Option B: Test in Browser

1. **Open your browser**
2. **Navigate to**: `http://[your-backend-url]/actuator/health`
3. **Should see**: `{"status":"UP"}`

---

## What This Means

### If It Works ✅

- Health check is **already configured** via `.ebextensions`
- **No manual configuration needed!**
- You can proceed to test other API endpoints

### If It Doesn't Work ❌

- Check CloudWatch logs for errors
- Verify the application started successfully
- Check if there are any startup errors

---

## If Health Check Doesn't Work

### Check Application Logs

1. **Go to Logs tab** (left sidebar)
2. **Click "Request logs"** → **"Last 100 lines"**
3. **Look for**:
   - Application startup errors
   - Database connection errors
   - Port conflicts

### Check Environment Status

1. **Go to Overview tab**
2. **Check "Health" status**:
   - **Green/Healthy**: Application is running
   - **Yellow/Degraded**: Some issues
   - **Red/Severe**: Critical problems

### Common Issues

1. **Application not starting**:
   - Check environment variables are correct
   - Check database connection

2. **Port issues**:
   - Verify `SERVER_PORT=5000` is set

3. **Actuator not enabled**:
   - Should be enabled (we added it to `pom.xml`)

---

## Next Steps After Testing

### If Health Check Works:

1. ✅ **Test API Endpoints**:
   ```bash
   # Test register endpoint
   curl -X POST http://[your-backend-url]/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"Test123!","fullName":"Test User"}'
   ```

2. ✅ **Proceed to Frontend Deployment** (Amplify)

### If Health Check Doesn't Work:

1. **Check logs** (see above)
2. **Verify environment variables** are set correctly
3. **Check Events tab** for deployment errors

---

## Why You Can't Find Health Check in Console

**Single Instance Environments**:
- Health check path is typically configured via `.ebextensions` files
- Not always directly editable in the console
- Your `.ebextensions/03-healthcheck.config` should handle this automatically

**Load Balanced Environments**:
- Health check path is editable in "Load balancer" section
- But you're using single instance, so this doesn't apply

---

**Action**: Test the endpoint first - it's likely already working! 🎯

