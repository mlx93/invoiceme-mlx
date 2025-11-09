# Platform Branch Locked - Solutions

**Problem**: Platform branch dropdown is locked/disabled  
**Reason**: Elastic Beanstalk doesn't allow changing platform branch on existing environments  
**Solution**: Create new environment with correct platform OR use platform version update

---

## Solution 1: Create New Environment with Java 17 (Recommended)

Since you can't change the platform branch, create a new environment with the correct platform:

### Step 1: Note Your Current Environment Variables

1. **Go to Configuration** → **Software** → **Environment properties**
2. **Copy all environment variables** (or use `ELASTIC_BEANSTALK_ENV_VALUES.txt`)
3. **Save them** - you'll need to add them to the new environment

### Step 2: Create New Environment

1. **Go to Application**: `invoiceme-mlx-backend`
2. **Click "Create environment"**
3. **Configure**:
   - **Environment name**: `invoiceme-mlx-backend-env-2` (or any new name)
   - **Platform**: **Java** → **Corretto 17** → **Java 17 running on 64bit Amazon Linux 2023** ⚠️ **IMPORTANT**
   - **Platform version**: Latest (4.7.1)
   - **Application code**: Upload `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
   - **Region**: `us-east-1` ⚠️ **VERIFY**
   - **Environment type**: **Single instance**
4. **Continue through wizard** (use defaults for other settings)
5. **Create environment** (wait 10-15 minutes)

### Step 3: Add Environment Variables

Once new environment is healthy:

1. **Configuration** → **Software** → **Environment properties** → **Edit**
2. **Add all variables** from `ELASTIC_BEANSTALK_ENV_VALUES.txt`
3. **Click "Apply"**

### Step 4: Terminate Old Environment

After new environment is working:

1. **Go to old environment**: `invoiceme-mlx-backend-env-1`
2. **Actions** → **Terminate environment**
3. **Confirm** termination

---

## Solution 2: Check if Platform Version Update Works

Sometimes you can update the platform version even if branch is locked:

1. **Try clicking "Platform version" dropdown** (not branch)
2. **See if there are Java 17 versions** available
3. **If yes**, select one and click "Update"

**Note**: This usually doesn't work if the branch is different, but worth trying.

---

## Solution 3: Clone Environment (Advanced)

1. **Go to old environment**
2. **Actions** → **Clone environment**
3. **During clone**, you can select **Java 17 platform**
4. **After clone is healthy**, swap URLs or terminate old one

---

## Recommended: Solution 1 (Create New Environment)

**Why**: 
- Cleanest approach
- Ensures correct platform from start
- Avoids platform migration issues

**Steps**:
1. ✅ Create new environment with **Java 17 (Corretto 17)**
2. ✅ Upload JAR file
3. ✅ Add environment variables
4. ✅ Test health endpoint
5. ✅ Terminate old environment

---

## Quick Checklist for New Environment

- [ ] Platform: **Corretto 17** (NOT Corretto 8)
- [ ] Region: **us-east-1**
- [ ] Environment type: **Single instance**
- [ ] JAR file: `backend/target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
- [ ] Environment variables: Copy from `ELASTIC_BEANSTALK_ENV_VALUES.txt`

---

**Next Step**: Create a new environment with Java 17 platform from the start. This is the cleanest solution!

