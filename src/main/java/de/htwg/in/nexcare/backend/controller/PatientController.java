package de.htwg.in.nexcare.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @GetMapping
    public List<String> getPatients() {
        return Arrays.asList("Maria Schmidt",
                "Thomas Weber",
                "Anna Hoffmann",
                "Peter Braun",
                "Lisa Krause");
    }
}
