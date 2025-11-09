# Fix DATABASE_URL Format for Spring Boot

**Current Format**: `jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=invoicemesupa`

**Issue**: Spring Boot's `application.yml` uses separate `DB_USERNAME` and `DB_PASSWORD`, so the URL format should not include user/password.

---

## Recommended DATABASE_URL Format

Since your `application.yml` uses:
```yaml
datasource:
  url: ${DATABASE_URL:...}
  username: ${DB_USERNAME:postgres}
  password: ${DB_PASSWORD:postgres}
```

**Use this format** (without user/password in URL):

```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
```

**Why**: Spring Boot will use `DB_USERNAME` and `DB_PASSWORD` separately, so they shouldn't be in the URL.

---

## Step 1: Update Environment Variables

1. **Go to Configuration** → **Software** → **Environment properties** → **Edit**
2. **Update `DATABASE_URL`** to:
   ```
   DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
   ```
   (Remove `?user=postgres&password=invoicemesupa` from the URL)

3. **Keep these separate**:
   ```
   DB_USERNAME=postgres
   DB_PASSWORD=invoicemesupa
   ```

4. **Click "Apply"** (environment will restart)

---

## Step 2: Restart App Server

After updating the DATABASE_URL:

1. **Actions** → **"Restart app server(s)"**
2. **Wait 5-10 minutes**
3. **Check health status**

---

## Alternative: Keep Current Format

If you want to keep user/password in the URL:

1. **Remove `DB_USERNAME` and `DB_PASSWORD`** from environment variables
2. **Keep `DATABASE_URL`** with user/password in URL
3. **Restart app server**

**But**: Your `application.yml` expects `DB_USERNAME` and `DB_PASSWORD`, so this might cause issues.

---

## Recommended: Use Separate Variables

**Best Practice**: Use separate `DB_USERNAME` and `DB_PASSWORD` (more secure, easier to manage)

**Update to**:
```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
```

---

**Next Step**: Update `DATABASE_URL` to remove user/password, then restart!

