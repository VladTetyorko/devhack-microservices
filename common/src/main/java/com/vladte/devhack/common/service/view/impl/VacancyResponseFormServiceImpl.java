package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.dto.VacancyResponseDTO;
import com.vladte.devhack.common.mapper.VacancyResponseMapper;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.domain.VacancyResponseService;
import com.vladte.devhack.common.service.domain.VacancyService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.common.service.view.VacancyResponseFormService;
import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.Vacancy;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
                .addAttribute("interviewStages", InterviewStage.values())
                .build();
    }

    private final VacancyResponseService vacancyResponseService;
    private final UserService userService;
    private final VacancyService vacancyService;
    private final VacancyResponseMapper vacancyResponseMapper;

    @Autowired
    public VacancyResponseFormServiceImpl(VacancyResponseService vacancyResponseService,
                                          UserService userService, VacancyService vacancyService,
                                          VacancyResponseMapper vacancyResponseMapper) {
        this.vacancyResponseService = vacancyResponseService;
        this.userService = userService;
        this.vacancyService = vacancyService;
        this.vacancyResponseMapper = vacancyResponseMapper;
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
        Optional<User> userOptional = userService.findById(userId);
        Optional<Vacancy> vacancyOptional = vacancyService.findById(vacancyResponseDTO.getVacancyId());
        if (userOptional.isEmpty() || vacancyOptional.isEmpty()) {
            return null;
        }

        User user = userOptional.get();
        Vacancy vacancy = vacancyOptional.get();
        VacancyResponse response = vacancyResponseMapper.toEntity(vacancyResponseDTO);
        response.setUser(user);
        response.setVacancy(vacancy);
        VacancyResponse savedResponse = vacancyResponseService.save(response);
        return vacancyResponseMapper.toDTO(savedResponse);
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
