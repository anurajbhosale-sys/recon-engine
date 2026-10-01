package com.recon.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface JournalLineRepository extends JpaRepository<JournalLine, UUID> {
    // CONCEPT: "all lines of one entry". Fast thanks to the entry_id index you added in V2.
    List<JournalLine> findByEntryId(UUID entryId);
}