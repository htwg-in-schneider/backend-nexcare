# nexcare-backend

Backend für die Nexcare-Patientenverwaltung (Spring Boot 3.5 + Java 21).

## How to Run

### Lokal mit H2 (default)
```sh
mvn spring-boot:run
```
Die Anwendung startet auf `http://localhost:8081` und nutzt die H2-File-DB unter `./target/nexcare-db`.

### Gegen die HTWG-MariaDB (Test- oder Produktions-DB)
Server und Zugangsdaten kommen aus dem Aufgaben-Kommentar. Aus dem HSKO-Netz (Eduroam) oder per VPN erreichbar; Port 3307.
```sh
SPRING_DATASOURCE_PASSWORD=<pwd> mvn spring-boot:run -Dspring-boot.run.profiles=test
SPRING_DATASOURCE_PASSWORD=<pwd> mvn spring-boot:run -Dspring-boot.run.profiles=prod
```
Connection-Details siehe `src/main/resources/application-test.properties` bzw. `application-prod.properties`. Passwort kommt **immer** aus der Env-Variable `SPRING_DATASOURCE_PASSWORD` (nicht im Code).

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

### Iteration 3: Database Integration

1. **DB-Konfiguration**:
   - H2 (file-based) und MariaDB Runtime-Dependencies in `pom.xml`.
   - `application.properties` mit H2-Konfiguration (`jdbc:h2:file:./target/nexcare-db`) und kommentiertem MariaDB-Block für Produktion.
   - `spring.jpa.hibernate.ddl-auto=update`.
   - Die in Iter 1a gesetzte DataSource-Autoconfig-Exclusion wurde entfernt, da jetzt eine DB benötigt wird.
2. **JPA-Entities**:
   - `Patient` mit `@Entity`, `@Id @GeneratedValue`, `@Enumerated(EnumType.STRING)` für `status`, `@Embedded` für `notfallkontakt`. Plus `equals`/`hashCode` auf Basis von `id`.
   - `NotfallKontakt` mit `@Embeddable` — die Felder werden in dieselbe Tabelle wie `Patient` geschrieben.
3. **Repository**:
   - `PatientRepository extends JpaRepository<Patient, Long>`.
4. **Data Loader**:
   - `config/DataLoader` als `CommandLineRunner` — lädt 5 Beispielpatienten beim Startup, falls die DB leer ist.
5. **Controller**:
   - `PatientController` verwendet jetzt das Repository für `findAll()`. POST ist weiter nur ein Stub — echtes CRUD kommt in Iteration 4.
