package de.htwg.in.nexcare.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    public static class Patient {
        private String vorname;
        private String nachname;

        public Patient() {
        }

        public Patient(String vorname, String nachname) {
            this.vorname = vorname;
            this.nachname = nachname;
        }

        public String getVorname() {
            return vorname;
        }

        public void setVorname(String vorname) {
            this.vorname = vorname;
        }

        public String getNachname() {
            return nachname;
        }

        public void setNachname(String nachname) {
            this.nachname = nachname;
        }
    }

    @GetMapping
    public List<Patient> getPatients() {
        return Arrays.asList(
                new Patient("Maria", "Schmidt"),
                new Patient("Thomas", "Weber"),
                new Patient("Anna", "Hoffmann"),
                new Patient("Peter", "Braun"),
                new Patient("Lisa", "Krause"));
    }

    @PostMapping
    public ResponseEntity<String> createPatient(@RequestBody Patient patient) {
        System.out.println("Controller called for patient: " + patient.getVorname() + " " + patient.getNachname());
        return ResponseEntity.ok("POST successful");
    }
}
