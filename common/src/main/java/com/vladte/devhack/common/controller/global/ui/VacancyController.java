package com.vladte.devhack.common.controller.global.ui;

import com.vladte.devhack.common.controller.BaseCrudController;
import com.vladte.devhack.common.model.dto.VacancyDTO;
import com.vladte.devhack.common.model.mapper.VacancyMapper;
import com.vladte.devhack.common.model.mapper.VacancyResponseMapper;
import com.vladte.devhack.common.service.domain.global.VacancyService;
import com.vladte.devhack.common.service.domain.personalized.VacancyResponseService;
import com.vladte.devhack.common.service.view.BaseViewService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.Vacancy;
import com.vladte.devhack.entities.VacancyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controller for handling requests related to vacancies.
 * This controller follows the MVC pattern with clear separation between model and view.
 */
@Controller
@RequestMapping("/vacancies")
@Slf4j
public class VacancyController extends BaseCrudController<Vacancy, VacancyDTO, UUID, VacancyService, VacancyMapper> {

    private final VacancyResponseService vacancyResponseService;
    private final VacancyResponseMapper vacancyResponseMapper;

    /**
     * Constructor with service, mapper, and view service injection.
     *
     * @param vacancyService  the vacancy service
     * @param vacancyMapper   the vacancy mapper
     * @param baseViewService the base view service
     */
    public VacancyController(
            VacancyService vacancyService,
            VacancyMapper vacancyMapper,
            VacancyResponseService vacancyResponseService,
            @Qualifier("baseViewServiceImpl") BaseViewService baseViewService, VacancyResponseMapper vacancyResponseMapper) {
        super(vacancyService, vacancyMapper);
        this.vacancyResponseMapper = vacancyResponseMapper;
        this.vacancyResponseService = vacancyResponseService;
        setBaseViewService(baseViewService);
    }

    @Override
    protected String getListViewName() {
        return "vacancy/list";
    }

    @Override
    protected String getDetailViewName() {
        return "vacancy/view";
    }

    @Override
    protected String getListPageTitle() {
        return "Vacancies";
    }

    @Override
    protected String getDetailPageTitle() {
        return "Vacancy Details";
    }

    @Override
    protected String getEntityName() {
        return "Vacancy";
    }


    /**
     * Display the form for creating a new vacancy.
     *
     * @param model the model to add attributes to
     * @return the view name
     */
    @GetMapping("/new")
    public String newVacancyForm(Model model) {
        log.debug("Displaying form for creating a new vacancy");

        ModelBuilder.of(model)
                .addAttribute("vacancy", new VacancyDTO())
                .setPageTitle("Create New Vacancy")
                .build();

        return "vacancy/form";
    }

    /**
     * Display the form for editing an existing vacancy.
     *
     * @param id    the ID of the vacancy to edit
     * @param model the model to add attributes to
     * @return the view name
     */
    @GetMapping("/{id}/edit")
    public String editVacancyForm(@PathVariable UUID id, Model model) {
        log.debug("Displaying form for editing vacancy with ID: {}", id);
        Vacancy vacancy = getEntityOrThrow(service.findById(id), "Vacancy not found");

        ModelBuilder.of(model)
                .addAttribute("vacancy", mapper.toDTO(vacancy))
                .setPageTitle("Edit Vacancy")
                .build();

        return "vacancy/form";
    }

    /**
     * Display the details of a vacancy.
     *
     * @param id    the ID of the vacancy to display
     * @param model the model to add attributes to
     * @return the view name
     */
    @Override
    public String view(@PathVariable UUID id, Model model) {
        log.debug("Displaying details for vacancy with ID: {}", id);
        Vacancy vacancy = getEntityOrThrow(service.findById(id), "Vacancy not found");
        List<VacancyResponse> vacancyResponses = vacancyResponseService.getVacancyResponsesByVacancy(vacancy);
        ModelBuilder.of(model)
                .addAttribute("vacancy", mapper.toDTO(vacancy))
                .addAttribute("vacancyResponses", vacancyResponseMapper.toDTOList(vacancyResponses))
                .setPageTitle(getDetailPageTitle())
                .build();

        return getDetailViewName();
    }

    /**
     * Save a new or updated vacancy.
     *
     * @param vacancyDTO the vacancy data
     * @return the redirect URL
     */
    @PostMapping
    public String save(@ModelAttribute VacancyDTO vacancyDTO) {
        log.debug("Saving vacancy: {}", vacancyDTO);
        Vacancy vacancy;
        if (vacancyDTO.getId() == null) {
            vacancy = mapper.toEntity(vacancyDTO);
        } else {
            vacancy = getEntityOrThrow(service.findById(vacancyDTO.getId()), "Vacancy not found");
            mapper.updateEntityFromDTO(vacancy, vacancyDTO);
        }
        service.save(vacancy);
        return "redirect:/vacancies";
    }

    /**
     * Delete a vacancy.
     *
     * @param id the ID of the vacancy to delete
     * @return the redirect URL
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        log.debug("Deleting vacancy with ID: {}", id);
        service.deleteById(id);
        return "redirect:/vacancies";
    }

    /**
     * Search vacancies by keyword.
     *
     * @param keyword the keyword to search for
     * @param model   the model to add attributes to
     * @return the view name
     */
    @GetMapping("/search")
    public String searchVacancies(@RequestParam String keyword, Model model) {
        log.debug("Searching vacancies with keyword: {}", keyword);
        List<Vacancy> vacancies = service.searchByKeyword(keyword);

        ModelBuilder.of(model)
                .addAttribute("vacancies", mapper.toDTOList(vacancies))
                .addAttribute("keyword", keyword)
                .setPageTitle("Search Results for: " + keyword)
                .build();

        return getListViewName();
    }
}
