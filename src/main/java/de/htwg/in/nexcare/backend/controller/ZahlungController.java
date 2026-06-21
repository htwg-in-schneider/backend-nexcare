package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.AppUser;
import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.model.PatientNachricht;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import de.htwg.in.nexcare.backend.repository.PatientNachrichtRepository;
import de.htwg.in.nexcare.backend.repository.PatientRepository;
import de.htwg.in.nexcare.backend.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/portal")
public class ZahlungController {

    private static final double TAGESSATZ = 10.0;
    private static final DateTimeFormatter DE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Autowired private AppUserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private PatientNachrichtRepository nachrichtRepository;
    @Autowired private SecurityService securityService;

    @GetMapping("/eigenanteil")
    public ResponseEntity<Map<String, Object>> getEigenanteil(@AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isPatient(jwt)) return ResponseEntity.status(403).build();
        return resolvePatient(jwt).map(result -> {
            Patient p = (Patient) result[0];
            double betrag = berechne(p);
            Map<String, Object> resp = new java.util.LinkedHashMap<>();
            resp.put("betrag", betrag);
            resp.put("tage", tage(p));
            resp.put("aufnahmeDatum", p.getAufnahmeDatum() != null ? p.getAufnahmeDatum().format(DE) : "–");
            resp.put("bezahlt", p.isEigenanteilBezahlt());
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/zahlung")
    public ResponseEntity<Map<String, Object>> zahlung(@AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isPatient(jwt)) return ResponseEntity.status(403).build();
        return resolvePatient(jwt).map(result -> {
            Patient p = (Patient) result[0];

            if (p.isEigenanteilBezahlt()) {
                return ResponseEntity.ok(Map.<String, Object>of("status", "bereits_bezahlt"));
            }

            double betrag = berechne(p);
            String referenzNr = "NXC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            p.setEigenanteilBezahlt(true);
            patientRepository.save(p);

            nachrichtRepository.save(new PatientNachricht(p,
                "Zahlungsbestätigung",
                String.format("Ihr Eigenanteil von %.2f € wurde erfolgreich bezahlt. Referenznr.: %s",
                    betrag, referenzNr),
                "ALLGEMEIN"));

            return ResponseEntity.ok(Map.<String, Object>of(
                "status", "bezahlt",
                "betrag", betrag,
                "referenzNr", referenzNr
            ));
        }).orElse(ResponseEntity.notFound().build());
    }

    private java.util.Optional<Object[]> resolvePatient(Jwt jwt) {
        String oauthId = jwt.getSubject();
        return userRepository.findByOauthId(oauthId)
            .filter(u -> u.getPatientId() != null)
            .flatMap(u -> patientRepository.findById(u.getPatientId())
                .map(p -> new Object[]{p, u}));
    }

    private long tage(Patient p) {
        if (p.getAufnahmeDatum() == null) return 1;
        long d = ChronoUnit.DAYS.between(p.getAufnahmeDatum(), LocalDate.now());
        return Math.max(1, d);
    }

    private double berechne(Patient p) {
        return tage(p) * TAGESSATZ;
    }
}
