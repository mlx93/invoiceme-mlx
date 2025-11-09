# Fix GitHub Actions Deployment Failure

## Common Issues

### Issue 1: Environment Name Mismatch ⚠️ **MOST LIKELY**

**Problem**: Workflow uses `invoiceme-backend-prod` but your actual environment might be named differently.

**Check**: What is your actual Elastic Beanstalk environment name?
- `invoiceme-mlx-back-env`?
- `Invoiceme-mlx-backend-env-1`?
- Something else?

**Fix**: Update `.github/workflows/deploy.yml` line 12:
```yaml
ELASTIC_BEANSTALK_ENV_NAME: invoiceme-mlx-back-env  # Use your actual environment name
```

---

### Issue 2: Application Name Mismatch

**Problem**: Workflow uses `invoiceme-backend` but your actual application might be named differently.

**Check**: What is your actual Elastic Beanstalk application name?
- `invoiceme-mlx-back`?
- `invoiceme-backend`?
- Something else?

**Fix**: Update `.github/workflows/deploy.yml` line 11:
```yaml
ELASTIC_BEANSTALK_APP_NAME: invoiceme-mlx-back  # Use your actual application name
```

---

### Issue 3: JAR File Not Found

**Problem**: Build creates JAR but path is wrong.

**Check**: After build, verify JAR exists:
```bash
ls -la backend/target/invoiceme-backend-2.0.0.jar
```

**Fix**: If JAR name is different, update line 57 in workflow.

---

### Issue 4: AWS Credentials

**Problem**: GitHub secrets not set or incorrect.

**Check**: GitHub → Settings → Secrets and variables → Actions
- `AWS_ACCESS_KEY_ID` ✅
- `AWS_SECRET_ACCESS_KEY` ✅

**Fix**: Verify secrets are correct and match your AWS IAM user.

---

### Issue 5: Elastic Beanstalk App/Environment Doesn't Exist

**Problem**: Trying to deploy to non-existent app/environment.

**Fix**: Create the app/environment manually in AWS Console first, then update workflow with correct names.

---

## Step-by-Step Fix

### Step 1: Check GitHub Actions Logs

1. **Go to**: GitHub → **Actions** tab
2. **Click**: Failed workflow run
3. **Click**: "Deploy Backend to Elastic Beanstalk" job
4. **Expand**: "Deploy to Elastic Beanstalk" step
5. **Look for**: Error message (e.g., "Environment not found", "Application not found", "File not found")

---

### Step 2: Verify Elastic Beanstalk Names

1. **Go to**: AWS Console → Elastic Beanstalk
2. **Note**:
   - **Application name**: (e.g., `invoiceme-mlx-back`)
   - **Environment name**: (e.g., `invoiceme-mlx-back-env`)

---

### Step 3: Update Workflow File

**Update `.github/workflows/deploy.yml`**:

```yaml
env:
  AWS_REGION: us-east-1
  ELASTIC_BEANSTALK_APP_NAME: invoiceme-mlx-back  # ← Your actual app name
  ELASTIC_BEANSTALK_ENV_NAME: invoiceme-mlx-back-env  # ← Your actual env name
```

---

### Step 4: Verify JAR Path

**Check line 57**:
```yaml
deployment_package: backend/target/invoiceme-backend-2.0.0.jar
```

**Verify**: This matches your actual JAR filename.

---

### Step 5: Test Locally

**Before pushing, verify build works**:
```bash
cd backend
mvn clean package -DskipTests
ls -la target/invoiceme-backend-2.0.0.jar
```

---

## Quick Fix Checklist

- [ ] Check GitHub Actions logs for exact error
- [ ] Verify Elastic Beanstalk application name matches workflow
- [ ] Verify Elastic Beanstalk environment name matches workflow
- [ ] Verify JAR file path is correct (`invoiceme-backend-2.0.0.jar`)
- [ ] Verify AWS credentials in GitHub secrets
- [ ] Update workflow file with correct names
- [ ] Commit and push changes
- [ ] Monitor new workflow run

---

## Most Common Fix

**90% of the time**, it's an environment name mismatch:

1. **Check your actual environment name** in AWS Console
2. **Update** `.github/workflows/deploy.yml` line 12
3. **Commit and push**

---

**Next Step**: Check the GitHub Actions logs to see the exact error message, then update the workflow file with the correct environment name!

