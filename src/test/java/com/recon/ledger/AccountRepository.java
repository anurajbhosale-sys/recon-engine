package com.recon.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    // CONCEPT: Spring reads the method name and writes the SQL:
    // SELECT * FROM accounts WHERE tenant_id = ? AND code = ?
    // Optional = "maybe found, maybe not", which forces you to handle "not found".
    Optional<Account> findByTenantIdAndCode(UUID tenantId, String code);
}