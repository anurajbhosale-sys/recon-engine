# Ledger module rules

- Ledger tables are append-only. Never write UPDATE or DELETE for journal_entries or journal_lines.
  Mistakes are fixed with a reversing entry (see ADR 003).
- An entry and all its lines are saved in ONE transaction.
- Balance (debits = credits) is checked in Java by JournalEntryRules.validate.
  PLANNED (not built yet): a database trigger as a second layer.
- Money is always long minor units (cents). Never double or BigDecimal (ADR 001).
- Any change here needs a test proving entries still balance.