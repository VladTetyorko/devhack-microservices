package com.vladte.devhack.common.repository;

import com.vladte.devhack.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    /**
     * Find a user by role.
     *
     * @param role the role to search for
     * @return an Optional containing the user, or empty if not found
     */
    Optional<User> findByRole(String role);

    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return an Optional containing the user, or empty if not found
     */
    Optional<User> findByEmail(String email);
}
