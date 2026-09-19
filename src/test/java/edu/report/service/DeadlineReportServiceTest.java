package edu.report.service;

import edu.report.domain.CourseDelivery;
import edu.report.domain.LearnerDeadlineReport;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public final class DeadlineReportServiceTest {
    public static void main(String[] args) {
        DeadlineReportService service = new DeadlineReportService(new PdfDeadlineReport(), null);
        LocalDate asOf = LocalDate.of(2026, 9, 15);
        LearnerDeadlineReport report = service.selectDueSoon(List.of(
                new CourseDelivery("Amina Patel", "amina@example.com", "Data Ethics", asOf.plusDays(3)),
                new CourseDelivery("Jon Bell", "jon@example.com", "Algebra II", asOf.plusDays(9))), asOf);

        assert report.dueSoon().size() == 1 : "only learners inside the seven-day window are reported";
        assert report.dueSoon().get(0).learnerName().equals("Amina Patel");
        String pdf = new String(new PdfDeadlineReport().render(report), StandardCharsets.US_ASCII);
        assert pdf.contains("Amina Patel") && pdf.contains("Data Ethics") : "PDF records the selected learner";
        System.out.println("deadline decision verified");
    }
}
