package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.dto.InterviewQuestionDTO;
import com.vladte.devhack.common.mapper.InterviewQuestionMapper;
import com.vladte.devhack.common.service.domain.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.TagService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.generations.QuestionGenerationOrchestrationService;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing InterviewQuestion entities.
 * Provides RESTful API endpoints for CRUD operations on interview questions.
 */
@RestController
@RequestMapping("/api/questions")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Interview Question", description = "Interview question management API")
@Slf4j
public class InterviewQuestionRestController extends BaseRestController<InterviewQuestion, InterviewQuestionDTO, UUID, InterviewQuestionService, InterviewQuestionMapper> {

    private final TagService tagService;
    private final UserService userService;
    private final QuestionGenerationOrchestrationService questionGenerationService;

    /**
     * Constructor with service and mapper injection.
     *
     * @param questionService           the interview question service
     * @param interviewQuestionMapper   the interview question mapper
     * @param tagService                the tag service
     * @param userService               the user service
     * @param questionGenerationService the question generation service
     */
    public InterviewQuestionRestController(
            InterviewQuestionService questionService,
            InterviewQuestionMapper interviewQuestionMapper,
            TagService tagService,
            UserService userService,
            QuestionGenerationOrchestrationService questionGenerationService) {
        super(questionService, interviewQuestionMapper);
        this.tagService = tagService;
        this.userService = userService;
        this.questionGenerationService = questionGenerationService;
    }

    /**
     * Find questions by tag.
     *
     * @param tagSlug  the tag slug to search for
     * @param pageable pagination information
     * @return a page of questions with the specified tag
     */
    @GetMapping("/by-tag/{tagSlug}")
    @Operation(summary = "Find questions by tag", description = "Returns a page of questions with the specified tag")
    public ResponseEntity<Page<InterviewQuestionDTO>> getQuestionsByTag(
            @Parameter(description = "Tag slug to search for")
            @PathVariable String tagSlug,
            Pageable pageable) {
        log.debug("REST request to find questions by tag slug: {}", tagSlug);

        com.vladte.devhack.entities.Tag tag = tagService.findTagBySlug(tagSlug)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found with slug: " + tagSlug));

        Page<InterviewQuestion> page = service.findQuestionsByTag(tag, pageable);
        Page<InterviewQuestionDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Search questions with filtering and pagination.
     *
     * @param query      the search query
     * @param difficulty the difficulty level
     * @param tagId      the tag ID
     * @param pageable   pagination information
     * @return a page of questions matching the search criteria
     */
    @GetMapping("/search")
    @Operation(summary = "Search questions", description = "Returns a page of questions matching the search criteria")
    public ResponseEntity<Page<InterviewQuestionDTO>> searchQuestions(
            @Parameter(description = "Search query")
            @RequestParam(required = false) String query,
            @Parameter(description = "Difficulty level")
            @RequestParam(required = false) String difficulty,
            @Parameter(description = "Tag ID")
            @RequestParam(required = false) UUID tagId,
            Pageable pageable) {
        log.debug("REST request to search questions with query: {}, difficulty: {}, tagId: {}", query, difficulty, tagId);

        Page<InterviewQuestion> page = service.searchQuestions(query, difficulty, tagId, pageable);
        Page<InterviewQuestionDTO> dtoPage = page.map(mapper::toDTO);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Get question statistics for the authenticated user.
     *
     * @param user the authenticated user
     * @return a map with question statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get question statistics", description = "Returns question statistics for the authenticated user")
    public ResponseEntity<Map<String, Integer>> getQuestionStats(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.debug("REST request to get question statistics for user: {}", user.getName());

        int totalQuestions = service.countAllQuestions();
        int userQuestions = service.countQuestionsByUser(user);
        int answeredQuestions = service.findAnsweredQuestionsByUser(user);

        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalQuestions", totalQuestions);
        stats.put("userQuestions", userQuestions);
        stats.put("answeredQuestions", answeredQuestions);

        return ResponseEntity.ok(stats);
    }

    /**
     * Generate questions using AI.
     *
     * @param tagName    the tag name
     * @param count      the number of questions to generate
     * @param difficulty the difficulty level
     * @return a success message
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate questions using AI", description = "Initiates asynchronous generation of questions using AI based on the specified parameters")
    public ResponseEntity<Map<String, Object>> generateQuestions(
            @Parameter(description = "Tag name")
            @RequestParam String tagName,
            @Parameter(description = "Number of questions to generate")
            @RequestParam(defaultValue = "5") int count,
            @Parameter(description = "Difficulty level")
            @RequestParam(defaultValue = "MEDIUM") String difficulty) {
        log.debug("REST request to generate questions with tag: {}, count: {}, difficulty: {}", tagName, count, difficulty);

        // Validate tag name
        if (!questionGenerationService.validateTagName(tagName)) {
            return ResponseEntity.badRequest().body(
                    questionGenerationService.buildApiResponse(false, "Invalid tag name: " + tagName)
            );
        }

        // Start asynchronous generation
        questionGenerationService.startQuestionGeneration(tagName, count, difficulty);

        // Return success message
        String message = questionGenerationService.buildGenerationSuccessMessage(count, difficulty, tagName);
        return ResponseEntity.ok(questionGenerationService.buildApiResponse(true, message));
    }

    /**
     * Auto-generate easy questions for a tag.
     *
     * @param tagName the tag name
     * @return a success message
     */
    @PostMapping("/auto-generate")
    @Operation(summary = "Auto-generate easy questions for a tag", description = "Initiates asynchronous generation of easy questions for the specified tag")
    public ResponseEntity<Map<String, Object>> autoGenerateEasyQuestions(
            @Parameter(description = "Tag name")
            @RequestParam String tagName) {
        log.debug("REST request to auto-generate easy questions for tag: {}", tagName);

        // Validate tag name
        if (!questionGenerationService.validateTagName(tagName)) {
            return ResponseEntity.badRequest().body(
                    questionGenerationService.buildApiResponse(false, "Invalid tag name: " + tagName)
            );
        }

        // Start asynchronous generation
        questionGenerationService.startEasyQuestionGeneration(tagName);

        // Return success message
        String message = questionGenerationService.buildEasyGenerationSuccessMessage(tagName);
        return ResponseEntity.ok(questionGenerationService.buildApiResponse(true, message));
    }

    /**
     * Auto-generate easy questions for multiple tags.
     *
     * @param tagIds the tag IDs
     * @return a success message
     */
    @PostMapping("/auto-generate-multi")
    @Operation(summary = "Auto-generate easy questions for multiple tags", description = "Initiates asynchronous generation of easy questions for the specified tags")
    public ResponseEntity<Map<String, Object>> autoGenerateEasyQuestionsForMultipleTags(
            @Parameter(description = "Tag IDs")
            @RequestParam List<UUID> tagIds) {
        log.debug("REST request to auto-generate easy questions for tags: {}", tagIds);

        if (tagIds == null || tagIds.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    questionGenerationService.buildApiResponse(false, "No tag IDs provided")
            );
        }

        // Start asynchronous generation
        questionGenerationService.startEasyQuestionGenerationForMultipleTags(tagIds);

        // Return success message
        String message = questionGenerationService.buildMultiTagEasyGenerationSuccessMessage(tagIds.size());
        return ResponseEntity.ok(questionGenerationService.buildApiResponse(true, message));
    }
}
