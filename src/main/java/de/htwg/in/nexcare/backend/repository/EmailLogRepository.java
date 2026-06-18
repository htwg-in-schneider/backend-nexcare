package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.EmailLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findAllByOrderBySentAtDesc(Pageable pageable);
}
