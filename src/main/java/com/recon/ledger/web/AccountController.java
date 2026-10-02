package com.recon.ledger.web;

import com.recon.ledger.AccountBalance;
import com.recon.ledger.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tenants/{tenantId}/accounts")
public class AccountController {

    private final LedgerService ledgerService;

    public AccountController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping("/{accountId}/balance") // CONCEPT: GET = read only, never changes anything
    public AccountBalance balance(@PathVariable UUID tenantId, @PathVariable UUID accountId) {
        return ledgerService.getBalance(tenantId, accountId);
    }
}