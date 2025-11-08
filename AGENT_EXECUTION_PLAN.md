# InvoiceMe Agent Execution Plan

**Date**: 2025-01-27  
**Status**: Ready to Execute  
**Purpose**: Define execution order, dependencies, and parallelization opportunities

---

## Execution Order & Dependencies

### Phase 1: Foundation (M1 - Domain & API Freeze)
**Duration**: 1 day  
**Goal**: Freeze database schema, domain model, and API contracts

#### 1.1 Data/DB Agent ⚡ **START FIRST** (Sequential)
**Prompt**: `ORCHESTRATOR_OUTPUT.md` Section 6.C  
**Dependencies**: None  
**Outputs Needed By**: Backend Agent, Frontend Agent (indirectly)

**Deliverables**:
- Database schema (Flyway migrations V1-V10)
- ERD diagram
- Schema documentation

**Why Sequential**: Foundation layer - everything depends on database structure

---

#### 1.2 Backend Agent - M1 Phase (Sequential)
**Prompt**: `ORCHESTRATOR_OUTPUT.md` Section 6.A (M1 deliverables only)  
**Dependencies**: Data/DB Agent (needs schema to understand relationships)  
**Outputs Needed By**: Frontend Agent, QA Agent

**M1 Deliverables** (Domain & API Freeze):
- `/backend/docs/domain-aggregates.md` — DDD boundaries
- `/backend/docs/api/openapi.yaml` — API contracts
- `/backend/docs/events.md` — Domain events list

**Why Sequential**: Must wait for database schema to design domain model correctly

---

### Phase 2: Core Implementation (M2 - Core Flows Working)
**Duration**: 2-3 days  
**Goal**: Implement all CRUD operations and core business logic

#### 2.1 Backend Agent - M2 Phase ⚡ **PARALLEL WITH FRONTEND** (Parallel)
**Prompt**: `ORCHESTRATOR_OUTPUT.md` Section 6.A (M2 deliverables)  
**Dependencies**: M1 complete (API contracts frozen)  
**Outputs Needed By**: QA Agent, DevOps Agent

**M2 Deliverables**:
- Command handlers (CreateCustomer, CreateInvoice, MarkAsSent, RecordPayment, etc.)
- Query handlers (GetCustomer, ListCustomers, GetInvoice, etc.)
- Domain events implementation
- Integration with database (using schema from Data/DB Agent)

**Parallel Opportunity**: Once API contracts are frozen (M1), Backend can implement APIs while Frontend builds UI that consumes them

---

#### 2.2 Frontend Agent - M2 Phase ⚡ **PARALLEL WITH BACKEND** (Parallel)
**Prompt**: `ORCHESTRATOR_OUTPUT.md` Section 6.B  
**Dependencies**: M1 complete (needs OpenAPI spec/API contracts)  
**Outputs Needed By**: QA Agent (for E2E tests)

**M2 Deliverables**:
- React components (Customer list/detail, Invoice list/detail, Payment form)
- Custom hooks (ViewModels): useCustomers, useInvoiceDetail, usePayments
- API integration (consumes Backend APIs)
- Authentication UI (login page)

**Parallel Opportunity**: Can start building UI as soon as API contracts are frozen, even if Backend APIs aren't fully implemented (can mock APIs initially)

---

#### 2.3 DevOps Agent - Local Setup ⚡ **CAN START EARLY** (Parallel)
**Prompt**: `ORCHESTRATOR_OUTPUT.md` Section 6.E (local setup only)  
**Dependencies**: None (can start anytime)  
**Outputs Needed By**: Backend Agent, Frontend Agent (for local development)

**Early Deliverables** (can start in Phase 1 or 2):
- `/docker-compose.yml` — Already created, but can enhance
- Local development environment verification
- GitHub Actions workflow skeleton (can be created early)

**Parallel Opportunity**: Can work on local setup and CI/CD configuration in parallel with Backend/Frontend development

**Note**: AWS deployment will wait until M2 complete (needs Backend JAR and Frontend build)

---

### Phase 3: Testing & Validation (M3 - Non-Functional Targets)
**Duration**: 0.5 day  
**Goal**: Validate performance and error handling

#### 3.1 QA Agent ⚡ **SEQUENTIAL** (Sequential)
**Prompt**: `ORCHESTRATOR_OUTPUT.md` Section 6.D  
**Dependencies**: M2 complete (needs Backend APIs and Frontend UI working)  
**Outputs Needed By**: Docs Agent (for demo evidence)

**Deliverables**:
- Integration tests (all 6 PRD scenarios)
- Performance tests (local + AWS)
- Test evidence (logs, screenshots, latency tables)

**Why Sequential**: Must wait for Backend and Frontend to be functional to write meaningful tests

---

#### 3.2 DevOps Agent - AWS Deployment ⚡ **PARALLEL WITH QA** (Parallel)
**Prompt**: `ORCHESTRATOR_OUTPUT.md` Section 6.E (AWS deployment)  
**Dependencies**: M2 complete (needs Backend JAR and Frontend build)  
**Outputs Needed By**: QA Agent (for AWS performance testing)

**M3 Deliverables**:
- AWS Elastic Beanstalk deployment (Backend)
- AWS Amplify deployment (Frontend)
- CloudWatch monitoring setup
- CI/CD pipeline functional

**Parallel Opportunity**: Can deploy to AWS while QA runs local tests, then QA can test AWS deployment

---

### Phase 4: Extended Features (M4 - Extended Features)
**Duration**: 1-2 days  
**Goal**: Implement extended features (Recurring Invoices, Late Fees, Refunds, etc.)

#### 4.1 Backend Agent - M4 Phase ⚡ **PARALLEL WITH FRONTEND** (Parallel)
**Dependencies**: M2 complete  
**Deliverables**: Extended feature APIs (Recurring Invoices, Refunds, Dashboard, etc.)

#### 4.2 Frontend Agent - M4 Phase ⚡ **PARALLEL WITH BACKEND** (Parallel)
**Dependencies**: M2 complete  
**Deliverables**: Extended feature UI (Recurring Invoices UI, Refunds UI, Customer Portal, Dashboard)

**Parallel Opportunity**: Same as M2 - Backend implements APIs, Frontend builds UI

---

### Phase 5: Documentation & Demo (M5 - Demo + Writeups)
**Duration**: 1 day  
**Goal**: Create final deliverables

#### 5.1 Docs Agent ⚡ **SEQUENTIAL** (Sequential)
**Prompt**: `ORCHESTRATOR_OUTPUT.md` Section 6.F  
**Dependencies**: M2 complete (or M4 if extended features), QA results, Demo script  
**Outputs Needed By**: Final submission

**Deliverables**:
- TECHNICAL_WRITEUP.md
- AI_TOOL_DOCUMENTATION.md
- Demo video coordination

**Why Sequential**: Must wait for all code, tests, and evidence to be complete

---

## Parallelization Summary

### ✅ Can Run in Parallel

| Phase | Agents | Why Parallel |
|-------|--------|--------------|
| **M1** | None | Sequential dependencies (schema → domain model) |
| **M2** | Backend + Frontend | API contracts frozen, can work independently |
| **M2** | DevOps (local setup) | No code dependencies, can start anytime |
| **M3** | QA + DevOps (AWS deploy) | QA tests locally while DevOps deploys to AWS |
| **M4** | Backend + Frontend | Same as M2 - parallel development |

### ⚠️ Must Run Sequentially

| Order | Agent | Depends On | Reason |
|-------|-------|------------|--------|
| 1 | Data/DB Agent | None | Foundation layer |
| 2 | Backend Agent (M1) | Data/DB Agent | Needs schema for domain model |
| 3 | Backend Agent (M2) | Backend Agent (M1) | Needs API contracts frozen |
| 4 | Frontend Agent | Backend Agent (M1) | Needs API contracts |
| 5 | QA Agent | Backend Agent (M2) + Frontend Agent (M2) | Needs working APIs and UI |
| 6 | Docs Agent | All agents complete | Needs all deliverables |

---

## Recommended Execution Timeline

### Day 1: M1 - Domain & API Freeze
```
09:00 - 12:00: Data/DB Agent (database schema, ERD, migrations)
12:00 - 17:00: Backend Agent M1 (domain aggregates, OpenAPI spec, events)
```

### Day 2-4: M2 - Core Flows (Parallel Development)
```
Day 2:
09:00 - 17:00: Backend Agent M2 (command/query handlers) ⚡ PARALLEL
09:00 - 17:00: Frontend Agent M2 (UI components, hooks) ⚡ PARALLEL
09:00 - 12:00: DevOps Agent (local setup, CI/CD skeleton) ⚡ PARALLEL

Day 3-4:
Continue Backend + Frontend parallel development
DevOps can work on AWS deployment prep
```

### Day 5: M3 - Non-Functional Validation
```
09:00 - 12:00: DevOps Agent (AWS deployment) ⚡ PARALLEL
09:00 - 17:00: QA Agent (integration tests, performance tests) ⚡ PARALLEL
```

### Day 6-7: M4 - Extended Features (If Time Permits)
```
Backend Agent M4 ⚡ PARALLEL
Frontend Agent M4 ⚡ PARALLEL
```

### Day 7-8: M5 - Documentation
```
Docs Agent (technical writeup, AI documentation, demo video)
```

---

## Critical Path (Longest Sequence)

**Data/DB → Backend M1 → Backend M2 → QA → Docs**

This is the minimum sequential path. All other work can happen in parallel.

---

## Execution Strategy Recommendations

### Option A: Maximum Parallelization (Recommended)
1. **Start**: Data/DB Agent (Day 1 morning)
2. **After Data/DB**: Backend Agent M1 (Day 1 afternoon)
3. **After M1**: Backend M2 + Frontend M2 + DevOps local setup (Day 2-4, all parallel)
4. **After M2**: QA + DevOps AWS deploy (Day 5, parallel)
5. **After M3**: Docs Agent (Day 7-8)

**Timeline**: 5-7 days (with parallelization)

### Option B: Sequential (Safer, Slower)
Run all agents sequentially in dependency order.

**Timeline**: 7-10 days (no parallelization)

---

## Agent Prompt Locations

All prompts ready in `ORCHESTRATOR_OUTPUT.md` Section 6:
- **6.A**: Backend Agent
- **6.B**: Frontend Agent
- **6.C**: Data/DB Agent
- **6.D**: QA Agent
- **6.E**: DevOps Agent
- **6.F**: Docs Agent

---

## Next Steps

1. **Issue Data/DB Agent Prompt** (Start Phase 1)
2. **After Data/DB completes**: Issue Backend Agent M1 Prompt
3. **After M1 completes**: Issue Backend Agent M2 + Frontend Agent prompts (parallel)
4. **After M2 completes**: Issue QA Agent + DevOps Agent AWS prompts (parallel)
5. **After M3 completes**: Issue Docs Agent prompt

---

**Status**: ✅ Ready to execute with parallelization strategy

