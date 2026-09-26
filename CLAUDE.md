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
6. Code in ledger/ and ingestion/ is hand-written by Anuraj and is the reference implementation.
   Match its patterns; don't refactor it without asking.

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
- Java 21, Spring Boot 4 (modular starters; don't use Boot 3 patterns), Maven via ./mvnw.
- Testcontainers for integration tests (never H2).
- Plain UUID foreign keys over JPA associations unless an ADR says otherwise.
- Package-by-feature modular monolith: `ingestion`, `ledger`, `matching`, `breaks`, `tenancy`,
  `ai`, `shared`. Modules call each other's public services, never each other's repositories.

## Commands
- Start DB: `docker compose up -d` (Postgres on host port 5433)
- Load MCP env: `set -a; source .env; set +a`
- Run: `./mvnw spring-boot:run`
- Test: `./mvnw verify`
