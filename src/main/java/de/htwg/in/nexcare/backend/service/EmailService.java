package de.htwg.in.nexcare.backend.service;

import de.htwg.in.nexcare.backend.model.EmailLog;
import de.htwg.in.nexcare.backend.model.EmailType;
import de.htwg.in.nexcare.backend.repository.EmailLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final Logger LOG = LoggerFactory.getLogger(EmailService.class);

    // ── shared layout constants ──────────────────────────────────────────────
    private static final String HEADER_BLUE   = "background:linear-gradient(135deg,#2563eb,#1d4ed8)";
    private static final String HEADER_GREEN  = "background:linear-gradient(135deg,#059669,#047857)";
    private static final String HEADER_AMBER  = "background:linear-gradient(135deg,#d97706,#b45309)";
    private static final String HEADER_VIOLET = "background:linear-gradient(135deg,#7c3aed,#6d28d9)";

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Value("${spring.mail.username:noreply@example.com}")
    private String fromAddress;

    // ── core send ────────────────────────────────────────────────────────────

    public void send(String to, String subject, String htmlBody, EmailType type) {
        boolean ok = false;
        String err = null;
        if (mailSender == null) {
            err = "JavaMailSender not configured";
            LOG.warn("JavaMailSender not configured – skipping email to {}", to);
        } else {
            try {
                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
                helper.setFrom(fromAddress);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
                mailSender.send(msg);
                ok = true;
                LOG.info("Email sent [{}] to {}: {}", type, to, subject);
            } catch (Exception e) {
                err = e.getMessage();
                LOG.error("Failed to send email [{}] to {}: {}", type, to, e.getMessage());
            }
        }
        emailLogRepository.save(new EmailLog(to, subject, type, LocalDateTime.now(), ok, err));
    }

    public void sendWithAttachment(String to, String subject, String htmlBody,
                                   EmailType type, String attachmentName,
                                   byte[] attachmentBytes, String mimeType) {
        boolean ok = false;
        String err = null;
        if (mailSender == null) {
            err = "JavaMailSender not configured";
            LOG.warn("JavaMailSender not configured – skipping email to {}", to);
        } else {
            try {
                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setFrom(fromAddress);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(htmlBody, true);
                helper.addAttachment(attachmentName,
                    () -> new java.io.ByteArrayInputStream(attachmentBytes), mimeType);
                mailSender.send(msg);
                ok = true;
                LOG.info("Email+attachment sent [{}] to {}", type, to);
            } catch (Exception e) {
                err = e.getMessage();
                LOG.error("Failed to send email+attachment [{}] to {}: {}", type, to, e.getMessage());
            }
        }
        emailLogRepository.save(new EmailLog(to, subject, type, LocalDateTime.now(), ok, err));
    }

    // ── shared layout builder ────────────────────────────────────────────────

    private String layout(String headerGradient, String headerLabel, String body) {
        return """
            <!DOCTYPE html>
            <html lang="de">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width,initial-scale=1"/>
            </head>
            <body style="margin:0;padding:0;background:#f1f5f9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Helvetica,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f1f5f9;padding:2.5rem 1rem;">
                <tr><td align="center">
                  <table width="600" cellpadding="0" cellspacing="0"
                         style="background:#ffffff;border-radius:1rem;overflow:hidden;
                                box-shadow:0 4px 32px rgba(0,0,0,.08);max-width:600px;">

                    <!-- ── Header ── -->
                    <tr><td style="%s;padding:2rem 2rem 1.75rem;text-align:center;">
                      <div style="display:inline-flex;align-items:center;gap:.6rem;">
                        <span style="font-size:1.5rem;color:#fff;font-weight:800;letter-spacing:-.02em;">NexCare</span>
                      </div>
                      <p style="margin:.35rem 0 0;color:rgba(255,255,255,.75);font-size:.825rem;letter-spacing:.04em;text-transform:uppercase;">%s</p>
                    </td></tr>

                    <!-- ── Content ── -->
                    <tr><td style="padding:2rem 2rem 1.5rem;">%s</td></tr>

                    <!-- ── Footer ── -->
                    <tr><td style="background:#f8fafc;padding:1.125rem 2rem;text-align:center;border-top:1px solid #e2e8f0;">
                      <p style="margin:0;color:#94a3b8;font-size:.7rem;letter-spacing:.02em;">
                        NexCare · HTWG Konstanz · Patientenverwaltungssystem
                      </p>
                    </td></tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(headerGradient, headerLabel, body);
    }

    private String infoBox(String html) {
        return "<div style=\"background:#f8fafc;border-radius:.75rem;border:1px solid #e2e8f0;" +
               "padding:1.25rem 1.5rem;margin:1.25rem 0;\">" + html + "</div>";
    }

    private String detailRow(String label, String value, boolean first) {
        String border = first ? "" : "border-top:1px solid #e2e8f0;";
        return "<div style=\"display:flex;justify-content:space-between;align-items:baseline;" +
               "padding:.5rem 0;" + border + "\">" +
               "<span style=\"color:#64748b;font-size:.8125rem;\">" + label + "</span>" +
               "<span style=\"color:#1e293b;font-weight:600;font-size:.875rem;\">" + value + "</span>" +
               "</div>";
    }

    private String autoNote() {
        return "<p style=\"margin:1.75rem 0 0;color:#94a3b8;font-size:.75rem;line-height:1.6;\">" +
               "Diese E-Mail wurde automatisch versandt. Bitte antworten Sie nicht direkt auf diese Nachricht.</p>";
    }

    // ── templates ────────────────────────────────────────────────────────────

    public String willkommensEmail(String patientName, String infoLink) {
        String body = """
            <h2 style="margin:0 0 .625rem;font-size:1.2rem;font-weight:700;color:#1e293b;">
              Willkommen im NexCare-Patientenportal, %s!
            </h2>
            <p style="margin:0 0 1.5rem;color:#475569;line-height:1.7;font-size:.9rem;">
              Ihre Patientenakte wurde durch das medizinische Fachpersonal im NexCare-System angelegt.
              In der aktuellen Version ist eine eigenständige Online-Registrierung noch nicht möglich –
              Ihr Zugang wird durch das Klinikum eingerichtet.
            </p>
            <div style="text-align:center;margin:1.75rem 0;">
              <a href="%s"
                 style="display:inline-block;background:#2563eb;color:#fff;
                        padding:.875rem 2.25rem;border-radius:.75rem;
                        text-decoration:none;font-weight:700;font-size:.9rem;
                        letter-spacing:.01em;">
                Weitere Informationen
              </a>
            </div>
            """.formatted(patientName, infoLink) + autoNote();
        return layout(HEADER_BLUE, "Willkommen", body);
    }

    public String aufnahmeBestaetigt(String patientName, String klinikum, String abteilung) {
        String abt = (abteilung != null && !abteilung.isBlank()) ? abteilung : "–";
        String body = """
            <h2 style="margin:0 0 .625rem;font-size:1.2rem;font-weight:700;color:#1e293b;">
              Ihr Aufnahmeantrag wurde bestätigt
            </h2>
            <p style="margin:0 0 1.25rem;color:#475569;line-height:1.7;font-size:.9rem;">
              Sehr geehrte/r %s,<br/>Ihr Aufnahmeantrag wurde von unserem medizinischen Team geprüft und genehmigt.
              Bitte melden Sie sich direkt an der Aufnahme des Klinikums.
            </p>
            """.formatted(patientName)
            + infoBox(detailRow("Klinikum", klinikum, true) + detailRow("Abteilung", abt, false))
            + autoNote();
        return layout(HEADER_GREEN, "Aufnahmebestätigung", body);
    }

    public String zahlungHtml(String patientName, String referenzNr, double betrag,
                               String klinikum, String aufnahmeDatum, String zahlungsDatum) {
        String body = """
            <h2 style="margin:0 0 .625rem;font-size:1.2rem;font-weight:700;color:#1e293b;">
              Zahlungsbestätigung
            </h2>
            <p style="margin:0 0 1.25rem;color:#475569;line-height:1.7;font-size:.9rem;">
              Ihre Zahlung wurde erfolgreich verarbeitet. Nachfolgend finden Sie Ihre Belegdetails.
            </p>
            """
            + infoBox(
                detailRow("Patient", patientName, true) +
                detailRow("Klinikum", klinikum, false) +
                detailRow("Aufnahmedatum", aufnahmeDatum, false) +
                detailRow("Zahlungsdatum", zahlungsDatum, false) +
                "<div style=\"padding:.75rem 0 .25rem;border-top:2px solid #2563eb;margin-top:.5rem;" +
                "display:flex;justify-content:space-between;align-items:baseline;\">" +
                "<span style=\"color:#2563eb;font-weight:700;font-size:.9rem;\">Gesamtbetrag</span>" +
                "<span style=\"color:#2563eb;font-weight:800;font-size:1.25rem;\">" +
                String.format("%.2f", betrag) + " €</span></div>"
            )
            + "<p style=\"margin:.75rem 0 0;color:#64748b;font-size:.775rem;\">Referenznummer: " +
              "<strong style=\"color:#1e293b;\">" + referenzNr + "</strong></p>"
            + autoNote();
        return layout(HEADER_BLUE, "Patientenportal", body);
    }

    public String kontaktHtml(String absender, String betreff, String nachricht) {
        String body = """
            <h2 style="margin:0 0 1.25rem;font-size:1.1rem;font-weight:700;color:#1e293b;">
              Neue Kontaktanfrage
            </h2>
            """
            + infoBox(
                detailRow("Von", absender, true) +
                detailRow("Betreff", betreff, false)
            )
            + "<div style=\"margin-top:1rem;\">"
            + "<p style=\"margin:0 0 .4rem;color:#64748b;font-size:.8rem;font-weight:600;" +
              "text-transform:uppercase;letter-spacing:.05em;\">Nachricht</p>"
            + "<div style=\"background:#f8fafc;border-radius:.75rem;border:1px solid #e2e8f0;" +
              "padding:1.25rem 1.5rem;color:#1e293b;line-height:1.7;font-size:.9rem;\">"
            + nachricht.replace("\n", "<br/>")
            + "</div></div>";
        return layout(HEADER_VIOLET, "Kontaktanfrage", body);
    }

    public String medikamentVerschriebenHtml(String patientName, String medikamentName,
                                              String dosierung, String wochentage,
                                              String startDatum, String endDatum) {
        String body = """
            <h2 style="margin:0 0 .625rem;font-size:1.2rem;font-weight:700;color:#1e293b;">
              Neues Medikament verschrieben
            </h2>
            <p style="margin:0 0 1.25rem;color:#475569;line-height:1.7;font-size:.9rem;">
              Sehr geehrte/r %s,<br/>Ihr behandelndes Team hat Ihnen ein neues Medikament verschrieben.
              Die Details finden Sie nachfolgend.
            </p>
            """.formatted(patientName)
            + infoBox(
                detailRow("Medikament", medikamentName, true) +
                detailRow("Dosierung", dosierung, false) +
                detailRow("Einnahmetage", wochentage, false) +
                detailRow("Zeitraum", startDatum + " – " + endDatum, false)
            )
            + "<p style=\"margin:1rem 0 0;color:#475569;font-size:.875rem;line-height:1.6;\">"
            + "Ihren vollständigen Medikamentenplan können Sie jederzeit im Patientenportal einsehen.</p>"
            + autoNote();
        return layout(HEADER_GREEN, "Medikamente", body);
    }

    public String bettZugewiesenHtml(String patientName, String klinikum,
                                      String etage, String zimmer, String bett) {
        String body = """
            <h2 style="margin:0 0 .625rem;font-size:1.2rem;font-weight:700;color:#1e293b;">
              Bett zugewiesen
            </h2>
            <p style="margin:0 0 1.25rem;color:#475569;line-height:1.7;font-size:.9rem;">
              Sehr geehrte/r %s,<br/>Ihnen wurde ein Bett zugewiesen. Bitte melden Sie sich an der Aufnahme.
            </p>
            """.formatted(patientName)
            + infoBox(
                detailRow("Klinikum", klinikum != null ? klinikum : "–", true) +
                detailRow("Etage/Station", etage != null ? etage : "–", false) +
                detailRow("Zimmer", zimmer != null ? zimmer : "–", false) +
                detailRow("Bett", bett != null ? bett : "–", false)
            )
            + autoNote();
        return layout(HEADER_AMBER, "Bettenzuweisung", body);
    }

    public String medikamentenplanHtml(String patientName, java.util.List<MedikamentenplanZeile> eintraege) {
        StringBuilder rows = new StringBuilder();
        for (int i = 0; i < eintraege.size(); i++) {
            MedikamentenplanZeile z = eintraege.get(i);
            rows.append(detailRow(z.medikament(), z.dosierung() + " · " + z.wochentage(), i == 0));
            rows.append("<div style=\"text-align:right;padding-bottom:.25rem;" +
                        "color:#94a3b8;font-size:.75rem;\">")
                .append(z.startDatum()).append(" – ").append(z.endDatum())
                .append("</div>");
        }
        String body = """
            <h2 style="margin:0 0 .625rem;font-size:1.2rem;font-weight:700;color:#1e293b;">
              Ihr aktueller Medikamentenplan
            </h2>
            <p style="margin:0 0 1.25rem;color:#475569;line-height:1.7;font-size:.9rem;">
              Sehr geehrte/r %s,<br/>anbei erhalten Sie Ihren aktuellen Medikamentenplan.
              Im Anhang dieser E-Mail finden Sie eine Kalenderdatei (.ics), mit der Sie Ihre
              Einnahmezeitpunkte direkt in Ihren Kalender importieren können.
            </p>
            """.formatted(patientName)
            + infoBox(rows.toString())
            + "<p style=\"margin:1rem 0 0;color:#475569;font-size:.875rem;line-height:1.6;\">"
            + "Öffnen Sie die angehängte <strong>.ics-Datei</strong>, um die Termine in "
            + "Google Kalender, Apple Kalender oder Outlook zu importieren.</p>"
            + autoNote();
        return layout(HEADER_VIOLET, "Medikamentenplan", body);
    }

    public record MedikamentenplanZeile(String medikament, String dosierung,
                                        String wochentage, String startDatum, String endDatum) {}
}
