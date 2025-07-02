package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.dto.VacancyResponseDTO;
import com.vladte.devhack.common.mapper.VacancyResponseMapper;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.domain.VacancyResponseService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.common.service.view.VacancyResponseFormService;
import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.User;
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

    private final VacancyResponseService vacancyResponseService;
    private final UserService userService;
    private final VacancyResponseMapper vacancyResponseMapper;

    @Autowired
    public VacancyResponseFormServiceImpl(VacancyResponseService vacancyResponseService,
                                          UserService userService,
                                          VacancyResponseMapper vacancyResponseMapper) {
        this.vacancyResponseService = vacancyResponseService;
        this.userService = userService;
        this.vacancyResponseMapper = vacancyResponseMapper;
    }

    @Override
    public void prepareNewVacancyResponseForm(Model model) {
        ModelBuilder.of(model)
                .addAttribute("vacancyResponse", new VacancyResponseDTO())
                .addAttribute("users", userService.findAll())
                .addAttribute("interviewStages", InterviewStage.values())
                .build();
    }

    @Override
    public VacancyResponseDTO prepareEditVacancyResponseForm(UUID id, Model model) {
        Optional<VacancyResponse> vacancyResponseOpt = vacancyResponseService.findById(id);
        if (vacancyResponseOpt.isPresent()) {
            VacancyResponse vacancyResponse = vacancyResponseOpt.get();
            VacancyResponseDTO vacancyResponseDTO = vacancyResponseMapper.toDTO(vacancyResponse);

            ModelBuilder.of(model)
                    .addAttribute("vacancyResponse", vacancyResponseDTO)
                    .addAttribute("users", userService.findAll())
                    .addAttribute("interviewStages", InterviewStage.values())
                    .build();

            return vacancyResponseDTO;
        }
        return null;
    }

    @Override
    public VacancyResponseDTO saveVacancyResponse(VacancyResponseDTO vacancyResponseDTO, UUID userId) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            VacancyResponse vacancyResponse = vacancyResponseMapper.toEntity(vacancyResponseDTO);
            vacancyResponse.setUser(user);
            VacancyResponse savedVacancyResponse = vacancyResponseService.save(vacancyResponse);
            return vacancyResponseMapper.toDTO(savedVacancyResponse);
        }
        return null;
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
