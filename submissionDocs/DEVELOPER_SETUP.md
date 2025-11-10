# InvoiceMe Developer Setup Guide

**Version**: 1.0  
**Last Updated**: January 2025

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Backend Setup](#backend-setup)
3. [Frontend Setup](#frontend-setup)
4. [Database Setup](#database-setup)
5. [Testing](#testing)
6. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

| Software | Version | Purpose |
|----------|---------|---------|
| **Java JDK** | 17 (LTS) | Backend runtime (Spring Boot) |
| **Node.js** | 20+ | Frontend runtime (Next.js) |
| **Maven** | 3.8+ | Backend build tool |
| **PostgreSQL** | 15+ | Database (or Docker) |
| **Git** | 2.30+ | Version control |

### Optional Software

- **Docker Desktop**: For local PostgreSQL database
- **IntelliJ IDEA** or **VS Code**: IDE for development
- **Postman** or **curl**: API testing

### System Requirements

- **macOS**: 12.0 (Monterey) or later
- **Windows**: 10 or later (64-bit)
- **Linux**: Ubuntu 20.04+ or equivalent
- **RAM**: 8GB minimum (16GB recommended)
- **Disk Space**: 2GB free space

---

## Backend Setup

### 1. Clone Repository

```bash
git clone https://github.com/your-org/invoiceme.git
cd invoiceme
```

### 2. Verify Java Installation

```bash
java -version
# Should show: openjdk version "17" or higher

mvn -version
# Should show: Apache Maven 3.8.x or higher
```

### 3. Configure Database Connection

Create `backend/src/main/resources/application.yml` (if not exists):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/invoiceme
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

jwt:
  secret: your-64-character-secret-key-here-minimum-64-chars-for-hs512-algorithm
  expiration: 86400000 # 24 hours in milliseconds

aws:
  region: us-east-1
  ses:
    from-email: your-email@example.com
  s3:
    bucket-name: invoiceme-pdfs-dev

server:
  port: 8080
```

**Important**: Generate a secure JWT secret (64 characters minimum for HS512):

```bash
# Using OpenSSL
openssl rand -base64 48

# Or use the provided script
./scripts/generate-bcrypt-hash.sh
```

### 4. Run Database Migrations

```bash
cd backend
mvn flyway:migrate
```

**Note**: Flyway will automatically run all migrations from `backend/src/main/resources/db/migration/`.

### 5. Build Backend

```bash
cd backend
mvn clean package -DskipTests
```

This creates `target/invoiceme-backend-2.0.0.jar`.

### 6. Start Backend

```bash
cd backend
java -jar target/invoiceme-backend-2.0.0.jar
```

Or run with Maven:

```bash
cd backend
mvn spring-boot:run
```

**Verify**: Backend should start on `http://localhost:8080`

### 7. Test Backend Health

```bash
curl http://localhost:8080/api/v1/health
# Should return: {"status":"UP"}
```

---

## Frontend Setup

### 1. Navigate to Frontend Directory

```bash
cd frontend
```

### 2. Install Dependencies

```bash
npm install
```

**Note**: This installs all dependencies from `package.json` (Next.js, React, TypeScript, Tailwind CSS, etc.)

### 3. Configure Environment Variables

Create `frontend/.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

**Note**: For production, set `NEXT_PUBLIC_API_URL` to your backend URL.

### 4. Start Development Server

```bash
npm run dev
```

**Verify**: Frontend should start on `http://localhost:3000`

### 5. Build for Production

```bash
npm run build
npm start
```

**Note**: Production build runs on `http://localhost:3000` (SSR mode).

---

## Database Setup

### Option 1: Local PostgreSQL (Recommended for Development)

#### Install PostgreSQL

**macOS** (using Homebrew):
```bash
brew install postgresql@15
brew services start postgresql@15
```

**Linux** (Ubuntu):
```bash
sudo apt update
sudo apt install postgresql-15
sudo systemctl start postgresql
```

**Windows**: Download from https://www.postgresql.org/download/windows/

#### Create Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE invoiceme;

# Create user (optional)
CREATE USER invoiceme_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE invoiceme TO invoiceme_user;

# Exit
\q
```

#### Update Backend Configuration

Update `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/invoiceme
    username: postgres
    password: postgres
```

### Option 2: Docker Compose (Quick Setup)

Create `docker-compose.yml` in project root:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: invoiceme
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Start PostgreSQL:

```bash
docker-compose up -d
```

**Verify**:
```bash
docker ps
# Should show postgres container running
```

### Option 3: Supabase (Production-like)

1. Create account at https://supabase.com
2. Create new project
3. Get connection string from Project Settings → Database
4. Update `application.yml` with Supabase connection string

**Note**: Use Supabase Connection Pooler for production deployments.

---

## Testing

### Backend Integration Tests

```bash
cd backend
mvn test
```

**Expected**: All tests should pass (12+ integration tests).

### Frontend Tests

```bash
cd frontend
npm test
```

**Note**: Frontend tests use Vitest and React Testing Library.

### Manual Testing Checklist

1. **Backend Health Check**:
   ```bash
   curl http://localhost:8080/api/v1/health
   ```

2. **Authentication**:
   ```bash
   # Register user
   curl -X POST http://localhost:8080/api/v1/auth/register \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"password123","fullName":"Test User","role":"ACCOUNTANT"}'
   
   # Login
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"test@example.com","password":"password123"}'
   ```

3. **Create Customer**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/customers \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -d '{"companyName":"Test Corp","email":"customer@test.com","customerType":"COMMERCIAL"}'
   ```

4. **Frontend**: Open `http://localhost:3000` and verify login page loads.

---

## Troubleshooting

### Backend Issues

**Issue**: `Port 8080 already in use`  
**Solution**: Change port in `application.yml`:
```yaml
server:
  port: 8081
```

**Issue**: `Database connection failed`  
**Solution**: 
- Verify PostgreSQL is running: `psql -U postgres`
- Check connection string in `application.yml`
- Verify database exists: `psql -U postgres -l`

**Issue**: `Flyway migration failed`  
**Solution**:
- Check database connection
- Verify migration files exist in `backend/src/main/resources/db/migration/`
- Check Flyway history table: `SELECT * FROM flyway_schema_history;`

**Issue**: `JWT secret too short`  
**Solution**: Generate 64-character secret:
```bash
openssl rand -base64 48
```

### Frontend Issues

**Issue**: `npm install fails`  
**Solution**:
- Clear cache: `npm cache clean --force`
- Delete `node_modules` and `package-lock.json`
- Run `npm install` again

**Issue**: `API calls fail (CORS error)`  
**Solution**: 
- Verify `NEXT_PUBLIC_API_URL` in `.env.local`
- Check backend CORS configuration
- Use Next.js rewrites (configured in `next.config.ts`)

**Issue**: `Build fails`  
**Solution**:
- Check TypeScript errors: `npm run type-check`
- Verify all dependencies installed: `npm install`
- Clear `.next` folder: `rm -rf .next`

### Database Issues

**Issue**: `Connection refused`  
**Solution**:
- Verify PostgreSQL is running: `pg_isready`
- Check port: `netstat -an | grep 5432`
- Verify credentials in `application.yml`

**Issue**: `Migration errors`  
**Solution**:
- Check Flyway baseline: `mvn flyway:baseline`
- Review migration files for syntax errors
- Check PostgreSQL logs: `tail -f /var/log/postgresql/postgresql-*.log`

---

## Development Workflow

### 1. Start Services

**Terminal 1** (Backend):
```bash
cd backend
mvn spring-boot:run
```

**Terminal 2** (Frontend):
```bash
cd frontend
npm run dev
```

**Terminal 3** (Database - if using Docker):
```bash
docker-compose up -d
```

### 2. Make Changes

- **Backend**: Changes auto-reload (Spring Boot DevTools)
- **Frontend**: Changes auto-reload (Next.js Fast Refresh)

### 3. Run Tests

```bash
# Backend
cd backend && mvn test

# Frontend
cd frontend && npm test
```

### 4. Commit Changes

```bash
git add .
git commit -m "Description of changes"
git push
```

---

## Next Steps

After setup is complete:

1. **Review Architecture**: Read `ARCHITECTURE.md` to understand the system design
2. **Explore API**: Use Postman or curl to test API endpoints (see `API_REFERENCE.md`)
3. **Run Integration Tests**: Execute full test suite to verify everything works
4. **Deploy**: Follow `DEPLOYMENT_GUIDE.md` for production deployment

---

**Document Version**: 1.0  
**Last Updated**: January 2025

