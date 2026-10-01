package com.recon.ledger;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId; // CONCEPT: plain FK, not a @ManyToOne object (no hidden queries)

    @Column(nullable = false)
    private String code;   // e.g. 'processor_receivable'

    @Column(nullable = false)
    private String name;   // e.g. 'Stripe receivable'

    @Enumerated(EnumType.STRING) // CONCEPT: store 'ASSET', not 0
    @Column(nullable = false)
    private AccountType type;

    @JdbcTypeCode(SqlTypes.CHAR) // CONCEPT: matches CHAR(3); without it, schema validation fails
    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Account() {}

    public Account(UUID tenantId, String code, String name, AccountType type, String currency) {
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.type = type;
        this.currency = currency;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public AccountType getType() { return type; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
}