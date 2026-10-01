package com.recon.ledger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    private final JournalEntryRepository entryRepository;
    private final JournalLineRepository lineRepository;
    private final AccountRepository accountRepository;

    // CONCEPT: constructor injection. Spring passes in the repositories automatically.
    public LedgerService(JournalEntryRepository entryRepository,
                         JournalLineRepository lineRepository,
                         AccountRepository accountRepository) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional // CONCEPT: all-or-nothing. Any exception below rolls back every insert.
    public PostEntryResult postEntry(PostEntryCommand command) {

        // 1. Idempotency: check if this entry was already posted
        var existingEntry = entryRepository.findByTenantIdAndIdempotencyKey(
                command.tenantId(),
                command.idempotencyKey());

        if (existingEntry.isPresent()) {
            return new PostEntryResult(existingEntry.get().getId(), false);
        }

        // 2. Validate debit/credit balance rules (your Day 1 code)
        JournalEntryRules.validate(command.lines());

        // 3. Validate accounts: exist, belong to this tenant, currency matches
        checkAccounts(command);

        // 4. Create and save the journal entry (complete, via constructor; no setters)
        JournalEntry savedEntry = entryRepository.save(new JournalEntry(
                command.tenantId(),
                command.effectiveDate(),
                command.description(),
                command.idempotencyKey(),
                null));                   // reversesEntryId: null for a normal entry

        // 5. Build the lines, each pointing at the saved entry's id
        var journalLines = command.lines().stream()
                .map(line -> new JournalLine(
                        savedEntry.getId(),
                        line.accountId(),
                        line.direction(),
                        line.amountMinor(),
                        line.currency()))
                .toList();

        // 6. Save all lines in one call
        lineRepository.saveAll(journalLines);

        // 7. Report: a new entry was created
        return new PostEntryResult(savedEntry.getId(), true);
    }

    private void checkAccounts(PostEntryCommand command) {
        var accountIds = command.lines().stream()
                .map(JournalLineRequest::accountId)
                .collect(Collectors.toSet());

        // CONCEPT: ONE query for all accounts, not one query per line (avoids N+1)
        Map<UUID, Account> accounts = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        for (JournalLineRequest line : command.lines()) {
            Account account = accounts.get(line.accountId());

            // CONCEPT: another tenant's account gets the SAME error as a missing one.
            // Saying "that account belongs to someone else" would leak that it exists.
            if (account == null || !account.getTenantId().equals(command.tenantId())) {
                throw new InvalidJournalEntryException("Unknown account: " + line.accountId());
            }
            if (!account.getCurrency().equals(line.currency())) {
                throw new InvalidJournalEntryException(
                        "Line currency " + line.currency()
                                + " does not match account currency " + account.getCurrency());
            }
        }
    }
}