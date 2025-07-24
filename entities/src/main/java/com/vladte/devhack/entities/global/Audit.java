package com.vladte.devhack.entities.global;

import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity for storing audit records.
 * Tracks operations performed on entities in the system.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Audit extends BasicEntity {

    /**
     * The type of operation performed (CREATE, READ, UPDATE, DELETE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationType operationType;

    /**
     * The name of the entity class that was operated on.
     */
    @Column(nullable = false)
    private String entityType;

    /**
     * The ID of the entity that was operated on (if applicable).
     */
    @Column
    private String entityId;

    /**
     * The user who performed the operation.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The timestamp when the operation was performed.
     */
    @Column(nullable = false)
    private LocalDateTime timestamp;

    /**
     * Additional details about the operation.
     */
    @Column(length = 1000)
    private String details;

    /**
     * Enum representing the types of operations that can be audited.
     */
    public enum OperationType {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }
}
