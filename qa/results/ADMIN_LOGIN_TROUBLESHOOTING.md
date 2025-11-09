# Admin Login Troubleshooting

## Current Status
- ❌ Login failing with "Invalid email or password"
- Admin user exists in database: `admin@invoiceme.com`
- Role: `SYSADMIN`, Status: `ACTIVE`
- Password hash: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`

## Possible Issues

### 1. Password Hash Mismatch
The stored hash might not match "Admin123!". Try:
- `password123!` (common test password)
- Check online BCrypt checker: https://bcrypt-generator.com/

### 2. Enum Read Issue
The `@ColumnTransformer` and `@Convert` combination might be causing Hibernate to fail when reading the user, causing `findByEmail` to return empty.

**Solution**: Restart backend to ensure enum converters are loaded properly.

### 3. Backend Not Picking Up Changes
If backend was started before enum converters were added, it won't have them loaded.

**Solution**: Restart backend.

## Next Steps
1. ✅ Restart backend (done)
2. Test login again
3. If still failing, check backend logs for enum conversion errors
4. Consider simplifying enum handling (remove @ColumnTransformer, use converter only)

## Admin Credentials (Once Fixed)
- Email: `admin@invoiceme.com`
- Password: `Admin123!` (or whatever password matches the hash)

