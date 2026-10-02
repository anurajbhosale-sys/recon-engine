package com.recon.ledger;

import java.util.UUID;

public record AccountBalance(UUID accountId, String code, String currency, long balanceMinor) {}