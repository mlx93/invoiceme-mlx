# New Secrets for GitHub and Elastic Beanstalk

## 🔐 New JWT Secret

**Generated**: Fresh secure random value (64 characters)

```
JWT_SECRET=cC9i5QFh9zuE1985BUiHfSXFwHTrahrsmdLtmbQp3f8M6JhvtISHeotisLegcyvA
```

**Length**: 64 characters (URL-safe base64, 48 bytes of entropy)

---

## 🌍 AWS Region

**Current/Recommended**: `us-east-1`

**If you want to change regions**, common options:
- `us-east-1` (N. Virginia) - **Current**
- `us-east-2` (Ohio)
- `us-west-1` (N. California)
- `us-west-2` (Oregon)
- `eu-west-1` (Ireland)
- `ap-southeast-1` (Singapore)

**Note**: Changing regions requires:
1. Creating new Elastic Beanstalk environment in new region
2. Updating S3 bucket region (or creating new bucket)
3. Updating SES region (if needed)
4. Updating all AWS service configurations

**Recommendation**: Keep `us-east-1` unless you have a specific reason to change.

---

## 📋 Where to Update

### GitHub Secrets

1. **Go to**: GitHub Repository → **Settings** → **Secrets and variables** → **Actions**
2. **Update**:
   - `JWT_SECRET` → `cC9i5QFh9zuE1985BUiHfSXFwHTrahrsmdLtmbQp3f8M6JhvtISHeotisLegcyvA`
   - `AWS_REGION` → `us-east-1` (or your chosen region)

### Elastic Beanstalk Environment Variables

1. **Go to**: Elastic Beanstalk → Your Environment → **Configuration** → **Software** → **Edit**
2. **Update**:
   - `JWT_SECRET` → `cC9i5QFh9zuE1985BUiHfSXFwHTrahrsmdLtmbQp3f8M6JhvtISHeotisLegcyvA`
   - `AWS_REGION` → `us-east-1` (or your chosen region)
3. **Click**: **Apply**
4. **Restart**: Actions → Restart app server

---

## ⚠️ Important Notes

### JWT Secret
- **Changing JWT secret invalidates all existing tokens**
- Users will need to log in again after update
- Make sure to update in **both** GitHub and Elastic Beanstalk simultaneously

### AWS Region
- **Changing region requires recreating infrastructure**
- Keep `us-east-1` unless you have specific requirements
- All AWS services (S3, SES, Elastic Beanstalk) should be in the same region

---

## ✅ Complete Updated Values

**For Elastic Beanstalk**:
```
JWT_SECRET=cC9i5QFh9zuE1985BUiHfSXFwHTrahrsmdLtmbQp3f8M6JhvtISHeotisLegcyvA
AWS_REGION=us-east-1
```

**For GitHub Secrets**:
```
JWT_SECRET=cC9i5QFh9zuE1985BUiHfSXFwHTrahrsmdLtmbQp3f8M6JhvtISHeotisLegcyvA
AWS_REGION=us-east-1
```

---

**Action**: Update both GitHub secrets and Elastic Beanstalk environment variables with the new JWT secret!

