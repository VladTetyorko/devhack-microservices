package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.common.service.statistics.DashboardService;
import com.vladte.devhack.common.service.view.DashboardViewService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.User;
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
        Optional<User> currentUserOpt = getCurrentUser();

        SystemStatistics systemStats = collectSystemStatistics();
        UserStatistics userStats = currentUserOpt.map(this::collectUserStatistics)
                .orElse(new UserStatistics());

        ModelBuilder modelBuilder = buildSystemStatisticsModel(ModelBuilder.of(model), systemStats);

        if (currentUserOpt.isPresent()) {
            modelBuilder = buildUserStatisticsModel(modelBuilder, userStats);
        }

        modelBuilder.build();
    }

    private record SystemStatistics(
            Map<String, Integer> progressPercentages,
            Map<String, Long> questionCountsByDifficulty,
            Map<String, Long> answerCountsByDifficulty,
            Map<String, Integer> answerPercentagesByDifficulty,
            Map<UUID, TagProgressMaps> tagProgressMaps
    ) {
    }

    private record UserStatistics(
            int questionCount,
            int answerCount,
            int noteCount,
            int tagCount,
            Map<String, Integer> progressPercentages,
            Map<String, Long> questionCountsByDifficulty,
            Map<String, Long> answerCountsByDifficulty,
            Map<String, Integer> answerPercentagesByDifficulty,
            Map<UUID, TagProgressMaps> tagProgressMaps
    ) {
        UserStatistics() {
            this(0, 0, 0, 0, new HashMap<>(), new HashMap<>(),
                    new HashMap<>(), new HashMap<>(), new HashMap<>());
        }
    }

    private record TagProgressMaps(
            Map<UUID, Integer> questionCounts,
            Map<UUID, Integer> answerCounts,
            Map<UUID, Integer> progressPercentages
    ) {
    }

    private SystemStatistics collectSystemStatistics() {
        Map<String, Integer> progressPercentages = dashboardService.calculateProgressPercentages();
        Map<String, Long> questionCountsByDifficulty = dashboardService.getQuestionCountsByDifficulty();
        Map<String, Long> answerCountsByDifficulty = dashboardService.getAnswerCountsByDifficulty();
        Map<String, Integer> answerPercentagesByDifficulty = dashboardService.calculateAnswerPercentagesByDifficulty();
        Map<UUID, DashboardService.TagProgress> tagProgress = dashboardService.calculateTagProgress();

        return new SystemStatistics(
                progressPercentages,
                questionCountsByDifficulty,
                answerCountsByDifficulty,
                answerPercentagesByDifficulty,
                mapTagProgress(tagProgress)
        );
    }

    private UserStatistics collectUserStatistics(User user) {
        return new UserStatistics(
                dashboardService.getQuestionCountByUser(user),
                dashboardService.getAnswerCountByUser(user),
                dashboardService.getNoteCountByUser(user),
                dashboardService.getTagCountByUser(user),
                dashboardService.calculateProgressPercentagesByUser(user),
                dashboardService.getQuestionCountsByDifficultyAndUser(user),
                dashboardService.getAnswerCountsByDifficultyAndUser(user),
                dashboardService.calculateAnswerPercentagesByDifficultyAndUser(user),
                mapTagProgress(dashboardService.calculateTagProgressByUser(user))
        );
    }

    private Map<UUID, TagProgressMaps> mapTagProgress(Map<UUID, DashboardService.TagProgress> tagProgress) {
        Map<UUID, TagProgressMaps> result = new HashMap<>();
        Map<UUID, Integer> questionCounts = new HashMap<>();
        Map<UUID, Integer> answerCounts = new HashMap<>();
        Map<UUID, Integer> progressPercentages = new HashMap<>();

        for (Map.Entry<UUID, DashboardService.TagProgress> entry : tagProgress.entrySet()) {
            UUID tagId = entry.getKey();
            DashboardService.TagProgress progress = entry.getValue();

            questionCounts.put(tagId, progress.getQuestionCount());
            answerCounts.put(tagId, progress.getAnswerCount());
            progressPercentages.put(tagId, progress.getProgressPercentage());
        }

        result.put(UUID.randomUUID(), new TagProgressMaps(questionCounts, answerCounts, progressPercentages));
        return result;
    }

    private ModelBuilder buildSystemStatisticsModel(ModelBuilder modelBuilder, SystemStatistics stats) {
        // Extract tag progress data from the nested structure
        Map<UUID, Integer> tagProgressPercentages = new HashMap<>();
        Map<UUID, Integer> tagAnswerCounts = new HashMap<>();
        Map<UUID, Integer> tagQuestionCounts = new HashMap<>();

        // Get the first (and only) TagProgressMaps entry
        if (!stats.tagProgressMaps().isEmpty()) {
            TagProgressMaps tagProgressMaps = stats.tagProgressMaps().values().iterator().next();
            tagProgressPercentages.putAll(tagProgressMaps.progressPercentages());
            tagAnswerCounts.putAll(tagProgressMaps.answerCounts());
            tagQuestionCounts.putAll(tagProgressMaps.questionCounts());
        }

        return modelBuilder
                .addAttribute("questionCount", dashboardService.getQuestionCount())
                .addAttribute("answerCount", dashboardService.getAnswerCount())
                .addAttribute("noteCount", dashboardService.getNoteCount())
                .addAttribute("tagCount", dashboardService.getTagCount())
                .addAttribute("questionProgress", stats.progressPercentages().get("questionProgress"))
                .addAttribute("answerProgress", stats.progressPercentages().get("answerProgress"))
                .addAttribute("noteProgress", stats.progressPercentages().get("noteProgress"))
                .addAttribute("tagProgress", stats.progressPercentages().get("tagProgress"))
                .addAttribute("easyQuestionCount", stats.questionCountsByDifficulty().get("Easy"))
                .addAttribute("mediumQuestionCount", stats.questionCountsByDifficulty().get("Medium"))
                .addAttribute("hardQuestionCount", stats.questionCountsByDifficulty().get("Hard"))
                .addAttribute("easyAnswerCount", stats.answerCountsByDifficulty().get("Easy"))
                .addAttribute("mediumAnswerCount", stats.answerCountsByDifficulty().get("Medium"))
                .addAttribute("hardAnswerCount", stats.answerCountsByDifficulty().get("Hard"))
                .addAttribute("easyAnswerPercentage", stats.answerPercentagesByDifficulty().get("Easy"))
                .addAttribute("mediumAnswerPercentage", stats.answerPercentagesByDifficulty().get("Medium"))
                .addAttribute("hardAnswerPercentage", stats.answerPercentagesByDifficulty().get("Hard"))
                .addAttribute("tagProgressPercentages", tagProgressPercentages)
                .addAttribute("tagAnswerCounts", tagAnswerCounts)
                .addAttribute("tagQuestionCounts", tagQuestionCounts);
    }

    private ModelBuilder buildUserStatisticsModel(ModelBuilder modelBuilder, UserStatistics stats) {
        return modelBuilder
                .addAttribute("userQuestionCount", stats.questionCount())
                .addAttribute("userAnswerCount", stats.answerCount())
                .addAttribute("userNoteCount", stats.noteCount())
                .addAttribute("userTagCount", stats.tagCount())
                .addAttribute("userQuestionProgress", stats.progressPercentages().get("questionProgress"))
                .addAttribute("userAnswerProgress", stats.progressPercentages().get("answerProgress"))
                .addAttribute("userNoteProgress", stats.progressPercentages().get("noteProgress"))
                .addAttribute("userTagProgress", stats.progressPercentages().get("tagProgress"))
                .addAttribute("userEasyQuestionCount", stats.questionCountsByDifficulty().get("Easy"))
                .addAttribute("userMediumQuestionCount", stats.questionCountsByDifficulty().get("Medium"))
                .addAttribute("userHardQuestionCount", stats.questionCountsByDifficulty().get("Hard"))
                .addAttribute("userEasyAnswerCount", stats.answerCountsByDifficulty().get("Easy"))
                .addAttribute("userMediumAnswerCount", stats.answerCountsByDifficulty().get("Medium"))
                .addAttribute("userHardAnswerCount", stats.answerCountsByDifficulty().get("Hard"))
                .addAttribute("userEasyAnswerPercentage", stats.answerPercentagesByDifficulty().get("Easy"))
                .addAttribute("userMediumAnswerPercentage", stats.answerPercentagesByDifficulty().get("Medium"))
                .addAttribute("userHardAnswerPercentage", stats.answerPercentagesByDifficulty().get("Hard"));
    }

    @Override
    public void setDashboardPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Study Dashboard")
                .build();
    }
}
