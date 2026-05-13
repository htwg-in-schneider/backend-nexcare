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

### Iteration 1c: REST-Controller with model class

- Model-Klassen in `model/`:
  - `Patient` (POJO, alle Felder aus dem Mockup: id, vorname, nachname, geburtsdatum, versicherungsnr, telefon, email, adresse, klinikum, etage, abteilung, station, zimmer, bett, status, notfallkontakt).
  - `PatientStatus` (Enum: `STATIONAER`, `AMBULANT`).
  - `NotfallKontakt` (POJO mit name, beziehung, telefon — eingebettet in Patient).
- `PatientController` liefert jetzt 5 voll befüllte `Patient`-Beispiele, die vom Frontend konsumiert werden können.
- POST nimmt ein vollständiges `Patient`-Objekt entgegen.

### Iteration 2: CORS Configuration

- `config/WebConfig` mit globalem CORS-Mapping (`addCorsMappings`) — erlaubt alle Origins/Methoden/Header.
- Damit kann das Vue-Frontend (typisch auf `http://localhost:5173`) das Backend (`:8081`) ohne CORS-Fehler ansprechen.
