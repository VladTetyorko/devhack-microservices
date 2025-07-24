package com.vladte.devhack.entities.enums;

import lombok.Getter;

import java.io.Serializable;

/**
 * Enum representing the different statuses of a job vacancy.
 */
@Getter
public enum VacancyStatus implements Serializable {
    OPEN("Open"),
    CLOSED("Closed"),
    EXPIRED("Expired");

    private final String displayName;

    VacancyStatus(String displayName) {
        this.displayName = displayName;
    }
}