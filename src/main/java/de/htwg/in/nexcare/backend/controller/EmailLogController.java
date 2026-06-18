package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.EmailLog;
import de.htwg.in.nexcare.backend.repository.EmailLogRepository;
import de.htwg.in.nexcare.backend.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/email-log")
public class EmailLogController {

    @Autowired private EmailLogRepository emailLogRepository;
    @Autowired private SecurityService securityService;

    @GetMapping
    public ResponseEntity<List<EmailLog>> getLogs(
            @RequestParam(defaultValue = "100") int limit,
            @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isAdmin(jwt)) return ResponseEntity.status(403).build();
        List<EmailLog> logs = emailLogRepository.findAllByOrderBySentAtDesc(
            PageRequest.of(0, Math.min(limit, 500)));
        return ResponseEntity.ok(logs);
    }
}
