package com.vladte.devhack.domain.model.mapper.global.ai;

import com.vladte.devhack.domain.entities.global.ai.AiPrompt;
import com.vladte.devhack.domain.model.dto.global.ai.AiPromptDTO;
import com.vladte.devhack.domain.model.mapper.EntityDTOMapper;
import org.springframework.stereotype.Component;

@Component
public class AiPromptMapper implements EntityDTOMapper<AiPrompt, AiPromptDTO> {

    public AiPromptDTO toDTO(AiPrompt entity) {
        if (entity == null) {
            return null;
        }

        AiPromptDTO dto = new AiPromptDTO();
        dto.setId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setSystemTemplate(entity.getSystemTemplate());
        dto.setUserTemplate(entity.getUserTemplate());
        dto.setEnabled(entity.getEnabled());
        dto.setArgsSchema(entity.getArgsSchema());
        dto.setDefaults(entity.getDefaults());
        dto.setModel(entity.getModel());
        dto.setParameters(entity.getParameters());
        dto.setResponseContract(entity.getResponseContract());
        dto.setVersion(entity.getVersion());
        dto.setDescription(entity.getDescription());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getCategory() != null) {
            dto.setCategoryId(entity.getCategory().getId());
            dto.setCategoryName(entity.getCategory().getName());
        }

        return dto;
    }

    public AiPrompt toEntity(AiPromptDTO dto) {
        if (dto == null) {
            return null;
        }

        AiPrompt entity = new AiPrompt();
        entity.setId(dto.getId());
        entity.setKey(dto.getKey());
        entity.setSystemTemplate(dto.getSystemTemplate());
        entity.setUserTemplate(dto.getUserTemplate());
        entity.setEnabled(dto.getEnabled());
        entity.setArgsSchema(dto.getArgsSchema());
        entity.setDefaults(dto.getDefaults());
        entity.setModel(dto.getModel());
        entity.setParameters(dto.getParameters());
        entity.setResponseContract(dto.getResponseContract());
        entity.setVersion(dto.getVersion());
        entity.setDescription(dto.getDescription());
        // Category should be set in service layer if needed
        return entity;
    }

    public AiPrompt updateEntityFromDTO(AiPrompt entity, AiPromptDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setKey(dto.getKey());
        entity.setSystemTemplate(dto.getSystemTemplate());
        entity.setUserTemplate(dto.getUserTemplate());
        entity.setEnabled(dto.getEnabled());
        entity.setArgsSchema(dto.getArgsSchema());
        entity.setDefaults(dto.getDefaults());
        entity.setModel(dto.getModel());
        entity.setParameters(dto.getParameters());
        entity.setResponseContract(dto.getResponseContract());
        entity.setVersion(dto.getVersion());
        entity.setDescription(dto.getDescription());
        return entity;
    }
}
