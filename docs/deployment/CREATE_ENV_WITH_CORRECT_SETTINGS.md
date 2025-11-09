# Create Environment with Correct Settings

**Current Status**: Platform is correct ✅  
**Action Needed**: Change application code option

---

## ✅ Platform Settings (CORRECT!)

- **Platform**: Java ✅
- **Platform branch**: **Corretto 17 running on 64bit Amazon Linux 2023** ✅✅✅
- **Platform version**: 4.7.1 (Recommended) ✅

**This is perfect!** Corretto 17 = Java 17, which matches your JAR file.

---

## ⚠️ Application Code (NEEDS CHANGE)

**Current**: "Sample application" is selected  
**Needed**: "Upload your code" must be selected

### Step 1: Select "Upload your code"

1. **Click the radio button** for **"Upload your code"**
   - Description: "Upload a source bundle from your computer or copy one from Amazon S3."

### Step 2: Upload Your JAR File

After selecting "Upload your code", you'll see options:

**Option A: Upload from Computer**
1. **Click "Choose file"** or **"Browse"**
2. **Navigate to**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
3. **Select the JAR file**
4. **Upload**

**Option B: Upload from S3** (if you've uploaded before)
1. **Select "Copy from S3"**
2. **Choose existing version** if available

---

## Complete Configuration Checklist

### Platform ✅
- [x] Platform: Java
- [x] Platform branch: Corretto 17 running on 64bit Amazon Linux 2023
- [x] Platform version: 4.7.1 (Recommended)

### Application Code ⚠️
- [ ] **Select "Upload your code"** (NOT "Sample application")
- [ ] **Upload**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`

### Environment Settings
- [ ] Environment name: `invoiceme-mlx-backend-env-2` (or your choice)
- [ ] Region: `us-east-1` (verify!)
- [ ] Environment type: **Single instance** (for simplicity)

### After Creation
- [ ] Add environment variables (from `ELASTIC_BEANSTALK_ENV_VALUES.txt`)
- [ ] Configure health check: `/actuator/health`
- [ ] Test health endpoint

---

## Next Steps After Uploading JAR

1. **Continue through wizard** (use defaults for other settings)
2. **Create environment** (wait 10-15 minutes)
3. **Add environment variables**:
   - Configuration → Software → Environment properties
   - Add all from `ELASTIC_BEANSTALK_ENV_VALUES.txt`
4. **Test**:
   ```bash
   curl http://[new-environment-url]/actuator/health
   ```

---

**Action**: Click **"Upload your code"** radio button and upload your JAR file!

