package com.vladte.devhack.domain.repository.user;

import com.vladte.devhack.domain.entities.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository
        extends JpaRepository<User, UUID> {

    /**
     * Quickly load a user along with its LOCAL credentials.
     */
    Optional<User> findByAuthProvidersEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.profile JOIN FETCH u.authProviders JOIN FETCH u.userAccess WHERE u.id = :id ")
    Optional<User> loadWithRelatedDetails(UUID id);
}
