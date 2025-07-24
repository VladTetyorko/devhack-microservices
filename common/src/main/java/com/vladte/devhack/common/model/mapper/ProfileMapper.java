package com.vladte.devhack.common.model.mapper;

import com.vladte.devhack.common.model.dto.ProfileDTO;
import com.vladte.devhack.entities.user.Profile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper implements EntityDTOMapper<Profile, ProfileDTO> {
    @Override
    public ProfileDTO toDTO(Profile entity) {
        if (entity == null) return null;
        ProfileDTO dto = new ProfileDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCvFileHref(entity.getCvFileHref());
        dto.setCvFileName(entity.getCvFileName());
        dto.setCvFileType(entity.getCvFileType());
        dto.setCvFileSize(entity.getCvFileSize());
        dto.setCvUploadedAt(entity.getCvUploadedAt());
        dto.setCvParsedSuccessfully(entity.getCvParsedSuccessfully());
        dto.setAiUsageEnabled(entity.getAiUsageEnabled());
        dto.setAiPreferredLanguage(entity.getAiPreferredLanguage());
        dto.setAiCvScore(entity.getAiCvScore());
        dto.setAiSkillsSummary(entity.getAiSkillsSummary());
        dto.setAiSuggestedImprovements(entity.getAiSuggestedImprovements());
        dto.setIsVisibleToRecruiters(entity.getVisibleToRecruiters());
        return dto;
    }

    @Override
    public Profile toEntity(ProfileDTO dto) {
        if (dto == null) return null;
        Profile entity = new Profile();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setCvFileHref(dto.getCvFileHref());
        entity.setCvFileName(dto.getCvFileName());
        entity.setCvFileType(dto.getCvFileType());
        entity.setCvFileSize(dto.getCvFileSize());
        entity.setCvUploadedAt(dto.getCvUploadedAt());
        entity.setCvParsedSuccessfully(dto.getCvParsedSuccessfully());
        entity.setAiUsageEnabled(dto.getAiUsageEnabled());
        entity.setAiPreferredLanguage(dto.getAiPreferredLanguage());
        entity.setAiCvScore(dto.getAiCvScore());
        entity.setAiSkillsSummary(dto.getAiSkillsSummary());
        entity.setAiSuggestedImprovements(dto.getAiSuggestedImprovements());
        entity.setVisibleToRecruiters(dto.getIsVisibleToRecruiters());
        return entity;
    }

    @Override
    public Profile updateEntityFromDTO(Profile entity, ProfileDTO dto) {
        if (entity == null || dto == null) return entity;
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getCvFileHref() != null) entity.setCvFileHref(dto.getCvFileHref());
        if (dto.getCvFileName() != null) entity.setCvFileName(dto.getCvFileName());
        if (dto.getCvFileType() != null) entity.setCvFileType(dto.getCvFileType());
        if (dto.getCvFileSize() != null) entity.setCvFileSize(dto.getCvFileSize());
        if (dto.getCvUploadedAt() != null) entity.setCvUploadedAt(dto.getCvUploadedAt());
        if (dto.getCvParsedSuccessfully() != null) entity.setCvParsedSuccessfully(dto.getCvParsedSuccessfully());
        if (dto.getAiUsageEnabled() != null) entity.setAiUsageEnabled(dto.getAiUsageEnabled());
        if (dto.getAiPreferredLanguage() != null) entity.setAiPreferredLanguage(dto.getAiPreferredLanguage());
        if (dto.getAiCvScore() != null) entity.setAiCvScore(dto.getAiCvScore());
        if (dto.getAiSkillsSummary() != null) entity.setAiSkillsSummary(dto.getAiSkillsSummary());
        if (dto.getAiSuggestedImprovements() != null)
            entity.setAiSuggestedImprovements(dto.getAiSuggestedImprovements());
        if (dto.getIsVisibleToRecruiters() != null) entity.setVisibleToRecruiters(dto.getIsVisibleToRecruiters());
        return entity;
    }
}
