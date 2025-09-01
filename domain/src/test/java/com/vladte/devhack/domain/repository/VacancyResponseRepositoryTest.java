package com.vladte.devhack.domain.repository;

import com.vladte.devhack.domain.entities.enums.AuthProviderType;
import com.vladte.devhack.domain.entities.global.InterviewStage;
import com.vladte.devhack.domain.entities.global.InterviewStageCategory;
import com.vladte.devhack.domain.entities.global.Tag;
import com.vladte.devhack.domain.entities.personalized.VacancyResponse;
import com.vladte.devhack.domain.entities.user.AuthenticationProvider;
import com.vladte.devhack.domain.entities.user.Profile;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.entities.user.UserAccess;
import com.vladte.devhack.domain.repository.global.InterviewStageCategoryRepository;
import com.vladte.devhack.domain.repository.global.InterviewStageRepository;
import com.vladte.devhack.domain.repository.global.TagRepository;
import com.vladte.devhack.domain.repository.personalized.VacancyResponseRepository;
import com.vladte.devhack.domain.repository.personalized.specification.VacancyResponseSpecification;
import com.vladte.devhack.domain.repository.user.UserRepository;
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

    @Autowired
    private InterviewStageRepository interviewStageRepository;

    @Autowired
    private InterviewStageCategoryRepository interviewStageCategoryRepository;

    private User testUser;
    private Tag testTag;
    private InterviewStage appliedStage;
    private InterviewStage technicalInterviewStage;

    @BeforeEach
    void setup() {
        // Create and save a test user
        testUser = createTestUser("testuser", "test@example.com");
        userRepository.save(testUser);

        // Create and save a test tag
        testTag = new Tag();
        testTag.setName("Java");
        tagRepository.save(testTag);

        // Create interview stage categories
        InterviewStageCategory initialCategory = InterviewStageCategory.builder()
                .code("INITIAL")
                .label("Initial Stage")
                .build();
        interviewStageCategoryRepository.save(initialCategory);

        InterviewStageCategory technicalCategory = InterviewStageCategory.builder()
                .code("TECHNICAL")
                .label("Technical Assessment")
                .build();
        interviewStageCategoryRepository.save(technicalCategory);

        // Create interview stages
        appliedStage = InterviewStage.builder()
                .code("APPLIED")
                .label("Applied")
                .orderIndex(1)
                .active(true)
                .finalStage(false)
                .category(initialCategory)
                .build();
        interviewStageRepository.save(appliedStage);

        technicalInterviewStage = InterviewStage.builder()
                .code("TECHNICAL_INTERVIEW")
                .label("Technical Interview")
                .orderIndex(4)
                .active(true)
                .finalStage(false)
                .category(technicalCategory)
                .build();
        interviewStageRepository.save(technicalInterviewStage);

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
        assertEquals(appliedStage, savedResponse.getInterviewStage());
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
        savedResponse.setInterviewStage(technicalInterviewStage);
        VacancyResponse updatedResponse = vacancyResponseRepository.save(savedResponse);

        // Assert
        assertEquals(responseId, updatedResponse.getId());
        assertEquals("Microsoft", updatedResponse.getCompanyName());
        assertEquals("Senior Developer", updatedResponse.getPosition());
        assertEquals(technicalInterviewStage, updatedResponse.getInterviewStage());
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
        User anotherUser = createTestUser("anotheruser", "another@example.com");
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
        response1.setInterviewStage(appliedStage);

        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Developer");
        response2.setInterviewStage(technicalInterviewStage);

        VacancyResponse response3 = createTestVacancyResponse("Amazon", "Senior Software Engineer");
        response3.setInterviewStage(technicalInterviewStage);

        vacancyResponseRepository.save(response1);
        vacancyResponseRepository.save(response2);
        vacancyResponseRepository.save(response3);

        // Act
        Specification<VacancyResponse> spec = VacancyResponseSpecification.byInterviewStage(technicalInterviewStage);
        List<VacancyResponse> responses = vacancyResponseRepository.findAll(spec);

        // Assert
        assertEquals(2, responses.size());
        assertTrue(responses.stream().allMatch(r -> r.getInterviewStage().equals(technicalInterviewStage)));
    }

    @Test
    @DisplayName("Should search vacancy responses with combined specifications")
    @Description("Test that vacancy responses can be searched using combined specifications")
    @Severity(SeverityLevel.CRITICAL)
    void testSearchVacancyResponses() {
        // Arrange
        VacancyResponse response1 = createTestVacancyResponse("Google", "Java Developer");
        response1.setInterviewStage(appliedStage);
        response1.setTechnologies("Java, Spring, Hibernate");

        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Java Engineer");
        response2.setInterviewStage(technicalInterviewStage);
        response2.setTechnologies("Java, .NET, Azure");

        VacancyResponse response3 = createTestVacancyResponse("Amazon", "Python Developer");
        response3.setInterviewStage(technicalInterviewStage);
        response3.setTechnologies("Python, AWS, Django");

        vacancyResponseRepository.save(response1);
        vacancyResponseRepository.save(response2);
        vacancyResponseRepository.save(response3);

        Pageable pageable = PageRequest.of(0, 10, Sort.by("companyName").ascending());

        // Act - Search by query and stage
        Specification<VacancyResponse> spec = VacancyResponseSpecification.searchVacancyResponses(
                "Java", technicalInterviewStage);
        Page<VacancyResponse> responses = vacancyResponseRepository.findAll(spec, pageable);

        // Assert
        assertEquals(1, responses.getContent().size());
        assertEquals("Microsoft", responses.getContent().get(0).getCompanyName());
        assertEquals("Java Engineer", responses.getContent().get(0).getPosition());
        assertEquals(technicalInterviewStage, responses.getContent().get(0).getInterviewStage());
    }

    @Test
    @DisplayName("Should combine multiple specifications")
    @Description("Test that multiple specifications can be combined with AND/OR logic")
    @Severity(SeverityLevel.CRITICAL)
    void testCombineSpecifications() {
        // Arrange
        VacancyResponse response1 = createTestVacancyResponse("Google", "Java Developer");
        response1.setInterviewStage(appliedStage);

        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Java Engineer");
        response2.setInterviewStage(technicalInterviewStage);

        VacancyResponse response3 = createTestVacancyResponse("Amazon", "Python Developer");
        response3.setInterviewStage(technicalInterviewStage);

        vacancyResponseRepository.save(response1);
        vacancyResponseRepository.save(response2);
        vacancyResponseRepository.save(response3);

        // Act - Combine specifications with AND
        Specification<VacancyResponse> spec1 = VacancyResponseSpecification.byPositionContainingIgnoreCase("Developer");
        Specification<VacancyResponse> spec2 = VacancyResponseSpecification.byInterviewStage(technicalInterviewStage);
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
        response.setInterviewStage(appliedStage);
        return response;
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
