package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.InterviewStageDTO;
import com.vladte.devhack.common.model.mapper.InterviewStageMapper;
import com.vladte.devhack.common.service.domain.personalized.InterviewStageService;
import com.vladte.devhack.entities.InterviewStage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing InterviewStage entities.
 * Provides RESTful API endpoints for CRUD operations on interview stages.
 */
@RestController
@RequestMapping("/api/interview-stages")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Interview Stage", description = "Interview stage management API")
@Slf4j
public class InterviewStageRestController extends BaseRestController<InterviewStage, InterviewStageDTO, UUID, InterviewStageService, InterviewStageMapper> {

    /**
     * Constructor with service and mapper injection.
     *
     * @param service the interview stage service
     * @param mapper  the interview stage mapper
     */
    public InterviewStageRestController(InterviewStageService service, InterviewStageMapper mapper) {
        super(service, mapper);
    }

    /**
     * Find an interview stage by code.
     *
     * @param code the stage code to search for
     * @return the stage with the specified code
     */
    @GetMapping("/by-code")
    @Operation(summary = "Find a stage by code", description = "Returns a stage with the specified code")
    public ResponseEntity<InterviewStageDTO> findByCode(
            @Parameter(description = "Stage code to search for")
            @RequestParam String code) {
        log.debug("REST request to find interview stage by code: {}", code);
        Optional<InterviewStage> stage = service.findByCode(code);
        return stage.map(s -> ResponseEntity.ok(mapper.toDTO(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all active interview stages ordered by order index.
     *
     * @return a list of active interview stages
     */
    @GetMapping("/active")
    @Operation(summary = "Get all active stages", description = "Returns all active interview stages ordered by order index")
    public ResponseEntity<List<InterviewStageDTO>> getAllActive() {
        log.debug("REST request to get all active interview stages");
        List<InterviewStage> stages = service.findAllActiveOrderByOrderIndex();
        return ResponseEntity.ok(mapper.toDTOList(stages));
    }

    /**
     * Find interview stages by category code.
     *
     * @param categoryCode the category code
     * @return a list of interview stages in the specified category
     */
    @GetMapping("/by-category")
    @Operation(summary = "Find stages by category", description = "Returns stages in the specified category")
    public ResponseEntity<List<InterviewStageDTO>> findByCategoryCode(
            @Parameter(description = "Category code to filter by")
            @RequestParam String categoryCode) {
        log.debug("REST request to find interview stages by category code: {}", categoryCode);
        List<InterviewStage> stages = service.findByCategoryCode(categoryCode);
        return ResponseEntity.ok(mapper.toDTOList(stages));
    }

    /**
     * Get all final stages.
     *
     * @return a list of final interview stages
     */
    @GetMapping("/final")
    @Operation(summary = "Get all final stages", description = "Returns all final interview stages")
    public ResponseEntity<List<InterviewStageDTO>> getAllFinalStages() {
        log.debug("REST request to get all final interview stages");
        List<InterviewStage> stages = service.findAllFinalStages();
        return ResponseEntity.ok(mapper.toDTOList(stages));
    }

    /**
     * Get all non-internal stages (visible to candidates).
     *
     * @return a list of non-internal interview stages
     */
    @GetMapping("/public")
    @Operation(summary = "Get all public stages", description = "Returns all non-internal interview stages visible to candidates")
    public ResponseEntity<List<InterviewStageDTO>> getAllNonInternalStages() {
        log.debug("REST request to get all non-internal interview stages");
        List<InterviewStage> stages = service.findAllNonInternalStages();
        return ResponseEntity.ok(mapper.toDTOList(stages));
    }

    /**
     * Get the next stage in the interview process.
     *
     * @param stageId the current stage ID
     * @return the next stage if exists
     */
    @GetMapping("/{stageId}/next")
    @Operation(summary = "Get next stage", description = "Returns the next stage in the interview process")
    public ResponseEntity<InterviewStageDTO> getNextStage(
            @Parameter(description = "Current stage ID")
            @PathVariable UUID stageId) {
        log.debug("REST request to get next stage for stage ID: {}", stageId);
        Optional<InterviewStage> currentStage = service.findById(stageId);
        if (currentStage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<InterviewStage> nextStage = service.getNextStage(currentStage.get());
        return nextStage.map(s -> ResponseEntity.ok(mapper.toDTO(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get the previous stage in the interview process.
     *
     * @param stageId the current stage ID
     * @return the previous stage if exists
     */
    @GetMapping("/{stageId}/previous")
    @Operation(summary = "Get previous stage", description = "Returns the previous stage in the interview process")
    public ResponseEntity<InterviewStageDTO> getPreviousStage(
            @Parameter(description = "Current stage ID")
            @PathVariable UUID stageId) {
        log.debug("REST request to get previous stage for stage ID: {}", stageId);
        Optional<InterviewStage> currentStage = service.findById(stageId);
        if (currentStage.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<InterviewStage> previousStage = service.getPreviousStage(currentStage.get());
        return previousStage.map(s -> ResponseEntity.ok(mapper.toDTO(s)))
                .orElse(ResponseEntity.notFound().build());
    }
}