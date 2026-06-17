package de.htwg.in.nexcare.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.htwg.in.nexcare.backend.model.Klinikum;

import java.util.List;

@Repository
public interface KlinikumRepository extends JpaRepository<Klinikum, Long> {
    List<Klinikum> findByNameContainingIgnoreCase(String name);
}
