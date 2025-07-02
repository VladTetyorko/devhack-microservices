package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.dto.VacancyResponseDTO;
import com.vladte.devhack.common.mapper.VacancyResponseMapper;
import com.vladte.devhack.common.service.domain.VacancyResponseService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.common.service.view.VacancyResponseDashboardService;
import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the VacancyResponseDashboardService interface.
 * This class prepares the model for the vacancy response dashboard view.
 */
@Service
public class VacancyResponseDashboardServiceImpl implements VacancyResponseDashboardService {

    private final VacancyResponseService vacancyResponseService;
    private final VacancyResponseMapper vacancyResponseMapper;

    @Autowired
    public VacancyResponseDashboardServiceImpl(VacancyResponseService vacancyResponseService,
                                               VacancyResponseMapper vacancyResponseMapper) {
        this.vacancyResponseService = vacancyResponseService;
        this.vacancyResponseMapper = vacancyResponseMapper;
    }


    @Override
    public Page<VacancyResponseDTO> prepareDashboardModel(int page, int size, Model model) {
        // Create pageable object for the vacancy responses
        Pageable pageable = PageRequest.of(page, size);

        // Get all vacancy responses with pagination for the "Top Companies" section
        Page<VacancyResponse> vacancyResponsePage = vacancyResponseService.findAll(pageable);

        // Convert entities to DTOs
        List<VacancyResponseDTO> vacancyResponseDTOs = vacancyResponsePage.getContent().stream()
                .map(vacancyResponseMapper::toDTO)
                .collect(Collectors.toList());

        // Get all vacancy responses for statistics (no pagination needed for statistics)
        List<VacancyResponse> allVacancyResponses = vacancyResponseService.findAll();

        // Count vacancies by interview stage
        Map<InterviewStage, Long> stageCountMap = allVacancyResponses.stream()
                .collect(Collectors.groupingBy(VacancyResponse::getInterviewStage, Collectors.counting()));

        // Get counts for each stage
        long appliedCount = stageCountMap.getOrDefault(InterviewStage.APPLIED, 0L);
        long phoneInterviewCount = stageCountMap.getOrDefault(InterviewStage.SCREENING, 0L);
        long technicalInterviewCount = stageCountMap.getOrDefault(InterviewStage.TECHNICAL_INTERVIEW, 0L);
        long finalInterviewCount = stageCountMap.getOrDefault(InterviewStage.STAKEHOLDER_INTERVIEW, 0L);
        long offerCount = stageCountMap.getOrDefault(InterviewStage.OFFER, 0L);
        long rejectedCount = stageCountMap.getOrDefault(InterviewStage.REJECTED, 0L);

        // Calculate percentages
        int totalVacancies = allVacancyResponses.size();
        int appliedPercentage = totalVacancies > 0 ? (int) (((double) appliedCount / totalVacancies) * 100) : 0;
        int phoneInterviewPercentage = totalVacancies > 0 ? (int) (((double) phoneInterviewCount / totalVacancies) * 100) : 0;
        int technicalInterviewPercentage = totalVacancies > 0 ? (int) (((double) technicalInterviewCount / totalVacancies) * 100) : 0;
        int finalInterviewPercentage = totalVacancies > 0 ? (int) (((double) finalInterviewCount / totalVacancies) * 100) : 0;
        int offerPercentage = totalVacancies > 0 ? (int) (((double) offerCount / totalVacancies) * 100) : 0;
        int rejectedPercentage = totalVacancies > 0 ? (int) (((double) rejectedCount / totalVacancies) * 100) : 0;

        // Build the model using ModelBuilder
        ModelBuilder.of(model)
                // Add pagination data
                .addAttribute("vacancyResponses", vacancyResponsePage.getContent())
                .addAttribute("currentPage", page)
                .addAttribute("totalPages", vacancyResponsePage.getTotalPages())
                .addAttribute("totalItems", vacancyResponsePage.getTotalElements())
                .addAttribute("size", size)

                // Add vacancy count
                .addAttribute("vacancyCount", allVacancyResponses.size())

                // Add counts for each stage
                .addAttribute("appliedCount", appliedCount)
                .addAttribute("phoneInterviewCount", phoneInterviewCount)
                .addAttribute("technicalInterviewCount", technicalInterviewCount)
                .addAttribute("finalInterviewCount", finalInterviewCount)
                .addAttribute("offerCount", offerCount)
                .addAttribute("rejectedCount", rejectedCount)

                // Add percentages
                .addAttribute("appliedPercentage", appliedPercentage)
                .addAttribute("phoneInterviewPercentage", phoneInterviewPercentage)
                .addAttribute("technicalInterviewPercentage", technicalInterviewPercentage)
                .addAttribute("finalInterviewPercentage", finalInterviewPercentage)
                .addAttribute("offerPercentage", offerPercentage)
                .addAttribute("rejectedPercentage", rejectedPercentage)
                .build();

        // Create a Page object with the DTOs
        return new PageImpl<>(vacancyResponseDTOs, pageable, vacancyResponsePage.getTotalElements());
    }

    @Override
    public void setDashboardPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Work Dashboard")
                .build();
    }
}
