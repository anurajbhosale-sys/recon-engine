package com.recon.ledger;

import com.recon.TestcontainersConfiguration;
import com.recon.tenancy.Tenant;
import com.recon.tenancy.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest                                 // CONCEPT: boots the whole app
@Import(TestcontainersConfiguration.class)      // CONCEPT: ...against a throwaway Postgres 16
class LedgerServiceIntegrationTest {

    @Autowired LedgerService ledgerService;
    @Autowired TenantRepository tenantRepository;
    @Autowired AccountRepository accountRepository;
    @Autowired JournalEntryRepository entryRepository;
    @Autowired JournalLineRepository lineRepository;

    UUID tenantId;
    Account receivable;
    Account fees;
    Account revenue;

    @BeforeEach // CONCEPT: runs before EACH test, so every test gets its own fresh tenant
    void setUp() {
        tenantId = tenantRepository.save(new Tenant("Chai Co")).getId();
        receivable = accountRepository.save(new Account(tenantId, "processor_receivable", "Stripe receivable", AccountType.ASSET, "USD"));
        fees = accountRepository.save(new Account(tenantId, "processing_fees", "Processing fees", AccountType.EXPENSE, "USD"));
        revenue = accountRepository.save(new Account(tenantId, "revenue", "Revenue", AccountType.REVENUE, "USD"));
    }

    // Your exercise step 1: $80 charge with a $2.62 fee
    private PostEntryCommand charge(String idempotencyKey) {
        return new PostEntryCommand(tenantId, LocalDate.of(2026, 9, 29), "Order 1001", idempotencyKey, List.of(
                new JournalLineRequest(receivable.getId(), Direction.DEBIT, 7738, "USD"),
                new JournalLineRequest(fees.getId(), Direction.DEBIT, 262, "USD"),
                new JournalLineRequest(revenue.getId(), Direction.CREDIT, 8000, "USD")));
    }

    private long entryCountForTenant() {
        return entryRepository.findAll().stream()
                .filter(e -> e.getTenantId().equals(tenantId))
                .count();
    }

    @Test
    void postsBalancedEntry() {
        var result = ledgerService.postEntry(charge("charge-1001"));

        assertThat(result.created()).isTrue();
        assertThat(lineRepository.findByEntryId(result.entryId())).hasSize(3);
    }

    @Test
    void sameKeyTwiceCreatesOneEntry() {
        var first = ledgerService.postEntry(charge("charge-1001"));
        var second = ledgerService.postEntry(charge("charge-1001")); // the "retry"

        assertThat(second.created()).isFalse();                      // recognized as a duplicate
        assertThat(second.entryId()).isEqualTo(first.entryId());     // same entry returned
        assertThat(entryCountForTenant()).isEqualTo(1);              // only ONE entry exists
        assertThat(lineRepository.findByEntryId(first.entryId())).hasSize(3); // not 6 lines
    }

    @Test
    void rejectsOtherTenantsAccount() {
        // A second company with its own receivable account
        UUID otherTenant = tenantRepository.save(new Tenant("Book Nook")).getId();
        Account foreign = accountRepository.save(new Account(otherTenant, "processor_receivable", "Their receivable", AccountType.ASSET, "USD"));

        // Chai Co tries to post into Book Nook's account
        var command = new PostEntryCommand(tenantId, LocalDate.of(2026, 9, 29), "Sneaky", "sneaky-1", List.of(
                new JournalLineRequest(foreign.getId(), Direction.DEBIT, 8000, "USD"),
                new JournalLineRequest(revenue.getId(), Direction.CREDIT, 8000, "USD")));

        assertThatThrownBy(() -> ledgerService.postEntry(command))
                .isInstanceOf(InvalidJournalEntryException.class);
        assertThat(entryCountForTenant()).isZero();                 // nothing was saved
    }
}