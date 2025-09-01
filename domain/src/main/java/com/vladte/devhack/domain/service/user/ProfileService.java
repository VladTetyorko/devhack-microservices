package com.vladte.devhack.domain.service.user;

import com.vladte.devhack.domain.entities.user.Profile;
import com.vladte.devhack.domain.service.CrudService;

import java.util.Optional;
import java.util.UUID;

public interface ProfileService extends CrudService<Profile, UUID> {

    Optional<Profile> findByUserId(UUID userId);

    /**
     * Update basic profile fields and return the saved entity.
     */
    Profile updateProfile(UUID userId, String name, String cvFileHref, String cvFileName, String cvFileType);
}
