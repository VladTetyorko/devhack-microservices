package com.vladte.devhack.domain.model.dto.auth;

import com.vladte.devhack.domain.model.dto.user.UserDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for login responses.
 * Contains authentication result and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response containing authentication result")
public class LoginResponseDTO implements Serializable {

    @Schema(description = "Authentication success status", example = "true")
    private boolean success;

    @Schema(description = "Response message", example = "Login successful")
    private String message;

    @Schema(description = "JWT access token (if applicable)")
    private String accessToken;

    @Schema(description = "JWT refresh token (if applicable)")
    private String refreshToken;

    @Schema(description = "Token expiration time")
    private LocalDateTime tokenExpiry;

    @Schema(description = "Authenticated user information")
    private UserDTO user;

    @Schema(description = "User's assigned roles")
    private String[] roles;

    /**
     * Create a successful login response.
     *
     * @param message the success message
     * @param user    the authenticated user
     * @param roles   the user's roles
     * @return the login response
     */
    public static LoginResponseDTO success(String message, UserDTO user, String[] roles) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setSuccess(true);
        response.setMessage(message);
        response.setUser(user);
        response.setRoles(roles);
        return response;
    }

    /**
     * Create a successful login response with tokens.
     *
     * @param message      the success message
     * @param user         the authenticated user
     * @param roles        the user's roles
     * @param accessToken  the access token
     * @param refreshToken the refresh token
     * @param tokenExpiry  the token expiry time
     * @return the login response
     */
    public static LoginResponseDTO success(String message, UserDTO user, String[] roles,
                                           String accessToken, String refreshToken, LocalDateTime tokenExpiry) {
        LoginResponseDTO response = success(message, user, roles);
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenExpiry(tokenExpiry);
        return response;
    }

    /**
     * Create a failed login response.
     *
     * @param message the error message
     * @return the login response
     */
    public static LoginResponseDTO failure(String message) {
        LoginResponseDTO response = new LoginResponseDTO();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}