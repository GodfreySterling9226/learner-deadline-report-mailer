package edu.report.service;

import edu.report.domain.CourseDelivery;
import edu.report.domain.LearnerDeadlineReport;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class PdfDeadlineReport {
    public byte[] render(LearnerDeadlineReport report) {
        List<String> lines = new ArrayList<>();
        lines.add("Learner deadline report");
        lines.add("As of " + report.asOf());
        for (CourseDelivery course : report.dueSoon()) {
            lines.add(course.learnerName() + " | " + course.courseName() + " | due " + course.deadline());
        }
        String stream = "BT /F1 12 Tf 50 760 Td " + text(lines.get(0)) + " Tj";
        for (int i = 1; i < lines.size(); i++) {
            stream += " 0 -22 Td " + text(lines.get(i)) + " Tj";
        }
        stream += " ET";
        String pdf = "%PDF-1.4\n1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj\n"
                + "2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj\n"
                + "3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources<< /Font<< /F1 4 0 R >> >> /Contents 5 0 R >>endobj\n"
                + "4 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>endobj\n"
                + "5 0 obj<< /Length " + stream.length() + " >>stream\n" + stream + "\nendstream endobj\n"
                + "xref\n0 6\n0000000000 65535 f \ntrailer<< /Size 6 /Root 1 0 R >>\nstartxref\n0\n%%EOF\n";
        return pdf.getBytes(StandardCharsets.US_ASCII);
    }

    private static String text(String value) {
        return "(" + value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)") + ")";
    }
}
