package com.vladte.devhack.domain.service.user.impl;

import com.vladte.devhack.domain.entities.user.Profile;
import com.vladte.devhack.domain.repository.user.ProfileRepository;
import com.vladte.devhack.domain.service.AuditableCrudService;
import com.vladte.devhack.domain.service.audit.AuditService;
import com.vladte.devhack.domain.service.user.ProfileService;
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
