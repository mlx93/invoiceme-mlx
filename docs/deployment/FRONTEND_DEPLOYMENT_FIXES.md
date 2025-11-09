# Frontend Deployment Fixes - Summary

**Date**: 2025-01-27  
**Status**: ✅ **RESOLVED**  
**Platform**: AWS Elastic Beanstalk → Vercel (Frontend), AWS Elastic Beanstalk (Backend)

---

## Issues Resolved

### Issue 1: Elastic Beanstalk Startup Timeouts
**Problem**: Frontend app not binding to EB's port/host, causing health check failures  
**Solution**: Updated `package.json` start script to bind to `0.0.0.0` and use `PORT` environment variable

**Fix**:
```json
"scripts": {
  "start": "next start -H 0.0.0.0 -p ${PORT:-8080}"
}
```

---

### Issue 2: Mixed Content (HTTPS → HTTP)
**Problem**: Vercel frontend (HTTPS) calling Elastic Beanstalk backend (HTTP) caused mixed-content errors  
**Solution**: Removed browser-exposed API URL, implemented Next.js rewrites to proxy API calls server-side

**Changes**:

**`frontend/src/lib/api.ts`**:
```typescript
// Use relative path and let Next.js rewrites proxy to the backend origin
// This avoids browser mixed-content issues (HTTPS page calling HTTP API)
const API_BASE_PATH = '/api/v1';

const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_PATH,
  headers: {
    'Content-Type': 'application/json',
  },
});
```

**`frontend/next.config.ts`**:
```typescript
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  async rewrites() {
    const origin = process.env.BACKEND_API_ORIGIN || "http://localhost:8080";
    return [
      {
        source: "/api/v1/:path*",
        destination: `${origin}/api/v1/:path*`,
      },
    ];
  },
};

export default nextConfig;
```

---

### Issue 3: CORS Configuration
**Problem**: Backend CORS not allowing Vercel and Elastic Beanstalk origins  
**Solution**: Updated CORS to use `setAllowedOriginPatterns` with wildcard patterns

**Fix** (`backend/src/main/java/com/invoiceme/infrastructure/security/SecurityConfig.java`):
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    // Allow development and common production frontend origins (Vercel and EB)
    configuration.setAllowedOriginPatterns(Arrays.asList(
        "http://localhost:3000",
        "https://*.vercel.app",
        "http://*.elasticbeanstalk.com",
        "https://*.elasticbeanstalk.com"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L); // 1 hour
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

---

### Issue 4: Frontend Deployment Platform
**Problem**: Elastic Beanstalk not ideal for Next.js SSR with dynamic routes  
**Solution**: Moved frontend to Vercel (native Next.js support), kept backend on Elastic Beanstalk

**CI/CD Changes** (`.github/workflows/deploy.yml`):
```yaml
deploy-frontend:
  name: Deploy Frontend to Vercel
  # Vercel's GitHub integration auto-deploys on push. Disable this job.
  if: ${{ false }}
  runs-on: ubuntu-latest
  needs: deploy-backend
```

**Note**: Vercel's native GitHub integration handles frontend deployments automatically on push to main.

---

## Environment Variables

### Vercel Project Settings

**Required**:
- `BACKEND_API_ORIGIN`: Backend Elastic Beanstalk URL
  - Example: `http://invoiceme-mlx-back-env.eba-jj8c3aur.us-east-1.elasticbeanstalk.com`

**Optional** (if using GitHub Actions):
- `VERCEL_TOKEN`: Vercel API token
- `VERCEL_ORG_ID`: Vercel organization ID
- `VERCEL_PROJECT_ID`: Vercel project ID

**Removed**:
- `NEXT_PUBLIC_API_URL`: No longer needed (using rewrites)

### Elastic Beanstalk (Backend)

**Unchanged**:
- All existing backend environment variables remain the same
- CORS configuration updated to allow Vercel origins

---

## Architecture Changes

### Before
```
Frontend: AWS Elastic Beanstalk (Node.js 20)
Backend: AWS Elastic Beanstalk (Java 17)
```

### After
```
Frontend: Vercel (Next.js SSR - native support)
Backend: AWS Elastic Beanstalk (Java 17)
```

**Benefits**:
- ✅ Vercel optimized for Next.js (faster builds, better SSR)
- ✅ Automatic deployments via GitHub integration
- ✅ Better HTTPS/SSL support
- ✅ No manual ZIP uploads needed

---

## Deployment Flow

### Frontend (Vercel)
1. **Push to GitHub** → Vercel auto-deploys
2. **Build**: Next.js production build
3. **Deploy**: Automatic to Vercel edge network
4. **URL**: Provided by Vercel (e.g., `https://invoiceme-mlx.vercel.app`)

### Backend (Elastic Beanstalk)
1. **Push to GitHub** → GitHub Actions triggers
2. **Build**: Maven package (creates JAR)
3. **Deploy**: Uploads JAR to Elastic Beanstalk
4. **URL**: Elastic Beanstalk environment URL

---

## Local Development

### Frontend
```bash
cd frontend
npm run dev
# Runs on http://localhost:3000 (default)
# PORT env var ignored in dev mode
```

### Backend
```bash
cd backend
mvn spring-boot:run
# Runs on http://localhost:8080
```

### API Proxy
- Frontend calls `/api/v1/*` (relative)
- Next.js rewrites proxy to `http://localhost:8080/api/v1/*` (from `BACKEND_API_ORIGIN` or default)
- No CORS issues in local development

---

## Testing Checklist

- [x] Frontend binds to EB port/host (health checks pass)
- [x] Next.js rewrites proxy API calls correctly
- [x] CORS allows Vercel origins
- [x] Frontend deploys to Vercel successfully
- [x] Backend deploys to Elastic Beanstalk successfully
- [x] Frontend → Backend API calls work (via proxy)
- [x] No mixed-content errors (HTTPS → HTTP)
- [x] Local development still works

---

## Key Learnings

1. **Next.js Rewrites**: Use server-side rewrites to proxy API calls, avoiding browser mixed-content issues
2. **CORS Patterns**: Use `setAllowedOriginPatterns` with wildcards for flexible origin matching
3. **Platform Choice**: Vercel is better suited for Next.js SSR than Elastic Beanstalk
4. **Environment Variables**: Use server-only env vars (`BACKEND_API_ORIGIN`) instead of `NEXT_PUBLIC_*` for backend URLs

---

## Operational Notes

- **Local Dev**: Unset `PORT` to keep `next dev` on port 3000
- **Manual Deployments**: No longer needed for frontend (Vercel auto-deploys)
- **Backend Deployments**: Still via GitHub Actions → Elastic Beanstalk
- **Monitoring**: Use Vercel dashboard for frontend, CloudWatch for backend

---

**Status**: ✅ **ALL DEPLOYMENT ISSUES RESOLVED** - Frontend on Vercel, Backend on Elastic Beanstalk, API proxying working correctly

