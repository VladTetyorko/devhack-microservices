package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.VacancyDTO;
import com.vladte.devhack.common.model.mapper.VacancyMapper;
import com.vladte.devhack.common.service.domain.global.VacancyService;
import com.vladte.devhack.entities.global.Vacancy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Vacancy entities.
 * Provides RESTful API endpoints for CRUD operations on vacancies.
 */
@RestController
@RequestMapping("/api/vacancies")
@Tag(name = "Vacancy", description = "Vacancy management API")
@Slf4j
public class VacancyRestController extends BaseRestController<Vacancy, VacancyDTO, UUID, VacancyService, VacancyMapper> {

    /**
     * Constructor with service and mapper injection.
     *
     * @param vacancyService the vacancy service
     * @param vacancyMapper  the vacancy mapper
     */
    public VacancyRestController(VacancyService vacancyService, VacancyMapper vacancyMapper) {
        super(vacancyService, vacancyMapper);
    }

    /**
     * Find vacancies by company name.
     *
     * @param companyName the company name to search for
     * @return a list of vacancies from the specified company
     */
    @GetMapping("/by-company")
    @Operation(summary = "Find vacancies by company name", description = "Returns a list of vacancies from the specified company")
    public ResponseEntity<List<VacancyDTO>> findByCompanyName(
            @Parameter(description = "Company name to search for")
            @RequestParam String companyName) {
        log.debug("REST request to find vacancies by company name: {}", companyName);
        List<Vacancy> vacancies = service.findByCompanyName(companyName);
        return ResponseEntity.ok(mapper.toDTOList(vacancies));
    }

    /**
     * Find vacancies by position.
     *
     * @param position the position to search for
     * @return a list of vacancies with the specified position
     */
    @GetMapping("/by-position")
    @Operation(summary = "Find vacancies by position", description = "Returns a list of vacancies with the specified position")
    public ResponseEntity<List<VacancyDTO>> findByPosition(
            @Parameter(description = "Position to search for")
            @RequestParam String position) {
        log.debug("REST request to find vacancies by position: {}", position);
        List<Vacancy> vacancies = service.findByPosition(position);
        return ResponseEntity.ok(mapper.toDTOList(vacancies));
    }

    /**
     * Find vacancies by source.
     *
     * @param source the source to search for
     * @return a list of vacancies from the specified source
     */
    @GetMapping("/by-source")
    @Operation(summary = "Find vacancies by source", description = "Returns a list of vacancies from the specified source")
    public ResponseEntity<List<VacancyDTO>> findBySource(
            @Parameter(description = "Source to search for")
            @RequestParam String source) {
        log.debug("REST request to find vacancies by source: {}", source);
        List<Vacancy> vacancies = service.findBySource(source);
        return ResponseEntity.ok(mapper.toDTOList(vacancies));
    }

    /**
     * Find vacancies by remote allowed status.
     *
     * @param remoteAllowed the remote allowed status to search for
     * @return a list of vacancies with the specified remote allowed status
     */
    @GetMapping("/by-remote")
    @Operation(summary = "Find vacancies by remote allowed status", description = "Returns a list of vacancies with the specified remote allowed status")
    public ResponseEntity<List<VacancyDTO>> findByRemoteAllowed(
            @Parameter(description = "Remote allowed status to search for")
            @RequestParam Boolean remoteAllowed) {
        log.debug("REST request to find vacancies by remote allowed status: {}", remoteAllowed);
        List<Vacancy> vacancies = service.findByRemoteAllowed(remoteAllowed);
        return ResponseEntity.ok(mapper.toDTOList(vacancies));
    }

    /**
     * Search vacancies by keyword.
     *
     * @param keyword the keyword to search for
     * @return a list of vacancies matching the keyword
     */
    @GetMapping("/search")
    @Operation(summary = "Search vacancies by keyword", description = "Returns a list of vacancies matching the keyword in company name, position, or technologies")
    public ResponseEntity<List<VacancyDTO>> searchByKeyword(
            @Parameter(description = "Keyword to search for")
            @RequestParam String keyword) {
        log.debug("REST request to search vacancies by keyword: {}", keyword);
        List<Vacancy> vacancies = service.searchByKeyword(keyword);
        return ResponseEntity.ok(mapper.toDTOList(vacancies));
    }

    /**
     * Get all vacancies with pagination and sorting.
     * This method overrides the one in BaseRestController to provide a more specific API documentation.
     *
     * @param pageable pagination information
     * @return a page of vacancies
     */
    @Override
    @GetMapping("/page")
    @Operation(summary = "Get all vacancies with pagination", description = "Returns a page of vacancies with pagination and sorting")
    public ResponseEntity<Page<VacancyDTO>> getAllPaged(Pageable pageable) {
        return super.getAllPaged(pageable);
    }
}