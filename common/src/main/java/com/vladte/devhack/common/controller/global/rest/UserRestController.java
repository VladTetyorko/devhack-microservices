package com.vladte.devhack.common.controller.global.rest;

import com.vladte.devhack.common.controller.BaseRestController;
import com.vladte.devhack.common.dto.UserDTO;
import com.vladte.devhack.common.mapper.UserMapper;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing User entities.
 * Provides RESTful API endpoints for CRUD operations on users.
 */
@RestController
@RequestMapping("/api/users")
@io.swagger.v3.oas.annotations.tags.Tag(name = "User", description = "User management API")
@Slf4j
public class UserRestController extends BaseRestController<User, UserDTO, UUID, UserService, UserMapper> {

    /**
     * Constructor with service and mapper injection.
     *
     * @param userService the user service
     * @param userMapper  the user mapper
     */
    public UserRestController(UserService userService, UserMapper userMapper) {
        super(userService, userMapper);
    }

    /**
     * Find a user by email.
     *
     * @param email the email to search for
     * @return the user with the specified email
     */
    @GetMapping("/by-email")
    @Operation(summary = "Find a user by email", description = "Returns a user with the specified email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content)
    })
    public ResponseEntity<UserDTO> findByEmail(
            @Parameter(description = "Email to search for")
            @RequestParam String email) {
        log.debug("REST request to find user by email: {}", email);
        Optional<User> user = service.findByEmail(email);
        return user.map(u -> ResponseEntity.ok(mapper.toDTO(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Register a new user.
     *
     * @param dto the user to register
     * @return the registered user
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Registers a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    public ResponseEntity<UserDTO> register(
            @Parameter(description = "User to register")
            @Valid @RequestBody UserDTO dto) {
        log.debug("REST request to register user: {}", dto);
        User user = mapper.toEntity(dto);
        User registeredUser = service.reguister(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(registeredUser));
    }

    /**
     * Register a new manager user.
     *
     * @param dto the user to register
     * @return the registered user
     */
    @PostMapping("/register-manager")
    @Operation(summary = "Register a new manager user", description = "Registers a new manager user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Manager user registered successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content)
    })
    public ResponseEntity<UserDTO> registerManager(
            @Parameter(description = "User to register as manager")
            @Valid @RequestBody UserDTO dto) {
        log.debug("REST request to register manager user: {}", dto);
        User user = mapper.toEntity(dto);
        User registeredUser = service.registerManager(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDTO(registeredUser));
    }

    /**
     * Get the system user.
     *
     * @return the system user
     */
    @GetMapping("/system")
    @Operation(summary = "Get the system user", description = "Returns the system user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "System user found",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<UserDTO> getSystemUser() {
        log.debug("REST request to get system user");
        User systemUser = service.getSystemUser();
        return ResponseEntity.ok(mapper.toDTO(systemUser));
    }
}