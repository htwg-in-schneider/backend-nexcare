package de.htwg.in.nexcare.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SystemSetting {

    @Id
    private String settingKey;

    private String settingValue;

    public SystemSetting() {}

    public SystemSetting(String settingKey, String settingValue) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
    }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingValue() { return settingValue; }
    public void setSettingValue(String settingValue) { this.settingValue = settingValue; }
}
