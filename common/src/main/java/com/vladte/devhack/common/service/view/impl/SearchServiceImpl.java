package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.domain.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.TagService;
import com.vladte.devhack.common.service.view.SearchService;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the SearchService interface.
 * This class follows the Single Responsibility Principle by focusing only on search-related operations.
 */
@Service
public class SearchServiceImpl implements SearchService {

    private final InterviewQuestionService questionService;
    private final TagService tagService;

    @Autowired
    public SearchServiceImpl(InterviewQuestionService questionService, TagService tagService) {
        this.questionService = questionService;
        this.tagService = tagService;
    }

    @Override
    public Page<InterviewQuestion> searchQuestions(String query, String difficulty, UUID tagId, Pageable pageable) {
        return questionService.searchQuestions(query, difficulty, tagId, pageable);
    }

    @Override
    public String buildSearchPageTitle(String query, String difficulty, UUID tagId) {
        StringBuilder titleBuilder = new StringBuilder("Search Results");

        if (StringUtils.hasText(query)) {
            titleBuilder.append(" for: ").append(query);
        }

        if (StringUtils.hasText(difficulty)) {
            titleBuilder.append(" (Difficulty: ").append(difficulty).append(")");
        }

        if (tagId != null) {
            Optional<Tag> tagOpt = tagService.findById(tagId);
            tagOpt.ifPresent(tag -> titleBuilder.append(" (Tag: ").append(tag.getName()).append(")"));
        }

        return titleBuilder.toString();
    }
}