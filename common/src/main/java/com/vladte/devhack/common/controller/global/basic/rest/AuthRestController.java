package com.vladte.devhack.common.controller.global.basic.rest;

import com.vladte.devhack.common.dto.UserDTO;
import com.vladte.devhack.common.mapper.UserMapper;
import com.vladte.devhack.common.service.domain.UserService;
import com.vladte.devhack.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for authentication operations.
 * Provides RESTful API endpoints for login, logout, and registration.
 */
@RestController
@RequestMapping("/api/auth")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Authentication", description = "Authentication API")
@Slf4j
public class AuthRestController {

    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Constructor with service and mapper injection.
     *
     * @param userService the user service
     * @param userMapper  the user mapper
     */
    public AuthRestController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
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
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> register(
            @Parameter(description = "User to register")
            @Valid @RequestBody UserDTO dto) {
        log.debug("REST request to register user: {}", dto);

        // Check if email already exists
        if (userService.findByEmail(dto.getEmail()).isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Email already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        User user = userMapper.toEntity(dto);
        User registeredUser = userService.reguister(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toDTO(registeredUser));
    }

    /**
     * Login a user.
     * This is a placeholder for a real login endpoint that would use Spring Security.
     * In a real application, this would be handled by Spring Security's authentication filter.
     *
     * @param loginRequest the login request containing email and password
     * @return a token or user information
     */
    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Authenticates a user and returns a token or user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> login(
            @Parameter(description = "Login request")
            @Valid @RequestBody LoginRequest loginRequest) {
        log.debug("REST request to login user with email: {}", loginRequest.getEmail());

        // This is a placeholder for a real login endpoint
        // In a real application, this would be handled by Spring Security's authentication filter
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a placeholder for a real login endpoint. In a real application, this would be handled by Spring Security's authentication filter.");
        return ResponseEntity.ok(response);
    }

    /**
     * Logout a user.
     * This is a placeholder for a real logout endpoint that would use Spring Security.
     * In a real application, this would be handled by Spring Security's logout filter.
     *
     * @return a success message
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout a user", description = "Logs out a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged out successfully",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> logout() {
        log.debug("REST request to logout user");

        // This is a placeholder for a real logout endpoint
        // In a real application, this would be handled by Spring Security's logout filter
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a placeholder for a real logout endpoint. In a real application, this would be handled by Spring Security's logout filter.");
        return ResponseEntity.ok(response);
    }

    /**
     * Data class for login requests.
     */
    @Setter
    @Getter
    @Validated
    private static class LoginRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 100, message = "Email must be less than 100 characters")
        @Schema(description = "User's email address", example = "user@example.com", required = true)
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        @Schema(description = "User's password", required = true)
        private String password;

    }
}
