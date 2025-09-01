package com.vladte.devhack.domain.service.user.impl;

import com.vladte.devhack.domain.entities.enums.AuthProviderType;
import com.vladte.devhack.domain.entities.user.AuthenticationProvider;
import com.vladte.devhack.domain.repository.user.AuthenticationProviderRepository;
import com.vladte.devhack.domain.service.AuditableCrudService;
import com.vladte.devhack.domain.service.audit.AuditService;
import com.vladte.devhack.domain.service.user.AuthenticationProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationProviderServiceImpl
        extends AuditableCrudService<AuthenticationProvider, UUID, AuthenticationProviderRepository>
        implements AuthenticationProviderService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationProviderServiceImpl.class);
    private final PasswordEncoder passwordEncoder;

    public AuthenticationProviderServiceImpl(
            AuthenticationProviderRepository repository,
            AuditService auditService,
            PasswordEncoder passwordEncoder
    ) {
        super(repository, auditService);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<AuthenticationProvider> findByProviderAndEmail(AuthProviderType provider, String email) {
        log.debug("Looking up {} auth by email {}", provider, email);
        return repository.findByProviderAndEmail(provider, email);
    }

    @Override
    public Optional<AuthenticationProvider> findByProviderAndProviderUserId(AuthProviderType provider, String providerUserId) {
        log.debug("Looking up {} auth by providerUserId {}", provider, providerUserId);
        return repository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public List<AuthenticationProvider> findAllByUserId(UUID userId) {
        log.debug("Fetching all auth providers for user {}", userId);
        return repository.findAllByUserId(userId);
    }

    @Override
    @Transactional
    public AuthenticationProvider registerLocal(String email, String rawPassword) {
        log.debug("Registering LOCAL auth for email {}", email);
        AuthenticationProvider cred = new AuthenticationProvider();
        cred.setProvider(AuthProviderType.LOCAL);
        cred.setEmail(email);
        cred.setPasswordHash(passwordEncoder.encode(rawPassword));
        return save(cred);
    }
}
