# Advanced Network Investigation - All Configs Correct

## ✅ What You've Verified

**Network Configuration** (All Correct):
- ✅ Security groups allow outbound port 5432 to 0.0.0.0/0
- ✅ Subnet (subnet-0ec51c4b01051563c) has route to 0.0.0.0/0 via IGW
- ✅ Network ACLs allow all traffic inbound/outbound
- ✅ Environment variables correctly set (DATABASE_URL, DB_USERNAME, DB_PASSWORD)
- ✅ Platform is Java 17 matching JAR version

**Application Behavior**:
- ✅ Tomcat initializes on port 5000
- ❌ Crashes during HikariPool/Flyway initialization
- ❌ Error: `java.net.SocketException: Network is unreachable`

---

## 🔍 Advanced Investigation Steps

### 1. Verify Instance Actually Has Public IP and Internet Access

**Problem**: Even with correct routing, the instance might not have a public IP assigned or might be in a different subnet than expected.

#### Check Instance Network Configuration

1. **Go to**: EC2 Console → Instances
2. **Find**: Your Elastic Beanstalk instance (filter by: `Invoiceme-mlx-back-env`)
3. **Verify**:
   - ✅ **Public IPv4 address** exists (not blank)
   - ✅ **Private IPv4 address** exists
   - ✅ **Subnet ID** matches what you checked: `subnet-0ec51c4b01051563c`
   - ✅ **VPC ID** matches: `vpc-03cd6462b46350c8e`
   - ✅ **Security groups** match what you configured

#### Test Internet Connectivity via Systems Manager

**Use AWS Systems Manager Session Manager to access the instance without SSH**:

1. **Go to**: Systems Manager → Session Manager
2. **Click**: Start session
3. **Select**: Your Elastic Beanstalk instance
4. **Run tests**:

```bash
# Test DNS resolution
nslookup db.rhyariaxwllotjiuchhz.supabase.co

# Test if DNS resolves to an IP
dig db.rhyariaxwllotjiuchhz.supabase.co

# Test connectivity to Supabase
telnet db.rhyariaxwllotjiuchhz.supabase.co 5432

# Or use nc (netcat)
nc -zv db.rhyariaxwllotjiuchhz.supabase.co 5432

# Test general internet connectivity
curl -I https://www.google.com

# Check what process is running on port 5000
sudo lsof -i :5000

# Check if Spring Boot is actually running
ps aux | grep java
```

**Expected Results**:
- ✅ DNS resolution should return an IP address
- ✅ Telnet/nc should connect (shows "Connected" or "succeeded")
- ✅ curl to google should return 200 OK
- ❌ If DNS fails: VPC DNS settings issue
- ❌ If connection fails but DNS works: Firewall/network issue
- ❌ If internet works but Supabase doesn't: Supabase firewall issue

---

### 2. Verify VPC DNS Settings

**Problem**: VPC might not have DNS resolution enabled, preventing hostname resolution.

#### Check VPC DNS Configuration

1. **Go to**: VPC Console → Your VPCs
2. **Find**: `vpc-03cd6462b46350c8e`
3. **Check**:
   - ✅ **DNS resolution**: Enabled
   - ✅ **DNS hostnames**: Enabled

**Both MUST be enabled** for your instance to resolve `db.rhyariaxwllotjiuchhz.supabase.co`.

#### Fix If Disabled

1. **Select**: Your VPC
2. **Actions** → **Edit VPC settings**
3. **Enable**:
   - ✅ Enable DNS resolution
   - ✅ Enable DNS hostnames
4. **Save**: Save changes
5. **Restart**: Elastic Beanstalk environment

---

### 3. Check Supabase Connection from Another Network

**Problem**: Supabase might have IP restrictions or the connection string/credentials might be incorrect.

#### Test Connection from Your Local Machine

```bash
# Test DNS resolution
nslookup db.rhyariaxwllotjiuchhz.supabase.co

# Test connection
telnet db.rhyariaxwllotjiuchhz.supabase.co 5432

# Or use psql
psql "postgresql://postgres:invoicemesupa@db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres"
```

**Expected**:
- ✅ Connection should succeed from your local machine
- ✅ Credentials should work
- ❌ If fails from local: Credentials or database issue
- ❌ If works locally but not from AWS: AWS network/Supabase firewall issue

#### Check Supabase Dashboard

1. **Go to**: [Supabase Dashboard](https://supabase.com/dashboard)
2. **Select**: Your project
3. **Go to**: Settings → Database
4. **Check**:
   - ✅ **Database Status**: Active (not paused)
   - ✅ **Connection Pooling**: Enabled
   - ✅ **Connection String**: Matches what you're using
   - ✅ **SSL Mode**: Check if SSL is required

#### Check Supabase Network Restrictions

1. **In Supabase Dashboard**: Settings → API
2. **Check**: Network Restrictions / IP Allow List
3. **Verify**: Either no restrictions, or AWS IP range is allowed

**If restricted**: Add your Elastic Beanstalk instance's public IP to the allowlist.

---

### 4. Verify Instance is in Correct Subnet

**Problem**: Instance might be in a different subnet than the one you verified.

#### Double-Check Instance Subnet

1. **Go to**: EC2 Console → Instances
2. **Find**: Your Elastic Beanstalk instance
3. **Verify**: Subnet ID = `subnet-0ec51c4b01051563c`

#### If Subnet is Different

1. **Check that subnet's route table**:
   - VPC Console → Subnets → Select subnet → Route table tab
2. **Verify**: Route to `0.0.0.0/0` exists
3. **Check that subnet's Network ACLs**:
   - Network ACL tab
   - Verify outbound rules allow traffic

---

### 5. Enable Verbose Logging and Connection Debugging

**Problem**: Current logs don't show enough detail about the connection failure.

#### Update application.yml for More Logging

**Add to Elastic Beanstalk environment variables**:

```
LOGGING_LEVEL_COM_ZAXXER_HIKARI=DEBUG
LOGGING_LEVEL_ORG_POSTGRESQL=DEBUG
LOGGING_LEVEL_ORG_FLYWAYDB=DEBUG
```

**This will show**:
- HikariCP connection pool details
- PostgreSQL driver connection attempts
- Flyway migration attempts
- Exact point of failure

#### Restart and Check Logs

1. **Restart**: Elastic Beanstalk → Actions → Restart app server
2. **Check logs**: `/var/log/web.stdout.log`
3. **Look for**:
   - HikariCP connection initialization details
   - PostgreSQL connection attempt details
   - Any additional error messages

---

### 6. Check Network ACL Default vs Custom

**Problem**: You verified Network ACL rules, but if it's a custom ACL, it needs explicit allow rules for ephemeral ports.

#### Check Network ACL Type

1. **Go to**: VPC Console → Network ACLs
2. **Find**: `acl-0f6bfa576d7aaf527`
3. **Check**: 
   - Is this the **default** ACL? (Name contains "default")
   - Or a **custom** ACL?

#### Custom ACL Requires Additional Rules

**For custom Network ACLs**, you need **BOTH**:

**Outbound Rules** (Already have):
- ✅ Rule 100: All traffic to 0.0.0.0/0

**Inbound Rules** (For response traffic):
- ✅ **Rule for ephemeral ports**: TCP 1024-65535 from 0.0.0.0/0
- ✅ This allows response traffic from Supabase back to your instance

#### Add Ephemeral Port Rule (If Missing)

1. **Go to**: Network ACL → Inbound rules
2. **Check**: Rule allowing ports 1024-65535 from 0.0.0.0/0
3. **If missing**:
   - Click: Edit inbound rules
   - Add rule:
     - Rule number: 101
     - Type: Custom TCP
     - Port range: 1024-65535
     - Source: 0.0.0.0/0
     - Allow/Deny: Allow
   - Save changes

**Why this matters**: When your app connects to Supabase (port 5432), the response comes back on an ephemeral port (1024-65535). If Network ACL blocks these ports, connection fails.

---

### 7. Check for Route Table Edge Cases

**Problem**: Route table might have conflicting or more specific routes.

#### Check All Routes in Route Table

1. **Go to**: VPC Console → Route Tables
2. **Find**: Route table for subnet-0ec51c4b01051563c
3. **Check ALL routes**:
   - Local route (should always exist)
   - Route to 0.0.0.0/0 via IGW
   - **Any other routes** that might be more specific

**Look for**:
- ❌ Blackhole routes
- ❌ More specific routes that override 0.0.0.0/0
- ❌ Routes pointing to non-active targets

---

### 8. Check Elastic Beanstalk Auto Scaling Group

**Problem**: New instances might be launching in a different subnet.

#### Check Auto Scaling Group Configuration

1. **Go to**: Elastic Beanstalk → Your Environment → Configuration
2. **Click**: Capacity (Edit)
3. **Check**: 
   - Availability Zones
   - Subnets
   - Verify correct subnet is selected

**If wrong subnet selected**:
- Update to use subnet-0ec51c4b01051563c
- Apply changes
- Wait for new instances to launch

---

### 9. Test with Direct IP Connection

**Problem**: DNS resolution might be failing even though it shouldn't.

#### Get Supabase IP Address

```bash
# From your local machine
nslookup db.rhyariaxwllotjiuchhz.supabase.co
```

**Example output**:
```
Server:  8.8.8.8
Address: 8.8.8.8#53

Non-authoritative answer:
Name:    db.rhyariaxwllotjiuchhz.supabase.co
Address: 54.123.45.67  # This is the IP
```

#### Temporarily Test with IP

**Update DATABASE_URL** in Elastic Beanstalk environment variables:

```
DATABASE_URL=jdbc:postgresql://54.123.45.67:5432/postgres
```

**Replace `54.123.45.67` with actual IP from nslookup.**

**Restart and test**:
- If works with IP but not hostname: DNS resolution issue
- If still fails: Not a DNS issue

**IMPORTANT**: Change back to hostname after testing (IPs can change).

---

### 10. Check Security Group Timing and Propagation

**Problem**: Security group rules might not have propagated yet.

#### Verify and Wait

1. **Check**: Security group rules are saved (not just edited)
2. **Wait**: 5-10 minutes for rules to propagate across AWS
3. **Restart**: Elastic Beanstalk environment (full restart, not just app server)
4. **Test**: Check logs again

---

## 🎯 Recommended Investigation Order

**Priority 1** (Most Likely Issues):

1. ✅ **Check VPC DNS settings** (Step 2) - 5 minutes
2. ✅ **Check Network ACL ephemeral ports** (Step 6) - 5 minutes
3. ✅ **Test from Systems Manager** (Step 1) - 10 minutes

**Priority 2** (If Priority 1 doesn't fix):

4. ✅ **Check Supabase network restrictions** (Step 3) - 5 minutes
5. ✅ **Verify instance subnet** (Step 4) - 3 minutes
6. ✅ **Test with direct IP** (Step 9) - 5 minutes

**Priority 3** (Deep Debugging):

7. ✅ **Enable verbose logging** (Step 5) - 10 minutes
8. ✅ **Check Auto Scaling Group** (Step 8) - 5 minutes
9. ✅ **Check route table edge cases** (Step 7) - 5 minutes

---

## 🔧 Quick Diagnostic Script

**If you can access the instance via Systems Manager**, run this:

```bash
#!/bin/bash

echo "=== Instance Network Diagnostics ==="
echo ""

echo "1. Check public IP:"
curl -s http://169.254.169.254/latest/meta-data/public-ipv4
echo ""

echo "2. Check private IP:"
curl -s http://169.254.169.254/latest/meta-data/local-ipv4
echo ""

echo "3. Check subnet:"
curl -s http://169.254.169.254/latest/meta-data/network/interfaces/macs/$(curl -s http://169.254.169.254/latest/meta-data/mac)/subnet-id
echo ""

echo "4. Test DNS resolution:"
nslookup db.rhyariaxwllotjiuchhz.supabase.co
echo ""

echo "5. Test internet connectivity:"
curl -I https://www.google.com
echo ""

echo "6. Test Supabase connectivity:"
nc -zv db.rhyariaxwllotjiuchhz.supabase.co 5432
echo ""

echo "7. Check if Java is running:"
ps aux | grep java
echo ""

echo "8. Check port 5000:"
sudo lsof -i :5000
echo ""
```

Save this as `/tmp/diagnostics.sh` on the instance and run:
```bash
chmod +x /tmp/diagnostics.sh
/tmp/diagnostics.sh
```

---

## 📋 Checklist

Go through each item systematically:

- [ ] VPC DNS resolution enabled
- [ ] VPC DNS hostnames enabled
- [ ] Instance has public IP address
- [ ] Instance subnet matches the one verified (subnet-0ec51c4b01051563c)
- [ ] Network ACL allows inbound ephemeral ports (1024-65535)
- [ ] Supabase has no IP restrictions, or AWS IP is allowed
- [ ] Connection works from local machine
- [ ] DNS resolves correctly from instance
- [ ] Security group rules are saved and propagated (wait 10 min)
- [ ] Auto Scaling Group uses correct subnet
- [ ] No blackhole or conflicting routes in route table

---

## 🚨 If Nothing Works

**Last Resort Options**:

### Option 1: Create New Environment in Known-Good Subnet

1. Create new Elastic Beanstalk environment
2. Use default VPC and default public subnet
3. Verify outbound internet access works
4. Then deploy application

### Option 2: Use Supabase Connection Pooler

Instead of direct connection:
```
DATABASE_URL=jdbc:postgresql://aws-0-[region].pooler.supabase.com:6543/postgres
```

This uses connection pooling which might have different network behavior.

### Option 3: Check with AWS Support

If all network configs are correct and none of the above works:
- Open AWS Support case
- Provide: Instance ID, Subnet ID, VPC ID, Route table ID, Network ACL ID
- Ask them to verify network path from instance to internet

---

## 📊 Expected Outcomes

**After following these steps, you should know**:

1. ✅ Can the instance reach the internet at all?
2. ✅ Can the instance resolve DNS?
3. ✅ Can the instance reach Supabase specifically?
4. ✅ Is this a VPC/AWS configuration issue or a Supabase issue?
5. ✅ What specific network component is blocking the connection?

---

**Next Action**: Start with Priority 1 checks (VPC DNS settings and Network ACL ephemeral ports). These are the most common causes when all other configs appear correct.

