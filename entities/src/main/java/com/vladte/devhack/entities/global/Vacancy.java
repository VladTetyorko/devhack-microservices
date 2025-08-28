package com.vladte.devhack.entities.global;

import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.enums.VacancyStatus;
import com.vladte.devhack.entities.personalized.VacancyResponse;
import jakarta.persistence.*;
import lombok.*;
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
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
