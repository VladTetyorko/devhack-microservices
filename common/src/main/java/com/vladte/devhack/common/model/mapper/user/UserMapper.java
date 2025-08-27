package com.vladte.devhack.common.model.mapper.user;

import com.vladte.devhack.common.model.dto.user.UserDTO;
import com.vladte.devhack.common.model.mapper.EntityDTOMapper;
import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.user.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper implements EntityDTOMapper<User, UserDTO> {

    @Override
    public UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());

        // Extract credential IDs
        if (entity.getAuthProviders() != null) {
            dto.setCredentialIds(entity.getAuthProviders().stream()
                    .map(BasicEntity::getId)
                    .collect(Collectors.toList()));
        }

        // Extract profile ID
        if (entity.getProfile() != null) {
            dto.setProfileId(entity.getProfile().getId());
        }

        // Extract access ID
        if (entity.getUserAccess() != null) {
            dto.setAccessId(entity.getUserAccess().getId());
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

        // Note: AuthProviders, Profile, and UserAccess need to be set by the service layer
        // as they require fetching the related entities from the database using the IDs

        return entity;
    }

    @Override
    public User updateEntityFromDTO(User entity, UserDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }

        // Note: AuthProviders, Profile, and UserAccess updates need to be handled by the service layer
        // as they require fetching the related entities from the database using the IDs

        return entity;
    }
}