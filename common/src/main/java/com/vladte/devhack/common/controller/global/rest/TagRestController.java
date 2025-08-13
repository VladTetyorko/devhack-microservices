package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.global.TagDTO;
import com.vladte.devhack.common.model.mapper.global.TagMapper;
import com.vladte.devhack.common.service.domain.global.TagService;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        Optional<Tag> tag = service.findTagByName(name);
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
        Optional<Tag> tag = service.findTagBySlug(slug);
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
        log.debug("REST request to get all tags with progress for user: {}", user.getProfile().getName());
        List<Tag> tags = service.findAll();
        List<Tag> tagsWithProgress = service.calculateProgressForAll(tags, user);
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
        log.debug("REST request to get tag statistics for user: {}", user.getProfile().getName());
        int totalTags = service.countAllTags();
        int userTags = service.countTagsByUser(user);

        TagStats stats = new TagStats(totalTags, userTags);
        return ResponseEntity.ok(stats);
    }

    // ========== Hierarchical Tag Endpoints ==========

    /**
     * Get all root tags (tags with no parent).
     *
     * @return a list of root tags
     */
    @GetMapping("/hierarchy/roots")
    @Operation(summary = "Get all root tags", description = "Returns all tags that have no parent (root level tags)")
    public ResponseEntity<List<TagDTO>> getRootTags() {
        log.debug("REST request to get all root tags");
        List<Tag> rootTags = service.findRootTags();
        return ResponseEntity.ok(mapper.toDTOList(rootTags));
    }

    /**
     * Get direct children of a specific tag.
     *
     * @param parentId the parent tag ID
     * @return a list of child tags
     */
    @GetMapping("/hierarchy/{parentId}/children")
    @Operation(summary = "Get children of a tag", description = "Returns all direct children of the specified tag")
    public ResponseEntity<List<TagDTO>> getChildren(
            @Parameter(description = "Parent tag ID")
            @PathVariable UUID parentId) {
        log.debug("REST request to get children of tag: {}", parentId);
        Optional<Tag> parentTag = service.findById(parentId);
        if (parentTag.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Tag> children = service.findChildren(parentTag.get());
        return ResponseEntity.ok(mapper.toDTOList(children));
    }

    /**
     * Get all descendants of a specific tag (entire subtree).
     *
     * @param parentId the parent tag ID
     * @return a list of descendant tags
     */
    @GetMapping("/hierarchy/{parentId}/descendants")
    @Operation(summary = "Get descendants of a tag", description = "Returns all descendants of the specified tag (entire subtree)")
    public ResponseEntity<List<TagDTO>> getDescendants(
            @Parameter(description = "Parent tag ID")
            @PathVariable UUID parentId) {
        log.debug("REST request to get descendants of tag: {}", parentId);
        Optional<Tag> parentTag = service.findById(parentId);
        if (parentTag.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Tag> descendants = service.findDescendants(parentTag.get());
        return ResponseEntity.ok(mapper.toDTOList(descendants));
    }

    /**
     * Get all ancestors of a specific tag.
     *
     * @param tagId the tag ID
     * @return a list of ancestor tags
     */
    @GetMapping("/hierarchy/{tagId}/ancestors")
    @Operation(summary = "Get ancestors of a tag", description = "Returns all ancestors of the specified tag from root to immediate parent")
    public ResponseEntity<List<TagDTO>> getAncestors(
            @Parameter(description = "Tag ID")
            @PathVariable UUID tagId) {
        log.debug("REST request to get ancestors of tag: {}", tagId);
        Optional<Tag> tag = service.findById(tagId);
        if (tag.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Tag> ancestors = service.findAncestors(tag.get());
        return ResponseEntity.ok(mapper.toDTOList(ancestors));
    }

    /**
     * Get subtree with limited depth.
     *
     * @param parentId the parent tag ID
     * @param depth    the maximum depth to include
     * @return a list of tags in subtree within depth limit
     */
    @GetMapping("/hierarchy/{parentId}/subtree")
    @Operation(summary = "Get subtree with depth limit", description = "Returns tags in subtree within the specified depth limit")
    public ResponseEntity<List<TagDTO>> getSubtree(
            @Parameter(description = "Parent tag ID")
            @PathVariable UUID parentId,
            @Parameter(description = "Maximum depth to include")
            @RequestParam int depth) {
        log.debug("REST request to get subtree of tag: {} with depth: {}", parentId, depth);
        Optional<Tag> parentTag = service.findById(parentId);
        if (parentTag.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Tag> subtree = service.findSubtree(parentTag.get(), depth);
        return ResponseEntity.ok(mapper.toDTOList(subtree));
    }

    /**
     * Get siblings of a specific tag.
     *
     * @param tagId the tag ID
     * @return a list of sibling tags
     */
    @GetMapping("/hierarchy/{tagId}/siblings")
    @Operation(summary = "Get siblings of a tag", description = "Returns all sibling tags (tags with same parent)")
    public ResponseEntity<List<TagDTO>> getSiblings(
            @Parameter(description = "Tag ID")
            @PathVariable UUID tagId) {
        log.debug("REST request to get siblings of tag: {}", tagId);
        Optional<Tag> tag = service.findById(tagId);
        if (tag.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Tag> siblings = service.findSiblings(tag.get());
        return ResponseEntity.ok(mapper.toDTOList(siblings));
    }

    /**
     * Get tags at specific depth level.
     *
     * @param depth the depth level (0 for root)
     * @return a list of tags at the specified depth
     */
    @GetMapping("/hierarchy/depth/{depth}")
    @Operation(summary = "Get tags at depth level", description = "Returns all tags at the specified depth level")
    public ResponseEntity<List<TagDTO>> getTagsByDepth(
            @Parameter(description = "Depth level (0 for root)")
            @PathVariable int depth) {
        log.debug("REST request to get tags at depth: {}", depth);
        List<Tag> tags = service.findTagsByDepth(depth);
        return ResponseEntity.ok(mapper.toDTOList(tags));
    }

    /**
     * Validate if a tag move is valid (no cycles).
     *
     * @param tagId       the tag ID to move
     * @param newParentId the new parent ID (optional)
     * @return validation response
     */
    @GetMapping("/hierarchy/{tagId}/validate-move")
    @Operation(summary = "Validate tag move", description = "Validates if moving a tag to a new parent would create a cycle")
    public ResponseEntity<TagValidationResponse> validateMove(
            @Parameter(description = "Tag ID to move")
            @PathVariable UUID tagId,
            @Parameter(description = "New parent ID (optional)")
            @RequestParam(required = false) UUID newParentId) {
        log.debug("REST request to validate move of tag: {} to parent: {}", tagId, newParentId);
        Optional<Tag> tag = service.findById(tagId);
        if (tag.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tag newParent = null;
        if (newParentId != null) {
            Optional<Tag> parentOpt = service.findById(newParentId);
            if (parentOpt.isEmpty()) {
                return ResponseEntity.ok(new TagValidationResponse(false, "New parent tag not found"));
            }
            newParent = parentOpt.get();
        }

        boolean isValid = service.validateMove(tag.get(), newParent);
        String message = isValid ? null : "Move would create a cycle or violate constraints";
        return ResponseEntity.ok(new TagValidationResponse(isValid, message));
    }

    /**
     * Get complete tag hierarchy.
     *
     * @return hierarchy response with root tags and all tags
     */
    @GetMapping("/hierarchy")
    @Operation(summary = "Get complete hierarchy", description = "Returns complete tag hierarchy with root tags and all tags")
    public ResponseEntity<TagHierarchyResponse> getHierarchy() {
        log.debug("REST request to get complete tag hierarchy");
        List<Tag> rootTags = service.findRootTags();
        List<Tag> allTags = service.findAll();
        return ResponseEntity.ok(new TagHierarchyResponse(mapper.toDTOList(rootTags), mapper.toDTOList(allTags)));
    }

    /**
     * Create a new tag with optional parent.
     *
     * @param request the tag creation request
     * @return the created tag
     */
    @PostMapping("/hierarchy")
    @Operation(summary = "Create tag with parent", description = "Creates a new tag with optional parent for hierarchical structure")
    public ResponseEntity<TagDTO> createTagWithParent(@RequestBody TagCreateRequest request) {
        log.debug("REST request to create tag with parent: {}", request);

        Tag parent = null;
        if (request.parentId() != null) {
            Optional<Tag> parentOpt = service.findById(UUID.fromString(request.parentId()));
            if (parentOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            parent = parentOpt.get();
        }

        Tag createdTag = service.createTag(request.name(), parent);
        return ResponseEntity.ok(mapper.toDTO(createdTag));
    }

    /**
     * Move a tag to a new parent.
     *
     * @param request the tag move request
     * @return the updated tag
     */
    @PutMapping("/hierarchy/move")
    @Operation(summary = "Move tag", description = "Moves a tag to a new parent, updating all subtree paths")
    public ResponseEntity<TagDTO> moveTag(@RequestBody TagMoveRequest request) {
        log.debug("REST request to move tag: {}", request);

        Optional<Tag> tag = service.findById(UUID.fromString(request.tagId()));
        if (tag.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Tag newParent = null;
        if (request.newParentId() != null) {
            Optional<Tag> parentOpt = service.findById(UUID.fromString(request.newParentId()));
            if (parentOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            newParent = parentOpt.get();
        }

        Tag movedTag = service.moveTag(tag.get(), newParent);
        return ResponseEntity.ok(mapper.toDTO(movedTag));
    }

    /**
     * Delete a tag with cascade option.
     *
     * @param id      the tag ID
     * @param cascade whether to cascade delete children
     * @return no content response
     */
    @DeleteMapping("/hierarchy/{id}")
    @Operation(summary = "Delete tag with cascade", description = "Deletes a tag with option to cascade delete children or move them to root")
    public ResponseEntity<Void> deleteTagWithCascade(
            @Parameter(description = "Tag ID")
            @PathVariable UUID id,
            @Parameter(description = "Whether to cascade delete children")
            @RequestParam(defaultValue = "false") boolean cascade) {
        log.debug("REST request to delete tag: {} with cascade: {}", id, cascade);
        Optional<Tag> tag = service.findById(id);
        if (tag.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        service.deleteTag(tag.get(), cascade);
        return ResponseEntity.noContent().build();
    }

    /**
     * Data class for tag statistics.
     */
    private record TagStats(int totalTags, int userTags) {
    }

    /**
     * Data class for tag validation response.
     */
    private record TagValidationResponse(boolean valid, String message) {
    }

    /**
     * Data class for tag hierarchy response.
     */
    private record TagHierarchyResponse(List<TagDTO> rootTags, List<TagDTO> allTags) {
    }

    /**
     * Data class for tag creation request.
     */
    private record TagCreateRequest(String name, String description, String parentId) {
    }

    /**
     * Data class for tag move request.
     */
    private record TagMoveRequest(String tagId, String newParentId) {
    }

}