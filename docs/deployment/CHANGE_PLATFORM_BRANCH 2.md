# Change Platform Branch to Java 17

**Current Location**: Edit page for `Invoiceme-mlx-backend-env-1`  
**Section**: "Update platform version"  
**Action**: Change Platform branch dropdown

---

## Step-by-Step Instructions

### Step 1: Find "Platform branch" Dropdown

On the Edit page, you should see:

**"Update platform version"** section with:
- **Platform branch**: Dropdown showing "Choose a platform branch" ← **Click this one**
- **Platform version**: Dropdown showing "4.7.1 (Recommended)"

### Step 2: Click "Platform branch" Dropdown

1. **Click on "Platform branch"** dropdown (the one that says "Choose a platform branch")
2. **You'll see a list** of available platforms

### Step 3: Select Java 17

In the dropdown, look for and select:

**"Corretto 17 running on 64bit Amazon Linux 2023"**

**OR** it might be listed as:
- "Java 17"
- "Corretto 17"
- "Java 17 running on 64bit Amazon Linux 2023"

### Step 4: Platform Version Will Auto-Update

After selecting the platform branch:
- **Platform version** dropdown will automatically update
- **Select**: "4.7.1 (Recommended)" or latest version

### Step 5: Click "Update"

1. **"Update" button** (bottom right) should become enabled (not grayed out)
2. **Click "Update"**
3. **Confirm** the update
4. **Wait 10-15 minutes** for platform update to complete

---

## What You Should See

**Before**:
- Platform branch: "Choose a platform branch" (or shows Corretto 8)
- Platform version: "4.7.1 (Recommended)"

**After selecting**:
- Platform branch: **"Corretto 17 running on 64bit Amazon Linux 2023"**
- Platform version: "4.7.1 (Recommended)" (or latest for Java 17)

---

## Important Warning

You'll see a yellow warning box that says:
> "This operation replaces your instances; your application is unavailable during the update."

**This is normal** - the platform update will restart your environment with Java 17. The app will be unavailable for 10-15 minutes during the update.

---

## Alternative: If Platform Branch Dropdown is Empty/Disabled

If you can't select a platform branch:

1. **Try refreshing the page**
2. **Or go back** and try:
   - Configuration → Platform → Edit
   - Look for platform selection there

---

## After Update

Once the platform update completes:

1. **Check "Platform" card** on Overview:
   - Should show: **"Corretto 17 running on 64bit Amazon Linux 2023"**

2. **Check "Health" status**:
   - Should become "Healthy" (green)

3. **Test**:
   ```bash
   curl http://invoiceme-mlx-backend-env-1.eba-f9m4p8pu.us-east-1.elasticbeanstalk.com/actuator/health
   ```

---

**Next Step**: Click the **"Platform branch"** dropdown and select **"Corretto 17 running on 64bit Amazon Linux 2023"**!

