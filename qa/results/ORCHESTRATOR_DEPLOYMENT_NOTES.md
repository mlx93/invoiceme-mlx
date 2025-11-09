# Deployment Process Notes for Orchestrator

## Critical Information

### 1. Database Changes Require Flyway Migrations + Backend Redeployment

**Every database change** must follow this process:
1. Create/update Flyway migration file in `backend/src/main/resources/db/migration/`
2. If modifying existing migration → run `mvn flyway:repair` to update checksum
3. Test migration locally
4. Commit changes
5. **Backend must be redeployed** for changes to take effect (Flyway runs migrations on startup)

**Example**: When we updated the admin password hash in `V12__create_initial_admin_user.sql`, we had to:
- Run `mvn flyway:repair` to fix checksum validation
- Restart backend for changes to apply

---

### 2. JWT Secret Key Must Be Updated in Production

**Current Status**:
- ✅ Local (`application.yml`): Updated with new 64-character secret
- ⚠️ **Production (Elastic Beanstalk)**: **NEEDS UPDATE** - Still has old 44-character secret

**New JWT Secret**: `3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8`

**Why**: HS512 algorithm requires minimum 512 bits (64 characters). Previous secret was 352 bits (44 chars) → caused `WeakKeyException`.

**Where to Update**:
1. **Local**: Already in `application.yml` as default fallback
2. **Production**: AWS Elastic Beanstalk Console → Configuration → Software → Environment properties → Update `JWT_SECRET` → Click Apply (auto-restarts)

**Documentation Updated**:
- ✅ `ELASTIC_BEANSTALK_ENV_VALUES.txt`
- ✅ `ELASTIC_BEANSTALK_ENV_VARS.md`

---

### 3. Configuration Persistence

**JWT Secret Configuration**:
- `application.yml` uses: `secret: ${JWT_SECRET:default_value}`
- Local development: Uses default if `JWT_SECRET` env var not set
- Production: Uses `JWT_SECRET` environment variable (overrides default)
- **Both must be updated** when changing secrets

**Other Configuration**:
- Database URL, AWS credentials, etc. follow same pattern
- Local: `application.yml` defaults
- Production: Elastic Beanstalk environment variables

---

## Quick Reference

### Database Change Workflow
```
1. Create migration file → 2. Test locally → 3. Commit → 4. Deploy backend → 5. Flyway runs on startup
```

### Configuration Change Workflow
```
1. Update application.yml (local) → 2. Update Elastic Beanstalk env vars (production) → 3. Restart
```

### Current JWT Secret
```
3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8
(Must be updated in Elastic Beanstalk Console)
```

---

**Key Takeaway**: Database changes = Migration file + Backend redeployment. Configuration changes = Update both local defaults AND production environment variables.

