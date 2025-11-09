# Fix Java Version Mismatch

**Problem**: Environment is running Java 8 (Corretto 8), but JAR was compiled with Java 17  
**Error**: `UnsupportedClassVersionError: class file version 61.0` (Java 17) vs `52.0` (Java 8)  
**Solution**: Change platform to Java 17 (Corretto 17)

---

## Step 1: Change Platform Version

1. **Go to your environment**: `invoiceme-mlx-backend-env-1`
2. **Look at the "Platform" card** (right side of Overview page)
3. **Click "Change version" button** (top right of Platform card)
4. **Select**: **"Corretto 17"** → **"Java 17 running on 64bit Amazon Linux 2023"**
5. **Platform version**: Select latest (e.g., "4.7.1 (Recommended)")
6. **Click "Apply"**

**OR**:

1. **Go to Configuration** (left sidebar)
2. **Find "Platform" section**
3. **Click "Edit"**
4. **Platform**: Change to **"Corretto 17"** → **"Java 17 running on 64bit Amazon Linux 2023"**
5. **Click "Apply"**

---

## Step 2: Wait for Platform Update

1. **Monitor "Events" tab** for progress
2. **Wait 10-15 minutes** for platform update to complete
3. **Environment will restart** automatically with Java 17

---

## Step 3: Verify Platform Changed

After update completes:

1. **Check "Platform" card**:
   - Should show: **"Corretto 17 running on 64bit Amazon Linux 2023"**
   - NOT "Corretto 8"

2. **Check "Health" status**:
   - Should change to "Healthy" (green) once Java 17 is running

---

## Step 4: Test Application

Once platform is updated and health is "Healthy":

```bash
curl http://invoiceme-mlx-backend-env-1.eba-f9m4p8pu.us-east-1.elasticbeanstalk.com/actuator/health
```

Expected: `{"status":"UP"}`

---

## Why This Happened

- **Your JAR**: Compiled with Java 17 (class file version 61.0)
- **Environment**: Configured for Java 8 (Corretto 8) - only recognizes up to version 52.0
- **Result**: JAR can't run on Java 8

---

## Quick Steps Summary

1. ✅ **Go to Configuration** → **Platform** → **Edit**
2. ✅ **Change to**: **Corretto 17** → **Java 17 running on 64bit Amazon Linux 2023**
3. ✅ **Click "Apply"**
4. ✅ **Wait 10-15 minutes** for platform update
5. ✅ **Test**: `curl http://[your-url]/actuator/health`

---

**Next Step**: Change the platform to Java 17 (Corretto 17) and wait for the update to complete!

