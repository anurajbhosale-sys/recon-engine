# CLAUDE.md — Recon Engine

Multi-tenant **payment reconciliation engine**: ingests internal ledger entries, processor
events, and bank settlement files; matches them; surfaces "breaks" for investigation.
Deterministic rules match first. AI assists only with leftovers and never decides ledger state.

**Current phase and next step:** read `docs/progress.md` at the start of every session.
**Phase plan:** `docs/roadmap.md` (read only when planning a new phase).

## Who you're working with
Anuraj — backend engineer building this for fintech backend interviews. He must be able to
explain every line. Understanding beats speed.

## Working agreement
1. Plan before code. Use the `plan-feature` skill for any feature. Wait for approval.
2. One slice per session. Stop after each slice; summarize what changed, why, what to review.
3. Teaching style: plain-English analogy before jargon; `// CONCEPT:` comments on non-obvious
   ideas; familiar patterns (controllers, DTOs, repos) → just write them; genuinely new material
   (concurrency, Kafka, outbox, partitioning) → ask Anuraj to predict/propose first, and leave
   the core logic as `TODO(human)` when he's in Learning mode.
4. Significant decisions get an ADR in `docs/adr/` (copy `000-template.md`).
5. Never skip ahead of the current phase or add infrastructure before it's justified.

## Invariants (never violate)
- Money = `BIGINT` minor units + ISO currency code. Never `double`/`float`.
- Ledger entries are append-only. Corrections are reversing entries.
- Every journal entry balances (debits = credits) — enforced in code and tested.
- All ingestion is idempotent (natural keys / idempotency keys + unique constraints).
- A source record is matched at most once — enforced by the database.
- AI output is a suggestion with confidence; confirmation is by rule or human.
- Every tenant-scoped query filters by `tenant_id`; tests prove cross-tenant isolation.
- Never edit an applied Flyway migration (a hook enforces this).

## Stack & conventions
- Java 21, Spring Boot (latest stable), build tool: <DECIDE IN PHASE 1, then record here>.
- PostgreSQL + Flyway. Testcontainers for integration tests (no H2).
- Plain UUID foreign keys over JPA associations unless an ADR says otherwise.
- Package-by-feature modular monolith: `ingestion`, `ledger`, `matching`, `breaks`, `tenancy`,
  `ai`, `shared`. Modules call each other's public services, never each other's repositories.
- Docker Compose for local dev; GitHub Actions CI.

## Commands
<Fill in during Phase 1: build, test, run, start DB, lint/format.>
