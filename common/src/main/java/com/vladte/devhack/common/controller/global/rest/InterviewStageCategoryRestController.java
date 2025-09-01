package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.domain.entities.global.InterviewStageCategory;
import com.vladte.devhack.domain.model.dto.global.InterviewStageCategoryDTO;
import com.vladte.devhack.domain.model.mapper.global.InterviewStageCategoryMapper;
import com.vladte.devhack.domain.service.global.InterviewStageCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing InterviewStageCategory entities.
 * Provides RESTful API endpoints for CRUD operations on interview stage categories.
 */
@RestController
@RequestMapping("/api/interview-stage-categories")
@Tag(name = "Interview Stage Category", description = "Interview stage category management API")
@Slf4j
public class InterviewStageCategoryRestController extends BaseRestController<InterviewStageCategory, InterviewStageCategoryDTO, UUID, InterviewStageCategoryService, InterviewStageCategoryMapper> {

    /**
     * Constructor with service and mapper injection.
     *
     * @param service the interview stage category service
     * @param mapper  the interview stage category mapper
     */
    public InterviewStageCategoryRestController(InterviewStageCategoryService service, InterviewStageCategoryMapper mapper) {
        super(service, mapper);
    }

    /**
     * Find an interview stage category by code.
     *
     * @param code the category code to search for
     * @return the category with the specified code
     */
    @GetMapping("/by-code")
    @Operation(summary = "Find a category by code", description = "Returns a category with the specified code")
    public ResponseEntity<InterviewStageCategoryDTO> findByCode(
            @Parameter(description = "Category code to search for")
            @RequestParam String code) {
        log.debug("REST request to find interview stage category by code: {}", code);
        Optional<InterviewStageCategory> category = relatedEntityService.findByCode(code);
        return category.map(c -> ResponseEntity.ok(relatedEntityMapper.toDTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }
}