package de.htwg.in.nexcare.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.htwg.in.nexcare.backend.model.Klinikum;

@Repository
public interface KlinikumRepository extends JpaRepository<Klinikum, Long> {
}
