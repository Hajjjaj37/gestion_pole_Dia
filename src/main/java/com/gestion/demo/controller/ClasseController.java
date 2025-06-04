package com.gestion.demo.controller;

import com.gestion.demo.dto.ClasseRequest;
import com.gestion.demo.model.Classe;
import com.gestion.demo.model.Formation;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.User;
import com.gestion.demo.model.Role;
import com.gestion.demo.repository.ClasseRepository;
import com.gestion.demo.repository.FormationRepository;
import com.gestion.demo.repository.FormateurRepository;
import com.gestion.demo.repository.UserRepository;
import com.gestion.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClasseController {

    private final ClasseRepository classeRepository;
    private final FormationRepository formationRepository;
    private final FormateurRepository formateurRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> createClasse(@RequestBody ClasseRequest request) {
        try {
            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom de la classe est requis"
                ));
            }

            if (request.getFormationId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "L'ID de la formation est requis"
                ));
            }

            // Validation des dates
            if (request.getDateDebut() != null && request.getDateFin() != null 
                && request.getDateDebut().after(request.getDateFin())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "La date de début doit être antérieure à la date de fin"
                ));
            }

            // Vérifier si la formation existe
            Formation formation = formationRepository.findById(request.getFormationId())
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'ID: " + request.getFormationId()));

            // Création de la classe
            Classe classe = new Classe();
            classe.setNom(request.getNom());
            classe.setDescription(request.getDescription());
            classe.setDateDebut(request.getDateDebut());
            classe.setDateFin(request.getDateFin());
            classe.setFormation(formation);

            // Sauvegarde de la classe
            Classe savedClasse = classeRepository.save(classe);

            // Préparation de la réponse
            Map<String, Object> classeData = new HashMap<>();
            classeData.put("id", savedClasse.getId());
            classeData.put("nom", savedClasse.getNom());
            classeData.put("description", savedClasse.getDescription());
            classeData.put("dateDebut", savedClasse.getDateDebut());
            classeData.put("dateFin", savedClasse.getDateFin());
            
            // Ajouter les informations de la formation
            Map<String, Object> formationData = new HashMap<>();
            formationData.put("id", formation.getId());
            formationData.put("nom", formation.getNom());
            formationData.put("description", formation.getDescription());
            formationData.put("duree", formation.getDuree());
            classeData.put("formation", formationData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Classe créée avec succès");
            response.put("data", classeData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la création de la classe", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la création de la classe: " + e.getMessage()
                ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllClasses() {
        try {
            List<Classe> classes = classeRepository.findAll();
            
            List<Map<String, Object>> classesData = new ArrayList<>();
            for (Classe classe : classes) {
                Map<String, Object> classeData = new HashMap<>();
                classeData.put("id", classe.getId());
                classeData.put("nom", classe.getNom());
                classeData.put("description", classe.getDescription());
                classeData.put("dateDebut", classe.getDateDebut());
                classeData.put("dateFin", classe.getDateFin());
                
                if (classe.getFormation() != null) {
                    Map<String, Object> formationData = new HashMap<>();
                    formationData.put("id", classe.getFormation().getId());
                    formationData.put("nom", classe.getFormation().getNom());
                    formationData.put("description", classe.getFormation().getDescription());
                    formationData.put("duree", classe.getFormation().getDuree());
                    classeData.put("formation", formationData);
                }
                
                classesData.add(classeData);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Classes récupérées avec succès");
            response.put("data", classesData);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des classes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la récupération des classes: " + e.getMessage()
                ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClasse(@PathVariable Long id, @RequestBody ClasseRequest request) {
        try {
            log.info("Début de la modification de la classe avec l'id: {}", id);
            
            // Vérifier si la classe existe
            Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'id: " + id));

            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom de la classe est requis"
                ));
            }

            // Validation des dates
            if (request.getDateDebut() != null && request.getDateFin() != null 
                && request.getDateDebut().after(request.getDateFin())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "La date de début doit être antérieure à la date de fin"
                ));
            }

            // Mise à jour des champs
            classe.setNom(request.getNom().trim());
            classe.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
            classe.setDateDebut(request.getDateDebut());
            classe.setDateFin(request.getDateFin());

            // Mise à jour de la formation si fournie
            if (request.getFormationId() != null) {
                Formation formation = formationRepository.findById(request.getFormationId())
                    .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'ID: " + request.getFormationId()));
                classe.setFormation(formation);
            }

            // Sauvegarder les modifications
            classe = classeRepository.save(classe);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Classe modifiée avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", classe.getId());
            data.put("nom", classe.getNom());
            data.put("description", classe.getDescription());
            data.put("dateDebut", classe.getDateDebut());
            data.put("dateFin", classe.getDateFin());
            
            // Ajouter les informations de la formation
            if (classe.getFormation() != null) {
                Map<String, Object> formationData = new HashMap<>();
                formationData.put("id", classe.getFormation().getId());
                formationData.put("nom", classe.getFormation().getNom());
                formationData.put("description", classe.getFormation().getDescription());
                formationData.put("duree", classe.getFormation().getDuree());
                data.put("formation", formationData);
            }
            
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erreur lors de la modification de la classe", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la modification de la classe", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la modification de la classe: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getClasseById(@PathVariable Long id) {
        try {
            log.info("=== DÉBUT DE LA RÉCUPÉRATION DE LA CLASSE ===");
            log.info("Recherche de la classe avec l'ID: {}", id);

            // Vérifier si la classe existe
            Classe classe = classeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'ID: " + id));

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Classe récupérée avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", classe.getId());
            data.put("nom", classe.getNom());
            data.put("description", classe.getDescription());
            data.put("dateDebut", classe.getDateDebut());
            data.put("dateFin", classe.getDateFin());
            
            // Ajouter les informations de la formation
            if (classe.getFormation() != null) {
                Map<String, Object> formationData = new HashMap<>();
                formationData.put("id", classe.getFormation().getId());
                formationData.put("nom", classe.getFormation().getNom());
                formationData.put("description", classe.getFormation().getDescription());
                formationData.put("duree", classe.getFormation().getDuree());
                data.put("formation", formationData);
            }
            
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération de la classe", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la classe", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la récupération de la classe: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClasse(@PathVariable Long id) {
        try {
            log.info("Début de la suppression de la classe avec l'id: {}", id);
            
            // Vérifier si la classe existe
            Classe classe = classeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'id: " + id));

            // 1. Supprimer les stagiaires associés à la classe
            classeRepository.deleteStagiairesFromClasse(id);

            // 2. Supprimer les associations formateur-classe
            classeRepository.deleteFormateurClasses(id);

            // 3. Supprimer la classe
            classeRepository.deleteClasseById(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Classe supprimée avec succès"
            ));

        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de la classe", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la classe", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la suppression de la classe: " + e.getMessage()
            ));
        }
    }
} 