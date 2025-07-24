package com.vladte.devhack.common.service.domain.user;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.user.UserAccess;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAccessService extends CrudService<UserAccess, UUID> {

    Optional<UserAccess> findByUserId(UUID userId);

    List<UserAccess> findAllByRole(String role);
}
