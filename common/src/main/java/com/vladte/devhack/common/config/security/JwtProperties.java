package com.vladte.devhack.common.config.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for JWT tokens.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * JWT secret key for signing tokens.
     */
    private String secret = "devhack-secret-key-change-in-production-environment-for-security";

    /**
     * JWT token expiration time in milliseconds.
     * Default: 24 hours (86400000 ms)
     */
    private long expiration = 86400000;

    /**
     * JWT refresh token expiration time in milliseconds.
     * Default: 7 days (604800000 ms)
     */
    private long refreshExpiration = 604800000;

    /**
     * JWT token issuer.
     */
    private String issuer = "DevHack";

    /**
     * JWT token audience.
     */
    private String audience = "DevHack-Users";
}