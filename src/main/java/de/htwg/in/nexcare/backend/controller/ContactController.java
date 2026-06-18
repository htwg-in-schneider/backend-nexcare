package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.EmailType;
import de.htwg.in.nexcare.backend.model.SystemSetting;
import de.htwg.in.nexcare.backend.repository.SystemSettingRepository;
import de.htwg.in.nexcare.backend.service.EmailService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    @Autowired private SystemSettingRepository settingRepository;
    @Autowired private EmailService emailService;

    public record ContactRequest(
        @NotBlank @Email @Size(max = 150) String absenderEmail,
        @NotBlank @Size(max = 200) String betreff,
        @NotBlank @Size(max = 2000) String nachricht
    ) {}

    @PostMapping
    public ResponseEntity<Map<String, String>> contact(@RequestBody ContactRequest req) {
        String adminEmail = settingRepository.findById("admin.email")
            .map(SystemSetting::getSettingValue)
            .orElse(null);

        if (adminEmail == null || adminEmail.isBlank()) {
            return ResponseEntity.ok(Map.of("status", "keine_admin_email"));
        }

        String html = emailService.kontaktHtml(req.absenderEmail(), req.betreff(), req.nachricht());
        emailService.send(adminEmail, "Kontaktanfrage: " + req.betreff(), html, EmailType.KONTAKT);
        return ResponseEntity.ok(Map.of("status", "gesendet"));
    }
}
