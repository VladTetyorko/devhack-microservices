package com.vladte.devhack.common.controller.user.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.user.UserAccessDTO;
import com.vladte.devhack.common.model.mapper.user.UserAccessMapper;
import com.vladte.devhack.common.service.domain.user.UserAccessService;
import com.vladte.devhack.entities.user.User;
import com.vladte.devhack.entities.user.UserAccess;
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
 * REST controller for managing UserAccess entities.
 * Provides RESTful API endpoints for CRUD operations on user access and permissions.
 */
@RestController
@RequestMapping("/api/user-access")
@Tag(name = "User Access", description = "User access and permissions management API")
@Slf4j
public class UserAccessRestController extends BaseRestController<UserAccess, UserAccessDTO, UUID, UserAccessService, UserAccessMapper> {

    /**
     * Constructor with service and mapper injection.
     *
     * @param userAccessService the user access service
     * @param userAccessMapper  the user access mapper
     */
    public UserAccessRestController(UserAccessService userAccessService, UserAccessMapper userAccessMapper) {
        super(userAccessService, userAccessMapper);
    }

    /**
     * Get the user access information for the authenticated user.
     *
     * @param user the authenticated user
     * @return the user's access information
     */
    @GetMapping("/my-access")
    @Operation(summary = "Get the user access information for the authenticated user",
            description = "Returns the access information for the currently authenticated user")
    public ResponseEntity<UserAccessDTO> getMyUserAccess(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to get user access for user: {}", user.getId());

        Optional<UserAccess> userAccess = service.findByUserId(user.getId());
        return userAccess.map(ua -> ResponseEntity.ok(mapper.toDTO(ua)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get user access information by user ID.
     *
     * @param userId the ID of the user
     * @return the user's access information
     */
    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get user access information by user ID",
            description = "Returns the access information for the specified user ID")
    public ResponseEntity<UserAccessDTO> getUserAccessByUserId(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId) {
        log.debug("REST request to get user access for user: {}", userId);

        Optional<UserAccess> userAccess = service.findByUserId(userId);
        return userAccess.map(ua -> ResponseEntity.ok(mapper.toDTO(ua)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all users with a specific role.
     *
     * @param role the role to filter by
     * @return a list of user access records with the specified role
     */
    @GetMapping("/by-role/{role}")
    @Operation(summary = "Get all users with a specific role",
            description = "Returns a list of user access records for users with the specified role")
    public ResponseEntity<List<UserAccessDTO>> getUserAccessByRole(
            @Parameter(description = "Role name")
            @PathVariable String role) {
        log.debug("REST request to get user access for role: {}", role);

        List<UserAccess> userAccessList = service.findAllByRole(role);
        List<UserAccessDTO> dtoList = mapper.toDTOList(userAccessList);
        return ResponseEntity.ok(dtoList);
    }

    /**
     * Update the role for a user.
     *
     * @param userId the ID of the user
     * @param role   the new role
     * @return the updated user access information
     */
    @PutMapping("/user/{userId}/role")
    @Operation(summary = "Update the role for a user",
            description = "Updates the role for the specified user")
    public ResponseEntity<UserAccessDTO> updateUserRole(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId,
            @Parameter(description = "New role")
            @RequestParam String role) {
        log.debug("REST request to update role for user: {} to role: {}", userId, role);

        Optional<UserAccess> userAccessOpt = service.findByUserId(userId);
        if (userAccessOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserAccess userAccess = userAccessOpt.get();
        userAccess.setRole(role);

        UserAccess savedUserAccess = service.save(userAccess);
        return ResponseEntity.ok(mapper.toDTO(savedUserAccess));
    }

    /**
     * Update AI usage permission for a user.
     *
     * @param userId         the ID of the user
     * @param aiUsageAllowed whether AI usage is allowed
     * @return the updated user access information
     */
    @PutMapping("/user/{userId}/ai-usage")
    @Operation(summary = "Update AI usage permission for a user",
            description = "Updates AI usage permission for the specified user")
    public ResponseEntity<UserAccessDTO> updateUserAiUsage(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId,
            @Parameter(description = "AI usage allowed")
            @RequestParam Boolean aiUsageAllowed) {
        log.debug("REST request to update AI usage for user: {} to: {}", userId, aiUsageAllowed);

        Optional<UserAccess> userAccessOpt = service.findByUserId(userId);
        if (userAccessOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserAccess userAccess = userAccessOpt.get();
        userAccess.setIsAiUsageAllowed(aiUsageAllowed);

        UserAccess savedUserAccess = service.save(userAccess);
        return ResponseEntity.ok(mapper.toDTO(savedUserAccess));
    }

    /**
     * Lock or unlock a user account.
     *
     * @param userId        the ID of the user
     * @param accountLocked whether the account should be locked
     * @return the updated user access information
     */
    @PutMapping("/user/{userId}/lock-status")
    @Operation(summary = "Lock or unlock a user account",
            description = "Updates the lock status for the specified user account")
    public ResponseEntity<UserAccessDTO> updateUserLockStatus(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId,
            @Parameter(description = "Account locked status")
            @RequestParam Boolean accountLocked) {
        log.debug("REST request to update lock status for user: {} to: {}", userId, accountLocked);

        Optional<UserAccess> userAccessOpt = service.findByUserId(userId);
        if (userAccessOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserAccess userAccess = userAccessOpt.get();
        userAccess.setIsAccountLocked(accountLocked);

        UserAccess savedUserAccess = service.save(userAccess);
        return ResponseEntity.ok(mapper.toDTO(savedUserAccess));
    }

    /**
     * Update multiple access settings for a user.
     *
     * @param userId         the ID of the user
     * @param role           the new role (optional)
     * @param aiUsageAllowed whether AI usage is allowed (optional)
     * @param accountLocked  whether the account should be locked (optional)
     * @return the updated user access information
     */
    @PutMapping("/user/{userId}/settings")
    @Operation(summary = "Update multiple access settings for a user",
            description = "Updates multiple access settings for the specified user")
    public ResponseEntity<UserAccessDTO> updateUserAccessSettings(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId,
            @Parameter(description = "New role")
            @RequestParam(required = false) String role,
            @Parameter(description = "AI usage allowed")
            @RequestParam(required = false) Boolean aiUsageAllowed,
            @Parameter(description = "Account locked status")
            @RequestParam(required = false) Boolean accountLocked) {
        log.debug("REST request to update access settings for user: {}", userId);

        Optional<UserAccess> userAccessOpt = service.findByUserId(userId);
        if (userAccessOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        UserAccess userAccess = userAccessOpt.get();

        if (role != null) {
            userAccess.setRole(role);
        }
        if (aiUsageAllowed != null) {
            userAccess.setIsAiUsageAllowed(aiUsageAllowed);
        }
        if (accountLocked != null) {
            userAccess.setIsAccountLocked(accountLocked);
        }

        UserAccess savedUserAccess = service.save(userAccess);
        return ResponseEntity.ok(mapper.toDTO(savedUserAccess));
    }
}
