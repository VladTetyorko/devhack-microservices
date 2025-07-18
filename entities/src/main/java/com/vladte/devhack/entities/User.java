package com.vladte.devhack.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BasicEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String role;

    // CV fields
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

    // AI usage fields
    @Column(name = "ai_usage_allowed")
    private Boolean aiUsageAllowed = false;

    @Column(name = "ai_usage_enabled")
    private Boolean aiUsageEnabled = false;

    @Column(name = "ai_preferred_language")
    private String aiPreferredLanguage = "en";

    @Column(name = "ai_cv_score")
    private Integer aiCvScore;

    @Column(name = "ai_skills_summary")
    private String aiSkillsSummary;

    @Column(name = "ai_suggested_improvements")
    private String aiSuggestedImprovements;

    // Visibility setting
    @Column(name = "is_visible_to_recruiters")
    private Boolean isVisibleToRecruiters = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VacancyResponse> vacancyResponses = new ArrayList<>();
}
