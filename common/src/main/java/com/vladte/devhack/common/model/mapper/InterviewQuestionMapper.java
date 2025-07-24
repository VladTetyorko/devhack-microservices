package com.vladte.devhack.common.model.mapper;

import com.vladte.devhack.common.model.dto.InterviewQuestionDTO;
import com.vladte.devhack.entities.global.InterviewQuestion;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper for converting between InterviewQuestion entity and InterviewQuestionDTO.
 */
@Component
public class InterviewQuestionMapper implements EntityDTOMapper<InterviewQuestion, InterviewQuestionDTO> {

    private final TagMapper tagMapper;
    private final AnswerMapper answerMapper;
    private final NoteMapper noteMapper;

    public InterviewQuestionMapper(TagMapper tagMapper, AnswerMapper answerMapper, NoteMapper noteMapper) {
        this.tagMapper = tagMapper;
        this.answerMapper = answerMapper;
        this.noteMapper = noteMapper;
    }

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
            dto.setUserName(entity.getUser().getProfile().getName());
        }

        if (entity.getTags() != null) {
            dto.setTags(entity.getTags().stream()
                    .map(tagMapper::toDTO)
                    .collect(Collectors.toSet()));
        }

        if (entity.getAnswers() != null) {
            dto.setAnswers(entity.getAnswers().stream()
                    .map(answerMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        if (entity.getNotes() != null) {
            dto.setNotes(entity.getNotes().stream()
                    .map(noteMapper::toDTO)
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
