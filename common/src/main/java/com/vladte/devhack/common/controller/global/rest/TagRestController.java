package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.TagDTO;
import com.vladte.devhack.common.model.mapper.TagMapper;
import com.vladte.devhack.common.service.domain.global.TagService;
import com.vladte.devhack.entities.Tag;
import com.vladte.devhack.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing Tag entities.
 * Provides RESTful API endpoints for CRUD operations on tags.
 */
@RestController
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag", description = "Tag management API")
@Slf4j
public class TagRestController extends BaseRestController<Tag, TagDTO, UUID, TagService, TagMapper> {

    /**
     * Constructor with service and mapper injection.
     *
     * @param tagService the tag service
     * @param tagMapper  the tag mapper
     */
    public TagRestController(TagService tagService, TagMapper tagMapper) {
        super(tagService, tagMapper);
    }

    /**
     * Find a tag by name.
     *
     * @param name the tag name to search for
     * @return the tag with the specified name
     */
    @GetMapping("/by-name")
    @Operation(summary = "Find a tag by name", description = "Returns a tag with the specified name")
    public ResponseEntity<TagDTO> findByName(
            @Parameter(description = "Tag name to search for")
            @RequestParam String name) {
        log.debug("REST request to find tag by name: {}", name);
        Optional<com.vladte.devhack.entities.Tag> tag = service.findTagByName(name);
        return tag.map(t -> ResponseEntity.ok(mapper.toDTO(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Find a tag by slug.
     *
     * @param slug the tag slug to search for
     * @return the tag with the specified slug
     */
    @GetMapping("/by-slug")
    @Operation(summary = "Find a tag by slug", description = "Returns a tag with the specified slug")
    public ResponseEntity<TagDTO> findBySlug(
            @Parameter(description = "Tag slug to search for")
            @RequestParam String slug) {
        log.debug("REST request to find tag by slug: {}", slug);
        Optional<com.vladte.devhack.entities.Tag> tag = service.findTagBySlug(slug);
        return tag.map(t -> ResponseEntity.ok(mapper.toDTO(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all tags with progress information for the authenticated user.
     *
     * @param user the authenticated user
     * @return a list of tags with progress information
     */
    @GetMapping("/with-progress")
    @Operation(summary = "Get all tags with progress information", description = "Returns all tags with progress information for the authenticated user")
    public ResponseEntity<List<TagDTO>> getAllWithProgress(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to get all tags with progress for user: {}", user.getName());
        List<com.vladte.devhack.entities.Tag> tags = service.findAll();
        List<com.vladte.devhack.entities.Tag> tagsWithProgress = service.calculateProgressForAll(tags, user);
        return ResponseEntity.ok(mapper.toDTOList(tagsWithProgress));
    }

    /**
     * Get tag count statistics.
     *
     * @param user the authenticated user
     * @return a map with tag count statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get tag count statistics", description = "Returns tag count statistics")
    public ResponseEntity<TagStats> getTagStats(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to get tag statistics for user: {}", user.getName());
        int totalTags = service.countAllTags();
        int userTags = service.countTagsByUser(user);

        TagStats stats = new TagStats(totalTags, userTags);
        return ResponseEntity.ok(stats);
    }

    /**
     * Data class for tag statistics.
     */

    private record TagStats(int totalTags, int userTags) {
    }
}