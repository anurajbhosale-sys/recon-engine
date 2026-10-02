package com.recon.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {

    List<JournalLine> findByEntryId(UUID entryId);

    // CONCEPT: the database adds up every line for this account (debit = +, credit = −).
    // COALESCE(..., 0) → an account with no lines returns 0, not NULL.
    // CAST(... AS BIGINT) → Postgres SUM of BIGINT returns NUMERIC; cast back so Java gets a long.
    @Query(value = """
            SELECT CAST(COALESCE(SUM(
                       CASE WHEN direction = 'DEBIT' THEN amount_minor ELSE -amount_minor END
                   ), 0) AS BIGINT)
            FROM journal_lines
            WHERE account_id = :accountId
            """, nativeQuery = true)
    long netDebitMinusCredit(@Param("accountId") UUID accountId);
}