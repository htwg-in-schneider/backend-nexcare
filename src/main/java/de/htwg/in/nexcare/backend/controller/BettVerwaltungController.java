package de.htwg.in.nexcare.backend.controller;

import de.htwg.in.nexcare.backend.model.*;
import de.htwg.in.nexcare.backend.repository.*;
import de.htwg.in.nexcare.backend.service.SecurityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/betten")
public class BettVerwaltungController {

    private final EtageRepository etageRepo;
    private final ZimmerRepository zimmerRepo;
    private final BettRepository bettRepo;
    private final KlinikumRepository klinikumRepo;
    private final PatientRepository patientRepo;
    private final PatientNachrichtRepository nachrichtRepo;
    private final SecurityService securityService;

    public BettVerwaltungController(EtageRepository etageRepo, ZimmerRepository zimmerRepo,
                                    BettRepository bettRepo, KlinikumRepository klinikumRepo,
                                    PatientRepository patientRepo,
                                    PatientNachrichtRepository nachrichtRepo, SecurityService securityService) {
        this.etageRepo = etageRepo;
        this.zimmerRepo = zimmerRepo;
        this.bettRepo = bettRepo;
        this.klinikumRepo = klinikumRepo;
        this.patientRepo = patientRepo;
        this.nachrichtRepo = nachrichtRepo;
        this.securityService = securityService;
    }

    /** Returns the full hospital structure: Klinikum → Etagen → Zimmer → Betten */
    @GetMapping("/struktur")
    public ResponseEntity<Map<String, Object>> getStruktur(@RequestParam Long klinikumId,
                                                            @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        Optional<Klinikum> opt = klinikumRepo.findById(klinikumId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Klinikum klinikum = opt.get();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("klinikumId", klinikum.getId());
        result.put("klinikumName", klinikum.getName());
        result.put("etagen", buildEtagen(klinikumId));
        return ResponseEntity.ok(result);
    }

    private List<Map<String, Object>> buildEtagen(Long klinikumId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Etage etage : etageRepo.findByKlinikumIdOrderByNummerAsc(klinikumId)) {
            Map<String, Object> e = new LinkedHashMap<>();
            e.put("id", etage.getId());
            e.put("nummer", etage.getNummer());
            e.put("bezeichnung", etage.getBezeichnung());
            e.put("zimmer", buildZimmer(etage.getId()));
            list.add(e);
        }
        return list;
    }

    private List<Map<String, Object>> buildZimmer(Long etageId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Zimmer zimmer : zimmerRepo.findByEtageIdOrderByNummerAsc(etageId)) {
            Map<String, Object> z = new LinkedHashMap<>();
            z.put("id", zimmer.getId());
            z.put("nummer", zimmer.getNummer());
            z.put("abteilung", zimmer.getAbteilung());
            z.put("station", zimmer.getStation());
            z.put("betten", buildBetten(zimmer.getId()));
            list.add(z);
        }
        return list;
    }

    private List<Map<String, Object>> buildBetten(Long zimmerId) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Bett bett : bettRepo.findByZimmerIdOrderByBezeichnungAsc(zimmerId)) {
            Map<String, Object> b = new LinkedHashMap<>();
            b.put("id", bett.getId());
            b.put("bezeichnung", bett.getBezeichnung());
            b.put("status", bett.getStatus().name());
            b.put("patientId", bett.getPatient() != null ? bett.getPatient().getId() : null);
            b.put("patientName", bett.getPatient() != null
                ? bett.getPatient().getVorname() + " " + bett.getPatient().getNachname() : null);
            list.add(b);
        }
        return list;
    }

    // ── Etage CRUD ───────────────────────────────────────────────────────────

    @PostMapping("/etage")
    public ResponseEntity<Map<String, Object>> createEtage(@RequestBody Map<String, Object> body,
                                                            @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        Object klinikumIdRaw = body.get("klinikumId");
        Object nummerRaw = body.get("nummer");
        Object bezeichnungRaw = body.get("bezeichnung");
        if (klinikumIdRaw == null || nummerRaw == null || bezeichnungRaw == null
                || bezeichnungRaw.toString().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("fehler", "klinikumId, nummer und bezeichnung sind erforderlich"));
        }
        Long klinikumId = Long.valueOf(klinikumIdRaw.toString());
        Optional<Klinikum> opt = klinikumRepo.findById(klinikumId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        int nummer = ((Number) nummerRaw).intValue();
        if (nummer < -5 || nummer > 100) {
            return ResponseEntity.badRequest().body(Map.of("fehler", "Stockwerk muss zwischen -5 und 100 liegen"));
        }
        String bezeichnung = bezeichnungRaw.toString().trim();
        if (bezeichnung.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("fehler", "Bezeichnung darf maximal 100 Zeichen haben"));
        }
        Etage etage = etageRepo.save(new Etage(nummer, bezeichnung, opt.get()));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", etage.getId());
        resp.put("nummer", etage.getNummer());
        resp.put("bezeichnung", etage.getBezeichnung());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/etage/{id}")
    public ResponseEntity<Void> deleteEtage(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isAdmin(jwt)) return ResponseEntity.status(403).build();
        if (!etageRepo.existsById(id)) return ResponseEntity.notFound().build();
        List<Zimmer> zimmerList = zimmerRepo.findByEtageIdOrderByNummerAsc(id);
        for (Zimmer z : zimmerList) releaseAndDeleteBetten(z.getId());
        zimmerRepo.deleteAll(zimmerList);
        etageRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Zimmer CRUD ──────────────────────────────────────────────────────────

    @PostMapping("/zimmer")
    public ResponseEntity<Map<String, Object>> createZimmer(@RequestBody Map<String, Object> body,
                                                             @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        Object etageIdRaw = body.get("etageId");
        Object nummerRaw = body.get("nummer");
        if (etageIdRaw == null || nummerRaw == null || nummerRaw.toString().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("fehler", "etageId und nummer sind erforderlich"));
        }
        Long etageId = Long.valueOf(etageIdRaw.toString());
        Optional<Etage> opt = etageRepo.findById(etageId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        String nummer = nummerRaw.toString().trim();
        if (nummer.length() > 20) {
            return ResponseEntity.badRequest().body(Map.of("fehler", "Zimmernummer darf maximal 20 Zeichen haben"));
        }
        String abteilung = body.getOrDefault("abteilung", "").toString().trim();
        String station = body.getOrDefault("station", "").toString().trim();
        Zimmer zimmer = zimmerRepo.save(new Zimmer(nummer, abteilung, station, opt.get()));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", zimmer.getId());
        resp.put("nummer", zimmer.getNummer());
        resp.put("abteilung", zimmer.getAbteilung());
        resp.put("station", zimmer.getStation());
        resp.put("betten", new ArrayList<>());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/zimmer/{id}")
    public ResponseEntity<Void> deleteZimmer(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isAdmin(jwt)) return ResponseEntity.status(403).build();
        if (!zimmerRepo.existsById(id)) return ResponseEntity.notFound().build();
        releaseAndDeleteBetten(id);
        zimmerRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ── Bett CRUD ────────────────────────────────────────────────────────────

    @PostMapping("/bett")
    public ResponseEntity<Map<String, Object>> createBett(@RequestBody Map<String, Object> body,
                                                           @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        Object zimmerIdRaw = body.get("zimmerId");
        Object bezeichnungRaw = body.get("bezeichnung");
        if (zimmerIdRaw == null || bezeichnungRaw == null || bezeichnungRaw.toString().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("fehler", "zimmerId und bezeichnung sind erforderlich"));
        }
        Long zimmerId = Long.valueOf(zimmerIdRaw.toString());
        Optional<Zimmer> opt = zimmerRepo.findById(zimmerId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        String bezeichnung = bezeichnungRaw.toString().trim();
        if (bezeichnung.length() > 50) {
            return ResponseEntity.badRequest().body(Map.of("fehler", "Bezeichnung darf maximal 50 Zeichen haben"));
        }
        Bett bett = bettRepo.save(new Bett(bezeichnung, opt.get()));
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", bett.getId());
        resp.put("bezeichnung", bett.getBezeichnung());
        resp.put("status", bett.getStatus().name());
        resp.put("patientId", null);
        resp.put("patientName", null);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/bett/{id}")
    public ResponseEntity<Void> deleteBett(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isAdmin(jwt)) return ResponseEntity.status(403).build();
        Optional<Bett> opt = bettRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Bett bett = opt.get();
        if (bett.getPatient() != null) {
            Patient p = bett.getPatient();
            p.setBett(null); p.setZimmer(null); p.setEtage(null);
            patientRepo.save(p);
        }
        bettRepo.delete(bett);
        return ResponseEntity.noContent().build();
    }

    // ── Patienten-Zuweisung ──────────────────────────────────────────────────

    @PutMapping("/bett/{bettId}/assign/{patientId}")
    public ResponseEntity<Map<String, Object>> assignPatient(
            @PathVariable Long bettId, @PathVariable Long patientId,
            @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        Optional<Bett> optBett = bettRepo.findById(bettId);
        Optional<Patient> optPatient = patientRepo.findById(patientId);
        if (optBett.isEmpty() || optPatient.isEmpty()) return ResponseEntity.notFound().build();

        Bett bett = optBett.get();
        Patient patient = optPatient.get();

        if (bett.getStatus() == BettStatus.GESPERRT) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Bett ist gesperrt.");
            return ResponseEntity.badRequest().body(err);
        }

        bettRepo.findByPatientId(patientId).ifPresent(oldBett -> {
            oldBett.setPatient(null);
            oldBett.setStatus(BettStatus.FREI);
            bettRepo.save(oldBett);
        });

        bett.setPatient(patient);
        bett.setStatus(BettStatus.BELEGT);
        bettRepo.save(bett);

        Zimmer zimmer = bett.getZimmer();
        Etage etage = zimmer.getEtage();
        patient.setZimmer(zimmer.getNummer());
        patient.setAbteilung(zimmer.getAbteilung());
        patient.setStation(zimmer.getStation());
        patient.setEtage(etage.getBezeichnung());
        patient.setBett(bett.getBezeichnung());
        patientRepo.save(patient);

        // Notify patient in portal
        String klinikumName = etage.getKlinikum() != null ? etage.getKlinikum().getName() : "–";
        nachrichtRepo.save(new PatientNachricht(patient,
            "Bett zugewiesen",
            "Ihnen wurde ein Bett zugewiesen. Klinikum: " + klinikumName +
            ", Etage: " + etage.getBezeichnung() +
            ", Zimmer: " + zimmer.getNummer() +
            ", Bett: " + bett.getBezeichnung() + ". Bitte melden Sie sich an der Aufnahme.",
            "BETT"));

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("bettId", bett.getId());
        resp.put("status", bett.getStatus().name());
        resp.put("patientId", patient.getId());
        resp.put("patientName", patient.getVorname() + " " + patient.getNachname());
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/bett/{bettId}/release")
    public ResponseEntity<Map<String, Object>> releaseBett(@PathVariable Long bettId,
                                                            @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        Optional<Bett> opt = bettRepo.findById(bettId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Bett bett = opt.get();
        if (bett.getPatient() != null) {
            Patient p = bett.getPatient();
            p.setBett(null); p.setZimmer(null); p.setAbteilung(null);
            p.setStation(null); p.setEtage(null);
            patientRepo.save(p);
        }
        bett.setPatient(null);
        bett.setStatus(BettStatus.FREI);
        bettRepo.save(bett);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("bettId", bett.getId());
        resp.put("status", "FREI");
        return ResponseEntity.ok(resp);
    }

    @PutMapping("/bett/{bettId}/status")
    public ResponseEntity<Map<String, Object>> updateBettStatus(
            @PathVariable Long bettId, @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Jwt jwt) {
        if (!securityService.isStaff(jwt)) return ResponseEntity.status(403).build();
        Optional<Bett> opt = bettRepo.findById(bettId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Bett bett = opt.get();
        try {
            BettStatus newStatus = BettStatus.valueOf(body.get("status"));
            if (newStatus == BettStatus.FREI && bett.getPatient() != null) {
                Patient p = bett.getPatient();
                p.setBett(null); p.setZimmer(null); p.setEtage(null);
                patientRepo.save(p);
                bett.setPatient(null);
            }
            bett.setStatus(newStatus);
            bettRepo.save(bett);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("bettId", bett.getId());
            resp.put("status", bett.getStatus().name());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Ungültiger Status.");
            return ResponseEntity.badRequest().body(err);
        }
    }

    private void releaseAndDeleteBetten(Long zimmerId) {
        List<Bett> betten = bettRepo.findByZimmerIdOrderByBezeichnungAsc(zimmerId);
        for (Bett bett : betten) {
            if (bett.getPatient() != null) {
                Patient p = bett.getPatient();
                p.setBett(null); p.setZimmer(null); p.setEtage(null);
                patientRepo.save(p);
            }
        }
        bettRepo.deleteAll(betten);
    }
}
