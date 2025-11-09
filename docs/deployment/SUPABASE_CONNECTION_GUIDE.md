# Supabase Connection Guide - InvoiceMe

**Last Updated**: 2025-01-27  
**Purpose**: Guide for connecting to Supabase from different environments

---

## Overview

Supabase provides two connection methods:
1. **Direct Connection** - Internal/VPN use only
2. **Connection Pooler** - External services (AWS, serverless, etc.)

**Critical**: Direct connection hostnames do NOT resolve externally. Use connection pooler for AWS/production deployments.

---

## Connection Methods

### 1. Direct Connection (Local Development)

**Use Case**: Local development, VPN, internal services

**Connection String**:
```
jdbc:postgresql://db.PROJECT_ID.supabase.co:5432/postgres
```

**Username**: `postgres`  
**Password**: Your Supabase database password

**Example**:
```
jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
Username: postgres
Password: invoicemesupa
```

**Limitations**:
- ❌ Does not resolve externally (AWS, serverless, etc.)
- ✅ Works from local machine
- ✅ Works with Supabase CLI/proxy

---

### 2. Connection Pooler (Production/AWS)

**Use Case**: AWS Elastic Beanstalk, serverless functions, external services

**Connection String**:
```
jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
```

**Username**: `postgres.PROJECT_ID`  
**Password**: Your Supabase database password

**Example**:
```
jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
Username: postgres.rhyariaxwllotjiuchhz
Password: invoicemesupa
```

**Pooler Types**:
- **Session Pooler** (port 5432): Full PostgreSQL features, connection pooling
- **Transaction Pooler** (port 6543): Faster, but limited features (not recommended for Spring Boot)

**Advantages**:
- ✅ Resolves to public IPs
- ✅ Accessible from external services
- ✅ Connection pooling built-in
- ✅ Required for production deployments

---

## Finding Your Connection Details

### 1. Get Project Reference ID

1. Go to Supabase Dashboard
2. Select your project
3. Go to **Settings** → **Database**
4. Find **Connection string** section
5. Your project reference ID is in the hostname: `db.YOUR_PROJECT_ID.supabase.co`

### 2. Get Pooler Connection String

1. Go to Supabase Dashboard
2. Select your project
3. Go to **Settings** → **Database**
4. Scroll to **Connection Pooling** section
5. Select **Session mode** (port 5432)
6. Copy the connection string

**Format**:
```
postgresql://postgres.PROJECT_ID:[PASSWORD]@aws-1-us-east-1.pooler.supabase.com:5432/postgres
```

### 3. Convert to JDBC Format

**From Supabase Format**:
```
postgresql://postgres.PROJECT_ID:[PASSWORD]@aws-1-us-east-1.pooler.supabase.com:5432/postgres
```

**To JDBC Format**:
```
jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
Username: postgres.PROJECT_ID
Password: [PASSWORD]
```

---

## Environment-Specific Configuration

### Local Development

**application.yml**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
    username: postgres
    password: ${DB_PASSWORD:invoicemesupa}
```

**Environment Variable**:
```bash
DATABASE_URL=jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=invoicemesupa
```

### AWS Elastic Beanstalk (Production)

**Environment Variables**:
```bash
DATABASE_URL=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
DB_USERNAME=postgres.rhyariaxwllotjiuchhz
DB_PASSWORD=invoicemesupa
```

**Important**: Use connection pooler for AWS deployments!

---

## Common Issues & Solutions

### Issue 1: "Network is unreachable"

**Symptoms**:
```
java.net.SocketException: Network is unreachable
```

**Cause**: Using direct connection hostname from external service (AWS, serverless)

**Solution**: Switch to connection pooler

**Before**:
```
jdbc:postgresql://db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres
Username: postgres
```

**After**:
```
jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
Username: postgres.rhyariaxwllotjiuchhz
```

---

### Issue 2: "Authentication failed"

**Symptoms**:
```
org.postgresql.util.PSQLException: FATAL: password authentication failed
```

**Cause**: Incorrect username format for pooler connection

**Solution**: Use `postgres.PROJECT_ID` format for pooler

**Wrong**:
```
Username: postgres
```

**Correct**:
```
Username: postgres.rhyariaxwllotjiuchhz
```

---

### Issue 3: Connection timeout

**Symptoms**:
```
java.net.SocketTimeoutException: Connect timed out
```

**Cause**: 
- Security group not allowing outbound port 5432
- Network ACL blocking traffic
- Wrong pooler region

**Solution**:
1. Check security group allows outbound port 5432
2. Verify network ACLs allow traffic
3. Use correct pooler region (e.g., `aws-1-us-east-1` for us-east-1)

---

## Regional Pooler Hostnames

Supabase provides region-specific pooler hostnames:

- **us-east-1**: `aws-1-us-east-1.pooler.supabase.com`
- **us-west-1**: `aws-1-us-west-1.pooler.supabase.com`
- **eu-west-1**: `aws-1-eu-west-1.pooler.supabase.com`
- **ap-southeast-1**: `aws-1-ap-southeast-1.pooler.supabase.com`

**Best Practice**: Use the pooler region closest to your AWS deployment region.

---

## Testing Connections

### Test Direct Connection (Local)

```bash
psql "postgresql://postgres:[PASSWORD]@db.rhyariaxwllotjiuchhz.supabase.co:5432/postgres"
```

### Test Pooler Connection (AWS/External)

```bash
psql "postgresql://postgres.rhyariaxwllotjiuchhz:[PASSWORD]@aws-1-us-east-1.pooler.supabase.com:5432/postgres"
```

### Test from Spring Boot

```yaml
spring:
  datasource:
    url: jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
    username: postgres.rhyariaxwllotjiuchhz
    password: ${DB_PASSWORD}
```

---

## Security Best Practices

1. **Never commit credentials**: Use environment variables
2. **Use connection pooler for production**: More secure and reliable
3. **Rotate passwords regularly**: Change database password periodically
4. **Use SSL/TLS**: Supabase pooler uses SSL by default
5. **Limit IP access**: Configure Supabase firewall rules if needed

---

## Quick Reference

### Local Development
```
DATABASE_URL=jdbc:postgresql://db.PROJECT_ID.supabase.co:5432/postgres
DB_USERNAME=postgres
```

### AWS/Production
```
DATABASE_URL=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:5432/postgres
DB_USERNAME=postgres.PROJECT_ID
```

---

**Status**: ✅ **CONNECTION GUIDE COMPLETE** - Use pooler for production deployments.

