package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.domain.DashboardService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.view.DashboardViewService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the DashboardViewService interface.
 * This class prepares the model for the dashboard view.
 */
@Service
public class DashboardViewServiceImpl implements DashboardViewService {

    private final DashboardService dashboardService;
    private final UserService userService;

    @Autowired
    public DashboardViewServiceImpl(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    /**
     * Get the current authenticated user.
     *
     * @return an Optional containing the current user, or empty if not authenticated
     */
    private Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        String email = authentication.getName();
        return userService.findByEmail(email);
    }

    @Override
    public void prepareDashboardModel(Model model) {
        // Get the current user
        Optional<User> currentUserOpt = getCurrentUser();

        // Get system-wide counts and progress percentages
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

        // Initialize user-specific data
        Map<String, Integer> userProgressPercentages = new HashMap<>();
        Map<String, Long> userQuestionCountsByDifficulty = new HashMap<>();
        Map<String, Long> userAnswerCountsByDifficulty = new HashMap<>();
        Map<String, Integer> userAnswerPercentagesByDifficulty = new HashMap<>();
        Map<UUID, DashboardService.TagProgress> userTagProgress = new HashMap<>();
        Map<UUID, Integer> userTagQuestionCounts = new HashMap<>();
        Map<UUID, Integer> userTagAnswerCounts = new HashMap<>();
        Map<UUID, Integer> userTagProgressPercentages = new HashMap<>();

        int userQuestionCount = 0;
        int userAnswerCount = 0;
        int userNoteCount = 0;
        int userTagCount = 0;

        // If user is authenticated, get user-specific data
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();

            // Get user-specific counts
            userQuestionCount = dashboardService.getQuestionCountByUser(currentUser);
            userAnswerCount = dashboardService.getAnswerCountByUser(currentUser);
            userNoteCount = dashboardService.getNoteCountByUser(currentUser);
            userTagCount = dashboardService.getTagCountByUser(currentUser);

            // Get user-specific progress percentages
            userProgressPercentages = dashboardService.calculateProgressPercentagesByUser(currentUser);

            // Get user-specific question counts by difficulty
            userQuestionCountsByDifficulty = dashboardService.getQuestionCountsByDifficultyAndUser(currentUser);

            // Get user-specific answer counts by difficulty
            userAnswerCountsByDifficulty = dashboardService.getAnswerCountsByDifficultyAndUser(currentUser);

            // Get user-specific answer percentages by difficulty
            userAnswerPercentagesByDifficulty = dashboardService.calculateAnswerPercentagesByDifficultyAndUser(currentUser);

            // Get user-specific tag progress
            userTagProgress = dashboardService.calculateTagProgressByUser(currentUser);

            // Create user-specific maps for the view
            for (Map.Entry<UUID, DashboardService.TagProgress> entry : userTagProgress.entrySet()) {
                UUID tagId = entry.getKey();
                DashboardService.TagProgress progress = entry.getValue();

                userTagQuestionCounts.put(tagId, progress.getQuestionCount());
                userTagAnswerCounts.put(tagId, progress.getAnswerCount());
                userTagProgressPercentages.put(tagId, progress.getProgressPercentage());
            }
        }

        // Build the model using ModelBuilder
        ModelBuilder modelBuilder = ModelBuilder.of(model)
                // Add system-wide counts
                .addAttribute("questionCount", dashboardService.getQuestionCount())
                .addAttribute("answerCount", dashboardService.getAnswerCount())
                .addAttribute("noteCount", dashboardService.getNoteCount())
                .addAttribute("tagCount", dashboardService.getTagCount())

                // Add system-wide progress percentages
                .addAttribute("questionProgress", progressPercentages.get("questionProgress"))
                .addAttribute("answerProgress", progressPercentages.get("answerProgress"))
                .addAttribute("noteProgress", progressPercentages.get("noteProgress"))
                .addAttribute("tagProgress", progressPercentages.get("tagProgress"))

                // Add system-wide question counts by difficulty
                .addAttribute("easyQuestionCount", questionCountsByDifficulty.get("Easy"))
                .addAttribute("mediumQuestionCount", questionCountsByDifficulty.get("Medium"))
                .addAttribute("hardQuestionCount", questionCountsByDifficulty.get("Hard"))

                // Add system-wide answer counts by difficulty
                .addAttribute("easyAnswerCount", answerCountsByDifficulty.get("Easy"))
                .addAttribute("mediumAnswerCount", answerCountsByDifficulty.get("Medium"))
                .addAttribute("hardAnswerCount", answerCountsByDifficulty.get("Hard"))

                // Add system-wide answer percentages by difficulty
                .addAttribute("easyAnswerPercentage", answerPercentagesByDifficulty.get("Easy"))
                .addAttribute("mediumAnswerPercentage", answerPercentagesByDifficulty.get("Medium"))
                .addAttribute("hardAnswerPercentage", answerPercentagesByDifficulty.get("Hard"))

                // Pass the tags list and system-wide progress data to the view
                .addAttribute("allTags", dashboardService.getAllTags())
                .addAttribute("tagQuestionCounts", tagQuestionCounts)
                .addAttribute("tagAnswerCounts", tagAnswerCounts)
                .addAttribute("tagProgressPercentages", tagProgressPercentages);

        // Add user-specific data if user is authenticated
        if (currentUserOpt.isPresent()) {
            modelBuilder
                    // Add user-specific counts
                    .addAttribute("userQuestionCount", userQuestionCount)
                    .addAttribute("userAnswerCount", userAnswerCount)
                    .addAttribute("userNoteCount", userNoteCount)
                    .addAttribute("userTagCount", userTagCount)

                    // Add user-specific progress percentages
                    .addAttribute("userQuestionProgress", userProgressPercentages.get("questionProgress"))
                    .addAttribute("userAnswerProgress", userProgressPercentages.get("answerProgress"))
                    .addAttribute("userNoteProgress", userProgressPercentages.get("noteProgress"))
                    .addAttribute("userTagProgress", userProgressPercentages.get("tagProgress"))

                    // Add user-specific question counts by difficulty
                    .addAttribute("userEasyQuestionCount", userQuestionCountsByDifficulty.get("Easy"))
                    .addAttribute("userMediumQuestionCount", userQuestionCountsByDifficulty.get("Medium"))
                    .addAttribute("userHardQuestionCount", userQuestionCountsByDifficulty.get("Hard"))

                    // Add user-specific answer counts by difficulty
                    .addAttribute("userEasyAnswerCount", userAnswerCountsByDifficulty.get("Easy"))
                    .addAttribute("userMediumAnswerCount", userAnswerCountsByDifficulty.get("Medium"))
                    .addAttribute("userHardAnswerCount", userAnswerCountsByDifficulty.get("Hard"))

                    // Add user-specific answer percentages by difficulty
                    .addAttribute("userEasyAnswerPercentage", userAnswerPercentagesByDifficulty.get("Easy"))
                    .addAttribute("userMediumAnswerPercentage", userAnswerPercentagesByDifficulty.get("Medium"))
                    .addAttribute("userHardAnswerPercentage", userAnswerPercentagesByDifficulty.get("Hard"))

                    // Pass user-specific tag progress data to the view
                    .addAttribute("userTagQuestionCounts", userTagQuestionCounts)
                    .addAttribute("userTagAnswerCounts", userTagAnswerCounts)
                    .addAttribute("userTagProgressPercentages", userTagProgressPercentages);
        }

        modelBuilder.build();
    }

    @Override
    public void setDashboardPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Study Dashboard")
                .build();
    }
}
