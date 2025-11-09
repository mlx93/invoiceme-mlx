# Deployment Operations Guide

**Purpose**: Critical operational procedures for maintaining and deploying InvoiceMe application  
**Last Updated**: 2025-01-27

---

## 🔐 JWT Secret Key Management

### Requirements
- **Algorithm**: HS512 (HMAC SHA-512)
- **Key Length**: 64 characters (512 bits) minimum
- **Format**: Base64-encoded string

### Current Production Secret
```
3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8
```
(64 characters, 512 bits)

### Configuration Locations

**Local Development** (`backend/src/main/resources/application.yml`):
```yaml
jwt:
  secret: ${JWT_SECRET:3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8}
```

**Production** (AWS Elastic Beanstalk Environment Variables):
- Variable: `JWT_SECRET`
- Value: `3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8`

### ⚠️ Critical Rule
**Every JWT secret change requires updating BOTH locations:**
1. ✅ Update `application.yml` (local default)
2. ✅ Update Elastic Beanstalk environment variables (production)
3. ✅ Restart backend application/environment

**Why**: If secrets don't match, existing JWT tokens will be invalidated, causing all users to be logged out.

---

## 🗄️ Database Migration Management

### Flyway Migration Process

**Location**: `backend/src/main/resources/db/migration/`

**File Naming Convention**: `V{version}__{description}.sql`
- Example: `V12__create_initial_admin_user.sql`
- Version numbers must be sequential
- Use double underscore `__` between version and description

### Creating New Migrations

1. **Create Migration File**:
   ```bash
   # Create new migration file
   touch backend/src/main/resources/db/migration/V13__your_migration_name.sql
   ```

2. **Write SQL**:
   ```sql
   -- V13__your_migration_name.sql
   CREATE TABLE new_table (
       id UUID PRIMARY KEY,
       name VARCHAR(255) NOT NULL
   );
   ```

3. **Test Locally**:
   ```bash
   cd backend
   mvn spring-boot:run
   # Flyway runs migrations automatically on startup
   ```

4. **Verify Migration**:
   - Check database: `SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;`
   - Verify tables/columns created correctly

5. **Deploy**:
   - Commit migration file to Git
   - Push to `main` branch
   - Backend deployment triggers Flyway migration automatically

### Modifying Existing Migrations

**⚠️ WARNING**: Modifying existing migrations can break deployments if they've already run.

**If Migration Not Yet Deployed**:
1. Modify migration file directly
2. Test locally
3. Commit and deploy

**If Migration Already Deployed**:
1. **DO NOT** modify existing migration file
2. Create new migration file to fix/update schema
3. Example: If V10 has an error, create V11 to fix it

**If Migration Failed**:
```bash
# Repair Flyway schema history
mvn flyway:repair

# Then fix migration file and redeploy
```

### Migration Best Practices

- ✅ **Always test migrations locally first**
- ✅ **Use transactions** (PostgreSQL supports DDL in transactions)
- ✅ **Make migrations idempotent** (use `IF NOT EXISTS`, `IF EXISTS`)
- ✅ **Version sequentially** (don't skip version numbers)
- ✅ **Document complex migrations** (add comments in SQL)
- ❌ **Never modify deployed migrations** (create new migration instead)
- ❌ **Never delete migration files** (breaks Flyway history)

---

## ⚙️ Configuration Management

### Two-Location Rule

**Every configuration change requires updating BOTH:**

1. **Local Default** (`backend/src/main/resources/application.yml`):
   ```yaml
   jwt:
     secret: ${JWT_SECRET:default-value-here}
   ```

2. **Production** (AWS Elastic Beanstalk Environment Variables):
   - Go to: Elastic Beanstalk → Configuration → Software → Environment properties
   - Update variable value
   - Apply changes (triggers environment restart)

### Configuration Variables Reference

| Variable | Local Default | Production Location | Notes |
|-----------|--------------|---------------------|-------|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/invoiceme` | Elastic Beanstalk env vars | Supabase connection string |
| `JWT_SECRET` | `3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8` | Elastic Beanstalk env vars | 64 chars, HS512 |
| `AWS_REGION` | `us-east-1` | Elastic Beanstalk env vars | AWS region |
| `AWS_ACCESS_KEY_ID` | (empty) | Elastic Beanstalk env vars | IAM user access key |
| `AWS_SECRET_ACCESS_KEY` | (empty) | Elastic Beanstalk env vars | IAM user secret key |
| `AWS_SES_FROM_EMAIL` | `mylesethan93@gmail.com` | Elastic Beanstalk env vars | Verified SES email |
| `AWS_S3_BUCKET_NAME` | `invoiceme-pdfs-mlx` | Elastic Beanstalk env vars | S3 bucket name |
| `SERVER_PORT` | `8080` | Elastic Beanstalk env vars | Port 5000 for EB |

### Configuration Change Workflow

1. **Update Local Default**:
   - Edit `application.yml`
   - Test locally: `mvn spring-boot:run`
   - Verify change works

2. **Update Production**:
   - AWS Console → Elastic Beanstalk → Configuration
   - Software → Environment properties → Edit
   - Update variable value
   - Apply changes (restarts environment)

3. **Verify Production**:
   - Check application logs in CloudWatch
   - Test API endpoints
   - Verify functionality

---

## 🔄 Deployment Workflow

### Standard Deployment Process

1. **Local Testing**:
   ```bash
   cd backend
   mvn clean package -DskipTests
   mvn spring-boot:run
   # Test locally
   ```

2. **Commit Changes**:
   ```bash
   git add .
   git commit -m "feat: your change description"
   git push origin main
   ```

3. **CI/CD Pipeline** (automatic):
   - GitHub Actions workflow triggers
   - Backend: Builds JAR → Deploys to Elastic Beanstalk
   - Frontend: Amplify auto-deploys from GitHub

4. **Verify Deployment**:
   ```bash
   ./scripts/verify-deployment.sh
   # Or manually test endpoints
   ```

### Manual Deployment (if CI/CD fails)

**Backend**:
1. Build JAR: `mvn clean package -DskipTests`
2. AWS Console → Elastic Beanstalk → Upload and Deploy
3. Upload: `target/invoiceme-backend-1.0.0-SNAPSHOT.jar`
4. Deploy

**Frontend**:
1. AWS Console → Amplify → App → Deploy
2. Or push to GitHub (Amplify auto-deploys)

---

## 🚨 Common Issues & Solutions

### Issue: JWT Tokens Invalid After Deployment

**Cause**: JWT secret changed in production but not in `application.yml`, or vice versa.

**Solution**:
1. Verify JWT secret matches in both locations
2. Update mismatched location
3. Restart backend/environment
4. Users will need to re-login (tokens invalidated)

### Issue: Database Migration Failed

**Cause**: Migration SQL error, version conflict, or Flyway schema history corrupted.

**Solution**:
```bash
# Check Flyway schema history
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;

# Repair if needed
mvn flyway:repair

# Fix migration file
# Test locally
# Redeploy
```

### Issue: Configuration Not Applied

**Cause**: Updated `application.yml` but not Elastic Beanstalk environment variables (or vice versa).

**Solution**:
1. Verify configuration in both locations
2. Update missing location
3. Restart backend/environment
4. Check application logs for errors

### Issue: Environment Variables Not Loading

**Cause**: Typo in variable name, missing variable, or environment not restarted.

**Solution**:
1. Verify variable names match exactly (case-sensitive)
2. Check Elastic Beanstalk environment variables
3. Restart environment
4. Check CloudWatch logs for errors

---

## 📋 Pre-Deployment Checklist

Before deploying any changes:

- [ ] **Database Migrations**:
  - [ ] Migration file created with correct version number
  - [ ] Migration tested locally
  - [ ] SQL syntax verified
  - [ ] Migration is idempotent (if possible)

- [ ] **Configuration Changes**:
  - [ ] `application.yml` updated (if needed)
  - [ ] Elastic Beanstalk environment variables updated (if needed)
  - [ ] Both locations match (if JWT secret changed)

- [ ] **Code Changes**:
  - [ ] Code tested locally
  - [ ] No compilation errors
  - [ ] Integration tests pass (if applicable)

- [ ] **Deployment**:
  - [ ] Changes committed to Git
  - [ ] Pushed to `main` branch
  - [ ] CI/CD pipeline runs successfully
  - [ ] Deployment verified (health check, API tests)

---

## 📚 Reference Documents

- **Deployment Guide**: `/docs/deployment.md`
- **Migration Strategy**: `/backend/docs/migrations.md`
- **Database Schema**: `/backend/docs/database-schema.md`
- **Deployment Notes**: `/qa/results/ORCHESTRATOR_DEPLOYMENT_NOTES.md`

---

## 🔑 Key Takeaways

1. **JWT Secret**: Always update BOTH `application.yml` AND Elastic Beanstalk env vars
2. **Migrations**: Test locally first, never modify deployed migrations
3. **Configuration**: Two-location rule - update local defaults AND production env vars
4. **Restart**: Configuration changes require backend/environment restart
5. **Verify**: Always verify deployments with health checks and API tests

---

**Status**: Operational procedures documented and ready for use

