-- An entry can be reversed at most once.
-- Postgres UNIQUE ignores NULLs, so normal entries (reverses_entry_id IS NULL) are unaffected.
ALTER TABLE journal_entries
    ADD CONSTRAINT uq_journal_entries_reverses_entry_id UNIQUE (reverses_entry_id);