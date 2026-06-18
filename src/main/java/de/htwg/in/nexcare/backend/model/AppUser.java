package de.htwg.in.nexcare.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name ist erforderlich")
    @Size(max = 150, message = "Name darf maximal 150 Zeichen haben")
    private String name;

    @Email(message = "E-Mail-Adresse ist ungültig")
    @NotBlank(message = "E-Mail ist erforderlich")
    @Size(max = 150, message = "E-Mail darf maximal 150 Zeichen haben")
    private String email;

    @Size(max = 250, message = "Adresse darf maximal 250 Zeichen haben")
    private String adresse;

    private String oauthId;

    @Enumerated(EnumType.STRING)
    private Role role;

    /** Links a PATIENT user to their Patient record. Null for staff. */
    private Long patientId;

    /** Optional contact email for notifications (separate from login email). */
    @Email(message = "Kontakt-E-Mail ist ungültig")
    @Size(max = 150, message = "Kontakt-E-Mail darf maximal 150 Zeichen haben")
    private String kontaktEmail;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getKontaktEmail() { return kontaktEmail; }
    public void setKontaktEmail(String kontaktEmail) { this.kontaktEmail = kontaktEmail; }
}
