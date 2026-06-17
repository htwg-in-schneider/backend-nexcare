package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.Medikament;
import de.htwg.in.nexcare.backend.model.MedikamentenEintrag;
import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.repository.MedikamentRepository;
import de.htwg.in.nexcare.backend.repository.MedikamentenEintragRepository;
import de.htwg.in.nexcare.backend.repository.PatientRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/** Manages the medication plan for a specific patient (UC4, UC5). Requires authentication. */
@RestController
@RequestMapping("/api/patient/{patientId}/medikamentenplan")
public class MedikamentenPlanController {

    private static final Logger LOG = LoggerFactory.getLogger(MedikamentenPlanController.class);

    @Autowired private MedikamentenEintragRepository eintragRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private MedikamentRepository medikamentRepository;

    @GetMapping
    public ResponseEntity<List<MedikamentenEintrag>> getEintraege(@PathVariable Long patientId) {
        if (!patientRepository.existsById(patientId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(eintragRepository.findByPatientId(patientId));
    }

    @PostMapping
    public ResponseEntity<MedikamentenEintrag> addEintrag(@PathVariable Long patientId,
                                                           @Valid @RequestBody MedikamentenEintrag eintrag) {
        Optional<Patient> patient = patientRepository.findById(patientId);
        if (patient.isEmpty()) return ResponseEntity.notFound().build();

        Optional<Medikament> medikament = medikamentRepository.findById(eintrag.getMedikament().getId());
        if (medikament.isEmpty()) return ResponseEntity.badRequest().build();

        eintrag.setId(null);
        eintrag.setPatient(patient.get());
        eintrag.setMedikament(medikament.get());
        MedikamentenEintrag saved = eintragRepository.save(eintrag);
        LOG.info("Added Medikament {} to patient {}", medikament.get().getName(), patientId);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{eintragId}")
    public ResponseEntity<Object> deleteEintrag(@PathVariable Long patientId,
                                                 @PathVariable Long eintragId) {
        if (!patientRepository.existsById(patientId)) return ResponseEntity.notFound().build();
        if (!eintragRepository.existsById(eintragId)) return ResponseEntity.notFound().build();
        eintragRepository.deleteById(eintragId);
        LOG.info("Removed Medikamenteneintrag {} from patient {}", eintragId, patientId);
        return ResponseEntity.noContent().build();
    }
}
