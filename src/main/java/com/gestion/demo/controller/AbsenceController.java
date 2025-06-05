package com.gestion.demo.controller;

import com.gestion.demo.dto.AbsenceDTO;
import com.gestion.demo.model.Absence;
import com.gestion.demo.model.Seance;
import com.gestion.demo.model.Stagiaire;
import com.gestion.demo.repository.AbsenceRepository;
import com.gestion.demo.repository.ClasseRepository;
import com.gestion.demo.repository.SeanceRepository;
import com.gestion.demo.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/absences")
@RequiredArgsConstructor
public class AbsenceController {

    private final AbsenceRepository absenceRepository;
    private final StagiaireRepository stagiaireRepository;
    private final SeanceRepository seanceRepository;
    private final ClasseRepository classeRepository;

    // API pour afficher les stagiaires absents d'une classe
    @GetMapping("/classe/{classeId}/stagiaires-absents")
    public ResponseEntity<?> getStagiairesAbsentsByClasse(
            @PathVariable Long classeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            log.info("Début de la récupération des stagiaires absents pour la classe: {}", classeId);
            
            if (!classeRepository.existsById(classeId)) {
                log.error("Classe non trouvée avec l'ID: {}", classeId);
                return ResponseEntity.badRequest().body(createErrorResponse("Classe non trouvée"));
            }

            // Récupérer les absences
            List<Absence> absences;
            if (date != null) {
                log.info("Recherche des absences pour la date: {}", date);
                absences = absenceRepository.findByClasseIdAndDate(classeId, date);
            } else {
                log.info("Recherche de toutes les absences");
                absences = absenceRepository.findByClasseId(classeId);
            }

            // Log pour debug
            log.info("Nombre d'absences trouvées: {}", absences.size());
            
            // Vérifier directement les absences du stagiaire 3 pour debug
            List<Absence> absencesStagiaire3 = absenceRepository.findByStagiaireIdNative(3L);
            log.info("Absences du stagiaire 3: {}", absencesStagiaire3.size());
            absencesStagiaire3.forEach(a -> log.info("Absence stagiaire 3 - Date: {}, Séance: {}", 
                a.getDateAbsence(), a.getSeance().getId()));

            // Convertir les absences en DTO
            List<AbsenceDTO> absenceDTOs = absences.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

            // Préparer la réponse
            Map<String, Object> result = new HashMap<>();
            result.put("totalAbsences", absences.size());
            result.put("absences", absenceDTOs);
            result.put("dateRecherche", date != null ? date.toString() : "toutes dates");
            
            // Ajouter des informations de debug
            result.put("debug", Map.of(
                "stagiaire3Absences", absencesStagiaire3.stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList())
            ));

            return ResponseEntity.ok(createSuccessResponse("Absences récupérées avec succès", result));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des absences", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de la récupération des absences: " + e.getMessage()));
        }
    }

    // API pour afficher les stagiaires absents par classe et période
    @GetMapping("/classe/{classeId}/stagiaires-absents/periode")
    public ResponseEntity<?> getStagiairesAbsentsByClasseAndPeriod(
            @PathVariable Long classeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            log.info("Début de la récupération des stagiaires absents pour la classe: {} et la période: {} à {}", 
                classeId, startDate, endDate);
            
            if (!classeRepository.existsById(classeId)) {
                return ResponseEntity.badRequest().body(createErrorResponse("Classe non trouvée"));
            }

            List<Absence> absences = absenceRepository.findByClasseIdAndDateBetween(classeId, startDate, endDate);

            // Grouper les absences par stagiaire
            Map<Long, List<AbsenceDTO>> absencesByStagiaire = absences.stream()
                .map(this::toDTO)
                .collect(Collectors.groupingBy(AbsenceDTO::getStagiaireId));

            return ResponseEntity.ok(createSuccessResponse("Stagiaires absents récupérés avec succès", absencesByStagiaire));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des stagiaires absents", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de la récupération des stagiaires absents: " + e.getMessage()));
        }
    }

    // Ajouter une absence
    @PostMapping
    public ResponseEntity<?> createAbsence(@RequestBody AbsenceDTO dto) {
        try {
            log.info("Début de la création d'une absence");
            
            if (dto == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("Les données de l'absence sont requises"));
            }

            if (dto.getStagiaireId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("L'ID du stagiaire est requis"));
            }

            if (dto.getSeanceId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("L'ID de la séance est requis"));
            }

            Optional<Stagiaire> stagiaireOpt = stagiaireRepository.findById(dto.getStagiaireId());
            Optional<Seance> seanceOpt = seanceRepository.findById(dto.getSeanceId());
            
            if (stagiaireOpt.isEmpty() || seanceOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Stagiaire ou Séance non trouvée"));
            }

            Absence absence = new Absence();
            absence.setStagiaire(stagiaireOpt.get());
            absence.setSeance(seanceOpt.get());
            absence.setDateAbsence(dto.getDateAbsence() != null ? dto.getDateAbsence() : LocalDate.now());
            absence.setMotif(dto.getMotif());

            absence = absenceRepository.save(absence);
            return ResponseEntity.ok(createSuccessResponse("Absence créée avec succès", toDTO(absence)));

        } catch (Exception e) {
            log.error("Erreur lors de la création de l'absence", e);
            return ResponseEntity.status(500).body(createErrorResponse("Erreur lors de la création de l'absence: " + e.getMessage()));
        }
    }

    // Lister toutes les absences
    @GetMapping
    public ResponseEntity<?> getAllAbsences() {
        try {
            log.info("Début de la récupération des absences");
            
            List<Absence> absences = absenceRepository.findAll();
            List<AbsenceDTO> absenceDTOs = absences.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(createSuccessResponse("Absences récupérées avec succès", absenceDTOs));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des absences", e);
            return ResponseEntity.status(500).body(createErrorResponse("Erreur lors de la récupération des absences: " + e.getMessage()));
        }
    }

    // Voir une absence par son id
    @GetMapping("/{id}")
    public ResponseEntity<?> getAbsenceById(@PathVariable Long id) {
        try {
            log.info("Début de la récupération de l'absence avec l'id: {}", id);
            
            Optional<Absence> absenceOpt = absenceRepository.findById(id);
            if (absenceOpt.isEmpty()) {
                return ResponseEntity.status(404).body(createErrorResponse("Absence non trouvée"));
            }

            return ResponseEntity.ok(createSuccessResponse("Absence récupérée avec succès", toDTO(absenceOpt.get())));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de l'absence", e);
            return ResponseEntity.status(500).body(createErrorResponse("Erreur lors de la récupération de l'absence: " + e.getMessage()));
        }
    }

    // Modifier une absence
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAbsence(@PathVariable Long id, @RequestBody AbsenceDTO dto) {
        try {
            log.info("Début de la modification de l'absence avec l'id: {}", id);
            
            Optional<Absence> absenceOpt = absenceRepository.findById(id);
            if (absenceOpt.isEmpty()) {
                return ResponseEntity.status(404).body(createErrorResponse("Absence non trouvée"));
            }

            Absence absence = absenceOpt.get();

            if (dto.getStagiaireId() != null) {
                Optional<Stagiaire> stagiaireOpt = stagiaireRepository.findById(dto.getStagiaireId());
                if (stagiaireOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(createErrorResponse("Stagiaire non trouvé"));
                }
                absence.setStagiaire(stagiaireOpt.get());
            }

            if (dto.getSeanceId() != null) {
                Optional<Seance> seanceOpt = seanceRepository.findById(dto.getSeanceId());
                if (seanceOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(createErrorResponse("Séance non trouvée"));
                }
                absence.setSeance(seanceOpt.get());
            }

            if (dto.getDateAbsence() != null) {
                absence.setDateAbsence(dto.getDateAbsence());
            }

            if (dto.getMotif() != null) {
                absence.setMotif(dto.getMotif());
            }

            absence = absenceRepository.save(absence);
            return ResponseEntity.ok(createSuccessResponse("Absence modifiée avec succès", toDTO(absence)));

        } catch (Exception e) {
            log.error("Erreur lors de la modification de l'absence", e);
            return ResponseEntity.status(500).body(createErrorResponse("Erreur lors de la modification de l'absence: " + e.getMessage()));
        }
    }

    // API pour supprimer une absence
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAbsence(@PathVariable Long id) {
        try {
            log.info("Début de la suppression de l'absence avec l'id: {}", id);
            
            // Vérifier si l'absence existe
            if (!absenceRepository.existsById(id)) {
                log.error("Absence non trouvée avec l'ID: {}", id);
                return ResponseEntity.status(404)
                    .body(createErrorResponse("Absence non trouvée"));
            }

            // Récupérer l'absence pour les logs
            Optional<Absence> absenceOpt = absenceRepository.findById(id);
            if (absenceOpt.isPresent()) {
                Absence absence = absenceOpt.get();
                log.info("Suppression de l'absence - Stagiaire: {}, Date: {}, Séance: {}", 
                    absence.getStagiaire().getId(),
                    absence.getDateAbsence(),
                    absence.getSeance().getId());
            }

            // Supprimer l'absence
            absenceRepository.deleteById(id);
            
            return ResponseEntity.ok(createSuccessResponse("Absence supprimée avec succès", null));

        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'absence", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de la suppression de l'absence: " + e.getMessage()));
        }
    }

    // Méthode utilitaire pour convertir Absence en AbsenceDTO
    private AbsenceDTO toDTO(Absence absence) {
        if (absence == null) {
            return null;
        }

        AbsenceDTO dto = new AbsenceDTO();
        dto.setId(absence.getId());
        
        if (absence.getStagiaire() != null) {
            dto.setStagiaireId(absence.getStagiaire().getId());
            dto.setStagiaireNom(absence.getStagiaire().getNom());
            dto.setStagiairePrenom(absence.getStagiaire().getPrenom());
        }
        
        if (absence.getSeance() != null) {
            dto.setSeanceId(absence.getSeance().getId());
            dto.setSeanceNom(absence.getSeance().getNom());
            dto.setSeanceHeureDebut(absence.getSeance().getHeureDebut());
            dto.setSeanceHeureFin(absence.getSeance().getHeureFin());
        }
        
        dto.setDateAbsence(absence.getDateAbsence());
        dto.setMotif(absence.getMotif());
        dto.setFormattedDate(absence.getDateAbsence() != null ? absence.getDateAbsence().toString() : "");
        dto.setValid(true);
        
        return dto;
    }

    // Méthodes utilitaires pour créer les réponses
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
} 