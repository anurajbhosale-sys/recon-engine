package com.recon.ledger;

import java.util.UUID;

// created = true  → a new entry was saved
// created = false → this idempotency key was already used; we returned the existing entry
public record PostEntryResult(UUID entryId, boolean created) {}