package com.vladte.devhack.common.service.domain;

import com.vladte.devhack.entities.User;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * Service interface for dashboard statistics and metrics.
 * This interface follows the Single Responsibility Principle by focusing only on dashboard-related operations.
 */
public interface DashboardService {

    /**
     * Get the total count of questions.
     *
     * @return the total number of questions
     */
    int getQuestionCount();

    /**
     * Get the count of questions created by a specific user.
     *
     * @param user the user
     * @return the number of questions created by the user
     */
    int getQuestionCountByUser(User user);

    /**
     * Get the total count of answers.
     *
     * @return the total number of answers
     */
    int getAnswerCount();

    /**
     * Get the count of answers created by a specific user.
     *
     * @param user the user
     * @return the number of answers created by the user
     */
    int getAnswerCountByUser(User user);

    /**
     * Get the total count of notes.
     *
     * @return the total number of notes
     */
    int getNoteCount();

    /**
     * Get the count of notes created by a specific user.
     *
     * @param user the user
     * @return the number of notes created by the user
     */
    int getNoteCountByUser(User user);

    /**
     * Get the total count of tags.
     *
     * @return the total number of tags
     */
    int getTagCount();

    /**
     * Get the count of tags used by a specific user in their questions.
     *
     * @param user the user
     * @return the number of tags used by the user
     */
    int getTagCountByUser(User user);

    /**
     * Calculate progress percentages for questions, answers, notes, and tags.
     *
     * @return a map containing progress percentages for different metrics
     */
    Map<String, Integer> calculateProgressPercentages();

    /**
     * Calculate progress percentages for questions, answers, notes, and tags for a specific user.
     *
     * @param user the user
     * @return a map containing progress percentages for different metrics for the user
     */
    Map<String, Integer> calculateProgressPercentagesByUser(User user);

    /**
     * Get counts of questions by difficulty level.
     *
     * @return a map with difficulty levels as keys and counts as values
     */
    Map<String, Long> getQuestionCountsByDifficulty();

    /**
     * Get counts of questions by difficulty level for a specific user.
     *
     * @param user the user
     * @return a map with difficulty levels as keys and counts as values for the user
     */
    Map<String, Long> getQuestionCountsByDifficultyAndUser(User user);

    /**
     * Get counts of answers by difficulty level of the associated questions.
     *
     * @return a map with difficulty levels as keys and counts as values
     */
    Map<String, Long> getAnswerCountsByDifficulty();

    /**
     * Get counts of answers by difficulty level of the associated questions for a specific user.
     *
     * @param user the user
     * @return a map with difficulty levels as keys and counts as values for the user
     */
    Map<String, Long> getAnswerCountsByDifficultyAndUser(User user);

    /**
     * Calculate the percentage of questions answered by difficulty level.
     *
     * @return a map with difficulty levels as keys and percentages as values
     */
    Map<String, Integer> calculateAnswerPercentagesByDifficulty();

    /**
     * Calculate the percentage of questions answered by difficulty level for a specific user.
     *
     * @param user the user
     * @return a map with difficulty levels as keys and percentages as values for the user
     */
    Map<String, Integer> calculateAnswerPercentagesByDifficultyAndUser(User user);

    /**
     * Calculate progress statistics for each tag.
     *
     * @return a map containing tag IDs mapped to their statistics
     */
    Map<UUID, TagProgress> calculateTagProgress();

    /**
     * Calculate progress statistics for each tag for a specific user.
     *
     * @param user the user
     * @return a map containing tag IDs mapped to their statistics for the user
     */
    Map<UUID, TagProgress> calculateTagProgressByUser(User user);

    /**
     * Class to hold progress statistics for a tag.
     */
    @Getter
    class TagProgress {
        private final int questionCount;
        private final int answerCount;
        private final int progressPercentage;

        public TagProgress(int questionCount, int answerCount, int progressPercentage) {
            this.questionCount = questionCount;
            this.answerCount = answerCount;
            this.progressPercentage = progressPercentage;
        }
    }
}
