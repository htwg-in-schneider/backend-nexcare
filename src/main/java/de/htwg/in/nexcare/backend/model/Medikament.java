package de.htwg.in.nexcare.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Medikament {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name ist erforderlich")
    private String name;

    @NotBlank(message = "Wirkstoff ist erforderlich")
    private String wirkstoff;

    private String beschreibung;

    private String dosiereinheit;

    private boolean archiviert = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getWirkstoff() { return wirkstoff; }
    public void setWirkstoff(String wirkstoff) { this.wirkstoff = wirkstoff; }

    public String getBeschreibung() { return beschreibung; }
    public void setBeschreibung(String beschreibung) { this.beschreibung = beschreibung; }

    public String getDosiereinheit() { return dosiereinheit; }
    public void setDosiereinheit(String dosiereinheit) { this.dosiereinheit = dosiereinheit; }

    public boolean isArchiviert() { return archiviert; }
    public void setArchiviert(boolean archiviert) { this.archiviert = archiviert; }
}
