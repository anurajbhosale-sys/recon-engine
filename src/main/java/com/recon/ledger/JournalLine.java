package com.recon.ledger;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;

@Entity
@Immutable // CONCEPT: lines are append-only too
@Table(name = "journal_lines")
public class JournalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entry_id", nullable = false)
    private UUID entryId;     // which entry this line belongs to (plain FK)

    @Column(name = "account_id", nullable = false)
    private UUID accountId;   // which bucket the money moves in or out of

    @Enumerated(EnumType.STRING) // CONCEPT: same Direction enum as your Day 1 rule
    @Column(nullable = false)
    private Direction direction;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor; // CONCEPT: primitive long can't be null, matching NOT NULL

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 3)
    private String currency;

    protected JournalLine() {}

    public JournalLine(UUID entryId, UUID accountId, Direction direction, long amountMinor, String currency) {
        this.entryId = entryId;
        this.accountId = accountId;
        this.direction = direction;
        this.amountMinor = amountMinor;
        this.currency = currency;
    }

    public UUID getId() { return id; }
    public UUID getEntryId() { return entryId; }
    public UUID getAccountId() { return accountId; }
    public Direction getDirection() { return direction; }
    public long getAmountMinor() { return amountMinor; }
    public String getCurrency() { return currency; }
}