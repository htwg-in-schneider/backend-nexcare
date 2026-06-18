package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AufnahmeAntrag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** OAuth subject of the submitting user. */
    private String oauthId;

    @Size(max = 150)
    private String patientName;

    @Size(max = 150)
    private String patientEmail;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "klinikum_id")
    private Klinikum klinikum;

    @Size(max = 100)
    private String abteilung;

    @Size(max = 100)
    private String station;

    @Size(max = 1000)
    private String nachricht;

    private LocalDateTime erstelltAm;

    @Enumerated(EnumType.STRING)
    private AntragStatus antragStatus = AntragStatus.OFFEN;

    /** Set when antrag is confirmed — links to the created Patient. */
    private Long patientId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientEmail() { return patientEmail; }
    public void setPatientEmail(String patientEmail) { this.patientEmail = patientEmail; }

    public Klinikum getKlinikum() { return klinikum; }
    public void setKlinikum(Klinikum klinikum) { this.klinikum = klinikum; }

    public String getAbteilung() { return abteilung; }
    public void setAbteilung(String abteilung) { this.abteilung = abteilung; }

    public String getStation() { return station; }
    public void setStation(String station) { this.station = station; }

    public String getNachricht() { return nachricht; }
    public void setNachricht(String nachricht) { this.nachricht = nachricht; }

    public LocalDateTime getErstelltAm() { return erstelltAm; }
    public void setErstelltAm(LocalDateTime erstelltAm) { this.erstelltAm = erstelltAm; }

    public AntragStatus getAntragStatus() { return antragStatus; }
    public void setAntragStatus(AntragStatus antragStatus) { this.antragStatus = antragStatus; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}
