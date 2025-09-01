package com.vladte.devhack.domain.service.user;

import com.vladte.devhack.domain.entities.user.UserAccess;
import com.vladte.devhack.domain.service.CrudService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccessService extends CrudService<UserAccess, UUID> {

    Optional<UserAccess> findByUserId(UUID userId);

    List<UserAccess> findAllByRole(String role);
}
