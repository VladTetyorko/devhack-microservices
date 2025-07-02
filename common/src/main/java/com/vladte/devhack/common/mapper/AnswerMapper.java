package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.dto.AnswerDTO;
import com.vladte.devhack.entities.Answer;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Answer entity and AnswerDTO.
 */
@Component
public class AnswerMapper implements EntityDTOMapper<Answer, AnswerDTO> {

    @Override
    public AnswerDTO toDTO(Answer entity) {
        if (entity == null) {
            return null;
        }

        AnswerDTO dto = new AnswerDTO();
        dto.setId(entity.getId());
        dto.setText(entity.getText());
        dto.setConfidenceLevel(entity.getConfidenceLevel());
        dto.setAiScore(entity.getAiScore());
        dto.setAiFeedback(entity.getAiFeedback());
        dto.setIsCorrect(entity.getIsCorrect());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getName());
        }

        if (entity.getQuestion() != null) {
            dto.setQuestionId(entity.getQuestion().getId());
            dto.setQuestionText(entity.getQuestion().getQuestionText());
        }

        return dto;
    }

    @Override
    public Answer toEntity(AnswerDTO dto) {
        if (dto == null) {
            return null;
        }

        Answer entity = new Answer();
        entity.setId(dto.getId());
        entity.setText(dto.getText());
        entity.setConfidenceLevel(dto.getConfidenceLevel());
        entity.setAiScore(dto.getAiScore());
        entity.setAiFeedback(dto.getAiFeedback());
        entity.setIsCorrect(dto.getIsCorrect());
        entity.setUpdatedAt(dto.getUpdatedAt());

        // Note: User and question need to be set by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

    @Override
    public Answer updateEntityFromDTO(Answer entity, AnswerDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setText(dto.getText());
        entity.setConfidenceLevel(dto.getConfidenceLevel());
        entity.setAiScore(dto.getAiScore());
        entity.setAiFeedback(dto.getAiFeedback());
        entity.setIsCorrect(dto.getIsCorrect());

        // Note: User and question need to be updated by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }
}