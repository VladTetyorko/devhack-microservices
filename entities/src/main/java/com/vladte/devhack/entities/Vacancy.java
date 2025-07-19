package com.vladte.devhack.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a job vacancy.
 */
@Entity
@Table(name = "vacancies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy extends BasicEntity {

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "technologies")
    private String technologies;

    @Column(name = "source")
    private String source;

    @Column(name = "url")
    private String url;

    @Column(name = "open_at")
    private LocalDateTime openAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VacancyStatus status = VacancyStatus.OPEN;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "remote_allowed")
    private Boolean remoteAllowed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VacancyResponse> responses = new ArrayList<>();
}
