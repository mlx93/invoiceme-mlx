# Restart Environment to Apply Environment Variables

**Issue**: Environment variables added but application hasn't restarted  
**Solution**: Restart the environment so Spring Boot picks up new variables

---

## Step 1: Restart App Server

1. **Go to Elastic Beanstalk** → Environment: `invoiceme-mlx-backend-env-1`
2. **Click "Actions" dropdown** (top right)
3. **Select "Restart app server(s)"**
4. **Confirm** the restart
5. **Wait 5-10 minutes** for restart to complete

**This will**:
- Restart Spring Boot application
- Load all environment variables
- Connect to database with new `DATABASE_URL`

---

## Step 2: Monitor Restart

1. **Go to "Events" tab** (left sidebar)
2. **Watch for**:
   - "Restarting application server"
   - "Successfully restarted application server"
   - Health status changing

3. **Check "Health" status**:
   - Should go: "Degraded" → "Warning" → "Healthy" (green)

---

## Step 3: Verify DATABASE_URL Format

Your current `DATABASE_URL`:
```
jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa
```

**Note**: Since you also have `DB_USERNAME` and `DB_PASSWORD` set separately, Spring Boot will use those from `application.yml`. The `DATABASE_URL` format you have should work, but if you get database connection errors, try:

**Option 1**: Keep current format (should work)
```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa
```

**Option 2**: Remove user/password from URL (if using DB_USERNAME/DB_PASSWORD)
```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
```

**Your current setup** (with both) should work fine - Spring Boot will use DB_USERNAME and DB_PASSWORD from the datasource configuration.

---

## Step 4: Test After Restart

Once health is "Healthy":

```bash
curl http://invoiceme-mlx-backend-env-1.eba-f9m4p8pu.us-east-1.elasticbeanstalk.com/actuator/health
```

Expected: `{"status":"UP"}`

---

## If Still Getting 502 After Restart

1. **Request logs**:
   - Logs tab → Request logs → Last 100 lines
   - Look for database connection errors

2. **Check Events tab**:
   - Look for restart errors

3. **Verify environment variables**:
   - Configuration → Software → Environment properties
   - Make sure all are still there after restart

---

**Next Step**: Click "Actions" → "Restart app server(s)" and wait for it to complete!

