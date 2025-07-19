package com.vladte.devhack.common.service.statistics.impl;

import com.vladte.devhack.common.model.mapper.TagMapper;
import com.vladte.devhack.common.service.domain.global.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.global.TagService;
import com.vladte.devhack.common.service.domain.personalized.AnswerService;
import com.vladte.devhack.common.service.domain.personalized.NoteService;
import com.vladte.devhack.common.service.statistics.DashboardService;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import com.vladte.devhack.entities.User;
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

    private static final int MAX_PERCENTAGE = 100;
    private static final int MIN_COUNT = 1;
    private static final String EASY = "Easy";
    private static final String MEDIUM = "Medium";
    private static final String HARD = "Hard";
    private static final String QUESTION_PROGRESS = "questionProgress";
    private static final String ANSWER_PROGRESS = "answerProgress";
    private static final String NOTE_PROGRESS = "noteProgress";
    private static final String TAG_PROGRESS = "tagProgress";

    private final InterviewQuestionService questionService;
    private final AnswerService answerService;
    private final NoteService noteService;
    private final TagService tagService;
    private final TagMapper tagMapper;


    public DashboardServiceImpl(
            InterviewQuestionService questionService,
            AnswerService answerService,
            NoteService noteService,
            TagService tagService, TagMapper tagMapper) {
        this.questionService = questionService;
        this.answerService = answerService;
        this.noteService = noteService;
        this.tagService = tagService;
        this.tagMapper = tagMapper;
    }

    @Override
    public int getQuestionCount() {
        return questionService.countAllQuestions();
    }

    @Override
    public int getQuestionCountByUser(User user) {
        return validateUser(user) ? questionService.findAnsweredQuestionsByUser(user) : 0;
    }

    @Override
    public int getAnswerCount() {
        return answerService.findAll().size();
    }

    @Override
    public int getAnswerCountByUser(User user) {
        return validateUser(user) ? answerService.findAnswersByUser(user).size() : 0;
    }

    @Override
    public int getNoteCount() {
        return noteService.findAll().size();
    }

    @Override
    public int getNoteCountByUser(User user) {
        return validateUser(user) ? noteService.findNotesByUser(user).size() : 0;
    }

    @Override
    public int getTagCount() {
        return tagService.countAllTags();
    }

    @Override
    public int getTagCountByUser(User user) {
        return validateUser(user) ? tagService.countTagsByUser(user) : 0;
    }

    private boolean validateUser(User user) {
        return user != null;
    }

    @Override
    public Map<String, Integer> calculateProgressPercentages() {
        Map<String, Integer> progressMap = new HashMap<>();
        int questionTotal = Math.max(MIN_COUNT, getQuestionCount());

        progressMap.put(QUESTION_PROGRESS, getQuestionCount() == 0 ? 0 : MAX_PERCENTAGE);
        progressMap.put(ANSWER_PROGRESS, calculatePercentage(getAnswerCount(), questionTotal));
        progressMap.put(NOTE_PROGRESS, calculatePercentage(getNoteCount(), questionTotal));
        progressMap.put(TAG_PROGRESS, calculatePercentage(getTagCount(), questionTotal));

        return progressMap;
    }

    @Override
    public Map<String, Integer> calculateProgressPercentagesByUser(User user) {
        if (!validateUser(user)) {
            return new HashMap<>();
        }

        Map<String, Integer> progressMap = new HashMap<>();
        int questionTotal = Math.max(MIN_COUNT, getQuestionCountByUser(user));

        progressMap.put(QUESTION_PROGRESS, getQuestionCountByUser(user) == 0 ? 0 : MAX_PERCENTAGE);
        progressMap.put(ANSWER_PROGRESS, calculatePercentage(getAnswerCountByUser(user), questionTotal));
        progressMap.put(NOTE_PROGRESS, calculatePercentage(getNoteCountByUser(user), questionTotal));
        progressMap.put(TAG_PROGRESS, calculatePercentage(getTagCountByUser(user), questionTotal));

        return progressMap;
    }

    private int calculatePercentage(int count, int total) {
        return Math.min(MAX_PERCENTAGE, (int) (((double) count / total) * MAX_PERCENTAGE));
    }

    @Override
    public Map<String, Long> getQuestionCountsByDifficulty() {
        List<InterviewQuestion> questions = questionService.findAll();
        return countQuestionsByDifficulty(questions, q -> true);
    }

    @Override
    public Map<String, Long> getQuestionCountsByDifficultyAndUser(User user) {
        if (!validateUser(user)) {
            return new HashMap<>();
        }
        List<InterviewQuestion> questions = questionService.findAll();
        return countQuestionsByDifficulty(questions,
                q -> q.getUser() != null && q.getUser().getId().equals(user.getId()));
    }

    private Map<String, Long> countQuestionsByDifficulty(List<InterviewQuestion> questions,
                                                         java.util.function.Predicate<InterviewQuestion> userFilter) {
        Map<String, Long> countsByDifficulty = new HashMap<>();
        countsByDifficulty.put(EASY, getQuestionCountByDifficulty(questions, EASY, userFilter));
        countsByDifficulty.put(MEDIUM, getQuestionCountByDifficulty(questions, MEDIUM, userFilter));
        countsByDifficulty.put(HARD, getQuestionCountByDifficulty(questions, HARD, userFilter));
        return countsByDifficulty;
    }

    private long getQuestionCountByDifficulty(List<InterviewQuestion> questions, String difficulty,
                                              java.util.function.Predicate<InterviewQuestion> userFilter) {
        return questions.stream()
                .filter(q -> difficulty.equals(q.getDifficulty()))
                .filter(userFilter)
                .count();
    }

    @Override
    public Map<String, Long> getAnswerCountsByDifficulty() {
        List<Answer> answers = answerService.findAll();
        return countAnswersByDifficulty(answers);
    }

    @Override
    public Map<String, Long> getAnswerCountsByDifficultyAndUser(User user) {
        if (!validateUser(user)) {
            return new HashMap<>();
        }
        List<Answer> answers = answerService.findAnswersByUser(user);
        return countAnswersByDifficulty(answers);
    }

    private Map<String, Long> countAnswersByDifficulty(List<Answer> answers) {
        Map<String, Long> countsByDifficulty = new HashMap<>();
        countsByDifficulty.put(EASY, getAnswerCountByDifficulty(answers, EASY));
        countsByDifficulty.put(MEDIUM, getAnswerCountByDifficulty(answers, MEDIUM));
        countsByDifficulty.put(HARD, getAnswerCountByDifficulty(answers, HARD));
        return countsByDifficulty;
    }

    private long getAnswerCountByDifficulty(List<Answer> answers, String difficulty) {
        return answers.stream()
                .filter(a -> a.getQuestion() != null && difficulty.equals(a.getQuestion().getDifficulty()))
                .count();
    }

    @Override
    public Map<String, Integer> calculateAnswerPercentagesByDifficulty() {
        return calculateAnswerPercentages(getQuestionCountsByDifficulty(), getAnswerCountsByDifficulty());
    }

    @Override
    public Map<String, Integer> calculateAnswerPercentagesByDifficultyAndUser(User user) {
        if (!validateUser(user)) {
            return new HashMap<>();
        }
        return calculateAnswerPercentages(
                getQuestionCountsByDifficultyAndUser(user),
                getAnswerCountsByDifficultyAndUser(user));
    }

    private Map<String, Integer> calculateAnswerPercentages(
            Map<String, Long> questionCounts,
            Map<String, Long> answerCounts) {
        Map<String, Integer> percentages = new HashMap<>();
        calculatePercentageByDifficulty(percentages, questionCounts, answerCounts, EASY);
        calculatePercentageByDifficulty(percentages, questionCounts, answerCounts, MEDIUM);
        calculatePercentageByDifficulty(percentages, questionCounts, answerCounts, HARD);
        return percentages;
    }

    private void calculatePercentageByDifficulty(
            Map<String, Integer> percentages,
            Map<String, Long> questionCounts,
            Map<String, Long> answerCounts,
            String difficulty) {
        long questionCount = questionCounts.get(difficulty);
        percentages.put(difficulty,
                questionCount > 0 ? (int) (((double) answerCounts.get(difficulty) / questionCount) * MAX_PERCENTAGE) : 0);
    }

    @Override
    public Map<UUID, TagProgress> calculateTagProgress() {
        return calculateTagProgressForQuestions(tagService.findAll(),
                questionService.findAll(),
                answerService.findAll(),
                q -> true);
    }

    @Override
    public Map<UUID, TagProgress> calculateTagProgressByUser(User user) {
        if (!validateUser(user)) {
            return new HashMap<>();
        }
        return calculateTagProgressForQuestions(tagService.findAll(),
                questionService.findAll(),
                answerService.findAnswersByUser(user),
                q -> q.getUser() != null && q.getUser().getId().equals(user.getId()));
    }

    private Map<UUID, TagProgress> calculateTagProgressForQuestions(
            List<Tag> tags,
            List<InterviewQuestion> questions,
            List<Answer> answers,
            java.util.function.Predicate<InterviewQuestion> userFilter) {
        Map<UUID, TagProgress> tagProgressMap = new HashMap<>();

        for (Tag tag : tags) {
            long questionsWithTag = countQuestionsWithTag(questions, tag, userFilter);
            long answersForTag = countAnswersForTag(answers, tag);

            int percentage = questionsWithTag > 0 ?
                    (int) (((double) answersForTag / questionsWithTag) * MAX_PERCENTAGE) : 0;

            tagProgressMap.put(tag.getId(), new TagProgress(
                    (int) questionsWithTag,
                    (int) answersForTag,
                    percentage));
        }

        return tagProgressMap;
    }

    private long countQuestionsWithTag(List<InterviewQuestion> questions, Tag tag,
                                       java.util.function.Predicate<InterviewQuestion> userFilter) {
        return questions.stream()
                .filter(q -> q.getTags().contains(tag))
                .filter(userFilter)
                .count();
    }

    private long countAnswersForTag(List<Answer> answers, Tag tag) {
        return answers.stream()
                .filter(a -> a.getQuestion() != null && a.getQuestion().getTags().contains(tag))
                .count();
    }

}
