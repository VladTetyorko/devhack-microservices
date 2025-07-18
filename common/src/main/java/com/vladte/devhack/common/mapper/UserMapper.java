package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.dto.UserDTO;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.Note;
import com.vladte.devhack.entities.User;
import com.vladte.devhack.entities.VacancyResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between User entity and UserDTO.
 */
@Component
public class UserMapper implements EntityDTOMapper<User, UserDTO> {

    @Override
    public UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setEmail(entity.getEmail());
        dto.setName(entity.getName());
        dto.setRole(entity.getRole());
        dto.setCreatedAt(entity.getCreatedAt());

        // Map CV fields
        dto.setCvFileHref(entity.getCvFileHref());
        dto.setCvFileName(entity.getCvFileName());
        dto.setCvFileType(entity.getCvFileType());
        dto.setCvFileSize(entity.getCvFileSize());
        dto.setCvUploadedAt(entity.getCvUploadedAt());
        dto.setCvParsedSuccessfully(entity.getCvParsedSuccessfully());

        // Map AI usage fields
        dto.setAiUsageAllowed(entity.getAiUsageAllowed());
        dto.setAiUsageEnabled(entity.getAiUsageEnabled());
        dto.setAiPreferredLanguage(entity.getAiPreferredLanguage());
        dto.setAiCvScore(entity.getAiCvScore());
        dto.setAiSkillsSummary(entity.getAiSkillsSummary());
        dto.setAiSuggestedImprovements(entity.getAiSuggestedImprovements());

        // Map visibility setting
        dto.setIsVisibleToRecruiters(entity.getIsVisibleToRecruiters());

        if (entity.getAnswers() != null) {
            dto.setAnswerIds(entity.getAnswers().stream()
                    .map(Answer::getId)
                    .collect(Collectors.toList()));
        }

        if (entity.getNotes() != null) {
            dto.setNoteIds(entity.getNotes().stream()
                    .map(Note::getId)
                    .collect(Collectors.toList()));
        }

        if (entity.getVacancyResponses() != null) {
            dto.setVacancyResponseIds(entity.getVacancyResponses().stream()
                    .map(VacancyResponse::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        User entity = new User();
        entity.setId(dto.getId());
        entity.setEmail(dto.getEmail());
        entity.setName(dto.getName());
        entity.setRole(dto.getRole());

        // Map CV fields
        entity.setCvFileHref(dto.getCvFileHref());
        entity.setCvFileName(dto.getCvFileName());
        entity.setCvFileType(dto.getCvFileType());
        entity.setCvFileSize(dto.getCvFileSize());
        entity.setCvUploadedAt(dto.getCvUploadedAt());
        entity.setCvParsedSuccessfully(dto.getCvParsedSuccessfully());

        // Map AI usage fields
        entity.setAiUsageAllowed(dto.getAiUsageAllowed());
        entity.setAiUsageEnabled(dto.getAiUsageEnabled());
        entity.setAiPreferredLanguage(dto.getAiPreferredLanguage());
        entity.setAiCvScore(dto.getAiCvScore());
        entity.setAiSkillsSummary(dto.getAiSkillsSummary());
        entity.setAiSuggestedImprovements(dto.getAiSuggestedImprovements());

        // Map visibility setting
        entity.setIsVisibleToRecruiters(dto.getIsVisibleToRecruiters());

        // Note: Password is not set from DTO for security reasons
        // Note: Answers, notes, and vacancy responses need to be set by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

    @Override
    public User updateEntityFromDTO(User entity, UserDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setEmail(dto.getEmail());
        entity.setName(dto.getName());
        entity.setRole(dto.getRole());

        // Update CV fields if provided in DTO
        if (dto.getCvFileHref() != null) entity.setCvFileHref(dto.getCvFileHref());
        if (dto.getCvFileName() != null) entity.setCvFileName(dto.getCvFileName());
        if (dto.getCvFileType() != null) entity.setCvFileType(dto.getCvFileType());
        if (dto.getCvFileSize() != null) entity.setCvFileSize(dto.getCvFileSize());
        if (dto.getCvUploadedAt() != null) entity.setCvUploadedAt(dto.getCvUploadedAt());
        if (dto.getCvParsedSuccessfully() != null) entity.setCvParsedSuccessfully(dto.getCvParsedSuccessfully());

        // Update AI usage fields if provided in DTO
        if (dto.getAiUsageAllowed() != null) entity.setAiUsageAllowed(dto.getAiUsageAllowed());
        if (dto.getAiUsageEnabled() != null) entity.setAiUsageEnabled(dto.getAiUsageEnabled());
        if (dto.getAiPreferredLanguage() != null) entity.setAiPreferredLanguage(dto.getAiPreferredLanguage());
        if (dto.getAiCvScore() != null) entity.setAiCvScore(dto.getAiCvScore());
        if (dto.getAiSkillsSummary() != null) entity.setAiSkillsSummary(dto.getAiSkillsSummary());
        if (dto.getAiSuggestedImprovements() != null)
            entity.setAiSuggestedImprovements(dto.getAiSuggestedImprovements());

        // Update visibility setting if provided in DTO
        if (dto.getIsVisibleToRecruiters() != null) entity.setIsVisibleToRecruiters(dto.getIsVisibleToRecruiters());

        // Note: Password is not updated from DTO for security reasons
        // Note: Answers, notes, and vacancy responses need to be updated by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }
}