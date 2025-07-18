package com.vladte.devhack.common.mapper;

import com.vladte.devhack.common.dto.InterviewQuestionDTO;
import com.vladte.devhack.entities.Answer;
import com.vladte.devhack.entities.InterviewQuestion;
import com.vladte.devhack.entities.Note;
import com.vladte.devhack.entities.Tag;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between InterviewQuestion entity and InterviewQuestionDTO.
 */
@Component
public class InterviewQuestionMapper implements EntityDTOMapper<InterviewQuestion, InterviewQuestionDTO> {

    @Override
    public InterviewQuestionDTO toDTO(InterviewQuestion entity) {
        if (entity == null) {
            return null;
        }

        InterviewQuestionDTO dto = new InterviewQuestionDTO();
        dto.setId(entity.getId());
        dto.setQuestionText(entity.getQuestionText());
        dto.setDifficulty(entity.getDifficulty());
        dto.setSource(entity.getSource());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getName());
        }

        if (entity.getTags() != null) {
            dto.setTagIds(entity.getTags().stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet()));
        }

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

        return dto;
    }

    @Override
    public InterviewQuestion toEntity(InterviewQuestionDTO dto) {
        if (dto == null) {
            return null;
        }

        InterviewQuestion entity = new InterviewQuestion();
        entity.setId(dto.getId());
        entity.setQuestionText(dto.getQuestionText());
        entity.setDifficulty(dto.getDifficulty());
        entity.setSource(dto.getSource());

        // Note: User, tags, answers, and notes need to be set by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

    @Override
    public InterviewQuestion updateEntityFromDTO(InterviewQuestion entity, InterviewQuestionDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setQuestionText(dto.getQuestionText());
        entity.setDifficulty(dto.getDifficulty());
        entity.setSource(dto.getSource());

        // Note: User, tags, answers, and notes need to be updated by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }
}
