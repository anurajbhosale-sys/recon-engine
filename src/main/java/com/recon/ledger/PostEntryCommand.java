package com.recon.ledger;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PostEntryCommand(
        UUID tenantId,
        LocalDate effectiveDate,
        String description,
        String idempotencyKey,
        List<JournalLineRequest> lines,
        UUID reversesEntryId) {          // NEW: null for normal entries, set for reversals

    public PostEntryCommand {
        if (tenantId == null || effectiveDate == null || idempotencyKey == null || idempotencyKey.isBlank() || lines == null) {
            throw new InvalidJournalEntryException("tenantId, effectiveDate and idempotencyKey are required");
        }
        lines = List.copyOf(lines);
    }

    // CONCEPT: a convenience constructor for normal entries. It delegates to the full one with null,
    // so every existing caller (controller, tests) keeps compiling without changes.
    public PostEntryCommand(UUID tenantId, LocalDate effectiveDate, String description,
                            String idempotencyKey, List<JournalLineRequest> lines) {
        this(tenantId, effectiveDate, description, idempotencyKey, lines, null);
    }
}