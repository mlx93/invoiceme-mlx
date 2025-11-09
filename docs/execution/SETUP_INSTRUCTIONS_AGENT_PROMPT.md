# Setup Instructions Agent — Prompt

**[AGENT]: Setup Instructions**

**GOAL**: Generate comprehensive, step-by-step setup instructions for InvoiceMe project infrastructure. Instructions must be detailed enough for manual execution by a developer with minimal AWS/cloud experience. Include troubleshooting, verification steps, and platform-specific variations (macOS, Windows, Linux).

**INPUTS**:
- ORCHESTRATOR_OUTPUT.md (Section 10.2 — existing high-level setup instructions)
- PRD_2_Tech_Spec.md (deployment architecture, technology stack)
- User questions/concerns about setup process

**DELIVERABLES**:
- `/docs/SETUP_INSTRUCTIONS.md` — Comprehensive setup guide with:
  - **Section 1: Prerequisites** — System requirements, software versions, account prerequisites
  - **Section 2: AWS Account Setup** — Detailed step-by-step with screenshots/annotations (if possible), IAM user creation, SES verification, S3 bucket creation, CloudWatch setup
  - **Section 3: Supabase Database Setup** — Project creation, connection string retrieval, pooling configuration
  - **Section 4: GitHub Repository Setup** — Repository creation, GitHub Actions secrets configuration
  - **Section 5: Local Development Environment** — Java 17, Node.js 18+, Docker Desktop installation (platform-specific), environment variables configuration
  - **Section 6: Verification & Testing** — Test each service connection (AWS CLI, Supabase connection, Docker, etc.)
  - **Section 7: Troubleshooting** — Common issues and solutions (AWS permissions, SES sandbox mode, Docker connection issues, etc.)
  - **Section 8: Quick Reference** — Command cheat sheet, environment variable checklist

**DONE CRITERIA**:
1. ✅ All setup steps include:
   - Exact navigation paths (e.g., "AWS Console → IAM → Users → Create User")
   - Screenshot descriptions or UI element descriptions
   - Expected outcomes/verification steps
   - Platform-specific variations (macOS vs Windows vs Linux)
2. ✅ Troubleshooting section covers:
   - AWS IAM permission errors
   - SES sandbox mode limitations
   - Supabase connection failures
   - Docker Desktop startup issues
   - Environment variable misconfigurations
3. ✅ Verification steps for each service:
   - AWS CLI test: `aws sts get-caller-identity`
   - Supabase connection test: `psql` command or connection string test
   - Docker test: `docker ps`
   - Java/Node.js version checks
4. ✅ Environment variable checklist:
   - All required variables listed with examples
   - Where to store them (`.env` file, GitHub Secrets, AWS Parameter Store)
   - Security best practices (never commit secrets)

**REPORT BACK WITH**:
- **Summary** (≤10 bullets):
  - Setup instructions created (sections completed)
  - Platform variations covered (macOS, Windows, Linux)
  - Troubleshooting scenarios documented (count)
  - Verification steps included (count)
  - Open questions/uncertainties (if any)
- **Artifacts paths**:
  - Setup guide: `/docs/SETUP_INSTRUCTIONS.md`
- **Evidence**:
  - Sample verification commands (tested locally if possible)
  - Troubleshooting scenarios validated

**DO NOT**:
- Assume user has prior AWS/cloud experience (explain concepts briefly)
- Skip verification steps (each service must be testable)
- Use platform-specific commands without alternatives (provide macOS, Windows, Linux versions)

---

**Example Structure** (for reference):

```markdown
# InvoiceMe Setup Instructions

## 1. Prerequisites
- macOS 12+ / Windows 10+ / Linux (Ubuntu 20.04+)
- Admin/sudo access for software installation
- Credit card for AWS account (free tier available)

## 2. AWS Account Setup

### 2.1 Create AWS Account
**Step 1**: Navigate to https://aws.amazon.com/
**Step 2**: Click "Create an AWS Account"
**Step 3**: Enter email, password, account name
**Step 4**: Provide credit card (won't be charged unless you exceed free tier)
**Step 5**: Verify email address
**Verification**: Log into AWS Console, verify you see "Services" menu

### 2.2 Create IAM User for Deployment
**Step 1**: AWS Console → Search "IAM" → Click "IAM"
**Step 2**: Left sidebar → "Users" → "Create User"
**Step 3**: User name: `invoiceme-deploy`
**Step 4**: Select "Programmatic access" (not console access)
**Step 5**: Click "Next: Permissions"
**Step 6**: Click "Attach existing policies directly"
**Step 7**: Search and select:
  - `AWSElasticBeanstalkFullAccess`
  - `AWSAmplifyFullAccess`
  - `AmazonSESFullAccess`
  - `AmazonS3FullAccess`
  - `CloudWatchFullAccess`
**Step 8**: Click "Next: Tags" → "Next: Review" → "Create User"
**Step 9**: **CRITICAL**: Copy "Access Key ID" and "Secret Access Key" (shown only once!)
**Step 10**: Save credentials securely (password manager recommended)
**Verification**: Test with AWS CLI:
  ```bash
  aws configure
  # Enter Access Key ID
  # Enter Secret Access Key
  # Enter region: us-east-1
  # Enter output format: json
  aws sts get-caller-identity
  # Should return your IAM user ARN
  ```

[... continue for all services ...]

## 7. Troubleshooting

### Issue: AWS CLI "Access Denied"
**Symptoms**: `aws sts get-caller-identity` returns "AccessDenied"
**Solution**: 
1. Verify IAM user has correct policies attached
2. Check AWS credentials: `aws configure list`
3. Verify region matches: `aws configure get region`

### Issue: SES Email Not Sending
**Symptoms**: Email service returns "Email address not verified"
**Solution**: 
1. SES starts in "Sandbox" mode
2. Can only send to verified email addresses
3. Verify recipient email in SES Console → Verified identities
4. For production: Request production access (takes 24-48 hours)

[... more troubleshooting scenarios ...]
```

---

**Note**: This agent should be able to answer follow-up questions like:
- "How do I verify my AWS SES email?"
- "What if I don't have a domain for SES?"
- "How do I test Supabase connection locally?"
- "Docker Desktop won't start on Windows, what do I do?"

