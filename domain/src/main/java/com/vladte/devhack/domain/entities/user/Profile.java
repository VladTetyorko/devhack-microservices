package com.vladte.devhack.domain.entities.user;

import com.vladte.devhack.domain.entities.BasicEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Profile extends BasicEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "cv_file_href")
    private String cvFileHref;

    @Column(name = "cv_file_name")
    private String cvFileName;

    @Column(name = "cv_file_type")
    private String cvFileType;

    @Column(name = "cv_file_size")
    private Long cvFileSize;

    @Column(name = "cv_storage_path")
    private String cvStoragePath;

    @Column(name = "cv_uploaded_at")
    private LocalDateTime cvUploadedAt;

    @Column(name = "cv_parsed_successfully")
    private Boolean cvParsedSuccessfully = false;

    // AI preferences & results
    @Column(name = "ai_usage_enabled")
    private Boolean aiUsageEnabled = false;

    @Column(name = "ai_preferred_language")
    private String aiPreferredLanguage = "en";

    @Column(name = "ai_cv_score")
    private Integer aiCvScore;

    @Column(name = "ai_skills_summary", columnDefinition = "TEXT")
    private String aiSkillsSummary;

    @Column(name = "ai_suggested_improvements", columnDefinition = "TEXT")
    private String aiSuggestedImprovements;

    @Column(name = "is_visible_to_recruiters")
    private Boolean visibleToRecruiters = true;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}
