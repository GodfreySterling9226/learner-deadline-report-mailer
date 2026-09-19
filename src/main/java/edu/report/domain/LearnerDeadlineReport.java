package edu.report.domain;

import java.time.LocalDate;
import java.util.List;

public record LearnerDeadlineReport(LocalDate asOf, List<CourseDelivery> dueSoon) {
    public LearnerDeadlineReport {
        dueSoon = List.copyOf(dueSoon);
    }
}
