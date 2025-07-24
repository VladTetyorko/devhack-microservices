package com.vladte.devhack.entities.personalized;

import com.vladte.devhack.entities.UserOwnedBasicEntity;
import com.vladte.devhack.entities.global.InterviewStage;
import com.vladte.devhack.entities.global.Tag;
import com.vladte.devhack.entities.global.Vacancy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a response to a job vacancy.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "vacancy_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyResponse extends UserOwnedBasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @Column(name = "pros")
    private String pros;

    @Column(name = "cons")
    private String cons;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "salary")
    private String salary;

    @Column(name = "location")
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    private InterviewStage interviewStage;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
            name = "vacancy_response_tags",
            joinColumns = @JoinColumn(name = "vacancy_response_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    // Convenience methods to access vacancy properties
    public String getCompanyName() {
        return vacancy != null ? vacancy.getCompanyName() : null;
    }

    public void setCompanyName(String companyName) {
        if (vacancy != null) {
            vacancy.setCompanyName(companyName);
        }
    }

    public String getPosition() {
        return vacancy != null ? vacancy.getPosition() : null;
    }

    public void setPosition(String position) {
        if (vacancy != null) {
            vacancy.setPosition(position);
        }
    }

    public String getTechnologies() {
        return vacancy != null ? vacancy.getTechnologies() : null;
    }

    public void setTechnologies(String technologies) {
        if (vacancy != null) {
            vacancy.setTechnologies(technologies);
        }
    }
}
