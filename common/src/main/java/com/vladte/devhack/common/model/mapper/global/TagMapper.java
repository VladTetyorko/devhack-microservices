package com.vladte.devhack.common.model.mapper.global;

import com.vladte.devhack.common.model.dto.global.TagDTO;
import com.vladte.devhack.common.model.mapper.EntityDTOMapper;
import com.vladte.devhack.entities.global.InterviewQuestion;
import com.vladte.devhack.entities.global.Tag;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between Tag entity and TagDTO.
 */
@Component
public class TagMapper implements EntityDTOMapper<Tag, TagDTO> {

    @Override
    public TagDTO toDTO(Tag entity) {
        if (entity == null) {
            return null;
        }

        TagDTO dto = new TagDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAnsweredQuestions(entity.getAnsweredQuestions());
        dto.setProgressPercentage(entity.getProgressPercentage());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getQuestions() != null) {
            dto.setQuestionIds(entity.getQuestions().stream()
                    .map(InterviewQuestion::getId)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    @Override
    public Tag toEntity(TagDTO dto) {
        if (dto == null) {
            return null;
        }

        Tag entity = new Tag();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setAnsweredQuestions(dto.getAnsweredQuestions());

        // Note: Questions need to be set by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

    @Override
    public Tag updateEntityFromDTO(Tag entity, TagDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setName(dto.getName());
        entity.setAnsweredQuestions(dto.getAnsweredQuestions());

        // Note: Questions need to be updated by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }
}