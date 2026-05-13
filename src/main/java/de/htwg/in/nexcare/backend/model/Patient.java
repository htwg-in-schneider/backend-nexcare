package de.htwg.in.nexcare.backend.model;

import java.time.LocalDate;

public class Patient {
    private long id;
    private String vorname;
    private String nachname;
    private LocalDate geburtsdatum;
    private String versicherungsnr;
    private String telefon;
    private String email;
    private String adresse;
    private String klinikum;
    private String etage;
    private String abteilung;
    private String station;
    private String zimmer;
    private String bett;
    private PatientStatus status;
    private NotfallKontakt notfallkontakt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getVorname() { return vorname; }
    public void setVorname(String vorname) { this.vorname = vorname; }

    public String getNachname() { return nachname; }
    public void setNachname(String nachname) { this.nachname = nachname; }

    public LocalDate getGeburtsdatum() { return geburtsdatum; }
    public void setGeburtsdatum(LocalDate geburtsdatum) { this.geburtsdatum = geburtsdatum; }

    public String getVersicherungsnr() { return versicherungsnr; }
    public void setVersicherungsnr(String versicherungsnr) { this.versicherungsnr = versicherungsnr; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getKlinikum() { return klinikum; }
    public void setKlinikum(String klinikum) { this.klinikum = klinikum; }

    public String getEtage() { return etage; }
    public void setEtage(String etage) { this.etage = etage; }

    public String getAbteilung() { return abteilung; }
    public void setAbteilung(String abteilung) { this.abteilung = abteilung; }

    public String getStation() { return station; }
    public void setStation(String station) { this.station = station; }

    public String getZimmer() { return zimmer; }
    public void setZimmer(String zimmer) { this.zimmer = zimmer; }

    public String getBett() { return bett; }
    public void setBett(String bett) { this.bett = bett; }

    public PatientStatus getStatus() { return status; }
    public void setStatus(PatientStatus status) { this.status = status; }

    public NotfallKontakt getNotfallkontakt() { return notfallkontakt; }
    public void setNotfallkontakt(NotfallKontakt notfallkontakt) { this.notfallkontakt = notfallkontakt; }

    @Override
    public String toString() {
        return "Patient{id=" + id + ", vorname=" + vorname + ", nachname=" + nachname
                + ", versicherungsnr=" + versicherungsnr + ", status=" + status + "}";
    }
}
