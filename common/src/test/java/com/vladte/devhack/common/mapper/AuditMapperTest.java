package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.model.dto.AuditDTO;
import com.vladte.devhack.common.model.mapper.AuditMapper;
import com.vladte.devhack.entities.Audit;
import com.vladte.devhack.entities.User;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Audit Mapper Tests")
class AuditMapperTest {

    private AuditMapper auditMapper;
    private Audit audit;
    private AuditDTO auditDTO;
    private User user;
    private final UUID USER_ID = UUID.randomUUID();
    private final UUID AUDIT_ID = UUID.randomUUID();
    private final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        auditMapper = new AuditMapper();

        // Create test user
        user = new User();
        user.setId(USER_ID);
        user.setName("Test User");

        // Create test audit entity
        audit = new Audit();
        audit.setId(AUDIT_ID);
        audit.setOperationType(Audit.OperationType.CREATE);
        audit.setEntityType("User");
        audit.setEntityId(USER_ID.toString());
        audit.setTimestamp(NOW);
        audit.setDetails("Created new user");
        audit.setCreatedAt(NOW);
        audit.setUser(user);

        // Create test audit DTO
        auditDTO = new AuditDTO();
        auditDTO.setId(AUDIT_ID);
        auditDTO.setOperationType(Audit.OperationType.CREATE);
        auditDTO.setEntityType("User");
        auditDTO.setEntityId(USER_ID.toString());
        auditDTO.setTimestamp(NOW);
        auditDTO.setDetails("Created new user");
        auditDTO.setCreatedAt(NOW);
        auditDTO.setUserId(USER_ID);
        auditDTO.setUserName("Test User");
    }

    @Test
    @DisplayName("Should convert entity to DTO with valid entity")
    @Description("Test that an Audit entity can be correctly converted to an AuditDTO")
    @Severity(SeverityLevel.CRITICAL)
    void testToDTOWithValidEntity() {
        AuditDTO result = auditMapper.toDTO(audit);

        assertNotNull(result);
        assertEquals(AUDIT_ID, result.getId());
        assertEquals(Audit.OperationType.CREATE, result.getOperationType());
        assertEquals("User", result.getEntityType());
        assertEquals(USER_ID.toString(), result.getEntityId());
        assertEquals(NOW, result.getTimestamp());
        assertEquals("Created new user", result.getDetails());
        assertEquals(NOW, result.getCreatedAt());
        assertEquals(USER_ID, result.getUserId());
        assertEquals("Test User", result.getUserName());
    }

    @Test
    @DisplayName("Should handle null entity when converting to DTO")
    @Description("Test that the mapper returns null when a null entity is provided")
    @Severity(SeverityLevel.NORMAL)
    void testToDTOWithNullEntity() {
        AuditDTO result = auditMapper.toDTO(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null user when converting to DTO")
    @Description("Test that the mapper correctly handles null user relationship")
    @Severity(SeverityLevel.NORMAL)
    void testToDTOWithNullUser() {
        audit.setUser(null);

        AuditDTO result = auditMapper.toDTO(audit);

        assertNotNull(result);
        assertEquals(AUDIT_ID, result.getId());
        assertNull(result.getUserId());
        assertNull(result.getUserName());
    }

    @Test
    @DisplayName("Should convert DTO to entity with valid DTO")
    @Description("Test that an AuditDTO can be correctly converted to an Audit entity")
    @Severity(SeverityLevel.CRITICAL)
    void testToEntityWithValidDTO() {
        Audit result = auditMapper.toEntity(auditDTO);

        assertNotNull(result);
        assertEquals(AUDIT_ID, result.getId());
        assertEquals(Audit.OperationType.CREATE, result.getOperationType());
        assertEquals("User", result.getEntityType());
        assertEquals(USER_ID.toString(), result.getEntityId());
        assertEquals(NOW, result.getTimestamp());
        assertEquals("Created new user", result.getDetails());

        // User should be null as it is set by the service layer
        assertNull(result.getUser());
    }

    @Test
    @DisplayName("Should handle null DTO when converting to entity")
    @Description("Test that the mapper returns null when a null DTO is provided")
    @Severity(SeverityLevel.NORMAL)
    void testToEntityWithNullDTO() {
        Audit result = auditMapper.toEntity(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should update entity from DTO")
    @Description("Test that an existing Audit entity can be correctly updated from an AuditDTO")
    @Severity(SeverityLevel.CRITICAL)
    void testUpdateEntityFromDTO() {
        // Create a new entity with different values
        Audit entityToUpdate = new Audit();
        entityToUpdate.setId(AUDIT_ID);
        entityToUpdate.setOperationType(Audit.OperationType.READ);
        entityToUpdate.setEntityType("Answer");
        entityToUpdate.setEntityId("old-id");
        entityToUpdate.setTimestamp(NOW.minusDays(1));
        entityToUpdate.setDetails("Old details");

        // Update the entity with DTO values
        Audit result = auditMapper.updateEntityFromDTO(entityToUpdate, auditDTO);

        // Verify the entity was updated
        assertNotNull(result);
        assertEquals(AUDIT_ID, result.getId());
        assertEquals(Audit.OperationType.CREATE, result.getOperationType());
        assertEquals("User", result.getEntityType());
        assertEquals(USER_ID.toString(), result.getEntityId());
        assertEquals(NOW, result.getTimestamp());
        assertEquals("Created new user", result.getDetails());

        // User should not be updated by the mapper
        assertNull(result.getUser());
    }

    @Test
    @DisplayName("Should handle null entity when updating from DTO")
    @Description("Test that the mapper returns null when a null entity is provided for updating")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateEntityFromDTOWithNullEntity() {
        Audit result = auditMapper.updateEntityFromDTO(null, auditDTO);
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null DTO when updating entity")
    @Description("Test that the mapper returns the original entity when a null DTO is provided for updating")
    @Severity(SeverityLevel.NORMAL)
    void testUpdateEntityFromDTOWithNullDTO() {
        Audit result = auditMapper.updateEntityFromDTO(audit, null);
        assertSame(audit, result);
    }

    @Test
    @DisplayName("Should convert list of entities to list of DTOs")
    @Description("Test that a list of Audit entities can be correctly converted to a list of AuditDTOs")
    @Severity(SeverityLevel.CRITICAL)
    void testToDTOList() {
        List<Audit> audits = Arrays.asList(audit, audit);
        List<AuditDTO> result = auditMapper.toDTOList(audits);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(AUDIT_ID, result.get(0).getId());
        assertEquals(AUDIT_ID, result.get(1).getId());
    }

    @Test
    @DisplayName("Should convert list of DTOs to list of entities")
    @Description("Test that a list of AuditDTOs can be correctly converted to a list of Audit entities")
    @Severity(SeverityLevel.CRITICAL)
    void testToEntityList() {
        List<AuditDTO> dtos = Arrays.asList(auditDTO, auditDTO);
        List<Audit> result = auditMapper.toEntityList(dtos);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(AUDIT_ID, result.get(0).getId());
        assertEquals(AUDIT_ID, result.get(1).getId());
    }
}