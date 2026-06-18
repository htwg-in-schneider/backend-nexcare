package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 100, message = "Vorname darf maximal 100 Zeichen haben")
    private String vorname;

    @NotBlank(message = "Nachname ist erforderlich")
    @Size(max = 100, message = "Nachname darf maximal 100 Zeichen haben")
    private String nachname;

    @NotNull(message = "Geburtsdatum ist erforderlich")
    @Past(message = "Geburtsdatum muss in der Vergangenheit liegen")
    private LocalDate geburtsdatum;

    @NotBlank(message = "Versicherungsnummer ist erforderlich")
    @Size(min = 5, max = 30, message = "Versicherungsnummer muss zwischen 5 und 30 Zeichen lang sein")
    private String versicherungsnr;

    @Pattern(regexp = "^[+0-9\\s()\\-]{0,20}$", message = "Telefonnummer ist ungültig")
    private String telefon;

    @Email(message = "E-Mail-Adresse ist ungültig")
    @Size(max = 150, message = "E-Mail darf maximal 150 Zeichen haben")
    private String email;

    @Size(max = 250, message = "Adresse darf maximal 250 Zeichen haben")
    private String adresse;

    @ManyToOne
    @JoinColumn(name = "klinikum_id")
    private Klinikum klinikum;

    @Size(max = 50, message = "Etage darf maximal 50 Zeichen haben")
    private String etage;

    @Size(max = 100, message = "Abteilung darf maximal 100 Zeichen haben")
    private String abteilung;

    @Size(max = 100, message = "Station darf maximal 100 Zeichen haben")
    private String station;

    @Size(max = 20, message = "Zimmer darf maximal 20 Zeichen haben")
    private String zimmer;

    @Size(max = 20, message = "Bett darf maximal 20 Zeichen haben")
    private String bett;

    @Enumerated(EnumType.STRING)
    private PatientStatus status;

    private LocalDate aufnahmeDatum;

    private boolean eigenanteilBezahlt = false;

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

    public LocalDate getAufnahmeDatum() { return aufnahmeDatum; }
    public void setAufnahmeDatum(LocalDate aufnahmeDatum) { this.aufnahmeDatum = aufnahmeDatum; }

    public boolean isEigenanteilBezahlt() { return eigenanteilBezahlt; }
    public void setEigenanteilBezahlt(boolean eigenanteilBezahlt) { this.eigenanteilBezahlt = eigenanteilBezahlt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient that = (Patient) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return id != null ? id.hashCode() : 0; }

    @Override
    public String toString() {
        return "Patient{id=" + id + ", vorname=" + vorname + ", nachname=" + nachname
                + ", versicherungsnr=" + versicherungsnr + ", status=" + status + "}";
    }
}
