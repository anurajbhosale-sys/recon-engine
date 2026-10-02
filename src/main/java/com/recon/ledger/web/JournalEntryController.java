package com.recon.ledger.web;

import com.recon.ledger.JournalLineRequest;
import com.recon.ledger.LedgerService;
import com.recon.ledger.PostEntryCommand;
import com.recon.ledger.PostEntryResult;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/tenants/{tenantId}/journal-entries")
public class JournalEntryController {

    private final LedgerService ledgerService;

    public JournalEntryController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<PostEntryResult> postEntry(
            @PathVariable UUID tenantId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PostEntryRequest body) {

        var command = new PostEntryCommand(
                tenantId,
                body.effectiveDate(),
                body.description(),
                idempotencyKey,
                body.lines().stream()
                        .map(l -> new JournalLineRequest(l.accountId(), l.direction(), l.amountMinor(), l.currency()))
                        .toList());

        return toResponse(tenantId, ledgerService.postEntry(command));
    }

    // NEW: POST /tenants/{tenantId}/journal-entries/{entryId}/reversal
    // CONCEPT: no body needed. The server builds the mirror entry from the original.
    @PostMapping("/{entryId}/reversal")
    public ResponseEntity<PostEntryResult> reverseEntry(
            @PathVariable UUID tenantId,
            @PathVariable UUID entryId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        return toResponse(tenantId, ledgerService.reverseEntry(tenantId, entryId, idempotencyKey));
    }

    // CONCEPT: one place for "201 + Location if new, 200 if replay" (DRY)
    private ResponseEntity<PostEntryResult> toResponse(UUID tenantId, PostEntryResult result) {
        if (result.created()) {
            URI location = URI.create("/tenants/" + tenantId + "/journal-entries/" + result.entryId());
            return ResponseEntity.created(location).body(result);
        }
        return ResponseEntity.ok(result);
    }
}