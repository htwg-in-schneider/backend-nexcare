package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.AntragStatus;
import de.htwg.in.nexcare.backend.model.AufnahmeAntrag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AufnahmeAntragRepository extends JpaRepository<AufnahmeAntrag, Long> {
    List<AufnahmeAntrag> findByAntragStatusOrderByErstelltAmDesc(AntragStatus status);
    Optional<AufnahmeAntrag> findByOauthIdAndAntragStatus(String oauthId, AntragStatus status);
    List<AufnahmeAntrag> findByOauthIdOrderByErstelltAmDesc(String oauthId);
}
