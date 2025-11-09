# Fix Node.js Version for Next.js

## Problem

**Error**: `You are using Node.js 18.20.8. For Next.js, Node.js version ">=20.9.0" is required.`

**Cause**: GitHub Actions workflow and Amplify are using Node.js 18, but Next.js requires Node.js 20+.

---

## ✅ Fixed

### 1. GitHub Actions Workflow

**Updated**: `.github/workflows/deploy.yml`
- Changed: `node-version: '18'` → `node-version: '20'`

### 2. Amplify Configuration

**Updated**: `amplify.yml`
- Added: `nvm use 20` in preBuild phase

---

## What Was Changed

### GitHub Actions (`.github/workflows/deploy.yml`)

**Before**:
```yaml
- name: Set up Node.js
  uses: actions/setup-node@v4
  with:
    node-version: '18'
```

**After**:
```yaml
- name: Set up Node.js
  uses: actions/setup-node@v4
  with:
    node-version: '20'
```

### Amplify (`amplify.yml`)

**Before**:
```yaml
preBuild:
  commands:
    - cd frontend
    - npm ci
```

**After**:
```yaml
preBuild:
  commands:
    - cd frontend
    - nvm use 20
    - npm ci
```

---

## Next Steps

1. ✅ **Commit and push** the changes:
   ```bash
   git add .github/workflows/deploy.yml amplify.yml
   git commit -m "Fix Node.js version: Update to Node.js 20 for Next.js compatibility"
   git push
   ```

2. ✅ **Monitor GitHub Actions**:
   - Go to: GitHub → Actions
   - Watch the new workflow run
   - Frontend build should now succeed

3. ✅ **Verify Amplify** (if deploying to Amplify):
   - Amplify will use Node.js 20 automatically
   - Build should succeed

---

## Why This Happened

**Next.js 14+ requires Node.js 20+**:
- Node.js 18 is no longer supported
- Node.js 20 is the minimum required version
- This is a breaking change in Next.js

---

## Summary

- ✅ **GitHub Actions**: Updated to Node.js 20
- ✅ **Amplify**: Updated to use Node.js 20
- ✅ **Ready to deploy**: Commit and push to trigger new build

**Action**: Commit and push the changes, then the frontend build should succeed!

