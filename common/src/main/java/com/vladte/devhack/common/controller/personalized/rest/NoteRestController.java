package com.vladte.devhack.common.controller.personalized.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.model.dto.personalized.NoteDTO;
import com.vladte.devhack.common.model.mapper.personalized.NoteMapper;
import com.vladte.devhack.common.service.domain.global.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.personalized.NoteService;
import com.vladte.devhack.common.service.domain.user.UserService;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.personalized.Note;
import com.vladte.devhack.entities.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing Note entities.
 * Provides RESTful API endpoints for CRUD operations on notes.
 */
@RestController
@RequestMapping("/api/notes")
@Tag(name = "Note", description = "Note management API")
@Slf4j
public class NoteRestController extends BaseRestController<Note, NoteDTO, UUID, NoteService, NoteMapper> {

    private final InterviewQuestionService questionService;
    private final UserService userService;

    /**
     * Constructor with service and mapper injection.
     *
     * @param noteService     the note service
     * @param noteMapper      the note mapper
     * @param questionService the question service
     * @param userService     the user service
     */
    public NoteRestController(NoteService noteService,
                              NoteMapper noteMapper,
                              InterviewQuestionService questionService,
                              UserService userService) {
        super(noteService, noteMapper);
        this.questionService = questionService;
        this.userService = userService;
    }

    /**
     * Get all notes for the authenticated user.
     *
     * @param user the authenticated user
     * @return a list of notes
     */
    @GetMapping("/my-notes")
    @Operation(summary = "Get all notes for the authenticated user",
            description = "Returns a list of notes for the authenticated user")
    public ResponseEntity<List<NoteDTO>> getMyNotes(
            @Parameter(hidden = true)
            @AuthenticationPrincipal User user) {
        log.info("REST request to get all notes for user: {}", user.getProfile().getName());
        List<Note> notes = service.findNotesByUser(user);
        return ResponseEntity.ok(mapper.toDTOList(notes));
    }

    /**
     * Get all notes for a specific question.
     *
     * @param questionId the ID of the question
     * @return a list of notes
     */
    @GetMapping("/by-question/{questionId}")
    @Operation(summary = "Get all notes for a specific question",
            description = "Returns a list of notes for the specified question")
    public ResponseEntity<List<NoteDTO>> getNotesByQuestion(
            @Parameter(description = "ID of the question")
            @PathVariable UUID questionId) {
        log.debug("REST request to get all notes for question: {}", questionId);

        InterviewQuestion question = questionService.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        List<Note> notes = service.findNotesByLinkedQuestion(question);
        return ResponseEntity.ok(mapper.toDTOList(notes));
    }

    /**
     * Get all notes for a specific user.
     *
     * @param userId the ID of the user
     * @return a list of notes
     */
    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get all notes for a specific user",
            description = "Returns a list of notes for the specified user")
    public ResponseEntity<List<NoteDTO>> getNotesByUser(
            @Parameter(description = "ID of the user")
            @PathVariable UUID userId) {
        log.debug("REST request to get all notes for user: {}", userId);

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        List<Note> notes = service.findNotesByUser(user);
        return ResponseEntity.ok(mapper.toDTOList(notes));
    }

    /**
     * Get all notes for a specific user and question.
     *
     * @param userId     the ID of the user
     * @param questionId the ID of the question
     * @return a list of notes
     */
    @GetMapping("/by-user-and-question")
    @Operation(summary = "Get all notes for a specific user and question",
            description = "Returns a list of notes for the specified user and question")
    public ResponseEntity<List<NoteDTO>> getNotesByUserAndQuestion(
            @Parameter(description = "ID of the user")
            @RequestParam UUID userId,
            @Parameter(description = "ID of the question")
            @RequestParam UUID questionId) {
        log.debug("REST request to get all notes for user: {} and question: {}", userId, questionId);

        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        InterviewQuestion question = questionService.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        List<Note> notes = service.findNotesByUserAndLinkedQuestion(user, question);
        return ResponseEntity.ok(mapper.toDTOList(notes));
    }
}
