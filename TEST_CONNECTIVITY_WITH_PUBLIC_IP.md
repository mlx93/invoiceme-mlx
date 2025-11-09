# Test Connectivity - Instance Has Public IP

## ✅ Instance Has Public IP

**Public IP**: `34.193.81.25`

**This is good** - the instance should be able to reach the internet. Since all network configs are correct and the instance has a public IP, let's test connectivity.

---

## Test 1: Check VPC DNS Settings

**DNS resolution must be enabled** for the instance to resolve `db.rhyariaxwllotjiuchhz.supabase.co`:

1. **Go to**: VPC Console → Your VPC (`vpc-03cd6462b46350c8e`)
2. **Actions** → **Edit DNS resolution**
3. **Verify**:
   - ✅ "Enable DNS resolution" is **checked**
   - ✅ "Enable DNS hostnames" is **checked**
4. **Save**: If changed, restart environment

**If DNS is disabled**: The instance cannot resolve hostnames, causing "Network is unreachable" errors.

---

## Test 2: Test Connectivity from Instance (If Possible)

**If you have SSH access** (or AWS Systems Manager Session Manager):

```bash
# Test DNS resolution
nslookup db.rhyariaxwllotjiuchhz.supabase.co

# Test port connectivity
nc -zv db.rhyariaxwllotjiuchhz.supabase.co 5432

# Or with telnet
telnet db.rhyariaxwllotjiuchhz.supabase.co 5432

# Test with curl (if available)
curl -v telnet://db.rhyariaxwllotjiuchhz.supabase.co:5432
```

**Expected**:
- DNS resolves to an IP address
- Port 5432 is reachable

**If DNS fails**: DNS resolution issue
**If connection fails**: Firewall or network issue

---

## Test 3: Check Supabase Firewall

**Supabase might be blocking your instance IP** (`34.193.81.25`):

1. **Go to**: Supabase Dashboard → Settings → Database
2. **Check**: 
   - Connection pooling settings
   - IP restrictions / firewall rules
   - Any IP whitelist enabled

**If IP restrictions enabled**:
- Add `34.193.81.25` to allowlist, OR
- Disable IP restrictions

**Note**: Supabase usually allows all IPs, but check if restrictions are enabled.

---

## Test 4: Verify DATABASE_URL from Instance

**Test if the connection string works**:

1. **Go to**: Elastic Beanstalk → Configuration → Software → Environment properties
2. **Verify**: `DATABASE_URL` is exactly:
   ```
   jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
   ```
3. **Check**: No spaces, no typos, no `?user=` parameters

---

## Test 5: Test from Your Local Machine

**Verify Supabase is accessible**:

```bash
# Test DNS
nslookup db.rhyariaxwllotjiuchhz.supabase.co

# Test connection
PGPASSWORD=invoicemesupa psql -h db.rhyariaxwllotjiuchhz.supabase.co -p 5432 -U postgres -d postgres -c "SELECT 1;"
```

**If this works**: Supabase is accessible, issue is with Elastic Beanstalk instance
**If this fails**: Supabase might have issues or credentials are wrong

---

## Most Likely Remaining Issues

### Issue 1: DNS Resolution Disabled (40% chance)

**Problem**: VPC DNS resolution is disabled

**Fix**: Enable DNS resolution and DNS hostnames in VPC settings

---

### Issue 2: Supabase IP Restrictions (30% chance)

**Problem**: Supabase firewall blocking instance IP `34.193.81.25`

**Fix**: Add instance IP to Supabase allowlist or disable restrictions

---

### Issue 3: DNS Hostname Resolution (20% chance)

**Problem**: Instance cannot resolve `db.rhyariaxwllotjiuchhz.supabase.co`

**Fix**: Enable DNS hostnames in VPC, verify DNS servers are configured

---

### Issue 4: Connection Timeout (10% chance)

**Problem**: Connection times out before completing

**Fix**: Increase connection timeout in `application.yml`, check Supabase connection limits

---

## Quick Action Items

1. 🔴 **Check VPC DNS settings** - Enable DNS resolution and hostnames
2. 🔴 **Check Supabase firewall** - Verify IP `34.193.81.25` is not blocked
3. 🔴 **Test connectivity** - If SSH available, test from instance
4. ✅ **Restart environment** - After fixing DNS settings

---

## Next Steps

1. **Check VPC DNS settings** (most likely fix)
2. **Check Supabase IP restrictions**
3. **Restart environment** after any changes
4. **Check logs** for success

---

**Action**: Check VPC DNS settings first - if DNS resolution is disabled, that would cause "Network is unreachable" even with correct routing!

