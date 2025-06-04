package com.gestion.demo.controller;

import com.gestion.demo.dto.FormationRequest;
import com.gestion.demo.model.Formation;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.Module;
import com.gestion.demo.model.User;
import com.gestion.demo.model.Role;
import com.gestion.demo.repository.FormationRepository;
import com.gestion.demo.repository.FormateurRepository;
import com.gestion.demo.repository.ModuleRepository;
import com.gestion.demo.repository.UserRepository;
import com.gestion.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.hibernate.Hibernate;

import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;

@Slf4j
@RestController
@RequestMapping("/api/formations")
@RequiredArgsConstructor
public class FormationController {

    private final FormationRepository formationRepository;
    private final ModuleRepository moduleRepository;

    @PostMapping
    public ResponseEntity<?> createFormation(@RequestBody FormationRequest request) {
        try {
            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom de la formation est requis"
                ));
            }

            // Création de la formation
            Formation formation = new Formation();
            formation.setNom(request.getNom());
            formation.setDescription(request.getDescription());
            formation.setDuree(request.getDuree());

            // Récupération et association des modules
            if (request.getModuleIds() != null && !request.getModuleIds().isEmpty()) {
                Set<Module> modules = new HashSet<>(moduleRepository.findAllById(request.getModuleIds()));
                formation.setModules(modules);
            }

            // Sauvegarde de la formation
            Formation savedFormation = formationRepository.save(formation);

            // Préparation de la réponse
            Map<String, Object> formationData = new HashMap<>();
            formationData.put("id", savedFormation.getId());
            formationData.put("nom", savedFormation.getNom());
            formationData.put("description", savedFormation.getDescription());
            formationData.put("duree", savedFormation.getDuree());

            // Conversion des modules en liste de maps
            List<Map<String, Object>> modulesData = savedFormation.getModules().stream()
                .map(module -> {
                    Map<String, Object> moduleData = new HashMap<>();
                    moduleData.put("id", module.getId());
                    moduleData.put("nom", module.getNom());
                    moduleData.put("description", module.getDescription());
                    moduleData.put("duree", module.getDuree());
                    return moduleData;
                })
                .collect(Collectors.toList());

            formationData.put("modules", modulesData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Formation créée avec succès");
            response.put("data", formationData);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la création de la formation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getFormationById(@PathVariable Long id) {
        try {
            log.info("=== DÉBUT DE LA RÉCUPÉRATION DE LA FORMATION ===");
            log.info("Recherche de la formation avec l'ID: {}", id);

            Formation formation = formationRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'ID: " + id));

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Formation récupérée avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", formation.getId());
            data.put("nom", formation.getNom());
            data.put("description", formation.getDescription());
            data.put("duree", formation.getDuree());
            
            // Ajouter les modules
            List<Map<String, Object>> modulesData = new ArrayList<>();
            for (Module module : formation.getModules()) {
                Map<String, Object> moduleData = new HashMap<>();
                moduleData.put("id", module.getId());
                moduleData.put("nom", module.getNom());
                moduleData.put("description", module.getDescription());
                moduleData.put("duree", module.getDuree());
                modulesData.add(moduleData);
            }
            data.put("modules", modulesData);
            
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la formation", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", "Erreur lors de la récupération de la formation: " + e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllFormations() {
        try {
            log.info("Début de la récupération des formations");
            
            List<Object[]> results = formationRepository.findAllFormationsWithModules();
            log.info("Nombre de résultats trouvés: {}", results != null ? results.size() : 0);
            
            if (results == null || results.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Aucune formation trouvée",
                    "data", new ArrayList<>()
                ));
            }

            Map<Long, Map<String, Object>> formationsMap = new HashMap<>();
            
            for (Object[] row : results) {
                try {
                    // Vérifier que les objets ne sont pas null avant de les convertir
                    if (row[0] != null) {
                        Long formationId = ((Number) row[0]).longValue();
                        String formationNom = String.valueOf(row[1]);
                        String formationDescription = String.valueOf(row[2]);
                        Integer formationDuree = row[3] != null ? ((Number) row[3]).intValue() : null;
                        
                        // Créer ou récupérer la formation
                        Map<String, Object> formationData = formationsMap.computeIfAbsent(formationId, k -> {
                            Map<String, Object> data = new HashMap<>();
                            data.put("id", formationId);
                            data.put("nom", formationNom);
                            data.put("description", formationDescription);
                            data.put("duree", formationDuree);
                            data.put("modules", new ArrayList<>());
                            return data;
                        });
                        
                        // Ajouter le module s'il existe
                        if (row[4] != null) { // module_id
                            Map<String, Object> moduleData = new HashMap<>();
                            moduleData.put("id", ((Number) row[4]).longValue());
                            moduleData.put("nom", String.valueOf(row[5]));
                            moduleData.put("description", String.valueOf(row[6]));
                            moduleData.put("duree", row[7] != null ? ((Number) row[7]).intValue() : null);
                            
                            ((List<Map<String, Object>>) formationData.get("modules")).add(moduleData);
                        }
                    }
                } catch (Exception e) {
                    log.error("Erreur lors du traitement d'une ligne: {}", e.getMessage());
                }
            }

            List<Map<String, Object>> formationsData = new ArrayList<>(formationsMap.values());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Formations récupérées avec succès");
            response.put("data", formationsData);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des formations", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la récupération des formations: " + e.getMessage()
                ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFormation(@PathVariable Long id) {
        try {
            log.info("Début de la suppression de la formation avec l'id: {}", id);
            
            // Vérifier si la formation existe
            Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'id: " + id));

            // 1. Supprimer les associations avec les modules
            formationRepository.deleteFormationModules(id);

            // 2. Supprimer les stagiaires des classes associées
            formationRepository.deleteStagiairesFromClasses(id);

            // 3. Supprimer les associations formateur-classe
            formationRepository.deleteFormateurClasses(id);

            // 4. Supprimer les classes associées
            formationRepository.deleteFormationClasses(id);

            // 5. Supprimer la formation
            formationRepository.deleteFormationById(id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Formation supprimée avec succès"
            ));

        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression de la formation", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la formation", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la suppression de la formation: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFormation(@PathVariable Long id, @RequestBody FormationRequest request) {
        try {
            log.info("Début de la modification de la formation avec l'id: {}", id);
            
            // Vérifier si la formation existe
            Formation formation = formationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formation non trouvée avec l'id: " + id));

            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom de la formation est requis"
                ));
            }

            // Mise à jour des champs
            formation.setNom(request.getNom().trim());
            formation.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
            formation.setDuree(request.getDuree());

            // Mise à jour des modules
            if (request.getModuleIds() != null) {
                Set<Module> modules = new HashSet<>(moduleRepository.findAllById(request.getModuleIds()));
                formation.setModules(modules);
            }

            // Sauvegarder les modifications
            formation = formationRepository.save(formation);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Formation modifiée avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", formation.getId());
            data.put("nom", formation.getNom());
            data.put("description", formation.getDescription());
            data.put("duree", formation.getDuree());
            
            // Ajouter les modules
            List<Map<String, Object>> modulesData = formation.getModules().stream()
                .map(module -> {
                    Map<String, Object> moduleData = new HashMap<>();
                    moduleData.put("id", module.getId());
                    moduleData.put("nom", module.getNom());
                    moduleData.put("description", module.getDescription());
                    moduleData.put("duree", module.getDuree());
                    return moduleData;
                })
                .collect(Collectors.toList());
            
            data.put("modules", modulesData);
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erreur lors de la modification de la formation", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la modification de la formation", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la modification de la formation: " + e.getMessage()
            ));
        }
    }
} 