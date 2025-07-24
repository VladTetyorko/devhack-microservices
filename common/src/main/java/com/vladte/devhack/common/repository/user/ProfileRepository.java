package com.vladte.devhack.common.repository.user;

import com.vladte.devhack.entities.user.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfileRepository
        extends JpaRepository<Profile, UUID> {

    /**
     * Lookup the profile by its owning user.
     */
    Optional<Profile> findByUserId(UUID userId);

    /**
     * (Optional) lookup by display name.
     */
    Optional<Profile> findByName(String name);
}
