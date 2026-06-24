package de.htwg.in.nexcare.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
public class PatientNachricht {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotBlank(message = "Titel ist erforderlich")
    @Size(max = 200, message = "Titel darf maximal 200 Zeichen haben")
    @Column(nullable = false)
    private String titel;

    @NotBlank(message = "Inhalt ist erforderlich")
    @Size(max = 2000, message = "Inhalt darf maximal 2000 Zeichen haben")
    @Column(nullable = false, length = 2000)
    private String inhalt;

    @NotBlank(message = "Typ ist erforderlich")
    @Size(max = 50, message = "Typ darf maximal 50 Zeichen haben")
    @Column(nullable = false)
    private String typ; // WILLKOMMEN, AUFNAHME, BETT, MEDIKAMENT, ALLGEMEIN

    @NotNull(message = "Erstellungsdatum ist erforderlich")
    @Column(nullable = false)
    private LocalDateTime erstelltAm;

    private boolean gelesen;

    public PatientNachricht() {}

    public PatientNachricht(Patient patient, String titel, String inhalt, String typ) {
        this.patient = patient;
        this.titel = titel;
        this.inhalt = inhalt;
        this.typ = typ;
        this.erstelltAm = LocalDateTime.now();
        this.gelesen = false;
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public String getTitel() { return titel; }
    public String getInhalt() { return inhalt; }
    public String getTyp() { return typ; }
    public LocalDateTime getErstelltAm() { return erstelltAm; }
    public boolean isGelesen() { return gelesen; }
    public void setGelesen(boolean gelesen) { this.gelesen = gelesen; }
}
