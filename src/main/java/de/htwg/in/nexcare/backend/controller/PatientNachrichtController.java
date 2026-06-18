package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.PatientNachricht;
import de.htwg.in.nexcare.backend.repository.PatientNachrichtRepository;
import de.htwg.in.nexcare.backend.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient/{patientId}/nachrichten")
public class PatientNachrichtController {

    @Autowired private PatientNachrichtRepository nachrichtRepository;
    @Autowired private SecurityService securityService;

    @GetMapping
    public ResponseEntity<List<PatientNachricht>> getNachrichten(@PathVariable Long patientId,
                                                                  @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaffOrPatient(jwt, patientId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(nachrichtRepository.findByPatientIdOrderByErstelltAmDesc(patientId));
    }

    @GetMapping("/ungelesen")
    public ResponseEntity<Map<String, Long>> getUngelesen(@PathVariable Long patientId,
                                                           @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaffOrPatient(jwt, patientId)) return ResponseEntity.status(403).build();
        long count = nachrichtRepository.countByPatientIdAndGelesenFalse(patientId);
        return ResponseEntity.ok(Map.of("anzahl", count));
    }

    @PatchMapping("/{nachrichtId}/gelesen")
    public ResponseEntity<Void> markGelesen(@PathVariable Long patientId,
                                             @PathVariable Long nachrichtId,
                                             @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaffOrPatient(jwt, patientId)) return ResponseEntity.status(403).build();
        return nachrichtRepository.findById(nachrichtId).map(n -> {
            n.setGelesen(true);
            nachrichtRepository.save(n);
            return ResponseEntity.<Void>ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
