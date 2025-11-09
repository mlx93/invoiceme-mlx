# Fix Elastic IP Address Limit Error

**Error**: "The maximum number of addresses has been reached"  
**Problem**: AWS account has hit the Elastic IP limit (default: 5 per region)  
**Solution**: Release unused Elastic IPs or request limit increase

---

## Step 1: Check Current Elastic IPs

1. **Go to AWS Console** → **EC2**
2. **Make sure region is `us-east-1`** (top-right corner)
3. **Click "Elastic IPs"** (left sidebar, under "Network & Security")
4. **You'll see a list of all Elastic IPs**

---

## Step 2: Release Unused Elastic IPs

Look for Elastic IPs that are:
- **Not associated** with any instance (shows "Not associated" in "Associated instance" column)
- **Associated with stopped/terminated instances**
- **Old/unused** Elastic IPs from previous environments

**To Release**:
1. **Select the unused Elastic IP** (checkbox)
2. **Click "Actions"** → **"Release Elastic IP addresses"**
3. **Confirm** the release
4. **Repeat** for all unused Elastic IPs

**⚠️ Warning**: Only release Elastic IPs that are truly unused. If you release one that's in use, you'll lose that IP address.

---

## Step 3: Retry Environment Creation

After releasing unused Elastic IPs:

### Option A: Retry Current Stack

1. **Go back to CloudFormation** → Stack: `awseb-e-ghyppk5e3w-stack`
2. **Click "Retry"** button (in the yellow warning banner)
3. **Wait for stack to retry** (5-10 minutes)

### Option B: Terminate and Recreate (Recommended)

Since the stack is in CREATE_FAILED state, it's cleaner to start fresh:

1. **Terminate current environment**:
   - Elastic Beanstalk → Environment → Actions → Terminate environment
   - Wait for termination (5-10 minutes)

2. **Create new environment**:
   - Same application: `invoiceme-mlx-backend`
   - **Region**: `us-east-1`
   - **Environment type**: **Single instance** (uses fewer resources, might not need Elastic IP)
   - Upload JAR and configure as before

---

## Alternative: Use Single Instance (Might Not Need Elastic IP)

**Single instance environments** often don't require Elastic IPs. Try:

1. **Terminate current environment**
2. **Create new environment** with:
   - **Environment type**: **Single instance** (not Load balanced)
   - This might avoid the Elastic IP requirement entirely

---

## Option 2: Request Limit Increase (If You Need More)

If you need to keep all your Elastic IPs:

1. **Go to AWS Support Center**
2. **Create a support case**
3. **Request**: Increase Elastic IP limit for `us-east-1`
4. **Wait for approval** (usually quick, but can take time)

**Note**: This is usually not necessary - most people have unused Elastic IPs they can release.

---

## Quick Steps Summary

1. ✅ **Go to EC2** → **Elastic IPs** (in `us-east-1` region)
2. ✅ **Find unused Elastic IPs** (not associated with running instances)
3. ✅ **Release unused ones** (Actions → Release)
4. ✅ **Retry CloudFormation stack** OR **Terminate and recreate environment**

---

## How Many Elastic IPs Do You Have?

**Check**:
- EC2 → Elastic IPs → Count how many you see
- If you have 5 or more, you've hit the limit
- Release any that show "Not associated"

---

## Recommended Approach

**Best Solution**: Release unused Elastic IPs + Use Single Instance environment

1. **Release unused Elastic IPs** (Step 2 above)
2. **Terminate current environment**
3. **Create new environment** with **Single instance** type
4. This avoids Elastic IP requirement and is simpler overall

---

**Next Step**: Go to EC2 → Elastic IPs and see how many you have. Release any unused ones!

