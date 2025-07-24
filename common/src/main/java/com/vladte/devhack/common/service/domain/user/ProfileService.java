package com.vladte.devhack.common.service.domain.user;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.user.Profile;

import java.util.Optional;
import java.util.UUID;

public interface ProfileService extends CrudService<Profile, UUID> {

    Optional<Profile> findByUserId(UUID userId);

    /**
     * Update basic profile fields and return the saved entity.
     */
    Profile updateProfile(UUID userId, String name, String cvFileHref, String cvFileName, String cvFileType);
}
