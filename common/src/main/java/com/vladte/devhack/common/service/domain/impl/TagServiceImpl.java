package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.TagRepository;
import com.vladte.devhack.common.service.domain.AnswerService;
import com.vladte.devhack.common.service.domain.TagService;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import com.vladte.devhack.entities.User;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the TagService interface.
 */
@Service
public class TagServiceImpl extends BaseServiceImpl<Tag, UUID, TagRepository> implements TagService {

    private final AnswerService answerService;

    /**
     * Constructor with repository and service injection.
     *
     * @param repository    the tag repository
     * @param answerService the answer service
     */

    public TagServiceImpl(TagRepository repository, AnswerService answerService) {
        super(repository);
        this.answerService = answerService;
    }

    @Override
    public Optional<Tag> findTagByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Optional<Tag> findTagBySlug(String slug) {
        // Find all tags and filter by slug
        return findAll().stream()
                .filter(tag -> tag.getSlug().equals(slug))
                .findFirst();
    }

    @Override
    public Tag calculateProgress(Tag tag, User user) {
        if (tag == null || user == null) {
            return tag;
        }

        // Get all questions for this tag
        Set<InterviewQuestion> questions = tag.getQuestions();
        if (questions.isEmpty()) {
            tag.setAnsweredQuestions(0);
            tag.setProgressPercentage(0.0);
            return tag;
        }

        // Get all answers by this user
        List<Answer> userAnswers = answerService.findAnswersByUser(user);

        // Count how many questions from this tag the user has answered
        Set<UUID> answeredQuestionIds = userAnswers.stream()
                .map(answer -> answer.getQuestion().getId())
                .collect(Collectors.toSet());

        int answeredCount = 0;
        for (InterviewQuestion question : questions) {
            if (answeredQuestionIds.contains(question.getId())) {
                answeredCount++;
            }
        }

        // Update the tag with progress information
        tag.setAnsweredQuestions(answeredCount);
        tag.updateProgress();

        return tag;
    }

    @Override
    public List<Tag> calculateProgressForAll(List<Tag> tags, User user) {
        if (tags == null || user == null) {
            return new ArrayList<>();
        }

        // Calculate progress for each tag
        for (Tag tag : tags) {
            calculateProgress(tag, user);
        }

        return tags;
    }

    @Override
    public int countTagsByUser(User user) {
        if (user == null) {
            return 0;
        }

        // Get all tags
        List<Tag> allTags = findAll();

        // Create a set to store unique tag IDs used by the user
        Set<UUID> userTagIds = new HashSet<>();

        // For each tag, check if it's used in any of the user's questions
        for (Tag tag : allTags) {
            boolean tagUsedByUser = tag.getQuestions().stream()
                    .anyMatch(question -> question.getUser() != null && question.getUser().getId().equals(user.getId()));

            if (tagUsedByUser) {
                userTagIds.add(tag.getId());
            }
        }

        return userTagIds.size();
    }

    @Override
    public int countAllTags() {
        return findAll().size();
    }
}
