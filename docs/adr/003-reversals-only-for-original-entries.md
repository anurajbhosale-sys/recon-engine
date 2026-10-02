# ADR 003: Reversals — at most once, and only for original entries
- **Status:** accepted
- **Date:** 2026-10-02

## Context
The ledger is append-only: entries are never edited or deleted. Mistakes must be corrected
by posting a reversing entry that mirrors the original with every direction flipped, so the
two net to zero while both remain in the history for auditing.

## Decision
- A reversal is a new entry with `reverses_entry_id` pointing at the original.
- An entry can be reversed **at most once**, enforced by the service (clear 409 error) and by
  a `UNIQUE` constraint on `reverses_entry_id` (V3), which also handles concurrent requests.
- **Reversing a reversal is forbidden.** Only original entries can be reversed. If a reversal
  was itself a mistake, the correct entry is simply posted again as a new entry.
- Reversals reuse the normal posting path (validation, account checks, idempotency).
- The idempotency replay check runs **before** the "already reversed" check, so a retried
  reversal gets 200 instead of a misleading 409.

## Alternatives considered
- **Allow reversing reversals:** more flexible, but correction chains of any depth are hard
  to audit and reason about.
- **Edit or delete the wrong entry:** simplest, but destroys the audit trail and breaks the
  append-only invariant.

## Consequences
- Every correction chain is exactly one step deep: original → reversal.
- Re-posting after a mistaken reversal creates a new entry with a new idempotency key.
- Reversals are dated when they happen (today); past periods are never rewritten.