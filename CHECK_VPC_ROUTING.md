# Check VPC/Subnet Routing - Network Unreachable

## ✅ Security Groups Are Correct

**Good news**: Your security groups have the correct rules:
- ✅ PostgreSQL (port 5432) outbound rule exists
- ✅ "All traffic" rule also exists (covers everything)

**But you're still getting "Network is unreachable"** - this suggests a **VPC/subnet routing issue**.

---

## 🔴 Most Likely Cause: Private Subnet Without NAT Gateway

**Problem**: Your Elastic Beanstalk instance is in a **private subnet** without a **NAT Gateway**, so it cannot reach the internet (including Supabase).

---

## Step 1: Check Instance Subnet Type

### Find Your Instance

1. **Go to**: EC2 Console → Instances
2. **Find**: Your Elastic Beanstalk instance
   - Look for instance in `Invoiceme-mlx-back-env` environment
   - Or filter by security group: `sg-06cf3dc6466809eed`
3. **Note**: Subnet ID (e.g., `subnet-xxxxx`)

### Check Subnet Type

1. **Go to**: VPC Console → Subnets
2. **Find**: Your subnet (from Step 1)
3. **Check**: Route table tab
4. **Look for**: Route to `0.0.0.0/0`

**What to look for**:
- ✅ Route to `0.0.0.0/0` via **Internet Gateway** = Public subnet (should work)
- ✅ Route to `0.0.0.0/0` via **NAT Gateway** = Private subnet with NAT (should work)
- ❌ **No route to `0.0.0.0/0`** = Private subnet without NAT (WON'T WORK)

---

## Step 2: Check Route Table

1. **Go to**: VPC Console → Route Tables
2. **Find**: Route table associated with your subnet
3. **Click**: Routes tab
4. **Check**: Is there a route to `0.0.0.0/0`?

**If missing route to `0.0.0.0/0`**:
- Your instance cannot reach the internet
- This causes "Network is unreachable" errors

---

## Step 3: Fix Options

### Option A: Add NAT Gateway (For Private Subnet) ✅ Recommended

**If your subnet is private** (no direct internet access):

1. **Create NAT Gateway**:
   - Go to: VPC Console → NAT Gateways
   - Click: Create NAT Gateway
   - **Subnet**: Choose a **public subnet** in your VPC
   - **Elastic IP**: Allocate new Elastic IP
   - **Create**: Create NAT Gateway (wait 2-3 minutes)

2. **Update Route Table**:
   - Go to: VPC Console → Route Tables
   - Find: Route table for your **private subnet**
   - Click: Routes tab → Edit routes
   - **Add route**:
     - Destination: `0.0.0.0/0`
     - Target: Select your NAT Gateway
   - **Save**: Save changes

3. **Restart Environment**:
   - Elastic Beanstalk → Actions → Restart app server

**Cost**: ~$0.045/hour for NAT Gateway (~$32/month)

---

### Option B: Move to Public Subnet (Simpler, but less secure)

**If you want to avoid NAT Gateway costs**:

1. **Find Public Subnet**:
   - Go to: VPC Console → Subnets
   - Look for subnet with route to Internet Gateway
   - Note: Subnet ID

2. **Update Elastic Beanstalk Environment**:
   - Go to: Configuration → Instances
   - Click: Edit
   - **VPC**: Select your VPC
   - **Subnets**: Select **public subnet** (with Internet Gateway route)
   - **Apply**: Apply changes (will recreate environment)

**Note**: This will recreate your environment (downtime)

---

## Step 4: Verify Internet Access

**After fixing routing**:

1. **Restart**: Elastic Beanstalk → Actions → Restart app server
2. **Check logs**: `/var/log/web.stdout.log`
3. **Look for**: `Started InvoiceMeApplication` (success)
4. **Test**: `curl http://[backend-url]/actuator/health`

---

## Quick Diagnosis

### Check Your Subnet

**From EC2 Console**:
1. Find your instance
2. Check "Subnet" column
3. Click subnet ID
4. Check "Route table" tab
5. Look for route to `0.0.0.0/0`

**If route exists**:
- ✅ Routing is correct
- Check other issues (Supabase firewall, etc.)

**If route missing**:
- ❌ **This is your problem**
- Add NAT Gateway or move to public subnet

---

## Summary

| Check | Status | Action |
|-------|--------|--------|
| **Security Groups** | ✅ Correct | No action needed |
| **Subnet Routing** | ❓ **Check this** | Add NAT Gateway or use public subnet |
| **VPC Configuration** | ❓ **Check this** | Verify route table |

---

## Most Likely Fix

**90% chance**: Your instance is in a **private subnet without NAT Gateway**.

**Fix**: Add NAT Gateway to VPC, then update route table.

---

**Action**: Check your subnet's route table - if there's no route to `0.0.0.0/0`, that's your problem! Add a NAT Gateway or move to a public subnet.

