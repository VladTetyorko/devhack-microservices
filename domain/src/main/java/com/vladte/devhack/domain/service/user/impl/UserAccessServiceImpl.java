package com.vladte.devhack.domain.service.user.impl;

import com.vladte.devhack.domain.entities.user.UserAccess;
import com.vladte.devhack.domain.repository.user.UserAccessRepository;
import com.vladte.devhack.domain.service.AuditableCrudService;
import com.vladte.devhack.domain.service.audit.AuditService;
import com.vladte.devhack.domain.service.user.UserAccessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserAccessServiceImpl
        extends AuditableCrudService<UserAccess, UUID, UserAccessRepository>
        implements UserAccessService {

    private static final Logger log = LoggerFactory.getLogger(UserAccessServiceImpl.class);

    public UserAccessServiceImpl(
            UserAccessRepository repository,
            AuditService auditService
    ) {
        super(repository, auditService);
    }

    @Override
    public Optional<UserAccess> findByUserId(UUID userId) {
        log.debug("Fetching admin settings for user {}", userId);
        return repository.findByUserId(userId);
    }

    @Override
    public List<UserAccess> findAllByRole(String role) {
        log.debug("Listing all users with role {}", role);
        return repository.findAllByRole(role);
    }
}