package com.vladte.devhack.entities.user;

import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.enums.AuthProviderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
        name = "user_auth_providers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "provider"}),         // one per type
                @UniqueConstraint(columnNames = {"provider", "provider_user_id"}) // unique social ID
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationProvider extends BasicEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProviderType provider;

    /**
     * For LOCAL: this is null (we use email field).
     * For SOCIAL: the unique ID returned by the provider (Google sub, Facebook id…)
     */
    @Column(name = "provider_user_id", length = 100)
    private String providerUserId;

    /**
     * LOCAL only: user’s email/login credential
     **/
    @Column(length = 100)
    private String email;

    /**
     * LOCAL only: hashed password
     **/
    @Column(length = 200)
    private String passwordHash;

    /**
     * SOCIAL only: optional access token
     */
    @Column(name = "access_token", length = 500)
    private String accessToken;

    /**
     * SOCIAL only: optional refresh token
     */
    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    /**
     * SOCIAL only: when the access token expires
     */
    private LocalDateTime tokenExpiry;


}
