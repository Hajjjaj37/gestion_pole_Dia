package com.gestion.demo.controller;

import com.gestion.demo.dto.SalleRequest;
import com.gestion.demo.model.Salle;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.repository.SalleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/salles")
@RequiredArgsConstructor
public class SalleController {

    private final SalleRepository salleRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> createSalle(@RequestBody SalleRequest request) {
        try {
            log.info("Début de la création de la salle");
            
            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Le nom de la salle est requis");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getNumero() == null || request.getNumero().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Le numéro de la salle est requis");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getCapacite() == null || request.getCapacite() <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "La capacité doit être supérieure à 0");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Vérifier si le nom existe déjà
            if (salleRepository.existsByNom(request.getNom())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Une salle avec ce nom existe déjà");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Vérifier si le numéro existe déjà
            if (salleRepository.existsByNumero(request.getNumero())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Une salle avec ce numéro existe déjà");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Créer la salle
            Salle salle = new Salle();
            salle.setNom(request.getNom().trim());
            salle.setNumero(request.getNumero().trim());
            salle.setDescription(request.getDescription() != null ? request.getDescription() : "");
            salle.setCapacite(request.getCapacite());
            salle.setEquipement(request.getEquipement() != null ? request.getEquipement() : "");

            // Sauvegarder la salle
            salle = salleRepository.save(salle);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Salle créée avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", salle.getId());
            data.put("nom", salle.getNom());
            data.put("numero", salle.getNumero());
            data.put("description", salle.getDescription());
            data.put("capacite", salle.getCapacite());
            data.put("equipement", salle.getEquipement());
            
            response.put("data", data);

            log.info("Salle créée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la création de la salle", e);
            return ResponseEntity.status(500).body(new HashMap<String, Object>() {{
                put("success", false);
                put("message", "Erreur lors de la création de la salle: " + e.getMessage());
            }});
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATEUR')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllSalles() {
        try {
            log.info("Début de la récupération des salles");
            
            List<Salle> salles = salleRepository.findAllWithFormateurs();
            if (salles == null || salles.isEmpty()) {
                return ResponseEntity.ok(new HashMap<String, Object>() {{
                    put("success", true);
                    put("message", "Aucune salle trouvée");
                    put("data", new ArrayList<>());
                }});
            }

            log.info("Nombre de salles trouvées : {}", salles.size());

            List<Map<String, Object>> sallesData = new ArrayList<>();
            
            for (Salle salle : salles) {
                Map<String, Object> salleData = new HashMap<>();
                salleData.put("id", salle.getId());
                salleData.put("nom", salle.getNom());
                salleData.put("numero", salle.getNumero());
                salleData.put("description", salle.getDescription());
                salleData.put("capacite", salle.getCapacite());
                salleData.put("equipement", salle.getEquipement());
                
                // Add debug logging
                log.info("Salle {} - Nombre de formateurs: {}", salle.getId(), 
                        salle.getFormateurs() != null ? salle.getFormateurs().size() : 0);
                
                List<Map<String, Object>> formateursData = new ArrayList<>();
                if (salle.getFormateurs() != null) {
                    salle.getFormateurs().forEach(formateur -> {
                        log.info("Formateur trouvé - ID: {}, Nom: {}", formateur.getId(), formateur.getNom());
                        Map<String, Object> formateurData = new HashMap<>();
                        formateurData.put("id", formateur.getId());
                        formateurData.put("nom", formateur.getNom());
                        formateurData.put("prenom", formateur.getPrenom());
                        formateursData.add(formateurData);
                    });
                }
                salleData.put("formateurs", formateursData);
                sallesData.add(salleData);
            }

            log.info("Salles récupérées avec succès");
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("message", "Salles récupérées avec succès");
                put("data", sallesData);
            }});
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des salles", e);
            return ResponseEntity.status(500).body(new HashMap<String, Object>() {{
                put("success", false);
                put("message", "Erreur lors de la récupération des salles: " + e.getMessage());
            }});
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATEUR')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getSalleById(@PathVariable Long id) {
        try {
            log.info("Début de la récupération de la salle avec l'id: {}", id);
            
            Optional<Salle> salleOpt = salleRepository.findByIdWithFormateurs(id);
            if (salleOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Salle non trouvée avec l'id: " + id);
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            Salle salle = salleOpt.get();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Salle récupérée avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", salle.getId());
            data.put("nom", salle.getNom());
            data.put("numero", salle.getNumero());
            data.put("description", salle.getDescription());
            data.put("capacite", salle.getCapacite());
            data.put("equipement", salle.getEquipement());
            
            // Handle formateurs safely
            List<Map<String, Object>> formateursData = new ArrayList<>();
            if (salle.getFormateurs() != null) {
                salle.getFormateurs().forEach(formateur -> {
                    Map<String, Object> formateurData = new HashMap<>();
                    formateurData.put("id", formateur.getId());
                    formateurData.put("nom", formateur.getNom());
                    formateurData.put("prenom", formateur.getPrenom());
                    formateursData.add(formateurData);
                });
            }
            data.put("formateurs", formateursData);
            response.put("data", data);

            log.info("Salle récupérée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la salle", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération de la salle: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateSalle(@PathVariable Long id, @RequestBody SalleRequest request) {
        try {
            log.info("Début de la modification de la salle avec l'id: {}", id);
            
            // Vérifier si la salle existe
            Optional<Salle> salleOpt = salleRepository.findById(id);
            if (salleOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Salle non trouvée avec l'id: " + id);
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            Salle salle = salleOpt.get();

            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Le nom de la salle est requis");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getNumero() == null || request.getNumero().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Le numéro de la salle est requis");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (request.getCapacite() == null || request.getCapacite() <= 0) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "La capacité doit être supérieure à 0");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Vérifier si le nom existe déjà pour une autre salle
            if (!salle.getNom().equals(request.getNom()) && 
                salleRepository.existsByNom(request.getNom())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Une salle avec ce nom existe déjà");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Vérifier si le numéro existe déjà pour une autre salle
            if (!salle.getNumero().equals(request.getNumero()) && 
                salleRepository.existsByNumero(request.getNumero())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Une salle avec ce numéro existe déjà");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Mise à jour des informations de la salle
            salle.setNom(request.getNom().trim());
            salle.setNumero(request.getNumero().trim());
            salle.setDescription(request.getDescription() != null ? request.getDescription() : "");
            salle.setCapacite(request.getCapacite());
            salle.setEquipement(request.getEquipement() != null ? request.getEquipement() : "");

            // Sauvegarder les modifications
            salle = salleRepository.save(salle);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Salle modifiée avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", salle.getId());
            data.put("nom", salle.getNom());
            data.put("numero", salle.getNumero());
            data.put("description", salle.getDescription());
            data.put("capacite", salle.getCapacite());
            data.put("equipement", salle.getEquipement());
            
            // Ajouter les formateurs de manière sécurisée
            List<Map<String, Object>> formateursData = new ArrayList<>();
            if (salle.getFormateurs() != null) {
                salle.getFormateurs().forEach(formateur -> {
                    Map<String, Object> formateurData = new HashMap<>();
                    formateurData.put("id", formateur.getId());
                    formateurData.put("nom", formateur.getNom());
                    formateurData.put("prenom", formateur.getPrenom());
                    formateursData.add(formateurData);
                });
            }
            data.put("formateurs", formateursData);
            
            response.put("data", data);

            log.info("Salle modifiée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la modification de la salle", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la modification de la salle: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
} 