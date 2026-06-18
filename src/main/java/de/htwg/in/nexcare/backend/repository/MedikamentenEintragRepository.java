package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.MedikamentenEintrag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MedikamentenEintragRepository extends JpaRepository<MedikamentenEintrag, Long> {
    @Query("SELECT e FROM MedikamentenEintrag e JOIN FETCH e.medikament WHERE e.patient.id = :pid")
    List<MedikamentenEintrag> findByPatientId(@Param("pid") Long patientId);
}
