package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.domain.DashboardService;
import com.vladte.devhack.common.service.view.DashboardViewService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the DashboardViewService interface.
 * This class prepares the model for the dashboard view.
 */
@Service
public class DashboardViewServiceImpl implements DashboardViewService {

    private final DashboardService dashboardService;

    @Autowired
    public DashboardViewServiceImpl(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public void prepareDashboardModel(Model model) {
        // Get counts and progress percentages
        Map<String, Integer> progressPercentages = dashboardService.calculateProgressPercentages();

        // Get question counts by difficulty
        Map<String, Long> questionCountsByDifficulty = dashboardService.getQuestionCountsByDifficulty();

        // Get answer counts by difficulty
        Map<String, Long> answerCountsByDifficulty = dashboardService.getAnswerCountsByDifficulty();

        // Get answer percentages by difficulty
        Map<String, Integer> answerPercentagesByDifficulty = dashboardService.calculateAnswerPercentagesByDifficulty();

        // Add tag progress data
        Map<UUID, DashboardService.TagProgress> tagProgress = dashboardService.calculateTagProgress();

        // Create maps for the view
        Map<UUID, Integer> tagQuestionCounts = new HashMap<>();
        Map<UUID, Integer> tagAnswerCounts = new HashMap<>();
        Map<UUID, Integer> tagProgressPercentages = new HashMap<>();

        for (Map.Entry<UUID, DashboardService.TagProgress> entry : tagProgress.entrySet()) {
            UUID tagId = entry.getKey();
            DashboardService.TagProgress progress = entry.getValue();

            tagQuestionCounts.put(tagId, progress.getQuestionCount());
            tagAnswerCounts.put(tagId, progress.getAnswerCount());
            tagProgressPercentages.put(tagId, progress.getProgressPercentage());
        }

        // Build the model using ModelBuilder
        ModelBuilder.of(model)
                // Add counts
                .addAttribute("questionCount", dashboardService.getQuestionCount())
                .addAttribute("answerCount", dashboardService.getAnswerCount())
                .addAttribute("noteCount", dashboardService.getNoteCount())
                .addAttribute("tagCount", dashboardService.getTagCount())

                // Add progress percentages
                .addAttribute("questionProgress", progressPercentages.get("questionProgress"))
                .addAttribute("answerProgress", progressPercentages.get("answerProgress"))
                .addAttribute("noteProgress", progressPercentages.get("noteProgress"))
                .addAttribute("tagProgress", progressPercentages.get("tagProgress"))

                // Add question counts by difficulty
                .addAttribute("easyQuestionCount", questionCountsByDifficulty.get("Easy"))
                .addAttribute("mediumQuestionCount", questionCountsByDifficulty.get("Medium"))
                .addAttribute("hardQuestionCount", questionCountsByDifficulty.get("Hard"))

                // Add answer counts by difficulty
                .addAttribute("easyAnswerCount", answerCountsByDifficulty.get("Easy"))
                .addAttribute("mediumAnswerCount", answerCountsByDifficulty.get("Medium"))
                .addAttribute("hardAnswerCount", answerCountsByDifficulty.get("Hard"))

                // Add answer percentages by difficulty
                .addAttribute("easyAnswerPercentage", answerPercentagesByDifficulty.get("Easy"))
                .addAttribute("mediumAnswerPercentage", answerPercentagesByDifficulty.get("Medium"))
                .addAttribute("hardAnswerPercentage", answerPercentagesByDifficulty.get("Hard"))

                // Pass the tags list and progress data to the view
                .addAttribute("allTags", dashboardService.getAllTags())
                .addAttribute("tagQuestionCounts", tagQuestionCounts)
                .addAttribute("tagAnswerCounts", tagAnswerCounts)
                .addAttribute("tagProgressPercentages", tagProgressPercentages)
                .build();
    }

    @Override
    public void setDashboardPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Study Dashboard")
                .build();
    }
}
