package com.recon.ledger;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Immutable // CONCEPT: Hibernate never issues UPDATE for this entity (append-only, layer 1)
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate; // CONCEPT: LocalDate = SQL DATE (no time, no timezone)

    private String description;      // nullable, so no @Column needed

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "reverses_entry_id")
    private UUID reversesEntryId;    // null for normal entries; set only on reversals

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected JournalEntry() {}

    public JournalEntry(UUID tenantId, LocalDate effectiveDate, String description,
                        String idempotencyKey, UUID reversesEntryId) {
        this.tenantId = tenantId;
        this.effectiveDate = effectiveDate;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.reversesEntryId = reversesEntryId;
    }

    // CONCEPT: getters only, no setters. Once created, an entry can't be changed.
    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public String getDescription() { return description; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public UUID getReversesEntryId() { return reversesEntryId; }
    public Instant getCreatedAt() { return createdAt; }
}