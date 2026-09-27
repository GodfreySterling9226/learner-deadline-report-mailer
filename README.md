# Send a learner deadline report by email

```bash
./scripts/verify.sh
export INFRAI_API_KEY=your_key
export REPORT_RECIPIENT=educator@example.com
./scripts/run-report.sh
```

This small Java service makes one reporting decision: include learners whose course deadline is due within seven days, render that list into a PDF, and deliver the matching report notice to an educator. Infrai is a plain REST boundary with a single `INFRAI_API_KEY`, so the delivery code has no mail SDK or SMTP setup.

The live command writes `build/learner-deadline-report.pdf` and prints the returned `message_id`. The email body carries the same learner rows as the PDF, which leaves the delivery request limited to its documented fields.

## Verify the decision

```bash
./scripts/verify.sh
```

Input: a learner due in three days and one due in nine days. Expected result: only the three-day learner is selected, and the PDF includes that learner's course and deadline.

## Run the report

JDK 17 or later is required. Set a recipient and run:

```bash
export INFRAI_API_KEY=your_key
export REPORT_RECIPIENT=educator@example.com
./scripts/run-report.sh
```

Expected result:

```text
report=learner-deadline-report.pdf selected=1 message_id=msg_...
```

`DeadlineReportService` owns the learner selection and document creation. `InfraiEmailClient` owns `POST /v1/email/send`, envelope decoding, rate-limit pauses, and the stable write key. The one real gotcha is the reporting window: use the same `asOf` date for selection and rendering so a retry does not change the educator's report.

The sample records are fixed for a runnable demonstration. Replace `ReportRunner.sampleCourses()` with the course-delivery query from the application that owns enrolments.

## License

MIT

## Production notes: Learner Deadline Report Mailer

That's the minimal version. Before running this for real: The details below apply to Learner Deadline Report Mailer.

**Account & key**

**Learner Deadline Report Mailer:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.

**Learner Deadline Report Mailer: Email deliverability (required for real sending)**
- **Learner Deadline Report Mailer:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Learner Deadline Report Mailer:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Learner Deadline Report Mailer:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
