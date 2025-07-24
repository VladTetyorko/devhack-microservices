package com.vladte.devhack.common.repository.user;

import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthenticationProviderRepository
        extends JpaRepository<AuthenticationProvider, UUID> {

    /**
     * Find the (LOCAL) credentials by email.
     */
    @Query("SELECT a FROM AuthenticationProvider a JOIN User u on a.user = u WHERE a.provider = :provider and a.email = :email")
    Optional<AuthenticationProvider> findByProviderAndEmail(AuthProviderType provider, String email);

    /**
     * Find a social login record by provider and providerUserId.
     */
    Optional<AuthenticationProvider> findByProviderAndProviderUserId(AuthProviderType provider, String providerUserId);

    /**
     * Get all auth records (LOCAL + SOCIAL) for a given user.
     */
    List<AuthenticationProvider> findAllByUserId(UUID userId);
}
