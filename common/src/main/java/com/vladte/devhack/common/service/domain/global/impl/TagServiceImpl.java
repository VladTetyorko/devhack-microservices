package com.vladte.devhack.common.service.domain.global.impl;

import com.vladte.devhack.common.exception.ExceptionUtils;
import com.vladte.devhack.common.exception.ServiceException;
import com.vladte.devhack.common.repository.global.TagRepository;
import com.vladte.devhack.common.service.domain.AuditableCrudService;
import com.vladte.devhack.common.service.domain.audit.AuditService;
import com.vladte.devhack.common.service.domain.global.TagService;
import com.vladte.devhack.common.service.domain.personalized.AnswerService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.personalized.Answer;
import com.vladte.devhack.entities.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the TagService interface.
 * Enhanced with comprehensive exception handling following SOLID and DRY principles.
 */
@Service
@Slf4j
public class TagServiceImpl extends AuditableCrudService<Tag, UUID, TagRepository> implements TagService {

    private final AnswerService answerService;

    /**
     * Constructor with repository and service injection.
     *
     * @param repository    the tag repository
     * @param answerService the answer service
     */

    public TagServiceImpl(TagRepository repository, AuditService auditService, AnswerService answerService) {
        super(repository, auditService);
        this.answerService = answerService;
    }

    @Override
    public Optional<Tag> findTagByName(String name) {
        ExceptionUtils.requireNonEmpty(name, "tag name");

        try {
            log.debug("Finding tag by name: {}", name);
            return repository.findByName(name);
        } catch (Exception e) {
            log.error("Error finding tag by name: {}", name, e);
            throw new ServiceException("Failed to find tag by name: " + name, e, "TAG_FIND_ERROR", name);
        }
    }

    @Override
    public Optional<Tag> findTagBySlug(String slug) {
        ExceptionUtils.requireNonEmpty(slug, "tag slug");

        try {
            log.debug("Finding tag by slug: {}", slug);
            // Find all tags and filter by slug
            return findAll().stream()
                    .filter(tag -> tag.getSlug().equals(slug))
                    .findFirst();
        } catch (Exception e) {
            log.error("Error finding tag by slug: {}", slug, e);
            throw new ServiceException("Failed to find tag by slug: " + slug, e, "TAG_FIND_ERROR", slug);
        }
    }

    @Override
    public Tag calculateProgress(Tag tag, User user) {
        ExceptionUtils.requireNonNull(tag, "tag");
        ExceptionUtils.requireNonNull(user, "user");

        try {
            log.debug("Calculating progress for tag: {} and user: {}", tag.getId(), user.getId());

            // Get all questions for this tag
            Set<InterviewQuestion> questions = tag.getQuestions();
            if (questions.isEmpty()) {
                log.debug("No questions found for tag: {}", tag.getId());
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

            log.debug("Progress calculated for tag: {} - answered: {}/{}",
                    tag.getId(), answeredCount, questions.size());
            return tag;

        } catch (Exception e) {
            log.error("Error calculating progress for tag: {} and user: {}", tag.getId(), user.getId(), e);
            throw new ServiceException("Failed to calculate progress for tag", e, "TAG_PROGRESS_ERROR", tag.getId(), user.getId());
        }
    }

    @Override
    public List<Tag> calculateProgressForAll(List<Tag> tags, User user) {
        ExceptionUtils.requireNonNull(tags, "tags list");
        ExceptionUtils.requireNonNull(user, "user");

        try {
            log.debug("Calculating progress for {} tags for user: {}", tags.size(), user.getId());

            // Calculate progress for each tag
            for (Tag tag : tags) {
                calculateProgress(tag, user);
            }

            log.debug("Progress calculated for all {} tags for user: {}", tags.size(), user.getId());
            return tags;

        } catch (Exception e) {
            log.error("Error calculating progress for all tags for user: {}", user.getId(), e);
            throw new ServiceException("Failed to calculate progress for all tags", e, "TAG_PROGRESS_ALL_ERROR", user.getId());
        }
    }

    @Override
    public int countTagsByUser(User user) {
        ExceptionUtils.requireNonNull(user, "user");

        try {
            log.debug("Counting tags used by user: {}", user.getId());

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

            int count = userTagIds.size();
            log.debug("User {} has used {} unique tags", user.getId(), count);
            return count;

        } catch (Exception e) {
            log.error("Error counting tags for user: {}", user.getId(), e);
            throw new ServiceException("Failed to count tags for user", e, "TAG_COUNT_USER_ERROR", user.getId());
        }
    }

    @Override
    public int countAllTags() {
        try {
            log.debug("Counting all tags");

            int count = findAll().size();
            log.debug("Total tags count: {}", count);
            return count;

        } catch (Exception e) {
            log.error("Error counting all tags", e);
            throw new ServiceException("Failed to count all tags", e, "TAG_COUNT_ALL_ERROR");
        }
    }
}
