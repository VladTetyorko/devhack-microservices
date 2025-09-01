package com.vladte.devhack.common.controller.user.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.domain.entities.user.Profile;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.model.dto.user.ProfileDTO;
import com.vladte.devhack.domain.model.mapper.user.ProfileMapper;
import com.vladte.devhack.domain.service.user.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing Profile entities.
 * Provides RESTful API endpoints for CRUD operations on user profiles.
 */
@RestController
@RequestMapping("/api/profiles")
@Tag(name = "Profile", description = "Profile management API")
@Slf4j
public class ProfileRestController extends BaseRestController<Profile, ProfileDTO, UUID, ProfileService, ProfileMapper> {

    /**
     * Constructor with service and mapper injection.
     *
     * @param profileService the profile service
     * @param profileMapper  the profile mapper
     */
    public ProfileRestController(ProfileService profileService, ProfileMapper profileMapper) {
        super(profileService, profileMapper);
    }

    /**
     * Get the profile of the authenticated user.
     *
     * @param user the authenticated user
     * @return the user's profile
     */
    @GetMapping("/my-profile")
    @Operation(summary = "Get the profile of the authenticated user",
            description = "Returns the profile of the currently authenticated user")
    public ResponseEntity<ProfileDTO> getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to get profile for user: {}", user.getId());

        Optional<Profile> profile = relatedEntityService.findByUserId(user.getId());
        return profile.map(p -> ResponseEntity.ok(relatedEntityMapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a profile by user ID.
     *
     * @param userId the ID of the user
     * @return the user's profile
     */
    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get a profile by user ID",
            description = "Returns the profile for the specified user ID")
    public ResponseEntity<ProfileDTO> getProfileByUserId(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId) {
        log.debug("REST request to get profile for user: {}", userId);

        Optional<Profile> profile = relatedEntityService.findByUserId(userId);
        return profile.map(p -> ResponseEntity.ok(relatedEntityMapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update the profile of the authenticated user.
     *
     * @param user       the authenticated user
     * @param name       the new name
     * @param cvFileHref the CV file URL
     * @param cvFileName the CV file name
     * @param cvFileType the CV file type
     * @return the updated profile
     */
    @PutMapping("/my-profile")
    @Operation(summary = "Update the profile of the authenticated user",
            description = "Updates the profile of the currently authenticated user")
    public ResponseEntity<ProfileDTO> updateMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @Parameter(description = "New display name")
            @RequestParam(required = false) String name,
            @Parameter(description = "CV file URL")
            @RequestParam(required = false) String cvFileHref,
            @Parameter(description = "CV file name")
            @RequestParam(required = false) String cvFileName,
            @Parameter(description = "CV file type")
            @RequestParam(required = false) String cvFileType) {
        log.debug("REST request to update profile for user: {}", user.getId());

        Profile updatedProfile = relatedEntityService.updateProfile(user.getId(), name, cvFileHref, cvFileName, cvFileType);
        return ResponseEntity.ok(relatedEntityMapper.toDTO(updatedProfile));
    }

    /**
     * Update AI usage settings for the authenticated user's profile.
     *
     * @param user                the authenticated user
     * @param aiUsageEnabled      whether AI usage is enabled
     * @param aiPreferredLanguage the preferred language for AI
     * @return the updated profile
     */
    @PutMapping("/my-profile/ai-settings")
    @Operation(summary = "Update AI settings for the authenticated user's profile",
            description = "Updates AI-related settings for the currently authenticated user's profile")
    public ResponseEntity<ProfileDTO> updateMyProfileAiSettings(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @Parameter(description = "Enable AI usage")
            @RequestParam(required = false) Boolean aiUsageEnabled,
            @Parameter(description = "Preferred language for AI")
            @RequestParam(required = false) String aiPreferredLanguage) {
        log.debug("REST request to update AI settings for user: {}", user.getId());

        Optional<Profile> profileOpt = relatedEntityService.findByUserId(user.getId());
        if (profileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Profile profile = profileOpt.get();
        if (aiUsageEnabled != null) {
            profile.setAiUsageEnabled(aiUsageEnabled);
        }
        if (aiPreferredLanguage != null) {
            profile.setAiPreferredLanguage(aiPreferredLanguage);
        }

        Profile savedProfile = relatedEntityService.save(profile);
        return ResponseEntity.ok(relatedEntityMapper.toDTO(savedProfile));
    }

    /**
     * Update visibility settings for the authenticated user's profile.
     *
     * @param user                  the authenticated user
     * @param isVisibleToRecruiters whether the profile is visible to recruiters
     * @return the updated profile
     */
    @PutMapping("/my-profile/visibility")
    @Operation(summary = "Update visibility settings for the authenticated user's profile",
            description = "Updates visibility settings for the currently authenticated user's profile")
    public ResponseEntity<ProfileDTO> updateMyProfileVisibility(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            @Parameter(description = "Make profile visible to recruiters")
            @RequestParam Boolean isVisibleToRecruiters) {
        log.debug("REST request to update visibility settings for user: {}", user.getId());

        Optional<Profile> profileOpt = relatedEntityService.findByUserId(user.getId());
        if (profileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Profile profile = profileOpt.get();
        profile.setVisibleToRecruiters(isVisibleToRecruiters);

        Profile savedProfile = relatedEntityService.save(profile);
        return ResponseEntity.ok(relatedEntityMapper.toDTO(savedProfile));
    }
}
