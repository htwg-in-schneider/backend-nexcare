package de.htwg.in.nexcare.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Klinikum {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name ist erforderlich")
    private String name;

    @NotBlank(message = "Ort ist erforderlich")
    private String ort;

    @OneToMany(mappedBy = "klinikum")
    @JsonIgnore
    private List<Patient> patienten = new ArrayList<>();

    public Klinikum() {
    }

    public Klinikum(String name, String ort) {
        this.name = name;
        this.ort = ort;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrt() { return ort; }
    public void setOrt(String ort) { this.ort = ort; }

    public List<Patient> getPatienten() { return patienten; }
    public void setPatienten(List<Patient> patienten) { this.patienten = patienten; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Klinikum that = (Klinikum) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Klinikum{id=" + id + ", name=" + name + ", ort=" + ort + "}";
    }
}
