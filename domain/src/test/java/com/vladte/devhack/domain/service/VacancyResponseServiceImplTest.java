package com.vladte.devhack.domain.service;

import com.vladte.devhack.domain.entities.global.InterviewStage;
import com.vladte.devhack.domain.entities.global.Vacancy;
import com.vladte.devhack.domain.entities.personalized.VacancyResponse;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.repository.personalized.VacancyResponseRepository;
import com.vladte.devhack.domain.service.audit.AuditService;
import com.vladte.devhack.domain.service.global.InterviewStageService;
import com.vladte.devhack.domain.service.personalized.impl.VacancyResponseServiceImpl;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Vacancy Response Service Implementation Tests")
class VacancyResponseServiceImplTest extends BaseServiceTest {

    @Mock
    private VacancyResponseRepository repository;

    @Mock
    private AuditService auditService;

    @Mock
    private InterviewStageService interviewStageService;

    private VacancyResponseServiceImpl vacancyResponseService;

    @BeforeEach
    void setUp() {
        vacancyResponseService = new VacancyResponseServiceImpl(repository, auditService, interviewStageService);
    }

    @Test
    @DisplayName("Should get vacancy responses by user")
    @Description("Test that getVacancyResponsesByUser returns paginated responses for a specific user")
    @Severity(SeverityLevel.CRITICAL)
    void testGetVacancyResponsesByUser() {
        // Given
        User user = createTestUser();
        Pageable pageable = PageRequest.of(0, 10);

        VacancyResponse response1 = createTestVacancyResponse("Google", "Software Engineer");
        VacancyResponse response2 = createTestVacancyResponse("Microsoft", "Developer");
        List<VacancyResponse> responses = List.of(response1, response2);
        Page<VacancyResponse> expectedPage = new PageImpl<>(responses);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        // When
        Page<VacancyResponse> result = vacancyResponseService.getVacancyResponsesByUser(user, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(expectedPage, result);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should search vacancy responses")
    @Description("Test that searchVacancyResponses returns filtered responses based on query and stage")
    @Severity(SeverityLevel.CRITICAL)
    void testSearchVacancyResponses() {
        // Given
        String query = "Java Developer";
        InterviewStage stage = createTestInterviewStage("TECHNICAL_INTERVIEW", 3);
        Pageable pageable = PageRequest.of(0, 10);

        VacancyResponse response1 = createTestVacancyResponse("TechCorp", "Java Developer");
        VacancyResponse response2 = createTestVacancyResponse("DevCompany", "Senior Java Developer");
        List<VacancyResponse> responses = List.of(response1, response2);
        Page<VacancyResponse> expectedPage = new PageImpl<>(responses);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        // When
        Page<VacancyResponse> result = vacancyResponseService.searchVacancyResponses(query, stage, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(expectedPage, result);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should search vacancy responses with null parameters")
    @Description("Test that searchVacancyResponses handles null query and stage correctly")
    @Severity(SeverityLevel.NORMAL)
    void testSearchVacancyResponsesWithNullParameters() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        VacancyResponse response = createTestVacancyResponse("AnyCompany", "Any Position");
        Page<VacancyResponse> expectedPage = new PageImpl<>(List.of(response));

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(expectedPage);

        // When
        Page<VacancyResponse> result = vacancyResponseService.searchVacancyResponses(null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(expectedPage, result);
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Should save new response for user and vacancy")
    @Description("Test that saveNewResponseForUserAndVacancy creates a new response with initial stage")
    @Severity(SeverityLevel.CRITICAL)
    void testSaveNewResponseForUserAndVacancy() {
        // Given
        User user = createTestUser();
        Vacancy vacancy = createTestVacancy("Software Engineer", "TechCorp");
        InterviewStage firstStage = createTestInterviewStage("APPLIED", 1);

        VacancyResponse savedResponse = createTestVacancyResponse("TechCorp", "Software Engineer");
        savedResponse.setUser(user);
        savedResponse.setVacancy(vacancy);
        savedResponse.setInterviewStage(firstStage);

        when(interviewStageService.findFirstStage()).thenReturn(Optional.of(firstStage));
        when(repository.save(any(VacancyResponse.class))).thenReturn(savedResponse);

        // When
        VacancyResponse result = vacancyResponseService.saveNewResponseForUserAndVacancy(user, vacancy);

        // Then
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals(vacancy, result.getVacancy());
        assertEquals(firstStage, result.getInterviewStage());
        verify(interviewStageService).findFirstStage();
        verify(repository).save(any(VacancyResponse.class));
    }

    @Test
    @DisplayName("Should throw exception when first stage not found")
    @Description("Test that saveNewResponseForUserAndVacancy throws exception when first stage is not found")
    @Severity(SeverityLevel.NORMAL)
    void testSaveNewResponseForUserAndVacancyFirstStageNotFound() {
        // Given
        User user = createTestUser();
        Vacancy vacancy = createTestVacancy("Software Engineer", "TechCorp");

        when(interviewStageService.findFirstStage()).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            vacancyResponseService.saveNewResponseForUserAndVacancy(user, vacancy);
        });
        verify(interviewStageService).findFirstStage();
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should get vacancy responses by vacancy")
    @Description("Test that getVacancyResponsesByVacancy returns all responses for a specific vacancy")
    @Severity(SeverityLevel.NORMAL)
    void testGetVacancyResponsesByVacancy() {
        // Given
        Vacancy vacancy = createTestVacancy("Backend Developer", "StartupCorp");

        VacancyResponse response1 = createTestVacancyResponse("StartupCorp", "Backend Developer");
        VacancyResponse response2 = createTestVacancyResponse("StartupCorp", "Backend Developer");
        List<VacancyResponse> expectedResponses = List.of(response1, response2);

        when(repository.findAllByVacancy(vacancy)).thenReturn(expectedResponses);

        // When
        List<VacancyResponse> result = vacancyResponseService.getVacancyResponsesByVacancy(vacancy);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedResponses, result);
        verify(repository).findAllByVacancy(vacancy);
    }

    @Test
    @DisplayName("Should find vacancy responses by stage for user")
    @Description("Test that findVacancyResponsesByStageForUser returns responses for specific stage and user")
    @Severity(SeverityLevel.NORMAL)
    void testFindVacancyResponsesByStageForUser() {
        // Given
        User user = createTestUser();
        Integer stageIndex = 2;

        VacancyResponse response1 = createTestVacancyResponse("Company1", "Position1");
        VacancyResponse response2 = createTestVacancyResponse("Company2", "Position2");
        List<VacancyResponse> expectedResponses = List.of(response1, response2);

        when(repository.findVacancyResponsesByUserAndInterviewStage_OrderIndex(user, stageIndex))
                .thenReturn(expectedResponses);

        // When
        List<VacancyResponse> result = vacancyResponseService.findVacancyResponsesByStageForUser(user, stageIndex);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedResponses, result);
        verify(repository).findVacancyResponsesByUserAndInterviewStage_OrderIndex(user, stageIndex);
    }

    @Test
    @DisplayName("Should mark outdated responses")
    @Description("Test that markOutdatedResponses marks old responses as rejected")
    @Severity(SeverityLevel.CRITICAL)
    void testMarkOutdatedResponses() {
        // Given
        User user = createTestUser();
        InterviewStage rejectedStage = createTestInterviewStage("REJECTED", 99);

        when(interviewStageService.findRejectedStage()).thenReturn(Optional.of(rejectedStage));

        // When
        vacancyResponseService.markOutdatedResponses(user);

        // Then
        verify(interviewStageService).findRejectedStage();
        verify(repository).bulkMarkOutdated(
                eq(user),
                eq(rejectedStage),
                eq("The interview stage was changed to REJECTED; response marked as outdated by the user."),
                eq(0), // INTERVIEW_STAGE_APPLIED_ORDER
                any(LocalDateTime.class)
        );
    }

    @Test
    @DisplayName("Should throw exception when rejected stage not found")
    @Description("Test that markOutdatedResponses throws exception when rejected stage is not found")
    @Severity(SeverityLevel.NORMAL)
    void testMarkOutdatedResponsesRejectedStageNotFound() {
        // Given
        User user = createTestUser();

        when(interviewStageService.findRejectedStage()).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            vacancyResponseService.markOutdatedResponses(user);
        });
        verify(interviewStageService).findRejectedStage();
        verify(repository, never()).bulkMarkOutdated(
                any(User.class),
                any(InterviewStage.class),
                anyString(),
                anyInt(),
                any(LocalDateTime.class)
        );
    }


    @Test
    @DisplayName("Should handle empty search results")
    @Description("Test that search methods handle empty results gracefully")
    @Severity(SeverityLevel.NORMAL)
    void testSearchVacancyResponsesEmpty() {
        // Given
        String query = "NonExistentPosition";
        InterviewStage stage = createTestInterviewStage("APPLIED", 1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<VacancyResponse> emptyPage = new PageImpl<>(List.of());

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        // When
        Page<VacancyResponse> result = vacancyResponseService.searchVacancyResponses(query, stage, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(repository).findAll(any(Specification.class), eq(pageable));
    }

    /**
     * Helper method to create a test user.
     */
    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        return user;
    }

    /**
     * Helper method to create a test vacancy response.
     */
    private VacancyResponse createTestVacancyResponse(String companyName, String position) {
        VacancyResponse response = new VacancyResponse();
        response.setId(UUID.randomUUID());
        response.setCompanyName(companyName);
        response.setPosition(position);
        return response;
    }

    /**
     * Helper method to create a test vacancy.
     */
    private Vacancy createTestVacancy(String position, String companyName) {
        Vacancy vacancy = new Vacancy();
        vacancy.setId(UUID.randomUUID());
        vacancy.setPosition(position);
        vacancy.setCompanyName(companyName);
        return vacancy;
    }

    /**
     * Helper method to create a test interview stage.
     */
    private InterviewStage createTestInterviewStage(String code, Integer orderIndex) {
        InterviewStage stage = new InterviewStage();
        stage.setId(UUID.randomUUID());
        stage.setCode(code);
        stage.setOrderIndex(orderIndex);
        return stage;
    }
}
