# InvoiceMe Documentation

This directory contains all project documentation organized by category.

---

## 📁 Directory Structure

### `/docs/milestones/`
Milestone status and completion reports:
- **M2_*** - M2 milestone status, progress, and completion reports
- **M3_*** - M3 milestone status, DevOps, and operational reports

### `/docs/deployment/`
Deployment guides, AWS configuration, and troubleshooting:
- **DEPLOYMENT_INSTRUCTIONS.md** - Step-by-step deployment guide
- **DEPLOYMENT_OPERATIONS_GUIDE.md** - Operations and maintenance guide
- **AWS_*** - AWS-specific deployment guides
- **ELASTIC_BEANSTALK_*** - Elastic Beanstalk configuration and troubleshooting
- **FIX_***, **TROUBLESHOOT_*** - Troubleshooting guides
- **CREATE_***, **UPDATE_***, **RESTART_*** - Environment management guides
- **CHANGE_***, **FIND_***, **HEALTH_CHECK_*** - Configuration guides

### `/docs/agents/`
Agent prompts, reports, and orchestration documents:
- **ORCHESTRATOR_OUTPUT.md** - Main orchestrator output with all agent prompts
- **FRONTEND_AGENT_*** - Frontend agent reports and status updates
- Agent-specific reports and outputs

### `/docs/execution/`
Project execution plans and guides:
- **AGENT_EXECUTION_PLAN.md** - Agent execution order and dependencies
- **EXECUTION_START.md** - Project execution start guide
- **SETUP_INSTRUCTIONS_AGENT_PROMPT.md** - Setup instructions agent prompt
- **REMAINING_QUESTIONS.md** - Open questions and clarifications

### `/docs/qa/`
QA testing guides and procedures:
- **QA_EXECUTION_GUIDE.md** - Comprehensive QA testing execution guide
- See also `/qa/results/` for test results and reports

### `/docs/operations/`
Operational guides and quick references:
- **ADMIN_LOGIN_*** - Admin user login instructions
- **RESTART_BACKEND_*** - Backend restart procedures

### `/docs/aws/`
AWS-specific guides (legacy, some files may be here):
- AWS deployment quick start guides
- AWS troubleshooting guides

### `/docs/`
Root documentation files:
- **deployment.md** - Deployment documentation (from DevOps agent)
- **monitoring.md** - Monitoring setup documentation
- **SETUP_INSTRUCTIONS.md** - Infrastructure setup instructions
- **SETUP_COMPLETION_REPORT.md** - Setup completion status
- **FEATURES.md** - Feature overview and scope
- **Demo_script.md** - Demo script for project presentation

---

## 📄 Root Directory Files (Keep in Root)

These files remain in the project root for easy access:

- **InvoiceMe.md** - Main assessment requirements document
- **PRD_1_Business_Reqs.md** - Business requirements document
- **PRD_2_Tech_Spec.md** - Technical specification document
- **ELASTIC_BEANSTALK_ENV_VALUES.txt** - Environment variables reference (for quick access)

---

## 🔍 Quick Reference

### Getting Started
1. Read `InvoiceMe.md` (root) for project overview
2. Read `PRD_1_Business_Reqs.md` (root) for business requirements
3. Read `PRD_2_Tech_Spec.md` (root) for technical specifications
4. Follow `/docs/execution/EXECUTION_START.md` for execution plan

### Deployment
- **Local Setup**: `/docs/SETUP_INSTRUCTIONS.md`
- **AWS Deployment**: `/docs/deployment/DEPLOYMENT_INSTRUCTIONS.md`
- **Operations**: `/docs/deployment/DEPLOYMENT_OPERATIONS_GUIDE.md`
- **Environment Variables**: `ELASTIC_BEANSTALK_ENV_VALUES.txt` (root)

### Testing
- **QA Guide**: `/docs/qa/QA_EXECUTION_GUIDE.md`
- **Test Results**: `/qa/results/`

### Agent Prompts
- **All Prompts**: `/AGENT_PROMPTS/` directory
- **Orchestrator Output**: `/docs/agents/ORCHESTRATOR_OUTPUT.md`

### Milestones
- **M2 Status**: `/docs/milestones/M2_*.md`
- **M3 Status**: `/docs/milestones/M3_*.md`

### Operations
- **Admin Login**: `/docs/operations/ADMIN_LOGIN_*.md`
- **Backend Restart**: `/docs/operations/RESTART_BACKEND_*.md`

---

**Last Updated**: 2025-01-27
