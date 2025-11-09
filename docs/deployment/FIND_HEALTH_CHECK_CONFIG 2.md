# Where to Find Health Check Configuration

**Current Location**: You're on the **"Health" tab** (for monitoring)  
**Needed Location**: **"Configuration" section** (for editing settings)

---

## Step-by-Step: Find Health Check Configuration

### Step 1: Click "Configuration" in Left Sidebar

1. **Look at the LEFT SIDEBAR** (not the tabs at the bottom)
2. **Under "Environment: Invoiceme-mlx-backend-env"** section
3. **Click "Configuration"** (it's a link in the sidebar, not a tab)

**Visual Guide**:
```
Left Sidebar:
├── Environment: Invoiceme-mlx-backend-env
│   ├── Go to environment
│   ├── Configuration  ← CLICK THIS ONE
│   ├── Events
│   ├── Health (you're here - this is just for viewing)
│   ├── Logs
│   └── ...
```

### Step 2: Find Health Check Section

Once you're in **Configuration**, you'll see multiple sections:

1. **Scroll down** through the configuration sections
2. **Look for one of these sections**:
   - **"Load balancer"** section (if using load balanced environment)
   - **OR "Health"** section (if using single instance)

### Step 3: Edit Health Check

1. **Find the section** (Load balancer or Health)
2. **Click "Edit"** button on the right side of that section
3. **Look for**: "Application health check URL" or "Health check path"
4. **Set to**: `/actuator/health`
5. **Click "Apply"** at the bottom

---

## Alternative: Check if Already Configured

The health check might already be configured via the `.ebextensions/03-healthcheck.config` file that was included in your JAR.

**To verify**:
1. **Go to Configuration** (left sidebar)
2. **Look for "Health" or "Load balancer"** section
3. **Check if health check path is already set** to `/actuator/health`

If it's already set, you can skip configuration and go straight to testing!

---

## If You Still Don't See It

### For Single Instance Environments:

1. **Configuration** → Scroll to **"Health"** section
2. **Click "Edit"**
3. **Look for**: "Application health check URL"
4. **Set to**: `/actuator/health`

### For Load Balanced Environments:

1. **Configuration** → Scroll to **"Load balancer"** section
2. **Click "Edit"**
3. **Look for**: "Application health check URL" or "Health check path"
4. **Set to**: `/actuator/health`

---

## Quick Test: Skip Configuration for Now

If you can't find the health check configuration, you can **test it directly** first:

1. **Get your backend URL** from Overview tab
2. **Test the endpoint**:
   ```bash
   curl http://[your-backend-url]/actuator/health
   ```

If it works, the health check might already be configured via `.ebextensions`!

---

## What You Should See in Configuration

When you click **"Configuration"** in the left sidebar, you should see sections like:

- **Capacity** (instance configuration)
- **Load balancer** (or **Health** for single instance) ← **This is where health check is**
- **Rolling updates and deployments**
- **Software** (environment variables)
- **Security**
- etc.

---

**Key Point**: The **"Health" tab** (where you are now) is for **monitoring/viewing** health metrics.  
The **"Configuration" section** (in left sidebar) is for **editing** settings like health check path.

