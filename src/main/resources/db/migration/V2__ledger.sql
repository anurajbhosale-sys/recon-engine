-- A company using the system. Every other table belongs to one tenant.
CREATE TABLE tenants (
                         id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                         name        TEXT        NOT NULL,
                         created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- A bucket that money moves in and out of.
CREATE TABLE accounts (
                          id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                          tenant_id   UUID        NOT NULL REFERENCES tenants(id),
                          code        TEXT        NOT NULL,
                          name        TEXT        NOT NULL,
                          type        TEXT        NOT NULL CHECK (type IN ('ASSET','LIABILITY','EQUITY','REVENUE','EXPENSE')),
                          currency    CHAR(3)     NOT NULL,
                          created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
                          UNIQUE (tenant_id, code)
);

-- One money movement. Never edited; mistakes are fixed by a reversing entry.
CREATE TABLE journal_entries (
                                 id                UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                 tenant_id         UUID        NOT NULL REFERENCES tenants(id),
                                 effective_date    DATE        NOT NULL,
                                 description       TEXT,
                                 idempotency_key   TEXT        NOT NULL,
                                 reverses_entry_id UUID        REFERENCES journal_entries(id),
                                 created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 UNIQUE (tenant_id, idempotency_key)
);

-- One debit or credit line within an entry.
CREATE TABLE journal_lines (
                               id           UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
                               entry_id     UUID    NOT NULL REFERENCES journal_entries(id),
                               account_id   UUID    NOT NULL REFERENCES accounts(id),
                               direction    TEXT    NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
                               amount_minor BIGINT  NOT NULL CHECK (amount_minor > 0),
                               currency     CHAR(3) NOT NULL
);

-- Postgres does not index foreign keys automatically.
CREATE INDEX idx_journal_lines_entry_id   ON journal_lines(entry_id);
CREATE INDEX idx_journal_lines_account_id ON journal_lines(account_id);