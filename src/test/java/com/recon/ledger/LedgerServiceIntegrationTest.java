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
import java.util.concurrent.*;

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

    @Test
    void concurrentSameKeyCreatesOneEntry() throws Exception {
        ExecutorService workers = Executors.newFixedThreadPool(2);   // CONCEPT: 2 workers running in parallel
        try {
            for (int round = 0; round < 10; round++) {
                String key = "race-" + round;
                CountDownLatch startingGun = new CountDownLatch(1);  // CONCEPT: both threads wait for the gun

                Callable<PostEntryResult> task = () -> {
                    startingGun.await();                             // wait at the line...
                    return ledgerService.postEntry(charge(key));     // ...then post the SAME key
                };

                Future<PostEntryResult> a = workers.submit(task);
                Future<PostEntryResult> b = workers.submit(task);
                startingGun.countDown();                             // 🔫 both go at once

                PostEntryResult resultA = a.get();                   // CONCEPT: .get() rethrows if that thread crashed
                PostEntryResult resultB = b.get();

                assertThat(resultA.entryId()).isEqualTo(resultB.entryId());   // both point to ONE entry
                assertThat(resultA.created() ^ resultB.created()).isTrue();   // exactly one says "I created it"
            }
            assertThat(entryCountForTenant()).isEqualTo(10);         // 10 rounds → exactly 10 entries
        } finally {
            workers.shutdownNow();                                   // always clean up the threads
        }
    }

    @Test
    void unbalancedEntrySavesNothing() {
        // The "forgot the fee line" mistake: debits 7738, credits 8000
        var command = new PostEntryCommand(tenantId, LocalDate.of(2026, 9, 29), "Unbalanced", "unbalanced-1", List.of(
                new JournalLineRequest(receivable.getId(), Direction.DEBIT, 7738, "USD"),
                new JournalLineRequest(revenue.getId(), Direction.CREDIT, 8000, "USD")));

        assertThatThrownBy(() -> ledgerService.postEntry(command))
                .isInstanceOf(InvalidJournalEntryException.class);
        assertThat(entryCountForTenant()).isZero();
    }

    @Test
    void rejectsUnknownAccount() {
        UUID madeUp = UUID.randomUUID(); // no such account exists
        var command = new PostEntryCommand(tenantId, LocalDate.of(2026, 9, 29), "Ghost account", "ghost-1", List.of(
                new JournalLineRequest(madeUp, Direction.DEBIT, 8000, "USD"),
                new JournalLineRequest(revenue.getId(), Direction.CREDIT, 8000, "USD")));

        assertThatThrownBy(() -> ledgerService.postEntry(command))
                .isInstanceOf(InvalidJournalEntryException.class)
                .hasMessageContaining("Unknown account");
        assertThat(entryCountForTenant()).isZero();
    }

    @Test
    void rejectsCurrencyMismatch() {
        // Both lines are EUR, so the entry itself is consistent and balanced...
        // ...but the accounts are USD accounts.
        var command = new PostEntryCommand(tenantId, LocalDate.of(2026, 9, 29), "Wrong currency", "eur-1", List.of(
                new JournalLineRequest(receivable.getId(), Direction.DEBIT, 8000, "EUR"),
                new JournalLineRequest(revenue.getId(), Direction.CREDIT, 8000, "EUR")));

        assertThatThrownBy(() -> ledgerService.postEntry(command))
                .isInstanceOf(InvalidJournalEntryException.class)
                .hasMessageContaining("does not match account currency");
        assertThat(entryCountForTenant()).isZero();
    }


}