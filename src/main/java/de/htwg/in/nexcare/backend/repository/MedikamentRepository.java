package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.Medikament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedikamentRepository extends JpaRepository<Medikament, Long> {
    List<Medikament> findByArchiviertFalse();
    List<Medikament> findByWirkstoffContainingIgnoreCaseAndArchiviertFalse(String wirkstoff);
    List<Medikament> findByNameContainingIgnoreCaseAndArchiviertFalse(String name);
}
