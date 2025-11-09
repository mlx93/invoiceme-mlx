# How to Change Region to us-east-1 in AWS Console

**Problem**: Elastic Beanstalk is defaulting to `us-east-2`  
**Solution**: Change AWS Console region to `us-east-1` before creating environment

---

## Method 1: Change Region in Elastic Beanstalk Console (Easiest)

### Step 1: Change Region Before Creating Environment

1. **Look at the top-right corner** of the AWS Console
2. You should see a **region selector** (shows current region like "us-east-2")
3. **Click on the region selector**
4. **Select `us-east-1`** (N. Virginia)
5. **Wait for the page to reload** (you'll see the region change in the URL)

### Step 2: Verify Region Changed

- Check the **domain field** in the environment creation form
- It should now show: `.us-east-1.elasticbeanstalk.com` (not `.us-east-2`)
- If it still shows `us-east-2`, refresh the page

### Step 3: Proceed with Environment Creation

- Now create your environment
- The domain will be in `us-east-1`

---

## Method 2: Change Region via URL

If the region selector doesn't work:

1. **Check the current URL** - it should have `us-east-2` in it:
   ```
   https://console.aws.amazon.com/elasticbeanstalk/home?region=us-east-2
   ```

2. **Manually change the URL** to:
   ```
   https://console.aws.amazon.com/elasticbeanstalk/home?region=us-east-1
   ```

3. **Press Enter** - page will reload in `us-east-1`

---

## Method 3: Change Region in AWS Console Header

1. **Look at the very top-right** of any AWS Console page
2. You'll see the **region selector** (usually shows something like "us-east-2" or "N. Virginia")
3. **Click it** → Select **"US East (N. Virginia) us-east-1"**
4. **Navigate back to Elastic Beanstalk** → Create environment

---

## Visual Guide

**Region Selector Location:**
```
┌─────────────────────────────────────────────┐
│  AWS Console Header                        │
│  [Services] [Resources] ... [Region ▼]     │
│                                    ↑        │
│                            Click here      │
└─────────────────────────────────────────────┘
```

**After clicking, you'll see:**
- US East (N. Virginia) us-east-1 ← **SELECT THIS**
- US East (Ohio) us-east-2
- US West (Oregon) us-west-2
- ... (other regions)

---

## Verification Checklist

Before clicking "Create environment":

- [ ] Region selector shows: **us-east-1** or **N. Virginia**
- [ ] Domain field shows: `.us-east-1.elasticbeanstalk.com`
- [ ] URL contains: `region=us-east-1`

---

## Important Notes

1. **Region must be changed BEFORE creating the environment**
   - You cannot change the region after environment is created
   - The region is part of the environment's infrastructure

2. **All AWS resources should be in the same region:**
   - Elastic Beanstalk environment: `us-east-1`
   - S3 bucket: `us-east-1` (check your bucket region)
   - SES: `us-east-1` (check your SES region)
   - `AWS_REGION` environment variable: `us-east-1`

3. **If your S3/SES are in `us-east-2`:**
   - You can either:
     - Keep environment in `us-east-2` and change `AWS_REGION=us-east-2`
     - Or move S3/SES to `us-east-1` (more complex)

---

## Quick Fix: If You Already Created in Wrong Region

If you accidentally created the environment in `us-east-2`:

1. **Terminate the environment**
2. **Change region to `us-east-1`** (using methods above)
3. **Create new environment** in `us-east-1`

---

**Next Step**: After changing region, proceed with environment creation as outlined in `RECREATE_ENVIRONMENT_STEPS.md`

