package de.htwg.in.nexcare.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.htwg.in.nexcare.backend.model.*;
import de.htwg.in.nexcare.backend.repository.*;

import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class DataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    public CommandLineRunner loadData(AppUserRepository userRepository,
                                      KlinikumRepository klinikumRepository,
                                      PatientRepository patientRepository,
                                      MedikamentRepository medikamentRepository,
                                      EtageRepository etageRepository,
                                      ZimmerRepository zimmerRepository,
                                      BettRepository bettRepository) {
        return args -> {
            loadInitialUsers(userRepository);
            if (klinikumRepository.count() == 0) {
                LOGGER.info("Database is empty. Loading initial data...");
                Klinikum[] klinika = loadInitialData(klinikumRepository, patientRepository);
                loadInitialBettStruktur(klinika[0], patientRepository, etageRepository, zimmerRepository, bettRepository);
            } else {
                LOGGER.info("Database already contains data. Skipping data loading.");
            }
            if (medikamentRepository.count() == 0) {
                loadInitialMedikamente(medikamentRepository);
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

    private Klinikum[] loadInitialData(KlinikumRepository klinikumRepository,
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
        return new Klinikum[]{ konstanz, singen };
    }

    private void loadInitialBettStruktur(Klinikum konstanz, PatientRepository patientRepo,
                                         EtageRepository etageRepo, ZimmerRepository zimmerRepo,
                                         BettRepository bettRepo) {
        // EG
        Etage eg = etageRepo.save(new Etage(0, "EG", konstanz));
        Zimmer z101 = zimmerRepo.save(new Zimmer("101", "Neurologie", "Station N1", eg));
        bettRepo.save(new Bett("Bett A", z101));
        bettRepo.save(new Bett("Bett B", z101));
        bettRepo.save(new Bett("Bett C", z101));
        Zimmer z102 = zimmerRepo.save(new Zimmer("102", "Neurologie", "Station N1", eg));
        bettRepo.save(new Bett("Bett A", z102));
        bettRepo.save(new Bett("Bett B", z102));

        // 2. OG – Thomas Weber in 212 Bett B
        Etage og2 = etageRepo.save(new Etage(2, "2. OG", konstanz));
        Zimmer z211 = zimmerRepo.save(new Zimmer("211", "Innere Medizin", "Station I2", og2));
        bettRepo.save(new Bett("Bett A", z211));
        bettRepo.save(new Bett("Bett B", z211));
        Zimmer z212 = zimmerRepo.save(new Zimmer("212", "Innere Medizin", "Station I2", og2));
        bettRepo.save(new Bett("Bett A", z212));
        Bett z212BettB = bettRepo.save(new Bett("Bett B", z212));
        bettRepo.save(new Bett("Bett C", z212));

        // Assign Thomas Weber to z212 Bett B
        patientRepo.findAll().stream()
            .filter(p -> "Thomas".equals(p.getVorname()) && "Weber".equals(p.getNachname()))
            .findFirst().ifPresent(thomas -> {
                z212BettB.setPatient(thomas);
                z212BettB.setStatus(BettStatus.BELEGT);
                bettRepo.save(z212BettB);
            });

        // 3. OG – Maria Schmidt in 304 Bett A
        Etage og3 = etageRepo.save(new Etage(3, "3. OG", konstanz));
        Zimmer z301 = zimmerRepo.save(new Zimmer("301", "Kardiologie", "Station K3", og3));
        bettRepo.save(new Bett("Bett A", z301));
        bettRepo.save(new Bett("Bett B", z301));
        bettRepo.save(new Bett("Bett C", z301));
        Zimmer z304 = zimmerRepo.save(new Zimmer("304", "Kardiologie", "Station K3", og3));
        Bett z304BettA = bettRepo.save(new Bett("Bett A", z304));
        bettRepo.save(new Bett("Bett B", z304));

        patientRepo.findAll().stream()
            .filter(p -> "Maria".equals(p.getVorname()) && "Schmidt".equals(p.getNachname()))
            .findFirst().ifPresent(maria -> {
                z304BettA.setPatient(maria);
                z304BettA.setStatus(BettStatus.BELEGT);
                bettRepo.save(z304BettA);
            });

        // 4. OG – Peter Braun in 418 Bett A
        Etage og4 = etageRepo.save(new Etage(4, "4. OG", konstanz));
        Zimmer z418 = zimmerRepo.save(new Zimmer("418", "Chirurgie", "Station C4", og4));
        Bett z418BettA = bettRepo.save(new Bett("Bett A", z418));
        bettRepo.save(new Bett("Bett B", z418));
        bettRepo.save(new Bett("Bett C", z418));
        Zimmer z419 = zimmerRepo.save(new Zimmer("419", "Chirurgie", "Station C4", og4));
        bettRepo.save(new Bett("Bett A", z419));
        bettRepo.save(new Bett("Bett B", z419));

        patientRepo.findAll().stream()
            .filter(p -> "Peter".equals(p.getVorname()) && "Braun".equals(p.getNachname()))
            .findFirst().ifPresent(peter -> {
                z418BettA.setPatient(peter);
                z418BettA.setStatus(BettStatus.BELEGT);
                bettRepo.save(z418BettA);
            });

        LOGGER.info("Initial Bettstruktur for Klinikum Konstanz loaded.");
    }

    private void loadInitialMedikamente(MedikamentRepository repo) {
        // Analgetika / Antipyretika
        repo.save(medikament("Ibuprofen 400mg", "Ibuprofen", "NSAR – Entzündungshemmer, Schmerzmittel und Fiebermittel", "mg"));
        repo.save(medikament("Ibuprofen 600mg", "Ibuprofen", "NSAR – höhere Dosierung bei starken Schmerzen", "mg"));
        repo.save(medikament("Paracetamol 500mg", "Paracetamol", "Fiebersenkend und schmerzlindernd, leberschonende Anwendung", "mg"));
        repo.save(medikament("Paracetamol 1000mg", "Paracetamol", "Höhere Dosierung bei starken Schmerzen oder Fieber", "mg"));
        repo.save(medikament("Diclofenac 50mg", "Diclofenac", "NSAR bei Gelenk- und Muskelschmerzen", "mg"));
        repo.save(medikament("Diclofenac Gel 1%", "Diclofenac", "Topische Anwendung bei lokalen Schmerzen", "g"));
        repo.save(medikament("Metamizol 500mg", "Metamizol", "Starkes Analgetikum und Antipyretikum", "mg"));
        repo.save(medikament("Tramadol 50mg", "Tramadol", "Opioidanalgetikum bei mittleren bis starken Schmerzen", "mg"));
        repo.save(medikament("Codein 30mg", "Codein", "Schwaches Opioid, auch antitussiv", "mg"));
        repo.save(medikament("Morphin 10mg", "Morphin", "Starkes Opioid bei schweren Schmerzen", "mg"));
        repo.save(medikament("Oxycodon 5mg", "Oxycodon", "Retardiertes starkes Opioid", "mg"));
        repo.save(medikament("Celecoxib 200mg", "Celecoxib", "COX-2-selektiver Hemmer, magenfreundliches NSAR", "mg"));

        // Antibiotika
        repo.save(medikament("Amoxicillin 500mg", "Amoxicillin", "Breitspektrum-Penicillin-Antibiotikum", "mg"));
        repo.save(medikament("Amoxicillin 875mg / Clavulansäure", "Amoxicillin+Clavulansäure", "Breitspektrum-Antibiotikum mit Betalaktamase-Inhibitor", "mg"));
        repo.save(medikament("Azithromycin 500mg", "Azithromycin", "Makrolid-Antibiotikum bei Atemwegsinfektionen", "mg"));
        repo.save(medikament("Clarithromycin 500mg", "Clarithromycin", "Makrolid bei atypischen Erregern", "mg"));
        repo.save(medikament("Ciprofloxacin 500mg", "Ciprofloxacin", "Fluorchinolon-Antibiotikum, besonders bei Harnwegsinfekten", "mg"));
        repo.save(medikament("Doxycyclin 100mg", "Doxycyclin", "Tetracyclin-Antibiotikum bei Atemwegs- und Hautinfektionen", "mg"));
        repo.save(medikament("Metronidazol 400mg", "Metronidazol", "Antibiotikum und Antiprotozoikum bei anaeroben Infektionen", "mg"));
        repo.save(medikament("Cefuroxim 500mg", "Cefuroxim", "Zweitgenerations-Cephalosporin", "mg"));
        repo.save(medikament("Ceftriaxon 1g", "Ceftriaxon", "Drittgenerations-Cephalosporin, i.v.-Anwendung", "g"));
        repo.save(medikament("Vancomycin 500mg", "Vancomycin", "Glykopeptid-Antibiotikum bei MRSA", "mg"));
        repo.save(medikament("Meropenem 1g", "Meropenem", "Carbapenem-Reserveantibiotikum", "g"));
        repo.save(medikament("Levofloxacin 500mg", "Levofloxacin", "Fluorchinolon bei schweren Atemwegsinfektionen", "mg"));
        repo.save(medikament("Clindamycin 300mg", "Clindamycin", "Lincosamid-Antibiotikum bei Knochen- und Haut-Infektionen", "mg"));
        repo.save(medikament("Nitrofurantoin 100mg", "Nitrofurantoin", "Harnwegsantiseptikum bei unkomplizierter Zystitis", "mg"));
        repo.save(medikament("Trimethoprim 200mg", "Trimethoprim", "Antibiotikum bei Harnwegsinfektionen", "mg"));

        // Antihypertensiva / Kardiovaskulär
        repo.save(medikament("Ramipril 5mg", "Ramipril", "ACE-Hemmer bei Bluthochdruck und Herzinsuffizienz", "mg"));
        repo.save(medikament("Ramipril 10mg", "Ramipril", "ACE-Hemmer – höhere Dosierung", "mg"));
        repo.save(medikament("Lisinopril 10mg", "Lisinopril", "ACE-Hemmer, kardioprotektiv", "mg"));
        repo.save(medikament("Enalapril 5mg", "Enalapril", "ACE-Hemmer bei Hypertonie", "mg"));
        repo.save(medikament("Losartan 50mg", "Losartan", "AT1-Antagonist bei Bluthochdruck", "mg"));
        repo.save(medikament("Valsartan 80mg", "Valsartan", "Sartan-Gruppe, Antihypertensivum", "mg"));
        repo.save(medikament("Candesartan 8mg", "Candesartan", "AT1-Antagonist, auch bei Herzinsuffizienz", "mg"));
        repo.save(medikament("Amlodipin 5mg", "Amlodipin", "Kalziumantagonist bei Bluthochdruck und Angina", "mg"));
        repo.save(medikament("Nifedipin 10mg retard", "Nifedipin", "Kalziumantagonist, retardiert", "mg"));
        repo.save(medikament("Metoprolol 50mg", "Metoprolol", "Beta-1-Blocker bei Hypertonie und Herzinsuffizienz", "mg"));
        repo.save(medikament("Metoprolol 100mg", "Metoprolol", "Beta-1-Blocker – höhere Dosierung", "mg"));
        repo.save(medikament("Bisoprolol 5mg", "Bisoprolol", "Beta-1-Blocker bei Herzinsuffizienz", "mg"));
        repo.save(medikament("Bisoprolol 10mg", "Bisoprolol", "Beta-1-Blocker – höhere Dosierung", "mg"));
        repo.save(medikament("Carvedilol 12,5mg", "Carvedilol", "Alpha-Beta-Blocker bei Herzinsuffizienz", "mg"));
        repo.save(medikament("Hydrochlorothiazid 25mg", "Hydrochlorothiazid", "Thiazid-Diuretikum bei Hypertonie", "mg"));
        repo.save(medikament("Torasemid 10mg", "Torasemid", "Schleifendiuretikum bei Herzinsuffizienz und Ödemen", "mg"));
        repo.save(medikament("Furosemid 40mg", "Furosemid", "Schleifendiuretikum, schnell wirksam", "mg"));
        repo.save(medikament("Spironolacton 25mg", "Spironolacton", "Kaliumsparendes Diuretikum, Aldosteronantagonist", "mg"));
        repo.save(medikament("Doxazosin 2mg", "Doxazosin", "Alpha-Blocker bei Hypertonie und BPH", "mg"));
        repo.save(medikament("Clonidin 0,075mg", "Clonidin", "Zentrales Antihypertensivum", "mg"));

        // Antikoagulantia / Plättchenhemmer
        repo.save(medikament("Marcumar", "Phenprocoumon", "Vitamin-K-Antagonist zur Thromboseprophylaxe", "mg"));
        repo.save(medikament("Warfarin 5mg", "Warfarin", "Oraler Antikoagulans, Vitamin-K-Antagonist", "mg"));
        repo.save(medikament("Rivaroxaban 20mg", "Rivaroxaban", "DOAK – direkter Faktor-Xa-Hemmer", "mg"));
        repo.save(medikament("Apixaban 5mg", "Apixaban", "DOAK – Faktor-Xa-Hemmer bei Vorhofflimmern", "mg"));
        repo.save(medikament("Dabigatran 150mg", "Dabigatran", "DOAK – direkter Thrombinhemmer", "mg"));
        repo.save(medikament("Edoxaban 60mg", "Edoxaban", "DOAK – Faktor-Xa-Hemmer", "mg"));
        repo.save(medikament("Heparin 5000 IE/0,2 ml", "Heparin", "Unfraktioniertes Heparin, s.c.", "IE"));
        repo.save(medikament("Enoxaparin 40mg", "Enoxaparin", "Niedermolekulares Heparin, Thromboseprophylaxe", "mg"));
        repo.save(medikament("Fondaparinux 2,5mg", "Fondaparinux", "Synthetischer Faktor-Xa-Hemmer", "mg"));
        repo.save(medikament("ASS 100mg", "Acetylsalicylsäure", "Thrombozytenaggregationshemmer in Niedrigdosis", "mg"));
        repo.save(medikament("Clopidogrel 75mg", "Clopidogrel", "ADP-Rezeptorantagonist bei KHK und Schlaganfall", "mg"));
        repo.save(medikament("Ticagrelor 90mg", "Ticagrelor", "P2Y12-Inhibitor bei ACS", "mg"));
        repo.save(medikament("Prasugrel 10mg", "Prasugrel", "Thrombozytenaggregationshemmer bei PCI", "mg"));

        // Diabetes
        repo.save(medikament("Metformin 500mg", "Metformin", "Biguanid – Erstlinientherapie Typ-2-Diabetes", "mg"));
        repo.save(medikament("Metformin 850mg", "Metformin", "Biguanid – Standarddosierung", "mg"));
        repo.save(medikament("Metformin 1000mg", "Metformin", "Biguanid – höchste Tablettendosis", "mg"));
        repo.save(medikament("Glibenclamid 3,5mg", "Glibenclamid", "Sulfonylharnstoff – Insulinsekretion steigernd", "mg"));
        repo.save(medikament("Glimepirid 2mg", "Glimepirid", "Sulfonylharnstoff – moderne Generation", "mg"));
        repo.save(medikament("Sitagliptin 100mg", "Sitagliptin", "DPP-4-Hemmer bei Typ-2-Diabetes", "mg"));
        repo.save(medikament("Empagliflozin 10mg", "Empagliflozin", "SGLT-2-Hemmer, kardiovaskulär protektiv", "mg"));
        repo.save(medikament("Dapagliflozin 10mg", "Dapagliflozin", "SGLT-2-Hemmer, nephroprotektiv", "mg"));
        repo.save(medikament("Liraglutid 1,2mg/0,2 ml", "Liraglutid", "GLP-1-Agonist, gewichtsreduzierend", "mg"));
        repo.save(medikament("Semaglutid 0,5mg", "Semaglutid", "GLP-1-Agonist, s.c. wöchentlich", "mg"));
        repo.save(medikament("Insulin Glargin 100 IE/ml", "Insulin glargin", "Basalinsulin (Langzeitinsulin)", "IE"));
        repo.save(medikament("Insulin Aspart 100 IE/ml", "Insulin aspart", "Kurzwirksames Insulin (Bolus)", "IE"));
        repo.save(medikament("Insulin Human NPH", "Humaninsulin", "Intermediäres Insulin, zweimal täglich", "IE"));

        // Lipidsenker
        repo.save(medikament("Simvastatin 20mg", "Simvastatin", "HMG-CoA-Reduktasehemmer", "mg"));
        repo.save(medikament("Simvastatin 40mg", "Simvastatin", "HMG-CoA-Reduktasehemmer – höhere Dosis", "mg"));
        repo.save(medikament("Atorvastatin 10mg", "Atorvastatin", "Statin der ersten Wahl bei hohem CV-Risiko", "mg"));
        repo.save(medikament("Atorvastatin 40mg", "Atorvastatin", "Hochdosis-Statin-Therapie", "mg"));
        repo.save(medikament("Rosuvastatin 10mg", "Rosuvastatin", "Hochwirksames Statin", "mg"));
        repo.save(medikament("Pravastatin 20mg", "Pravastatin", "Statin – interaktionsärmer", "mg"));
        repo.save(medikament("Ezetimib 10mg", "Ezetimib", "Cholesterin-Resorptionshemmer, Kombinationstherapie", "mg"));
        repo.save(medikament("Fenofibrat 160mg", "Fenofibrat", "Fibrat bei Hypertriglyzeridämie", "mg"));

        // Protonenpumpenhemmer / Magenschutz
        repo.save(medikament("Omeprazol 20mg", "Omeprazol", "PPI bei Sodbrennen und Ulkus", "mg"));
        repo.save(medikament("Omeprazol 40mg", "Omeprazol", "PPI – höhere Dosis", "mg"));
        repo.save(medikament("Pantoprazol 40mg", "Pantoprazol", "PPI – Standardtherapie, i.v. verfügbar", "mg"));
        repo.save(medikament("Lansoprazol 30mg", "Lansoprazol", "PPI bei gastroösophagealem Reflux", "mg"));
        repo.save(medikament("Esomeprazol 20mg", "Esomeprazol", "Enantiomer-PPI, hochpotent", "mg"));
        repo.save(medikament("Ranitidin 150mg", "Ranitidin", "H2-Blocker – alternative Magensäurehemmung", "mg"));
        repo.save(medikament("Sucralfat 1g", "Sucralfat", "Schleimhautschutzmittel bei Magenulkus", "g"));
        repo.save(medikament("Metoclopramid 10mg", "Metoclopramid", "Prokinetikum bei Übelkeit und Gastroparese", "mg"));
        repo.save(medikament("Ondansetron 4mg", "Ondansetron", "5-HT3-Antagonist bei Chemotherapie-Übelkeit", "mg"));
        repo.save(medikament("Domperidon 10mg", "Domperidon", "Prokinetikum und Antiemetikum", "mg"));
        repo.save(medikament("Loperamid 2mg", "Loperamid", "Opioid-Antidiarrhoikum, nicht resorbiert", "mg"));
        repo.save(medikament("Bisacodyl 5mg", "Bisacodyl", "Stimulationslaxans bei Obstipation", "mg"));
        repo.save(medikament("Macrogol 3350", "Macrogol", "Osmotisches Laxans", "g"));
        repo.save(medikament("Lactulose Sirup", "Lactulose", "Osmotisches Laxans und Leber-Enzephalopathie-Therapie", "ml"));

        // Schilddrüse
        repo.save(medikament("Levothyroxin 50 µg", "Levothyroxin", "Schilddrüsenhormon bei Hypothyreose", "µg"));
        repo.save(medikament("Levothyroxin 75 µg", "Levothyroxin", "Schilddrüsenhormon – Standarddosierung", "µg"));
        repo.save(medikament("Levothyroxin 100 µg", "Levothyroxin", "Schilddrüsenhormon – höhere Dosis", "µg"));
        repo.save(medikament("Thiamazol 10mg", "Thiamazol", "Thyreostatikum bei Hyperthyreose", "mg"));
        repo.save(medikament("Carbimazol 20mg", "Carbimazol", "Thyreostatikum, Prodrug des Thiamazols", "mg"));
        repo.save(medikament("Kaliumjodid 100 µg", "Kaliumjodid", "Jodid-Supplementation und Strumaprävention", "µg"));

        // Asthma / COPD
        repo.save(medikament("Salbutamol Aerosol", "Salbutamol", "Beta-2-Agonist (SABA) – Bedarfsmedikament", "Hub"));
        repo.save(medikament("Formoterol 12 µg", "Formoterol", "Langwirksamer Beta-2-Agonist (LABA)", "µg"));
        repo.save(medikament("Salmeterol 50 µg", "Salmeterol", "LABA, kombiniert mit Kortikosteroid", "µg"));
        repo.save(medikament("Budesonid 200 µg", "Budesonid", "Inhalatives Kortikosteroid (ICS) bei Asthma", "µg"));
        repo.save(medikament("Fluticason 125 µg", "Fluticasonfuroat", "Potentes ICS bei Asthma und COPD", "µg"));
        repo.save(medikament("Tiotropium 18 µg", "Tiotropium", "Langwirksamer Muscarin-Antagonist (LAMA) bei COPD", "µg"));
        repo.save(medikament("Ipratropium 20 µg", "Ipratropium", "SAMA – kurzwirksamer Anticholinergiker", "µg"));
        repo.save(medikament("Roflumilast 500 µg", "Roflumilast", "PDE-4-Inhibitor bei schwerer COPD", "µg"));
        repo.save(medikament("Montelukast 10mg", "Montelukast", "Leukotrienantagonist bei Asthma allergischer Genese", "mg"));
        repo.save(medikament("Theophyllin 200mg retard", "Theophyllin", "Xanthin-Bronchodilatator, retardiert", "mg"));

        // Antiepileptika / Neurologie
        repo.save(medikament("Levetiracetam 500mg", "Levetiracetam", "Breitspektrum-Antiepileptikum", "mg"));
        repo.save(medikament("Lamotrigin 100mg", "Lamotrigin", "Antiepileptikum und Stimmungsstabilisator", "mg"));
        repo.save(medikament("Valproat 500mg retard", "Valproinsäure", "Antiepileptikum und Phasenprophylaxe", "mg"));
        repo.save(medikament("Carbamazepin 200mg", "Carbamazepin", "Antiepileptikum und Trigeminus-Neuralgie", "mg"));
        repo.save(medikament("Gabapentin 300mg", "Gabapentin", "Antiepileptikum und neuropathische Schmerzen", "mg"));
        repo.save(medikament("Pregabalin 75mg", "Pregabalin", "Alpha-2-delta-Ligand bei neuropathischen Schmerzen", "mg"));
        repo.save(medikament("Topiramat 50mg", "Topiramat", "Antiepileptikum, auch Migräneprophylaxe", "mg"));
        repo.save(medikament("Phenytoin 100mg", "Phenytoin", "Klassisches Antiepileptikum, schmale therapeutische Breite", "mg"));
        repo.save(medikament("Clonazepam 0,5mg", "Clonazepam", "Benzodiazepin-Antiepileptikum", "mg"));
        repo.save(medikament("Levodopa/Carbidopa 100/25mg", "Levodopa+Carbidopa", "Parkinson-Therapie – Goldstandard", "mg"));
        repo.save(medikament("Pramipexol 0,18mg", "Pramipexol", "Dopaminagonist bei Parkinson", "mg"));
        repo.save(medikament("Rasagilin 1mg", "Rasagilin", "MAO-B-Hemmer bei Parkinson", "mg"));

        // Psychopharmaka / Psychiatrie
        repo.save(medikament("Sertralin 50mg", "Sertralin", "SSRI-Antidepressivum bei Depression und Angst", "mg"));
        repo.save(medikament("Escitalopram 10mg", "Escitalopram", "SSRI – selektiv und gut verträglich", "mg"));
        repo.save(medikament("Citalopram 20mg", "Citalopram", "SSRI-Antidepressivum", "mg"));
        repo.save(medikament("Fluoxetin 20mg", "Fluoxetin", "SSRI – lange Halbwertszeit", "mg"));
        repo.save(medikament("Venlafaxin 75mg retard", "Venlafaxin", "SNRI bei Depression und Angststörungen", "mg"));
        repo.save(medikament("Duloxetin 60mg", "Duloxetin", "SNRI – auch bei neuropathischen Schmerzen", "mg"));
        repo.save(medikament("Mirtazapin 30mg", "Mirtazapin", "Tetrazyklisches Antidepressivum, sedierend", "mg"));
        repo.save(medikament("Amitriptylin 25mg", "Amitriptylin", "Trizyklisches Antidepressivum, auch Schmerztherapie", "mg"));
        repo.save(medikament("Lithium 450mg retard", "Lithiumcarbonat", "Phasenprophylaxe bei bipolarer Störung", "mg"));
        repo.save(medikament("Quetiapin 25mg", "Quetiapin", "Atypisches Antipsychotikum, auch bipolar", "mg"));
        repo.save(medikament("Olanzapin 10mg", "Olanzapin", "Atypisches Antipsychotikum", "mg"));
        repo.save(medikament("Risperidon 2mg", "Risperidon", "Atypisches Antipsychotikum", "mg"));
        repo.save(medikament("Haloperidol 5mg", "Haloperidol", "Klassisches Antipsychotikum", "mg"));
        repo.save(medikament("Lorazepam 1mg", "Lorazepam", "Benzodiazepin bei Angst und Agitation", "mg"));
        repo.save(medikament("Diazepam 5mg", "Diazepam", "Benzodiazepin – Muskelrelaxans und Anxiolytikum", "mg"));
        repo.save(medikament("Zolpidem 10mg", "Zolpidem", "Non-Benzodiazepin-Hypnotikum bei Schlafstörungen", "mg"));

        // Herzinsuffizienz / Kardiologie
        repo.save(medikament("Digitoxin 0,07mg", "Digitoxin", "Herzglykosid bei Herzinsuffizienz und Vorhofflimmern", "mg"));
        repo.save(medikament("Digoxin 0,25mg", "Digoxin", "Herzglykosid – renale Elimination", "mg"));
        repo.save(medikament("Ivabdin 5mg", "Ivabradin", "Sinusknoten-Hemmer bei Herzinsuffizienz", "mg"));
        repo.save(medikament("Sacubitril/Valsartan 49/51mg", "Sacubitril+Valsartan", "ARNi – innovativ bei Herzinsuffizienz mit reduzierter EF", "mg"));
        repo.save(medikament("Amiodaron 200mg", "Amiodaron", "Antiarrhythmikum der Klasse III", "mg"));
        repo.save(medikament("Flecainid 100mg", "Flecainid", "Antiarrhythmikum Klasse IC", "mg"));
        repo.save(medikament("Nitroglycerin Spray", "Glyceroltrinitrat", "Nitrat bei akutem Angina-pectoris-Anfall", "Hub"));
        repo.save(medikament("Isosorbiddinitrat 20mg", "Isosorbiddinitrat", "Langwirksames Nitrat zur Anfallsprophylaxe", "mg"));
        repo.save(medikament("Molsidomin 2mg", "Molsidomin", "NO-Donor bei Angina pectoris", "mg"));

        // Allergie / Immunologie
        repo.save(medikament("Cetirizin 10mg", "Cetirizin", "Nicht-sedierendes Antihistaminikum (H1)", "mg"));
        repo.save(medikament("Loratadin 10mg", "Loratadin", "Nicht-sedierendes Antihistaminikum der 2. Generation", "mg"));
        repo.save(medikament("Desloratadin 5mg", "Desloratadin", "Aktiver Metabolit von Loratadin, potenter", "mg"));
        repo.save(medikament("Fexofenadin 120mg", "Fexofenadin", "H1-Antihistaminikum, kaum sedierend", "mg"));
        repo.save(medikament("Prednisolon 5mg", "Prednisolon", "Systemisches Kortikosteroid, kurze Halbwertszeit", "mg"));
        repo.save(medikament("Prednisolon 50mg", "Prednisolon", "Hochdosis-Kortikosteroid bei schweren Entzündungen", "mg"));
        repo.save(medikament("Methylprednisolon 16mg", "Methylprednisolon", "Kortikosteroid – hochpotent, wenig mineralokortikoid", "mg"));
        repo.save(medikament("Dexamethason 4mg", "Dexamethason", "Stark potentes Glukokortikoid, kein mineralokort. Effekt", "mg"));
        repo.save(medikament("Epinephrin 0,3mg Autoinjekt.", "Epinephrin", "Notfalltherapie bei anaphylaktischer Reaktion", "mg"));

        // Niere / Urologie
        repo.save(medikament("Tamsulosin 0,4mg", "Tamsulosin", "Alpha-1-Blocker bei BPH", "mg"));
        repo.save(medikament("Finasterid 5mg", "Finasterid", "5-Alpha-Reduktasehemmer bei BPH", "mg"));
        repo.save(medikament("Dutasterid 0,5mg", "Dutasterid", "5-Alpha-Reduktasehemmer – dual-wirksam", "mg"));
        repo.save(medikament("Desmopressin 0,1mg", "Desmopressin", "ADH-Analogon bei Diabetes insipidus und Enuresis", "mg"));
        repo.save(medikament("Tolvaptan 15mg", "Tolvaptan", "Vasopressin-V2-Antagonist bei Hyponatriämie / ADPKD", "mg"));

        // Geriatrie / Osteoporose
        repo.save(medikament("Alendronsäure 70mg wöchentl.", "Alendronsäure", "Bisphosphonat bei Osteoporose, wöchentlich", "mg"));
        repo.save(medikament("Risedronat 35mg wöchentl.", "Risedronsäure", "Bisphosphonat bei Osteoporose", "mg"));
        repo.save(medikament("Zoledronat 5mg/100ml i.v.", "Zoledronsäure", "Bisphosphonat – jährliche i.v.-Infusion", "mg"));
        repo.save(medikament("Denosumab 60mg/ml", "Denosumab", "RANK-Ligand-Inhibitor bei Osteoporose", "mg"));
        repo.save(medikament("Kalzium 500mg + Vitamin D3", "Calciumcarbonat+Colecalciferol", "Supplemente zur Osteoporoseprävention", "mg"));
        repo.save(medikament("Vitamin D3 1000 IE", "Colecalciferol", "Vitamin-D-Supplementation", "IE"));
        repo.save(medikament("Vitamin B12 1000 µg", "Cyanocobalamin", "Vitamin-B12-Mangel und perniziöse Anämie", "µg"));
        repo.save(medikament("Folsäure 5mg", "Folsäure", "Folat-Substitution bei Mangel oder Schwangerschaft", "mg"));
        repo.save(medikament("Eisensulfat 100mg", "Eisen(II)-sulfat", "Orale Eisensubstitution bei Eisenmangelanämie", "mg"));
        repo.save(medikament("Ferrinject 500mg i.v.", "Eisencarboxymaltose", "Intravenöse Eisensubstitution", "mg"));

        // Onkologie / supportiv
        repo.save(medikament("Ondansetron 8mg i.v.", "Ondansetron", "5-HT3-Antagonist – antiemetische Supportivtherapie", "mg"));
        repo.save(medikament("Dexamethason 8mg i.v.", "Dexamethason", "Antiemetisch und antiinflammatorisch bei Chemotherapie", "mg"));
        repo.save(medikament("Filgrastim 300 µg", "Filgrastim", "G-CSF – Neutrophilenstimulation nach Chemotherapie", "µg"));
        repo.save(medikament("Erythropoetin 4000 IE", "Epoetin alfa", "Erythropoese-stimulierend bei Tumoranämie", "IE"));
        repo.save(medikament("Methotrexat 10mg", "Methotrexat", "Zytostatikum und DMARD bei Rheumatoider Arthritis", "mg"));
        repo.save(medikament("Imatinib 400mg", "Imatinib", "Tyrosinkinasehemmer bei CML", "mg"));

        // Rheumatologie
        repo.save(medikament("Hydroxychloroquin 200mg", "Hydroxychloroquin", "DMARD bei Lupus und Rheumatoider Arthritis", "mg"));
        repo.save(medikament("Sulfasalazin 500mg", "Sulfasalazin", "DMARD bei RA und chronisch-entzündlichen Darmerkrankungen", "mg"));
        repo.save(medikament("Leflunomid 20mg", "Leflunomid", "DMARD bei Rheumatoider Arthritis", "mg"));
        repo.save(medikament("Allopurinol 300mg", "Allopurinol", "Xanthinoxidase-Hemmer bei Gicht", "mg"));
        repo.save(medikament("Febuxostat 80mg", "Febuxostat", "Selektiver Xanthinoxidase-Hemmer bei Gicht", "mg"));
        repo.save(medikament("Colchicin 0,5mg", "Colchicin", "Antimitotikum bei akutem Gichtanfall und Perikarditis", "mg"));

        // Dermatologie (oral/systemisch)
        repo.save(medikament("Isotretinoin 20mg", "Isotretinoin", "Vitamin-A-Säure-Derivat bei schwerer Akne", "mg"));
        repo.save(medikament("Aciclovir 400mg", "Aciclovir", "Virostatikum bei Herpes simplex und Zoster", "mg"));
        repo.save(medikament("Valaciclovir 500mg", "Valaciclovir", "Prodrug des Aciclovir, bessere Bioverfügbarkeit", "mg"));
        repo.save(medikament("Fluconazol 150mg", "Fluconazol", "Triazol-Antimykotikum bei Candida-Infektionen", "mg"));
        repo.save(medikament("Itraconazol 100mg", "Itraconazol", "Antimykotikum bei Pilzinfektionen der Nägel", "mg"));

        // Augenheilkunde / HNO
        repo.save(medikament("Latanoprost 0,005% Augentropfen", "Latanoprost", "Prostaglandin-Analogon bei Glaukom", "ml"));
        repo.save(medikament("Timolol 0,5% Augentropfen", "Timolol", "Betablocker-Augentropfen bei Glaukom", "ml"));
        repo.save(medikament("Xylometazolin 0,1% Nasenspray", "Xylometazolin", "Alpha-Sympathomimetikum bei Schnupfen", "ml"));
        repo.save(medikament("Mometason 50 µg Nasenspray", "Mometasonfuroat", "Intranasales Kortikosteroid bei Rhinitis", "µg"));
        repo.save(medikament("Betahistin 16mg", "Betahistin", "H3-Rezeptor-Antagonist bei Morbus Menière", "mg"));

        LOGGER.info("Loaded {} Medikamente.", repo.count());
    }

    private static Medikament medikament(String name, String wirkstoff, String beschreibung, String einheit) {
        Medikament m = new Medikament();
        m.setName(name);
        m.setWirkstoff(wirkstoff);
        m.setBeschreibung(beschreibung);
        m.setDosiereinheit(einheit);
        return m;
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
