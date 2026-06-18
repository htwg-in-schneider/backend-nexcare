package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.PatientNachricht;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientNachrichtRepository extends JpaRepository<PatientNachricht, Long> {
    List<PatientNachricht> findByPatientIdOrderByErstelltAmDesc(Long patientId);
    long countByPatientIdAndGelesenFalse(Long patientId);
}
