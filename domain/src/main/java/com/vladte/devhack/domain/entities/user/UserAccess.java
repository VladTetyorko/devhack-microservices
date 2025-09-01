package com.vladte.devhack.domain.entities.user;

import com.vladte.devhack.domain.entities.BasicEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_access")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user"})
public class UserAccess extends BasicEntity {

    @Column(nullable = false)
    private String role;

    @Column(name = "ai_usage_allowed")
    private Boolean isAiUsageAllowed = false;

    @Column(name = "account_locked")
    private Boolean isAccountLocked = false;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
}