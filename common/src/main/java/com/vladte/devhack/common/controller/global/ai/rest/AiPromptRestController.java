package com.vladte.devhack.common.controller.global.ai.rest;

import com.vladte.devhack.common.model.dto.global.ai.AiPromptDTO;
import com.vladte.devhack.common.model.mapper.global.ai.AiPromptMapper;
import com.vladte.devhack.common.service.domain.ai.AiPromptService;
import com.vladte.devhack.entities.global.ai.AiPrompt;
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
 * REST controller for managing AiPrompt entities.
 * Provides RESTful API endpoints for CRUD operations on AI prompts.
 */
@RestController
@RequestMapping("/api/ai-prompts")
@Tag(name = "AI Prompt", description = "AI prompt management API")
@Slf4j
@Validated
public class AiPromptRestController {

    private final AiPromptService aiPromptService;
    private final AiPromptMapper aiPromptMapper;

    /**
     * Constructor with service and mapper injection.
     *
     * @param aiPromptService the AI prompt service
     * @param aiPromptMapper  the AI prompt mapper
     */
    public AiPromptRestController(AiPromptService aiPromptService, AiPromptMapper aiPromptMapper) {
        this.aiPromptService = aiPromptService;
        this.aiPromptMapper = aiPromptMapper;
    }

    /**
     * Get all AI prompts.
     *
     * @return a list of all AI prompts as DTOs
     */
    @GetMapping
    @Operation(summary = "Get all AI prompts", description = "Returns a list of all AI prompts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<List<AiPromptDTO>> getAll() {
        log.debug("REST request to get all AI prompts");
        List<AiPrompt> prompts = aiPromptService.findAll();
        return ResponseEntity.ok(aiPromptMapper.toDTOList(prompts));
    }

    /**
     * Get all AI prompts with pagination.
     *
     * @param pageable pagination information
     * @return a page of AI prompts as DTOs
     */
    @GetMapping("/page")
    @Operation(summary = "Get all AI prompts with pagination", description = "Returns a page of AI prompts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved page",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<Page<AiPromptDTO>> getAllPaged(
            @Parameter(description = "Pagination information (page, size, sort)")
            Pageable pageable) {
        log.debug("REST request to get a page of AI prompts");
        List<AiPrompt> prompts = aiPromptService.findAll();
        List<AiPromptDTO> dtoList = aiPromptMapper.toDTOList(prompts);

        // Create a page from the list (this is a simplified implementation)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dtoList.size());
        Page<AiPromptDTO> page = new PageImpl<>(dtoList.subList(start, end), pageable, dtoList.size());

        return ResponseEntity.ok(page);
    }

    /**
     * Get an AI prompt by ID.
     *
     * @param id the ID of the AI prompt to retrieve
     * @return the AI prompt as a DTO
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get an AI prompt by ID", description = "Returns a single AI prompt by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved AI prompt",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "AI prompt not found",
                    content = @Content)
    })
    public ResponseEntity<AiPromptDTO> getById(
            @Parameter(description = "ID of the AI prompt to be retrieved")
            @PathVariable UUID id) {
        log.debug("REST request to get AI prompt with ID: {}", id);
        Optional<AiPrompt> prompt = aiPromptService.findById(id);
        return prompt.map(p -> ResponseEntity.ok(aiPromptMapper.toDTO(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new AI prompt.
     *
     * @param dto the AI prompt to create as a DTO
     * @return the created AI prompt as a DTO
     */
    @PostMapping
    @Operation(summary = "Create a new AI prompt", description = "Creates a new AI prompt")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "AI prompt created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    public ResponseEntity<AiPromptDTO> create(
            @Parameter(description = "AI prompt to be created")
            @Valid @RequestBody AiPromptDTO dto) {
        log.debug("REST request to create AI prompt: {}", dto);
        AiPrompt prompt = aiPromptMapper.toEntity(dto);
        AiPrompt savedPrompt = aiPromptService.save(prompt);
        return ResponseEntity.status(HttpStatus.CREATED).body(aiPromptMapper.toDTO(savedPrompt));
    }

    /**
     * Update an existing AI prompt.
     *
     * @param id  the ID of the AI prompt to update
     * @param dto the updated AI prompt as a DTO
     * @return the updated AI prompt as a DTO
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing AI prompt", description = "Updates an existing AI prompt by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "AI prompt updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "AI prompt not found",
                    content = @Content)
    })
    public ResponseEntity<AiPromptDTO> update(
            @Parameter(description = "ID of the AI prompt to be updated")
            @PathVariable UUID id,
            @Parameter(description = "Updated AI prompt")
            @Valid @RequestBody AiPromptDTO dto) {
        log.debug("REST request to update AI prompt with ID: {}", id);
        Optional<AiPrompt> existingPrompt = aiPromptService.findById(id);
        if (existingPrompt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        AiPrompt prompt = existingPrompt.get();
        aiPromptMapper.updateEntityFromDTO(prompt, dto);
        AiPrompt savedPrompt = aiPromptService.save(prompt);
        return ResponseEntity.ok(aiPromptMapper.toDTO(savedPrompt));
    }

    /**
     * Delete an AI prompt by ID.
     *
     * @param id the ID of the AI prompt to delete
     * @return no content if successful
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an AI prompt", description = "Deletes an AI prompt by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "AI prompt deleted successfully"),
            @ApiResponse(responseCode = "404", description = "AI prompt not found",
                    content = @Content)
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the AI prompt to be deleted")
            @PathVariable UUID id) {
        log.debug("REST request to delete AI prompt with ID: {}", id);
        Optional<AiPrompt> existingPrompt = aiPromptService.findById(id);
        if (existingPrompt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        aiPromptService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all active AI prompts.
     *
     * @return a list of active AI prompts
     */
    @GetMapping("/active")
    @Operation(summary = "Get all active AI prompts", description = "Returns a list of all active AI prompts")
    public ResponseEntity<List<AiPromptDTO>> getActivePrompts() {
        log.debug("REST request to get all active AI prompts");
        List<AiPrompt> prompts = aiPromptService.findActive();
        return ResponseEntity.ok(aiPromptMapper.toDTOList(prompts));
    }

    /**
     * Get AI prompts by category.
     *
     * @param categoryId the ID of the category
     * @return a list of AI prompts in the specified category
     */
    @GetMapping("/by-category/{categoryId}")
    @Operation(summary = "Get AI prompts by category", description = "Returns a list of AI prompts in the specified category")
    public ResponseEntity<List<AiPromptDTO>> getPromptsByCategory(
            @Parameter(description = "ID of the category")
            @PathVariable UUID categoryId) {
        log.debug("REST request to get AI prompts for category: {}", categoryId);
        List<AiPrompt> prompts = aiPromptService.findByCategoryId(categoryId);
        return ResponseEntity.ok(aiPromptMapper.toDTOList(prompts));
    }
}
