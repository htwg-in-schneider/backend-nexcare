package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.AppUser;
import de.htwg.in.nexcare.backend.model.EmailType;
import de.htwg.in.nexcare.backend.model.Medikament;
import de.htwg.in.nexcare.backend.model.MedikamentenEintrag;
import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import de.htwg.in.nexcare.backend.repository.MedikamentRepository;
import de.htwg.in.nexcare.backend.repository.MedikamentenEintragRepository;
import de.htwg.in.nexcare.backend.repository.PatientRepository;
import de.htwg.in.nexcare.backend.service.EmailService;
import de.htwg.in.nexcare.backend.service.EmailService.MedikamentenplanZeile;
import de.htwg.in.nexcare.backend.service.SecurityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** Manages the medication plan for a specific patient (UC4, UC5). Requires authentication. */
@RestController
@RequestMapping("/api/patient/{patientId}/medikamentenplan")
public class MedikamentenPlanController {

    private static final Logger LOG = LoggerFactory.getLogger(MedikamentenPlanController.class);
    private static final DateTimeFormatter DE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired private MedikamentenEintragRepository eintragRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private MedikamentRepository medikamentRepository;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private EmailService emailService;
    @Autowired private SecurityService securityService;

    /** Staff or the patient themselves may read the plan. */
    @GetMapping
    public ResponseEntity<List<MedikamentenEintrag>> getEintraege(@PathVariable Long patientId,
                                                                    @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaffOrPatient(jwt, patientId)) return ResponseEntity.status(403).build();
        if (!patientRepository.existsById(patientId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(eintragRepository.findByPatientId(patientId));
    }

    /** Only staff (ARZT / KRANKENSCHWESTER / ADMIN) may prescribe medication. */
    @PostMapping
    public ResponseEntity<MedikamentenEintrag> addEintrag(@PathVariable Long patientId,
                                                           @Valid @RequestBody MedikamentenEintrag eintrag,
                                                           @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) return ResponseEntity.notFound().build();

        Optional<Medikament> medikament = medikamentRepository.findById(eintrag.getMedikament().getId());
        if (medikament.isEmpty()) return ResponseEntity.badRequest().build();

        if (eintrag.getEndDatum() != null && eintrag.getStartDatum() != null
                && eintrag.getEndDatum().isBefore(eintrag.getStartDatum())) {
            return ResponseEntity.badRequest().build();
        }

        eintrag.setId(null);
        eintrag.setPatient(patient.get());
        eintrag.setMedikament(medikament.get());
        MedikamentenEintrag saved = eintragRepository.save(eintrag);
        LOG.info("Added Medikament {} to patient {}", medikament.get().getName(), patientId);

        // notify patient if they have a contact email
        appUserRepository.findByPatientId(patientId).ifPresent(user -> {
            String kontaktEmail = user.getKontaktEmail();
            if (kontaktEmail != null && !kontaktEmail.isBlank()) {
                Patient p = patient.get();
                String name = p.getVorname() + " " + p.getNachname();
                String wochentage = humanWochentage(saved.getWochentage());
                String start = saved.getStartDatum().format(DE);
                String end = saved.getEndDatum().format(DE);
                String html = emailService.medikamentVerschriebenHtml(
                    name, medikament.get().getName(), saved.getDosierung(),
                    wochentage, start, end);
                emailService.send(kontaktEmail,
                    "Neues Medikament verschrieben – " + medikament.get().getName(),
                    html, EmailType.MEDIKAMENT_VERSCHRIEBEN);
            }
        });

        return ResponseEntity.ok(saved);
    }

    /** Only staff may remove medication entries. */
    @DeleteMapping("/{eintragId}")
    public ResponseEntity<Object> deleteEintrag(@PathVariable Long patientId,
                                                 @PathVariable Long eintragId,
                                                 @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        if (!patientRepository.existsById(patientId)) return ResponseEntity.notFound().build();
        if (!eintragRepository.existsById(eintragId)) return ResponseEntity.notFound().build();
        eintragRepository.deleteById(eintragId);
        LOG.info("Removed Medikamenteneintrag {} from patient {}", eintragId, patientId);
        return ResponseEntity.noContent().build();
    }

    /** Only staff may send the medication plan by email. */
    @PostMapping("/senden")
    public ResponseEntity<Map<String, String>> sendPlan(@PathVariable Long patientId,
                                                         @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        Optional<Patient> optPatient = patientRepository.findById(patientId);
        if (optPatient.isEmpty()) return ResponseEntity.notFound().build();

        Optional<AppUser> optUser = appUserRepository.findByPatientId(patientId);
        if (optUser.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Kein Benutzerkonto verknüpft."));
        }
        String kontaktEmail = optUser.get().getKontaktEmail();
        if (kontaktEmail == null || kontaktEmail.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Patient hat keine Kontakt-E-Mail hinterlegt."));
        }

        Patient patient = optPatient.get();
        String patientName = patient.getVorname() + " " + patient.getNachname();
        List<MedikamentenEintrag> eintraege = eintragRepository.findByPatientId(patientId);

        List<MedikamentenplanZeile> zeilen = new ArrayList<>();
        for (MedikamentenEintrag e : eintraege) {
            zeilen.add(new MedikamentenplanZeile(
                e.getMedikament().getName(),
                e.getDosierung(),
                humanWochentage(e.getWochentage()),
                e.getStartDatum().format(DE),
                e.getEndDatum().format(DE)
            ));
        }

        String html = emailService.medikamentenplanHtml(patientName, zeilen);
        byte[] ics = generateIcs(patientName, eintraege);

        emailService.sendWithAttachment(
            kontaktEmail,
            "Ihr Medikamentenplan – NexCare",
            html,
            EmailType.MEDIKAMENTENPLAN,
            "medikamentenplan.ics",
            ics,
            "text/calendar"
        );

        return ResponseEntity.ok(Map.of("status", "gesendet", "empfaenger", kontaktEmail));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private String humanWochentage(String codes) {
        if (codes == null) return "–";
        return codes.replace("MO", "Mo").replace("DI", "Di").replace("MI", "Mi")
                    .replace("DO", "Do").replace("FR", "Fr").replace("SA", "Sa")
                    .replace("SO", "So");
    }

    /** Maps NexCare weekday codes to RFC 5545 BYDAY values. */
    private String toRfc5545Day(String code) {
        return switch (code.trim()) {
            case "MO" -> "MO";
            case "DI" -> "TU";
            case "MI" -> "WE";
            case "DO" -> "TH";
            case "FR" -> "FR";
            case "SA" -> "SA";
            case "SO" -> "SU";
            default -> code;
        };
    }

    private byte[] generateIcs(String patientName, List<MedikamentenEintrag> eintraege) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//NexCare//Medikamentenplan//DE\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");

        DateTimeFormatter icsDate = DateTimeFormatter.ofPattern("yyyyMMdd");

        for (MedikamentenEintrag e : eintraege) {
            String[] uhrzeiten = e.getUhrzeiten() != null ? e.getUhrzeiten().split(",") : new String[]{"08:00"};
            String[] wochentage = e.getWochentage() != null ? e.getWochentage().split(",") : new String[]{};

            String byday = "";
            if (wochentage.length > 0) {
                StringBuilder bd = new StringBuilder();
                for (String w : wochentage) {
                    if (!bd.isEmpty()) bd.append(",");
                    bd.append(toRfc5545Day(w));
                }
                byday = "BYDAY=" + bd;
            }

            String until = e.getEndDatum().format(icsDate) + "T235959Z";
            String rrule = "FREQ=WEEKLY;" + byday + ";UNTIL=" + until;

            for (String zeit : uhrzeiten) {
                String[] parts = zeit.trim().split(":");
                int h = Integer.parseInt(parts[0]);
                int m = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                String dtstart = e.getStartDatum().format(icsDate) +
                                 String.format("T%02d%02d00", h, m);
                String dtend   = e.getStartDatum().format(icsDate) +
                                 String.format("T%02d%02d00", h + 1 > 23 ? 23 : h + 1, m);

                sb.append("BEGIN:VEVENT\r\n");
                sb.append("UID:").append(UUID.randomUUID()).append("@nexcare\r\n");
                sb.append("DTSTART:").append(dtstart).append("\r\n");
                sb.append("DTEND:").append(dtend).append("\r\n");
                sb.append("RRULE:").append(rrule).append("\r\n");
                sb.append("SUMMARY:").append(e.getMedikament().getName())
                  .append(" – ").append(e.getDosierung()).append("\r\n");
                sb.append("DESCRIPTION:Medikament für ").append(patientName)
                  .append("\\nDosierung: ").append(e.getDosierung()).append("\r\n");
                sb.append("CATEGORIES:Medikamente\r\n");
                sb.append("END:VEVENT\r\n");
            }
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
