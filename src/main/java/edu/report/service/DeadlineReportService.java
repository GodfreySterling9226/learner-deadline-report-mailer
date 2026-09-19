package edu.report.service;

import edu.report.domain.CourseDelivery;
import edu.report.domain.LearnerDeadlineReport;
import edu.report.infra.InfraiEmailClient;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public final class DeadlineReportService {
    private final PdfDeadlineReport pdfRenderer;
    private final InfraiEmailClient emailClient;

    public DeadlineReportService(PdfDeadlineReport pdfRenderer, InfraiEmailClient emailClient) {
        this.pdfRenderer = pdfRenderer;
        this.emailClient = emailClient;
    }

    public LearnerDeadlineReport selectDueSoon(List<CourseDelivery> courses, LocalDate asOf) {
        LocalDate windowEnd = asOf.plusDays(7);
        return new LearnerDeadlineReport(asOf, courses.stream()
                .filter(course -> !course.deadline().isBefore(asOf) && !course.deadline().isAfter(windowEnd))
                .sorted(Comparator.comparing(CourseDelivery::deadline))
                .toList());
    }

    public DeliveryResult createAndSend(List<CourseDelivery> courses, LocalDate asOf, String educatorEmail, Path pdfPath) {
        LearnerDeadlineReport report = selectDueSoon(courses, asOf);
        try {
            Files.createDirectories(pdfPath.getParent());
            Files.write(pdfPath, pdfRenderer.render(report));
        } catch (IOException e) {
            throw new IllegalStateException("Could not write report PDF", e);
        }
        String subject = "Learner deadlines through " + asOf.plusDays(7);
        String operationKey = "deadline-report-" + asOf + "-" + educatorEmail;
        String messageId = emailClient.sendReport(educatorEmail, subject, html(report), operationKey);
        return new DeliveryResult(pdfPath.getFileName().toString(), report.dueSoon().size(), messageId);
    }

    private static String html(LearnerDeadlineReport report) {
        String rows = report.dueSoon().stream()
                .map(course -> "<li>" + course.learnerName() + ": " + course.courseName() + " due " + course.deadline() + "</li>")
                .reduce("", String::concat);
        return "<h1>Learner deadline report</h1><p>As of " + report.asOf() + "</p><ul>" + rows + "</ul>";
    }

    public record DeliveryResult(String pdfName, int selectedLearners, String messageId) { }
}
