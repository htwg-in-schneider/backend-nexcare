package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;

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
    @Size(max = 100, message = "Dosierung darf maximal 100 Zeichen haben")
    private String dosierung;

    /** Comma-separated weekday codes: MO,DI,MI,DO,FR,SA,SO */
    @NotBlank(message = "Wochentage sind erforderlich")
    @Pattern(regexp = "^(MO|DI|MI|DO|FR|SA|SO)(,(MO|DI|MI|DO|FR|SA|SO))*$",
             message = "Wochentage müssen kommaseparierte Kürzel sein (MO,DI,MI,DO,FR,SA,SO)")
    private String wochentage;

    /** Comma-separated times in HH:mm format: 08:00,14:00,20:00 */
    @NotBlank(message = "Uhrzeiten sind erforderlich")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d(,([01]\\d|2[0-3]):[0-5]\\d)*$",
             message = "Uhrzeiten müssen im Format HH:mm sein, kommasepariert")
    private String uhrzeiten;

    @NotNull(message = "Startdatum ist erforderlich")
    private LocalDate startDatum;

    @NotNull(message = "Enddatum ist erforderlich")
    private LocalDate endDatum;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Medikament getMedikament() { return medikament; }
    public void setMedikament(Medikament medikament) { this.medikament = medikament; }

    public String getDosierung() { return dosierung; }
    public void setDosierung(String dosierung) { this.dosierung = dosierung; }

    public String getWochentage() { return wochentage; }
    public void setWochentage(String wochentage) { this.wochentage = wochentage; }

    public String getUhrzeiten() { return uhrzeiten; }
    public void setUhrzeiten(String uhrzeiten) { this.uhrzeiten = uhrzeiten; }

    public LocalDate getStartDatum() { return startDatum; }
    public void setStartDatum(LocalDate startDatum) { this.startDatum = startDatum; }

    public LocalDate getEndDatum() { return endDatum; }
    public void setEndDatum(LocalDate endDatum) { this.endDatum = endDatum; }
}
