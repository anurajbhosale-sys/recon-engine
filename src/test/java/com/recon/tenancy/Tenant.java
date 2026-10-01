package com.recon.tenancy;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity                    // CONCEPT: "this class maps to a table"
@Table(name = "tenants")   // CONCEPT: ...specifically this one
public class Tenant {

    @Id                                              // CONCEPT: the primary key
    @GeneratedValue(strategy = GenerationType.UUID)  // CONCEPT: Hibernate creates the UUID when saving
    private UUID id;

    @Column(nullable = false)
    private String name;

    // CONCEPT: the DB fills this with DEFAULT now(); Hibernate never writes it
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Tenant() {} // CONCEPT: Hibernate needs an empty constructor to build objects from rows

    public Tenant(String name) {
        this.name = name;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
}