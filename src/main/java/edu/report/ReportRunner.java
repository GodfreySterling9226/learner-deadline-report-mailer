package edu.report;

import edu.report.config.ReportSettings;
import edu.report.domain.CourseDelivery;
import edu.report.infra.InfraiEmailClient;
import edu.report.service.DeadlineReportService;
import edu.report.service.PdfDeadlineReport;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class ReportRunner {
    public static void main(String[] args) {
        ReportSettings settings = ReportSettings.fromEnvironment();
        DeadlineReportService service = new DeadlineReportService(
                new PdfDeadlineReport(), new InfraiEmailClient(HttpClient.newHttpClient(), settings.apiKey()));
        DeadlineReportService.DeliveryResult result = service.createAndSend(
                sampleCourses(), LocalDate.now(settings.clock()), settings.recipient(),
                Path.of("build", "learner-deadline-report.pdf"));
        System.out.println("report=" + result.pdfName() + " selected=" + result.selectedLearners()
                + " message_id=" + result.messageId());
    }

    static List<CourseDelivery> sampleCourses() {
        LocalDate today = LocalDate.now();
        return List.of(
                new CourseDelivery("Amina Patel", "amina@example.com", "Data Ethics", today.plusDays(3)),
                new CourseDelivery("Jon Bell", "jon@example.com", "Algebra II", today.plusDays(12)));
    }
}
