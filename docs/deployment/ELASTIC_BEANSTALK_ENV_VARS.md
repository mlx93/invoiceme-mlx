# Elastic Beanstalk Environment Variables - Ready to Copy

**Environment**: `Invoiceme-mlx-backend-env`  
**Application**: `invoiceme-mlx-backend`

---

## Where to Add Environment Variables

1. **In Elastic Beanstalk Console**:
   - Go to your environment: `Invoiceme-mlx-backend-env`
   - Click **Configuration** (left sidebar)
   - Scroll down to **Software** section
   - Click **Edit** button
   - Scroll to **Environment properties** section
   - Click **Add environment property** for each variable below

---

## Environment Variables to Add

Copy and paste these values:

### Database Configuration

```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=[YOUR_SUPABASE_PASSWORD]
```

**⚠️ ACTION REQUIRED**: Replace `[YOUR_SUPABASE_PASSWORD]` with your actual Supabase database password. You can find this in:
- Supabase Dashboard → Project Settings → Database → Database password
- Or check your password manager if you saved it during setup

```
DB_USERNAME=postgres
```

```
DB_PASSWORD=[YOUR_SUPABASE_PASSWORD]
```

**⚠️ ACTION REQUIRED**: Replace `[YOUR_SUPABASE_PASSWORD]` with your actual Supabase password (same as above)

### JWT Configuration

```
JWT_SECRET=3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8
```

**✅ UPDATED**: This is the new JWT secret (64 characters, 512 bits) required for HS512 algorithm. **MUST be updated in Elastic Beanstalk environment variables.**

### AWS Configuration

```
AWS_REGION=us-east-1
```

**✅ READY TO USE**: This is correct for your setup

```
AWS_ACCESS_KEY_ID=[YOUR_AWS_ACCESS_KEY]
```

**⚠️ ACTION REQUIRED**: Get this from:
- GitHub Secrets: https://github.com/mlx93/invoiceme-mlx/settings/secrets/actions
- Or AWS Console → IAM → Users → `invoiceme-deploy-mlx` → Security credentials
- Or your password manager

```
AWS_SECRET_ACCESS_KEY=[YOUR_AWS_SECRET_KEY]
```

**⚠️ ACTION REQUIRED**: Get this from the same place as AWS_ACCESS_KEY_ID

```
AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
```

**✅ READY TO USE**: This is your verified SES email

```
AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
```

**✅ READY TO USE**: This is your S3 bucket name

### Server Configuration

```
SERVER_PORT=5000
```

**✅ READY TO USE**: Required for Elastic Beanstalk

```
SPRING_PROFILES_ACTIVE=production
```

**✅ READY TO USE**: Sets production profile

### Logging (Optional but Recommended)

```
LOGGING_LEVEL_ROOT=INFO
```

```
LOGGING_LEVEL_COM_INVOICEME=INFO
```

---

## Quick Copy-Paste Format

Here's the format ready to paste into Elastic Beanstalk (replace the placeholders):

```
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres?user=postgres&password=YOUR_PASSWORD_HERE
DB_USERNAME=postgres
DB_PASSWORD=YOUR_PASSWORD_HERE
JWT_SECRET=3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=YOUR_ACCESS_KEY_HERE
AWS_SECRET_ACCESS_KEY=YOUR_SECRET_KEY_HERE
AWS_SES_FROM_EMAIL=mylesethan93@gmail.com
AWS_S3_BUCKET_NAME=invoiceme-pdfs-mlx
SERVER_PORT=5000
SPRING_PROFILES_ACTIVE=production
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_INVOICEME=INFO
```

---

## Where to Configure Health Check Path

The health check path is configured in a **different section**:

1. **In Elastic Beanstalk Console**:
   - Go to your environment: `Invoiceme-mlx-backend-env`
   - Click **Configuration** (left sidebar)
   - Find **Load balancer** section (or **Health** section, depending on your setup)
   - Click **Edit**
   - Look for **"Application health check URL"** or **"Health check path"**
   - Set it to: `/actuator/health`
   - Click **Apply**

**Note**: If you're using a **single instance** (not load balanced), the health check might be under:
- **Configuration** → **Health** → **Edit** → **Application health check URL**

**Alternative**: If you don't see the health check option yet, you can configure it after the environment is created and healthy. The `.ebextensions/03-healthcheck.config` file should handle this automatically, but you can also set it manually in the console.

---

## How to Get Missing Values

### Supabase Password
1. Go to: https://supabase.com/dashboard
2. Select your project: `invoiceme`
3. Go to: **Settings** → **Database**
4. Look for **Database password** section
5. If you forgot it, you can reset it (but this will require updating connection strings)

### AWS Credentials
1. **Option 1 - GitHub Secrets** (easiest):
   - Go to: https://github.com/mlx93/invoiceme-mlx/settings/secrets/actions
   - View `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`
   - Copy the values

2. **Option 2 - AWS Console**:
   - Go to: https://console.aws.amazon.com/iam/
   - Users → `invoiceme-deploy-mlx`
   - Security credentials tab
   - Access keys section
   - If you don't see them, you'll need to create new ones

---

## After Adding Variables

1. Click **Apply** at the bottom
2. Wait for environment to restart (2-5 minutes)
3. Check **Events** tab to monitor progress
4. Once healthy (green), test:
   ```bash
   curl http://[your-backend-url]/actuator/health
   ```

---

**Last Updated**: 2025-01-27

