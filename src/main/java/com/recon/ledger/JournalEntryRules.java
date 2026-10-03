package com.recon.ledger;

import java.util.List;

public final class JournalEntryRules {

    private JournalEntryRules() {}

    public static void validate(List<JournalLineRequest> lines) {
        // Check 0: at least 2 lines
        if (lines == null || lines.size() < 2) {
            throw new InvalidJournalEntryException("An entry needs at least 2 lines");
        }

        // Rule 1: every line uses the same currency
        var currency = lines.get(0).currency();
        for (JournalLineRequest line : lines) {
            if (!currency.equals(line.currency())) {
                throw new InvalidJournalEntryException("All journal lines must use the same currency");
            }
        }

        // Rule 2: total debits must equal total credits
        long totalDebits = 0;
        long totalCredits = 0;
        try {
            for (JournalLineRequest line : lines) {
                if (line.direction() == Direction.DEBIT) {
                    // CONCEPT: addExact throws instead of silently wrapping around past Long.MAX_VALUE
                    totalDebits = Math.addExact(totalDebits, line.amountMinor());
                } else {
                    totalCredits = Math.addExact(totalCredits, line.amountMinor());
                }
            }
        } catch (ArithmeticException e) {
            // CONCEPT: turn the overflow into OUR exception → the client gets a clean 400, not a 500
            throw new InvalidJournalEntryException("Entry total is out of range");
        }

        if (totalDebits != totalCredits) {
            throw new InvalidJournalEntryException(
                    "Unbalanced entry: debits=" + totalDebits + ", credits=" + totalCredits);
        }
    }
}