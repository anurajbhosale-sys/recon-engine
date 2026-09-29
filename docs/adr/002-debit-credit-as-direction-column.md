# ADR 002: Store debit/credit as a direction column with positive amounts
- **Status:** accepted
- **Date:** 2026-09-29

## Context
In double-entry bookkeeping, every journal line is either a debit or a credit, and each
journal entry must balance (total debits = total credits). The `journal_lines` table needs
a way to record which side each line is on.

## Decision
Each line stores a `direction` column ('DEBIT' or 'CREDIT') and an `amount_minor` that is
always positive, enforced by `CHECK (amount_minor > 0)`.

## Alternatives considered
- **Signed amounts** (debits positive, credits negative): the balance check becomes a simple
  `SUM(amount) = 0`, which is easy to query and enforce. Rejected because rows are harder
  for humans to read and debug, and the meaning of the sign is a convention that is easy
  to get backwards across different account types.

## Consequences
- Rows read exactly like an accounting journal, which makes debugging and reconciliation
  investigations easier.
- A zero or negative amount is impossible at the database level.
- The balance check needs a CASE expression:
  `SUM(CASE WHEN direction = 'DEBIT' THEN amount_minor ELSE -amount_minor END) = 0`.
- Account balances need the same CASE logic, so it should live in one shared place in the
  code rather than being repeated.