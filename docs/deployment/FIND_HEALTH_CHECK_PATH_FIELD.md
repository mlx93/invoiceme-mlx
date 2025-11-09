# Finding the Health Check Path Field

**Current Location**: Monitoring section (Enhanced health reporting)  
**What You Need**: Health check path/URL field

---

## The Health Check Path is NOT in Monitoring Section

The **Monitoring** section you're looking at is for:
- Health reporting (Basic vs Enhanced)
- CloudWatch metrics
- Log streaming

The **health check path** is in a **different section**.

---

## Where to Find Health Check Path

### Option 1: Look for "Load Balancer" Section

1. **Scroll UP or DOWN** from the Monitoring section
2. **Look for a section called "Load balancer"** (even if you're using single instance)
3. **Click "Edit"** on that section
4. **Look for**: "Application health check URL" or "Health check path"
5. **Set to**: `/actuator/health`

### Option 2: Look for Separate "Health" Section

1. **Scroll through all configuration sections**
2. **Look for a section called "Health"** (separate from Monitoring)
3. **Click "Edit"**
4. **Look for**: "Application health check URL"
5. **Set to**: `/actuator/health`

### Option 3: Check if Already Configured

The health check might already be set via `.ebextensions/03-healthcheck.config` file.

**To check**:
1. **Look at the current health check path** (if you see it anywhere)
2. **If it says `/actuator/health`**, it's already configured! ✅
3. **Just click "Cancel"** and test the endpoint

---

## Configuration Sections to Look For

When you're in Configuration, you should see sections like:

- **Capacity** (instance type, scaling)
- **Load balancer** ← **Check here first**
- **Rolling updates and deployments**
- **Software** (environment variables - you already configured this)
- **Monitoring** (you're here - this is different)
- **Health** ← **Or check here**
- **Security**
- **Managed platform updates**

---

## Quick Test: Skip Configuration

If you can't find the field, **test the endpoint first**:

1. **Go to Overview tab** → Copy your backend URL
2. **Test**:
   ```bash
   curl http://[your-backend-url]/actuator/health
   ```

If it works, the `.ebextensions` file already configured it!

---

## What the Field Looks Like

When you find it, you'll see something like:

```
Application health check URL: [text field]
```

Or:

```
Health check path: [text field]
```

**Set it to**: `/actuator/health`

---

**Next Step**: Scroll through the configuration sections and look for "Load balancer" or a separate "Health" section (not Monitoring).

