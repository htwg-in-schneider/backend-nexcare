package de.htwg.in.nexcare.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.htwg.in.nexcare.backend.model.NotfallKontakt;
import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.model.PatientStatus;
import de.htwg.in.nexcare.backend.repository.PatientRepository;

import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class DataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    public CommandLineRunner loadPatients(PatientRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                LOGGER.info("Database is empty. Loading initial patients...");
                loadInitialData(repository);
            } else {
                LOGGER.info("Database already contains patients. Skipping data loading.");
            }
        };
    }

    private void loadInitialData(PatientRepository repository) {
        Patient maria = build("Maria", "Schmidt", LocalDate.of(1985, 3, 15), "V-2024-001",
                "+49 170 1234567", "m.schmidt@email.de", "Musterstr. 12, 78462 Konstanz",
                "Klinikum Konstanz", "3. OG", "Kardiologie", "Station K3", "304", "Bett A",
                PatientStatus.STATIONAER,
                new NotfallKontakt("Hans Schmidt", "Ehepartner", "+49 170 7654321"));

        Patient thomas = build("Thomas", "Weber", LocalDate.of(1972, 7, 22), "V-2024-002",
                "+49 171 2345678", "t.weber@email.de", "Seestr. 8, 78464 Konstanz",
                "Klinikum Konstanz", "2. OG", "Innere Medizin", "Station I2", "212", "Bett B",
                PatientStatus.STATIONAER,
                new NotfallKontakt("Sabine Weber", "Ehepartnerin", "+49 171 8765432"));

        Patient anna = build("Anna", "Hoffmann", LocalDate.of(1990, 11, 3), "V-2024-003",
                "+49 172 3456789", "a.hoffmann@email.de", "Bahnhofstr. 22, 78462 Konstanz",
                "Klinikum Konstanz", "EG", "Ambulanz", "–", "–", "–",
                PatientStatus.AMBULANT,
                new NotfallKontakt("Klaus Hoffmann", "Vater", "+49 172 9876543"));

        Patient peter = build("Peter", "Braun", LocalDate.of(1965, 5, 8), "V-2024-004",
                "+49 173 4567890", "p.braun@email.de", "Rheinsteig 5, 78462 Konstanz",
                "Klinikum Konstanz", "4. OG", "Chirurgie", "Station C4", "418", "Bett A",
                PatientStatus.STATIONAER,
                new NotfallKontakt("Maria Braun", "Tochter", "+49 173 0987654"));

        Patient lisa = build("Lisa", "Krause", LocalDate.of(1998, 9, 17), "V-2024-005",
                "+49 174 5678901", "l.krause@email.de", "Marktplatz 3, 78462 Konstanz",
                "Klinikum Konstanz", "EG", "Ambulanz", "–", "–", "–",
                PatientStatus.AMBULANT,
                new NotfallKontakt("Julia Krause", "Schwester", "+49 174 1098765"));

        repository.saveAll(Arrays.asList(maria, thomas, anna, peter, lisa));
        LOGGER.info("Initial patients loaded successfully.");
    }

    private static Patient build(String vorname, String nachname, LocalDate geb, String vNr,
                                 String tel, String email, String adresse, String klinikum,
                                 String etage, String abteilung, String station, String zimmer,
                                 String bett, PatientStatus status, NotfallKontakt notfall) {
        Patient p = new Patient();
        p.setVorname(vorname);
        p.setNachname(nachname);
        p.setGeburtsdatum(geb);
        p.setVersicherungsnr(vNr);
        p.setTelefon(tel);
        p.setEmail(email);
        p.setAdresse(adresse);
        p.setKlinikum(klinikum);
        p.setEtage(etage);
        p.setAbteilung(abteilung);
        p.setStation(station);
        p.setZimmer(zimmer);
        p.setBett(bett);
        p.setStatus(status);
        p.setNotfallkontakt(notfall);
        return p;
    }
}
