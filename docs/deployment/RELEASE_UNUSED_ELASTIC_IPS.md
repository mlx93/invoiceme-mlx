# Release Unused Elastic IPs - Step by Step

**Current Status**: You have 5 Elastic IPs (the limit)  
**Goal**: Release unused ones to free up space for Elastic Beanstalk

---

## Step 1: Check Which Elastic IPs Are In Use

The table you're seeing doesn't show the "Associated instance" column. Let's check:

1. **Look at the table columns** - do you see a column showing which instance each IP is associated with?
2. **If not, click on each Elastic IP** individually to see its details
3. **Look for**: "Associated instance" or "Network interface" field

---

## Step 2: Identify Unused Elastic IPs

**Unused Elastic IPs** are ones that:
- Show "Not associated" or "No association"
- Are associated with stopped/terminated instances
- Are from old projects you're not using

**Based on your list**:
- `flashcard-prod` - Might be in use (check association)
- `aws-tf-starter-preview-ecs-ec2-nat-eip-...` (3 of them) - These look like preview/test environments, might be unused
- One with no name - Could be unused

---

## Step 3: Release Unused Elastic IPs

### For Each Unused Elastic IP:

1. **Select the Elastic IP** (checkbox on the left)
2. **Click "Actions" dropdown** (top right)
3. **Select "Release Elastic IP addresses"**
4. **Confirm** the release
5. **Repeat** for all unused ones

**⚠️ Important**: 
- Only release ones that are truly unused
- If you're not sure, check if the associated instance is running
- You can always allocate a new one later if needed

---

## Step 4: After Releasing IPs

Once you've freed up at least 1 Elastic IP:

1. **Go back to Elastic Beanstalk**
2. **Terminate the failed environment**:
   - Environment → Actions → Terminate environment
   - Wait 5-10 minutes

3. **Create new environment** with **Single Instance**:
   - This might not even need an Elastic IP (simpler setup)
   - Region: `us-east-1`
   - Environment type: **Single instance**
   - Upload your JAR file

---

## Quick Check: Which Ones Can You Release?

**Look for**:
- Elastic IPs from old/test projects (like `aws-tf-starter-preview-*`)
- Ones with no name (unnamed)
- Ones showing "Not associated"

**Keep**:
- `flashcard-prod` (if it's actively being used)

---

## Alternative: Use Single Instance (Might Not Need Elastic IP)

**Good News**: Single instance environments often don't require Elastic IPs!

**Try This**:
1. **Release 1-2 unused Elastic IPs** (to be safe)
2. **Terminate current environment**
3. **Create new environment** with:
   - **Environment type**: **Single instance** (not Load balanced)
   - This simpler setup might avoid the Elastic IP requirement entirely

---

## What to Do Right Now

1. **Click on each Elastic IP** to see if it's associated with a running instance
2. **Release any that are unused** (especially the `aws-tf-starter-preview-*` ones if they're from old projects)
3. **Keep at least 1-2 free** for Elastic Beanstalk

**Question**: Are any of those `aws-tf-starter-preview-*` Elastic IPs from old projects you're not using anymore? Those would be good candidates to release.

---

**Next Step**: Check which Elastic IPs are associated with running instances, then release the unused ones!

