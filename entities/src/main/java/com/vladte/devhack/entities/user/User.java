package com.vladte.devhack.entities.user;

import com.vladte.devhack.entities.BasicEntity;
import com.vladte.devhack.entities.enums.AuthProviderType;
import com.vladte.devhack.entities.personalized.Answer;
import com.vladte.devhack.entities.personalized.Note;
import com.vladte.devhack.entities.personalized.VacancyResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends BasicEntity {

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuthenticationProvider> authProviders;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserAccess userAccess;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Answer> answers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VacancyResponse> vacancyResponses = new ArrayList<>();

    public Optional<AuthenticationProvider> getLocalAuth() {
        return authProviders.stream()
                .filter(a -> a.getProvider() == AuthProviderType.LOCAL)
                .findFirst();
    }

    public Optional<AuthenticationProvider> findByProvider(AuthProviderType type) {
        return authProviders.stream()
                .filter(a -> a.getProvider() == type)
                .findFirst();
    }
}