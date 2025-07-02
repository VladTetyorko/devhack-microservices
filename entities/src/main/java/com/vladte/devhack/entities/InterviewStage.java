package com.vladte.devhack.entities;

import lombok.Getter;

import java.io.Serializable;

/**
 * Enum representing the different stages of an interview process.
 */
@Getter
public enum InterviewStage implements Serializable {
    APPLIED("Applied"),
    PRE_SCREEN("Pre-Screen"),
    SCREENING("Screening"),
    TECHNICAL_INTERVIEW("Technical Interview"),
    PM_INTERVIEW("PM Interview"),
    STAKEHOLDER_INTERVIEW("Stakeholder Interview"),
    OFFER("Offer"),
    REJECTED("Rejected"),
    ACCEPTED("Accepted");

    private final String displayName;

    InterviewStage(String displayName) {
        this.displayName = displayName;
    }
}