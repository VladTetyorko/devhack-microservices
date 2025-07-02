package com.vladte.devhack.common.service.domain;

import com.vladte.devhack.entities.Tag;

import java.util.List;
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
     * Get the total count of answers.
     *
     * @return the total number of answers
     */
    int getAnswerCount();

    /**
     * Get the total count of notes.
     *
     * @return the total number of notes
     */
    int getNoteCount();

    /**
     * Get the total count of tags.
     *
     * @return the total number of tags
     */
    int getTagCount();

    /**
     * Calculate progress percentages for questions, answers, notes, and tags.
     *
     * @return a map containing progress percentages for different metrics
     */
    Map<String, Integer> calculateProgressPercentages();

    /**
     * Get counts of questions by difficulty level.
     *
     * @return a map with difficulty levels as keys and counts as values
     */
    Map<String, Long> getQuestionCountsByDifficulty();

    /**
     * Get counts of answers by difficulty level of the associated questions.
     *
     * @return a map with difficulty levels as keys and counts as values
     */
    Map<String, Long> getAnswerCountsByDifficulty();

    /**
     * Calculate the percentage of questions answered by difficulty level.
     *
     * @return a map with difficulty levels as keys and percentages as values
     */
    Map<String, Integer> calculateAnswerPercentagesByDifficulty();

    /**
     * Calculate progress statistics for each tag.
     *
     * @return a map containing tag IDs mapped to their statistics
     */
    Map<UUID, TagProgress> calculateTagProgress();

    /**
     * Get all tags.
     *
     * @return a list of all tags
     */
    List<Tag> getAllTags();

    /**
     * Class to hold progress statistics for a tag.
     */
    class TagProgress {
        private final int questionCount;
        private final int answerCount;
        private final int progressPercentage;

        public TagProgress(int questionCount, int answerCount, int progressPercentage) {
            this.questionCount = questionCount;
            this.answerCount = answerCount;
            this.progressPercentage = progressPercentage;
        }

        public int getQuestionCount() {
            return questionCount;
        }

        public int getAnswerCount() {
            return answerCount;
        }

        public int getProgressPercentage() {
            return progressPercentage;
        }
    }
}
