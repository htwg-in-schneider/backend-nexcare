package de.htwg.in.nexcare.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import de.htwg.in.nexcare.backend.model.Bett;
import de.htwg.in.nexcare.backend.model.BettStatus;
import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.model.PatientNachricht;
import de.htwg.in.nexcare.backend.model.PatientStatus;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import de.htwg.in.nexcare.backend.repository.BettRepository;
import de.htwg.in.nexcare.backend.repository.PatientNachrichtRepository;
import de.htwg.in.nexcare.backend.repository.PatientRepository;
import de.htwg.in.nexcare.backend.service.SecurityService;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private static final Logger LOG = LoggerFactory.getLogger(PatientController.class);

    @Autowired private PatientRepository patientRepository;
    @Autowired private AppUserRepository appUserRepository;
    @Autowired private PatientNachrichtRepository nachrichtRepository;
    @Autowired private BettRepository bettRepository;
    @Autowired private SecurityService securityService;

    /** Any authenticated user can list patients (role-restricted views handled in frontend). */
    @GetMapping
    public ResponseEntity<List<Patient>> getPatients(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) PatientStatus status,
            @RequestParam(required = false) Long klinikum) {

        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();

        if (name == null && status == null && klinikum == null) {
            return ResponseEntity.ok(patientRepository.findAll());
        }

        Specification<Patient> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (name != null && !name.isBlank()) {
                String like = "%" + name.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("vorname")), like),
                        cb.like(cb.lower(root.get("nachname")), like),
                        cb.like(cb.lower(root.get("versicherungsnr")), like)
                ));
            }
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (klinikum != null) predicates.add(cb.equal(root.get("klinikum").get("id"), klinikum));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return ResponseEntity.ok(patientRepository.findAll(spec));
    }

    /** Staff can view any patient; a patient can only view their own record. */
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id,
                                                   @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaffOrPatient(jwt, id)) return ResponseEntity.status(403).build();
        return patientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Only staff (ARZT / KRANKENSCHWESTER / ADMIN) may create patient records. */
    @PostMapping
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody Patient patient,
                                                  @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();

        if (patient.getId() != null) patient.setId(null);
        if (patient.getGeburtsdatum() != null && patient.getGeburtsdatum().getYear() < 1900) {
            return ResponseEntity.badRequest().build();
        }
        if (patient.getAufnahmeDatum() == null) patient.setAufnahmeDatum(java.time.LocalDate.now());

        Patient saved = patientRepository.save(patient);
        LOG.info("Created new patient id={}", saved.getId());

        // Send welcome message in patient portal
        nachrichtRepository.save(new PatientNachricht(saved,
            "Willkommen im NexCare-Patientenportal",
            "Ihre Patientenakte wurde angelegt. Sobald Ihr Zugang eingerichtet ist, " +
            "können Sie sich hier anmelden und Ihren Behandlungsplan einsehen.",
            "WILLKOMMEN"));

        return ResponseEntity.ok(saved);
    }

    /** Only staff may discharge (sets status to ENTLASSEN, record is preserved). */
    @PatchMapping("/{id}/entlassen")
    public ResponseEntity<Patient> entlassen(@PathVariable Long id,
                                              @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        return patientRepository.findById(id).map(p -> {
            p.setStatus(PatientStatus.ENTLASSEN);
            // Bett freigeben, damit es neu belegt werden kann
            bettRepository.findByPatientId(id).ifPresent(bett -> {
                bett.setPatient(null);
                bett.setStatus(BettStatus.FREI);
                bettRepository.save(bett);
            });
            p.setBett(null); p.setZimmer(null); p.setEtage(null);
            p.setAbteilung(null); p.setStation(null);
            Patient saved = patientRepository.save(p);
            LOG.info("Patient {} entlassen, Bett freigegeben", id);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Only staff may update patient records. */
    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id,
                                                  @Valid @RequestBody Patient details,
                                                  @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();

        Optional<Patient> opt = patientRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Patient patient = opt.get();
        patient.setVorname(details.getVorname());
        patient.setNachname(details.getNachname());
        patient.setGeburtsdatum(details.getGeburtsdatum());
        patient.setVersicherungsnr(details.getVersicherungsnr());
        patient.setTelefon(details.getTelefon());
        patient.setEmail(details.getEmail());
        patient.setAdresse(details.getAdresse());
        patient.setKlinikum(details.getKlinikum());
        patient.setEtage(details.getEtage());
        patient.setAbteilung(details.getAbteilung());
        patient.setStation(details.getStation());
        patient.setZimmer(details.getZimmer());
        patient.setBett(details.getBett());
        patient.setStatus(details.getStatus());
        patient.setNotfallkontakt(details.getNotfallkontakt());
        Patient updated = patientRepository.save(patient);
        LOG.info("Updated patient id={}", updated.getId());
        return ResponseEntity.ok(updated);
    }

    /** Only ADMIN may permanently delete a patient record. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePatient(@PathVariable Long id,
                                                 @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isAdmin(jwt)) return ResponseEntity.status(403).build();
        Optional<Patient> opt = patientRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        patientRepository.delete(opt.get());
        LOG.info("Deleted patient id={}", id);
        return ResponseEntity.noContent().build();
    }
}
