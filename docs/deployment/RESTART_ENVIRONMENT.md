# Restart Environment After Adding Environment Variables

**Goal**: Restart the environment so it picks up the new environment variables  
**Status**: Environment variables added, need to restart

---

## Step 1: Restart App Server (Recommended)

This is the quickest way to apply environment variable changes:

1. **Go to your environment**: `invoiceme-mlx-backend-env-1`
2. **Click "Actions" dropdown** (top right, next to "Upload and deploy")
3. **Select "Restart app server(s)"**
4. **Confirm** the restart
5. **Wait 5-10 minutes** for restart to complete

**What this does**:
- Restarts the application with new environment variables
- Faster than full environment restart
- Preserves all configuration

---

## Step 2: Monitor Restart Progress

1. **Go to "Events" tab** (left sidebar)
2. **Watch for**:
   - "Restarting application server"
   - "Successfully restarted application server"
   - Health status changing back to "Healthy"

3. **Check "Health" status**:
   - Should go from "Degraded" → "Warning" → "Healthy" (green)

---

## Alternative: Full Environment Restart

If "Restart app server" doesn't work:

1. **Click "Actions" dropdown**
2. **Select "Restart environment"**
3. **Confirm** the restart
4. **Wait 10-15 minutes** (longer than app server restart)

---

## Step 3: Verify Environment Variables Applied

After restart:

1. **Go to Configuration** → **Software** → **Environment properties**
2. **Verify all variables are still there** (they should be)
3. **Check "Health" status** - should be "Healthy" (green)

---

## Step 4: Test Application

Once health is "Healthy":

1. **Get Backend URL**:
   - Overview tab → Copy "Domain"
   - Example: `http://invoiceme-mlx-backend-env-1.eba-f9m4p8pu.us-east-1.elasticbeanstalk.com`

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

## If Health Still Shows "Degraded"

After restart, if it's still degraded:

1. **Request logs**:
   - Logs tab → Request logs → Last 100 lines
   - Look for startup errors

2. **Check Events tab**:
   - Look for error messages
   - Check if restart completed successfully

3. **Verify environment variables**:
   - Make sure all are set correctly
   - Especially `DATABASE_URL` and `SERVER_PORT=5000`

---

## Quick Steps Summary

1. ✅ **Actions** → **"Restart app server(s)"**
2. ✅ **Wait 5-10 minutes**
3. ✅ **Check Events tab** for progress
4. ✅ **Check Health status** - should turn green
5. ✅ **Test**: `curl http://[your-backend-url]/actuator/health`

---

**Next Step**: Click "Actions" → "Restart app server(s)" and wait for it to complete!

