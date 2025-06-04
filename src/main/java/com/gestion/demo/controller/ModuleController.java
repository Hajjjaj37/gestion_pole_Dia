package com.gestion.demo.controller;

import com.gestion.demo.dto.ModuleRequest;
import com.gestion.demo.model.Module;
import com.gestion.demo.model.User;
import com.gestion.demo.model.Role;
import com.gestion.demo.repository.ModuleRepository;
import com.gestion.demo.repository.UserRepository;
import com.gestion.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<?> createModule(@RequestBody ModuleRequest request) {
        try {
            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom du module est requis"
                ));
            }

            Module module = new Module();
            module.setNom(request.getNom().trim());
            module.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
            module.setDuree(request.getDuree());

            module = moduleRepository.save(module);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Module créé avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", module.getId());
            data.put("nom", module.getNom());
            data.put("description", module.getDescription());
            data.put("duree", module.getDuree());
            
            response.put("data", data);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Erreur lors de la création du module", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la création du module: " + e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllModules() {
        try {
            log.info("Début de la récupération des modules");
            List<Module> modules = moduleRepository.findAll();
            
            if (modules.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Aucun module trouvé",
                    "data", new ArrayList<>()
                ));
            }

            List<Map<String, Object>> modulesData = modules.stream()
                .map(module -> {
                    Map<String, Object> moduleMap = new HashMap<>();
                    moduleMap.put("id", module.getId());
                    moduleMap.put("nom", module.getNom());
                    moduleMap.put("description", module.getDescription());
                    moduleMap.put("duree", module.getDuree());
                    return moduleMap;
                })
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Modules récupérés avec succès");
            response.put("data", modulesData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des modules", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la récupération des modules: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateModule(@PathVariable Long id, @RequestBody ModuleRequest request) {
        try {
            log.info("Début de la modification du module avec l'id: {}", id);
            
            // Vérifier si le module existe
            Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'id: " + id));

            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom du module est requis"
                ));
            }

            // Mise à jour des champs
            module.setNom(request.getNom().trim());
            module.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
            module.setDuree(request.getDuree());

            // Sauvegarder les modifications
            module = moduleRepository.save(module);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Module modifié avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", module.getId());
            data.put("nom", module.getNom());
            data.put("description", module.getDescription());
            data.put("duree", module.getDuree());
            
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erreur lors de la modification du module", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la modification du module", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la modification du module: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getModuleById(@PathVariable Long id) {
        try {
            log.info("Début de la récupération du module avec l'id: {}", id);
            
            // Vérifier si le module existe
            Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'id: " + id));

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Module récupéré avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", module.getId());
            data.put("nom", module.getNom());
            data.put("description", module.getDescription());
            data.put("duree", module.getDuree());
            
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erreur lors de la récupération du module", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du module", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la récupération du module: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Long id) {
        try {
            log.info("Début de la suppression du module avec l'id: {}", id);
            
            // Vérifier si le module existe
            Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Module non trouvé avec l'id: " + id));

            // Supprimer d'abord les associations dans la table formation_module
            moduleRepository.deleteModuleAssociations(id);

            // Supprimer le module
            moduleRepository.delete(module);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Module supprimé avec succès"
            ));

        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression du module", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du module", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la suppression du module: " + e.getMessage()
            ));
        }
    }
} 