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

@RestController                                          // CONCEPT: handles HTTP, returns JSON
@RequestMapping("/tenants/{tenantId}/journal-entries")   // CONCEPT: the URL this class answers
public class JournalEntryController {

    private final LedgerService ledgerService;

    public JournalEntryController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<PostEntryResult> postEntry(
            @PathVariable UUID tenantId,                              // from the URL
            @RequestHeader("Idempotency-Key") String idempotencyKey,  // from the header, Stripe-style
            @Valid @RequestBody PostEntryRequest body) {              // from the JSON body, validated

        // Translate the API shape → the internal command
        var command = new PostEntryCommand(
                tenantId,
                body.effectiveDate(),
                body.description(),
                idempotencyKey,
                body.lines().stream()
                        .map(l -> new JournalLineRequest(l.accountId(), l.direction(), l.amountMinor(), l.currency()))
                        .toList());

        PostEntryResult result = ledgerService.postEntry(command);

        // CONCEPT: the status code tells the client what happened
        if (result.created()) {
            URI location = URI.create("/tenants/" + tenantId + "/journal-entries/" + result.entryId());
            return ResponseEntity.created(location).body(result);   // 201 Created + Location header
        }
        return ResponseEntity.ok(result);                            // 200 OK = duplicate, already done
    }
}
