# Network Diagnostic Checklist - InvoiceMe Elastic Beanstalk

## Your Configuration Summary

```
Application: invoiceme-mlx-back
Environment: Invoiceme-mlx-back-env
Region: us-east-1
VPC: vpc-03cd6462b46350c8e
Subnet: subnet-0ec51c4b01051563c
Route Table: Has route to 0.0.0.0/0 via igw-06dbf36bda309f0dc
Network ACL: acl-0f6bfa576d7aaf527 (Rule 100: All traffic allowed)
Security Groups:
  - awseb-e-3p5minfpmi-stack-*
  - AWSEBSecurityGroup-zJUbqTxeEr1y
Database: db.rhyariaxwllotjiuchhz.supabase.co:5432
```

---

## 🔍 Priority 1: Most Likely Issues

### ✅ 1. Check VPC DNS Settings

**Why**: Instance needs DNS resolution to reach `db.rhyariaxwllotjiuchhz.supabase.co`

1. **Go to**: VPC Console → Your VPCs
2. **Find**: `vpc-03cd6462b46350c8e`
3. **Actions** → **Edit VPC settings**
4. **Verify BOTH are enabled**:
   - [ ] ✅ Enable DNS resolution
   - [ ] ✅ Enable DNS hostnames
5. **If disabled**: Enable both and **Save**
6. **Restart**: Elastic Beanstalk environment

**Time**: 5 minutes  
**Likelihood**: 🔴 HIGH - Very common cause of "Network unreachable"

---

### ✅ 2. Check Network ACL Inbound Rules (Ephemeral Ports)

**Why**: Response traffic from Supabase comes back on ephemeral ports (1024-65535)

1. **Go to**: VPC Console → Network ACLs
2. **Find**: `acl-0f6bfa576d7aaf527`
3. **Click**: Inbound rules tab
4. **Check**: Is there a rule allowing TCP 1024-65535 from 0.0.0.0/0?

**Current Status**:
- [ ] ✅ Rule exists (continue to next check)
- [ ] ❌ Rule missing (ADD IT - see below)

**If missing, add rule**:
1. Click: Edit inbound rules
2. Add rule:
   - Rule number: **101**
   - Type: **Custom TCP**
   - Port range: **1024-65535**
   - Source: **0.0.0.0/0**
   - Allow/Deny: **Allow**
3. Save changes
4. Restart: Elastic Beanstalk environment

**Time**: 5 minutes  
**Likelihood**: 🔴 HIGH - Network ACL with Rule 100 "All traffic" should cover this, but worth verifying

---

### ✅ 3. Test Connectivity from Instance

**Why**: Directly verify what the instance can reach

**Use AWS Systems Manager Session Manager**:

1. **Go to**: Systems Manager → Session Manager
2. **Click**: Start session
3. **Select**: Your Elastic Beanstalk instance
4. **Run**:

```bash
# Test DNS resolution
nslookup db.rhyariaxwllotjiuchhz.supabase.co

# Test connectivity
nc -zv db.rhyariaxwllotjiuchhz.supabase.co 5432

# Test general internet
curl -I https://www.google.com
```

**Expected Results**:
- [ ] ✅ DNS resolves to an IP address
- [ ] ✅ nc shows "succeeded" or "Connected"
- [ ] ✅ curl returns 200 OK

**If DNS fails**:
- VPC DNS settings are disabled (see Check #1)

**If DNS works but connection fails**:
- Network path is blocked somewhere
- Supabase firewall blocking your IP
- Network ACL missing ephemeral port rule

**If internet doesn't work**:
- Route table not actually applied to subnet
- Instance in wrong subnet

**Time**: 10 minutes  
**Likelihood**: 🔴 HIGH - Will tell you exactly what's failing

---

## 🔍 Priority 2: Configuration Verification

### ✅ 4. Verify Instance is in Correct Subnet

**Why**: Instance might be launching in a different subnet

1. **Go to**: EC2 Console → Instances
2. **Find**: Your instance (filter: `Invoiceme-mlx-back-env`)
3. **Verify**:
   - [ ] Subnet ID = `subnet-0ec51c4b01051563c`
   - [ ] VPC ID = `vpc-03cd6462b46350c8e`
   - [ ] Has **Public IPv4 address** (not blank)
   - [ ] Has **Private IPv4 address**

**If subnet is different**:
1. Note the actual subnet ID
2. Check that subnet's route table (VPC Console → Subnets)
3. Verify it has route to 0.0.0.0/0

**If no public IP**:
1. Go to: Elastic Beanstalk → Configuration → Instances
2. Check: "Public IP address" setting
3. Enable if disabled

**Time**: 3 minutes  
**Likelihood**: 🟡 MEDIUM - Easy to verify

---

### ✅ 5. Check Supabase Network Restrictions

**Why**: Supabase might have IP restrictions enabled

1. **Go to**: [Supabase Dashboard](https://supabase.com/dashboard)
2. **Select**: Your project (rhyariaxwllotjiuchhz)
3. **Go to**: Settings → Database
4. **Check**:
   - [ ] Database status = **Active** (not paused)
   - [ ] SSL mode setting
   - [ ] Network restrictions / IP allowlist

**Go to**: Settings → API
- [ ] Check for network restrictions
- [ ] Verify no IP allowlist is blocking AWS

**If IP restrictions exist**:
1. Get your instance's public IP (from EC2 console)
2. Add it to Supabase allowlist
3. Or disable IP restrictions

**Time**: 5 minutes  
**Likelihood**: 🟡 MEDIUM - Supabase usually allows all IPs by default

---

### ✅ 6. Test Connection from Local Machine

**Why**: Verify Supabase is reachable and credentials work

**From your local machine**:

```bash
# Test DNS
nslookup db.rhyariaxwllotjiuchhz.supabase.co

# Test connection
telnet db.rhyariaxwllotjiuchhz.supabase.co 5432

# Test with psql (if installed)
psql "postgresql://postgres:invoicemesupa@db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres"
```

**Expected**:
- [ ] ✅ Connection succeeds
- [ ] ✅ Credentials work

**If fails from local machine**:
- Problem is with Supabase or credentials, not AWS

**If works locally but not from AWS**:
- AWS network issue or Supabase blocking AWS IP

**Time**: 3 minutes  
**Likelihood**: 🟡 MEDIUM - Quick sanity check

---

## 🔍 Priority 3: Deep Debugging

### ✅ 7. Enable Verbose Logging

**Why**: Get more details about connection failure

**Add environment variables**:

1. **Go to**: Elastic Beanstalk → Configuration → Software
2. **Add**:
```
LOGGING_LEVEL_COM_ZAXXER_HIKARI=DEBUG
LOGGING_LEVEL_ORG_POSTGRESQL=DEBUG
LOGGING_LEVEL_ORG_FLYWAYDB=DEBUG
```
3. **Apply**: Apply changes
4. **Wait**: 2-3 minutes
5. **Check logs**: `/var/log/web.stdout.log`

**Look for**:
- HikariCP initialization details
- PostgreSQL driver connection attempts
- Exact error messages

**Time**: 10 minutes  
**Likelihood**: 🟢 LOW - But provides more info

---

### ✅ 8. Check Auto Scaling Group Subnet Configuration

**Why**: New instances might be launching in wrong subnet

1. **Go to**: Elastic Beanstalk → Configuration → Capacity
2. **Check**: 
   - [ ] Subnets section
   - [ ] Verify `subnet-0ec51c4b01051563c` is selected
   - [ ] Or at least a public subnet is selected

**If wrong subnet**:
1. Edit capacity settings
2. Select correct subnet
3. Apply (will relaunch instances)

**Time**: 5 minutes  
**Likelihood**: 🟢 LOW - But easy to check

---

### ✅ 9. Test with Direct IP (Bypass DNS)

**Why**: Isolate if it's a DNS issue

**Get Supabase IP**:
```bash
nslookup db.rhyariaxwllotjiuchhz.supabase.co
```

**Temporarily update DATABASE_URL**:
```
DATABASE_URL=jdbc:postgresql://[IP_FROM_NSLOOKUP]:5432/postgres
```

**Restart and test**:
- [ ] If works with IP: DNS resolution issue
- [ ] If still fails: Not a DNS issue

**IMPORTANT**: Change back to hostname after test

**Time**: 5 minutes  
**Likelihood**: 🟢 LOW - But good for diagnosis

---

## 📊 Decision Tree

```
Can't connect to Supabase
│
├─ Priority 1: Test from instance (Check #3)
│  │
│  ├─ DNS fails
│  │  └─ Fix: VPC DNS settings (Check #1)
│  │
│  ├─ DNS works, connection fails
│  │  ├─ Fix: Network ACL ephemeral ports (Check #2)
│  │  └─ Fix: Supabase IP restrictions (Check #5)
│  │
│  └─ Internet doesn't work
│     ├─ Verify instance subnet (Check #4)
│     └─ Verify route table is applied
│
├─ Priority 2: Verify configuration
│  ├─ Check instance subnet (Check #4)
│  ├─ Check Supabase restrictions (Check #5)
│  └─ Test from local machine (Check #6)
│
└─ Priority 3: Deep debugging
   ├─ Enable verbose logging (Check #7)
   ├─ Check Auto Scaling Group (Check #8)
   └─ Test with direct IP (Check #9)
```

---

## 🎯 Recommended Action Plan

**Start here** (15 minutes total):

1. ✅ **Check VPC DNS settings** (Check #1) - 5 min
2. ✅ **Check Network ACL inbound rules** (Check #2) - 5 min
3. ✅ **Test from instance via Systems Manager** (Check #3) - 5 min

**After Check #3, you'll know**:
- Can the instance reach the internet?
- Can it resolve DNS?
- Can it connect to Supabase?

**This will tell you exactly where the problem is.**

---

## 🚨 If All Checks Pass But Still Fails

### Option A: Create Diagnostic Script

Save this on the instance and run:

```bash
#!/bin/bash
echo "=== Network Diagnostics ==="
echo "Public IP:" $(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
echo "Subnet:" $(curl -s http://169.254.169.254/latest/meta-data/network/interfaces/macs/$(curl -s http://169.254.169.254/latest/meta-data/mac)/subnet-id)
echo ""
echo "DNS Test:"
nslookup db.rhyariaxwllotjiuchhz.supabase.co
echo ""
echo "Connectivity Test:"
nc -zv db.rhyariaxwllotjiuchhz.supabase.co 5432
echo ""
echo "Internet Test:"
curl -I https://www.google.com
```

### Option B: Check Spring Boot Connection Timeout

The error might be a timeout, not a network block. Check application.yml:

```yaml
spring:
  datasource:
    hikari:
      connection-timeout: 30000  # 30 seconds
```

Try increasing to 60000 (60 seconds) to see if it's just slow.

### Option C: Try Supabase Connection Pooler

Use Supabase's connection pooler instead:

```
DATABASE_URL=jdbc:postgresql://aws-0-us-east-1.pooler.supabase.com:6543/postgres
```

This might have different network behavior.

---

## 📝 Results Tracker

As you go through checks, mark your results:

| Check | Time | Result | Notes |
|-------|------|--------|-------|
| 1. VPC DNS | ___ min | ✅ / ❌ | Enabled: Yes/No |
| 2. Network ACL | ___ min | ✅ / ❌ | Rule exists: Yes/No |
| 3. Test from instance | ___ min | ✅ / ❌ | DNS: __, Connection: __ |
| 4. Instance subnet | ___ min | ✅ / ❌ | Correct: Yes/No |
| 5. Supabase restrictions | ___ min | ✅ / ❌ | Restrictions: Yes/No |
| 6. Test from local | ___ min | ✅ / ❌ | Works: Yes/No |
| 7. Verbose logging | ___ min | ✅ / ❌ | New errors: __ |
| 8. Auto Scaling | ___ min | ✅ / ❌ | Correct subnet: Yes/No |
| 9. Direct IP test | ___ min | ✅ / ❌ | Works with IP: Yes/No |

---

## 🎉 Success Indicators

**When fixed, you should see**:

1. **In logs** (`/var/log/web.stdout.log`):
```
HikariPool-1 - Starting...
HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection@...
HikariPool-1 - Start completed.
Flyway Community Edition X.X.X by Redgate
Database: jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
Successfully validated X migrations
Migrating schema "public" to version X.X.X
Successfully applied X migrations to schema "public"
Started InvoiceMeApplication in X.XXX seconds
Tomcat started on port(s): 5000 (http)
```

2. **Health check works**:
```bash
curl http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/actuator/health
```
**Returns**: `{"status":"UP"}`

3. **No 502 errors** from Nginx

---

**Start with Priority 1 checks. Based on the results, move to Priority 2 or 3 as needed.**

