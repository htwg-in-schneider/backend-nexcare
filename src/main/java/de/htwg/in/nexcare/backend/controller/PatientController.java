package de.htwg.in.nexcare.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.repository.PatientRepository;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping
    public List<Patient> getPatients() {
        return patientRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<String> createPatient(@RequestBody Patient patient) {
        System.out.println("Controller called for patient: " + patient);
        return ResponseEntity.ok("POST successful");
    }
}
