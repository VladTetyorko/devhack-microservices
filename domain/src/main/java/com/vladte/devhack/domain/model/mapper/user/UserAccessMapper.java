package com.vladte.devhack.domain.model.mapper.user;

import com.vladte.devhack.domain.entities.user.UserAccess;
import com.vladte.devhack.domain.model.dto.user.UserAccessDTO;
import com.vladte.devhack.domain.model.mapper.EntityDTOMapper;
import org.springframework.stereotype.Component;

@Component
public class UserAccessMapper implements EntityDTOMapper<UserAccess, UserAccessDTO> {
    @Override
    public UserAccessDTO toDTO(UserAccess entity) {
        if (entity == null) {
            return null;
        }
        UserAccessDTO dto = new UserAccessDTO();
        dto.setId(entity.getId());
        dto.setRole(entity.getRole());
        dto.setAiUsageAllowed(entity.getIsAiUsageAllowed());
        dto.setAccountLocked(entity.getIsAccountLocked());
        return dto;
    }

    @Override
    public UserAccess toEntity(UserAccessDTO dto) {
        if (dto == null) {
            return null;
        }
        UserAccess entity = new UserAccess();
        entity.setId(dto.getId());
        entity.setRole(dto.getRole());
        entity.setIsAiUsageAllowed(dto.getAiUsageAllowed());
        entity.setIsAccountLocked(dto.getAccountLocked());
        return entity;
    }

    @Override
    public UserAccess updateEntityFromDTO(UserAccess entity, UserAccessDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }
        if (dto.getRole() != null) entity.setRole(dto.getRole());
        if (dto.getAiUsageAllowed() != null) entity.setIsAiUsageAllowed(dto.getAiUsageAllowed());
        if (dto.getAccountLocked() != null) entity.setIsAccountLocked(dto.getAccountLocked());
        return entity;
    }
}