package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.repository.KlinikumRepository;
import de.htwg.in.nexcare.backend.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides bed occupancy data per klinikum (UC2, UC3, UC8).
 * Returns patients grouped-ready — frontend handles the grouping by zimmer.
 */
@RestController
@RequestMapping("/api/betten")
public class BettenController {

    @Autowired private PatientRepository patientRepository;
    @Autowired private KlinikumRepository klinikumRepository;

    @GetMapping("/klinikum/{klinikumId}")
    public ResponseEntity<List<Patient>> getPatientenByKlinikum(@PathVariable Long klinikumId) {
        if (!klinikumRepository.existsById(klinikumId)) return ResponseEntity.notFound().build();

        Specification<Patient> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("klinikum").get("id"), klinikumId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return ResponseEntity.ok(patientRepository.findAll(spec));
    }
}
