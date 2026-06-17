package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.Klinikum;
import de.htwg.in.nexcare.backend.model.Role;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import de.htwg.in.nexcare.backend.repository.KlinikumRepository;
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

@RestController
@RequestMapping("/api/klinikum")
public class KlinikumController {

    private static final Logger LOG = LoggerFactory.getLogger(KlinikumController.class);

    @Autowired
    private KlinikumRepository klinikumRepository;

    @Autowired
    private AppUserRepository userRepository;

    private boolean callerIsAdmin(Jwt jwt) {
        if (jwt == null) return false;
        return userRepository.findByOauthId(jwt.getSubject())
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    @GetMapping
    public List<Klinikum> getKlinika(@RequestParam(required = false) String name) {
        if (name != null && !name.isBlank()) {
            return klinikumRepository.findByNameContainingIgnoreCase(name);
        }
        return klinikumRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Klinikum> getKlinikumById(@PathVariable Long id) {
        return klinikumRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Klinikum> createKlinikum(@Valid @RequestBody Klinikum klinikum,
                                                    @AuthenticationPrincipal Jwt jwt) {
        if (!callerIsAdmin(jwt)) return ResponseEntity.status(403).build();
        klinikum.setId(null);
        Klinikum saved = klinikumRepository.save(klinikum);
        LOG.info("Created Klinikum id={}", saved.getId());
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Klinikum> updateKlinikum(@PathVariable Long id,
                                                    @Valid @RequestBody Klinikum details,
                                                    @AuthenticationPrincipal Jwt jwt) {
        if (!callerIsAdmin(jwt)) return ResponseEntity.status(403).build();
        Optional<Klinikum> opt = klinikumRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Klinikum k = opt.get();
        k.setName(details.getName());
        k.setOrt(details.getOrt());
        Klinikum saved = klinikumRepository.save(k);
        LOG.info("Updated Klinikum id={}", id);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteKlinikum(@PathVariable Long id,
                                                  @AuthenticationPrincipal Jwt jwt) {
        if (!callerIsAdmin(jwt)) return ResponseEntity.status(403).build();
        if (klinikumRepository.findById(id).isEmpty()) return ResponseEntity.notFound().build();
        klinikumRepository.deleteById(id);
        LOG.info("Deleted Klinikum id={}", id);
        return ResponseEntity.noContent().build();
    }
}
