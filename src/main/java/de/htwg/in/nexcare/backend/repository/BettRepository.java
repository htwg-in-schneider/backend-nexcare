package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.Bett;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BettRepository extends JpaRepository<Bett, Long> {
    List<Bett> findByZimmerIdOrderByBezeichnungAsc(Long zimmerId);
    Optional<Bett> findByPatientId(Long patientId);
}
