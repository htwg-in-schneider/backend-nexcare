package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.Etage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EtageRepository extends JpaRepository<Etage, Long> {
    List<Etage> findByKlinikumIdOrderByNummerAsc(Long klinikumId);
}
