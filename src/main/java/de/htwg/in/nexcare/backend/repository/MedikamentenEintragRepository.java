package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.MedikamentenEintrag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedikamentenEintragRepository extends JpaRepository<MedikamentenEintrag, Long> {
    List<MedikamentenEintrag> findByPatientId(Long patientId);
}
