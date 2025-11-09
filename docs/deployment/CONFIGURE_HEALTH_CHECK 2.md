# Configure Health Check - Step by Step

**Environment**: `Invoiceme-mlx-backend-env`  
**Status**: ✅ Environment successfully launched  
**Next Step**: Configure health check endpoint

---

## Step 1: Navigate to Configuration

1. **Go to AWS Console** → **Elastic Beanstalk**
2. **Select your environment**: `Invoiceme-mlx-backend-env`
3. **Click "Configuration"** (left sidebar - should already be selected)

---

## Step 2: Find Health Check Configuration

The health check can be in one of two places depending on your setup:

### Option A: Load Balancer Section (If using Load Balanced)

1. **Scroll down** to find **"Load balancer"** section
2. **Click "Edit"** button
3. **Look for**: "Application health check URL" or "Health check path"
4. **Set value to**: `/actuator/health`
5. **Health check interval**: `30` seconds (default is fine)
6. **Click "Apply"** at the bottom

### Option B: Health Section (If using Single Instance)

1. **Scroll down** to find **"Health"** section
2. **Click "Edit"** button
3. **Look for**: "Application health check URL" or "Health check path"
4. **Set value to**: `/actuator/health`
5. **Click "Apply"** at the bottom

**Note**: If you don't see a "Load balancer" section, you're using a single instance setup, so use the "Health" section instead.

---

## Step 3: Wait for Configuration Update

1. **Monitor the "Events" tab** (left sidebar)
2. **Wait 2-3 minutes** for the configuration to apply
3. **Status should remain "Healthy"** (green)

---

## Step 4: Get Your Backend URL

1. **Go to "Overview" tab** (left sidebar)
2. **Find "Domain"** or **"Environment URL"**
3. **Copy the URL** (e.g., `http://invoiceme-mlx-backend-env.us-east-1.elasticbeanstalk.com`)

**Your Backend URL**: `_________________________________`

---

## Step 5: Test Health Check Endpoint

### Test with curl (Terminal)

```bash
curl http://[your-backend-url]/actuator/health
```

**Replace `[your-backend-url]`** with your actual URL from Step 4.

**Expected Response**:
```json
{"status":"UP"}
```

### Test in Browser

1. **Open browser**
2. **Navigate to**: `http://[your-backend-url]/actuator/health`
3. **Should see**: `{"status":"UP"}`

---

## Troubleshooting

### Health Check Returns 404

**Problem**: Endpoint not found

**Solutions**:
1. **Verify Actuator is enabled**:
   - Check `backend/pom.xml` - should have `spring-boot-starter-actuator` dependency ✅
   - Check `backend/src/main/resources/application.yml` - should have actuator config ✅

2. **Check Security Configuration**:
   - Verify `/actuator/**` is permitted in `SecurityConfig.java` ✅ (already configured)

3. **Wait a bit longer**: Sometimes it takes a few minutes for changes to propagate

### Health Check Returns 500 or Error

**Problem**: Application error

**Solutions**:
1. **Check CloudWatch Logs**:
   - Elastic Beanstalk → Environment → **Logs** → Request logs
   - Look for error messages

2. **Check Environment Variables**:
   - Verify `DATABASE_URL` is correct
   - Verify database is accessible

3. **Check Application Logs**:
   - Look for startup errors
   - Check database connection issues

### Health Check Returns Connection Refused

**Problem**: Application not running

**Solutions**:
1. **Check Environment Status**:
   - Should be "Healthy" (green)
   - If "Degraded" or "Severe", check Events tab

2. **Check Application Logs**:
   - Look for startup failures
   - Check for port conflicts

---

## Additional Health Check Endpoints

Once `/actuator/health` is working, you can also test:

### Info Endpoint
```bash
curl http://[your-backend-url]/actuator/info
```

### Full Health Details (if configured)
```bash
curl http://[your-backend-url]/actuator/health
# Should return: {"status":"UP"}
```

---

## Verification Checklist

- [ ] Health check path configured: `/actuator/health`
- [ ] Configuration applied successfully
- [ ] Environment status: Healthy (green)
- [ ] Health endpoint returns: `{"status":"UP"}`
- [ ] Backend URL copied and saved

---

## Next Steps After Health Check Works

1. ✅ **Test API Endpoints**:
   ```bash
   # Test register endpoint
   curl -X POST http://[your-backend-url]/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"Test123!","fullName":"Test User"}'
   ```

2. ✅ **Deploy Frontend** (next phase):
   - Configure Amplify with backend URL
   - Set `NEXT_PUBLIC_API_URL` environment variable

3. ✅ **Set Up Monitoring**:
   - Configure CloudWatch alarms
   - Set up dashboards

---

**Status**: Ready to configure health check ✅  
**Time Required**: ~5 minutes

