package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.domain.TagService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.common.service.view.QuestionFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * Implementation of the QuestionFormService interface.
 * This class handles form-related operations for questions.
 */
@Service
public class QuestionFormServiceImpl implements QuestionFormService {

    private final TagService tagService;

    @Autowired
    public QuestionFormServiceImpl(TagService tagService) {
        this.tagService = tagService;
    }


    @Override
    public void prepareGenerateQuestionsForm(Model model) {
        ModelBuilder.of(model)
                .addAttribute("tags", tagService.findAll())
                .build();
    }

    @Override
    public void prepareAutoGenerateQuestionsForm(Model model) {
        ModelBuilder.of(model)
                .addAttribute("tags", tagService.findAll())
                .build();
    }

    @Override
    public void setAutoGenerateQuestionsPageTitle(Model model) {
        ModelBuilder.of(model)
                .setPageTitle("Auto-Generate Easy Questions with AI")
                .build();
    }
}
