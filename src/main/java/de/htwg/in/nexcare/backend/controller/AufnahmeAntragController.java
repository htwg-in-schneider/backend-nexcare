package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.*;
import de.htwg.in.nexcare.backend.repository.*;
import de.htwg.in.nexcare.backend.service.SecurityService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class AufnahmeAntragController {

    private static final Logger LOG = LoggerFactory.getLogger(AufnahmeAntragController.class);

    @Autowired private AufnahmeAntragRepository antragRepository;
    @Autowired private AppUserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private KlinikumRepository klinikumRepository;
    @Autowired private PatientNachrichtRepository nachrichtRepository;
    @Autowired private SecurityService securityService;

    public record AntragRequest(
        @NotNull Long klinikumId,
        @Size(max = 100) String abteilung,
        @Size(max = 100) String station,
        @Size(max = 1000) String nachricht
    ) {}

    /** Patient submits a self-admission request. Only PATIENT role may call this. */
    @PostMapping("/api/portal/aufnahme-antrag")
    public ResponseEntity<AufnahmeAntrag> submitAntrag(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody AntragRequest req) {
        if (!securityService.isPatient(jwt)) return ResponseEntity.status(403).build();
        String oauthId = jwt.getSubject();

        AppUser user = userRepository.findByOauthId(oauthId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        // Prevent duplicate open requests
        if (antragRepository.findByOauthIdAndAntragStatus(oauthId, AntragStatus.OFFEN).isPresent()) {
            return ResponseEntity.status(409).build();
        }

        Klinikum klinikum = klinikumRepository.findById(req.klinikumId()).orElse(null);
        if (klinikum == null) return ResponseEntity.badRequest().build();

        AufnahmeAntrag antrag = new AufnahmeAntrag();
        antrag.setOauthId(oauthId);
        antrag.setPatientName(user.getName());
        antrag.setPatientEmail(user.getEmail());
        antrag.setKlinikum(klinikum);
        antrag.setAbteilung(req.abteilung());
        antrag.setStation(req.station());
        antrag.setNachricht(req.nachricht());
        antrag.setErstelltAm(LocalDateTime.now());
        antrag.setAntragStatus(AntragStatus.OFFEN);

        AufnahmeAntrag saved = antragRepository.save(antrag);
        LOG.info("Aufnahmeantrag #{} von user {} erstellt", saved.getId(), oauthId);
        return ResponseEntity.ok(saved);
    }

    /** Returns the open antrag for the current patient user (if any). PATIENT only. */
    @GetMapping("/api/portal/aufnahme-antrag")
    public ResponseEntity<AufnahmeAntrag> getMeinAntrag(@AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isPatient(jwt)) return ResponseEntity.status(403).build();
        String oauthId = jwt.getSubject();
        return antragRepository.findByOauthIdAndAntragStatus(oauthId, AntragStatus.OFFEN)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.noContent().build());
    }

    /** Staff: list all open admission requests. */
    @GetMapping("/api/aufnahme-antraege")
    public ResponseEntity<List<AufnahmeAntrag>> listOffeneAntraege(@AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(antragRepository.findByAntragStatusOrderByErstelltAmDesc(AntragStatus.OFFEN));
    }

    /** Staff: confirm antrag → create Patient record, link to user. */
    @PostMapping("/api/aufnahme-antraege/{id}/bestaetigen")
    public ResponseEntity<Patient> bestaetigen(@PathVariable Long id,
                                                @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        AufnahmeAntrag antrag = antragRepository.findById(id).orElse(null);
        if (antrag == null) return ResponseEntity.notFound().build();
        if (antrag.getAntragStatus() != AntragStatus.OFFEN) return ResponseEntity.status(409).build();

        // Create or find patient for this user
        AppUser user = userRepository.findByOauthId(antrag.getOauthId()).orElse(null);

        Patient patient;
        if (user != null && user.getPatientId() != null) {
            patient = patientRepository.findById(user.getPatientId()).orElse(new Patient());
        } else {
            patient = new Patient();
        }

        if (patient.getVorname() == null) {
            String[] parts = antrag.getPatientName().split(" ", 2);
            patient.setVorname(parts[0]);
            patient.setNachname(parts.length > 1 ? parts[1] : parts[0]);
            patient.setVersicherungsnr("AUSSTEHEND-" + id);
        }
        patient.setKlinikum(antrag.getKlinikum());
        patient.setAbteilung(antrag.getAbteilung());
        patient.setStation(antrag.getStation());
        patient.setStatus(PatientStatus.STATIONAER);
        if (patient.getAufnahmeDatum() == null) {
            patient.setAufnahmeDatum(LocalDate.now());
        }

        Patient saved = patientRepository.save(patient);

        if (user != null) {
            user.setPatientId(saved.getId());
            userRepository.save(user);
        }

        antrag.setAntragStatus(AntragStatus.BESTAETIGT);
        antrag.setPatientId(saved.getId());
        antragRepository.save(antrag);

        LOG.info("Aufnahmeantrag #{} bestätigt → Patient #{}", id, saved.getId());

        // Notify patient in portal
        String klinikumName = antrag.getKlinikum() != null ? antrag.getKlinikum().getName() : "–";
        String abteilung = antrag.getAbteilung() != null && !antrag.getAbteilung().isBlank() ? antrag.getAbteilung() : "–";
        nachrichtRepository.save(new PatientNachricht(saved,
            "Aufnahmeantrag bestätigt",
            "Ihr Aufnahmeantrag wurde genehmigt. Klinikum: " + klinikumName + ", Abteilung: " + abteilung +
            ". Bitte melden Sie sich an der Aufnahme.",
            "AUFNAHME"));

        return ResponseEntity.ok(saved);
    }

    /** Staff: reject antrag. */
    @PostMapping("/api/aufnahme-antraege/{id}/ablehnen")
    public ResponseEntity<Void> ablehnen(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        AufnahmeAntrag antrag = antragRepository.findById(id).orElse(null);
        if (antrag == null) return ResponseEntity.notFound().build();
        if (antrag.getAntragStatus() != AntragStatus.OFFEN) return ResponseEntity.status(409).build();

        antrag.setAntragStatus(AntragStatus.ABGELEHNT);
        antragRepository.save(antrag);

        LOG.info("Aufnahmeantrag #{} abgelehnt", id);
        return ResponseEntity.noContent().build();
    }
}
