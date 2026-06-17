package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.AppUser;
import de.htwg.in.nexcare.backend.model.Role;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Admin-only endpoint for user management. Role check is done in-code since we store roles in DB. */
@RestController
@RequestMapping("/api/admin/users")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    private boolean callerIsAdmin(Jwt jwt) {
        String oauthId = jwt.getSubject();
        return appUserRepository.findByOauthId(oauthId)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
    }

    @GetMapping
    public ResponseEntity<List<AppUser>> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        if (!callerIsAdmin(jwt)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable Long id,
                                                @AuthenticationPrincipal Jwt jwt) {
        if (!callerIsAdmin(jwt)) return ResponseEntity.status(403).build();
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppUser> updateUser(@PathVariable Long id,
                                               @RequestBody AppUser details,
                                               @AuthenticationPrincipal Jwt jwt) {
        if (!callerIsAdmin(jwt)) return ResponseEntity.status(403).build();
        return userRepository.findById(id).map(user -> {
            if (details.getName() != null && !details.getName().isBlank()) {
                user.setName(details.getName());
            }
            user.setAdresse(details.getAdresse());
            if (details.getRole() != null) {
                user.setRole(details.getRole());
            }
            AppUser saved = userRepository.save(user);
            LOG.info("Admin updated user id={}", id);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
}
