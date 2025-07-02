package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.service.domain.*;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import com.vladte.devhack.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of the DashboardService interface.
 * This class follows the Single Responsibility Principle by focusing only on dashboard-related operations.
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final InterviewQuestionService questionService;
    private final AnswerService answerService;
    private final NoteService noteService;
    private final TagService tagService;

    @Autowired
    public DashboardServiceImpl(
            InterviewQuestionService questionService,
            AnswerService answerService,
            NoteService noteService,
            TagService tagService) {
        this.questionService = questionService;
        this.answerService = answerService;
        this.noteService = noteService;
        this.tagService = tagService;
    }

    @Override
    public int getQuestionCount() {
        return questionService.countAllQuestions();
    }

    @Override
    public int getQuestionCountByUser(User user) {
        if (user == null) {
            return 0;
        }
        return questionService.countQuestionsByUser(user);
    }

    @Override
    public int getAnswerCount() {
        return answerService.findAll().size();
    }

    @Override
    public int getAnswerCountByUser(User user) {
        if (user == null) {
            return 0;
        }
        return answerService.findAnswersByUser(user).size();
    }

    @Override
    public int getNoteCount() {
        return noteService.findAll().size();
    }

    @Override
    public int getNoteCountByUser(User user) {
        if (user == null) {
            return 0;
        }
        return noteService.findNotesByUser(user).size();
    }

    @Override
    public int getTagCount() {
        return tagService.countAllTags();
    }

    @Override
    public int getTagCountByUser(User user) {
        if (user == null) {
            return 0;
        }
        return tagService.countTagsByUser(user);
    }

    @Override
    public Map<String, Integer> calculateProgressPercentages() {
        Map<String, Integer> progressMap = new HashMap<>();

        int questionTotal = Math.max(100, getQuestionCount()); // Assume 100 questions total
        int answerProgress = Math.min(100, (int) (((double) getAnswerCount() / questionTotal) * 100));
        int noteProgress = Math.min(100, (int) (((double) getNoteCount() / questionTotal) * 100));
        int tagProgress = Math.min(100, (int) (((double) getTagCount() / 20) * 100)); // Assume 20 tags total

        progressMap.put("questionProgress", getQuestionCount() == 0 ? 0 : 100);
        progressMap.put("answerProgress", answerProgress);
        progressMap.put("noteProgress", noteProgress);
        progressMap.put("tagProgress", tagProgress);

        return progressMap;
    }

    @Override
    public Map<String, Integer> calculateProgressPercentagesByUser(User user) {
        if (user == null) {
            return new HashMap<>();
        }

        Map<String, Integer> progressMap = new HashMap<>();

        int questionTotal = Math.max(100, getQuestionCountByUser(user)); // Assume 100 questions total
        int answerProgress = Math.min(100, (int) (((double) getAnswerCountByUser(user) / questionTotal) * 100));
        int noteProgress = Math.min(100, (int) (((double) getNoteCountByUser(user) / questionTotal) * 100));
        int tagProgress = Math.min(100, (int) (((double) getTagCountByUser(user) / 20) * 100)); // Assume 20 tags total

        progressMap.put("questionProgress", getQuestionCountByUser(user) == 0 ? 0 : 100);
        progressMap.put("answerProgress", answerProgress);
        progressMap.put("noteProgress", noteProgress);
        progressMap.put("tagProgress", tagProgress);

        return progressMap;
    }

    @Override
    public Map<String, Long> getQuestionCountsByDifficulty() {
        List<InterviewQuestion> questions = questionService.findAll();

        Map<String, Long> countsByDifficulty = new HashMap<>();
        countsByDifficulty.put("Easy", questions.stream().filter(q -> "Easy".equals(q.getDifficulty())).count());
        countsByDifficulty.put("Medium", questions.stream().filter(q -> "Medium".equals(q.getDifficulty())).count());
        countsByDifficulty.put("Hard", questions.stream().filter(q -> "Hard".equals(q.getDifficulty())).count());

        return countsByDifficulty;
    }

    @Override
    public Map<String, Long> getQuestionCountsByDifficultyAndUser(User user) {
        if (user == null) {
            return new HashMap<>();
        }

        List<InterviewQuestion> questions = questionService.findAll();

        Map<String, Long> countsByDifficulty = new HashMap<>();
        countsByDifficulty.put("Easy", questions.stream()
                .filter(q -> "Easy".equals(q.getDifficulty()) && q.getUser() != null && q.getUser().getId().equals(user.getId()))
                .count());
        countsByDifficulty.put("Medium", questions.stream()
                .filter(q -> "Medium".equals(q.getDifficulty()) && q.getUser() != null && q.getUser().getId().equals(user.getId()))
                .count());
        countsByDifficulty.put("Hard", questions.stream()
                .filter(q -> "Hard".equals(q.getDifficulty()) && q.getUser() != null && q.getUser().getId().equals(user.getId()))
                .count());

        return countsByDifficulty;
    }

    @Override
    public Map<String, Long> getAnswerCountsByDifficulty() {
        List<Answer> answers = answerService.findAll();

        Map<String, Long> countsByDifficulty = new HashMap<>();
        countsByDifficulty.put("Easy", answers.stream()
                .filter(a -> a.getQuestion() != null && "Easy".equals(a.getQuestion().getDifficulty()))
                .count());
        countsByDifficulty.put("Medium", answers.stream()
                .filter(a -> a.getQuestion() != null && "Medium".equals(a.getQuestion().getDifficulty()))
                .count());
        countsByDifficulty.put("Hard", answers.stream()
                .filter(a -> a.getQuestion() != null && "Hard".equals(a.getQuestion().getDifficulty()))
                .count());

        return countsByDifficulty;
    }

    @Override
    public Map<String, Long> getAnswerCountsByDifficultyAndUser(User user) {
        if (user == null) {
            return new HashMap<>();
        }

        List<Answer> answers = answerService.findAnswersByUser(user);

        Map<String, Long> countsByDifficulty = new HashMap<>();
        countsByDifficulty.put("Easy", answers.stream()
                .filter(a -> a.getQuestion() != null && "Easy".equals(a.getQuestion().getDifficulty()))
                .count());
        countsByDifficulty.put("Medium", answers.stream()
                .filter(a -> a.getQuestion() != null && "Medium".equals(a.getQuestion().getDifficulty()))
                .count());
        countsByDifficulty.put("Hard", answers.stream()
                .filter(a -> a.getQuestion() != null && "Hard".equals(a.getQuestion().getDifficulty()))
                .count());

        return countsByDifficulty;
    }

    @Override
    public Map<String, Integer> calculateAnswerPercentagesByDifficulty() {
        Map<String, Long> questionCounts = getQuestionCountsByDifficulty();
        Map<String, Long> answerCounts = getAnswerCountsByDifficulty();

        Map<String, Integer> percentages = new HashMap<>();
        percentages.put("Easy",
                questionCounts.get("Easy") > 0 ? (int) (((double) answerCounts.get("Easy") / questionCounts.get("Easy")) * 100) : 0);
        percentages.put("Medium",
                questionCounts.get("Medium") > 0 ? (int) (((double) answerCounts.get("Medium") / questionCounts.get("Medium")) * 100) : 0);
        percentages.put("Hard",
                questionCounts.get("Hard") > 0 ? (int) (((double) answerCounts.get("Hard") / questionCounts.get("Hard")) * 100) : 0);

        return percentages;
    }

    @Override
    public Map<String, Integer> calculateAnswerPercentagesByDifficultyAndUser(User user) {
        if (user == null) {
            return new HashMap<>();
        }

        Map<String, Long> questionCounts = getQuestionCountsByDifficultyAndUser(user);
        Map<String, Long> answerCounts = getAnswerCountsByDifficultyAndUser(user);

        Map<String, Integer> percentages = new HashMap<>();
        percentages.put("Easy",
                questionCounts.get("Easy") > 0 ? (int) (((double) answerCounts.get("Easy") / questionCounts.get("Easy")) * 100) : 0);
        percentages.put("Medium",
                questionCounts.get("Medium") > 0 ? (int) (((double) answerCounts.get("Medium") / questionCounts.get("Medium")) * 100) : 0);
        percentages.put("Hard",
                questionCounts.get("Hard") > 0 ? (int) (((double) answerCounts.get("Hard") / questionCounts.get("Hard")) * 100) : 0);

        return percentages;
    }

    @Override
    public Map<UUID, TagProgress> calculateTagProgress() {
        List<Tag> tags = tagService.findAll();
        List<InterviewQuestion> questions = questionService.findAll();
        List<Answer> answers = answerService.findAll();

        Map<UUID, TagProgress> tagProgressMap = new HashMap<>();

        for (Tag tag : tags) {
            // Count questions with this tag
            long questionsWithTag = questions.stream()
                    .filter(q -> q.getTags().contains(tag))
                    .count();

            // Count answers for questions with this tag
            long answersForTag = answers.stream()
                    .filter(a -> a.getQuestion() != null && a.getQuestion().getTags().contains(tag))
                    .count();

            // Calculate percentage
            int tagAnswerPercentage = questionsWithTag > 0
                    ? (int) (((double) answersForTag / questionsWithTag) * 100)
                    : 0;

            // Create TagProgress object
            TagProgress progress = new TagProgress(
                    (int) questionsWithTag,
                    (int) answersForTag,
                    tagAnswerPercentage
            );

            tagProgressMap.put(tag.getId(), progress);
        }

        return tagProgressMap;
    }

    @Override
    public Map<UUID, TagProgress> calculateTagProgressByUser(User user) {
        if (user == null) {
            return new HashMap<>();
        }

        List<Tag> tags = tagService.findAll();
        List<InterviewQuestion> questions = questionService.findAll();
        List<Answer> answers = answerService.findAnswersByUser(user);

        Map<UUID, TagProgress> tagProgressMap = new HashMap<>();

        for (Tag tag : tags) {
            // Count questions with this tag created by the user
            long questionsWithTag = questions.stream()
                    .filter(q -> q.getTags().contains(tag) && q.getUser() != null && q.getUser().getId().equals(user.getId()))
                    .count();

            // Count answers by the user for questions with this tag
            long answersForTag = answers.stream()
                    .filter(a -> a.getQuestion() != null && a.getQuestion().getTags().contains(tag))
                    .count();

            // Calculate percentage
            int tagAnswerPercentage = questionsWithTag > 0
                    ? (int) (((double) answersForTag / questionsWithTag) * 100)
                    : 0;

            // Create TagProgress object
            TagProgress progress = new TagProgress(
                    (int) questionsWithTag,
                    (int) answersForTag,
                    tagAnswerPercentage
            );

            tagProgressMap.put(tag.getId(), progress);
        }

        return tagProgressMap;
    }

    @Override
    public List<Tag> getAllTags() {
        return tagService.findAll();
    }
}
