package com.vladte.devhack.common.repository;

import com.vladte.devhack.common.repository.specification.VacancyResponseSpecification;
import com.vladte.devhack.entities.InterviewStage;
import com.vladte.devhack.entities.Tag;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.VacancyResponse;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the VacancyResponseRepository.
 */
@DisplayName("Vacancy Response Repository Tests")
class VacancyResponseRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private VacancyResponseRepository vacancyResponseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    private User testUser;
    private Tag testTag;

    @BeforeEach
    void setup() {
        // Create and save a test user
        testUser = new User();
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        userRepository.save(testUser);

        // Create and save a test tag
        testTag = new Tag();
        testTag.setName("Java");
        tagRepository.save(testTag);

        // Clear any existing vacancy responses
        vacancyResponseRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save a vacancy response")
    @Description("Test that a vacancy response can be saved to the database")
    @Severity(SeverityLevel.BLOCKER)
    void testSaveVacancyResponse() {
        // Arrange
        VacancyResponse response = createTestVacancyResponse("Google", "Software Engineer");

        // Act
        VacancyResponse savedResponse = vacancyResponseRepository.save(response);

        // Assert
        assertNotNull(savedResponse.getId());
        assertNotNull(savedResponse.getCreatedAt());
        assertEquals("Google", savedResponse.getCompanyName());
        assertEquals("Software Engineer", savedResponse.getPosition());
        assertEquals(testUser.getId(), savedResponse.getUser().getId());
        assertEquals(InterviewStage.APPLIED, savedResponse.getInterviewStage());
    }

    @Test
    @DisplayName("Should find a vacancy response by ID")
    @Description("Test that a vacancy response can be retrieved by its ID")
    @Severity(SeverityLevel.CRITICAL)
    void testFindVacancyResponseById() {
        // Arrange
        VacancyResponse response = createTestVacancyResponse("Google", "Software Engineer");
        VacancyResponse savedResponse = vacancyResponseRepository.save(response);
        UUID responseId = savedResponse.getId();

        // Act
        Optional<VacancyResponse> foundResponse = vacancyResponseRepository.findById(responseId);

        // Assert
        assertTrue(foundResponse.isPresent());
        assertEquals(responseId, foundResponse.get().getId());
        assertEquals("Google", foundResponse.get().getCompanyName());
        assertEquals("Software Engineer", foundResponse.get().getPosition());
    }

    @Test
    @DisplayName("Should update a vacancy response")
    @Description("Test that a vacancy response can be updated in the database")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateVacancyResponse() {
        // Arrange
        VacancyResponse response = createTestVacancyResponse("Google", "Software Engineer");
        VacancyResponse savedResponse = vacancyResponseRepository.save(response);
        UUID responseId = savedResponse.getId();

        // Act
        savedResponse.setCompanyName("Microsoft");
        savedResponse.setPosition("Senior Developer");
        savedResponse.setInterviewStage(InterviewStage.TECHNICAL_INTERVIEW);
        VacancyResponse updatedResponse = vacancyResponseRepository.save(savedResponse);

        // Assert
        assertEquals(responseId, updatedResponse.getId());
        assertEquals("Microsoft", updatedResponse.getCompanyName());
        assertEquals("Senior Developer", updatedResponse.getPosition());
        assertEquals(InterviewStage.TECHNICAL_INTERVIEW, updatedResponse.getInterviewStage());
        assertNotNull(updatedResponse.getUpdatedAt());
    }

    @Test
    @DisplayName("Should delete a vacancy response")
    @Description("Test that a vacancy response can be deleted from the database")
    @Severity(SeverityLevel.CRITICAL)
    void testDeleteVacancyResponse() {
        // Arrange
        VacancyResponse response = createTestVacancyResponse("Google", "Software Engineer");
        VacancyResponse savedResponse = vacancyResponseRepository.save(response);
        UUID responseId = savedResponse.getId();

        // Act
        vacancyResponseRepository.deleteById(responseId);

        // Assert
        Optional<VacancyResponse> foundResponse = vacancyResponseRepository.findById(responseId);
        assertFalse(foundResponse.isPresent());
    }

    @Test
    @DisplayName("Should find vacancy responses by user specification")
    @Description("Test that vacancy responses can be found using the user specification")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByUserSpecification() {
        // Arrange
        VacancyResponse response1 = createTestVacancyResponse("Google", "Software Engineer");
        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Developer");
        vacancyResponseRepository.save(response1);
        vacancyResponseRepository.save(response2);

        // Create another user and response
        User anotherUser = new User();
        anotherUser.setName("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("password");
        userRepository.save(anotherUser);

        VacancyResponse response3 = createTestVacancyResponse("Amazon", "Developer");
        response3.setUser(anotherUser);
        vacancyResponseRepository.save(response3);

        // Act
        Specification<VacancyResponse> spec = VacancyResponseSpecification.byUser(testUser);
        List<VacancyResponse> responses = vacancyResponseRepository.findAll(spec);

        // Assert
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.getUser().getId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("Should find vacancy responses by company name specification")
    @Description("Test that vacancy responses can be found using the company name specification")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByCompanyNameSpecification() {
        // Arrange
        VacancyResponse response1 = createTestVacancyResponse("Google", "Software Engineer");
        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Developer");
        VacancyResponse response3 = createTestVacancyResponse("Google Cloud", "Cloud Engineer");
        vacancyResponseRepository.save(response1);
        vacancyResponseRepository.save(response2);
        vacancyResponseRepository.save(response3);

        // Act
        Specification<VacancyResponse> spec = VacancyResponseSpecification.byCompanyNameContainingIgnoreCase("Google");
        List<VacancyResponse> responses = vacancyResponseRepository.findAll(spec);

        // Assert
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.getCompanyName().toLowerCase().contains("google")));
    }

    @Test
    @DisplayName("Should find vacancy responses by position specification")
    @Description("Test that vacancy responses can be found using the position specification")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByPositionSpecification() {
        // Arrange
        VacancyResponse response1 = createTestVacancyResponse("Google", "Software Engineer");
        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Developer");
        VacancyResponse response3 = createTestVacancyResponse("Amazon", "Senior Software Engineer");
        vacancyResponseRepository.save(response1);
        vacancyResponseRepository.save(response2);
        vacancyResponseRepository.save(response3);

        // Act
        Specification<VacancyResponse> spec = VacancyResponseSpecification.byPositionContainingIgnoreCase("Engineer");
        List<VacancyResponse> responses = vacancyResponseRepository.findAll(spec);

        // Assert
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.getPosition().toLowerCase().contains("engineer")));
    }

    @Test
    @DisplayName("Should find vacancy responses by interview stage specification")
    @Description("Test that vacancy responses can be found using the interview stage specification")
    @Severity(SeverityLevel.CRITICAL)
    void testFindByInterviewStageSpecification() {
        // Arrange
        VacancyResponse response1 = createTestVacancyResponse("Google", "Software Engineer");
        response1.setInterviewStage(InterviewStage.APPLIED);

        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Developer");
        response2.setInterviewStage(InterviewStage.TECHNICAL_INTERVIEW);

        VacancyResponse response3 = createTestVacancyResponse("Amazon", "Senior Software Engineer");
        response3.setInterviewStage(InterviewStage.TECHNICAL_INTERVIEW);

        vacancyResponseRepository.save(response1);
        vacancyResponseRepository.save(response2);
        vacancyResponseRepository.save(response3);

        // Act
        Specification<VacancyResponse> spec = VacancyResponseSpecification.byInterviewStage(InterviewStage.TECHNICAL_INTERVIEW);
        List<VacancyResponse> responses = vacancyResponseRepository.findAll(spec);

        // Assert
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.getInterviewStage() == InterviewStage.TECHNICAL_INTERVIEW));
    }

    @Test
    @DisplayName("Should search vacancy responses with combined specifications")
    @Description("Test that vacancy responses can be searched using combined specifications")
    @Severity(SeverityLevel.CRITICAL)
    void testSearchVacancyResponses() {
        // Arrange
        VacancyResponse response1 = createTestVacancyResponse("Google", "Java Developer");
        response1.setInterviewStage(InterviewStage.APPLIED);
        response1.setTechnologies("Java, Spring, Hibernate");

        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Java Engineer");
        response2.setInterviewStage(InterviewStage.TECHNICAL_INTERVIEW);
        response2.setTechnologies("Java, .NET, Azure");

        VacancyResponse response3 = createTestVacancyResponse("Amazon", "Python Developer");
        response3.setInterviewStage(InterviewStage.TECHNICAL_INTERVIEW);
        response3.setTechnologies("Python, AWS, Django");

        vacancyResponseRepository.save(response1);
        vacancyResponseRepository.save(response2);
        vacancyResponseRepository.save(response3);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("companyName").ascending());

        // Act - Search by query and stage
        Specification<VacancyResponse> spec = VacancyResponseSpecification.searchVacancyResponses(
                "Java", InterviewStage.TECHNICAL_INTERVIEW);
        Page<VacancyResponse> responses = vacancyResponseRepository.findAll(spec, pageable);

        // Assert
        assertEquals(1, responses.getContent().size());
        assertEquals("Microsoft", responses.getContent().get(0).getCompanyName());
        assertEquals("Java Engineer", responses.getContent().get(0).getPosition());
        assertEquals(InterviewStage.TECHNICAL_INTERVIEW, responses.getContent().get(0).getInterviewStage());
    }

    @Test
    @DisplayName("Should combine multiple specifications")
    @Description("Test that multiple specifications can be combined with AND/OR logic")
    @Severity(SeverityLevel.CRITICAL)
    void testCombineSpecifications() {
        // Arrange
        VacancyResponse response1 = createTestVacancyResponse("Google", "Java Developer");
        response1.setInterviewStage(InterviewStage.APPLIED);

        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Java Engineer");
        response2.setInterviewStage(InterviewStage.TECHNICAL_INTERVIEW);

        VacancyResponse response3 = createTestVacancyResponse("Amazon", "Python Developer");
        response3.setInterviewStage(InterviewStage.TECHNICAL_INTERVIEW);

        vacancyResponseRepository.save(response1);
        vacancyResponseRepository.save(response2);
        vacancyResponseRepository.save(response3);

        // Act - Combine specifications with AND
        Specification<VacancyResponse> spec1 = VacancyResponseSpecification.byPositionContainingIgnoreCase("Developer");
        Specification<VacancyResponse> spec2 = VacancyResponseSpecification.byInterviewStage(InterviewStage.TECHNICAL_INTERVIEW);
        Specification<VacancyResponse> combinedAnd = spec1.and(spec2);

        List<VacancyResponse> andResults = vacancyResponseRepository.findAll(combinedAnd);

        // Assert
        assertEquals(1, andResults.size());
        assertEquals("Amazon", andResults.get(0).getCompanyName());

        // Act - Combine specifications with OR
        Specification<VacancyResponse> spec3 = VacancyResponseSpecification.byCompanyNameContainingIgnoreCase("Google");
        Specification<VacancyResponse> spec4 = VacancyResponseSpecification.byCompanyNameContainingIgnoreCase("Microsoft");
        Specification<VacancyResponse> combinedOr = spec3.or(spec4);

        List<VacancyResponse> orResults = vacancyResponseRepository.findAll(combinedOr);

        // Assert
        assertEquals(2, orResults.size());
        assertTrue(orResults.stream().anyMatch(r -> r.getCompanyName().equals("Google")));
        assertTrue(orResults.stream().anyMatch(r -> r.getCompanyName().equals("Microsoft")));
    }

    /**
     * Helper method to create a test vacancy response.
     */
    private VacancyResponse createTestVacancyResponse(String companyName, String position) {
        VacancyResponse response = new VacancyResponse();
        response.setCompanyName(companyName);
        response.setPosition(position);
        response.setTechnologies("Java, Spring, Hibernate");
        response.setUser(testUser);
        response.setInterviewStage(InterviewStage.APPLIED);
        return response;
    }
}
