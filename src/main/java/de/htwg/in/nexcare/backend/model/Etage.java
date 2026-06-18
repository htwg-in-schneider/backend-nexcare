package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Etage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Min(value = -5, message = "Stockwerk muss mindestens -5 sein")
    @Max(value = 100, message = "Stockwerk darf höchstens 100 sein")
    private int nummer;

    @NotBlank(message = "Bezeichnung ist erforderlich")
    @Size(max = 100, message = "Bezeichnung darf maximal 100 Zeichen haben")
    private String bezeichnung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klinikum_id")
    @NotNull(message = "Klinikum ist erforderlich")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Klinikum klinikum;

    public Etage() {}

    public Etage(int nummer, String bezeichnung, Klinikum klinikum) {
        this.nummer = nummer;
        this.bezeichnung = bezeichnung;
        this.klinikum = klinikum;
    }

    public Long getId() { return id; }
    public int getNummer() { return nummer; }
    public void setNummer(int nummer) { this.nummer = nummer; }
    public String getBezeichnung() { return bezeichnung; }
    public void setBezeichnung(String bezeichnung) { this.bezeichnung = bezeichnung; }
    public Klinikum getKlinikum() { return klinikum; }
    public void setKlinikum(Klinikum klinikum) { this.klinikum = klinikum; }
}
