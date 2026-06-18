package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.Medikament;
import de.htwg.in.nexcare.backend.model.MedikamentenEintrag;
import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.model.PatientNachricht;
import de.htwg.in.nexcare.backend.repository.MedikamentRepository;
import de.htwg.in.nexcare.backend.repository.MedikamentenEintragRepository;
import de.htwg.in.nexcare.backend.repository.PatientNachrichtRepository;
import de.htwg.in.nexcare.backend.repository.PatientRepository;
import de.htwg.in.nexcare.backend.service.SecurityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Manages the medication plan for a specific patient (UC4, UC5). Requires authentication. */
@RestController
@RequestMapping("/api/patient/{patientId}/medikamentenplan")
public class MedikamentenPlanController {

    private static final Logger LOG = LoggerFactory.getLogger(MedikamentenPlanController.class);
    private static final DateTimeFormatter DE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired private MedikamentenEintragRepository eintragRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private MedikamentRepository medikamentRepository;
    @Autowired private PatientNachrichtRepository nachrichtRepository;
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

        if (eintrag.getStartDatum() != null && eintrag.getStartDatum().isBefore(LocalDate.of(2000, 1, 1))) {
            return ResponseEntity.badRequest().build();
        }
        if (eintrag.getEndDatum() != null && eintrag.getStartDatum() != null
                && eintrag.getEndDatum().isBefore(eintrag.getStartDatum())) {
            return ResponseEntity.badRequest().build();
        }

        eintrag.setId(null);
        eintrag.setPatient(patient.get());
        eintrag.setMedikament(medikament.get());
        MedikamentenEintrag saved = eintragRepository.save(eintrag);
        LOG.info("Added Medikament {} to patient {}", medikament.get().getName(), patientId);

        // Notify patient in portal
        String wochentage = humanWochentage(saved.getWochentage());
        nachrichtRepository.save(new PatientNachricht(patient.get(),
            "Neues Medikament verschrieben",
            medikament.get().getName() + " – " + saved.getDosierung() +
            " (" + wochentage + ", " + saved.getStartDatum().format(DE) +
            " bis " + saved.getEndDatum().format(DE) + ")",
            "MEDIKAMENT"));

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

    // ── helpers ──────────────────────────────────────────────────────────────

    private String humanWochentage(String codes) {
        if (codes == null) return "–";
        return codes.replace("MO", "Mo").replace("DI", "Di").replace("MI", "Mi")
                    .replace("DO", "Do").replace("FR", "Fr").replace("SA", "Sa")
                    .replace("SO", "So");
    }
}
