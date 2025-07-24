package com.vladte.devhack.common.repository.user;

import com.vladte.devhack.entities.user.UserAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAccessRepository
        extends JpaRepository<UserAccess, UUID> {

    /**
     * Find the admin settings for a given user.
     */
    Optional<UserAccess> findByUserId(UUID userId);

    /**
     * Find all users having a given role (e.g. “ADMIN”).
     */
    List<UserAccess> findAllByRole(String role);
}
