package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.domain.global.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.global.TagService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.common.service.view.TagQuestionService;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the TagQuestionService interface.
 * This class handles operations related to questions filtered by tags.
 */
@Service
public class TagQuestionServiceImpl implements TagQuestionService {

    private final TagService tagService;
    private final InterviewQuestionService questionService;


    public TagQuestionServiceImpl(TagService tagService, InterviewQuestionService questionService) {
        this.tagService = tagService;
        this.questionService = questionService;
    }

    @Override
    public Tag prepareQuestionsByTagModel(String tagSlug, Model model) {
        Tag tag = getTagBySlugOrThrow(tagSlug, "Tag not found");
        List<InterviewQuestion> questions = questionService.findQuestionsByTag(tag);

        ModelBuilder.of(model)
                .addAttribute("questions", questions)
                .addAttribute("tag", tag)
                .build();

        return tag;
    }

    @Override
    public Tag prepareQuestionsByTagModel(String tagSlug, Pageable pageable, Model model) {
        Tag tag = getTagBySlugOrThrow(tagSlug, "Tag not found");
        Page<InterviewQuestion> questionPage = questionService.findQuestionsByTag(tag, pageable);

        ModelBuilder.of(model)
                .addPagination(questionPage, pageable.getPageNumber(), pageable.getPageSize(), "questions")
                .addAttribute("tag", tag)
                .build();

        return tag;
    }

    @Override
    public void setQuestionsByTagPageTitle(Model model, String tagName) {
        ModelBuilder.of(model)
                .setPageTitle("Questions tagged with " + tagName)
                .build();
    }

    @Override
    public Tag getTagBySlugOrThrow(String tagSlug, String errorMessage) {
        Optional<Tag> tagOpt = tagService.findTagBySlug(tagSlug);
        if (tagOpt.isPresent()) {
            return tagOpt.get();
        }
        throw new RuntimeException(errorMessage);
    }
}
