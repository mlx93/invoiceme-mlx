# InvoiceMe Setup Instructions

**Last Updated**: 2025-01-27  
**Purpose**: Comprehensive step-by-step guide to set up the InvoiceMe project infrastructure and local development environment.

---

## Table of Contents

1. [Prerequisites](#1-prerequisites)
2. [AWS Account Setup](#2-aws-account-setup)
3. [Supabase Database Setup](#3-supabase-database-setup)
4. [GitHub Repository Setup](#4-github-repository-setup)
5. [Local Development Environment](#5-local-development-environment)
6. [Verification & Testing](#6-verification--testing)
7. [Troubleshooting](#7-troubleshooting)
8. [Quick Reference](#8-quick-reference)

---

## 1. Prerequisites

### 1.1 System Requirements

**Operating Systems Supported:**
- **macOS**: 12.0 (Monterey) or later
- **Windows**: 10 or later (64-bit)
- **Linux**: Ubuntu 20.04+ or equivalent

**Required Permissions:**
- Administrator/sudo access for software installation
- Internet connection for downloading dependencies and accessing cloud services

### 1.2 Software Versions

| Software | Minimum Version | Purpose |
|----------|----------------|---------|
| Java | 17 (LTS) | Backend runtime (Spring Boot) |
| Node.js | 18.x or higher | Frontend runtime (Next.js) |
| Docker Desktop | Latest | Local PostgreSQL database |
| Git | 2.30+ | Version control |
| AWS CLI | 2.x (optional) | AWS service management |

### 1.3 Account Prerequisites

Before starting, ensure you have or can create:
- **AWS Account** (free tier available, credit card required)
- **Supabase Account** (free tier available)
- **GitHub Account** (free tier available)
- **Email Address** (for service verification)

**Note**: AWS free tier provides 12 months of free usage for eligible services. You won't be charged unless you exceed free tier limits.

---

## 2. AWS Account Setup

### 2.1 Create AWS Account

**Step 1**: Navigate to https://aws.amazon.com/

**Step 2**: Click "Create an AWS Account" (top right)

**Step 3**: Enter account information:
- Email address
- Password (strong password recommended)
- AWS account name (e.g., "InvoiceMe Development")

**Step 4**: Provide payment information:
- Credit card required (won't be charged unless you exceed free tier)
- Billing address

**Step 5**: Verify your identity:
- Phone number verification (SMS or call)
- Email verification (check inbox for verification link)

**Step 6**: Select support plan:
- Choose "Basic Plan" (free) for development

**Verification**: 
- Log into AWS Console at https://console.aws.amazon.com/
- Verify you see the "Services" menu in the top navigation
- Check that your account name appears in the top right

**Expected Outcome**: You should see the AWS Management Console dashboard with access to all services.

---

### 2.2 Create IAM User for Deployment

**Why**: IAM (Identity and Access Management) users provide secure programmatic access to AWS services. We'll create a dedicated user for CI/CD deployments.

**Step 1**: Navigate to IAM Console
- AWS Console → Search "IAM" in the top search bar → Click "IAM"

**Step 2**: Create New User
- Left sidebar → Click "Users"
- Click "Create User" button (top right)

**Step 3**: Set User Details
- **User name**: `invoiceme-deploy`
- **AWS credential type**: Select "Access key - Programmatic access" (NOT console access)
- Click "Next"

**Step 4**: Set Permissions
- Select "Attach existing policies directly"
- Search and select the following policies (check boxes):
  - `AWSElasticBeanstalkFullAccess` (for backend deployment)
  - `AWSAmplifyFullAccess` (for frontend deployment)
  - `AmazonSESFullAccess` (for email notifications)
  - `AmazonS3FullAccess` (for PDF storage)
  - `CloudWatchFullAccess` (for logging and monitoring)
- Click "Next"

**Step 5**: Review and Create
- Review user name and permissions
- Click "Create User"

**Step 6**: **CRITICAL** - Save Credentials
- **Access Key ID**: Copy this value immediately (shown only once!)
- **Secret Access Key**: Click "Show" and copy this value immediately (shown only once!)
- **Save these credentials securely**:
  - Use a password manager (1Password, LastPass, etc.)
  - Or save to a secure file (never commit to Git!)
  - You'll need these for GitHub Actions CI/CD

**Step 7**: Download CSV (Optional)
- Click "Download .csv" to save credentials as a backup
- Store the CSV file securely

**Verification**: Test credentials with AWS CLI:
```bash
# Install AWS CLI first (if not installed)
# macOS:
brew install awscli

# Windows:
# Download from https://aws.amazon.com/cli/

# Configure AWS CLI
aws configure
# Enter Access Key ID: <paste from Step 6>
# Enter Secret Access Key: <paste from Step 6>
# Enter region: us-east-1
# Enter output format: json

# Test credentials
aws sts get-caller-identity
# Should return JSON with your IAM user ARN
```

**Expected Outcome**: `aws sts get-caller-identity` returns JSON containing your IAM user ARN (e.g., `arn:aws:iam::123456789012:user/invoiceme-deploy`).

---

### 2.3 Verify SES Email Address

**Why**: AWS SES (Simple Email Service) starts in "Sandbox" mode, which only allows sending emails to verified addresses. We'll verify your email for testing.

**Step 1**: Navigate to SES Console
- AWS Console → Search "SES" → Click "Amazon SES"

**Step 2**: Verify Email Address
- Left sidebar → Click "Verified identities"
- Click "Create identity" button

**Step 3**: Choose Identity Type
- Select "Email address" (for testing)
- Enter your email address (e.g., `yourname@example.com`)
- Click "Create identity"

**Step 4**: Verify Email
- Check your email inbox for verification email from AWS
- Click the verification link in the email
- You should see "Email address verified" confirmation

**Step 5**: Request Production Access (Optional, for Production)
- While in Sandbox mode, you can only send to verified emails
- To send to any email address:
  - Click "Account dashboard" → "Request production access"
  - Fill out use case form (takes 24-48 hours for approval)
  - For development, Sandbox mode is sufficient

**Verification**: 
- In SES Console → Verified identities, you should see your email with status "Verified"

**Expected Outcome**: Your email address appears in the verified identities list with status "Verified".

**Note**: For production, you can verify a domain instead of individual emails. This allows sending from any address on that domain (e.g., `noreply@yourdomain.com`).

---

### 2.4 Create S3 Bucket for PDF Storage

**Why**: S3 (Simple Storage Service) stores generated invoice PDFs. This is optional for MVP (can generate on-demand), but recommended for production.

**Step 1**: Navigate to S3 Console
- AWS Console → Search "S3" → Click "S3"

**Step 2**: Create Bucket
- Click "Create bucket" button

**Step 3**: Configure Bucket
- **Bucket name**: `invoiceme-pdfs-{your-unique-id}` (must be globally unique)
  - Example: `invoiceme-pdfs-john-doe-2025`
  - Use lowercase letters, numbers, and hyphens only
- **AWS Region**: `us-east-1` (or your preferred region)
- **Object Ownership**: ACLs disabled (recommended)
- **Block Public Access**: 
  - **Uncheck** "Block all public access" (we'll use signed URLs for security)
  - Or keep checked and use bucket policy for signed URLs (more secure)

**Step 4**: Configure Versioning (Optional)
- Enable versioning if you want to keep PDF history
- Click "Next" through remaining options (defaults are fine)

**Step 5**: Create Bucket
- Review settings
- Click "Create bucket"

**Step 6**: Configure Bucket Policy (If Using Signed URLs)
- Click on your bucket name
- Go to "Permissions" tab
- Click "Bucket policy"
- Add policy for signed URL access (see AWS S3 documentation)

**Verification**: 
- In S3 Console, you should see your bucket listed
- Click bucket name → Verify it's empty (we'll upload PDFs later)

**Expected Outcome**: Bucket created and visible in S3 Console.

**Environment Variable to Save**:
```
AWS_S3_BUCKET_NAME=invoiceme-pdfs-{your-unique-id}
```

---

### 2.5 AWS Setup Summary

**Environment Variables Collected**:
```bash
AWS_ACCESS_KEY_ID=<from IAM user creation>
AWS_SECRET_ACCESS_KEY=<from IAM user creation>
AWS_REGION=us-east-1
AWS_SES_FROM_EMAIL=<your-verified-email@example.com>
AWS_S3_BUCKET_NAME=invoiceme-pdfs-{your-unique-id}
```

**Save these securely** - you'll need them for:
- GitHub Actions secrets (Section 4)
- Local development `.env` file (Section 5)

---

## 3. Supabase Database Setup

### 3.1 Create Supabase Account

**Step 1**: Navigate to Supabase
- Go to https://supabase.com/
- Click "Start your project" or "Sign up"

**Step 2**: Sign Up
- Choose authentication method:
  - GitHub (recommended - links to your GitHub account)
  - Email (requires email verification)
- Complete sign-up process

**Step 3**: Verify Email (If Using Email Sign-Up)
- Check inbox for verification email
- Click verification link

**Verification**: You should be logged into Supabase dashboard.

---

### 3.2 Create Supabase Project

**Step 1**: Create New Project
- Dashboard → Click "New Project" button

**Step 2**: Configure Project
- **Project Name**: `invoiceme`
- **Database Password**: 
  - Click "Generate a password" (recommended)
  - **CRITICAL**: Copy and save this password securely (shown only once!)
  - Or create your own strong password (min 12 characters)
- **Region**: 
  - Choose closest to you for best performance
  - Or `us-east-1` for AWS compatibility
- **Pricing Plan**: Select "Free" (sufficient for development)

**Step 3**: Create Project
- Click "Create new project"
- Wait for provisioning (~2 minutes)
- You'll see a progress indicator

**Verification**: 
- Project appears in dashboard with status "Active"
- You can click on project to open project dashboard

---

### 3.3 Get Database Connection String

**Step 1**: Navigate to Project Settings
- Click on your project name
- Left sidebar → Click "Settings" (gear icon)
- Click "Database" (under Project Settings)

**Step 2**: Find Connection String
- Scroll to "Connection string" section
- You'll see "URI" format connection string:
  ```
  postgresql://postgres:[YOUR-PASSWORD]@[HOST]:5432/postgres
  ```
- Click "Copy" button to copy full connection string

**Step 3**: Save Connection String
- Replace `[YOUR-PASSWORD]` with your actual database password
- Save as `DATABASE_URL` environment variable

**Example Connection String**:
```
postgresql://postgres:MySecurePassword123@db.abcdefghijklmnop.supabase.co:5432/postgres
```

**Verification**: Test connection (optional):
```bash
# Install PostgreSQL client (if not installed)
# macOS:
brew install postgresql

# Windows:
# Download from https://www.postgresql.org/download/windows/

# Test connection
psql "postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres"
# Should connect successfully (type \q to exit)
```

---

### 3.4 Enable Connection Pooling (Optional, Recommended for Production)

**Why**: Connection pooling improves performance and reduces database load. Supabase provides pgBouncer for this.

**Step 1**: Navigate to Connection Pooling Settings
- Project Settings → Database → Connection pooling

**Step 2**: Choose Pooling Mode
- **Session mode**: For development (allows transactions, prepared statements)
- **Transaction mode**: For production (better performance, limited features)

**Step 3**: Get Pooled Connection String
- Copy the pooled connection string (different port, usually 6543)
- Save as `DATABASE_POOL_URL` environment variable

**Example Pooled Connection String**:
```
postgresql://postgres:MySecurePassword123@db.abcdefghijklmnop.supabase.co:6543/postgres?pgbouncer=true
```

**Note**: For local development, you can use the regular connection string. Use pooled connection for production deployments.

---

### 3.5 Supabase Setup Summary

**Environment Variables Collected**:
```bash
DATABASE_URL=postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres
DATABASE_POOL_URL=postgresql://postgres:[PASSWORD]@[HOST]:6543/postgres?pgbouncer=true
```

**Save these securely** - you'll need them for:
- GitHub Actions secrets (Section 4)
- Local development `.env` file (Section 5)

---

## 4. GitHub Repository Setup

### 4.1 Create GitHub Account (If Needed)

**Step 1**: Navigate to GitHub
- Go to https://github.com/
- Click "Sign up" (if you don't have an account)

**Step 2**: Create Account
- Enter username, email, password
- Verify email address

**Verification**: You should be logged into GitHub dashboard.

---

### 4.2 Create GitHub Repository

**Step 1**: Create New Repository
- GitHub Dashboard → Click "+" icon (top right) → "New repository"

**Step 2**: Configure Repository
- **Repository name**: `InvoiceMe`
- **Description**: "Production-quality ERP-style invoicing system with DDD, CQRS, and VSA"
- **Visibility**: 
  - Private (recommended for development)
  - Public (if you want to share)
- **Initialize repository**: 
  - ❌ **Do NOT** check "Add a README file" (we'll create one)
  - ❌ **Do NOT** add .gitignore or license (we'll add these)

**Step 3**: Create Repository
- Click "Create repository"

**Verification**: You should see an empty repository with setup instructions.

---

### 4.3 Set Up GitHub Actions Secrets

**Why**: GitHub Actions needs access to AWS, Supabase, and other services for CI/CD. Secrets are encrypted and only accessible during workflow runs.

**Step 1**: Navigate to Repository Settings
- Repository page → Click "Settings" tab (top navigation)

**Step 2**: Open Secrets Configuration
- Left sidebar → "Secrets and variables" → "Actions"
- Click "New repository secret" button

**Step 3**: Add Secrets (One at a Time)

Add each secret with the following names and values:

**Secret 1: AWS_ACCESS_KEY_ID**
- Name: `AWS_ACCESS_KEY_ID`
- Value: `<from Section 2.2, Step 6>`
- Click "Add secret"

**Secret 2: AWS_SECRET_ACCESS_KEY**
- Name: `AWS_SECRET_ACCESS_KEY`
- Value: `<from Section 2.2, Step 6>`
- Click "Add secret"

**Secret 3: DATABASE_URL**
- Name: `DATABASE_URL`
- Value: `<from Section 3.3, Step 2>`
- Click "Add secret"

**Secret 4: JWT_SECRET**
- Name: `JWT_SECRET`
- Value: Generate with command:
  ```bash
  # macOS/Linux:
  openssl rand -base64 32
  
  # Windows (PowerShell):
  [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
  ```
- Copy the generated value and paste as secret value
- Click "Add secret"

**Secret 5: AWS_SES_FROM_EMAIL**
- Name: `AWS_SES_FROM_EMAIL`
- Value: `<your-verified-email@example.com>` (from Section 2.3)
- Click "Add secret"

**Secret 6: AWS_S3_BUCKET_NAME**
- Name: `AWS_S3_BUCKET_NAME`
- Value: `<invoiceme-pdfs-{your-unique-id}>` (from Section 2.4)
- Click "Add secret"

**Secret 7: AWS_REGION** (Optional, but recommended)
- Name: `AWS_REGION`
- Value: `us-east-1`
- Click "Add secret"

**Verification**: 
- In "Secrets and variables" → "Actions", you should see all 7 secrets listed
- Secrets are masked (showing only `***`)

**Expected Outcome**: All secrets configured and ready for CI/CD workflows.

---

### 4.4 GitHub Setup Summary

**Secrets Configured**:
- ✅ `AWS_ACCESS_KEY_ID`
- ✅ `AWS_SECRET_ACCESS_KEY`
- ✅ `DATABASE_URL`
- ✅ `JWT_SECRET`
- ✅ `AWS_SES_FROM_EMAIL`
- ✅ `AWS_S3_BUCKET_NAME`
- ✅ `AWS_REGION`

**Next Steps**: After cloning repository and pushing code, GitHub Actions will automatically use these secrets for deployments.

---

## 5. Local Development Environment

### 5.1 Install Java 17

**macOS**:
```bash
# Using Homebrew (recommended)
brew install openjdk@17

# Verify installation
java -version
# Should show: openjdk version "17.x.x"

# Set JAVA_HOME (add to ~/.zshrc or ~/.bash_profile)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
```

**Windows**:
1. Download Java 17 from https://adoptium.net/
2. Choose "Windows x64" → "JDK 17" → Download installer
3. Run installer → Follow installation wizard
4. Verify installation:
   ```cmd
   java -version
   ```
   Should show: `openjdk version "17.x.x"`

**Linux (Ubuntu/Debian)**:
```bash
# Update package list
sudo apt update

# Install OpenJDK 17
sudo apt install openjdk-17-jdk

# Verify installation
java -version
# Should show: openjdk version "17.x.x"

# Set JAVA_HOME (add to ~/.bashrc)
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

**Verification**: 
```bash
java -version
# Expected: openjdk version "17.x.x" or similar
```

---

### 5.2 Install Node.js 18+

**macOS**:
```bash
# Using Homebrew (recommended)
brew install node@18

# Verify installation
node -v
# Should show: v18.x.x or higher

npm -v
# Should show: 9.x.x or higher
```

**Windows**:
1. Download Node.js from https://nodejs.org/
2. Choose "LTS" version (18.x or higher)
3. Run installer → Follow installation wizard (check "Add to PATH")
4. Verify installation:
   ```cmd
   node -v
   npm -v
   ```

**Linux (Ubuntu/Debian)**:
```bash
# Using NodeSource repository (recommended)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Verify installation
node -v
npm -v
```

**Verification**: 
```bash
node -v
# Expected: v18.x.x or higher

npm -v
# Expected: 9.x.x or higher
```

---

### 5.3 Install Docker Desktop

**macOS**:
1. Download Docker Desktop from https://www.docker.com/products/docker-desktop
2. Choose "Mac with Intel chip" or "Mac with Apple chip" (M1/M2)
3. Open downloaded `.dmg` file
4. Drag Docker icon to Applications folder
5. Launch Docker Desktop from Applications
6. Complete initial setup (may require system password)
7. Wait for Docker to start (whale icon in menu bar)

**Windows**:
1. Download Docker Desktop from https://www.docker.com/products/docker-desktop
2. Run installer → Follow installation wizard
3. Restart computer when prompted
4. Launch Docker Desktop from Start menu
5. Complete initial setup
6. Wait for Docker to start (whale icon in system tray)

**Linux (Ubuntu/Debian)**:
```bash
# Install Docker Engine
sudo apt update
sudo apt install docker.io docker-compose

# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group (to run without sudo)
sudo usermod -aG docker $USER
# Log out and log back in for changes to take effect

# Verify installation
docker --version
docker-compose --version
```

**Verification**: 
```bash
docker --version
# Expected: Docker version 24.x.x or higher

docker ps
# Should show empty list (no containers running yet)
```

**Troubleshooting**: 
- **macOS/Windows**: If Docker Desktop won't start, check System Preferences → Security & Privacy → Allow Docker
- **Linux**: If `docker ps` fails, try `sudo docker ps` (user may not be in docker group yet)

---

### 5.4 Clone Repository

**Step 1**: Get Repository URL
- GitHub repository page → Click green "Code" button
- Copy HTTPS URL (e.g., `https://github.com/yourusername/InvoiceMe.git`)

**Step 2**: Clone Repository
```bash
# Navigate to your desired directory
cd ~/Desktop  # or wherever you want the project

# Clone repository
git clone https://github.com/yourusername/InvoiceMe.git

# Navigate into project
cd InvoiceMe
```

**Verification**: 
```bash
ls -la
# Should show project files and directories
```

---

### 5.5 Set Up Local Environment Variables

**Step 1**: Create `.env` File
- In project root directory (`InvoiceMe/`), create `.env` file

**Step 2**: Add Environment Variables

**macOS/Linux**:
```bash
# Create .env file
touch .env

# Open in editor
nano .env
# or
code .env  # if using VS Code
```

**Windows**:
```cmd
# Create .env file
type nul > .env

# Open in Notepad
notepad .env
```

**Step 3**: Add Variables to `.env` File

Copy and paste the following template, replacing placeholder values:

```bash
# Database (Local PostgreSQL via Docker)
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/invoiceme

# JWT Secret (generate new one for local dev)
JWT_SECRET=<generate with: openssl rand -base64 32>

# AWS Configuration (use test values for local dev)
AWS_REGION=us-east-1
AWS_ACCESS_KEY_ID=<your-aws-access-key-id>
AWS_SECRET_ACCESS_KEY=<your-aws-secret-access-key>
AWS_SES_FROM_EMAIL=<your-verified-email@example.com>
AWS_S3_BUCKET_NAME=invoiceme-pdfs-local

# Frontend API URL
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1

# Spring Boot Configuration
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
```

**Step 4**: Generate JWT Secret
```bash
# macOS/Linux:
openssl rand -base64 32

# Windows (PowerShell):
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

Copy the generated value and replace `<generate with: openssl rand -base64 32>` in `.env` file.

**Step 5**: Save `.env` File
- Save and close the file

**Important**: 
- ✅ Add `.env` to `.gitignore` (never commit secrets!)
- ✅ Keep `.env` file secure (don't share publicly)

**Verification**: 
```bash
# macOS/Linux:
cat .env
# Should show your environment variables (without exposing secrets)

# Windows:
type .env
```

---

### 5.6 Set Up Docker Compose for Local PostgreSQL

**Step 1**: Create `docker-compose.yml` (if not exists)

In project root, create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: invoiceme-postgres
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: invoiceme
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
```

**Step 2**: Start PostgreSQL Container
```bash
# Start PostgreSQL
docker-compose up -d postgres

# Verify container is running
docker ps
# Should show invoiceme-postgres container with status "Up"
```

**Step 3**: Verify Database Connection
```bash
# Test connection (macOS/Linux)
psql postgresql://postgres:postgres@localhost:5432/invoiceme

# If psql not installed, use Docker:
docker exec -it invoiceme-postgres psql -U postgres -d invoiceme

# Should connect successfully
# Type \q to exit
```

**Verification**: 
```bash
docker ps
# Expected: invoiceme-postgres container running

docker logs invoiceme-postgres
# Should show: "database system is ready to accept connections"
```

---

### 5.7 Local Development Setup Summary

**Installed Software**:
- ✅ Java 17
- ✅ Node.js 18+
- ✅ Docker Desktop
- ✅ Git

**Configured**:
- ✅ Repository cloned
- ✅ `.env` file created with all variables
- ✅ PostgreSQL running in Docker

**Next Steps**: 
- Backend setup (Section 6.1)
- Frontend setup (Section 6.2)

---

## 6. Verification & Testing

### 6.1 Verify AWS CLI Configuration

**Test AWS Credentials**:
```bash
# Check current AWS identity
aws sts get-caller-identity

# Expected output:
# {
#     "UserId": "AIDA...",
#     "Account": "123456789012",
#     "Arn": "arn:aws:iam::123456789012:user/invoiceme-deploy"
# }
```

**Test SES Email Verification**:
```bash
# List verified email addresses
aws ses list-verified-email-addresses --region us-east-1

# Expected output:
# {
#     "VerifiedEmailAddresses": [
#         "your-email@example.com"
#     ]
# }
```

**Test S3 Bucket Access**:
```bash
# List S3 buckets
aws s3 ls

# Expected output:
# invoiceme-pdfs-{your-unique-id}
```

**If AWS CLI Not Installed**: 
- Install AWS CLI (see Section 2.2, Verification step)
- Or skip CLI tests and verify via AWS Console

---

### 6.2 Verify Supabase Connection

**Test Database Connection**:
```bash
# Using psql (if installed)
psql "postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres"

# Should connect successfully
# Type \q to exit

# Or using Docker (if psql not installed)
docker run -it --rm postgres:15-alpine psql "postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres"
```

**Test via Supabase Dashboard**:
- Supabase Dashboard → Your Project → "SQL Editor"
- Run test query:
  ```sql
  SELECT version();
  ```
- Should return PostgreSQL version information

**Expected Outcome**: Connection successful, can query database.

---

### 6.3 Verify Docker Setup

**Test Docker Installation**:
```bash
# Check Docker version
docker --version
# Expected: Docker version 24.x.x

# Check Docker Compose version
docker-compose --version
# Expected: docker-compose version 2.x.x

# List running containers
docker ps
# Should show invoiceme-postgres (if started)
```

**Test PostgreSQL Container**:
```bash
# Start PostgreSQL (if not running)
docker-compose up -d postgres

# Check container logs
docker logs invoiceme-postgres
# Should show: "database system is ready to accept connections"

# Test connection
docker exec -it invoiceme-postgres psql -U postgres -d invoiceme -c "SELECT version();"
# Should return PostgreSQL version
```

**Expected Outcome**: Docker running, PostgreSQL container healthy, database accessible.

---

### 6.4 Verify Java Installation

**Test Java Version**:
```bash
java -version
# Expected: openjdk version "17.x.x"

# Check JAVA_HOME (macOS/Linux)
echo $JAVA_HOME
# Expected: /path/to/java-17

# Windows (PowerShell):
$env:JAVA_HOME
```

**Test Maven Wrapper** (if backend code exists):
```bash
cd backend
./mvnw --version
# macOS/Linux: Should show Maven version

# Windows:
.\mvnw.cmd --version
```

**Expected Outcome**: Java 17 installed and accessible, Maven wrapper working (if backend exists).

---

### 6.5 Verify Node.js Installation

**Test Node.js Version**:
```bash
node -v
# Expected: v18.x.x or higher

npm -v
# Expected: 9.x.x or higher
```

**Test npm Installation** (if frontend code exists):
```bash
cd frontend
npm install
# Should install dependencies without errors
```

**Expected Outcome**: Node.js 18+ installed, npm working, dependencies install successfully.

---

### 6.6 Verify GitHub Repository Access

**Test Git Configuration**:
```bash
# Check Git version
git --version
# Expected: git version 2.30.x or higher

# Check remote repository
git remote -v
# Should show your GitHub repository URL
```

**Test GitHub Actions Secrets** (if repository exists):
- GitHub → Your Repository → Settings → Secrets and variables → Actions
- Verify all secrets are listed (see Section 4.3)

**Expected Outcome**: Git configured, repository cloned, secrets configured.

---

## 7. Troubleshooting

### 7.1 AWS Issues

#### Issue: AWS CLI "Access Denied"

**Symptoms**: 
```bash
aws sts get-caller-identity
# Returns: "AccessDenied" or "An error occurred (AccessDenied)"
```

**Solutions**:
1. **Verify IAM User Permissions**:
   - AWS Console → IAM → Users → `invoiceme-deploy`
   - Check "Permissions" tab → Verify policies are attached
   - Ensure policies include: `AWSElasticBeanstalkFullAccess`, `AWSAmplifyFullAccess`, etc.

2. **Check AWS Credentials**:
   ```bash
   aws configure list
   # Verify Access Key ID and Secret Access Key are set
   
   # Check region
   aws configure get region
   # Should return: us-east-1 (or your configured region)
   ```

3. **Reconfigure AWS CLI**:
   ```bash
   aws configure
   # Re-enter Access Key ID, Secret Access Key, region, output format
   ```

4. **Verify Credentials Not Expired**:
   - IAM → Users → `invoiceme-deploy` → Security credentials
   - Check "Access keys" → Status should be "Active"

---

#### Issue: SES Email Not Sending

**Symptoms**: 
- Email service returns "Email address not verified"
- Emails not received

**Solutions**:
1. **Verify Email Address**:
   - SES Console → Verified identities
   - Ensure your email shows status "Verified"
   - If not verified, check inbox for verification email

2. **Check SES Sandbox Mode**:
   - SES starts in "Sandbox" mode
   - Can only send to verified email addresses
   - **Solution**: Verify recipient email addresses, or request production access

3. **Request Production Access**:
   - SES Console → Account dashboard → "Request production access"
   - Fill out use case form (24-48 hour approval)
   - For testing, verify all recipient emails

4. **Check Sending Limits**:
   - Sandbox: 200 emails/day, 1 email/second
   - Production: Higher limits (varies by account)

---

#### Issue: S3 Bucket Access Denied

**Symptoms**: 
- Cannot upload files to S3 bucket
- "AccessDenied" error

**Solutions**:
1. **Verify IAM User Permissions**:
   - IAM → Users → `invoiceme-deploy` → Permissions
   - Ensure `AmazonS3FullAccess` policy is attached

2. **Check Bucket Policy**:
   - S3 → Your bucket → Permissions → Bucket policy
   - Verify policy allows your IAM user access

3. **Verify Bucket Name**:
   - Ensure bucket name matches `AWS_S3_BUCKET_NAME` environment variable
   - Bucket names are globally unique

---

### 7.2 Supabase Issues

#### Issue: Cannot Connect to Supabase Database

**Symptoms**: 
- Connection timeout
- "Connection refused" error

**Solutions**:
1. **Verify Connection String**:
   - Check `DATABASE_URL` in `.env` file
   - Ensure password is correct (no extra spaces)
   - Verify host and port are correct

2. **Check Supabase Project Status**:
   - Supabase Dashboard → Your Project
   - Ensure project status is "Active" (not paused)
   - Free tier projects pause after inactivity

3. **Test Connection via Supabase Dashboard**:
   - Project → SQL Editor → Run test query
   - If dashboard works but CLI doesn't, check connection string format

4. **Check Firewall/Network**:
   - Ensure your network allows outbound connections to port 5432
   - Some corporate networks block database ports

5. **Verify IP Allowlist** (if enabled):
   - Supabase → Project Settings → Database → Connection pooling
   - Check if IP allowlist is enabled
   - Add your IP address if needed

---

#### Issue: Database Password Not Working

**Symptoms**: 
- "Password authentication failed"

**Solutions**:
1. **Reset Database Password**:
   - Supabase Dashboard → Project Settings → Database
   - Click "Reset database password"
   - Save new password securely
   - Update `DATABASE_URL` in `.env` file

2. **Check Password Encoding**:
   - Ensure password doesn't contain special characters that need URL encoding
   - If password has `@`, `#`, `%`, etc., URL-encode them in connection string

3. **Verify Connection String Format**:
   ```
   postgresql://postgres:[PASSWORD]@[HOST]:5432/postgres
   ```
   - Replace `[PASSWORD]` with actual password (no brackets)

---

### 7.3 Docker Issues

#### Issue: Docker Desktop Won't Start (macOS/Windows)

**Symptoms**: 
- Docker Desktop fails to launch
- Error message about virtualization

**Solutions**:

**macOS**:
1. **Check System Requirements**:
   - macOS 12.0+ (Monterey or later)
   - For Apple Silicon (M1/M2): Use "Mac with Apple chip" installer
   - For Intel Macs: Use "Mac with Intel chip" installer

2. **Grant Permissions**:
   - System Preferences → Security & Privacy → Privacy
   - Allow Docker Desktop in "Full Disk Access"

3. **Restart Docker**:
   ```bash
   # Quit Docker Desktop completely
   # Then restart from Applications
   ```

**Windows**:
1. **Enable Virtualization**:
   - BIOS/UEFI → Enable "Virtualization Technology" or "VT-x"
   - Enable "Hyper-V" in Windows Features

2. **Check WSL 2**:
   - Docker Desktop requires WSL 2 on Windows
   - Install WSL 2: `wsl --install` in PowerShell (as admin)

3. **Restart Computer**:
   - After enabling virtualization, restart Windows

---

#### Issue: Docker Container Won't Start

**Symptoms**: 
- `docker-compose up -d` fails
- Container exits immediately

**Solutions**:
1. **Check Container Logs**:
   ```bash
   docker logs invoiceme-postgres
   # Look for error messages
   ```

2. **Check Port Conflicts**:
   ```bash
   # Check if port 5432 is already in use
   # macOS/Linux:
   lsof -i :5432
   
   # Windows:
   netstat -ano | findstr :5432
   ```
   - If port is in use, stop the conflicting service or change port in `docker-compose.yml`

3. **Remove Old Container**:
   ```bash
   docker-compose down
   docker-compose up -d
   ```

4. **Check Docker Resources**:
   - Docker Desktop → Settings → Resources
   - Ensure enough memory allocated (min 2GB)

---

#### Issue: Cannot Connect to PostgreSQL Container

**Symptoms**: 
- Connection refused from host machine
- "Connection timeout"

**Solutions**:
1. **Verify Container is Running**:
   ```bash
   docker ps
   # Should show invoiceme-postgres with status "Up"
   ```

2. **Check Port Mapping**:
   ```bash
   docker port invoiceme-postgres
   # Should show: 5432/tcp -> 0.0.0.0:5432
   ```

3. **Test Connection from Container**:
   ```bash
   docker exec -it invoiceme-postgres psql -U postgres -d invoiceme
   # Should connect successfully
   ```

4. **Verify Connection String**:
   - Ensure `DATABASE_URL` uses `localhost:5432` (not container name)
   - For local development: `postgresql://postgres:postgres@localhost:5432/invoiceme`

---

### 7.4 Environment Variable Issues

#### Issue: Environment Variables Not Loading

**Symptoms**: 
- Application can't find environment variables
- "Environment variable not set" errors

**Solutions**:
1. **Verify `.env` File Location**:
   - Ensure `.env` file is in project root directory
   - Not in `backend/` or `frontend/` subdirectories

2. **Check `.env` File Format**:
   ```bash
   # Correct format:
   DATABASE_URL=postgresql://...
   
   # Wrong format (no spaces around =):
   DATABASE_URL = postgresql://...  # ❌
   ```

3. **Verify No Trailing Spaces**:
   - Remove trailing spaces after values
   - Use quotes if value contains spaces:
     ```bash
     JWT_SECRET="your secret with spaces"
     ```

4. **Check File Encoding**:
   - Ensure `.env` file is UTF-8 encoded
   - Avoid special characters that need escaping

5. **Restart Application**:
   - Environment variables are loaded at startup
   - Restart backend/frontend after changing `.env`

---

#### Issue: JWT Secret Generation Fails

**Symptoms**: 
- `openssl rand -base64 32` command not found

**Solutions**:

**macOS**:
- OpenSSL should be pre-installed
- If not: `brew install openssl`

**Windows**:
- Use PowerShell alternative:
  ```powershell
  [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
  ```
- Or install OpenSSL: https://slproweb.com/products/Win32OpenSSL.html

**Linux**:
- Install OpenSSL: `sudo apt install openssl`

**Alternative**: Use online generator (not recommended for production):
- https://www.random.org/strings/ (generate 32-character string, base64 encode)

---

### 7.5 GitHub Issues

#### Issue: Cannot Push to GitHub Repository

**Symptoms**: 
- `git push` fails with authentication error

**Solutions**:
1. **Verify Git Credentials**:
   ```bash
   git config --global user.name "Your Name"
   git config --global user.email "your-email@example.com"
   ```

2. **Use Personal Access Token** (if using HTTPS):
   - GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
   - Generate new token with `repo` scope
   - Use token as password when pushing

3. **Use SSH Instead** (recommended):
   ```bash
   # Generate SSH key
   ssh-keygen -t ed25519 -C "your-email@example.com"
   
   # Add to GitHub
   # GitHub → Settings → SSH and GPG keys → New SSH key
   # Paste public key (~/.ssh/id_ed25519.pub)
   
   # Change remote URL to SSH
   git remote set-url origin git@github.com:yourusername/InvoiceMe.git
   ```

---

#### Issue: GitHub Actions Secrets Not Working

**Symptoms**: 
- CI/CD pipeline fails with "Secret not found" errors

**Solutions**:
1. **Verify Secret Names**:
   - Repository → Settings → Secrets and variables → Actions
   - Ensure secret names match exactly (case-sensitive)
   - Common names: `AWS_ACCESS_KEY_ID`, `DATABASE_URL`, `JWT_SECRET`

2. **Check Workflow File**:
   - Ensure workflow uses `secrets.SECRET_NAME` syntax:
     ```yaml
     env:
       DATABASE_URL: ${{ secrets.DATABASE_URL }}
     ```

3. **Re-add Secrets**:
   - Delete and re-add secrets if names don't match

---

### 7.6 General Issues

#### Issue: Port Already in Use

**Symptoms**: 
- "Port 8080 already in use" (backend)
- "Port 3000 already in use" (frontend)
- "Port 5432 already in use" (PostgreSQL)

**Solutions**:
1. **Find Process Using Port**:
   ```bash
   # macOS/Linux:
   lsof -i :8080
   
   # Windows:
   netstat -ano | findstr :8080
   ```

2. **Kill Process**:
   ```bash
   # macOS/Linux:
   kill -9 <PID>
   
   # Windows:
   taskkill /PID <PID> /F
   ```

3. **Change Port** (alternative):
   - Backend: Set `SERVER_PORT=8081` in `.env`
   - Frontend: Set `PORT=3001` in `package.json`
   - PostgreSQL: Change port mapping in `docker-compose.yml`

---

#### Issue: Permission Denied Errors

**Symptoms**: 
- "Permission denied" when running scripts
- Cannot execute `./mvnw` or `npm install`

**Solutions**:

**macOS/Linux**:
```bash
# Make script executable
chmod +x ./mvnw
chmod +x ./scripts/*.sh

# If npm install fails:
sudo chown -R $(whoami) ~/.npm
```

**Windows**:
- Run PowerShell/Command Prompt as Administrator
- Or change execution policy:
  ```powershell
  Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
  ```

---

## 8. Quick Reference

### 8.1 Command Cheat Sheet

#### Docker Commands
```bash
# Start PostgreSQL
docker-compose up -d postgres

# Stop PostgreSQL
docker-compose down

# View logs
docker logs invoiceme-postgres

# Connect to database
docker exec -it invoiceme-postgres psql -U postgres -d invoiceme

# Restart container
docker-compose restart postgres
```

#### Backend Commands (After Setup)
```bash
# Navigate to backend
cd backend

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build JAR
./mvnw clean package

# Windows:
.\mvnw.cmd spring-boot:run
```

#### Frontend Commands (After Setup)
```bash
# Navigate to frontend
cd frontend

# Install dependencies
npm install

# Run development server
npm run dev

# Build for production
npm run build

# Run tests
npm test
```

#### Git Commands
```bash
# Check status
git status

# Add changes
git add .

# Commit
git commit -m "feat: Add feature description"

# Push to GitHub
git push origin main

# Pull latest changes
git pull origin main
```

#### AWS CLI Commands
```bash
# Check identity
aws sts get-caller-identity

# List S3 buckets
aws s3 ls

# List verified SES emails
aws ses list-verified-email-addresses --region us-east-1

# Configure AWS CLI
aws configure
```

---

### 8.2 Environment Variable Checklist

**Required for Local Development**:
```bash
✅ DATABASE_URL=postgresql://postgres:postgres@localhost:5432/invoiceme
✅ JWT_SECRET=<32-character base64 string>
✅ AWS_REGION=us-east-1
✅ AWS_ACCESS_KEY_ID=<your-access-key>
✅ AWS_SECRET_ACCESS_KEY=<your-secret-key>
✅ AWS_SES_FROM_EMAIL=<verified-email@example.com>
✅ AWS_S3_BUCKET_NAME=invoiceme-pdfs-local
✅ NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
✅ SPRING_PROFILES_ACTIVE=dev
✅ SERVER_PORT=8080
```

**Required for GitHub Actions (Secrets)**:
```bash
✅ AWS_ACCESS_KEY_ID
✅ AWS_SECRET_ACCESS_KEY
✅ DATABASE_URL
✅ JWT_SECRET
✅ AWS_SES_FROM_EMAIL
✅ AWS_S3_BUCKET_NAME
✅ AWS_REGION
```

---

### 8.3 Service URLs & Endpoints

**Local Development**:
- Backend API: http://localhost:8080/api/v1
- Frontend App: http://localhost:3000
- PostgreSQL: localhost:5432

**AWS Services** (After Deployment):
- Backend (Elastic Beanstalk): `https://your-app.elasticbeanstalk.com`
- Frontend (Amplify): `https://your-app.amplifyapp.com`
- Supabase Dashboard: https://app.supabase.com/project/your-project-id

---

### 8.4 Account Creation Checklist

**Before Starting Development**:
- [ ] AWS Account created
- [ ] AWS IAM User (`invoiceme-deploy`) created
- [ ] AWS Access Keys saved securely
- [ ] AWS SES email verified
- [ ] AWS S3 bucket created
- [ ] Supabase account created
- [ ] Supabase project created
- [ ] Database connection string saved
- [ ] GitHub account created
- [ ] GitHub repository created
- [ ] GitHub Actions secrets configured (7 secrets)
- [ ] Java 17 installed
- [ ] Node.js 18+ installed
- [ ] Docker Desktop installed and running
- [ ] Repository cloned locally
- [ ] `.env` file created with all variables
- [ ] PostgreSQL container running
- [ ] All verification tests passing

---

### 8.5 Useful Links

**Documentation**:
- AWS Documentation: https://docs.aws.amazon.com/
- Supabase Documentation: https://supabase.com/docs
- Spring Boot Documentation: https://spring.io/projects/spring-boot
- Next.js Documentation: https://nextjs.org/docs
- Docker Documentation: https://docs.docker.com/

**Service Consoles**:
- AWS Console: https://console.aws.amazon.com/
- Supabase Dashboard: https://app.supabase.com/
- GitHub: https://github.com/

**Support**:
- AWS Support: https://aws.amazon.com/support/
- Supabase Support: https://supabase.com/support
- GitHub Support: https://support.github.com/

---

## Summary

This guide covers the complete setup process for the InvoiceMe project infrastructure. After completing all sections, you should have:

✅ **AWS Account** configured with IAM user, SES email, and S3 bucket  
✅ **Supabase Database** project created with connection string  
✅ **GitHub Repository** set up with CI/CD secrets  
✅ **Local Development Environment** ready (Java, Node.js, Docker)  
✅ **All Services Verified** and working  

**Next Steps**: 
1. Proceed with backend implementation (see project documentation)
2. Set up frontend development environment
3. Run integration tests
4. Deploy to AWS

**Questions or Issues?** Refer to Section 7 (Troubleshooting) or check project documentation.

---

**Document Version**: 1.0  
**Last Updated**: 2025-01-27

