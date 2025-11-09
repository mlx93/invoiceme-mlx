# Root Cause: Network Unreachable - Database Connection

## 🔴 Root Cause Identified

**Error**: `java.net.SocketException: Network is unreachable`

**Meaning**: The Elastic Beanstalk instance **cannot reach** the Supabase database server.

**This is a NETWORK/FIREWALL issue**, not a configuration issue.

---

## What's Happening

1. ✅ **Spring Boot app starts** (port 5000 configured correctly)
2. ✅ **Tomcat starts** (`Tomcat initialized with port 5000`)
3. ✅ **Configuration is correct** (database URL, credentials)
4. ❌ **Cannot connect to database** (`Network is unreachable`)
5. ❌ **App crashes** during Flyway migration
6. ❌ **Nothing listening on port 5000** (app crashed)
7. ❌ **Nginx gets 502** (`Connection refused`)

---

## The Real Problem

**Network connectivity blocked**:
- Elastic Beanstalk instance cannot reach `db.rhyariaxwllotjiuchhz.supabase.co:5432`
- This is a **firewall/security group** issue, not a config issue

---

## Possible Causes

### 1. Security Group Blocking Outbound Traffic ⚠️ **MOST LIKELY**

**Problem**: Elastic Beanstalk security group doesn't allow outbound connections to PostgreSQL (port 5432)

**Fix**:
1. **Go to**: EC2 Console → Security Groups
2. **Find**: Security group attached to your Elastic Beanstalk instance
3. **Check**: Outbound rules
4. **Add rule** (if missing):
   - Type: PostgreSQL (or Custom TCP)
   - Port: 5432
   - Destination: 0.0.0.0/0 (or Supabase IP range)
   - Description: Allow outbound to Supabase

---

### 2. VPC/Subnet Configuration

**Problem**: Instance is in a private subnet without NAT Gateway

**Check**:
1. **Go to**: EC2 Console → Instances
2. **Find**: Your Elastic Beanstalk instance
3. **Check**: Subnet type (public vs private)
4. **If private subnet**: Needs NAT Gateway for internet access

**Fix**: 
- Move to public subnet, OR
- Add NAT Gateway to VPC

---

### 3. Supabase Firewall Blocking

**Problem**: Supabase firewall blocking Elastic Beanstalk instance IP

**Check**:
1. **Go to**: Supabase Dashboard → Settings → Database
2. **Check**: Connection pooling / IP restrictions
3. **Verify**: No IP whitelist blocking your instance

**Fix**:
- Add Elastic Beanstalk instance IP to Supabase allowlist, OR
- Disable IP restrictions in Supabase

---

### 4. Network ACLs Blocking Traffic

**Problem**: Network ACLs blocking outbound traffic

**Check**:
1. **Go to**: VPC Console → Network ACLs
2. **Find**: ACL associated with your subnet
3. **Check**: Outbound rules allow port 5432

---

## Step-by-Step Diagnosis

### Step 1: Check Security Group Outbound Rules

1. **Go to**: EC2 Console → Security Groups
2. **Find**: Security group used by Elastic Beanstalk
   - Look for: `awseb-e-*` or check Elastic Beanstalk environment → Configuration → Instances → Security groups
3. **Check**: Outbound rules tab
4. **Verify**: Rule allowing port 5432 outbound

**If missing**, add:
- Type: PostgreSQL (or Custom TCP)
- Port: 5432
- Destination: 0.0.0.0/0
- Description: Allow outbound to Supabase

---

### Step 2: Test Connectivity from Instance

**If SSH is enabled** (or use AWS Systems Manager Session Manager):

```bash
# Test if instance can reach Supabase
nc -zv db.rhyariaxwllotjiuchhz.supabase.co 5432

# Or with telnet
telnet db.rhyariaxwllotjiuchhz.supabase.co 5432
```

**If connection fails**: Security group or network issue
**If connection succeeds**: Configuration issue (check credentials)

---

### Step 3: Check Instance Subnet

1. **Go to**: EC2 Console → Instances
2. **Find**: Your Elastic Beanstalk instance
3. **Check**: Subnet ID
4. **Go to**: VPC Console → Subnets
5. **Check**: Route table for subnet
6. **Verify**: Route to `0.0.0.0/0` via Internet Gateway (for public subnet) or NAT Gateway (for private subnet)

---

### Step 4: Check Supabase Settings

1. **Go to**: Supabase Dashboard → Settings → Database
2. **Check**: 
   - Connection pooling settings
   - IP restrictions / firewall rules
   - Database status (not paused)

---

## Quick Fix (Most Likely Solution)

### Fix Security Group Outbound Rules

1. **Go to**: EC2 Console → Security Groups
2. **Find**: Security group for Elastic Beanstalk environment
   - **How to find**: Elastic Beanstalk → Your Environment → Configuration → Instances → Security groups
3. **Click**: Security group name
4. **Go to**: Outbound rules tab
5. **Click**: Edit outbound rules
6. **Add rule**:
   - Type: **PostgreSQL** (or Custom TCP)
   - Port: **5432**
   - Destination: **0.0.0.0/0** (or specific Supabase IP)
   - Description: **Allow outbound to Supabase**
7. **Save**: Save rules
8. **Restart**: Elastic Beanstalk environment (Actions → Restart app server)

---

## Verify Fix

**After fixing security group**:

1. **Restart**: Actions → Restart app server
2. **Wait**: 2-3 minutes
3. **Check logs**: `/var/log/web.stdout.log`
4. **Look for**: `Started InvoiceMeApplication` (success)
5. **Test**: `curl http://[backend-url]/actuator/health`
6. **Expected**: `{"status":"UP"}`

---

## Summary

| Issue | Status | Fix |
|-------|--------|-----|
| **Network unreachable** | 🔴 **Root cause** | Add security group outbound rule for port 5432 |
| **502 Bad Gateway** | 🔴 Symptom | Will fix after network issue resolved |
| **Connection refused** | 🔴 Symptom | Will fix after network issue resolved |
| **App crashes** | 🔴 Symptom | Will fix after network issue resolved |

---

## Action Items

1. 🔴 **Check security group outbound rules** (Priority 1)
2. 🔴 **Add rule for port 5432** if missing
3. 🔴 **Restart environment** after fixing
4. ✅ **Verify app starts** (check logs)
5. ✅ **Test health endpoint**

---

**Bottom Line**: The root cause is **network connectivity** - your Elastic Beanstalk instance cannot reach Supabase. Fix the security group outbound rules to allow port 5432, then restart the environment.

