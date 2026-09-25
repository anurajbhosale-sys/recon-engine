# Roadmap — each phase ends demo-able

## Phase 1 — Core ledger & ingestion
Tenants, accounts, double-entry journal, CSV settlement-file ingestion (idempotent),
processor-event ingestion via REST, Docker Compose, CI.
Done when: re-uploading the same file creates zero duplicates; unbalanced entries are rejected.
No-AI rebuild: the idempotent file ingester.

## Phase 2 — Deterministic matching
Rule engine (exact amount + date window + reference). Matches, breaks, break lifecycle
(open → investigating → resolved). Seeded dataset with known expected breaks.
Done when: seeded data reconciles exactly to the expected breaks.
No-AI rebuild: one matching rule end to end.

## Phase 3 — Concurrency hardening
Parallel matcher workers with `SELECT ... FOR UPDATE SKIP LOCKED`; optimistic locking on breaks;
load test (Gatling or k6). Incident drills start.
Done when: N concurrent workers never double-match (test + DB constraint prove it).
No-AI rebuild: the worker claim query and its transaction boundary.

## Phase 4 — Event-driven + observability
Kafka for processor events, transactional outbox, retries, dead-letter topic.
OpenTelemetry traces, Prometheus metrics, Grafana dashboard (match rate, break age, ingest lag).
Done when: killing the app mid-publish loses no events and duplicates are harmless.
No-AI rebuild: the outbox relay.

## Phase 5 — AI layer
pgvector embeddings for fuzzy description matching; LLM break triage with structured outputs;
RAG over resolved breaks; evaluation harness (precision/recall on labeled data); cost and
latency metrics per call.
Done when: eval report shows AI lift over rules-only, with cost per 1k breaks.

## Phase 6 — Investigation agent
Tool-using agent (`query_ledger`, `fetch_settlement`, `find_similar_breaks`), read-only tools,
proposes a resolution with cited evidence.
Done when: agent resolutions are evaluated against the labeled set.

## Phase 7 — Deploy & harden
AWS (ECS + RDS + S3), secrets management, OWASP Top 10 review, partitioning of transaction
tables, injected production incidents.
Done when: deployed, monitored, and you've debugged 3 injected incidents from telemetry alone.
