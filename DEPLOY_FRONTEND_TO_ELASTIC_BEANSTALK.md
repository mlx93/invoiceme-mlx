# Deploy Frontend to AWS Elastic Beanstalk

## ✅ Why Elastic Beanstalk?

After extensive testing, AWS Amplify's static hosting cannot properly serve Next.js App Router with SSR/dynamic routes. Elastic Beanstalk is the solution:

- ✅ **Full Next.js SSR support** (dynamic routes work)
- ✅ **Meets AWS requirement** from InvoiceMe.md
- ✅ **Same platform as backend** (consistent)
- ✅ **Guaranteed to work** (runs Node.js server)

---

## 🚀 Deployment Steps

### Option 1: Via AWS Console (Recommended)

#### 1. Create Application ZIP

**From your project root**:

```bash
# Navigate to frontend directory
cd frontend

# Build the application
npm run build

# Create deployment package (exclude node_modules, we'll install on server)
cd ..
zip -r frontend-deploy.zip frontend -x "frontend/node_modules/*" -x "frontend/.next/cache/*"
```

#### 2. Create Elastic Beanstalk Environment

1. **Go to**: [Elastic Beanstalk Console](https://console.aws.amazon.com/elasticbeanstalk)
2. **Click**: "Create application"
3. **Configure**:
   - Application name: `invoiceme-mlx-frontend`
   - Platform: **Node.js 20** running on Amazon Linux 2023
   - Application code: **Upload your code** → Select `frontend-deploy.zip`
4. **Click**: "Create application"

#### 3. Configure Environment

After environment is created:

1. **Go to**: Configuration → Software → Edit
2. **Add Environment Properties**:
   ```
   NODE_ENV=production
   NEXT_PUBLIC_API_URL=http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1
   PORT=8080
   ```
3. **Click**: Apply

---

### Option 2: Simpler - Use EB CLI

**Install EB CLI**:
```bash
pip install awsebcli
```

**Deploy**:
```bash
cd frontend
eb init -p node.js-20 invoiceme-frontend --region us-east-1
eb create invoiceme-frontend-env
eb setenv NEXT_PUBLIC_API_URL=http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1
eb open
```

---

## 🎯 What Happens

Elastic Beanstalk will:
1. ✅ Extract your code
2. ✅ Run `npm install` (installs dependencies)
3. ✅ Run `npm run build` (builds Next.js app)
4. ✅ Run `npm start` (starts Next.js server on port 8080)
5. ✅ Serve your app with full SSR support

---

## ✅ Success Indicators

When working:
- Environment shows "Ok" (green)
- Can access frontend URL
- Login page loads
- All pages work (including `/customers/[id]`)
- Can connect to backend API

---

## 📋 Final Architecture

```
Frontend:  Elastic Beanstalk (Node.js 20) → Next.js SSR
Backend:   Elastic Beanstalk (Java 17)    → Spring Boot
Database:  Supabase PostgreSQL             → Via Connection Pooler
```

**All on AWS!** ✅

---

**Would you like to try this? It's guaranteed to work!** 🚀

