# Production CORS Error Fix

## Problem
Frontend deployed on AWS Elastic Beanstalk is trying to connect to `http://localhost:8080/api/v1/auth/login` instead of the actual backend URL. This causes:
1. **CORS Error**: Browser blocks requests from production domain to localhost
2. **Network Error**: `localhost:8080` doesn't exist in the production environment

## Root Cause
The frontend API client (`frontend/src/lib/api.ts`) uses:
```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
```

The `NEXT_PUBLIC_API_URL` environment variable is **not set** in the Elastic Beanstalk environment, so it falls back to `localhost:8080`.

## Solution

### Step 1: Set Environment Variable in Elastic Beanstalk

1. **Go to**: AWS Elastic Beanstalk Console
2. **Select**: Your frontend environment (`invoiceme-mlx-env`)
3. **Navigate**: Configuration → Software → Edit
4. **Add Environment Property**:
   ```
   NEXT_PUBLIC_API_URL=http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1
   ```
   ⚠️ **Replace with your actual backend URL if different**
5. **Click**: Apply
6. **Wait**: Environment will restart (2-3 minutes)

### Step 2: Verify Backend CORS Configuration

Ensure backend allows requests from frontend domain. Check `SecurityConfig.java`:

```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",  // Dev
    "http://invoiceme-mlx-env.eba-dpfprff7.us-east-1.elasticbeanstalk.com"  // Production
));
```

**Or use wildcard** (less secure but easier):
```java
configuration.setAllowedOrigins(Arrays.asList("*"));  // Allow all origins
```

### Step 3: Update Backend CORS (If Needed)

If backend doesn't allow frontend origin, update `backend/src/main/java/com/invoiceme/infrastructure/security/SecurityConfig.java`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "http://invoiceme-mlx-env.eba-dpfprff7.us-east-1.elasticbeanstalk.com",
        "https://invoiceme-mlx-env.eba-dpfprff7.us-east-1.elasticbeanstalk.com"  // HTTPS if available
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

Then **redeploy backend** with this change.

## Quick Fix Commands

### Via AWS Console (Easiest)
1. Elastic Beanstalk → Frontend Environment → Configuration → Software
2. Add: `NEXT_PUBLIC_API_URL` = `http://YOUR-BACKEND-URL/api/v1`
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
    Namespace=aws:elasticbeanstalk:application:environment,OptionName=NEXT_PUBLIC_API_URL,Value=http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1 \
  --region us-east-1
```

## Verification

After setting the environment variable and restarting:

1. **Check Environment Variable**:
   - Elastic Beanstalk → Configuration → Software
   - Verify `NEXT_PUBLIC_API_URL` is set correctly

2. **Test Frontend**:
   - Open frontend URL in browser
   - Open DevTools → Network tab
   - Try to login
   - Verify API requests go to backend URL (not localhost)

3. **Check Console**:
   - Should see requests to: `http://invoiceme-mlx-back-env.../api/v1/auth/login`
   - Should NOT see requests to: `http://localhost:8080/...`

## Important Notes

- ⚠️ **Next.js Environment Variables**: Must be prefixed with `NEXT_PUBLIC_` to be available in browser
- ⚠️ **Environment Restart**: Changes require environment restart (automatic when you apply)
- ⚠️ **Backend URL**: Make sure backend URL is correct and accessible
- ⚠️ **HTTPS**: If using HTTPS, update both frontend env var and backend CORS config

## Alternative: Hardcode for Production (Not Recommended)

If you can't set environment variables, you could modify `api.ts` to detect production:

```typescript
const getApiBaseUrl = () => {
  // In production (Elastic Beanstalk)
  if (typeof window !== 'undefined' && window.location.hostname.includes('elasticbeanstalk.com')) {
    return 'http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com/api/v1';
  }
  // Development
  return process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';
};

const API_BASE_URL = getApiBaseUrl();
```

**But setting environment variable is the correct solution!**

