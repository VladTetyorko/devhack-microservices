package com.vladte.devhack.common.service.domain.impl;

import com.vladte.devhack.common.repository.UserRepository;
import com.vladte.devhack.common.service.domain.AuditService;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the UserService interface.
 */
@Service
public class UserServiceImpl extends BaseServiceImpl<User, UUID, UserRepository> implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String SYSTEM_ROLE = "SYSTEM";
    private static final String SYSTEM_USER_NAME = "system";
    private static final String SYSTEM_USER_EMAIL = "system@devhack.com";
    private static final String SYSTEM_USER_PASSWORD = "system";

    private final PasswordEncoder passwordEncoder;
    private final UserService self;

    /**
     * Constructor with repository and auditUtil injection.
     *
     * @param repository      the user repository
     * @param auditUtil       the audit utility
     * @param passwordEncoder for password encoding
     */

    public UserServiceImpl(UserRepository repository, AuditService auditUtil, PasswordEncoder passwordEncoder, @Lazy UserService self) {
        super(repository, auditUtil);
        this.passwordEncoder = passwordEncoder;
        this.self = self;
    }

    /**
     * Get the system user.
     * If the system user doesn't exist, it will be created.
     *
     * @return the system user
     */
    @Override
    @Transactional
    @Cacheable(value = "users", key = "#root.methodName")
    public User getSystemUser() {
        logger.debug("Getting system user");

        Optional<User> systemUser = repository.findByRole(SYSTEM_ROLE);

        if (systemUser.isPresent()) {
            logger.debug("Found existing system user with ID: {}", systemUser.get().getId());
            return systemUser.get();
        }

        logger.info("System user not found, creating a new one");

        User newSystemUser = new User();
        newSystemUser.setName(SYSTEM_USER_NAME);
        newSystemUser.setEmail(SYSTEM_USER_EMAIL);
        newSystemUser.setPassword(SYSTEM_USER_PASSWORD);
        newSystemUser.setRole(SYSTEM_ROLE);

        User savedSystemUser = repository.save(newSystemUser);
        logger.info("Created new system user with ID: {}", savedSystemUser.getId());

        return savedSystemUser;
    }

    @Override
    public User reguister(User user) {
        logger.debug("Registering user: {}", user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");

        return super.save(user, self.getSystemUser(), "Registering user ");
    }

    /**
     * Register a new manager user.
     *
     * @param user the user to register
     * @return the registered user
     */
    public User registerManager(User user) {
        logger.debug("Registering manager user: {}", user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("MANAGER");

        return super.save(user, self.getSystemUser(), "Registering manager user ");
    }

    /**
     * Find a user by ID and create an audit record.
     * This method demonstrates how to use the audit functionality.
     *
     * @param id          the ID of the user to find
     * @param currentUser the user performing the operation
     * @param details     additional details about the operation
     * @return an Optional containing the user, or empty if not found
     */
    public java.util.Optional<User> findByIdWithAudit(UUID id, User currentUser, String details) {
        return findById(id, currentUser, details);
    }

    /**
     * Delete a user by ID and create an audit record.
     * This method demonstrates how to use the audit functionality.
     *
     * @param id          the ID of the user to delete
     * @param currentUser the user performing the operation
     * @param details     additional details about the operation
     */
    public void deleteByIdWithAudit(UUID id, User currentUser, String details) {
        deleteById(id, User.class, currentUser, details);
    }

    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return an Optional containing the user, or empty if not found
     */
    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Finding user by email: {}", email);
        return repository.findByEmail(email);
    }

    /**
     * Load a user by username (email in our case).
     *
     * @param email the email of the user to load
     * @return a UserDetails object containing the user's details
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOptional = repository.findByEmail(email);

        User user = userOptional.orElseThrow(() ->
                new UsernameNotFoundException("User not found with email: " + email));

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                user.getRole() != null ? "ROLE_" + user.getRole() : "ROLE_USER");

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}
