package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.Medikament;
import de.htwg.in.nexcare.backend.model.Role;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import de.htwg.in.nexcare.backend.repository.MedikamentRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/** Manages the global drug catalog. Reads are public; writes are admin-only. */
@RestController
@RequestMapping("/api/medikament")
public class MedikamentController {

    private static final Logger LOG = LoggerFactory.getLogger(MedikamentController.class);

    @Autowired private MedikamentRepository medikamentRepository;
    @Autowired private AppUserRepository userRepository;

    private boolean callerIsAdmin(Jwt jwt) {
        if (jwt == null) return false;
        return userRepository.findByOauthId(jwt.getSubject())
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    @GetMapping
    public List<Medikament> getMedikamente(@RequestParam(required = false) String suche) {
        if (suche != null && !suche.isBlank()) {
            // search by name OR wirkstoff
            List<Medikament> byName = medikamentRepository.findByNameContainingIgnoreCaseAndArchiviertFalse(suche);
            List<Medikament> byWirkstoff = medikamentRepository.findByWirkstoffContainingIgnoreCaseAndArchiviertFalse(suche);
            byName.addAll(byWirkstoff.stream().filter(m -> byName.stream().noneMatch(n -> n.getId().equals(m.getId()))).toList());
            return byName;
        }
        return medikamentRepository.findByArchiviertFalse();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medikament> getMedikamentById(@PathVariable Long id) {
        return medikamentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Medikament> createMedikament(@Valid @RequestBody Medikament medikament,
                                                        @AuthenticationPrincipal Jwt jwt) {
        if (!callerIsAdmin(jwt)) return ResponseEntity.status(403).build();
        medikament.setId(null);
        Medikament saved = medikamentRepository.save(medikament);
        LOG.info("Created Medikament id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medikament> updateMedikament(@PathVariable Long id,
                                                        @Valid @RequestBody Medikament details,
                                                        @AuthenticationPrincipal Jwt jwt) {
        if (!callerIsAdmin(jwt)) return ResponseEntity.status(403).build();
        Optional<Medikament> opt = medikamentRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Medikament m = opt.get();
        m.setName(details.getName());
        m.setWirkstoff(details.getWirkstoff());
        m.setBeschreibung(details.getBeschreibung());
        m.setDosiereinheit(details.getDosiereinheit());
        Medikament saved = medikamentRepository.save(m);
        LOG.info("Updated Medikament id={}", id);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> archiviereMedikament(@PathVariable Long id,
                                                        @AuthenticationPrincipal Jwt jwt) {
        if (!callerIsAdmin(jwt)) return ResponseEntity.status(403).build();
        Optional<Medikament> opt = medikamentRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Medikament m = opt.get();
        m.setArchiviert(true);
        medikamentRepository.save(m);
        LOG.info("Archived Medikament id={}", id);
        return ResponseEntity.noContent().build();
    }
}
