package com.vladte.devhack.common.controller;

import com.vladte.devhack.common.service.domain.InterviewQuestionService;
import com.vladte.devhack.common.service.domain.NoteService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.common.service.view.ModelBuilder;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Note;
import com.vladte.devhack.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/notes")
public class NoteController extends UserEntityController<Note, UUID, NoteService> {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final InterviewQuestionService questionService;

    @Autowired
    public NoteController(NoteService noteService, UserService userService, InterviewQuestionService questionService) {
        super(noteService, userService);
        this.questionService = questionService;
    }

    @Override
    protected User getEntityUser(Note entity) {
        return entity.getUser();
    }

    @Override
    protected String getListViewName() {
        return "notes/list";
    }

    @Override
    protected String getDetailViewName() {
        return "notes/view";
    }

    @Override
    protected String getListPageTitle() {
        return "Notes";
    }

    @Override
    protected String getDetailPageTitle() {
        return "Note Details";
    }

    @Override
    protected String getEntityName() {
        return "Note";
    }

    @Override
    public String view(@PathVariable UUID id, Model model) {
        logger.debug("Viewing note with ID: {} with access control", id);

        // Get the note from the service
        Note note = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + id));

        // Check if the current user has access to the note
        if (!hasAccessToEntity(note)) {
            logger.warn("Access denied to note with ID: {}", id);
            throw new SecurityException("Access denied to note with ID: " + id);
        }

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        ModelBuilder.of(model)
                .addAttribute("note", note)
                .addAttribute("currentUser", currentUser)
                .setPageTitle("Note Details")
                .build();

        return "notes/view";
    }

    @GetMapping("/new")
    public String newNoteForm(@RequestParam(required = false) UUID questionId, Model model) {
        logger.debug("Displaying new note form with access control");

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        Note note = new Note();

        // Create a ModelBuilder instance
        ModelBuilder modelBuilder = ModelBuilder.of(model)
                .addAttribute("note", note)
                .addAttribute("users", userService.findAll())
                .addAttribute("questions", questionService.findAll())
                .addAttribute("currentUser", currentUser)
                .setPageTitle("Create New Note");

        if (questionId != null) {
            Optional<InterviewQuestion> questionOpt = questionService.findById(questionId);
            questionOpt.ifPresent(question -> {
                note.setQuestion(question);
                modelBuilder.addAttribute("question", question);
            });
        }

        modelBuilder.build();
        return "notes/form";
    }

    @GetMapping("/{id}/edit")
    public String editNoteForm(@PathVariable UUID id, Model model) {
        logger.debug("Editing note with ID: {} with access control", id);

        // Get the note from the service
        Note note = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + id));

        // Check if the current user has access to the note
        if (!hasAccessToEntity(note)) {
            logger.warn("Access denied to edit note with ID: {}", id);
            throw new SecurityException("Access denied to edit note with ID: " + id);
        }

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        ModelBuilder.of(model)
                .addAttribute("note", note)
                .addAttribute("users", userService.findAll())
                .addAttribute("questions", questionService.findAll())
                .addAttribute("currentUser", currentUser)
                .setPageTitle("Edit Note")
                .build();

        return "notes/form";
    }

    @PostMapping
    public String saveNote(
            @ModelAttribute Note note,
            @RequestParam UUID userId,
            @RequestParam UUID questionId) {
        logger.debug("Saving note with access control");

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Check if the current user is a manager or is saving their own note
        if (!isCurrentUserManager() && !currentUser.getId().equals(userId)) {
            logger.warn("Access denied to save note for user with ID: {}", userId);
            throw new SecurityException("Access denied to save note for user with ID: " + userId);
        }

        User user = getEntityOrThrow(userService.findById(userId), "User not found");
        InterviewQuestion question = getEntityOrThrow(questionService.findById(questionId), "Question not found");

        note.setUser(user);
        note.setQuestion(question);

        service.save(note);
        logger.info("Note saved successfully");

        return "redirect:/notes";
    }

    @GetMapping("/{id}/delete")
    public String deleteNote(@PathVariable UUID id) {
        logger.debug("Deleting note with ID: {} with access control", id);

        // Get the note from the service
        Note note = service.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found with ID: " + id));

        // Check if the current user has access to the note
        if (!hasAccessToEntity(note)) {
            logger.warn("Access denied to delete note with ID: {}", id);
            throw new SecurityException("Access denied to delete note with ID: " + id);
        }

        // Delete the note
        service.deleteById(id);

        logger.info("Note with ID: {} deleted successfully", id);
        return "redirect:/notes";
    }

    @GetMapping("/user/{userId}")
    public String getNotesByUser(@PathVariable UUID userId, Model model) {
        logger.debug("Getting notes for user with ID: {} with access control", userId);

        // Get the user from the service
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Check if the current user is a manager or is viewing their own notes
        if (!isCurrentUserManager() && !currentUser.getId().equals(userId)) {
            logger.warn("Access denied to view notes for user with ID: {}", userId);
            throw new SecurityException("Access denied to view notes for user with ID: " + userId);
        }

        // Get notes for the user
        List<Note> notes = service.findNotesByUser(user);

        ModelBuilder.of(model)
                .addAttribute("notes", notes)
                .addAttribute("user", user)
                .addAttribute("currentUser", currentUser)
                .setPageTitle("Notes by " + user.getName())
                .build();

        return "notes/list";
    }

    @GetMapping("/question/{questionId}")
    public String getNotesByQuestion(@PathVariable UUID questionId, Model model) {
        logger.debug("Getting notes for question with ID: {} with access control", questionId);

        // Get the question from the service
        InterviewQuestion question = questionService.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + questionId));

        // Get the current authenticated user
        User currentUser = getCurrentUser();

        // Check if the current user is a manager or has access to the question
        // For simplicity, we'll allow access if the user is a manager
        // In a real application, you might want to check if the user has access to the question based on other criteria
        if (!isCurrentUserManager()) {
            logger.warn("Access denied to view notes for question with ID: {}", questionId);
            throw new SecurityException("Access denied to view notes for question with ID: " + questionId);
        }

        // Get notes for the question
        List<Note> notes = service.findNotesByLinkedQuestion(question);

        ModelBuilder.of(model)
                .addAttribute("notes", notes)
                .addAttribute("question", question)
                .addAttribute("currentUser", currentUser)
                .setPageTitle("Notes for Question: " + question.getQuestionText())
                .build();

        return "notes/list";
    }
}
