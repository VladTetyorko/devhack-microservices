package com.vladte.devhack.common.controller.global.ui;

import com.vladte.devhack.common.controller.BaseCrudController;
import com.vladte.devhack.common.model.dto.global.InterviewQuestionDTO;
import com.vladte.devhack.common.model.mapper.global.InterviewQuestionMapper;
import com.vladte.devhack.common.service.domain.global.InterviewQuestionService;
import com.vladte.devhack.common.service.generations.QuestionGenerationOrchestrationService;
import com.vladte.devhack.common.service.view.DashboardViewService;
import com.vladte.devhack.common.service.view.QuestionFormService;
import com.vladte.devhack.common.service.view.SearchViewService;
import com.vladte.devhack.common.service.view.TagQuestionService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Simplified UI controller for managing InterviewQuestion views.
 * Focuses only on rendering pages and handling form submissions that redirect to views.
 * Follows the Single Responsibility Principle by handling only UI concerns.
 */
@Controller
@RequestMapping("/questions")
public class InterviewQuestionController extends BaseCrudController<InterviewQuestion, InterviewQuestionDTO, UUID, InterviewQuestionService, InterviewQuestionMapper> {

    private static final Logger log = LoggerFactory.getLogger(InterviewQuestionController.class);
    private final QuestionGenerationOrchestrationService questionGenerationOrchestrationService;
    private final DashboardViewService dashboardViewService;
    private final QuestionFormService questionFormService;
    private final TagQuestionService tagQuestionService;
    private final SearchViewService searchViewService;


    public InterviewQuestionController(
            InterviewQuestionService questionService,
            InterviewQuestionMapper interviewQuestionMapper,
            QuestionGenerationOrchestrationService questionGenerationOrchestrationService,
            DashboardViewService dashboardViewService,
            QuestionFormService questionFormService,
            TagQuestionService tagQuestionService,
            SearchViewService searchViewService) {
        super(questionService, interviewQuestionMapper);
        this.questionGenerationOrchestrationService = questionGenerationOrchestrationService;
        this.dashboardViewService = dashboardViewService;
        this.questionFormService = questionFormService;
        this.tagQuestionService = tagQuestionService;
        this.searchViewService = searchViewService;
    }

    @Override
    protected String getListViewName() {
        return "questions/list";
    }

    @Override
    protected String getDetailViewName() {
        return "questions/view";
    }

    @Override
    protected String getListPageTitle() {
        return "Interview Questions";
    }

    @Override
    protected String getDetailPageTitle() {
        return "Question Details";
    }

    @Override
    protected String getEntityName() {
        return "Question";
    }

    @Override
    public String view(@PathVariable UUID id, Model model) {
        super.view(id, model);

        return "questions/view";
    }


    @Override
    public String list(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size) {
        String viewName = super.list(model, page, size);

        questionFormService.prepareGenerateQuestionsForm(model);

        return viewName;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        dashboardViewService.setDashboardPageTitle(model);
        dashboardViewService.prepareDashboardModel(model);
        return "questions/dashboard";
    }


    @GetMapping("/tag/{tagSlug}")
    public String getQuestionsByTag(
            @PathVariable String tagSlug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Delegate to the tag question service with pagination
        Pageable pageable = PageRequest.of(page, size);
        Tag tag = tagQuestionService.prepareQuestionsByTagModel(tagSlug, pageable, model);
        tagQuestionService.setQuestionsByTagPageTitle(model, tag.getName());

        // Add tags to the model for the question generation modal
        questionFormService.prepareGenerateQuestionsForm(model);

        return "questions/list";
    }

    @GetMapping("/search")
    public String searchQuestions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) UUID tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        // Delegate to the search view service
        searchViewService.prepareSearchResultsModel(query, difficulty, tagId, page, size, model);
        searchViewService.setSearchResultsPageTitle(model, query, difficulty, tagId);

        // Add tags to the model for the question generation modal
        questionFormService.prepareGenerateQuestionsForm(model);

        return "questions/list";
    }

    @PostMapping("/generate")
    public String generateQuestions(
            @RequestParam String topic,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "Medium") String difficulty,
            RedirectAttributes redirectAttributes) {

        // Validate input
        if (questionGenerationOrchestrationService.isTagInvalid(topic)) {
            redirectAttributes.addFlashAttribute("error", "Topic name is required");
            return "redirect:/questions/generate";
        }

        // Start the asynchronous generation process without blocking
        questionGenerationOrchestrationService.startQuestionGeneration(topic, count, difficulty);

        // Success message
        String successMessage = questionGenerationOrchestrationService.buildGenerationSuccessMessage(count, difficulty, topic);
        redirectAttributes.addFlashAttribute("success", successMessage);

        // Find the tag to redirect to its questions page
        Optional<Tag> tag = questionGenerationOrchestrationService.findTagByName(topic);
        if (tag.isPresent()) {
            return "redirect:/questions/tag/" + tag.get().getSlug();
        }

        return "redirect:/questions";
    }

    @GetMapping("/generate")
    public String showGenerateQuestionsForm(Model model) {
        questionFormService.prepareGenerateQuestionsForm(model);
        model.addAttribute("pageTitle", "Generate Interview Questions");
        return "questions/generate";
    }

    @GetMapping("/generate/auto")
    public String showAutoGenerateQuestionsForm(Model model) {
        questionFormService.prepareAutoGenerateQuestionsForm(model);
        questionFormService.setAutoGenerateQuestionsPageTitle(model);
        return "questions/auto-generate";
    }

    @GetMapping("/generate/multi")
    public String showMultiTagAutoGenerateQuestionsForm(Model model) {
        questionFormService.prepareAutoGenerateQuestionsForm(model);
        model.addAttribute("pageTitle", "Auto-Generate Easy Questions for Multiple Tags with AI");
        return "questions/auto-generate-multi";
    }

    @PostMapping("/generate/auto")
    public String autoGenerateEasyQuestions(
            @RequestParam String tagName,
            RedirectAttributes redirectAttributes) {

        if (questionGenerationOrchestrationService.isTagInvalid(tagName)) {
            redirectAttributes.addFlashAttribute("error", "Tag name is required");
            return "redirect:/questions/generate/auto";
        }

        questionGenerationOrchestrationService.startEasyQuestionGeneration(tagName);

        String successMessage = questionGenerationOrchestrationService.buildEasyGenerationSuccessMessage(tagName);
        redirectAttributes.addFlashAttribute("success", successMessage);

        // Find the tag to redirect to its questions page
        Optional<Tag> tag = questionGenerationOrchestrationService.findTagByName(tagName);
        return tag.map(value -> "redirect:/questions/tag/" + value.getSlug()).orElse("redirect:/questions");
    }

    @PostMapping("/generate/multi")
    public String autoGenerateEasyQuestionsForMultipleTags(
            @RequestParam(value = "tagIds", required = false) List<UUID> tagIds,
            RedirectAttributes redirectAttributes) {

        // Validate input
        if (tagIds == null || tagIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "At least one tag must be selected");
            return "redirect:/questions/generate/multi";
        }

        log.info("Starting asynchronous generation of easy questions for {} tags", tagIds.size());

        // Start the asynchronous generation process without blocking
        questionGenerationOrchestrationService.startEasyQuestionGenerationForMultipleTags(tagIds);

        // Add message that generation has started
        String successMessage = questionGenerationOrchestrationService.buildMultiTagEasyGenerationSuccessMessage(tagIds.size());
        redirectAttributes.addFlashAttribute("success", successMessage);

        return "redirect:/questions";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        service.deleteById(id);
        return "redirect:/questions";
    }
}
