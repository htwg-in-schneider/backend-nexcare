package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.SystemSetting;
import de.htwg.in.nexcare.backend.repository.SystemSettingRepository;
import de.htwg.in.nexcare.backend.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
public class SystemSettingController {

    @Autowired private SystemSettingRepository settingRepository;
    @Autowired private SecurityService securityService;

    @GetMapping
    public ResponseEntity<List<SystemSetting>> getAll(@AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isAdmin(jwt)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(settingRepository.findAll());
    }

    @PutMapping("/{key}")
    public ResponseEntity<SystemSetting> upsert(@PathVariable String key,
                                                 @RequestBody Map<String, String> body,
                                                 @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isAdmin(jwt)) return ResponseEntity.status(403).build();
        String value = body.getOrDefault("value", "");
        SystemSetting setting = settingRepository.findById(key)
            .orElse(new SystemSetting(key, value));
        setting.setSettingValue(value);
        return ResponseEntity.ok(settingRepository.save(setting));
    }
}
