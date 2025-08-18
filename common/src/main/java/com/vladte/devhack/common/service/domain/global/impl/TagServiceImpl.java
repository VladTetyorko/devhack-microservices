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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            return repository.findBySlug(slug);
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
    @Cacheable(value = "tagCountByUser", key = "#user.id", cacheManager = "shortTermCacheManager")
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
    @Cacheable(value = "totalTagCount", key = "'all'", cacheManager = "mediumTermCacheManager")
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

    @Override
    @Transactional
    public Tag createTag(String name, Tag parent) {
        ExceptionUtils.requireNonEmpty(name, "tag name");

        try {
            log.debug("Creating tag with name: {} and parent: {}", name, parent != null ? parent.getId() : "null");

            // Create new tag
            Tag tag = new Tag();
            tag.setName(name);
            tag.setParent(parent);

            // Generate slug and path
            tag.updateSlugAndPath();

            // Validate uniqueness of slug within parent scope
            Optional<Tag> existingTag = repository.findByParentAndSlug(parent, tag.getSlug());
            if (existingTag.isPresent()) {
                throw new ServiceException("Tag with slug '" + tag.getSlug() + "' already exists under the same parent",
                        "TAG_SLUG_DUPLICATE_ERROR", tag.getSlug());
            }

            // Save the tag
            Tag savedTag = repository.save(tag);
            log.debug("Created tag: {} with path: {}", savedTag.getId(), savedTag.getPath());
            return savedTag;

        } catch (Exception e) {
            log.error("Error creating tag with name: {} and parent: {}", name, parent != null ? parent.getId() : "null", e);
            throw new ServiceException("Failed to create tag", e, "TAG_CREATE_ERROR", name);
        }
    }

    @Override
    @Transactional
    public Tag moveTag(Tag tag, Tag newParent) {
        ExceptionUtils.requireNonNull(tag, "tag");

        try {
            log.debug("Moving tag: {} to new parent: {}", tag.getId(), newParent != null ? newParent.getId() : "null");

            // Validate move to prevent cycles
            if (!validateMove(tag, newParent)) {
                throw new ServiceException("Cannot move tag: would create a cycle", "TAG_MOVE_CYCLE_ERROR", tag.getId());
            }

            // Store old path for subtree update
            String oldPath = tag.getPath();

            // Update parent and regenerate path
            tag.setParent(newParent);
            tag.updateSlugAndPath();
            String newPath = tag.getPath();

            // Update all subtree paths
            if (oldPath != null && !oldPath.equals(newPath)) {
                repository.updateSubtreePaths(oldPath, newPath);
            }

            // Save the updated tag
            Tag savedTag = repository.save(tag);
            log.debug("Moved tag: {} from path: {} to path: {}", savedTag.getId(), oldPath, newPath);
            return savedTag;

        } catch (Exception e) {
            log.error("Error moving tag: {} to new parent: {}", tag.getId(), newParent != null ? newParent.getId() : "null", e);
            throw new ServiceException("Failed to move tag", e, "TAG_MOVE_ERROR", tag.getId());
        }
    }

    @Override
    @Transactional
    public boolean deleteTag(Tag tag, boolean cascadeDelete) {
        ExceptionUtils.requireNonNull(tag, "tag");

        try {
            log.debug("Deleting tag: {} with cascade: {}", tag.getId(), cascadeDelete);

            if (cascadeDelete) {
                // Delete entire subtree
                repository.delete(tag);
            } else {
                // Move children to root before deleting
                List<Tag> children = findChildren(tag);
                for (Tag child : children) {
                    moveTag(child, null);
                }
                repository.delete(tag);
            }

            log.debug("Deleted tag: {} with cascade: {}", tag.getId(), cascadeDelete);
            return true;

        } catch (Exception e) {
            log.error("Error deleting tag: {} with cascade: {}", tag.getId(), cascadeDelete, e);
            throw new ServiceException("Failed to delete tag", e, "TAG_DELETE_ERROR", tag.getId());
        }
    }

    @Override
    public List<Tag> findRootTags() {
        try {
            log.debug("Finding root tags");
            return repository.findByParentIsNull();
        } catch (Exception e) {
            log.error("Error finding root tags", e);
            throw new ServiceException("Failed to find root tags", e, "TAG_FIND_ROOTS_ERROR");
        }
    }

    @Override
    public List<Tag> findChildren(Tag parent) {
        ExceptionUtils.requireNonNull(parent, "parent tag");

        try {
            log.debug("Finding children of tag: {}", parent.getId());
            return repository.findByParent(parent);
        } catch (Exception e) {
            log.error("Error finding children of tag: {}", parent.getId(), e);
            throw new ServiceException("Failed to find children", e, "TAG_FIND_CHILDREN_ERROR", parent.getId());
        }
    }

    @Override
    public List<Tag> findDescendants(Tag parent) {
        ExceptionUtils.requireNonNull(parent, "parent tag");

        try {
            log.debug("Finding descendants of tag: {}", parent.getId());
            if (parent.getPath() == null) {
                return new ArrayList<>();
            }
            return repository.findDescendants(parent.getPath());
        } catch (Exception e) {
            log.error("Error finding descendants of tag: {}", parent.getId(), e);
            throw new ServiceException("Failed to find descendants", e, "TAG_FIND_DESCENDANTS_ERROR", parent.getId());
        }
    }

    @Override
    public List<Tag> findAncestors(Tag tag) {
        ExceptionUtils.requireNonNull(tag, "tag");

        try {
            log.debug("Finding ancestors of tag: {}", tag.getId());
            if (tag.getPath() == null) {
                return new ArrayList<>();
            }
            return repository.findAncestors(tag.getPath());
        } catch (Exception e) {
            log.error("Error finding ancestors of tag: {}", tag.getId(), e);
            throw new ServiceException("Failed to find ancestors", e, "TAG_FIND_ANCESTORS_ERROR", tag.getId());
        }
    }

    @Override
    public List<Tag> findSubtree(Tag parent, int depth) {
        ExceptionUtils.requireNonNull(parent, "parent tag");

        try {
            log.debug("Finding subtree of tag: {} with depth: {}", parent.getId(), depth);
            if (parent.getPath() == null) {
                return new ArrayList<>();
            }
            return repository.findSubtreeWithDepth(parent.getPath(), depth);
        } catch (Exception e) {
            log.error("Error finding subtree of tag: {} with depth: {}", parent.getId(), depth, e);
            throw new ServiceException("Failed to find subtree", e, "TAG_FIND_SUBTREE_ERROR", parent.getId());
        }
    }

    @Override
    public List<Tag> findSiblings(Tag tag) {
        ExceptionUtils.requireNonNull(tag, "tag");

        try {
            log.debug("Finding siblings of tag: {}", tag.getId());
            return repository.findSiblings(tag.getParent(), tag.getId());
        } catch (Exception e) {
            log.error("Error finding siblings of tag: {}", tag.getId(), e);
            throw new ServiceException("Failed to find siblings", e, "TAG_FIND_SIBLINGS_ERROR", tag.getId());
        }
    }

    @Override
    public List<Tag> findTagsByDepth(int depth) {
        try {
            log.debug("Finding tags at depth: {}", depth);
            return repository.findByDepth(depth);
        } catch (Exception e) {
            log.error("Error finding tags at depth: {}", depth, e);
            throw new ServiceException("Failed to find tags by depth", e, "TAG_FIND_BY_DEPTH_ERROR", depth);
        }
    }

    @Override
    public boolean validateMove(Tag tag, Tag newParent) {
        ExceptionUtils.requireNonNull(tag, "tag");

        try {
            log.debug("Validating move of tag: {} to parent: {}", tag.getId(), newParent != null ? newParent.getId() : "null");

            // Cannot move to itself
            if (newParent != null && tag.getId().equals(newParent.getId())) {
                return false;
            }

            // Cannot move to its own descendant (would create cycle)
            if (newParent != null && tag.isAncestorOf(newParent)) {
                return false;
            }

            // Check if slug would be unique under new parent
            Optional<Tag> existingTag = repository.findByParentAndSlug(newParent, tag.getSlug());
            return existingTag.isEmpty() || existingTag.get().getId().equals(tag.getId());

        } catch (Exception e) {
            log.error("Error validating move of tag: {} to parent: {}", tag.getId(), newParent != null ? newParent.getId() : "null", e);
            return false;
        }
    }
}
