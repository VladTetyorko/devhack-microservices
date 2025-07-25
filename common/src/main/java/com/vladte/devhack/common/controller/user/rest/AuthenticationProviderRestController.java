package com.vladte.devhack.common.controller.user.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.user.AuthenticationProviderDTO;
import com.vladte.devhack.common.model.mapper.user.AuthenticationProviderMapper;
import com.vladte.devhack.common.service.domain.user.AuthenticationProviderService;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import com.vladte.devhack.entities.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing AuthenticationProvider entities.
 * Provides RESTful API endpoints for CRUD operations on authentication providers.
 */
@RestController
@RequestMapping("/api/auth-providers")
@Tag(name = "Authentication Provider", description = "Authentication provider management API")
@Slf4j
public class AuthenticationProviderRestController extends BaseRestController<AuthenticationProvider, AuthenticationProviderDTO, UUID, AuthenticationProviderService, AuthenticationProviderMapper> {

    /**
     * Constructor with service and mapper injection.
     *
     * @param authProviderService the authentication provider service
     * @param authProviderMapper  the authentication provider mapper
     */
    public AuthenticationProviderRestController(AuthenticationProviderService authProviderService,
                                                AuthenticationProviderMapper authProviderMapper) {
        super(authProviderService, authProviderMapper);
    }

    /**
     * Get all authentication providers for the authenticated user.
     *
     * @param user the authenticated user
     * @return a list of authentication providers
     */
    @GetMapping("/my-providers")
    @Operation(summary = "Get all authentication providers for the authenticated user",
            description = "Returns a list of authentication providers for the currently authenticated user")
    public ResponseEntity<List<AuthenticationProviderDTO>> getMyAuthProviders(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to get authentication providers for user: {}", user.getId());

        List<AuthenticationProvider> providers = service.findAllByUserId(user.getId());
        List<AuthenticationProviderDTO> dtoList = mapper.toDTOList(providers);
        return ResponseEntity.ok(dtoList);
    }

    /**
     * Get all authentication providers for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of authentication providers
     */
    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get all authentication providers for a specific user",
            description = "Returns a list of authentication providers for the specified user")
    public ResponseEntity<List<AuthenticationProviderDTO>> getAuthProvidersByUserId(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId) {
        log.debug("REST request to get authentication providers for user: {}", userId);

        List<AuthenticationProvider> providers = service.findAllByUserId(userId);
        List<AuthenticationProviderDTO> dtoList = mapper.toDTOList(providers);
        return ResponseEntity.ok(dtoList);
    }

    /**
     * Find an authentication provider by provider type and email.
     *
     * @param provider the provider type
     * @param email    the email address
     * @return the authentication provider if found
     */
    @GetMapping("/by-provider-email")
    @Operation(summary = "Find authentication provider by provider type and email",
            description = "Returns an authentication provider for the specified provider type and email")
    public ResponseEntity<AuthenticationProviderDTO> getAuthProviderByProviderAndEmail(
            @Parameter(description = "Provider type")
            @RequestParam AuthProviderType provider,
            @Parameter(description = "Email address")
            @RequestParam String email) {
        log.debug("REST request to get authentication provider for provider: {} and email: {}", provider, email);

        Optional<AuthenticationProvider> authProvider = service.findByProviderAndEmail(provider, email);
        return authProvider.map(p -> ResponseEntity.ok(mapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Find an authentication provider by provider type and provider user ID.
     *
     * @param provider       the provider type
     * @param providerUserId the provider-specific user ID
     * @return the authentication provider if found
     */
    @GetMapping("/by-provider-userid")
    @Operation(summary = "Find authentication provider by provider type and provider user ID",
            description = "Returns an authentication provider for the specified provider type and provider user ID")
    public ResponseEntity<AuthenticationProviderDTO> getAuthProviderByProviderAndProviderUserId(
            @Parameter(description = "Provider type")
            @RequestParam AuthProviderType provider,
            @Parameter(description = "Provider-specific user ID")
            @RequestParam String providerUserId) {
        log.debug("REST request to get authentication provider for provider: {} and providerUserId: {}", provider, providerUserId);

        Optional<AuthenticationProvider> authProvider = service.findByProviderAndProviderUserId(provider, providerUserId);
        return authProvider.map(p -> ResponseEntity.ok(mapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Register a new local authentication provider.
     *
     * @param email       the email address
     * @param rawPassword the raw password
     * @return the created authentication provider
     */
    @PostMapping("/register-local")
    @Operation(summary = "Register a new local authentication provider",
            description = "Creates a new local authentication provider with email and password")
    public ResponseEntity<AuthenticationProviderDTO> registerLocalAuthProvider(
            @Parameter(description = "Email address")
            @RequestParam String email,
            @Parameter(description = "Raw password")
            @RequestParam String rawPassword) {
        log.debug("REST request to register local authentication provider for email: {}", email);

        AuthenticationProvider authProvider = service.registerLocal(email, rawPassword);
        return ResponseEntity.ok(mapper.toDTO(authProvider));
    }

    /**
     * Update token information for an authentication provider.
     *
     * @param id           the ID of the authentication provider
     * @param accessToken  the new access token
     * @param refreshToken the new refresh token
     * @return the updated authentication provider
     */
    @PutMapping("/{id}/tokens")
    @Operation(summary = "Update token information for an authentication provider",
            description = "Updates access and refresh tokens for the specified authentication provider")
    public ResponseEntity<AuthenticationProviderDTO> updateTokens(
            @Parameter(description = "ID of the authentication provider")
            @PathVariable UUID id,
            @Parameter(description = "New access token")
            @RequestParam(required = false) String accessToken,
            @Parameter(description = "New refresh token")
            @RequestParam(required = false) String refreshToken) {
        log.debug("REST request to update tokens for authentication provider: {}", id);

        Optional<AuthenticationProvider> authProviderOpt = service.findById(id);
        if (authProviderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AuthenticationProvider authProvider = authProviderOpt.get();
        if (accessToken != null) {
            authProvider.setAccessToken(accessToken);
        }
        if (refreshToken != null) {
            authProvider.setRefreshToken(refreshToken);
        }

        AuthenticationProvider savedAuthProvider = service.save(authProvider);
        return ResponseEntity.ok(mapper.toDTO(savedAuthProvider));
    }
}