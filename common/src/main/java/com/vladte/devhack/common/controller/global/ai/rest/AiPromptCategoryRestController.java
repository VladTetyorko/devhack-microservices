package com.vladte.devhack.common.controller.global.ai.rest;

import com.vladte.devhack.domain.entities.global.ai.AiPromptCategory;
import com.vladte.devhack.domain.model.dto.global.ai.AiPromptCategoryDTO;
import com.vladte.devhack.domain.model.mapper.global.ai.AiPromptCategoryMapper;
import com.vladte.devhack.domain.service.ai.AiPromptCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing AiPromptCategory entities.
 * Provides RESTful API endpoints for CRUD operations on AI prompt categories.
 */
@RestController
@RequestMapping("/api/ai-prompt-categories")
@Tag(name = "AI Prompt Category", description = "AI prompt category management API")
@Slf4j
@Validated
public class AiPromptCategoryRestController {

    private final AiPromptCategoryService aiPromptCategoryService;
    private final AiPromptCategoryMapper aiPromptCategoryMapper;

    /**
     * Constructor with service and mapper injection.
     *
     * @param aiPromptCategoryService the AI prompt category service
     * @param aiPromptCategoryMapper  the AI prompt category mapper
     */
    public AiPromptCategoryRestController(AiPromptCategoryService aiPromptCategoryService,
                                          AiPromptCategoryMapper aiPromptCategoryMapper) {
        this.aiPromptCategoryService = aiPromptCategoryService;
        this.aiPromptCategoryMapper = aiPromptCategoryMapper;
    }

    /**
     * Get all AI prompt categories.
     *
     * @return a list of all AI prompt categories as DTOs
     */
    @GetMapping
    @Operation(summary = "Get all AI prompt categories", description = "Returns a list of all AI prompt categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<AiPromptCategoryDTO>> getAll() {
        log.debug("REST request to get all AI prompt categories");
        List<AiPromptCategory> categories = aiPromptCategoryService.findAll();
        return ResponseEntity.ok(aiPromptCategoryMapper.toDTOList(categories));
    }

    /**
     * Get all AI prompt categories with pagination.
     *
     * @param pageable pagination information
     * @return a page of AI prompt categories as DTOs
     */
    @GetMapping("/page")
    @Operation(summary = "Get all AI prompt categories with pagination", description = "Returns a page of AI prompt categories")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved page",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Page<AiPromptCategoryDTO>> getAllPaged(
            @Parameter(description = "Pagination information (page, size, sort)")
            Pageable pageable) {
        log.debug("REST request to get a page of AI prompt categories");
        List<AiPromptCategory> categories = aiPromptCategoryService.findAll();
        List<AiPromptCategoryDTO> dtoList = aiPromptCategoryMapper.toDTOList(categories);

        // Create a page from the list (this is a simplified implementation)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtoList.size());
        Page<AiPromptCategoryDTO> page = new PageImpl<>(dtoList.subList(start, end), pageable, dtoList.size());

        return ResponseEntity.ok(page);
    }

    /**
     * Get an AI prompt category by ID.
     *
     * @param id the ID of the AI prompt category to retrieve
     * @return the AI prompt category as a DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get an AI prompt category by ID", description = "Returns a single AI prompt category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved AI prompt category",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "AI prompt category not found",
                    content = @Content)
    })
    public ResponseEntity<AiPromptCategoryDTO> getById(
            @Parameter(description = "ID of the AI prompt category to be retrieved")
            @PathVariable UUID id) {
        log.debug("REST request to get AI prompt category with ID: {}", id);
        Optional<AiPromptCategory> category = aiPromptCategoryService.findById(id);
        return category.map(c -> ResponseEntity.ok(aiPromptCategoryMapper.toDTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new AI prompt category.
     *
     * @param dto the AI prompt category to create as a DTO
     * @return the created AI prompt category as a DTO
     */
    @PostMapping
    @Operation(summary = "Create a new AI prompt category", description = "Creates a new AI prompt category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "AI prompt category created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    public ResponseEntity<AiPromptCategoryDTO> create(
            @Parameter(description = "AI prompt category to be created")
            @Valid @RequestBody AiPromptCategoryDTO dto) {
        log.debug("REST request to create AI prompt category: {}", dto);
        AiPromptCategory category = aiPromptCategoryMapper.toEntity(dto);
        AiPromptCategory savedCategory = aiPromptCategoryService.save(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(aiPromptCategoryMapper.toDTO(savedCategory));
    }

    /**
     * Update an existing AI prompt category.
     *
     * @param id  the ID of the AI prompt category to update
     * @param dto the updated AI prompt category as a DTO
     * @return the updated AI prompt category as a DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing AI prompt category", description = "Updates an existing AI prompt category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI prompt category updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "AI prompt category not found",
                    content = @Content)
    })
    public ResponseEntity<AiPromptCategoryDTO> update(
            @Parameter(description = "ID of the AI prompt category to be updated")
            @PathVariable UUID id,
            @Parameter(description = "Updated AI prompt category")
            @Valid @RequestBody AiPromptCategoryDTO dto) {
        log.debug("REST request to update AI prompt category with ID: {}", id);
        Optional<AiPromptCategory> existingCategory = aiPromptCategoryService.findById(id);
        if (existingCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AiPromptCategory category = existingCategory.get();
        aiPromptCategoryMapper.updateEntityFromDTO(category, dto);
        AiPromptCategory savedCategory = aiPromptCategoryService.save(category);
        return ResponseEntity.ok(aiPromptCategoryMapper.toDTO(savedCategory));
    }

    /**
     * Delete an AI prompt category by ID.
     *
     * @param id the ID of the AI prompt category to delete
     * @return no content if successful
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an AI prompt category", description = "Deletes an AI prompt category by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "AI prompt category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "AI prompt category not found",
                    content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the AI prompt category to be deleted")
            @PathVariable UUID id) {
        log.debug("REST request to delete AI prompt category with ID: {}", id);
        Optional<AiPromptCategory> existingCategory = aiPromptCategoryService.findById(id);
        if (existingCategory.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        aiPromptCategoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}