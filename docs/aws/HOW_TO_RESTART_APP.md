# How to Restart Elastic Beanstalk App

**Environment**: `invoiceme-mlx-backend-env-1`  
**Goal**: Restart the application to pick up environment variables

---

## Step-by-Step Instructions

### Step 1: Navigate to Your Environment

1. **Go to AWS Console** → **Elastic Beanstalk**
2. **Click "Environments"** (left sidebar)
3. **Click on**: `invoiceme-mlx-backend-env-1`

### Step 2: Find Actions Menu

1. **Look at the TOP RIGHT** of the page
2. **Find the "Actions" dropdown button** (next to "Upload and deploy" button)
3. **Click the "Actions" dropdown**

### Step 3: Select Restart Option

In the Actions dropdown, you'll see options. Choose one:

**Option A: Restart App Server (Recommended - Faster)**
- **Click**: "Restart app server(s)"
- **Faster** (5-10 minutes)
- **Restarts just the application**, keeps environment running

**Option B: Restart Environment (Full Restart)**
- **Click**: "Restart environment"
- **Slower** (10-15 minutes)
- **Restarts everything** (app + infrastructure)

### Step 4: Confirm Restart

1. **A confirmation dialog** will appear
2. **Click "Restart"** or "Confirm" to proceed
3. **Wait for restart** to complete

---

## Step 5: Monitor Restart Progress

1. **Go to "Events" tab** (left sidebar)
2. **Watch for**:
   - "Restarting application server" (or "Restarting environment")
   - "Successfully restarted application server"
   - Health status updates

3. **Check "Health" status**:
   - Should change: "Degraded" → "Warning" → "Healthy" (green)
   - Takes 5-10 minutes for app server restart
   - Takes 10-15 minutes for full environment restart

---

## Visual Guide

```
┌─────────────────────────────────────────────┐
│  Elastic Beanstalk Console                  │
│                                             │
│  [Refresh] [Actions ▼] [Upload and deploy] │
│                    ↑                        │
│              Click here                     │
└─────────────────────────────────────────────┘

After clicking Actions:
┌─────────────────────────┐
│ Actions Menu:           │
│ • Restart app server(s) │ ← Click this
│ • Restart environment   │
│ • ...                   │
└─────────────────────────┘
```

---

## After Restart

Once health shows "Healthy" (green):

1. **Test health endpoint**:
   ```bash
   curl http://invoiceme-mlx-backend-env-1.eba-f9m4p8pu.us-east-1.elasticbeanstalk.com/actuator/health
   ```

2. **Expected**: `{"status":"UP"}`

---

## Troubleshooting

### Can't Find Actions Button

- **Look for**: Three dots menu (⋯) or dropdown arrow
- **Location**: Top right, near "Upload and deploy" button
- **Alternative**: Look for "Environment actions" or similar

### Restart Takes Too Long

- **Normal**: 5-10 minutes for app server restart
- **If > 15 minutes**: Check Events tab for errors
- **If stuck**: Check Logs tab for issues

### Health Still Degraded After Restart

- **Request logs**: Logs tab → Request logs → Last 100 lines
- **Check Events**: Look for restart errors
- **Verify environment variables**: Make sure all are still set

---

## Quick Summary

1. ✅ **Go to environment**: `invoiceme-mlx-backend-env-1`
2. ✅ **Click "Actions" dropdown** (top right)
3. ✅ **Select "Restart app server(s)"**
4. ✅ **Confirm** the restart
5. ✅ **Wait 5-10 minutes**
6. ✅ **Check Events tab** for progress
7. ✅ **Test**: `curl http://[your-url]/actuator/health`

---

**Ready to restart!** 🚀

