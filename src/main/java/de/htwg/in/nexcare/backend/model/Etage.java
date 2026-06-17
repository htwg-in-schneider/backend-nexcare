package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Etage {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int nummer;
    private String bezeichnung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "klinikum_id")
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
