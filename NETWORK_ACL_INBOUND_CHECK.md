# Network ACL - Check Inbound Rules Too

## ✅ Outbound Rules Are Correct

**Your Network ACL has**:
- ✅ Rule 100: Allow All traffic (0.0.0.0/0) - **This covers port 5432**
- ✅ Default rule: Deny (but Rule 100 takes precedence)

**However**: Network ACLs are **stateless** - you need **BOTH inbound AND outbound** rules!

---

## ⚠️ Check Inbound Rules

**Network ACLs are stateless**:
- Outbound rule allows your request to go out ✅
- **But inbound rule must allow the response back** ❓

**Even though you're connecting outbound**, the database response comes back **inbound**, so you need an inbound rule too.

---

## Step 1: Check Inbound Rules

1. **Go to**: Network ACL → **Inbound rules** tab
2. **Check**: Is there a rule allowing traffic?

**Look for**:
- ✅ Rule allowing "All traffic" from `0.0.0.0/0` = Good
- ❌ Only default deny rule = **Problem!**

---

## Step 2: Add Inbound Rule (If Missing)

**If inbound rules only have default deny**:

1. **Click**: "Edit inbound rules"
2. **Add rule**:
   - Rule number: 100 (or any number < 32767)
   - Type: **All traffic** (or Custom TCP)
   - Protocol: **All** (or TCP)
   - Port range: **All** (or 1024-65535 for ephemeral ports)
   - Source: **0.0.0.0/0**
   - Allow/Deny: **Allow**
3. **Save**: Save changes

**Why**: Database responses come back on ephemeral ports (1024-65535), so you need to allow inbound traffic.

---

## Step 3: Verify Instance Subnet

**Make sure your instance is actually using this Network ACL**:

1. **Go to**: EC2 Console → Instances
2. **Find**: Your Elastic Beanstalk instance
3. **Check**: Subnet ID
4. **Go to**: VPC Console → Subnets
5. **Find**: Your instance's subnet
6. **Check**: Network ACL (should be `acl-0f6bfa576d7aaf527`)

**If different Network ACL**: Check that Network ACL's inbound rules too.

---

## Step 4: Restart Environment

**After fixing inbound rules**:

1. **Go to**: Elastic Beanstalk → Your Environment
2. **Click**: Actions → **Restart app server**
3. **Wait**: 2-3 minutes
4. **Check logs**: `/var/log/web.stdout.log`
5. **Look for**: `Started InvoiceMeApplication` (success)

---

## Network ACL Rule Order

**Important**: Rules are evaluated in order (lowest number first).

**Your current setup**:
- Rule 100: Allow All (takes precedence) ✅
- Default (*): Deny All (only if Rule 100 doesn't match)

**This is correct** - Rule 100 will match first and allow traffic.

---

## Why Inbound Rules Matter

**Even for outbound connections**:
1. Your app sends request outbound (allowed by outbound rule) ✅
2. Database sends response back **inbound** (needs inbound rule) ❓
3. If inbound is denied, connection fails ❌

**Network ACLs are stateless** - they don't track connections, so you need explicit rules for both directions.

---

## Quick Checklist

- [ ] **Outbound rules** - ✅ Already correct (Rule 100 allows all)
- [ ] **Inbound rules** - ❓ **Check this** - Need rule allowing responses
- [ ] **Instance subnet** - Verify matches Network ACL subnet
- [ ] **Restart environment** - After fixing inbound rules

---

## Most Likely Fix

**90% chance**: Inbound rules are missing or blocking responses.

**Fix**: Add inbound rule allowing "All traffic" from `0.0.0.0/0` (Rule 100).

---

**Action**: Check the **Inbound rules** tab - if there's no allow rule, add one for "All traffic" from `0.0.0.0/0`!

