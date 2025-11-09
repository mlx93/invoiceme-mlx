# Health Check Path Location

**Found It!** The health check path is in the **"Instance traffic and scaling"** section.

---

## Step-by-Step Instructions

### Step 1: Click "Edit" on "Instance traffic and scaling"

1. **Find the "Instance traffic and scaling"** card/section
2. **Click the "Edit" button** on the right side of that section

### Step 2: Look for Health Check Settings

Once you click Edit, you'll see sub-sections. Look for:

- **"Health"** sub-section
- **OR "Load balancer"** sub-section (if visible)
- **OR scroll down** to find health check settings

### Step 3: Find Health Check Path Field

Look for a field labeled:
- **"Application health check URL"**
- **OR "Health check path"**
- **OR "Health check"**

### Step 4: Set the Value

1. **Enter**: `/actuator/health`
2. **Click "Apply changes"** at the top (or bottom of the page)

---

## Alternative: It Might Already Be Configured

Since you have `.ebextensions/03-healthcheck.config` in your JAR file, the health check might already be set automatically!

**Quick Test First**:
1. **Go to Overview tab** → Copy your backend URL
2. **Test without configuring**:
   ```bash
   curl http://[your-backend-url]/actuator/health
   ```

If it returns `{"status":"UP"}`, it's already configured! ✅

---

## What You'll See

When you click Edit on "Instance traffic and scaling", you should see options like:

- **Instances** (instance type, scaling)
- **Health** ← **Look here for health check path**
- **Security groups**
- **Platform-specific options**

---

**Next Step**: Click "Edit" on "Instance traffic and scaling" and look for a "Health" sub-section or health check path field.

