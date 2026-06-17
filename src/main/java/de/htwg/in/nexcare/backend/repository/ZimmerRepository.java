package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.Zimmer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZimmerRepository extends JpaRepository<Zimmer, Long> {
    List<Zimmer> findByEtageIdOrderByNummerAsc(Long etageId);
}
