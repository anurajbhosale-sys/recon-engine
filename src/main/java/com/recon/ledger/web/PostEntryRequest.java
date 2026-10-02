package com.recon.ledger.web;

import com.recon.ledger.Direction;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// CONCEPT: the JSON shape clients send. Validation annotations reject bad input
// BEFORE it reaches the service (missing fields, wrong types, negative amounts).
public record PostEntryRequest(
        @NotNull LocalDate effectiveDate,
        String description,
        @NotEmpty @Valid List<Line> lines) {   // CONCEPT: @Valid = also validate each Line inside

    public record Line(
            @NotNull UUID accountId,
            @NotNull Direction direction,
            @Positive long amountMinor,
            @NotBlank @Size(min = 3, max = 3) String currency) {}
}