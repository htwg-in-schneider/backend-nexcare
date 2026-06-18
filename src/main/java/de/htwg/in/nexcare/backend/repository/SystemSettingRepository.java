package de.htwg.in.nexcare.backend.repository;

import de.htwg.in.nexcare.backend.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {}
