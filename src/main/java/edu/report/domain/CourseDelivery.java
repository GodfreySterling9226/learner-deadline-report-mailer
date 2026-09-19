package edu.report.domain;

import java.time.LocalDate;

public record CourseDelivery(String learnerName, String learnerEmail, String courseName, LocalDate deadline) {
    public CourseDelivery {
        if (learnerName.isBlank() || learnerEmail.isBlank() || courseName.isBlank()) {
            throw new IllegalArgumentException("Learner and course fields are required");
        }
    }
}
