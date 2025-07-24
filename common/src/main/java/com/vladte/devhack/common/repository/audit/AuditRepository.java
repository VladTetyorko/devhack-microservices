package com.vladte.devhack.common.repository.audit;

import com.vladte.devhack.entities.global.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for accessing and manipulating Audit entities.
 */
@Repository
public interface AuditRepository extends JpaRepository<Audit, UUID> {
    // No additional methods needed beyond those in JpaRepository
}
