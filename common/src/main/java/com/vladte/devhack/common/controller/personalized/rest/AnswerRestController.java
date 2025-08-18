package com.vladte.devhack.common.controller.personalized.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.personalized.AnswerDTO;
import com.vladte.devhack.common.model.mapper.personalized.AnswerMapper;
import com.vladte.devhack.common.service.domain.global.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.personalized.AnswerService;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.personalized.Answer;
import com.vladte.devhack.entities.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing Answer entities.
 * Provides RESTful API endpoints for CRUD operations on answers.
 */
@RestController
@RequestMapping("/api/answers")
@Tag(name = "Answer", description = "Answer management API")
@Slf4j
public class AnswerRestController extends BaseRestController<Answer, AnswerDTO, UUID, AnswerService, AnswerMapper> {

    private final InterviewQuestionService questionService;
    private final UserService userService;

    /**
     * Constructor with service and mapper injection.
     *
     * @param answerService   the answer service
     * @param answerMapper    the answer mapper
     * @param questionService the question service
     * @param userService     the user service
     */
    public AnswerRestController(AnswerService answerService,
                                AnswerMapper answerMapper,
                                InterviewQuestionService questionService,
                                UserService userService) {
        super(answerService, answerMapper);
        this.questionService = questionService;
        this.userService = userService;
    }

    /**
     * Get all answers for the authenticated user.
     *
     * @param user     the authenticated user
     * @param pageable pagination information
     * @return a page of answers
     */
    @GetMapping("/my-answers")
    @Operation(summary = "Get all answers for the authenticated user",
            description = "Returns a page of answers for the authenticated user")
    public ResponseEntity<Page<AnswerDTO>> getMyAnswers(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user,
            Pageable pageable) {
        log.debug("REST request to get all answers for user: {}", user.getProfile().getName());
        Page<Answer> page = service.findAnswersByUser(user, pageable);
        Page<AnswerDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get all answers for a specific question.
     *
     * @param questionId the ID of the question
     * @param pageable   pagination information
     * @return a page of answers
     */
    @GetMapping("/by-question/{questionId}")
    @Operation(summary = "Get all answers for a specific question",
            description = "Returns a page of answers for the specified question")
    public ResponseEntity<Page<AnswerDTO>> getAnswersByQuestion(
            @Parameter(description = "ID of the question")
            @PathVariable UUID questionId,
            Pageable pageable) {
        log.debug("REST request to get all answers for question: {}", questionId);

        InterviewQuestion question = questionService.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        Page<Answer> page = service.findAnswersByQuestion(question, pageable);
        Page<AnswerDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get all answers for a specific user.
     *
     * @param userId   the ID of the user
     * @param pageable pagination information
     * @return a page of answers
     */
    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get all answers for a specific user",
            description = "Returns a page of answers for the specified user")
    public ResponseEntity<Page<AnswerDTO>> getAnswersByUser(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId,
            Pageable pageable) {
        log.debug("REST request to get all answers for user: {}", userId);

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Page<Answer> page = service.findAnswersByUser(user, pageable);
        Page<AnswerDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Create a new answer for a specific question.
     * This endpoint explicitly handles the question ID and user from authentication.
     *
     * @param questionId the ID of the question being answered
     * @param answerDTO  the answer data
     * @param user       the authenticated user
     * @return the created answer
     */
    @PostMapping("/for-question/{questionId}")
    @Operation(summary = "Create a new answer for a specific question",
            description = "Creates a new answer for the specified question with the authenticated user")
    public ResponseEntity<AnswerDTO> createAnswerForQuestion(
            @Parameter(description = "ID of the question being answered")
            @PathVariable UUID questionId,
            @Parameter(description = "Answer data")
            @RequestBody AnswerDTO answerDTO,
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to create answer for question: {} by user: {}", questionId, user.getProfile().getName());

        // Validate that the question exists
        InterviewQuestion question = questionService.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        // Set the question ID and user ID in the DTO
        answerDTO.setQuestionId(questionId);
        answerDTO.setUserId(user.getId());

        // Convert DTO to entity and save
        Answer entity = mapper.toEntity(answerDTO);
        entity.setQuestion(question);
        entity.setUser(user);

        Answer savedEntity = service.save(entity);
        AnswerDTO responseDTO = mapper.toDTO(savedEntity);

        log.info("Answer created successfully for question: {} by user: {}", questionId, user.getProfile().getName());
        return ResponseEntity.status(201).body(responseDTO);
    }

    /**
     * Check an answer using AI and update its score and feedback.
     *
     * @param id the ID of the answer to check
     * @return the updated answer
     */
    @PostMapping("/{id}/evaluate")
    @Operation(summary = "Evaluate an answer using AI",
            description = "Initiates an AI check for the specified answer and returns a status")
    public ResponseEntity<String> checkAnswerWithAi(
            @Parameter(description = "ID of the answer to check")
            @PathVariable UUID id) {
        log.debug("REST request to check answer with AI: {}", id);

        service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + id));

        service.checkAnswerWithAiAsync(id);

        return ResponseEntity.ok("AI check initiated. The answer will be evaluated in the background.");
    }

}
