package de.htwg.in.nexcare.backend.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class NotfallKontakt {
    private String name;
    private String beziehung;
    private String telefon;

    public NotfallKontakt() {
    }

    public NotfallKontakt(String name, String beziehung, String telefon) {
        this.name = name;
        this.beziehung = beziehung;
        this.telefon = telefon;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBeziehung() { return beziehung; }
    public void setBeziehung(String beziehung) { this.beziehung = beziehung; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
}
