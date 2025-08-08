package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.global.InterviewQuestionDTO;
import com.vladte.devhack.common.model.mapper.global.InterviewQuestionMapper;
import com.vladte.devhack.common.service.domain.global.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.global.TagService;
import com.vladte.devhack.common.service.generations.QuestionGenerationOrchestrationService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.Setter;
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
 * Clean REST controller for managing InterviewQuestion entities.
 * Provides comprehensive RESTful API endpoints for all question operations.
 * Follows the Single Responsibility Principle by handling only API concerns.
 */
@RestController
@RequestMapping("/api/questions")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Questions", description = "Question management API")
@Slf4j
public class InterviewQuestionRestController extends BaseRestController<InterviewQuestion, InterviewQuestionDTO, UUID, InterviewQuestionService, InterviewQuestionMapper> {

    private final TagService tagService;
    private final QuestionGenerationOrchestrationService questionGenerationService;

    /**
     * Constructor with service and mapper injection.
     *
     * @param questionService           the interview question service
     * @param interviewQuestionMapper   the interview question mapper
     * @param tagService                the tag service
     * @param questionGenerationService the question generation service
     */
    public InterviewQuestionRestController(
            InterviewQuestionService questionService,
            InterviewQuestionMapper interviewQuestionMapper,
            TagService tagService,
            QuestionGenerationOrchestrationService questionGenerationService) {
        super(questionService, interviewQuestionMapper);
        this.tagService = tagService;
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

        Tag tag = tagService.findTagBySlug(tagSlug)
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
        log.debug("REST request to get question statistics for user: {}", user.getProfile().getName());

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
     * Generate questions using AI with custom parameters.
     *
     * @param request the generation request containing all parameters
     * @return a success response with generation details
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate questions using AI", description = "Initiates asynchronous generation of questions using AI based on the specified parameters")
    public ResponseEntity<Map<String, Object>> generateQuestions(
            @RequestBody @Parameter(description = "Generation request parameters") GenerateQuestionsRequest request) {
        log.debug("REST request to generate questions with request: {}", request);

        // Validate request
        if (questionGenerationService.isTagInvalid(request.getTopic())) {
            return ResponseEntity.badRequest().body(
                    questionGenerationService.buildApiResponse(false, "Invalid topic: " + request.getTopic())
            );
        }

        if (request.getCount() < 1 || request.getCount() > 20) {
            return ResponseEntity.badRequest().body(
                    questionGenerationService.buildApiResponse(false, "Question count must be between 1 and 20")
            );
        }

        // Start asynchronous generation
        questionGenerationService.startQuestionGeneration(
                request.getTopic(),
                request.getCount(),
                request.getDifficulty()
        );

        // Return success response
        String message = questionGenerationService.buildGenerationSuccessMessage(
                request.getCount(),
                request.getDifficulty(),
                request.getTopic()
        );

        Map<String, Object> response = questionGenerationService.buildApiResponse(true, message);
        response.put("estimatedTime", "30 seconds");
        response.put("topic", request.getTopic());

        return ResponseEntity.ok(response);
    }

    /**
     * Auto-generate easy questions for a specific topic.
     *
     * @param tagName the tag name to generate questions for
     * @return a success response
     */
    @PostMapping("/generate/auto")
    @Operation(summary = "Auto-generate easy questions", description = "Generates 3 easy questions for the specified topic")
    public ResponseEntity<Map<String, Object>> autoGenerateEasyQuestions(
            @Parameter(description = "Tag name")
            @RequestParam String tagName) {
        log.debug("REST request to auto-generate easy questions for tag: {}", tagName);

        // Validate tag name
        if (questionGenerationService.isTagInvalid(tagName)) {
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
     * Auto-generate easy questions for multiple topics.
     *
     * @param tagIds the list of tag IDs to generate questions for
     * @return a success response
     */
    @PostMapping("/generate/multi")
    @Operation(summary = "Auto-generate easy questions for multiple topics", description = "Generates easy questions for multiple specified topics")
    public ResponseEntity<Map<String, Object>> autoGenerateEasyQuestionsForMultipleTags(
            @Parameter(description = "List of tag IDs")
            @RequestParam("tagIds") List<UUID> tagIds) {
        log.debug("REST request to auto-generate easy questions for {} tags", tagIds.size());

        // Validate input
        if (tagIds == null || tagIds.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    questionGenerationService.buildApiResponse(false, "At least one tag must be selected")
            );
        }

        if (tagIds.size() > 10) {
            return ResponseEntity.badRequest().body(
                    questionGenerationService.buildApiResponse(false, "Maximum 10 tags allowed per request")
            );
        }

        // Start asynchronous generation
        questionGenerationService.startEasyQuestionGenerationForMultipleTags(tagIds);

        // Return success message
        String message = questionGenerationService.buildMultiTagEasyGenerationSuccessMessage(tagIds.size());
        return ResponseEntity.ok(questionGenerationService.buildApiResponse(true, message));
    }

    /**
     * Data class for question generation requests.
     */
    @Setter
    @Getter
    public static class GenerateQuestionsRequest {
        // Getters and setters
        private String topic;
        private int count = 5;
        private String difficulty = "Medium";
        private String type = "mixed";
        private String experience = "junior";

        // Constructors
        public GenerateQuestionsRequest() {
        }

        public GenerateQuestionsRequest(String topic, int count, String difficulty, String type, String experience) {
            this.topic = topic;
            this.count = count;
            this.difficulty = difficulty;
            this.type = type;
            this.experience = experience;
        }

        @Override
        public String toString() {
            return "GenerateQuestionsRequest{" +
                    "topic='" + topic + '\'' +
                    ", count=" + count +
                    ", difficulty='" + difficulty + '\'' +
                    ", type='" + type + '\'' +
                    ", experience='" + experience + '\'' +
                    '}';
        }
    }
}
