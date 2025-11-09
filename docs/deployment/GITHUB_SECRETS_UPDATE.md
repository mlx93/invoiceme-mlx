# GitHub Secrets Update Guide

## Current Status

Looking at your GitHub Actions workflow (`.github/workflows/deploy.yml`), the workflow **doesn't directly use DATABASE_URL** - it only deploys the JAR to Elastic Beanstalk, where environment variables are set separately.

However, **DATABASE_URL is listed in your GitHub secrets**, and it should be updated to match the correct format for consistency and potential future use (like running integration tests).

---

## ✅ Secrets That Need Updating

### 1. DATABASE_URL (Update Required)

**Current (likely wrong format)**:
```
jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa
```

**✅ CORRECT Format** (update to this):
```
jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
```

**Why**: Spring Boot expects username/password as separate environment variables, not embedded in the URL.

---

### 2. Optional: Add DB_USERNAME and DB_PASSWORD (If Needed)

If you plan to run tests in GitHub Actions that need database access, add these secrets:

- **DB_USERNAME**: `postgres`
- **DB_PASSWORD**: `invoicemesupa`

**Note**: Currently not needed since your workflow doesn't run database tests.

---

## How to Update GitHub Secrets

### Step 1: Update DATABASE_URL Secret

1. **Go to**: GitHub Repository → **Settings** → **Secrets and variables** → **Actions**
2. **Find**: `DATABASE_URL` in the list
3. **Click**: Pencil icon (Edit)
4. **Update value to**:
   ```
   jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
   ```
5. **Click**: **Update secret**

---

### Step 2: Verify Other Secrets (Optional)

**Check these secrets are correct**:

- ✅ `AWS_ACCESS_KEY_ID` - Should match your AWS IAM user
- ✅ `AWS_SECRET_ACCESS_KEY` - Should match your AWS IAM user
- ✅ `AWS_REGION` - Should be `us-east-1`
- ✅ `AWS_S3_BUCKET_NAME` - Should be `invoiceme-pdfs-mlx`
- ✅ `AWS_SES_FROM_EMAIL` - Should be `mylesethan93@gmail.com`
- ✅ `JWT_SECRET` - Should match your JWT secret

**These don't need updating** unless values have changed.

---

## Current GitHub Secrets List

Based on your repository, you have:

1. ✅ `AWS_ACCESS_KEY_ID`
2. ✅ `AWS_REGION`
3. ✅ `AWS_S3_BUCKET_NAME`
4. ✅ `AWS_SECRET_ACCESS_KEY`
5. ✅ `AWS_SES_FROM_EMAIL`
6. ⚠️ `DATABASE_URL` - **NEEDS UPDATE** (remove username/password from URL)
7. ✅ `JWT_SECRET`

**Missing** (optional, only if running DB tests):
- `DB_USERNAME` (optional)
- `DB_PASSWORD` (optional)

---

## Why Update DATABASE_URL?

Even though your workflow doesn't use it directly:

1. **Consistency**: Matches Elastic Beanstalk format
2. **Future-proof**: If you add database tests to CI/CD
3. **Documentation**: Shows correct format for reference
4. **Best practice**: Keep secrets in sync across environments

---

## Summary

**Action Required**:
- ✅ **Update `DATABASE_URL`** to: `jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres`
- ❌ **Remove** username/password from URL (they're not needed in GitHub secrets since workflow doesn't use DB)

**No Action Needed**:
- Other secrets look fine (unless values have changed)

---

**Next Step**: Update the `DATABASE_URL` secret in GitHub to match the correct format!

