package com.vladte.devhack.common.service.domain.user.impl;

import com.vladte.devhack.common.repository.user.ProfileRepository;
import com.vladte.devhack.common.service.domain.AuditableCrudService;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.user.ProfileService;
import com.vladte.devhack.entities.user.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileServiceImpl
        extends AuditableCrudService<Profile, UUID, ProfileRepository>
        implements ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileServiceImpl.class);

    public ProfileServiceImpl(
            ProfileRepository repository,
            AuditService auditService
    ) {
        super(repository, auditService);
    }

    @Override
    public Optional<Profile> findByUserId(UUID userId) {
        log.debug("Fetching profile for user {}", userId);
        return repository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Profile updateProfile(UUID userId, String name, String cvFileHref, String cvFileName, String cvFileType) {
        Profile profile = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user " + userId));

        profile.setName(name);
        profile.setCvFileHref(cvFileHref);
        profile.setCvFileName(cvFileName);
        profile.setCvFileType(cvFileType);

        return save(profile);
    }
}
