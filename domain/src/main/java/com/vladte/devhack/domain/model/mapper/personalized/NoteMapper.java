package com.vladte.devhack.domain.model.mapper.personalized;

import com.vladte.devhack.domain.entities.personalized.Note;
import com.vladte.devhack.domain.model.dto.personalized.NoteDTO;
import com.vladte.devhack.domain.model.mapper.EntityDTOMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Note entity and NoteDTO.
 */
@Component
public class NoteMapper implements EntityDTOMapper<Note, NoteDTO> {

    @Override
    public NoteDTO toDTO(Note entity) {
        if (entity == null) {
            return null;
        }

        NoteDTO dto = new NoteDTO();
        dto.setId(entity.getId());
        dto.setNoteText(entity.getNoteText());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getUser() != null) {
            dto.setUserId(entity.getUser().getId());
            dto.setUserName(entity.getUser().getProfile().getName());
        }

        if (entity.getQuestion() != null) {
            dto.setQuestionId(entity.getQuestion().getId());
            dto.setQuestionText(entity.getQuestion().getQuestionText());
        }

        return dto;
    }

    @Override
    public Note toEntity(NoteDTO dto) {
        if (dto == null) {
            return null;
        }

        Note entity = new Note();
        entity.setId(dto.getId());
        entity.setNoteText(dto.getNoteText());
        entity.setUpdatedAt(dto.getUpdatedAt());

        // Note: User and question need to be set by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }

    @Override
    public Note updateEntityFromDTO(Note entity, NoteDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        entity.setNoteText(dto.getNoteText());

        // Note: User and question need to be updated by the service layer
        // as they require fetching the related entities from the database

        return entity;
    }
}