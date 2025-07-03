package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.dto.AnswerDTO;
import com.vladte.devhack.common.mapper.AnswerMapper;
import com.vladte.devhack.common.service.domain.AnswerService;
import com.vladte.devhack.common.service.domain.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.view.AnswerFormService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/answers")
public class AnswerController extends UserEntityController<Answer, UUID, AnswerService> {

    private static final Logger logger = LoggerFactory.getLogger(AnswerController.class);

    private final InterviewQuestionService questionService;
    private final AnswerFormService answerFormService;
    private final AnswerMapper answerMapper;

    @Autowired
    public AnswerController(AnswerService answerService,
                            UserService userService,
                            InterviewQuestionService questionService,
                            AnswerFormService answerFormService,
                            AnswerMapper answerMapper) {
        super(answerService, userService);
        this.questionService = questionService;
        this.answerFormService = answerFormService;
        this.answerMapper = answerMapper;
    }

    @Override
    protected User getEntityUser(Answer entity) {
        return entity.getUser();
    }

    @Override
    protected String getListViewName() {
        return "answers/list";
    }

    @Override
    protected String getDetailViewName() {
        return "answers/view";
    }

    @Override
    protected String getListPageTitle() {
        return "Answers";
    }

    @Override
    protected String getDetailPageTitle() {
        return "Answer Details";
    }

    @Override
    protected String getEntityName() {
        return "Answer";
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        logger.debug("Listing answers with access control and pagination");

        // Get the current authenticated user using the parent class method
        User currentUser = getCurrentUser();

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);

        // Get answers for the current user with pagination
        Page<Answer> answerPage = service.findAnswersByUser(currentUser, pageable);

        // Convert to DTO page
        Page<AnswerDTO> answerDTOPage = answerPage.map(answerMapper::toDTO);

        // Using the ModelBuilder to build the model with pagination data
        ModelBuilder.of(model)
                .addPagination(answerDTOPage, page, size, "answers")
                .setPageTitle("My Answers")
                .addAttribute("currentUser", currentUser)
                .build();

        return "answers/list";
    }


    @GetMapping("/new")
    public String newAnswerForm(@RequestParam(required = false) UUID questionId, Model model) {
        logger.debug("Displaying new answer form with access control");

        // If a question ID is provided, check if the question exists
        if (questionId != null) {
            InterviewQuestion question = questionService.findById(questionId)
                    .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

            // Add the question to the model
            model.addAttribute("question", question);
        }

        // Delegate to the form service
        answerFormService.prepareNewAnswerForm(questionId, model);
        answerFormService.setNewAnswerPageTitle(model);

        return "answers/form";
    }

    /**
     * Display the form for editing an existing answer.
     *
     * @param id    the ID of the answer to edit
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/{id}")
    public String view(@PathVariable UUID id, Model model) {
        logger.debug("Viewing answer with ID: {} with access control", id);

        // Get the answer from the service
        Answer answer = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + id));

        // Check if the current user has access to the answer
        if (!hasAccessToEntity(answer)) {
            logger.warn("Access denied to answer with ID: {}", id);
            throw new SecurityException("Access denied to answer with ID: " + id);
        }

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Delegate to the form service
        AnswerDTO answerDTO = answerFormService.prepareEditAnswerForm(id, model);

        // Add the question to the model for the view
        if (answerDTO.getQuestionId() != null) {
            questionService.findById(answerDTO.getQuestionId())
                    .ifPresent(question -> model.addAttribute("question", question));
        }

        // Add the current user to the model
        model.addAttribute("currentUser", currentUser);

        answerFormService.setEditAnswerPageTitle(model);
        return "answers/view";
    }


    /**
     * Display the form for editing an existing answer.
     *
     * @param id    the ID of the answer to edit
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/{id}/edit")
    public String editAnswerForm(@PathVariable UUID id, Model model) {
        logger.debug("Editing answer with ID: {} with access control", id);

        // Get the answer from the service
        Answer answer = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + id));

        // Check if the current user has access to the answer
        if (!hasAccessToEntity(answer)) {
            logger.warn("Access denied to edit answer with ID: {}", id);
            throw new SecurityException("Access denied to edit answer with ID: " + id);
        }

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Delegate to the form service
        if (answerFormService.prepareEditAnswerForm(id, model) == null) {
            throw new IllegalArgumentException("Answer not found");
        }

        // Add the current user to the model
        model.addAttribute("currentUser", currentUser);

        answerFormService.setEditAnswerPageTitle(model);
        return "answers/form";
    }

    /**
     * Process the form submission for creating or updating an answer.
     *
     * @param answerDTO  the answer data from the form
     * @param questionId the ID of the question being answered
     * @return a redirect to the answer list
     */
    @PostMapping
    public String saveAnswer(
            @ModelAttribute AnswerDTO answerDTO,
            @RequestParam UUID questionId) {
        logger.debug("Saving answer with access control");

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Save the answer
        answerFormService.saveAnswer(answerDTO, currentUser.getId(), questionId);
        logger.info("Answer saved successfully");

        return "redirect:/answers";
    }

    /**
     * Delete an answer.
     *
     * @param id the ID of the answer to delete
     * @return a redirect to the answer list
     */
    @GetMapping("/{id}/delete")
    public String deleteAnswer(@PathVariable UUID id) {
        logger.debug("Deleting answer with ID: {} with access control", id);

        // Get the answer from the service
        Answer answer = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + id));

        // Check if the current user has access to the answer
        if (!hasAccessToEntity(answer)) {
            logger.warn("Access denied to delete answer with ID: {}", id);
            throw new SecurityException("Access denied to delete answer with ID: " + id);
        }

        // Delete the answer
        service.deleteById(id);
        logger.info("Answer with ID: {} deleted successfully", id);

        return "redirect:/answers";
    }

    /**
     * Display answers by user with pagination.
     *
     * @param userId the ID of the user
     * @param page   the page number (0-based)
     * @param size   the page size
     * @param model  the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/user/{userId}")
    public String getAnswersByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        logger.debug("Getting answers for user with ID: {} with access control and pagination", userId);

        // Get the user from the service
        User user = getEntityOrThrow(userService.findById(userId), "User not found");

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Check if the current user is a manager or is viewing their own answers
        if (!isCurrentUserManager() && !currentUser.getId().equals(userId)) {
            logger.warn("Access denied to view answers for user with ID: {}", userId);
            throw new SecurityException("Access denied to view answers for user with ID: " + userId);
        }

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);

        // Get answers for the user with pagination
        Page<Answer> answerPage = service.findAnswersByUser(user, pageable);

        // Convert to DTO page
        Page<AnswerDTO> answerDTOPage = answerPage.map(answerMapper::toDTO);

        // Using ModelBuilder to build the model with pagination
        ModelBuilder.of(model)
                .addPagination(answerDTOPage, page, size, "answers")
                .addAttribute("user", user)
                .addAttribute("currentUser", currentUser)
                .setPageTitle("Answers by " + user.getName())
                .build();

        return "answers/list";
    }

    /**
     * Display answers by question with pagination.
     *
     * @param questionId the ID of the question
     * @param page       the page number (0-based)
     * @param size       the page size
     * @param model      the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/question/{questionId}")
    public String getAnswersByQuestion(
            @PathVariable UUID questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        logger.debug("Getting answers for question with ID: {} with access control and pagination", questionId);

        // Get the question from the service
        InterviewQuestion question = getEntityOrThrow(questionService.findById(questionId), "Question not found");

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Check if the current user is a manager or has access to the question
        // For simplicity, we'll allow access if the user is a manager or if the question is public
        // In a real application, you might want to check if the user has access to the question based on other criteria
        if (!isCurrentUserManager()) {
            logger.warn("Access denied to view answers for question with ID: {}", questionId);
            throw new SecurityException("Access denied to view answers for question with ID: " + questionId);
        }

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size);

        // Get answers for the question with pagination
        Page<Answer> answerPage = service.findAnswersByQuestion(question, pageable);

        // Convert to DTO page
        Page<AnswerDTO> answerDTOPage = answerPage.map(answerMapper::toDTO);

        // Using ModelBuilder to build the model with pagination
        ModelBuilder.of(model)
                .addPagination(answerDTOPage, page, size, "answers")
                .addAttribute("question", question)
                .addAttribute("currentUser", currentUser)
                .setPageTitle("Answers for Question: " + question.getQuestionText())
                .build();

        return "answers/list";
    }

    /**
     * Check an answer using AI and update its score and feedback.
     * This is a non-blocking method that only sends an async request to check.
     *
     * @param id    the ID of the answer to check
     * @param model the model to add attributes to
     * @return the name of the view to render
     */
    @GetMapping("/{id}/check")
    public String checkAnswerWithAi(@PathVariable UUID id, Model model) {
        logger.debug("Initiating async check of answer with AI for answer with ID: {} with access control", id);

        // Get the answer from the service
        Answer answer = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + id));

        // Check if the current user has access to the answer
        if (!hasAccessToEntity(answer)) {
            logger.warn("Access denied to check answer with AI for answer with ID: {}", id);
            throw new SecurityException("Access denied to check answer with AI for answer with ID: " + id);
        }

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Get the answer DTO for display
        AnswerDTO answerDTO = answerFormService.prepareEditAnswerForm(id, model);

        // Add the question to the model for the view
        if (answerDTO.getQuestionId() != null) {
            questionService.findById(answerDTO.getQuestionId())
                    .ifPresent(question -> model.addAttribute("question", question));
        }
        // Delegate to the form service - non-blocking call
        answerFormService.checkAnswerWithAiAsync(id);

        // Add information to the model
        ModelBuilder.of(model)
                .addAttribute("message", "AI check initiated. The answer will be evaluated in the background. Refresh this page in a few moments to see the results.")
                .build();

        // Set the page title
        answerFormService.setEditAnswerPageTitle(model);

        return "answers/view";
    }
}
