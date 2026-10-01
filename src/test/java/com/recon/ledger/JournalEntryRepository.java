package com.recon.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    // CONCEPT: the idempotency check. "Did this tenant already send this key?"
    // Fast because of the UNIQUE (tenant_id, idempotency_key) constraint,
    // which Postgres backs with an index automatically.
    Optional<JournalEntry> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey);
}