package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.model.dto.VacancyResponseDTO;
import com.vladte.devhack.common.model.mapper.VacancyResponseMapper;
import com.vladte.devhack.common.service.domain.global.InterviewStageService;
import com.vladte.devhack.common.service.domain.global.VacancyService;
import com.vladte.devhack.common.service.domain.personalized.VacancyResponseService;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.common.service.view.VacancyResponseFormService;
import com.vladte.devhack.entities.global.InterviewStage;
import com.vladte.devhack.entities.global.Vacancy;
import com.vladte.devhack.entities.personalized.VacancyResponse;
import com.vladte.devhack.entities.user.User;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the VacancyResponseFormService interface.
 * This class handles form-related operations for vacancy responses.
 */
@Service
public class VacancyResponseFormServiceImpl implements VacancyResponseFormService {

    private void prepareCommonFormAttributes(Model model, VacancyResponseDTO vacancyResponseDTO) {
        ModelBuilder.of(model)
                .addAttribute("vacancyResponse", vacancyResponseDTO)
                .addAttribute("users", userService.findAll())
                .addAttribute("vacancies", vacancyService.findAll())
                .addAttribute("interviewStages", interviewStageService.findAllActiveOrderByOrderIndex())
                .build();
    }

    private final VacancyResponseService vacancyResponseService;
    private final UserService userService;
    private final VacancyService vacancyService;
    private final VacancyResponseMapper vacancyResponseMapper;
    private final InterviewStageService interviewStageService;


    public VacancyResponseFormServiceImpl(VacancyResponseService vacancyResponseService,
                                          UserService userService, VacancyService vacancyService,
                                          VacancyResponseMapper vacancyResponseMapper,
                                          InterviewStageService interviewStageService) {
        this.vacancyResponseService = vacancyResponseService;
        this.userService = userService;
        this.vacancyService = vacancyService;
        this.vacancyResponseMapper = vacancyResponseMapper;
        this.interviewStageService = interviewStageService;
    }

    @Override
    public void prepareNewVacancyResponseForm(Model model) {
        prepareCommonFormAttributes(model, new VacancyResponseDTO());
    }

    @Override
    public VacancyResponseDTO prepareEditVacancyResponseForm(UUID id, Model model) {
        return vacancyResponseService.findById(id)
                .map(response -> {
                    VacancyResponseDTO dto = vacancyResponseMapper.toDTO(response);
                    prepareCommonFormAttributes(model, dto);
                    return dto;
                })
                .orElse(null);
    }

    @Override
    public VacancyResponseDTO saveVacancyResponse(VacancyResponseDTO vacancyResponseDTO, UUID userId) {
        return saveVacancyResponse(vacancyResponseDTO, userId, null);
    }

    @Override
    public VacancyResponseDTO saveVacancyResponse(VacancyResponseDTO vacancyResponseDTO, UUID userId, UUID interviewStageId) {
        return interviewStageService.findById(interviewStageId)
                .map(interviewStage -> {
                    VacancyResponse populatedVacancyResponse = populateVacancyResponseWithRelations(vacancyResponseDTO, userId, interviewStage);
                    VacancyResponse savedResponse = vacancyResponseService.save(populatedVacancyResponse);
                    return vacancyResponseMapper.toDTO(savedResponse);
                })
                .orElse(null);
    }

    private VacancyResponse populateVacancyResponseWithRelations(VacancyResponseDTO vacancyResponseDTO, UUID userId, InterviewStage interviewStage) {
        Optional<User> userOptional = userService.findById(userId);
        Optional<Vacancy> vacancyOptional = vacancyService.findById(vacancyResponseDTO.getVacancyId());

        if (userOptional.isEmpty() || vacancyOptional.isEmpty()) {
            return null;
        }

        VacancyResponse populatedVacancy = vacancyResponseMapper.toEntity(vacancyResponseDTO);
        populatedVacancy.setUser(userOptional.get());
        populatedVacancy.setVacancy(vacancyOptional.get());
        if (interviewStage != null) {
            populatedVacancy.setInterviewStage(interviewStage);
        }

        return populatedVacancy;
    }

    @Override
    public void deleteVacancyResponse(UUID id) {
        vacancyResponseService.deleteById(id);
    }

    @Override
    public void setNewVacancyResponsePageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("New Vacancy Response")
                .build();
    }

    @Override
    public void setEditVacancyResponsePageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Edit Vacancy Response")
                .build();
    }
}
