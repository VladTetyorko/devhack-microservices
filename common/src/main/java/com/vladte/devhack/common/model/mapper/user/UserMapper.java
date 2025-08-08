package com.vladte.devhack.common.model.mapper.user;

import com.vladte.devhack.common.model.dto.user.UserDTO;
import com.vladte.devhack.common.model.mapper.EntityDTOMapper;
import com.vladte.devhack.entities.user.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper implements EntityDTOMapper<User, UserDTO> {
    private final AuthenticationProviderMapper authMapper;
    private final ProfileMapper profileMapper;
    private final UserAccessMapper accessMapper;

    public UserMapper(
            AuthenticationProviderMapper authMapper,
            ProfileMapper profileMapper,
            UserAccessMapper accessMapper
    ) {
        this.authMapper = authMapper;
        this.profileMapper = profileMapper;
        this.accessMapper = accessMapper;
    }

    @Override
    public UserDTO toDTO(User entity) {
        if (entity == null) return null;
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setCreatedAt(entity.getCreatedAt());

        // Credentials
        dto.setCredentials(
                entity.getAuthProviders().stream()
                        .map(authMapper::toDTO)
                        .collect(Collectors.toList())
        );

        // Profile
        dto.setProfile(profileMapper.toDTO(entity.getProfile()));

        // Admin settings
        dto.setAccess(accessMapper.toDTO(entity.getUserAccess()));

        return dto;
    }

    @Override
    public User toEntity(UserDTO dto) {
        if (dto == null) return null;
        User entity = new User();
        entity.setId(dto.getId());

        // Map credentials
        if (dto.getCredentials() != null) {
            entity.setAuthProviders(
                    dto.getCredentials().stream()
                            .map(authMapper::toEntity)
                            .collect(Collectors.toList())
            );
        }

        // Profile
        entity.setProfile(profileMapper.toEntity(dto.getProfile()));

        // Access
        entity.setUserAccess(accessMapper.toEntity(dto.getAccess()));

        return entity;
    }

    @Override
    public User updateEntityFromDTO(User entity, UserDTO dto) {
        if (entity == null || dto == null) return entity;
        // update nested objects if provided
        if (dto.getProfile() != null) {
            profileMapper.updateEntityFromDTO(entity.getProfile(), dto.getProfile());
        }
        if (dto.getAccess() != null) {
            accessMapper.updateEntityFromDTO(entity.getUserAccess(), dto.getAccess());
        }
        // credentials updates handled separately via auth service
        return entity;
    }
}