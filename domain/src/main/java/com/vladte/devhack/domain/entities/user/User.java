package com.vladte.devhack.domain.entities.user;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.vladte.devhack.domain.entities.BasicEntity;
import com.vladte.devhack.domain.entities.enums.AuthProviderType;
import com.vladte.devhack.domain.entities.personalized.Answer;
import com.vladte.devhack.domain.entities.personalized.Note;
import com.vladte.devhack.domain.entities.personalized.VacancyResponse;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"profile", "userAccess", "authProviders", "answers", "notes", "vacancyResponses"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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

    @JsonIgnore
    public Optional<AuthenticationProvider> getLocalAuth() {
        return authProviders.stream()
                .filter(a -> a.getProvider() == AuthProviderType.LOCAL)
                .findFirst();
    }

}
