package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.AppUser;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/** Allows authenticated users to read and update their own profile. */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Logger LOG = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private AppUserRepository userRepository;

    @GetMapping
    public ResponseEntity<AppUser> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        LOG.info("getProfile for sub={}", oauthId);
        return userRepository.findByOauthId(oauthId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<AppUser> updateProfile(@AuthenticationPrincipal Jwt jwt,
                                                  @RequestBody AppUser details) {
        String oauthId = jwt.getSubject();
        return userRepository.findByOauthId(oauthId).map(user -> {
            if (details.getName() != null && !details.getName().isBlank()) {
                user.setName(details.getName());
            }
            user.setAdresse(details.getAdresse());
            AppUser saved = userRepository.save(user);
            LOG.info("Updated profile for sub={}", oauthId);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }
}
