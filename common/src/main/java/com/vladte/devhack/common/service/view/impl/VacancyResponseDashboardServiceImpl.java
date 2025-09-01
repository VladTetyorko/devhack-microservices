package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.common.service.view.VacancyResponseDashboardService;
import com.vladte.devhack.domain.entities.global.InterviewStage;
import com.vladte.devhack.domain.entities.global.InterviewStageCategory;
import com.vladte.devhack.domain.entities.personalized.VacancyResponse;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.model.dto.personalized.VacancyResponseDTO;
import com.vladte.devhack.domain.model.mapper.personalized.VacancyResponseMapper;
import com.vladte.devhack.domain.service.global.InterviewStageCategoryService;
import com.vladte.devhack.domain.service.global.InterviewStageService;
import com.vladte.devhack.domain.service.personalized.VacancyResponseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the VacancyResponseDashboardService interface.
 * This class prepares the model for the vacancy response dashboard view.
 */
@Service
public class VacancyResponseDashboardServiceImpl implements VacancyResponseDashboardService {

    private final VacancyResponseService vacancyResponseService;
    private final VacancyResponseMapper vacancyResponseMapper;
    private final InterviewStageService interviewStageService;
    private final InterviewStageCategoryService interviewStageCategoryService;

    public VacancyResponseDashboardServiceImpl(VacancyResponseService vacancyResponseService,
                                               VacancyResponseMapper vacancyResponseMapper,
                                               InterviewStageService interviewStageService,
                                               InterviewStageCategoryService interviewStageCategoryService) {
        this.vacancyResponseService = vacancyResponseService;
        this.vacancyResponseMapper = vacancyResponseMapper;
        this.interviewStageService = interviewStageService;
        this.interviewStageCategoryService = interviewStageCategoryService;
    }


    @Override
    public void prepareDashboardModel(int page, int size, Model model) {

        // Get all vacancy responses with pagination for the "Top Companies" section
        Pageable pageable = PageRequest.of(page, size);
        Page<VacancyResponse> entityPage = vacancyResponseService.findAll(pageable);

        // 2) convert to Page<DTO> in one line:
        Page<VacancyResponseDTO> dtoPage = entityPage.map(vacancyResponseMapper::toDTO);

        // Get all vacancy responses for statistics (no pagination needed for statistics)
        List<VacancyResponse> allVacancyResponses = vacancyResponseService.findAll();

        // Count vacancies by interview stage
        Map<InterviewStage, Long> stageCountMap = allVacancyResponses.stream()
                .filter(vr -> vr.getInterviewStage() != null)
                .collect(Collectors.groupingBy(VacancyResponse::getInterviewStage, Collectors.counting()));

        // Get stages by code
        InterviewStage appliedStage = interviewStageService.findByCode("APPLIED").orElse(null);
        InterviewStage screeningStage = interviewStageService.findByCode("SCREENING").orElse(null);
        InterviewStage technicalStage = interviewStageService.findByCode("TECHNICAL_INTERVIEW").orElse(null);
        InterviewStage stakeholderStage = interviewStageService.findByCode("STAKEHOLDER_INTERVIEW").orElse(null);
        InterviewStage offerStage = interviewStageService.findByCode("OFFER").orElse(null);
        InterviewStage rejectedStage = interviewStageService.findByCode("REJECTED").orElse(null);

        // Get counts for each stage
        long appliedCount = appliedStage != null ? stageCountMap.getOrDefault(appliedStage, 0L) : 0L;
        long phoneInterviewCount = screeningStage != null ? stageCountMap.getOrDefault(screeningStage, 0L) : 0L;
        long technicalInterviewCount = technicalStage != null ? stageCountMap.getOrDefault(technicalStage, 0L) : 0L;
        long finalInterviewCount = stakeholderStage != null ? stageCountMap.getOrDefault(stakeholderStage, 0L) : 0L;
        long offerCount = offerStage != null ? stageCountMap.getOrDefault(offerStage, 0L) : 0L;
        long rejectedCount = rejectedStage != null ? stageCountMap.getOrDefault(rejectedStage, 0L) : 0L;

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
                .addAttribute("vacancyResponses", dtoPage.getContent())
                .addAttribute("currentPage", dtoPage.getNumber())
                .addAttribute("totalPages", dtoPage.getTotalPages())
                .addAttribute("totalItems", dtoPage.getTotalElements())
                .addAttribute("size", dtoPage.getSize())

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
    }

    @Override
    public void setDashboardPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Work Dashboard")
                .build();
    }

    @Override
    public void prepareBoardModel(Model model, User currentUser, Integer stageIndex) {
        // Get all vacancy responses
        List<VacancyResponse> allVacancyResponses = vacancyResponseService.findVacancyResponsesByStageForUser(currentUser, stageIndex);

        // Group vacancy responses by interview stage
        Map<InterviewStage, List<VacancyResponseDTO>> responsesByStage = allVacancyResponses.stream()
                .filter(vr -> vr.getInterviewStage() != null)
                .collect(Collectors.groupingBy(
                        VacancyResponse::getInterviewStage,
                        Collectors.mapping(vacancyResponseMapper::toDTO, Collectors.toList())
                ));

        // Get all active stages ordered by orderIndex
        List<InterviewStage> allActiveStages = interviewStageService.findAllActiveOrderByOrderIndex();

        // Group stages by category and maintain order
        Map<InterviewStageCategory, List<InterviewStage>> stagesByCategory = allActiveStages.stream()
                .filter(stage -> stage.getCategory() != null)
                .collect(Collectors.groupingBy(
                        InterviewStage::getCategory,
                        LinkedHashMap::new, // Preserve insertion order
                        Collectors.toList()
                ));

        // Create flat list of stages for stage-by-stage navigation
        List<Map<String, Object>> boardStages = new ArrayList<>();

        for (Map.Entry<InterviewStageCategory, List<InterviewStage>> categoryEntry : stagesByCategory.entrySet()) {
            InterviewStageCategory category = categoryEntry.getKey();
            List<InterviewStage> categoryStages = categoryEntry.getValue();

            // Sort stages within category by orderIndex
            categoryStages.sort(Comparator.comparing(InterviewStage::getOrderIndex,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            // Add each stage to the flat list
            for (InterviewStage stage : categoryStages) {
                Map<String, Object> stageData = new HashMap<>();
                stageData.put("id", stage.getId());
                stageData.put("code", stage.getCode());
                stageData.put("label", stage.getLabel());
                stageData.put("orderIndex", stage.getOrderIndex());
                stageData.put("colorClass", stage.getColorClass() != null ? stage.getColorClass() : "secondary");
                stageData.put("iconClass", stage.getIconClass() != null ? stage.getIconClass() : "fas fa-circle");
                stageData.put("responses", responsesByStage.getOrDefault(stage, new ArrayList<>()));

                // Add category information
                Map<String, Object> categoryInfo = new HashMap<>();
                categoryInfo.put("id", category.getId());
                categoryInfo.put("code", category.getCode());
                categoryInfo.put("label", category.getLabel());
                stageData.put("category", categoryInfo);

                boardStages.add(stageData);
            }
        }

        // Add model attributes
        ModelBuilder.of(model)
                .addAttribute("boardStages", boardStages)
                .addAttribute("allStages", allActiveStages)
                .setPageTitle("Job Applications Board")
                .build();
    }

    @Override
    public void prepareBoardModelByCategory(Model model, User currentUser, String categoryCode) {
        // Get all categories ordered by orderIndex
        List<InterviewStageCategory> allCategories = interviewStageCategoryService.findAll().stream()
                .sorted(Comparator.comparing(InterviewStageCategory::getOrderIndex,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

        // Determine current category
        InterviewStageCategory currentCategory;
        if (categoryCode != null && !categoryCode.isEmpty()) {
            currentCategory = interviewStageCategoryService.findByCode(categoryCode).orElse(null);
            if (currentCategory == null && !allCategories.isEmpty()) {
                currentCategory = allCategories.getFirst(); // Fallback to first category
            }
        } else {
            currentCategory = !allCategories.isEmpty() ? allCategories.getFirst() : null;
        }

        if (currentCategory == null) {
            // No categories available
            ModelBuilder.of(model)
                    .addAttribute("currentCategory", null)
                    .addAttribute("allCategories", allCategories)
                    .addAttribute("categoryStages", new ArrayList<>())
                    .addAttribute("hasNextCategory", false)
                    .addAttribute("hasPreviousCategory", false)
                    .addAttribute("nextCategoryCode", null)
                    .addAttribute("previousCategoryCode", null)
                    .setPageTitle("Job Applications Board")
                    .build();
            return;
        }

        // Get all vacancy responses for the current user
        List<VacancyResponse> allVacancyResponses = vacancyResponseService.findAll().stream()
                .filter(vr -> vr.getUser() != null && vr.getUser().equals(currentUser))
                .toList();

        // Group vacancy responses by interview stage
        Map<InterviewStage, List<VacancyResponseDTO>> responsesByStage = allVacancyResponses.stream()
                .filter(vr -> vr.getInterviewStage() != null)
                .collect(Collectors.groupingBy(
                        VacancyResponse::getInterviewStage,
                        Collectors.mapping(vacancyResponseMapper::toDTO, Collectors.toList())
                ));

        // Get stages for the current category
        List<InterviewStage> categoryStages = interviewStageService.findByCategoryCode(currentCategory.getCode())
                .stream()
                .filter(stage -> stage.getActive() != null && stage.getActive())
                .sorted(Comparator.comparing(InterviewStage::getOrderIndex,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        // Create stage data for the current category
        List<Map<String, Object>> stagesData = new ArrayList<>();
        for (InterviewStage stage : categoryStages) {
            Map<String, Object> stageData = new HashMap<>();
            stageData.put("id", stage.getId());
            stageData.put("code", stage.getCode());
            stageData.put("label", stage.getLabel());
            stageData.put("orderIndex", stage.getOrderIndex());
            stageData.put("colorClass", stage.getColorClass() != null ? stage.getColorClass() : "secondary");
            stageData.put("iconClass", stage.getIconClass() != null ? stage.getIconClass() : "fas fa-circle");
            stageData.put("responses", responsesByStage.getOrDefault(stage, new ArrayList<>()));
            stagesData.add(stageData);
        }

        // Determine navigation information
        int currentCategoryIndex = allCategories.indexOf(currentCategory);
        boolean hasNextCategory = currentCategoryIndex < allCategories.size() - 1;
        boolean hasPreviousCategory = currentCategoryIndex > 0;
        String nextCategoryCode = hasNextCategory ? allCategories.get(currentCategoryIndex + 1).getCode() : null;
        String previousCategoryCode = hasPreviousCategory ? allCategories.get(currentCategoryIndex - 1).getCode() : null;

        // Add model attributes
        ModelBuilder.of(model)
                .addAttribute("currentCategory", currentCategory)
                .addAttribute("allCategories", allCategories)
                .addAttribute("categoryStages", stagesData)
                .addAttribute("hasNextCategory", hasNextCategory)
                .addAttribute("hasPreviousCategory", hasPreviousCategory)
                .addAttribute("nextCategoryCode", nextCategoryCode)
                .addAttribute("previousCategoryCode", previousCategoryCode)
                .addAttribute("currentCategoryIndex", currentCategoryIndex + 1)
                .addAttribute("totalCategories", allCategories.size())
                .setPageTitle("Job Applications Board")
                .build();
    }
}
