package com.recon.ledger;

import java.util.List;

public final class JournalEntryRules {

    private JournalEntryRules() {} // CONCEPT: static utility class, so no instances

    public static void validate(List<JournalLineRequest> lines) {
        if (lines == null || lines.size() < 2) {
            throw new InvalidJournalEntryException("An entry needs at least 2 lines");
        }
        var currency = lines.get(0).currency();

        for (JournalLineRequest line : lines) {
            if (!currency.equals(line.currency())) {
                throw new InvalidJournalEntryException(
                        "All journal lines must use the same currency"
                );
            }
        }

        long totalDebits = lines.stream()
                .filter(line -> line.direction() == Direction.DEBIT)
                .mapToLong(JournalLineRequest::amountMinor)   // CONCEPT: mapToLong gives a LongStream, and sum() adds it
                .sum();

        long totalCredits = lines.stream()
                .filter(line -> line.direction() == Direction.CREDIT)
                .mapToLong(JournalLineRequest::amountMinor)
                .sum();

        if (totalDebits != totalCredits) {
            throw new InvalidJournalEntryException(
                    "Unbalanced entry: debits=" + totalDebits + ", credits=" + totalCredits);
        }
        // TODO(human): implement the remaining two rules
        // 1. Every line must have the same currency as the first line.
        //    If not, throw InvalidJournalEntryException.
        // 2. Add up all DEBIT amounts and all CREDIT amounts separately.
        //    If they're not equal, throw InvalidJournalEntryException.
    }
}