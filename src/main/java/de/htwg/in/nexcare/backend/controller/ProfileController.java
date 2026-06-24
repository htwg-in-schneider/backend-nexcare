package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.AppUser;
import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import de.htwg.in.nexcare.backend.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** Allows authenticated users to read and update their own profile. */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileController.class);

    @Autowired private AppUserRepository userRepository;
    @Autowired private PatientRepository patientRepository;

    @GetMapping
    public ResponseEntity<AppUser> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        return userRepository.findByOauthId(oauthId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Returns the Patient record linked to the logged-in PATIENT user. */
    @GetMapping("/mein-patient")
    public ResponseEntity<Patient> getMeinPatient(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        return userRepository.findByOauthId(oauthId)
                .filter(u -> u.getPatientId() != null)
                .flatMap(u -> patientRepository.findById(u.getPatientId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/mein-patient")
    public ResponseEntity<Patient> updateMeinPatient(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestBody Patient details) {
        String oauthId = jwt.getSubject();
        return userRepository.findByOauthId(oauthId)
                .filter(u -> u.getPatientId() != null)
                .flatMap(u -> patientRepository.findById(u.getPatientId()))
                .map(patient -> {
                    if (details.getVorname() != null) patient.setVorname(details.getVorname());
                    if (details.getNachname() != null) patient.setNachname(details.getNachname());
                    patient.setEmail(details.getEmail());
                    patient.setTelefon(details.getTelefon());
                    patient.setStrasse(details.getStrasse());
                    patient.setHausnummer(details.getHausnummer());
                    patient.setAdresszusatz(details.getAdresszusatz());
                    patient.setPlz(details.getPlz());
                    patient.setOrt(details.getOrt());
                    patient.setLand(details.getLand());
                    Patient saved = patientRepository.save(patient);
                    LOG.info("Patient {} updated own profile", saved.getId());
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<AppUser> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestBody AppUser details) {
        String oauthId = jwt.getSubject();
        return userRepository.findByOauthId(oauthId).map(user -> {
            if (details.getName() != null && !details.getName().isBlank()) {
                user.setName(details.getName());
            }
            user.setStrasse(details.getStrasse());
            user.setPlz(details.getPlz());
            user.setOrt(details.getOrt());
            user.setKontaktEmail(details.getKontaktEmail());
            AppUser saved = userRepository.save(user);
            LOG.info("Updated profile for sub={}", oauthId);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
}
