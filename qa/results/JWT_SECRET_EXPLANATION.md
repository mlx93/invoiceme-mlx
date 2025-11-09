# JWT Secret Key Explanation

## Where Did the 64-Character JWT Secret Come From?

The new JWT secret (`3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8`) was generated to meet the security requirements for the HS512 algorithm used by the JJWT library.

### Why It Was Changed

1. **Security Requirement**: The HS512 algorithm requires a signing key of at least 512 bits (64 characters for base64-encoded strings).
2. **Previous Issue**: The original JWT secret was too short, causing a `WeakKeyException` during token generation.
3. **Solution**: Generated a new 64-character base64-encoded string to meet the 512-bit requirement.

### How It Was Generated

The secret was generated as a random base64-encoded string. It's a cryptographically secure random value that serves as the signing key for JWT tokens.

## Environment Variable Updates Required

### ✅ Already Updated (Local Development)
- **`backend/src/main/resources/application.yml`**: Line 82 contains the new JWT secret as the default value
- **`docs/deployment/ELASTIC_BEANSTALK_ENV_VALUES.txt`**: Line 15 contains the new JWT secret

### ⚠️ REQUIRED: AWS Elastic Beanstalk Environment Variables

**You MUST update the `JWT_SECRET` environment variable in your AWS Elastic Beanstalk environment:**

1. Go to AWS Elastic Beanstalk Console
2. Select your environment
3. Go to **Configuration** → **Software** → **Environment properties**
4. Find or add: `JWT_SECRET`
5. Set value to: `3pQd3F32jrtNjrsreiPU3nG8bZ6y1P-rAfRPhyhhpbXS_8f995CDwBhqAcqUl-U8`
6. Click **Apply** and wait for the environment to update

### ❌ NOT Required: PostgreSQL

**No changes needed in PostgreSQL.** The JWT secret is:
- A Spring Boot application property (not a database value)
- Used only by the backend application for signing/verifying JWT tokens
- Stored in environment variables or configuration files, not in the database

## Important Notes

- **Security**: This secret should be kept confidential and never committed to version control in production
- **Consistency**: The same secret must be used across all instances of the backend application
- **Rotation**: If you need to rotate the secret, you'll need to:
  1. Generate a new secret
  2. Update all environment variables
  3. Restart all backend instances
  4. Note: Existing tokens will become invalid (users will need to log in again)

