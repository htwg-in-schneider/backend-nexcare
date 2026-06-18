package de.htwg.in.nexcare.backend.service;

import de.htwg.in.nexcare.backend.model.AppUser;
import de.htwg.in.nexcare.backend.model.Role;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Central service for role-based authorization checks.
 * All role decisions go through here to keep controllers consistent.
 */
@Service
public class SecurityService {

    @Autowired
    private AppUserRepository userRepository;

    public Optional<AppUser> currentUser(Jwt jwt) {
        if (jwt == null) return Optional.empty();
        return userRepository.findByOauthId(jwt.getSubject());
    }

    /** Staff = ARZT, KRANKENSCHWESTER, or ADMIN. */
    public boolean isStaff(Jwt jwt) {
        return currentUser(jwt).map(u -> u.getRole() != null && u.getRole() != Role.PATIENT).orElse(false);
    }

    public boolean isAdmin(Jwt jwt) {
        return currentUser(jwt).map(u -> u.getRole() == Role.ADMIN).orElse(false);
    }

    public boolean isPatient(Jwt jwt) {
        return currentUser(jwt).map(u -> u.getRole() == Role.PATIENT).orElse(false);
    }

    /** Returns 403-appropriate indicator: caller is staff OR is the specific patient. */
    public boolean isStaffOrPatient(Jwt jwt, Long patientId) {
        return currentUser(jwt).map(u -> {
            if (u.getRole() != Role.PATIENT) return true;
            return patientId != null && patientId.equals(u.getPatientId());
        }).orElse(false);
    }
}
