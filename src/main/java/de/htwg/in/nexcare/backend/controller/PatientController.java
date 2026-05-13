package de.htwg.in.nexcare.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.repository.PatientRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private static final Logger LOG = LoggerFactory.getLogger(PatientController.class);

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping
    public List<Patient> getPatients() {
        return patientRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        Optional<Patient> opt = patientRepository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Patient createPatient(@RequestBody Patient patient) {
        if (patient.getId() != null) {
            patient.setId(null);
            LOG.warn("Attempted to create a patient with an existing ID. ID has been reset to null.");
        }
        Patient saved = patientRepository.save(patient);
        LOG.info("Created new patient with id {}", saved.getId());
        return saved;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @RequestBody Patient details) {
        Optional<Patient> opt = patientRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
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
        LOG.info("Updated patient with id {}", updated.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePatient(@PathVariable Long id) {
        Optional<Patient> opt = patientRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        patientRepository.delete(opt.get());
        LOG.info("Deleted patient with id {}", id);
        return ResponseEntity.noContent().build();
    }
}
