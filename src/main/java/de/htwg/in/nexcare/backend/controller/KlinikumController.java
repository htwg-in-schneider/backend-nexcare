package de.htwg.in.nexcare.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.htwg.in.nexcare.backend.model.Klinikum;
import de.htwg.in.nexcare.backend.repository.KlinikumRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/klinikum")
public class KlinikumController {

    @Autowired
    private KlinikumRepository klinikumRepository;

    @GetMapping
    public List<Klinikum> getKlinika() {
        return klinikumRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Klinikum> getKlinikumById(@PathVariable Long id) {
        Optional<Klinikum> opt = klinikumRepository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
