# Ledger module rules
(Move this file into the real ledger package directory once it exists.)

- Tables here are append-only. No UPDATE/DELETE statements on ledger tables, ever.
- A journal entry and all its lines are written in ONE transaction; the balance check runs
  before commit and a DB-level check/trigger backs it up.
- Reversals reference the original entry id; never mutate the original.
- Any change to this module requires a test proving entries still balance.
