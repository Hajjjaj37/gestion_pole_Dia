package com.gestion.demo.controller;

import com.gestion.demo.dto.EmploiTempsRequest;
import com.gestion.demo.dto.EmploiTempsResponse;
import com.gestion.demo.model.EmploiTemps;
import com.gestion.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;
import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/emploi-temps")
@RequiredArgsConstructor
public class EmploiTempsController {

    private final EmploiTempsRepository emploiTempsRepository;
    private final FormateurRepository formateurRepository;
    private final ModuleRepository moduleRepository;
    private final ClasseRepository classeRepository;
    private final SalleRepository salleRepository;
    private final SeanceRepository seanceRepository;

    @PostMapping
    public ResponseEntity<?> createEmploiTemps(@RequestBody EmploiTempsRequest request) {
        try {
            log.info("Début de la création de l'emploi du temps");
            
            // Vérifier les conflits
            if (hasConflicts(request.getJour(), request)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Conflit détecté dans l'emploi du temps"));
            }

            // Créer l'emploi du temps
            EmploiTemps emploiTemps = createEmploiTemps(request.getJour(), request, 1);
            emploiTemps = emploiTempsRepository.save(emploiTemps);

            return ResponseEntity.ok(createSuccessResponse("Emploi du temps créé avec succès", convertToDTO(emploiTemps)));

        } catch (Exception e) {
            log.error("Erreur lors de la création de l'emploi du temps", e);
            return ResponseEntity.status(500).body(createErrorResponse("Erreur lors de la création de l'emploi du temps: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllEmploiTemps() {
        try {
            log.info("Début de la récupération de tous les emplois du temps");
            
            List<EmploiTemps> emploiTemps = emploiTempsRepository.findAll();
            List<EmploiTempsResponse> responseList = emploiTemps.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(createSuccessResponse("Emploi du temps récupéré avec succès", responseList));
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'emploi du temps", e);
            return ResponseEntity.status(500).body(createErrorResponse("Erreur lors de la récupération de l'emploi du temps: " + e.getMessage()));
        }
    }

    @GetMapping("/jour/{jour}")
    public ResponseEntity<?> getEmploiTempsByDay(@PathVariable String jour) {
        try {
            log.info("Début de la récupération de l'emploi du temps pour le jour: {}", jour);
            
            DayOfWeek dayOfWeek = convertToDayOfWeek(jour);
            List<EmploiTemps> emploiTemps = emploiTempsRepository.findByJourOrderByNumeroSeance(dayOfWeek);
            List<EmploiTempsResponse> responseList = emploiTemps.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(createSuccessResponse("Emploi du temps récupéré avec succès", responseList));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse("Jour invalide. Utilisez un jour valide (LUNDI, MARDI, etc.)"));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'emploi du temps", e);
            return ResponseEntity.status(500).body(createErrorResponse("Erreur lors de la récupération de l'emploi du temps: " + e.getMessage()));
        }
    }

    @PostMapping("/classe/{classeId}")
    public ResponseEntity<?> createEmploiTempsForClasse(
            @PathVariable Long classeId,
            @RequestBody List<EmploiTempsRequest> requests) {
        try {
            log.info("Début de la création de l'emploi du temps pour la classe ID: {}", classeId);

            // Vérifier si la classe existe
            if (!classeRepository.existsById(classeId)) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Classe non trouvée avec l'ID: " + classeId));
            }

            // Vérifier si la classe a déjà un emploi du temps
            List<EmploiTemps> existingEmploiTemps = emploiTempsRepository.findByClasseId(classeId);
            if (!existingEmploiTemps.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Cette classe possède déjà un emploi du temps"));
            }

            List<EmploiTemps> createdEmploiTemps = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            // Grouper les séances par jour
            Map<DayOfWeek, List<EmploiTempsRequest>> seancesByDay = requests.stream()
                .collect(Collectors.groupingBy(EmploiTempsRequest::getJour));

            // Vérifier que tous les jours de la semaine sont couverts
            if (seancesByDay.size() < 5) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("L'emploi du temps doit couvrir tous les jours de la semaine (Lundi à Vendredi)"));
            }

            // Traiter chaque jour
            for (Map.Entry<DayOfWeek, List<EmploiTempsRequest>> entry : seancesByDay.entrySet()) {
                DayOfWeek jour = entry.getKey();
                List<EmploiTempsRequest> seancesDuJour = entry.getValue();

                // Vérifier le nombre de séances par jour
                if (seancesDuJour.size() > 4) {
                    errors.add("Maximum 4 séances autorisées pour le jour " + jour);
                    continue;
                }

                // Traiter chaque séance du jour
                for (int i = 0; i < seancesDuJour.size(); i++) {
                    EmploiTempsRequest request = seancesDuJour.get(i);
                    try {
                        // Vérifier que la séance est pour la bonne classe
                        if (!request.getClasseId().equals(classeId)) {
                            errors.add("La séance " + (i + 1) + " du jour " + jour + 
                                " ne correspond pas à la classe spécifiée");
                            continue;
                        }

                        // Vérifier les entités
                        if (!validateEntities(request)) {
                            errors.add("Entités invalides pour la séance " + (i + 1) + " du jour " + jour);
                            continue;
                        }

                        // Vérifier les conflits
                        if (hasConflicts(jour, request)) {
                            errors.add("Conflit détecté pour la séance " + (i + 1) + " du jour " + jour);
                            continue;
                        }

                        // Créer l'emploi du temps
                        EmploiTemps emploiTemps = createEmploiTemps(jour, request, i + 1);
                        emploiTemps = emploiTempsRepository.save(emploiTemps);
                        createdEmploiTemps.add(emploiTemps);

                    } catch (Exception e) {
                        errors.add("Erreur pour la séance " + (i + 1) + " du jour " + jour + ": " + e.getMessage());
                    }
                }
            }

            // Préparer la réponse
            List<EmploiTempsResponse> responseList = createdEmploiTemps.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            Map<String, Object> response = createSuccessResponse(
                "Emploi du temps créé avec succès pour la classe", 
                responseList
            );

            if (!errors.isEmpty()) {
                response.put("warnings", errors);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la création de l'emploi du temps", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de la création de l'emploi du temps: " + e.getMessage()));
        }
    }

    @GetMapping("/classe/{classeId}/jour/{jour}")
    public ResponseEntity<?> getEmploiTempsByClasseAndDay(
            @PathVariable Long classeId,
            @PathVariable String jour) {
        try {
            log.info("Début de la récupération de l'emploi du temps pour la classe ID: {} et le jour: {}", classeId, jour);
            
            // Vérifier si la classe existe
            if (!classeRepository.existsById(classeId)) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Classe non trouvée avec l'ID: " + classeId));
            }

            // Convertir le jour en DayOfWeek
            DayOfWeek dayOfWeek;
            try {
                dayOfWeek = convertToDayOfWeek(jour);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("Jour invalide. Utilisez un jour valide (MONDAY, TUESDAY, etc.)"));
            }

            // Récupérer l'emploi du temps avec la nouvelle méthode
            List<EmploiTemps> emploiTemps = emploiTempsRepository.findByClasseIdAndJourOrderByNumeroSeance(classeId, dayOfWeek);
            
            if (emploiTemps.isEmpty()) {
                return ResponseEntity.ok(createSuccessResponse(
                    "Aucun emploi du temps trouvé pour cette classe et ce jour",
                    new ArrayList<>()
                ));
            }

            // Convertir en DTO
            List<EmploiTempsResponse> responseList = emploiTemps.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(createSuccessResponse(
                "Emploi du temps récupéré avec succès",
                responseList
            ));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'emploi du temps", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de la récupération de l'emploi du temps: " + e.getMessage()));
        }
    }

    @PostMapping("/import-csv/{classeId}")
    public ResponseEntity<?> importEmploiTempsFromCsv(
            @PathVariable Long classeId,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Le fichier est vide"));
        }

        // Vérifier si la classe existe
        if (!classeRepository.existsById(classeId)) {
            return ResponseEntity.badRequest().body(createErrorResponse("Classe non trouvée avec l'ID: " + classeId));
        }

        // Vérifier si la classe a déjà un emploi du temps
        List<EmploiTemps> existingEmploiTemps = emploiTempsRepository.findByClasseId(classeId);
        if (!existingEmploiTemps.isEmpty()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Cette classe possède déjà un emploi du temps"));
        }

        List<EmploiTemps> createdEmploiTemps = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (CSVParser parser = CSVParser.parse(
                file.getInputStream(),
                StandardCharsets.UTF_8,
                CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreSurroundingSpaces()
                    .withTrim()
            )) {
            // Nettoyage des clés d'en-tête pour enlever BOM et espaces
            Map<String, Integer> headerMap = parser.getHeaderMap();
            Map<String, String> cleanedHeaderMap = new HashMap<>();
            for (String key : headerMap.keySet()) {
                String cleaned = key.replace("\uFEFF", "").trim();
                cleanedHeaderMap.put(cleaned, key);
            }

            for (CSVRecord record : parser) {
                try {
                    String jourStr = record.get(cleanedHeaderMap.get("Jour")).trim();
                    DayOfWeek jour = convertToDayOfWeek(jourStr);

                    Long csvClasseId = Long.parseLong(record.get(cleanedHeaderMap.get("Classe ID")).trim());
                    if (!csvClasseId.equals(classeId)) {
                        errors.add("La ligne " + record.getRecordNumber() + " ne correspond pas à la classe spécifiée.");
                        continue;
                    }

                    EmploiTempsRequest request = new EmploiTempsRequest();
                    request.setJour(jour);
                    request.setSeanceId(Long.parseLong(record.get(cleanedHeaderMap.get("Séance ID")).trim()));
                    request.setFormateurId(Long.parseLong(record.get(cleanedHeaderMap.get("Formateur ID")).trim()));
                    request.setModuleId(Long.parseLong(record.get(cleanedHeaderMap.get("Module ID")).trim()));
                    request.setClasseId(classeId);
                    request.setSalleId(Long.parseLong(record.get(cleanedHeaderMap.get("Salle ID")).trim()));

                    // Vérifier les entités
                    if (!validateEntities(request)) {
                        errors.add("Entités invalides pour la ligne: " + record.getRecordNumber());
                        continue;
                    }

                    // Vérifier les conflits
                    if (hasConflicts(jour, request)) {
                        errors.add("Conflit détecté pour la ligne: " + record.getRecordNumber());
                        continue;
                    }

                    // Créer l'emploi du temps
                    EmploiTemps emploiTemps = createEmploiTemps(jour, request, (int) record.getRecordNumber());
                    emploiTemps = emploiTempsRepository.save(emploiTemps);
                    createdEmploiTemps.add(emploiTemps);

                } catch (Exception e) {
                    errors.add("Erreur à la ligne " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }

            List<EmploiTempsResponse> responseList = createdEmploiTemps.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

            Map<String, Object> response = createSuccessResponse("Import terminé", responseList);
            if (!errors.isEmpty()) {
                response.put("warnings", errors);
            }
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Erreur lors de l'import: " + e.getMessage()));
        }
    }

    @PutMapping("/{emploiId}")
    public ResponseEntity<?> updateEmploiTemps(
            @PathVariable Long emploiId,
            @RequestBody EmploiTempsRequest request) {
        Optional<EmploiTemps> optionalEmploi = emploiTempsRepository.findById(emploiId);
        if (!optionalEmploi.isPresent()) {
            return ResponseEntity.badRequest().body(createErrorResponse("Emploi du temps non trouvé"));
        }
        EmploiTemps emploi = optionalEmploi.get();

        // Met à jour les champs
        emploi.setJour(request.getJour());
        emploi.setSeance(seanceRepository.findById(request.getSeanceId()).orElse(null));
        emploi.setFormateur(formateurRepository.findById(request.getFormateurId()).orElse(null));
        emploi.setModule(moduleRepository.findById(request.getModuleId()).orElse(null));
        emploi.setClasse(classeRepository.findById(request.getClasseId()).orElse(null));
        emploi.setSalle(salleRepository.findById(request.getSalleId()).orElse(null));

        emploiTempsRepository.save(emploi);

        return ResponseEntity.ok(createSuccessResponse("Emploi du temps modifié avec succès", convertToDTO(emploi)));
    }

    private DayOfWeek convertToDayOfWeek(String jour) {
        try {
            String jourUpper = jour.toUpperCase();
            switch (jourUpper) {
                case "LUNDI": return DayOfWeek.MONDAY;
                case "MARDI": return DayOfWeek.TUESDAY;
                case "MERCREDI": return DayOfWeek.WEDNESDAY;
                case "JEUDI": return DayOfWeek.THURSDAY;
                case "VENDREDI": return DayOfWeek.FRIDAY;
                case "SAMEDI": return DayOfWeek.SATURDAY;
                case "DIMANCHE": return DayOfWeek.SUNDAY;
                default: return DayOfWeek.valueOf(jourUpper);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Jour invalide : " + jour);
        }
    }

    private boolean validateEntities(EmploiTempsRequest request) {
        return classeRepository.existsById(request.getClasseId()) &&
               formateurRepository.existsById(request.getFormateurId()) &&
               moduleRepository.existsById(request.getModuleId()) &&
               salleRepository.existsById(request.getSalleId()) &&
               seanceRepository.existsById(request.getSeanceId());
    }

    private boolean hasConflicts(DayOfWeek jour, EmploiTempsRequest request) {
        return emploiTempsRepository.existsByJourAndSeanceIdAndClasseId(jour, request.getSeanceId(), request.getClasseId()) ||
               emploiTempsRepository.existsByJourAndSeanceIdAndFormateurId(jour, request.getSeanceId(), request.getFormateurId()) ||
               emploiTempsRepository.existsByJourAndSeanceIdAndSalleId(jour, request.getSeanceId(), request.getSalleId());
    }

    private EmploiTemps createEmploiTemps(DayOfWeek jour, EmploiTempsRequest request, int numeroSeance) {
        EmploiTemps emploiTemps = new EmploiTemps();
        emploiTemps.setJour(jour);
        emploiTemps.setNumeroSeance(numeroSeance);
        
        emploiTemps.setSeance(seanceRepository.findById(request.getSeanceId())
            .orElseThrow(() -> new RuntimeException("Séance non trouvée")));
        
        emploiTemps.setFormateur(formateurRepository.findById(request.getFormateurId())
            .orElseThrow(() -> new RuntimeException("Formateur non trouvé")));
        
        emploiTemps.setModule(moduleRepository.findById(request.getModuleId())
            .orElseThrow(() -> new RuntimeException("Module non trouvé")));
        
        emploiTemps.setClasse(classeRepository.findById(request.getClasseId())
            .orElseThrow(() -> new RuntimeException("Classe non trouvée")));
        
        emploiTemps.setSalle(salleRepository.findById(request.getSalleId())
            .orElseThrow(() -> new RuntimeException("Salle non trouvée")));

        return emploiTemps;
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    private EmploiTempsResponse convertToDTO(EmploiTemps emploiTemps) {
        EmploiTempsResponse response = new EmploiTempsResponse();
        response.setId(emploiTemps.getId());
        response.setJour(emploiTemps.getJour());
        response.setNumeroSeance(emploiTemps.getNumeroSeance());

        if (emploiTemps.getSeance() != null) {
            EmploiTempsResponse.SeanceDTO seanceDTO = new EmploiTempsResponse.SeanceDTO();
            seanceDTO.setId(emploiTemps.getSeance().getId());
            seanceDTO.setNom(emploiTemps.getSeance().getNom());
            seanceDTO.setPeriode(emploiTemps.getSeance().getPeriode());
            seanceDTO.setNumero(emploiTemps.getSeance().getNumero());
            seanceDTO.setHeureDebut(emploiTemps.getSeance().getHeureDebut());
            seanceDTO.setHeureFin(emploiTemps.getSeance().getHeureFin());
            response.setSeance(seanceDTO);
        }

        if (emploiTemps.getFormateur() != null) {
            EmploiTempsResponse.FormateurDTO formateurDTO = new EmploiTempsResponse.FormateurDTO();
            formateurDTO.setId(emploiTemps.getFormateur().getId());
            formateurDTO.setNom(emploiTemps.getFormateur().getNom());
            formateurDTO.setPrenom(emploiTemps.getFormateur().getPrenom());
            formateurDTO.setEmail(emploiTemps.getFormateur().getEmail());
            formateurDTO.setSpecialite(emploiTemps.getFormateur().getSpecialite());
            response.setFormateur(formateurDTO);
        }

        if (emploiTemps.getModule() != null) {
            EmploiTempsResponse.ModuleDTO moduleDTO = new EmploiTempsResponse.ModuleDTO();
            moduleDTO.setId(emploiTemps.getModule().getId());
            moduleDTO.setNom(emploiTemps.getModule().getNom());
            moduleDTO.setDescription(emploiTemps.getModule().getDescription());
            moduleDTO.setDuree(emploiTemps.getModule().getDuree());
            response.setModule(moduleDTO);
        }

        if (emploiTemps.getClasse() != null) {
            EmploiTempsResponse.ClasseDTO classeDTO = new EmploiTempsResponse.ClasseDTO();
            classeDTO.setId(emploiTemps.getClasse().getId());
            classeDTO.setNom(emploiTemps.getClasse().getNom());
            classeDTO.setDescription(emploiTemps.getClasse().getDescription());
            
            // Gestion plus sûre des dates avec conversion
            try {
                if (emploiTemps.getClasse().getDateDebut() != null) {
                    // Conversion de Date vers LocalDate si nécessaire
                    LocalDate dateDebut = emploiTemps.getClasse().getDateDebut()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                    classeDTO.setDateDebut(dateDebut);
                }
                
                if (emploiTemps.getClasse().getDateFin() != null) {
                    // Conversion de Date vers LocalDate si nécessaire
                    LocalDate dateFin = emploiTemps.getClasse().getDateFin()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                    classeDTO.setDateFin(dateFin);
                }
            } catch (Exception e) {
                log.warn("Erreur de conversion des dates pour la classe ID: {} - Erreur: {}", 
                    emploiTemps.getClasse().getId(), e.getMessage());
            }
            
            response.setClasse(classeDTO);
        }

        if (emploiTemps.getSalle() != null) {
            EmploiTempsResponse.SalleDTO salleDTO = new EmploiTempsResponse.SalleDTO();
            salleDTO.setId(emploiTemps.getSalle().getId());
            salleDTO.setNom(emploiTemps.getSalle().getNom());
            salleDTO.setNumero(emploiTemps.getSalle().getNumero());
            salleDTO.setDescription(emploiTemps.getSalle().getDescription());
            salleDTO.setCapacite(emploiTemps.getSalle().getCapacite());
            salleDTO.setEquipement(emploiTemps.getSalle().getEquipement());
            response.setSalle(salleDTO);
        }

        return response;
    }
}
 