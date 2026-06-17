package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
public class MedikamentenEintrag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medikament_id", nullable = false)
    private Medikament medikament;

    @NotBlank(message = "Dosierung ist erforderlich")
    private String dosierung;

    @NotBlank(message = "Einnahme ist erforderlich")
    private String einnahme;

    @NotNull(message = "Verschreibungsdatum ist erforderlich")
    private LocalDate verschreibungsDatum;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Medikament getMedikament() { return medikament; }
    public void setMedikament(Medikament medikament) { this.medikament = medikament; }

    public String getDosierung() { return dosierung; }
    public void setDosierung(String dosierung) { this.dosierung = dosierung; }

    public String getEinnahme() { return einnahme; }
    public void setEinnahme(String einnahme) { this.einnahme = einnahme; }

    public LocalDate getVerschreibungsDatum() { return verschreibungsDatum; }
    public void setVerschreibungsDatum(LocalDate verschreibungsDatum) { this.verschreibungsDatum = verschreibungsDatum; }
}
