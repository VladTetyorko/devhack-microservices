package com.vladte.devhack.common.model.mapper;

import com.vladte.devhack.common.model.dto.AuthenticationProviderDTO;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.user.AuthenticationProvider;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationProviderMapper implements EntityDTOMapper<AuthenticationProvider, AuthenticationProviderDTO> {
    @Override
    public AuthenticationProviderDTO toDTO(AuthenticationProvider entity) {
        if (entity == null) return null;
        AuthenticationProviderDTO dto = new AuthenticationProviderDTO();
        dto.setId(entity.getId());
        dto.setProvider(entity.getProvider().name());
        dto.setProviderUserId(entity.getProviderUserId());
        dto.setEmail(entity.getEmail());
        dto.setTokenExpiry(entity.getTokenExpiry());
        return dto;
    }

    @Override
    public AuthenticationProvider toEntity(AuthenticationProviderDTO dto) {
        if (dto == null) return null;
        AuthenticationProvider entity = new AuthenticationProvider();
        entity.setId(dto.getId());
        entity.setProvider(AuthProviderType.valueOf(dto.getProvider()));
        entity.setProviderUserId(dto.getProviderUserId());
        entity.setEmail(dto.getEmail());
        entity.setAccessToken(dto.getAccessToken());
        entity.setRefreshToken(dto.getRefreshToken());
        entity.setTokenExpiry(dto.getTokenExpiry());
        return entity;
    }

    @Override
    public AuthenticationProvider updateEntityFromDTO(AuthenticationProvider entity, AuthenticationProviderDTO dto) {
        if (entity == null || dto == null) return entity;
        entity.setProvider(AuthProviderType.valueOf(dto.getProvider()));
        entity.setProviderUserId(dto.getProviderUserId());
        entity.setEmail(dto.getEmail());
        entity.setAccessToken(dto.getAccessToken());
        entity.setRefreshToken(dto.getRefreshToken());
        entity.setTokenExpiry(dto.getTokenExpiry());
        return entity;
    }
}