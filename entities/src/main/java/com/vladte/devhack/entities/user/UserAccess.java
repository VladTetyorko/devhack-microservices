package com.vladte.devhack.entities.user;

import com.vladte.devhack.entities.BasicEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user_access")
@Data
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