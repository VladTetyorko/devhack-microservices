package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.dto.VacancyResponseDTO;
import com.vladte.devhack.common.mapper.VacancyResponseMapper;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.domain.VacancyResponseService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.common.service.view.VacancyResponseViewService;
import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of the VacancyResponseViewService interface.
 * This class prepares the model for the vacancy response views.
 */
@Service
public class VacancyResponseViewServiceImpl implements VacancyResponseViewService {

    private final VacancyResponseService vacancyResponseService;
    private final UserService userService;
    private final VacancyResponseMapper vacancyResponseMapper;

    @Autowired
    public VacancyResponseViewServiceImpl(VacancyResponseService vacancyResponseService,
                                          UserService userService,
                                          VacancyResponseMapper vacancyResponseMapper) {
        this.vacancyResponseService = vacancyResponseService;
        this.userService = userService;
        this.vacancyResponseMapper = vacancyResponseMapper;
    }

    @Override
    public Page<VacancyResponseDTO> prepareCurrentUserVacancyResponsesModel(int page, int size, Model model) {
        // Get the current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        // Find the user by email
        User currentUser = userService.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);

        // Get vacancy responses for the current user with pagination
        Page<VacancyResponse> vacancyResponsePage = vacancyResponseService.getVacancyResponsesByUser(currentUser, pageable);

        // Convert entities to DTOs
        List<VacancyResponseDTO> vacancyResponseDTOs = vacancyResponsePage.getContent().stream()
                .map(vacancyResponseMapper::toDTO)
                .collect(Collectors.toList());

        // Add pagination data to model using ModelBuilder
        ModelBuilder.of(model)
                .addAttribute("vacancyResponses", vacancyResponseDTOs)
                .addAttribute("currentPage", page)
                .addAttribute("totalPages", vacancyResponsePage.getTotalPages())
                .addAttribute("totalItems", vacancyResponsePage.getTotalElements())
                .addAttribute("size", size)
                .addAttribute("user", currentUser)
                .build();

        // Return the page of DTOs
        return new PageImpl<>(vacancyResponseDTOs, pageable, vacancyResponsePage.getTotalElements());
    }

    @Override
    public void setCurrentUserVacancyResponsesPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("My Vacancy Responses")
                .build();
    }

    @Override
    public void prepareSearchResultsModel(String query, String stage, int page, int size, Model model) {
        // Convert stage string to enum if provided
        InterviewStage interviewStage = null;
        if (stage != null && !stage.isEmpty()) {
            try {
                interviewStage = InterviewStage.valueOf(stage);
            } catch (IllegalArgumentException e) {
                // Invalid stage parameter, ignore it
            }
        }

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);

        // Search with pagination
        Page<VacancyResponse> vacancyResponsePage = vacancyResponseService.searchVacancyResponses(query, interviewStage, pageable);

        // Convert entities to DTOs
        List<VacancyResponseDTO> vacancyResponseDTOs = vacancyResponsePage.getContent().stream()
                .map(vacancyResponseMapper::toDTO)
                .collect(Collectors.toList());

        // Add pagination data to model using ModelBuilder
        ModelBuilder.of(model)
                .addAttribute("vacancyResponses", vacancyResponseDTOs)
                .addAttribute("currentPage", page)
                .addAttribute("totalPages", vacancyResponsePage.getTotalPages())
                .addAttribute("totalItems", vacancyResponsePage.getTotalElements())
                .addAttribute("size", size)
                // Add filter parameters to model for maintaining state in the view
                .addAttribute("query", query)
                .addAttribute("stage", stage)
                .build();
    }

    @Override
    public void setSearchResultsPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Vacancy Responses")
                .build();
    }

    @Override
    public User prepareUserVacancyResponsesModel(UUID userId, int page, int size, Model model) {
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Create pageable object
            Pageable pageable = PageRequest.of(page, size);

            // Get user's vacancies with pagination
            Page<VacancyResponse> vacancyResponsePage = vacancyResponseService.getVacancyResponsesByUser(user, pageable);

            // Add pagination data to model using ModelBuilder
            ModelBuilder.of(model)
                    .addAttribute("vacancyResponses", vacancyResponsePage.getContent())
                    .addAttribute("currentPage", page)
                    .addAttribute("totalPages", vacancyResponsePage.getTotalPages())
                    .addAttribute("totalItems", vacancyResponsePage.getTotalElements())
                    .addAttribute("size", size)
                    .addAttribute("user", user)
                    .build();

            return user;
        }
        return null;
    }

    @Override
    public void setUserVacancyResponsesPageTitle(Model model, User user) {
        ModelBuilder.of(model)
                .setPageTitle("Vacancy Responses for " + user.getName())
                .build();
    }
}
