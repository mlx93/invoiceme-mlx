# JWT Secret Source and Configuration

## Where Did the New JWT Secret Come From?

The new JWT secret `3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8` was **generated using Python's `secrets` module**:

```python
import secrets
# Generate a 64-character (512-bit) secret for HS512
secret = secrets.token_urlsafe(48)  # 48 bytes = 64 base64 chars = 512 bits
```

**Why 64 characters?**
- HS512 algorithm requires **minimum 512 bits** (64 characters)
- Previous secret was 44 characters (352 bits) → caused `WeakKeyException`
- New secret: 64 characters (512 bits) → compliant with HS512

---

## Where to Update JWT_SECRET

### ✅ Already Updated (Local Development)
- **File**: `backend/src/main/resources/application.yml`
- **Line**: `secret: ${JWT_SECRET:3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8}`
- **Status**: ✅ Updated (default fallback for local dev)

### ⚠️ NEEDS UPDATE (Production - AWS Elastic Beanstalk)
- **Location**: AWS Elastic Beanstalk Console
- **Path**: Configuration → Software → Environment properties
- **Variable**: `JWT_SECRET`
- **New Value**: `3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8`
- **Action**: Update and click "Apply" (triggers auto-restart)

### ✅ Documentation Updated
- `docs/deployment/ELASTIC_BEANSTALK_ENV_VALUES.txt` - ✅ Updated
- `qa/results/DEPLOYMENT_PROCESS_NOTES.md` - ✅ Documented

---

## PostgreSQL - NO Changes Needed

**PostgreSQL does NOT need JWT_SECRET** - it's a backend application configuration, not a database setting.

JWT_SECRET is used by:
- Spring Boot backend (`JwtTokenProvider`)
- NOT by PostgreSQL database

---

## AWS Elastic Beanstalk - REQUIRED Update

**You MUST update `JWT_SECRET` in AWS Elastic Beanstalk** for production to work:

### Steps:
1. Go to: AWS Console → Elastic Beanstalk → Your Environment
2. Click: **Configuration** (left sidebar)
3. Scroll to: **Software** section
4. Click: **Edit** button
5. Find: `JWT_SECRET` in Environment properties
6. Update to: `3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8`
7. Click: **Apply** (environment will restart automatically)

### Why This Matters:
- Old secret (44 chars) → Causes `WeakKeyException` → Login fails
- New secret (64 chars) → HS512 compliant → Login works
- **Without this update, production login will fail**

---

## Summary

| Location | Status | Action Required |
|----------|--------|----------------|
| `application.yml` (local) | ✅ Updated | None |
| AWS Elastic Beanstalk | ⚠️ **NEEDS UPDATE** | Update `JWT_SECRET` env var |
| PostgreSQL | N/A | No changes needed |
| Documentation | ✅ Updated | None |

**Critical**: Update AWS Elastic Beanstalk `JWT_SECRET` environment variable before deploying to production!

