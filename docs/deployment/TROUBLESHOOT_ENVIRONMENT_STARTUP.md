# Troubleshoot Environment Startup Issues

**Problem**: Environment stuck at "No Data" / "Pending" for 20+ minutes  
**Status**: Environment not starting properly

---

## Step 1: Check Events Tab for Errors

1. **Click "Events" tab** (left sidebar or bottom tabs)
2. **Look for**:
   - ❌ Red error messages
   - ⚠️ Yellow warnings
   - Any messages with "Failed" or "Error"

**Common Errors to Look For**:
- "Failed to deploy application"
- "Environment health has transitioned to Severe"
- "Service role" or "Instance profile" errors
- "CloudFormation" errors

**What to do**: Copy any error messages you see

---

## Step 2: Request Logs

The logs might not be showing because they need to be requested:

1. **Click "Logs" tab** (left sidebar)
2. **Click "Request logs"** button (top right)
3. **Select**: "Last 100 lines" or "Full logs"
4. **Click "Request"**
5. **Wait 1-2 minutes** for logs to generate
6. **Click "Download"** when ready
7. **Open the log file** and look for:
   - Application startup errors
   - Database connection errors
   - Port conflicts
   - Missing environment variables

---

## Step 3: Check CloudFormation Stack

1. **Go to AWS Console** → **CloudFormation**
2. **Find stack**: `awseb-e-ghyppk5e3w-stack` (or similar)
3. **Click on the stack**
4. **Go to "Events" tab**
5. **Look for**:
   - ❌ CREATE_FAILED events
   - ⚠️ Any failed resource creation

**Common Issues**:
- IAM role permissions
- VPC/network configuration
- Instance launch failures

---

## Step 4: Check Environment Variables

1. **Go to Configuration** → **Software** → **Environment properties**
2. **Verify all variables are set**:
   - `DATABASE_URL` - Should be set
   - `SERVER_PORT=5000` - Should be set
   - `AWS_ACCESS_KEY_ID` - Should be set
   - All other variables from `ELASTIC_BEANSTALK_ENV_VALUES.txt`

---

## Step 5: Common Issues & Solutions

### Issue 1: Application Not Starting

**Symptoms**: No logs, no health data

**Possible Causes**:
- JAR file corrupted or invalid
- Application startup error
- Missing dependencies

**Solution**:
1. Check logs (Step 2)
2. Verify JAR file was uploaded correctly
3. Try rebuilding and re-uploading JAR

### Issue 2: Database Connection Failed

**Symptoms**: Application starts but crashes immediately

**Solution**:
1. Verify `DATABASE_URL` is correct
2. Check Supabase database is accessible
3. Test connection string locally

### Issue 3: Port Conflict

**Symptoms**: Application can't bind to port

**Solution**:
1. Verify `SERVER_PORT=5000` is set
2. Check if port is already in use

### Issue 4: IAM Permissions

**Symptoms**: CloudFormation stack fails

**Solution**:
1. Check service role permissions
2. Verify EC2 instance profile has correct permissions

---

## Step 6: Try Rebuilding Environment

If nothing works, try recreating:

1. **Terminate current environment**
2. **Create new environment** with same settings
3. **Make sure region is `us-east-1`**
4. **Upload JAR file again**

---

## Quick Diagnostic Commands

### Check if Environment Has URL (Even if Not Shown)

Try accessing directly:
```bash
# Try this URL pattern
curl http://invoiceme-mlx-backend-env.us-east-1.elasticbeanstalk.com/actuator/health
```

### Check CloudWatch Logs Directly

1. **Go to CloudWatch** → **Log groups**
2. **Look for**: `/aws/elasticbeanstalk/invoiceme-mlx-backend-env/var/log/eb-engine.log`
3. **View recent log events**

---

## What to Share for Help

If you need help, share:

1. **Events tab**: Any error messages
2. **Logs**: Last 50 lines of application logs
3. **CloudFormation Events**: Any CREATE_FAILED events
4. **Environment Variables**: Screenshot of Software configuration

---

## Most Likely Issues

Based on "No Data" and "No Logs":

1. **Application not starting** - Check logs for startup errors
2. **JAR file issue** - Verify JAR was uploaded correctly
3. **Environment variables missing** - Verify all vars are set
4. **Database connection** - Check DATABASE_URL is correct

---

**Next Step**: Check Events tab first, then request logs. Share what you find!

