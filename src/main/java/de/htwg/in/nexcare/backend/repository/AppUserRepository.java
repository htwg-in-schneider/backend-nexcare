package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByOauthId(String oauthId);
    Optional<AppUser> findByEmail(String email);
    Optional<AppUser> findByPatientId(Long patientId);
}
