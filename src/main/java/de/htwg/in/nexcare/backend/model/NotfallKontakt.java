package de.htwg.in.nexcare.backend.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Embeddable
public class NotfallKontakt {
    @Size(max = 100, message = "Name darf maximal 100 Zeichen haben")
    @Pattern(regexp = "^$|^[\\p{L} .'-]+$", message = "Name darf nur Buchstaben, Bindestriche und Leerzeichen enthalten")
    private String name;

    @Size(max = 100, message = "Beziehung darf maximal 100 Zeichen haben")
    private String beziehung;

    @Pattern(regexp = "^$|^[+0-9\\s() -]{1,20}$", message = "Telefonnummer ist ungültig")
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
