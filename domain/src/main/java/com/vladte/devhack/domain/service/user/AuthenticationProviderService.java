// AuthenticationProviderService.java
package com.vladte.devhack.domain.service.user;

import com.vladte.devhack.domain.entities.enums.AuthProviderType;
import com.vladte.devhack.domain.entities.user.AuthenticationProvider;
import com.vladte.devhack.domain.service.CrudService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuthenticationProviderService extends CrudService<AuthenticationProvider, UUID> {

    Optional<AuthenticationProvider> findByProviderAndEmail(AuthProviderType provider, String email);

    Optional<AuthenticationProvider> findByProviderAndProviderUserId(AuthProviderType provider, String providerUserId);

    List<AuthenticationProvider> findAllByUserId(UUID userId);

    /**
     * Register a new LOCAL authentication provider (email/password).
     */
    AuthenticationProvider registerLocal(String email, String rawPassword);

}