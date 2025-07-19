package com.vladte.devhack.common.service.domain.user;

import com.vladte.devhack.common.service.domain.CrudService;
import com.vladte.devhack.entities.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for User entity operations.
 */
public interface UserService extends CrudService<User, UUID>, UserDetailsService {
    /**
     * Get the system user.
     * If the system user doesn't exist, it will be created.
     *
     * @return the system user
     */
    User getSystemUser();

    void updateUsersSv(User user, String fileName, String cvUrl, String contentType);

    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return an Optional containing the user, or empty if not found
     */
    Optional<User> findByEmail(String email);

    User reguister(User user);

    /**
     * Register a new manager user.
     *
     * @param user the user to register
     * @return the registered user
     */
    User registerManager(User user);
}
