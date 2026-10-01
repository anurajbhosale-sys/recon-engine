package com.recon.tenancy;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

// CONCEPT: <Tenant, UUID> = "manages Tenant objects whose ID is a UUID".
// Spring generates save(), findById(), findAll(), delete()... at runtime. You write no SQL.
public interface TenantRepository extends JpaRepository<Tenant, UUID> {}