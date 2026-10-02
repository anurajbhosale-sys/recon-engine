package com.recon.ledger;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LedgerService {

    private final JournalEntryRepository entryRepository;
    private final JournalLineRepository lineRepository;
    private final AccountRepository accountRepository;
    private final TransactionTemplate transactionTemplate;

    public LedgerService(JournalEntryRepository entryRepository,
                         JournalLineRepository lineRepository,
                         AccountRepository accountRepository,
                         PlatformTransactionManager transactionManager) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.accountRepository = accountRepository;
        // CONCEPT: TransactionTemplate = start/commit a transaction in code, not via annotation
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    // CONCEPT: no @Transactional here on purpose. The transaction lives INSIDE execute(),
    // so if it fails, we're back OUTSIDE it and can recover in a fresh one.
    public PostEntryResult postEntry(PostEntryCommand command) {
        try {
            return transactionTemplate.execute(status -> postInTransaction(command));
        } catch (DataIntegrityViolationException e) {
            // We lost a race: another request with the same key committed first.
            // CONCEPT: this lookup runs in a NEW transaction; the failed one is gone.
            return entryRepository
                    .findByTenantIdAndIdempotencyKey(command.tenantId(), command.idempotencyKey())
                    .map(existing -> new PostEntryResult(existing.getId(), false))
                    // CONCEPT: if no entry exists, the violation was something ELSE → don't hide it
                    .orElseThrow(() -> e);
        }
    }

    @Transactional(readOnly = true) // CONCEPT: a read-only transaction. Safe here: it's called from the controller, another class.
    public AccountBalance getBalance(UUID tenantId, UUID accountId) {
        // Same rule as posting: another tenant's account looks exactly like a missing one
        Account account = accountRepository.findById(accountId)
                .filter(a -> a.getTenantId().equals(tenantId))
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        long net = lineRepository.netDebitMinusCredit(accountId);           // debits − credits
        long balance = account.getType().isDebitNormal() ? net : -net;     // flip for credit-normal

        return new AccountBalance(account.getId(), account.getCode(), account.getCurrency(), balance);
    }

    private PostEntryResult postInTransaction(PostEntryCommand command) {
        // 1. Idempotency: check if this entry was already posted
        var existingEntry = entryRepository.findByTenantIdAndIdempotencyKey(
                command.tenantId(),
                command.idempotencyKey());

        if (existingEntry.isPresent()) {
            return new PostEntryResult(existingEntry.get().getId(), false);
        }

        // 2. Validate debit/credit balance rules
        JournalEntryRules.validate(command.lines());

        // 3. Validate accounts: exist, belong to this tenant, currency matches
        checkAccounts(command);

        // 4. Create and save the journal entry
        JournalEntry savedEntry = entryRepository.save(new JournalEntry(
                command.tenantId(),
                command.effectiveDate(),
                command.description(),
                command.idempotencyKey(),
                null));

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

        Map<UUID, Account> accounts = accountRepository.findAllById(accountIds).stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));

        for (JournalLineRequest line : command.lines()) {
            Account account = accounts.get(line.accountId());

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