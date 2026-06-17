package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Vorname ist erforderlich")
    private String vorname;

    @NotBlank(message = "Nachname ist erforderlich")
    private String nachname;

    @NotNull(message = "Geburtsdatum ist erforderlich")
    private LocalDate geburtsdatum;

    @NotBlank(message = "Versicherungsnummer ist erforderlich")
    private String versicherungsnr;

    private String telefon;

    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;

    private String adresse;

    @ManyToOne
    @JoinColumn(name = "klinikum_id")
    private Klinikum klinikum;

    private String etage;
    private String abteilung;
    private String station;
    private String zimmer;
    private String bett;

    @Enumerated(EnumType.STRING)
    private PatientStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "name",      column = @Column(name = "notfall_name")),
        @AttributeOverride(name = "beziehung", column = @Column(name = "notfall_beziehung")),
        @AttributeOverride(name = "telefon",   column = @Column(name = "notfall_telefon"))
    })
    private NotfallKontakt notfallkontakt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Klinikum getKlinikum() { return klinikum; }
    public void setKlinikum(Klinikum klinikum) { this.klinikum = klinikum; }

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient that = (Patient) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Patient{id=" + id + ", vorname=" + vorname + ", nachname=" + nachname
                + ", versicherungsnr=" + versicherungsnr + ", status=" + status + "}";
    }
}
