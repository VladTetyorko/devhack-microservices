package com.vladte.devhack.common.repository;

import com.vladte.devhack.entities.Audit;
import com.vladte.devhack.entities.User;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the AuditRepository.
 */
@DisplayName("Audit Repository Tests")
class AuditRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setup() {
        // Create and save a test user
        testUser = new User();
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        userRepository.save(testUser);

        // Clear any existing audit records
        auditRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save an audit record")
    @Description("Test that an audit record can be saved to the database")
    @Severity(SeverityLevel.BLOCKER)
    void testSaveAudit() {
        // Arrange
        Audit audit = createTestAudit(Audit.OperationType.CREATE, "User", testUser.getId().toString());

        // Act
        Audit savedAudit = auditRepository.save(audit);

        // Assert
        assertNotNull(savedAudit.getId());
        assertNotNull(savedAudit.getCreatedAt());
        assertEquals(Audit.OperationType.CREATE, savedAudit.getOperationType());
        assertEquals("User", savedAudit.getEntityType());
        assertEquals(testUser.getId().toString(), savedAudit.getEntityId());
        assertEquals(testUser.getId(), savedAudit.getUser().getId());
    }

    @Test
    @DisplayName("Should find an audit record by ID")
    @Description("Test that an audit record can be retrieved by its ID")
    @Severity(SeverityLevel.CRITICAL)
    void testFindAuditById() {
        // Arrange
        Audit audit = createTestAudit(Audit.OperationType.READ, "User", testUser.getId().toString());
        Audit savedAudit = auditRepository.save(audit);
        UUID auditId = savedAudit.getId();

        // Act
        Optional<Audit> foundAudit = auditRepository.findById(auditId);

        // Assert
        assertTrue(foundAudit.isPresent());
        assertEquals(auditId, foundAudit.get().getId());
        assertEquals(Audit.OperationType.READ, foundAudit.get().getOperationType());
        assertEquals("User", foundAudit.get().getEntityType());
    }

    @Test
    @DisplayName("Should update an audit record")
    @Description("Test that an audit record can be updated in the database")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateAudit() {
        // Arrange
        Audit audit = createTestAudit(Audit.OperationType.UPDATE, "User", testUser.getId().toString());
        Audit savedAudit = auditRepository.save(audit);
        UUID auditId = savedAudit.getId();

        // Act
        savedAudit.setOperationType(Audit.OperationType.DELETE);
        savedAudit.setDetails("User account deleted");
        Audit updatedAudit = auditRepository.save(savedAudit);

        // Assert
        assertEquals(auditId, updatedAudit.getId());
        assertEquals(Audit.OperationType.DELETE, updatedAudit.getOperationType());
        assertEquals("User account deleted", updatedAudit.getDetails());
    }

    @Test
    @DisplayName("Should delete an audit record")
    @Description("Test that an audit record can be deleted from the database")
    @Severity(SeverityLevel.CRITICAL)
    void testDeleteAudit() {
        // Arrange
        Audit audit = createTestAudit(Audit.OperationType.DELETE, "User", testUser.getId().toString());
        Audit savedAudit = auditRepository.save(audit);
        UUID auditId = savedAudit.getId();

        // Act
        auditRepository.deleteById(auditId);

        // Assert
        Optional<Audit> foundAudit = auditRepository.findById(auditId);
        assertFalse(foundAudit.isPresent());
    }

    @Test
    @DisplayName("Should create audit records for different operation types")
    @Description("Test that audit records can be created for different operation types")
    @Severity(SeverityLevel.NORMAL)
    void testDifferentOperationTypes() {
        // Arrange & Act
        Audit createAudit = createTestAudit(Audit.OperationType.CREATE, "User", testUser.getId().toString());
        createAudit.setDetails("User account created");
        auditRepository.save(createAudit);

        Audit readAudit = createTestAudit(Audit.OperationType.READ, "User", testUser.getId().toString());
        readAudit.setDetails("User profile viewed");
        auditRepository.save(readAudit);

        Audit updateAudit = createTestAudit(Audit.OperationType.UPDATE, "User", testUser.getId().toString());
        updateAudit.setDetails("User profile updated");
        auditRepository.save(updateAudit);

        Audit deleteAudit = createTestAudit(Audit.OperationType.DELETE, "User", testUser.getId().toString());
        deleteAudit.setDetails("User account deleted");
        auditRepository.save(deleteAudit);

        // Assert
        assertEquals(4, auditRepository.count());
    }

    @Test
    @DisplayName("Should create audit records for different entity types")
    @Description("Test that audit records can be created for different entity types")
    @Severity(SeverityLevel.NORMAL)
    void testDifferentEntityTypes() {
        // Arrange & Act
        Audit userAudit = createTestAudit(Audit.OperationType.CREATE, "User", UUID.randomUUID().toString());
        auditRepository.save(userAudit);

        Audit questionAudit = createTestAudit(Audit.OperationType.CREATE, "InterviewQuestion", UUID.randomUUID().toString());
        auditRepository.save(questionAudit);

        Audit answerAudit = createTestAudit(Audit.OperationType.CREATE, "Answer", UUID.randomUUID().toString());
        auditRepository.save(answerAudit);

        Audit noteAudit = createTestAudit(Audit.OperationType.CREATE, "Note", UUID.randomUUID().toString());
        auditRepository.save(noteAudit);

        // Assert
        assertEquals(4, auditRepository.count());
    }

    /**
     * Helper method to create a test audit record.
     */
    private Audit createTestAudit(Audit.OperationType operationType, String entityType, String entityId) {
        return Audit.builder()
                .operationType(operationType)
                .entityType(entityType)
                .entityId(entityId)
                .user(testUser)
                .timestamp(LocalDateTime.now())
                .details("Test audit record")
                .build();
    }
}