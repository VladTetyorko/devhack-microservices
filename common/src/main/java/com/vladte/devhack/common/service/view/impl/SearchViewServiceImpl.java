package com.vladte.devhack.common.service.view.impl;

import com.vladte.devhack.common.service.domain.global.TagService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.common.service.view.SearchService;
import com.vladte.devhack.common.service.view.SearchViewService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.UUID;

/**
 * Implementation of the SearchViewService interface.
 * This class prepares the model for the search results view.
 */
@Service
public class SearchViewServiceImpl implements SearchViewService {

    private final SearchService searchService;
    private final TagService tagService;


    public SearchViewServiceImpl(SearchService searchService, TagService tagService) {
        this.searchService = searchService;
        this.tagService = tagService;
    }

    @Override
    public Pageable prepareSearchResultsModel(String query, String difficulty, UUID tagId, int page, int size, Model model) {
        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);

        // Search questions with pagination and filtering
        Page<InterviewQuestion> questionPage = searchService.searchQuestions(
                query, difficulty, tagId, pageable);

        // Build the model using ModelBuilder
        ModelBuilder.of(model)
                // Add pagination data
                .addAttribute("questions", questionPage.getContent())
                .addAttribute("currentPage", page)
                .addAttribute("totalPages", questionPage.getTotalPages())
                .addAttribute("totalItems", questionPage.getTotalElements())

                // Add filter parameters for maintaining state in the view
                .addAttribute("query", query)
                .addAttribute("difficulty", difficulty)
                .addAttribute("tagId", tagId)

                // Add all tags for the filter dropdown
                .addAttribute("allTags", tagService.findAll())
                .build();

        return pageable;
    }

    @Override
    public void setSearchResultsPageTitle(Model model, String query, String difficulty, UUID tagId) {
        String pageTitle = searchService.buildSearchPageTitle(query, difficulty, tagId);
        ModelBuilder.of(model)
                .setPageTitle(pageTitle)
                .build();
    }
}
