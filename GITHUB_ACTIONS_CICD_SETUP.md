# GitHub Actions CI/CD Setup for InvoiceMe

## ✅ Configuration Complete

Your GitHub Actions workflow now automatically deploys **both backend and frontend** to AWS Elastic Beanstalk on every push to `main`.

---

## 🔧 What Changed

### Updated `.github/workflows/deploy.yml`

**Before**: Frontend deployed to AWS Amplify (not working with dynamic routes)  
**After**: Frontend deploys to Elastic Beanstalk (full SSR support)

**Key Changes**:
1. ✅ Creates deployment ZIP with correct structure
2. ✅ Deploys to Elastic Beanstalk environment
3. ✅ Uses same AWS credentials as backend
4. ✅ Runs after backend deployment completes

---

## 🎯 GitHub Secrets Required

Make sure these are set in your GitHub repository:

**Go to**: Repository → Settings → Secrets and variables → Actions

### Required Secrets:
```
AWS_ACCESS_KEY_ID          = [Your AWS access key]
AWS_SECRET_ACCESS_KEY      = [Your AWS secret key]
NEXT_PUBLIC_API_URL        = http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1
```

### Check if Secrets Exist:
Go to: https://github.com/mlx93/invoiceme-mlx/settings/secrets/actions

---

## 📝 Verify Environment Name

**IMPORTANT**: Make sure the frontend environment name matches what you created in AWS.

**Current configuration** (line 117 in deploy.yml):
```yaml
environment_name: invoiceme-mlx-frontend-env
```

**If your environment has a different name**, update line 117:
1. Go to Elastic Beanstalk Console
2. Find your frontend environment name
3. Update `deploy.yml` line 117 with the exact name

---

## 🚀 How It Works

### Trigger: Every `git push` to `main`

**Workflow Steps**:

1. **Backend Deployment** (runs first)
   - Checkout code
   - Set up Java 17
   - Run tests (optional)
   - Build JAR
   - Deploy to Elastic Beanstalk
   - Wait for deployment

2. **Frontend Deployment** (runs after backend succeeds)
   - Checkout code
   - Set up Node.js 20
   - Install dependencies
   - Run tests (optional)
   - Build Next.js app
   - Create deployment ZIP (with correct structure)
   - Deploy to Elastic Beanstalk
   - Wait for deployment

---

## 📦 Deployment Flow

```bash
# On your local machine:
git add .
git commit -m "Update frontend/backend"
git push origin main

# GitHub Actions automatically:
# 1. Builds backend JAR
# 2. Deploys to invoiceme-mlx-back-env
# 3. Builds frontend production bundle
# 4. Creates frontend-deploy.zip
# 5. Deploys to invoiceme-mlx-frontend-env
# 6. Both services are updated!
```

---

## 🧪 Testing the Workflow

### Option 1: Push a Change
```bash
# Make a small change
echo "# Test deployment" >> README.md
git add README.md
git commit -m "Test GitHub Actions deployment"
git push origin main
```

### Option 2: Manual Trigger
1. Go to: https://github.com/mlx93/invoiceme-mlx/actions
2. Click "Deploy to AWS"
3. Click "Run workflow"
4. Select `main` branch
5. Click "Run workflow"

---

## 📊 Monitor Deployments

### GitHub Actions
**URL**: https://github.com/mlx93/invoiceme-mlx/actions

You'll see:
- ✅ Green checkmark = Success
- ❌ Red X = Failed (click for logs)
- 🟡 Yellow dot = In progress

### AWS Elastic Beanstalk
**Console**: https://console.aws.amazon.com/elasticbeanstalk

You'll see:
- Backend: `invoiceme-mlx-back-env`
- Frontend: `invoiceme-mlx-frontend-env`

---

## 🐛 Troubleshooting

### Issue 1: Environment Name Mismatch
**Error**: `The environment "invoiceme-mlx-frontend-env" does not exist`

**Solution**: Update line 117 in `deploy.yml` with your actual environment name

### Issue 2: Missing GitHub Secrets
**Error**: `AWS credentials not found`

**Solution**: 
1. Go to: https://github.com/mlx93/invoiceme-mlx/settings/secrets/actions
2. Add `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`

### Issue 3: Deployment Fails
**Error**: Various deployment errors

**Solution**:
1. Check GitHub Actions logs
2. Check CloudWatch logs in AWS
3. Verify environment variables are set in Elastic Beanstalk

### Issue 4: Frontend ZIP Structure Wrong
**Error**: `Instance deployment failed to generate a 'Procfile'`

**Solution**: The workflow already handles this correctly by:
```bash
cd frontend
zip -r ../frontend-deploy.zip .
```

This ensures `package.json` is at the root of the ZIP.

---

## ⚡ Quick Commands

### Force redeploy without code changes:
```bash
git commit --allow-empty -m "Trigger deployment"
git push origin main
```

### Check deployment status:
```bash
# Via AWS CLI
aws elasticbeanstalk describe-environments \
  --application-name invoiceme-mlx \
  --region us-east-1
```

---

## 🎉 Next Steps

1. **Commit this workflow**:
   ```bash
   git add .github/workflows/deploy.yml
   git commit -m "Update GitHub Actions to deploy frontend to Elastic Beanstalk"
   git push origin main
   ```

2. **Watch it deploy**:
   - Go to: https://github.com/mlx93/invoiceme-mlx/actions
   - Watch both backend and frontend deploy

3. **Verify deployment**:
   - Check Elastic Beanstalk console
   - Test frontend URL
   - Verify frontend → backend API calls work

---

## 📋 Deployment Checklist

- [x] GitHub Actions workflow updated
- [ ] Verify frontend environment name matches AWS
- [ ] Verify GitHub Secrets are set
- [ ] Commit and push workflow changes
- [ ] Test automatic deployment
- [ ] Verify both services updated

---

**You're all set!** 🚀 Every push to `main` will now automatically deploy both frontend and backend to AWS Elastic Beanstalk.

