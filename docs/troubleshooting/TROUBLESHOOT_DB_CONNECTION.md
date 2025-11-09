# Troubleshoot Database Connection (Format is Correct)

## ✅ Your Configuration is Correct

You have the right format:
```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
```

**But the app still fails to connect.** Let's troubleshoot further.

---

## Possible Causes

### 1. Environment Variables Not Applied

**Check**: Did you restart after setting environment variables?

**Fix**:
1. Go to: **Actions** → **Restart app server**
2. Wait 2-3 minutes
3. Check logs: `/var/log/web.stdout.log`

---

### 2. Network/Firewall Issue

**Problem**: Elastic Beanstalk instance can't reach Supabase (firewall/security group blocking)

**Check Supabase Settings**:
1. Go to Supabase Dashboard
2. **Settings** → **Database**
3. Check **Connection pooling** settings
4. Verify **Allowed IPs** (if IP restrictions are enabled)

**Supabase Default**: Usually allows all IPs, but check if you have IP restrictions enabled.

---

### 3. Database Credentials Incorrect

**Test**: Verify credentials work

**Option A: Test from your local machine**:
```bash
PGPASSWORD=invoicemesupa psql -h db.rhyariaxwllotjiuchhz.supabase.co -p 5432 -U postgres -d postgres -c "SELECT 1;"
```

**Option B: Check Supabase Dashboard**:
1. Go to Supabase → **Settings** → **Database**
2. Verify password is correct
3. Check if database is paused/stopped

---

### 4. Connection Timeout

**Problem**: Connection takes too long (network latency)

**Check logs for**:
- `Connection timeout`
- `Connection attempt failed`
- `Timed out waiting for connection`

**Fix**: Increase timeout in `application.yml` (already set to 30 seconds, which should be enough)

---

### 5. Database URL Format Issue (Less Likely)

**Double-check**: Make sure there are NO extra characters:
- ✅ `jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres`
- ❌ `jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?` (trailing ?)
- ❌ `jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres ` (trailing space)

---

## Step-by-Step Troubleshooting

### Step 1: Verify Environment Variables Are Set

1. **Go to**: Configuration → Software → Environment properties
2. **Verify**:
   - DATABASE_URL is exactly: `jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres`
   - DB_USERNAME is: `postgres`
   - DB_PASSWORD is: `invoicemesupa`
   - No extra spaces or characters

3. **If correct**, proceed to Step 2
4. **If incorrect**, fix and restart

---

### Step 2: Restart Application Server

**After verifying environment variables**:

1. **Go to**: Actions → **Restart app server**
2. **Wait**: 2-3 minutes for restart
3. **Check logs**: Logs → Request logs → `/var/log/web.stdout.log`
4. **Look for**:
   - ✅ `Started InvoiceMeApplication` = Success
   - ❌ `Unable to obtain connection` = Still failing

---

### Step 3: Check Latest Logs

**Get the most recent error**:

1. **Go to**: Logs → Request logs
2. **Select**: `/var/log/web.stdout.log`
3. **Select**: "Last 100 lines"
4. **Request logs**

**Look for**:
- `Unable to obtain connection from database`
- `Connection refused`
- `Connection timeout`
- `Authentication failed`
- Any specific error messages

---

### Step 4: Test Connection from Elastic Beanstalk Instance

**If possible, SSH into the instance** (if SSH is enabled):

1. **Go to**: EC2 Console → Find your Elastic Beanstalk instance
2. **SSH into instance** (if enabled)
3. **Test connection**:
   ```bash
   # Install psql if needed
   sudo yum install postgresql15 -y
   
   # Test connection
   PGPASSWORD=invoicemesupa psql -h db.rhyariaxwllotjiuchhz.supabase.co -p 5432 -U postgres -d postgres -c "SELECT 1;"
   ```

**If this fails**: Network/firewall issue
**If this succeeds**: Application configuration issue

---

### Step 5: Check Supabase Database Status

1. **Go to**: Supabase Dashboard
2. **Check**: Database status (is it paused/stopped?)
3. **Check**: Settings → Database → Connection string
4. **Verify**: Password is correct

---

## Common Solutions

### Solution 1: Restart After Environment Variable Changes

**If you just added/changed environment variables**:
- Environment variables require a restart to take effect
- **Action**: Actions → Restart app server

---

### Solution 2: Check for Trailing Spaces/Characters

**Copy-paste can introduce hidden characters**:
- Check DATABASE_URL has no trailing spaces
- Check DB_USERNAME and DB_PASSWORD have no extra characters
- Re-type if needed

---

### Solution 3: Verify Supabase Database is Accessible

**Test from your local machine**:
```bash
PGPASSWORD=invoicemesupa psql -h db.rhyariaxwllotjiuchhz.supabase.co -p 5432 -U postgres -d postgres -c "SELECT version();"
```

**If this fails**: Database credentials or network issue
**If this succeeds**: Elastic Beanstalk-specific issue

---

## Next Steps

1. ✅ **Restart app server** (if you haven't after setting env vars)
2. ✅ **Check latest logs** (`/var/log/web.stdout.log`)
3. ✅ **Share the exact error message** from logs
4. ✅ **Test connection locally** to verify credentials work

---

**Action**: Restart the app server first, then check the logs for the exact error message. Share what you find!

