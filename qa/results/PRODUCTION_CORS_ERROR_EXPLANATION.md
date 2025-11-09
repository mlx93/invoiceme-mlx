# Production CORS Error Explanation & Fix

## What's Going Wrong

### The Error
```
Access to XMLHttpRequest at 'http://localhost:8080/api/v1/auth/login' 
from origin 'http://invoiceme-mlx-env.eba-dpfprff7.us-east-1.elasticbeanstalk.com' 
has been blocked by CORS policy: The request client is not a secure context 
and the resource is in more-private address space `loopback`.
```

### Root Cause
The frontend deployed on AWS Elastic Beanstalk is trying to make API calls to `http://localhost:8080`, which:

1. **Doesn't exist in production** - `localhost` refers to the server itself, not your backend
2. **Is blocked by browser security** - Browsers prevent requests from public domains to localhost (loopback address)
3. **Is a configuration issue** - The `NEXT_PUBLIC_API_URL` environment variable is not set

### Why It Works in Dev
- In development, `localhost:8080` is your actual backend running locally
- Browser allows localhost-to-localhost requests
- Environment variable might be set in `.env.local` or shell

### Why It Fails in Production
- Frontend code falls back to `http://localhost:8080/api/v1` when `NEXT_PUBLIC_API_URL` is not set
- Production frontend runs on AWS, not your local machine
- Browser security prevents public domain → localhost requests

## The Fix

### Step 1: Set Environment Variable in Elastic Beanstalk (Frontend)

1. **Go to**: [AWS Elastic Beanstalk Console](https://console.aws.amazon.com/elasticbeanstalk)
2. **Select**: Your frontend environment (`invoiceme-mlx-env`)
3. **Navigate**: Configuration → Software → Environment properties → Edit
4. **Add**:
   ```
   NEXT_PUBLIC_API_URL=http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1
   ```
   ⚠️ **Replace with your actual backend URL** (check your backend Elastic Beanstalk environment)
5. **Click**: Apply
6. **Wait**: Environment will restart automatically (2-3 minutes)

### Step 2: Verify Backend CORS Allows Frontend Domain

Check if backend allows requests from your frontend domain. Update if needed:

**File**: `backend/src/main/java/com/invoiceme/infrastructure/security/SecurityConfig.java`

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",  // Dev
        "http://invoiceme-mlx-env.eba-dpfprff7.us-east-1.elasticbeanstalk.com",  // Production HTTP
        "https://invoiceme-mlx-env.eba-dpfprff7.us-east-1.elasticbeanstalk.com"  // Production HTTPS (if available)
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

**Then redeploy backend** with this change.

### Step 3: Find Your Backend URL

If you don't know your backend URL:

1. **Go to**: Elastic Beanstalk Console
2. **Select**: Your backend environment
3. **Copy**: The environment URL (e.g., `http://invoiceme-mlx-back-env.eba-xxxxx.us-east-1.elasticbeanstalk.com`)
4. **Use**: `http://YOUR-BACKEND-URL/api/v1` as the `NEXT_PUBLIC_API_URL` value

## Quick Fix Commands

### Via AWS Console (Recommended)
1. Elastic Beanstalk → Frontend Environment → Configuration → Software
2. Add environment property: `NEXT_PUBLIC_API_URL` = `http://YOUR-BACKEND-URL/api/v1`
3. Apply and wait for restart

### Via EB CLI
```bash
cd frontend
eb setenv NEXT_PUBLIC_API_URL=http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1
```

### Via AWS CLI
```bash
aws elasticbeanstalk update-environment \
  --environment-name invoiceme-mlx-env \
  --option-settings \
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=NEXT_PUBLIC_API_URL,Value=http://YOUR-BACKEND-URL/api/v1 \
  --region us-east-1
```

## Verification Steps

After setting the environment variable:

1. **Wait for restart** (2-3 minutes)
2. **Open frontend URL** in browser
3. **Open DevTools** → Network tab
4. **Try to login**
5. **Check Network requests**:
   - ✅ Should see: `http://invoiceme-mlx-back-env.../api/v1/auth/login`
   - ❌ Should NOT see: `http://localhost:8080/...`

## Important Notes

- ⚠️ **Next.js Environment Variables**: Must be prefixed with `NEXT_PUBLIC_` to be available in browser code
- ⚠️ **Environment Restart**: Changes require environment restart (automatic when you apply)
- ⚠️ **Backend URL**: Make sure backend URL is correct and accessible
- ⚠️ **HTTPS**: If using HTTPS, update both frontend env var and backend CORS config
- ⚠️ **Build Time**: Environment variables are baked into the build, so you may need to rebuild/redeploy

## Why This Happens

The frontend code (`frontend/src/lib/api.ts`) has:
```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
```

When `NEXT_PUBLIC_API_URL` is not set, it defaults to `localhost:8080`, which only works in development.

## Prevention

Always set `NEXT_PUBLIC_API_URL` in production environments. Consider:
- Adding it to deployment documentation
- Adding it to CI/CD pipeline configuration
- Adding validation to fail build if not set in production

