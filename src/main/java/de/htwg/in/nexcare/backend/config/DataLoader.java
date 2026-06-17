package de.htwg.in.nexcare.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.htwg.in.nexcare.backend.model.AppUser;
import de.htwg.in.nexcare.backend.model.Klinikum;
import de.htwg.in.nexcare.backend.model.NotfallKontakt;
import de.htwg.in.nexcare.backend.model.Patient;
import de.htwg.in.nexcare.backend.model.PatientStatus;
import de.htwg.in.nexcare.backend.model.Role;
import de.htwg.in.nexcare.backend.repository.AppUserRepository;
import de.htwg.in.nexcare.backend.repository.KlinikumRepository;
import de.htwg.in.nexcare.backend.repository.PatientRepository;

import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class DataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    public CommandLineRunner loadData(AppUserRepository userRepository,
                                      KlinikumRepository klinikumRepository,
                                      PatientRepository patientRepository) {
        return args -> {
            loadInitialUsers(userRepository);
            if (klinikumRepository.count() == 0) {
                LOGGER.info("Database is empty. Loading initial data...");
                loadInitialData(klinikumRepository, patientRepository);
            } else {
                LOGGER.info("Database already contains data. Skipping data loading.");
            }
        };
    }

    private void loadInitialUsers(AppUserRepository userRepository) {
        // Auth0 IDs are set via env variables; defaults allow local dev without auth
        upsertUser(userRepository, "Anna Patient", "patient@nexcare.de",
                System.getenv().getOrDefault("NEXCARE_PATIENT_OAUTH_ID", "auth0|nexcare-patient"), Role.PATIENT);
        upsertUser(userRepository, "Sandra Pflege", "pflege@nexcare.de",
                System.getenv().getOrDefault("NEXCARE_NURSE_OAUTH_ID", "auth0|nexcare-nurse"), Role.KRANKENSCHWESTER);
        upsertUser(userRepository, "Dr. Max Arzt", "arzt@nexcare.de",
                System.getenv().getOrDefault("NEXCARE_ARZT_OAUTH_ID", "auth0|nexcare-arzt"), Role.ARZT);
        upsertUser(userRepository, "Admin NexCare", "admin@nexcare.de",
                System.getenv().getOrDefault("NEXCARE_ADMIN_OAUTH_ID", "auth0|nexcare-admin"), Role.ADMIN);
    }

    private void upsertUser(AppUserRepository userRepository, String name, String email,
                             String oauthId, Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(existing -> {
            existing.setName(name);
            existing.setOauthId(oauthId);
            existing.setRole(role);
            userRepository.save(existing);
            LOGGER.info("Updated {} user: {}", role, email);
        }, () -> {
            AppUser u = new AppUser();
            u.setName(name);
            u.setEmail(email);
            u.setOauthId(oauthId);
            u.setRole(role);
            userRepository.save(u);
            LOGGER.info("Created {} user: {}", role, email);
        });
    }

    private void loadInitialData(KlinikumRepository klinikumRepository,
                                 PatientRepository patientRepository) {
        Klinikum konstanz = klinikumRepository.save(new Klinikum("Klinikum Konstanz", "Konstanz"));
        Klinikum singen   = klinikumRepository.save(new Klinikum("Klinikum Singen", "Singen"));
        klinikumRepository.save(new Klinikum("Universitätsklinikum Freiburg", "Freiburg"));

        Patient maria = build("Maria", "Schmidt", LocalDate.of(1985, 3, 15), "V-2024-001",
                "+49 170 1234567", "m.schmidt@email.de", "Musterstr. 12, 78462 Konstanz",
                konstanz, "3. OG", "Kardiologie", "Station K3", "304", "Bett A",
                PatientStatus.STATIONAER,
                new NotfallKontakt("Hans Schmidt", "Ehepartner", "+49 170 7654321"));

        Patient thomas = build("Thomas", "Weber", LocalDate.of(1972, 7, 22), "V-2024-002",
                "+49 171 2345678", "t.weber@email.de", "Seestr. 8, 78464 Konstanz",
                konstanz, "2. OG", "Innere Medizin", "Station I2", "212", "Bett B",
                PatientStatus.STATIONAER,
                new NotfallKontakt("Sabine Weber", "Ehepartnerin", "+49 171 8765432"));

        Patient anna = build("Anna", "Hoffmann", LocalDate.of(1990, 11, 3), "V-2024-003",
                "+49 172 3456789", "a.hoffmann@email.de", "Bahnhofstr. 22, 78462 Konstanz",
                konstanz, "EG", "Ambulanz", "–", "–", "–",
                PatientStatus.AMBULANT,
                new NotfallKontakt("Klaus Hoffmann", "Vater", "+49 172 9876543"));

        Patient peter = build("Peter", "Braun", LocalDate.of(1965, 5, 8), "V-2024-004",
                "+49 173 4567890", "p.braun@email.de", "Rheinsteig 5, 78462 Konstanz",
                konstanz, "4. OG", "Chirurgie", "Station C4", "418", "Bett A",
                PatientStatus.STATIONAER,
                new NotfallKontakt("Maria Braun", "Tochter", "+49 173 0987654"));

        Patient lisa = build("Lisa", "Krause", LocalDate.of(1998, 9, 17), "V-2024-005",
                "+49 174 5678901", "l.krause@email.de", "Marktplatz 3, 78462 Konstanz",
                singen, "EG", "Ambulanz", "–", "–", "–",
                PatientStatus.AMBULANT,
                new NotfallKontakt("Julia Krause", "Schwester", "+49 174 1098765"));

        patientRepository.saveAll(Arrays.asList(maria, thomas, anna, peter, lisa));
        LOGGER.info("Initial Klinika ({}) and patients ({}) loaded successfully.",
                klinikumRepository.count(), patientRepository.count());
    }

    private static Patient build(String vorname, String nachname, LocalDate geb, String vNr,
                                 String tel, String email, String adresse, Klinikum klinikum,
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
