package com.recon.ledger;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JournalEntryRulesTest {

    private final UUID receivable = UUID.randomUUID();
    private final UUID fees = UUID.randomUUID();
    private final UUID revenue = UUID.randomUUID();

    @Test
    void acceptsBalancedEntry() {
        // Step 1 of your exercise: $80 charge, $2.62 fee
        var lines = List.of(
                new JournalLineRequest(receivable, Direction.DEBIT, 7738, "USD"),
                new JournalLineRequest(fees, Direction.DEBIT, 262, "USD"),
                new JournalLineRequest(revenue, Direction.CREDIT, 8000, "USD"));

        assertThatCode(() -> JournalEntryRules.validate(lines)).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnbalancedEntry() {
        // Forgot the fee line: debits 7738, credits 8000
        var lines = List.of(
                new JournalLineRequest(receivable, Direction.DEBIT, 7738, "USD"),
                new JournalLineRequest(revenue, Direction.CREDIT, 8000, "USD"));

        assertThatThrownBy(() -> JournalEntryRules.validate(lines))
                .isInstanceOf(InvalidJournalEntryException.class);
    }

    @Test
    void rejectsSingleLine() {
        var lines = List.of(new JournalLineRequest(receivable, Direction.DEBIT, 100, "USD"));

        assertThatThrownBy(() -> JournalEntryRules.validate(lines))
                .isInstanceOf(InvalidJournalEntryException.class);
    }

    @Test
    void rejectsMixedCurrencies() {
        var lines = List.of(
                new JournalLineRequest(receivable, Direction.DEBIT, 100, "USD"),
                new JournalLineRequest(revenue, Direction.CREDIT, 100, "EUR"));

        assertThatThrownBy(() -> JournalEntryRules.validate(lines))
                .isInstanceOf(InvalidJournalEntryException.class);
    }

    @Test
    void rejectsZeroAmount() {
        assertThatThrownBy(() -> new JournalLineRequest(receivable, Direction.DEBIT, 0, "USD"))
                .isInstanceOf(InvalidJournalEntryException.class);
    }
}