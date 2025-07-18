package com.vladte.devhack.common.controller.personalized.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.dto.VacancyResponseDTO;
import com.vladte.devhack.common.mapper.VacancyResponseMapper;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.domain.VacancyResponseService;
import com.vladte.devhack.common.service.domain.VacancyService;
import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.Vacancy;
import com.vladte.devhack.entities.VacancyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing VacancyResponse entities.
 * Provides RESTful API endpoints for CRUD operations on vacancy responses.
 */
@RestController
@RequestMapping("/api/vacancy-responses")
@Tag(name = "Vacancy Response", description = "Vacancy response management API")
@Slf4j
public class VacancyResponseRestController extends BaseRestController<VacancyResponse, VacancyResponseDTO, UUID, VacancyResponseService, VacancyResponseMapper> {

    private final UserService userService;
    private final VacancyService vacancyService;

    /**
     * Constructor with service and mapper injection.
     *
     * @param vacancyResponseService the vacancy response service
     * @param vacancyResponseMapper  the vacancy response mapper
     * @param userService            the user service
     * @param vacancyService         the vacancy service
     */
    public VacancyResponseRestController(VacancyResponseService vacancyResponseService,
                                         VacancyResponseMapper vacancyResponseMapper,
                                         UserService userService,
                                         VacancyService vacancyService) {
        super(vacancyResponseService, vacancyResponseMapper);
        this.userService = userService;
        this.vacancyService = vacancyService;
    }

    /**
     * Get all vacancy responses for the authenticated user.
     *
     * @param user     the authenticated user
     * @param pageable pagination information
     * @return a page of vacancy responses
     */
    @GetMapping("/my-responses")
    @Operation(summary = "Get all vacancy responses for the authenticated user",
            description = "Returns a page of vacancy responses for the authenticated user")
    public ResponseEntity<Page<VacancyResponseDTO>> getMyVacancyResponses(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        log.debug("REST request to get all vacancy responses for user: {}", user.getName());
        Page<VacancyResponse> page = service.getVacancyResponsesByUser(user, pageable);
        Page<VacancyResponseDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Search for vacancy responses by query and interview stage.
     *
     * @param query    the search query for company name, position, or technologies
     * @param stage    the interview stage to filter by
     * @param pageable pagination information
     * @return a page of vacancy responses
     */
    @GetMapping("/search")
    @Operation(summary = "Search for vacancy responses",
            description = "Returns a page of vacancy responses matching the search criteria")
    public ResponseEntity<Page<VacancyResponseDTO>> searchVacancyResponses(
            @Parameter(description = "Search query for company name, position, or technologies")
            @RequestParam(required = false) String query,
            @Parameter(description = "Interview stage to filter by")
            @RequestParam(required = false) InterviewStage stage,
            Pageable pageable) {
        log.debug("REST request to search vacancy responses with query: {} and stage: {}", query, stage);
        Page<VacancyResponse> page = service.searchVacancyResponses(query, stage, pageable);
        Page<VacancyResponseDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Create a new vacancy response for a vacancy.
     *
     * @param vacancyId the ID of the vacancy
     * @param user      the authenticated user
     * @return the created vacancy response
     */
    @PostMapping("/for-vacancy/{vacancyId}")
    @Operation(summary = "Create a new vacancy response for a vacancy",
            description = "Creates a new vacancy response for the specified vacancy and authenticated user")
    public ResponseEntity<VacancyResponseDTO> createVacancyResponse(
            @Parameter(description = "ID of the vacancy")
            @PathVariable UUID vacancyId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to create vacancy response for vacancy: {} and user: {}", vacancyId, user.getName());

        Vacancy vacancy = vacancyService.findById(vacancyId)
                .orElseThrow(() -> new IllegalArgumentException("Vacancy not found with ID: " + vacancyId));

        VacancyResponse vacancyResponse = service.saveNewResponseForUserAndVacancy(user, vacancy);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(vacancyResponse));
    }

    /**
     * Get all vacancy responses for a specific vacancy.
     *
     * @param vacancyId the ID of the vacancy
     * @return a list of vacancy responses
     */
    @GetMapping("/by-vacancy/{vacancyId}")
    @Operation(summary = "Get all vacancy responses for a specific vacancy",
            description = "Returns a list of vacancy responses for the specified vacancy")
    public ResponseEntity<List<VacancyResponseDTO>> getVacancyResponsesByVacancy(
            @Parameter(description = "ID of the vacancy")
            @PathVariable UUID vacancyId) {
        log.debug("REST request to get all vacancy responses for vacancy: {}", vacancyId);

        Vacancy vacancy = vacancyService.findById(vacancyId)
                .orElseThrow(() -> new IllegalArgumentException("Vacancy not found with ID: " + vacancyId));

        List<VacancyResponse> vacancyResponses = service.getVacancyResponsesByVacancy(vacancy);
        return ResponseEntity.ok(mapper.toDTOList(vacancyResponses));
    }

    /**
     * Get all vacancy responses for a specific user.
     *
     * @param userId   the ID of the user
     * @param pageable pagination information
     * @return a page of vacancy responses
     */
    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get all vacancy responses for a specific user",
            description = "Returns a page of vacancy responses for the specified user")
    public ResponseEntity<Page<VacancyResponseDTO>> getVacancyResponsesByUser(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId,
            Pageable pageable) {
        log.debug("REST request to get all vacancy responses for user: {}", userId);

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Page<VacancyResponse> page = service.getVacancyResponsesByUser(user, pageable);
        Page<VacancyResponseDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Update the interview stage of a vacancy response.
     *
     * @param id      the ID of the vacancy response
     * @param request the request containing the new interview stage
     * @param user    the authenticated user
     * @return the updated vacancy response
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update the interview stage of a vacancy response",
            description = "Updates the interview stage of the specified vacancy response")
    public ResponseEntity<VacancyResponseDTO> updateVacancyResponseStatus(
            @Parameter(description = "ID of the vacancy response")
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to update status of vacancy response: {} to stage: {} by user: {}",
                id, request.getInterviewStage(), user.getName());

        VacancyResponse vacancyResponse = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacancy response not found with ID: " + id));

        // Check if the current user owns this vacancy response
        if (!vacancyResponse.getUser().getId().equals(user.getId())) {
            log.warn("User {} attempted to update vacancy response {} owned by user {}",
                    user.getName(), id, vacancyResponse.getUser().getName());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update the interview stage
        vacancyResponse.setInterviewStage(request.getInterviewStage());
        VacancyResponse updatedResponse = service.save(vacancyResponse);

        return ResponseEntity.ok(mapper.toDTO(updatedResponse));
    }

    /**
     * Request DTO for updating vacancy response status.
     */
    @Setter
    @Getter
    public static class StatusUpdateRequest {
        @Parameter(description = "New interview stage", required = true)
        private InterviewStage interviewStage;

        public StatusUpdateRequest() {
        }

        public StatusUpdateRequest(InterviewStage interviewStage) {
            this.interviewStage = interviewStage;
        }

    }
}
