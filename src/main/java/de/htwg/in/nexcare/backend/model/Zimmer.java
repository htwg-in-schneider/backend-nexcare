package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Zimmer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Zimmernummer ist erforderlich")
    @Size(max = 20, message = "Zimmernummer darf maximal 20 Zeichen haben")
    private String nummer;

    @Size(max = 100, message = "Abteilung darf maximal 100 Zeichen haben")
    private String abteilung;

    @Size(max = 100, message = "Station darf maximal 100 Zeichen haben")
    private String station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etage_id")
    @NotNull(message = "Etage ist erforderlich")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Etage etage;

    public Zimmer() {}

    public Zimmer(String nummer, String abteilung, String station, Etage etage) {
        this.nummer = nummer;
        this.abteilung = abteilung;
        this.station = station;
        this.etage = etage;
    }

    public Long getId() { return id; }
    public String getNummer() { return nummer; }
    public void setNummer(String nummer) { this.nummer = nummer; }
    public String getAbteilung() { return abteilung; }
    public void setAbteilung(String abteilung) { this.abteilung = abteilung; }
    public String getStation() { return station; }
    public void setStation(String station) { this.station = station; }
    public Etage getEtage() { return etage; }
    public void setEtage(Etage etage) { this.etage = etage; }
}
