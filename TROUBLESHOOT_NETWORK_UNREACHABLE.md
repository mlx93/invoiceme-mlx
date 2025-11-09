# Troubleshoot Network Unreachable - Routing is Correct

## ✅ Routing is Correct

**Your subnet has**:
- ✅ Route to `0.0.0.0/0` via Internet Gateway
- ✅ Public subnet (can reach internet)
- ✅ Security groups have correct rules

**But still getting "Network is unreachable"** - let's check other causes.

---

## Other Possible Causes

### 1. Network ACLs Blocking Traffic ⚠️ **CHECK THIS**

**Problem**: Network ACLs can block traffic even if security groups allow it.

**Check**:
1. **Go to**: VPC Console → Network ACLs
2. **Find**: Network ACL for your subnet (`acl-0f6bfa576d7aaf527`)
3. **Check**: **Outbound rules** tab
4. **Verify**: Rule allowing port 5432 (or all traffic) to `0.0.0.0/0`

**If missing**:
- **Add outbound rule**:
  - Rule number: 100 (or any number)
  - Type: PostgreSQL (or Custom TCP)
  - Port: 5432
  - Destination: 0.0.0.0/0
  - Allow/Deny: **Allow**
- **Save**: Save changes

---

### 2. Supabase Firewall/IP Restrictions

**Problem**: Supabase might be blocking your Elastic Beanstalk instance IP.

**Check**:
1. **Go to**: Supabase Dashboard → Settings → Database
2. **Check**: 
   - Connection pooling settings
   - IP restrictions / firewall rules
   - Any IP whitelist

**Fix**:
- Disable IP restrictions, OR
- Add your Elastic Beanstalk instance IP to allowlist

**Note**: Supabase usually allows all IPs by default, but check if you've enabled restrictions.

---

### 3. DNS Resolution Issue

**Problem**: Instance cannot resolve `db.rhyariaxwllotjiuchhz.supabase.co` hostname.

**Test** (if SSH access available):
```bash
# Test DNS resolution
nslookup db.rhyariaxwllotjiuchhz.supabase.co

# Test connectivity
nc -zv db.rhyariaxwllotjiuchhz.supabase.co 5432
```

**Fix**: Check VPC DNS settings (usually automatic, but verify).

---

### 4. Instance in Different Subnet

**Problem**: Instance might be in a different subnet than the one you checked.

**Verify**:
1. **Go to**: EC2 Console → Instances
2. **Find**: Your Elastic Beanstalk instance
3. **Check**: Subnet ID
4. **Verify**: Matches the subnet you checked (`subnet-0ec51c4b01051563c`)

**If different subnet**:
- Check that subnet's route table
- Verify it has route to `0.0.0.0/0`

---

### 5. Route Table Not Associated

**Problem**: Route table might not be associated with the subnet.

**Check**:
1. **Go to**: VPC Console → Subnets
2. **Find**: Your subnet
3. **Check**: Route table tab
4. **Verify**: Route table is associated (shows route table ID)

**If not associated**:
- Click "Edit route table association"
- Select correct route table
- Save

---

## Step-by-Step Diagnosis

### Step 1: Check Network ACLs (Priority 1)

1. **Go to**: VPC Console → Network ACLs
2. **Find**: Network ACL `acl-0f6bfa576d7aaf527` (from your subnet)
3. **Click**: Network ACL name
4. **Go to**: **Outbound rules** tab
5. **Check**: Is there a rule allowing port 5432 (or all traffic)?

**If missing**:
- Click "Edit outbound rules"
- Add rule:
  - Rule number: 100
  - Type: PostgreSQL (or Custom TCP)
  - Port: 5432
  - Destination: 0.0.0.0/0
  - Allow/Deny: **Allow**
- Save changes

---

### Step 2: Verify Instance Subnet

1. **Go to**: EC2 Console → Instances
2. **Find**: Your Elastic Beanstalk instance
3. **Check**: Subnet ID
4. **Verify**: Matches `subnet-0ec51c4b01051563c`

**If different**: Check that subnet's route table and Network ACLs

---

### Step 3: Check Supabase Settings

1. **Go to**: Supabase Dashboard → Settings → Database
2. **Check**: 
   - Connection pooling
   - IP restrictions
   - Firewall rules
3. **Verify**: No IP whitelist blocking your instance

---

### Step 4: Test Connectivity (If Possible)

**If you have SSH access** (or AWS Systems Manager Session Manager):

```bash
# Test DNS
nslookup db.rhyariaxwllotjiuchhz.supabase.co

# Test connection
nc -zv db.rhyariaxwllotjiuchhz.supabase.co 5432

# Or with telnet
telnet db.rhyariaxwllotjiuchhz.supabase.co 5432
```

**If DNS fails**: DNS resolution issue
**If connection fails**: Network ACL or firewall issue

---

## Most Likely Fix: Network ACLs

**90% chance**: Network ACLs are blocking outbound traffic.

**Default Network ACLs** usually allow all traffic, but **custom Network ACLs** deny by default.

**Check**: Your Network ACL outbound rules - if it's a custom ACL, you need to explicitly allow port 5432.

---

## Quick Checklist

- [ ] **Network ACL outbound rules** - Check and add rule for port 5432
- [ ] **Instance subnet** - Verify matches subnet you checked
- [ ] **Route table association** - Verify route table is associated
- [ ] **Supabase firewall** - Check for IP restrictions
- [ ] **DNS resolution** - Test if hostname resolves
- [ ] **Restart environment** - After fixing Network ACLs

---

## Next Steps

1. 🔴 **Check Network ACLs** (most likely fix)
2. ✅ **Add outbound rule** for port 5432 if missing
3. ✅ **Restart environment**
4. ✅ **Check logs** for success

---

**Action**: Check Network ACLs first - if it's a custom ACL, add an outbound rule allowing port 5432 to 0.0.0.0/0!

