package com.vladte.devhack.common.controller.global.basic.rest;

import com.vladte.devhack.common.service.security.JwtTokenProvider;
import com.vladte.devhack.domain.entities.enums.AuthProviderType;
import com.vladte.devhack.domain.entities.user.AuthenticationProvider;
import com.vladte.devhack.domain.entities.user.Profile;
import com.vladte.devhack.domain.entities.user.User;
import com.vladte.devhack.domain.model.dto.auth.*;
import com.vladte.devhack.domain.model.dto.user.UserDTO;
import com.vladte.devhack.domain.model.mapper.user.UserMapper;
import com.vladte.devhack.domain.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructor with service, mapper, authentication manager, and JWT token provider injection.
     *
     * @param userService           the user service
     * @param userMapper            the user mapper
     * @param authenticationManager the authentication manager
     * @param jwtTokenProvider      the JWT token provider
     */
    public AuthRestController(UserService userService, UserMapper userMapper,
                              AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Register a new user.
     *
     * @param registerRequest the registration request
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
            @Parameter(description = "Registration request")
            @Valid @RequestBody RegisterRequestDTO registerRequest) {
        log.debug("REST request to register user with email: {}", registerRequest.getEmail());

        // Validate password confirmation
        if (!registerRequest.isPasswordsMatch()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Passwords do not match");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // Check if email already exists
        if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Email already exists");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            // Create user from registration request
            User user = createUserFromRegisterRequest(registerRequest);
            User registeredUser = userService.register(user);
            UserDTO userDTO = userMapper.toDTO(registeredUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("user", userDTO);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            log.error("Error during user registration for email: {}", registerRequest.getEmail(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Registration failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Login a user.
     * Authenticates user credentials and returns user information.
     *
     * @param loginRequest the login request containing email and password
     * @return authentication response with user information
     */
    @PostMapping("/login")
    @Operation(summary = "Login a user", description = "Authenticates a user and returns user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<LoginResponseDTO> login(
            @Parameter(description = "Login request")
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        log.debug("REST request to login user with email: {}", loginRequest.getEmail());

        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Get user details
            Optional<User> userOptional = userService.findByEmail(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(LoginResponseDTO.failure("User not found"));
            }

            User user = userOptional.get();
            UserDTO userDTO = userMapper.toDTO(user);

            // Extract roles
            String[] roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toArray(String[]::new);

            // Generate JWT tokens
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(loginRequest.getEmail());

            // Get token expiration
            var tokenExpiry = jwtTokenProvider.getExpirationFromToken(accessToken);

            // Create successful response with JWT tokens
            LoginResponseDTO response = LoginResponseDTO.success(
                    "Login successful",
                    userDTO,
                    roles,
                    accessToken,
                    refreshToken,
                    tokenExpiry
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponseDTO.failure("Invalid email or password"));
        } catch (Exception e) {
            log.error("Error during login for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponseDTO.failure("An error occurred during login"));
        }
    }

    /**
     * Logout a user.
     * This is a placeholder for a real logout endpoint that would use Spring Security.
     * In a real application, this would be handled by Spring Security's logout filter.
     *
     * @return a success message
     */
//    @PostMapping("/logout")
//    @Operation(summary = "Logout a user", description = "Logs out a user")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "User logged out successfully",
//                    content = @Content(mediaType = "application/json"))
//    })
//    public ResponseEntity<?> logout() {
//        log.debug("REST request to logout user");
//
//        // This is a placeholder for a real logout endpoint
//        // In a real application, this would be handled by Spring Security's logout filter
//        Map<String, String> response = new HashMap<>();
//        response.put("message", "This is a placeholder for a real logout endpoint. In a real application, this would be handled by Spring Security's logout filter.");
//        return ResponseEntity.ok(response);
//    }

    /**
     * Request password reset.
     *
     * @param resetRequest the password reset request
     * @return success message
     */
    @PostMapping("/password-reset/request")
    @Operation(summary = "Request password reset", description = "Sends password reset email to user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset email sent",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> requestPasswordReset(
            @Parameter(description = "Password reset request")
            @Valid @RequestBody PasswordResetRequestDTO resetRequest) {
        log.debug("REST request to reset password for email: {}", resetRequest.getEmail());

        Optional<User> userOptional = userService.findByEmail(resetRequest.getEmail());
        if (userOptional.isEmpty()) {
            // For security reasons, we don't reveal if the email exists or not
            Map<String, String> response = new HashMap<>();
            response.put("message", "If the email exists, a password reset link has been sent.");
            return ResponseEntity.ok(response);
        }

        // TODO: Implement password reset token generation and email sending
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset functionality will be implemented in future versions.");
        return ResponseEntity.ok(response);
    }

    /**
     * Confirm password reset.
     *
     * @param confirmRequest the password reset confirmation
     * @return success message
     */
    @PostMapping("/password-reset/confirm")
    @Operation(summary = "Confirm password reset", description = "Resets password using token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid token or passwords don't match",
                    content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<?> confirmPasswordReset(
            @Parameter(description = "Password reset confirmation")
            @Valid @RequestBody PasswordResetConfirmDTO confirmRequest) {
        log.debug("REST request to confirm password reset with token");

        // Validate password confirmation
        if (!confirmRequest.isPasswordsMatch()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Passwords do not match");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // TODO: Implement password reset token validation and password update
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset confirmation functionality will be implemented in future versions.");
        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to create User entity from RegisterRequestDTO.
     *
     * @param registerRequest the registration request
     * @return the created User entity
     */
    private User createUserFromRegisterRequest(RegisterRequestDTO registerRequest) {
        User user = new User();

        // Create AuthenticationProvider for LOCAL authentication
        AuthenticationProvider localAuth = new AuthenticationProvider();
        localAuth.setProvider(AuthProviderType.LOCAL);
        localAuth.setEmail(registerRequest.getEmail());
        localAuth.setPasswordHash(registerRequest.getPassword()); // This will be encoded by the service
        localAuth.setUser(user);

        // Set up authProviders list
        user.setAuthProviders(List.of(localAuth));

        // Create Profile with name from registration request
        Profile profile = new Profile();
        profile.setName(registerRequest.getFullName());
        profile.setUser(user);
        user.setProfile(profile);

        return user;
    }

}
