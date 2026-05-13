# nexcare-backend

Backend für die Nexcare-Patientenverwaltung (Spring Boot 3.5 + Java 21).

## How to Run

```sh
mvn spring-boot:run
```

Die Anwendung startet auf `http://localhost:8081`.

## Iterations

### Iteration 1a: First REST Controller

- Basic Projekt-Konfiguration in `application.properties` (Port 8081, hübsche JSON-Ausgabe, DataSource-Autoconfig deaktiviert solange keine DB benötigt wird).
- `PatientController` mit GET `/api/patient`, der eine Liste von Strings (Patientennamen) zurückgibt.
- Test mit `curl http://localhost:8081/api/patient`.

### Iteration 1b: JSON (de)serialization

- `PatientController` unterstützt jetzt GET und POST auf `/api/patient`.
- Verwendung von Java-Objekten (innere POJO-Klasse `Patient` mit `vorname`/`nachname`) statt Strings.
- Test GET: `curl http://localhost:8081/api/patient`
- Test POST: `curl -X POST http://localhost:8081/api/patient -H 'Content-Type: application/json' -d '{"vorname":"Maxi","nachname":"Muster"}'`
