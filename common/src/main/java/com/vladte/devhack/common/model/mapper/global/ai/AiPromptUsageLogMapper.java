package com.vladte.devhack.common.model.mapper.global.ai;

import com.vladte.devhack.common.model.dto.global.ai.AiPromptUsageLogDTO;
import com.vladte.devhack.common.model.mapper.EntityDTOMapper;
import com.vladte.devhack.entities.global.ai.AiPromptUsageLog;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between AiPromptUsageLog entity and AiPromptUsageLogDTO.
 */
@Component
public class AiPromptUsageLogMapper implements EntityDTOMapper<AiPromptUsageLog, AiPromptUsageLogDTO> {

    public AiPromptUsageLogDTO toDTO(AiPromptUsageLog entity) {
        if (entity == null) {
            return null;
        }

        AiPromptUsageLogDTO dto = new AiPromptUsageLogDTO();
        dto.setId(entity.getId());
        dto.setInput(entity.getInput());
        dto.setResult(entity.getResult());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            if (entity.getUser().getProfile() != null) {
                dto.setUserName(entity.getUser().getProfile().getName());
            }
        }

        if (entity.getPrompt() != null) {
            dto.setPromptId(entity.getPrompt().getId());
            dto.setPromptCode(entity.getPrompt().getCode());
        }

        return dto;
    }

    public AiPromptUsageLog toEntity(AiPromptUsageLogDTO dto) {
        if (dto == null) {
            return null;
        }

        AiPromptUsageLog entity = new AiPromptUsageLog();
        entity.setId(dto.getId());
        entity.setInput(dto.getInput());
        entity.setResult(dto.getResult());

        // Note: User and Prompt need to be set by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

    public AiPromptUsageLog updateEntityFromDTO(AiPromptUsageLog entity, AiPromptUsageLogDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setInput(dto.getInput());
        entity.setResult(dto.getResult());

        // Note: User and Prompt need to be updated by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

}
