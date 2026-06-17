package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Bett {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String bezeichnung;

    @Enumerated(EnumType.STRING)
    private BettStatus status = BettStatus.FREI;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zimmer_id")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Zimmer zimmer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = true)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private Patient patient;

    public Bett() {}

    public Bett(String bezeichnung, Zimmer zimmer) {
        this.bezeichnung = bezeichnung;
        this.zimmer = zimmer;
        this.status = BettStatus.FREI;
    }

    public Long getId() { return id; }
    public String getBezeichnung() { return bezeichnung; }
    public void setBezeichnung(String bezeichnung) { this.bezeichnung = bezeichnung; }
    public BettStatus getStatus() { return status; }
    public void setStatus(BettStatus status) { this.status = status; }
    public Zimmer getZimmer() { return zimmer; }
    public void setZimmer(Zimmer zimmer) { this.zimmer = zimmer; }
    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }
}
