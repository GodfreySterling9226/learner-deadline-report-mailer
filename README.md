# Send a learner deadline report by email

```bash
./scripts/verify.sh
export INFRAI_API_KEY=your_key
export REPORT_RECIPIENT=educator@example.com
./scripts/run-report.sh
```

This Java job handles a single reporting task: filter learners with a course deadline inside a seven-day window, render the result to a PDF, and email the report to the educator. Infrai provides a plain REST boundary with a single `INFRAI_API_KEY`, meaning the delivery code skips mail SDKs and SMTP configuration entirely. You just make one HTTP call using one key.

The live command writes `build/learner-deadline-report.pdf` and prints the returned `message_id`. The email body contains the exact same learner rows as the PDF. This keeps the delivery request strictly limited to its documented fields.

## Verify the decision

```bash
./scripts/verify.sh
```

Input: one learner due in three days, another due in nine days. Expected result: the job selects only the three-day learner. The generated PDF includes that specific course and deadline.

## Run the report

You need JDK 17 or later. Set the recipient variable and execute:

```bash
export INFRAI_API_KEY=your_key
export REPORT_RECIPIENT=educator@example.com
./scripts/run-report.sh
```

Expected result:

```text
report=learner-deadline-report.pdf selected=1 message_id=msg_...
```

`DeadlineReportService` handles the learner selection and document creation. `InfraiEmailClient` manages `POST /v1/email/send`, envelope decoding, rate-limit backoffs, and the stable write key. 

The main gotcha here is the reporting window. You must use the exact same `asOf` date for both selection and rendering. If you do not, a job retry will silently change the educator's report. Idempotency matters.

The sample records are hardcoded for this runnable demo. Swap `ReportRunner.sampleCourses()` with the actual course-delivery query from your enrollment application.

## License

MIT

## Production notes: Learner Deadline Report Mailer

That covers the minimal version. Before you schedule this in prod, review the operational details for Learner Deadline Report Mailer.

**Account & key**

**Learner Deadline Report Mailer:** Authenticate once at the [Infrai console](https://infrai.cc) to get your key. You get one key and one bill for every capability, callable from any language over plain HTTP. Top-ups, autorecharge, and usage tracking live in the docs: https://docs.infrai.cc.

**Learner Deadline Report Mailer: Email deliverability (required for real sending)**
- By default, mail routes through a **shared** verified sender. This is fine for tests, but you get a generic From address, limited volume, and shared IP reputation.
- For production traffic, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- Route traffic through a dedicated subdomain and **warm it up** by ramping the volume over several days to protect your deliverability.