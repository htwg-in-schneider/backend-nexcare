# NexCare – Patientenverwaltung (Backend)

Spring Boot 3.5 + Java 21 + Auth0 JWT + JPA/Hibernate

## Lokal starten

### Mit H2 (Standard)

```sh
mvn spring-boot:run
```

Startet auf `http://localhost:8081` mit H2-File-DB unter `./target/nexcare-db`. Beim ersten Start werden automatisch Demo-Daten geladen:

- 3 Klinika (Konstanz, Singen, Freiburg)
- 5 Patienten (inkl. Bett-/Zimmer-/Etagen-Struktur)
- 200+ Medikamente im Katalog
- 4 Demo-Benutzer (Arzt, Pflegekraft, Patient, Admin)

### Mit MariaDB (HTWG)

```sh
SPRING_DATASOURCE_PASSWORD=<pwd> mvn spring-boot:run -Dspring-boot.run.profiles=test
SPRING_DATASOURCE_PASSWORD=<pwd> mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Connection-Details in `application-test.properties` bzw. `application-prod.properties`.

## API-Endpunkte

| Methode | Pfad | Beschreibung | Berechtigung |
|---------|------|-------------|-------------|
| GET | `/api/patient` | Patientenliste (Filter: `name`, `status`, `klinikum`) | Staff |
| GET | `/api/patient/{id}` | Patient-Details | Staff oder eigener Patient |
| POST | `/api/patient` | Patient anlegen | Staff |
| PUT | `/api/patient/{id}` | Patient aktualisieren | Staff |
| PATCH | `/api/patient/{id}/entlassen` | Patient entlassen (Bett wird automatisch freigegeben) | Staff |
| DELETE | `/api/patient/{id}` | Patient löschen | Admin |
| GET | `/api/medikament` | Medikamentenkatalog (Filter: `suche`) | Alle |
| GET/POST/DELETE | `/api/patient/{id}/medikamentenplan` | Medikamentenplan verwalten | Staff (schreiben), Staff+Patient (lesen) |
| GET | `/api/betten/struktur?klinikumId=` | Klinikum-Struktur mit Betten | Staff |
| PUT | `/api/betten/bett/{id}/assign/{patientId}` | Bett zuweisen (`@Transactional`) | Staff |
| PUT | `/api/betten/bett/{id}/release` | Bett freigeben | Staff |
| GET | `/api/klinikum` | Klinika-Liste | Alle |
| GET | `/api/profile/me` | Eigenes Benutzerprofil | Authentifiziert |
| GET | `/api/patient/{id}/nachrichten` | In-App-Nachrichten eines Patienten | Staff oder eigener Patient |

## Architektur

- **Auth**: Auth0 JWT via `okta-spring-boot-starter` — jeder Request wird gegen den Auth0-Issuer validiert
- **Rollen**: `PATIENT`, `KRANKENSCHWESTER`, `ARZT`, `ADMIN` — in `AppUser.role` gespeichert
- **SecurityService**: zentrale Berechtigungsprüfung (`isStaff`, `isAdmin`, `isStaffOrPatient`)
- **GlobalExceptionHandler**: fängt alle Exceptions ab und gibt strukturierte JSON-Fehler zurück
- **DataLoader**: idempotenter `CommandLineRunner` für Demo-Daten
