# Verify Security Group Fix - Still Getting "Network Unreachable"

## ⚠️ Still Getting Error

**Error**: `java.net.SocketException: Network is unreachable`

**This means**: The security group rule might not be applied correctly, OR there's a VPC/subnet routing issue.

---

## You Have TWO Security Groups

From your configuration, I can see:
1. `awseb-e-3p5minfpmi-stack-*` (Elastic Beanstalk managed)
2. `AWSEBSecurityGroup-zJUbqTxeEr1y` (Application security group)

**Both security groups need the outbound rule!**

---

## Step 1: Verify Which Security Groups Need the Rule

### Check Both Security Groups

1. **Go to**: EC2 Console → Security Groups
2. **Find BOTH security groups**:
   - `awseb-e-3p5minfpmi-stack-*` (or similar)
   - `AWSEBSecurityGroup-zJUbqTxeEr1y`
3. **For EACH security group**:
   - Click on the security group name
   - Go to **Outbound rules** tab
   - **Check**: Is there a rule for PostgreSQL (port 5432) to 0.0.0.0/0?
   - **If missing**: Add it

---

## Step 2: Add Rule to BOTH Security Groups

### Security Group 1: Elastic Beanstalk Managed

1. **Find**: `awseb-e-3p5minfpmi-stack-*` (or similar name)
2. **Click**: Security group name
3. **Go to**: Outbound rules tab
4. **Click**: Edit outbound rules
5. **Add rule**:
   - Type: **PostgreSQL** (or Custom TCP)
   - Port: **5432**
   - Destination: **0.0.0.0/0**
   - Description: **Allow outbound to Supabase**
6. **Save**: Save rules

### Security Group 2: Application Security Group

1. **Find**: `AWSEBSecurityGroup-zJUbqTxeEr1y`
2. **Click**: Security group name
3. **Go to**: Outbound rules tab
4. **Click**: Edit outbound rules
5. **Add rule**:
   - Type: **PostgreSQL** (or Custom TCP)
   - Port: **5432**
   - Destination: **0.0.0.0/0**
   - Description: **Allow outbound to Supabase**
6. **Save**: Save rules

---

## Step 3: Check VPC/Subnet Configuration

**If security groups are correct but still failing**, check subnet routing:

### Check Instance Subnet

1. **Go to**: EC2 Console → Instances
2. **Find**: Your Elastic Beanstalk instance
3. **Check**: Subnet ID
4. **Go to**: VPC Console → Subnets
5. **Find**: Your subnet
6. **Check**: Route table

### Check Route Table

1. **Go to**: VPC Console → Route Tables
2. **Find**: Route table associated with your subnet
3. **Check**: Routes tab
4. **Verify**: Route to `0.0.0.0/0` via:
   - **Internet Gateway** (for public subnet) ✅
   - **NAT Gateway** (for private subnet) ✅
   - **Missing** (for private subnet without NAT) ❌

**If missing route to 0.0.0.0/0**:
- **Public subnet**: Add route via Internet Gateway
- **Private subnet**: Add NAT Gateway, then route via NAT Gateway

---

## Step 4: Verify Security Group Rule Was Applied

### Check Rule Details

1. **Go to**: EC2 Console → Security Groups
2. **Click**: Each security group
3. **Go to**: Outbound rules tab
4. **Verify**:
   - ✅ Type: PostgreSQL (or Custom TCP)
   - ✅ Port: 5432
   - ✅ Destination: 0.0.0.0/0
   - ✅ Status: Active

---

## Step 5: Restart Environment

**After adding rules to BOTH security groups**:

1. **Go to**: Elastic Beanstalk → Your Environment
2. **Click**: Actions → **Restart app server**
3. **Wait**: 2-3 minutes
4. **Check logs**: `/var/log/web.stdout.log`
5. **Look for**: `Started InvoiceMeApplication` (success)

---

## Common Issues

### Issue 1: Rule Added to Wrong Security Group

**Problem**: Added rule to one security group, but instance uses another

**Fix**: Add rule to **BOTH** security groups

---

### Issue 2: Private Subnet Without NAT Gateway

**Problem**: Instance in private subnet, no route to internet

**Symptoms**: 
- Security group rules are correct
- Still getting "Network is unreachable"

**Fix**:
- Add NAT Gateway to VPC, OR
- Move instance to public subnet

---

### Issue 3: Security Group Rule Not Saved

**Problem**: Added rule but didn't click "Save"

**Fix**: Verify rule is actually saved in Outbound rules tab

---

## Quick Checklist

- [ ] Rule added to **BOTH** security groups
- [ ] Rule type: PostgreSQL (port 5432)
- [ ] Rule destination: 0.0.0.0/0
- [ ] Rule status: Active
- [ ] Subnet has route to 0.0.0.0/0 (Internet Gateway or NAT Gateway)
- [ ] Environment restarted after adding rules
- [ ] Checked logs for `Started InvoiceMeApplication`

---

## Next Steps

1. ✅ **Add rule to BOTH security groups** (most likely fix)
2. ✅ **Check subnet routing** (if still failing)
3. ✅ **Restart environment**
4. ✅ **Check logs** for success

---

**Action**: Add the PostgreSQL outbound rule to **BOTH** security groups, then restart the environment!

