package com.vladte.devhack.common.repository;

import com.vladte.devhack.common.repository.global.InterviewQuestionRepository;
import com.vladte.devhack.common.repository.personalized.NoteRepository;
import com.vladte.devhack.common.repository.user.UserRepository;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.personalized.Note;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import com.vladte.devhack.entities.user.Profile;
import com.vladte.devhack.entities.user.User;
import com.vladte.devhack.entities.user.UserAccess;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the NoteRepository.
 */
@DisplayName("Note Repository Tests")
class NoteRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterviewQuestionRepository questionRepository;

    private User testUser;
    private InterviewQuestion testQuestion;

    @BeforeEach
    void setup() {
        // Create and save a test user
        testUser = createTestUser("testuser", "test@example.com");
        userRepository.save(testUser);

        // Create and save a test question
        testQuestion = new InterviewQuestion();
        testQuestion.setQuestionText("Test question?");
        testQuestion.setDifficulty("Medium");
        testQuestion.setUser(testUser);
        questionRepository.save(testQuestion);

        // Clear any existing notes
        noteRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save a note")
    @Description("Test that a note can be saved to the database")
    @Severity(SeverityLevel.BLOCKER)
    void testSaveNote() {
        // Arrange
        Note note = createTestNote("This is a test note");

        // Act
        Note savedNote = noteRepository.save(note);

        // Assert
        assertNotNull(savedNote.getId());
        assertNotNull(savedNote.getCreatedAt());
        assertEquals("This is a test note", savedNote.getNoteText());
        assertEquals(testUser.getId(), savedNote.getUser().getId());
        assertEquals(testQuestion.getId(), savedNote.getQuestion().getId());
    }

    @Test
    @DisplayName("Should find a note by ID")
    @Description("Test that a note can be retrieved by its ID")
    @Severity(SeverityLevel.CRITICAL)
    void testFindNoteById() {
        // Arrange
        Note note = createTestNote("This is a test note");
        Note savedNote = noteRepository.save(note);
        UUID noteId = savedNote.getId();

        // Act
        Optional<Note> foundNote = noteRepository.findById(noteId);

        // Assert
        assertTrue(foundNote.isPresent());
        assertEquals(noteId, foundNote.get().getId());
        assertEquals("This is a test note", foundNote.get().getNoteText());
    }

    @Test
    @DisplayName("Should update a note")
    @Description("Test that a note can be updated in the database")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateNote() {
        // Arrange
        Note note = createTestNote("Original note");
        Note savedNote = noteRepository.save(note);
        UUID noteId = savedNote.getId();

        // Act
        savedNote.setNoteText("Updated note");
        Note updatedNote = noteRepository.save(savedNote);

        // Assert
        assertEquals(noteId, updatedNote.getId());
        assertEquals("Updated note", updatedNote.getNoteText());
        assertNotNull(updatedNote.getUpdatedAt());
    }

    @Test
    @DisplayName("Should delete a note")
    @Description("Test that a note can be deleted from the database")
    @Severity(SeverityLevel.CRITICAL)
    void testDeleteNote() {
        // Arrange
        Note note = createTestNote("This is a test note");
        Note savedNote = noteRepository.save(note);
        UUID noteId = savedNote.getId();

        // Act
        noteRepository.deleteById(noteId);

        // Assert
        Optional<Note> foundNote = noteRepository.findById(noteId);
        assertFalse(foundNote.isPresent());
    }

    @Test
    @DisplayName("Should find notes by user")
    @Description("Test that notes can be found by user")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByUser() {
        // Arrange
        Note note1 = createTestNote("Note 1");
        Note note2 = createTestNote("Note 2");
        noteRepository.save(note1);
        noteRepository.save(note2);

        // Act
        List<Note> notes = noteRepository.findByUser(testUser);

        // Assert
        assertEquals(2, notes.size());
        assertTrue(notes.stream().anyMatch(n -> n.getNoteText().equals("Note 1")));
        assertTrue(notes.stream().anyMatch(n -> n.getNoteText().equals("Note 2")));
    }

    @Test
    @DisplayName("Should find notes by question")
    @Description("Test that notes can be found by question")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByQuestion() {
        // Arrange
        Note note1 = createTestNote("Note 1");
        Note note2 = createTestNote("Note 2");
        noteRepository.save(note1);
        noteRepository.save(note2);

        // Act
        List<Note> notes = noteRepository.findByQuestion(testQuestion);

        // Assert
        assertEquals(2, notes.size());
        assertTrue(notes.stream().anyMatch(n -> n.getNoteText().equals("Note 1")));
        assertTrue(notes.stream().anyMatch(n -> n.getNoteText().equals("Note 2")));
    }

    @Test
    @DisplayName("Should find notes by user and question")
    @Description("Test that notes can be found by both user and question")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByUserAndQuestion() {
        // Arrange
        Note note1 = createTestNote("Note 1");
        Note note2 = createTestNote("Note 2");
        noteRepository.save(note1);
        noteRepository.save(note2);

        // Create another user and question
        User anotherUser = createTestUser("anotheruser", "another@example.com");
        userRepository.save(anotherUser);

        InterviewQuestion anotherQuestion = new InterviewQuestion();
        anotherQuestion.setQuestionText("Another question?");
        anotherQuestion.setDifficulty("Easy");
        anotherQuestion.setUser(anotherUser);
        questionRepository.save(anotherQuestion);

        // Create notes with different user/question combinations
        Note note3 = new Note();
        note3.setNoteText("Note 3");
        note3.setUser(anotherUser);
        note3.setQuestion(testQuestion);
        noteRepository.save(note3);

        Note note4 = new Note();
        note4.setNoteText("Note 4");
        note4.setUser(testUser);
        note4.setQuestion(anotherQuestion);
        noteRepository.save(note4);

        // Act
        List<Note> notes = noteRepository.findByUserAndQuestion(testUser, testQuestion);

        // Assert
        assertEquals(2, notes.size());
        assertTrue(notes.stream().anyMatch(n -> n.getNoteText().equals("Note 1")));
        assertTrue(notes.stream().anyMatch(n -> n.getNoteText().equals("Note 2")));
    }

    @Test
    @DisplayName("Should find notes by user with pagination")
    @Description("Test that notes can be found by user with pagination")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByUserPaginated() {
        // Arrange
        for (int i = 1; i <= 10; i++) {
            Note note = createTestNote("Note " + i);
            noteRepository.save(note);
        }

        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());

        // Act
        Page<Note> notePage = noteRepository.findByUser(testUser, pageable);

        // Assert
        assertEquals(5, notePage.getContent().size());
        assertEquals(10, notePage.getTotalElements());
        assertEquals(2, notePage.getTotalPages());
    }

    @Test
    @DisplayName("Should search notes with filters")
    @Description("Test that notes can be searched with text, user ID, and question ID filters")
    @Severity(SeverityLevel.CRITICAL)
    void testSearchNotes() {
        // Arrange
        Note note1 = createTestNote("Java programming note");
        Note note2 = createTestNote("Python programming note");
        Note note3 = createTestNote("JavaScript programming note");
        noteRepository.save(note1);
        noteRepository.save(note2);
        noteRepository.save(note3);

        Pageable pageable = PageRequest.of(0, 10);

        // Act - Search by text
        Page<Note> javaNotes = noteRepository.searchNotes("Java", null, null, pageable);

        // Assert
        assertEquals(2, javaNotes.getContent().size());
        assertTrue(javaNotes.getContent().stream().anyMatch(n -> n.getNoteText().equals("Java programming note")));
        assertTrue(javaNotes.getContent().stream().anyMatch(n -> n.getNoteText().equals("JavaScript programming note")));

        // Act - Search by user ID
        Page<Note> userNotes = noteRepository.searchNotes(null, testUser.getId(), null, pageable);

        // Assert
        assertEquals(3, userNotes.getContent().size());

        // Act - Search by question ID
        Page<Note> questionNotes = noteRepository.searchNotes(null, null, testQuestion.getId(), pageable);

        // Assert
        assertEquals(3, questionNotes.getContent().size());

        // Act - Search with all filters
        Page<Note> filteredNotes = noteRepository.searchNotes(
                "Java", testUser.getId(), testQuestion.getId(), pageable);

        // Assert
        assertEquals(2, filteredNotes.getContent().size());
    }

    /**
     * Helper method to create a test note.
     */
    private Note createTestNote(String noteText) {
        Note note = new Note();
        note.setNoteText(noteText);
        note.setUser(testUser);
        note.setQuestion(testQuestion);
        return note;
    }

    /**
     * Helper method to create a test user with proper entity structure.
     */
    private User createTestUser(String name, String email) {
        User user = new User();

        // Create Profile
        Profile profile = new Profile();
        profile.setName(name);
        profile.setUser(user);
        user.setProfile(profile);

        // Create AuthenticationProvider for LOCAL authentication
        AuthenticationProvider localAuth = new AuthenticationProvider();
        localAuth.setProvider(AuthProviderType.LOCAL);
        localAuth.setEmail(email);
        localAuth.setPasswordHash("password"); // This would normally be encoded
        localAuth.setUser(user);
        user.setAuthProviders(List.of(localAuth));

        // Create UserAccess
        UserAccess userAccess = new UserAccess();
        userAccess.setRole("USER");
        userAccess.setUser(user);
        user.setUserAccess(userAccess);

        return user;
    }
}
