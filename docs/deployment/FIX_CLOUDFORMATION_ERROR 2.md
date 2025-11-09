# Fix CloudFormation Auto Scaling Group Error

**Error**: `Resource AWSEBAutoScalingGroup does not exist for stack awseb-e-ghyppk5e3w-stack`  
**Problem**: CloudFormation failed to create infrastructure resources  
**Result**: No EC2 instances, environment can't start

---

## Root Cause

The CloudFormation stack failed to create the Auto Scaling Group, which means:
- No EC2 instances were launched
- Application can't run (no servers)
- Environment is stuck in "No Data" state

---

## Solution Options

### Option 1: Check CloudFormation Stack Details (Recommended First)

1. **Go to AWS Console** → **CloudFormation**
2. **Find stack**: `awseb-e-ghyppk5e3w-stack`
3. **Click on the stack**
4. **Go to "Events" tab**
5. **Look for**:
   - CREATE_FAILED events
   - Specific error messages
   - Which resource failed and why

**Common Causes**:
- IAM permissions missing
- Service role doesn't have required permissions
- VPC/network configuration issues
- Region-specific resource limits

### Option 2: Check IAM Roles

The error might be due to missing IAM permissions:

1. **Go to IAM** → **Roles**
2. **Find**: `aws-elasticbeanstalk-service-role`
3. **Check permissions**:
   - Should have: `AWSElasticBeanstalkService`, `AWSElasticBeanstalkManagedUpdatesCustomerRolePolicy`
   - Should have: `CloudWatchFullAccess`, `EC2FullAccess` (or equivalent)

4. **Find**: `aws-elasticbeanstalk-ec2-role`
5. **Check permissions**:
   - Should have: `AWSElasticBeanstalkWebTier`, `AWSElasticBeanstalkWorkerTier`, `AWSElasticBeanstalkMulticontainerDocker`

### Option 3: Terminate and Recreate (If Above Doesn't Work)

If CloudFormation is corrupted, recreate the environment:

1. **Terminate current environment**:
   - Elastic Beanstalk → Environment → Actions → Terminate environment
   - Wait for termination (5-10 minutes)

2. **Create new environment**:
   - Use same application: `invoiceme-mlx-backend`
   - **Important**: Make sure region is `us-east-1`
   - Use same settings, but try:
     - **Single instance** (simpler, fewer resources)
     - **Default VPC** (if you selected custom VPC before)

---

## Quick Fix: Try Single Instance First

The Auto Scaling Group error suggests you might have selected "Load balanced" environment type. Try:

1. **Terminate current environment**
2. **Create new environment** with:
   - **Environment type**: **Single instance** (not Load balanced)
   - **Region**: `us-east-1`
   - **Platform**: Java (Corretto 17)
   - **Upload JAR**: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`

Single instance is simpler and requires fewer CloudFormation resources.

---

## Check CloudFormation Stack Now

**Immediate Action**:

1. **Go to**: https://console.aws.amazon.com/cloudformation/
2. **Find stack**: `awseb-e-ghyppk5e3w-stack`
3. **Click on it** → **Events tab**
4. **Look for CREATE_FAILED events**
5. **Share the specific error message**

This will tell us exactly what went wrong.

---

## Most Likely Issues

1. **IAM Permissions**: Service role or instance profile missing permissions
2. **Environment Type**: Load balanced requires more resources than single instance
3. **VPC Configuration**: Custom VPC might have issues
4. **Region Mismatch**: Resources created in wrong region

---

## Recommended Next Steps

1. ✅ **Check CloudFormation Events** (see what specific resource failed)
2. ✅ **Check IAM Roles** (verify permissions)
3. ✅ **If needed, terminate and recreate** with Single Instance (simpler)

---

**Action**: Check CloudFormation stack Events tab first - share what you find!

