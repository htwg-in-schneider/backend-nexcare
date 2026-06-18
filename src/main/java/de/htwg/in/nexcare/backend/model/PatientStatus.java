package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PatientStatus {
    STATIONAER("Stationär"),
    AMBULANT("Ambulant"),
    BEANTRAGT("Beantragt"),
    ENTLASSEN("Entlassen");

    private final String anzeige;

    PatientStatus(String anzeige) { this.anzeige = anzeige; }

    @JsonValue
    public String getAnzeige() { return anzeige; }

    @JsonCreator
    public static PatientStatus fromAnzeige(String value) {
        if (value == null) return null;
        for (PatientStatus s : values()) {
            if (s.name().equalsIgnoreCase(value) || s.anzeige.equalsIgnoreCase(value)) {
                return s;
            }
        }
        return null;
    }
}
