package com.gestion.demo.controller;

import com.gestion.demo.dto.StagiaireRequest;
import com.gestion.demo.model.Classe;
import com.gestion.demo.model.Stagiaire;
import com.gestion.demo.repository.ClasseRepository;
import com.gestion.demo.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stagiaires")
@RequiredArgsConstructor
@Slf4j
public class StagiaireController {

    private final StagiaireRepository stagiaireRepository;
    private final ClasseRepository classeRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> createStagiaire(@RequestBody StagiaireRequest request) {
        try {
            log.info("Début de la création du stagiaire");
            
            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom du stagiaire est requis"
                ));
            }

            if (request.getClasseId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "L'ID de la classe est requis"
                ));
            }

            // Vérifier si la classe existe
            Classe classe = classeRepository.findById(request.getClasseId())
                .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'ID: " + request.getClasseId()));

            // Vérifier si l'email existe déjà
            if (request.getEmail() != null && stagiaireRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email déjà utilisé"
                ));
            }

            // Créer le stagiaire
            Stagiaire stagiaire = new Stagiaire();
            stagiaire.setNom(request.getNom().trim());
            stagiaire.setPrenom(request.getPrenom() != null ? request.getPrenom().trim() : "");
            stagiaire.setEmail(request.getEmail());
            stagiaire.setTelephone(request.getTelephone());

            // Gestion de la date de naissance
            if (request.getDateNaissance() != null) {
                stagiaire.setDateNaissance(request.getDateNaissance());
            }

            stagiaire.setClasse(classe);

            // Sauvegarder le stagiaire
            stagiaire = stagiaireRepository.save(stagiaire);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stagiaire créé avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", stagiaire.getId());
            data.put("nom", stagiaire.getNom());
            data.put("prenom", stagiaire.getPrenom());
            data.put("email", stagiaire.getEmail());
            data.put("telephone", stagiaire.getTelephone());
            data.put("dateNaissance", stagiaire.getDateNaissance());
            
            // Ajouter les informations de la classe
            Map<String, Object> classeData = new HashMap<>();
            classeData.put("id", classe.getId());
            classeData.put("nom", classe.getNom());
            classeData.put("description", classe.getDescription());
            classeData.put("dateDebut", classe.getDateDebut());
            classeData.put("dateFin", classe.getDateFin());
            
            // Ajouter les informations de la formation
            if (classe.getFormation() != null) {
                Map<String, Object> formationData = new HashMap<>();
                formationData.put("id", classe.getFormation().getId());
                formationData.put("nom", classe.getFormation().getNom());
                formationData.put("description", classe.getFormation().getDescription());
                formationData.put("duree", classe.getFormation().getDuree());
                classeData.put("formation", formationData);
            }
            
            data.put("classe", classeData);
            
            response.put("data", data);

            log.info("Stagiaire créé avec succès");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erreur lors de la création du stagiaire: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la création du stagiaire", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la création du stagiaire: " + e.getMessage()
                ));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATEUR')")
    public ResponseEntity<?> getAllStagiaires() {
        try {
            List<Stagiaire> stagiaires = stagiaireRepository.findAll();
            
            List<Map<String, Object>> stagiairesData = new ArrayList<>();
            for (Stagiaire stagiaire : stagiaires) {
                Map<String, Object> stagiaireData = new HashMap<>();
                stagiaireData.put("id", stagiaire.getId());
                stagiaireData.put("nom", stagiaire.getNom());
                stagiaireData.put("prenom", stagiaire.getPrenom());
                stagiaireData.put("email", stagiaire.getEmail());
                stagiaireData.put("telephone", stagiaire.getTelephone());
                stagiaireData.put("dateNaissance", stagiaire.getDateNaissance());
                
                // Ajouter les informations de la classe
                if (stagiaire.getClasse() != null) {
                    Map<String, Object> classeData = new HashMap<>();
                    classeData.put("id", stagiaire.getClasse().getId());
                    classeData.put("nom", stagiaire.getClasse().getNom());
                    stagiaireData.put("classe", classeData);
                }
                
                stagiairesData.add(stagiaireData);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stagiaires récupérés avec succès",
                "data", stagiairesData
            ));
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des stagiaires", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la récupération des stagiaires: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/classe/{classeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATEUR')")
    public ResponseEntity<?> getStagiairesByClasse(@PathVariable Long classeId) {
        try {
            // Vérifier si la classe existe
            Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'ID: " + classeId));

            // Récupérer les stagiaires de la classe
            List<Stagiaire> stagiaires = stagiaireRepository.findByClasseId(classeId);
            
            List<Map<String, Object>> stagiairesData = new ArrayList<>();
            for (Stagiaire stagiaire : stagiaires) {
                Map<String, Object> stagiaireData = new HashMap<>();
                stagiaireData.put("id", stagiaire.getId());
                stagiaireData.put("nom", stagiaire.getNom());
                stagiaireData.put("prenom", stagiaire.getPrenom());
                stagiaireData.put("email", stagiaire.getEmail());
                stagiaireData.put("telephone", stagiaire.getTelephone());
                stagiaireData.put("dateNaissance", stagiaire.getDateNaissance());
                
                stagiairesData.add(stagiaireData);
            }

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stagiaires récupérés avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("classe", Map.of(
                "id", classe.getId(),
                "nom", classe.getNom(),
                "description", classe.getDescription(),
                "dateDebut", classe.getDateDebut(),
                "dateFin", classe.getDateFin()
            ));
            data.put("stagiaires", stagiairesData);
            
            response.put("data", data);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des stagiaires", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la récupération des stagiaires: " + e.getMessage()
                ));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> updateStagiaire(@PathVariable Long id, @RequestBody StagiaireRequest request) {
        try {
            log.info("Début de la modification du stagiaire avec l'id: {}", id);
            
            // Vérifier si le stagiaire existe
            Stagiaire stagiaire = stagiaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé avec l'id: " + id));

            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom du stagiaire est requis"
                ));
            }

            // Vérifier si l'email existe déjà pour un autre stagiaire
            if (!stagiaire.getEmail().equals(request.getEmail()) && 
                stagiaireRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email déjà utilisé par un autre stagiaire"
                ));
            }

            // Mise à jour des informations du stagiaire
            stagiaire.setNom(request.getNom().trim());
            stagiaire.setPrenom(request.getPrenom() != null ? request.getPrenom().trim() : "");
            stagiaire.setEmail(request.getEmail());
            stagiaire.setTelephone(request.getTelephone());
            
            // Mise à jour de la date de naissance si fournie
            if (request.getDateNaissance() != null) {
                stagiaire.setDateNaissance(request.getDateNaissance());
            }

            // Mise à jour de la classe si fournie
            if (request.getClasseId() != null) {
                Classe classe = classeRepository.findById(request.getClasseId())
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'ID: " + request.getClasseId()));
                stagiaire.setClasse(classe);
            }

            // Sauvegarder les modifications
            stagiaire = stagiaireRepository.save(stagiaire);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Stagiaire modifié avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", stagiaire.getId());
            data.put("nom", stagiaire.getNom());
            data.put("prenom", stagiaire.getPrenom());
            data.put("email", stagiaire.getEmail());
            data.put("telephone", stagiaire.getTelephone());
            data.put("dateNaissance", stagiaire.getDateNaissance());
            
            // Ajouter les informations de la classe
            if (stagiaire.getClasse() != null) {
                Map<String, Object> classeData = new HashMap<>();
                classeData.put("id", stagiaire.getClasse().getId());
                classeData.put("nom", stagiaire.getClasse().getNom());
                classeData.put("description", stagiaire.getClasse().getDescription());
                classeData.put("dateDebut", stagiaire.getClasse().getDateDebut());
                classeData.put("dateFin", stagiaire.getClasse().getDateFin());
                
                // Ajouter les informations de la formation
                if (stagiaire.getClasse().getFormation() != null) {
                    Map<String, Object> formationData = new HashMap<>();
                    formationData.put("id", stagiaire.getClasse().getFormation().getId());
                    formationData.put("nom", stagiaire.getClasse().getFormation().getNom());
                    formationData.put("description", stagiaire.getClasse().getFormation().getDescription());
                    formationData.put("duree", stagiaire.getClasse().getFormation().getDuree());
                    classeData.put("formation", formationData);
                }
                
                data.put("classe", classeData);
            }
            
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erreur lors de la modification du stagiaire: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la modification du stagiaire", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la modification du stagiaire: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteStagiaire(@PathVariable Long id) {
        try {
            log.info("Début de la suppression du stagiaire avec l'id: {}", id);
            
            // Vérifier si le stagiaire existe
            Stagiaire stagiaire = stagiaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé avec l'id: " + id));

            // Supprimer le stagiaire
            stagiaireRepository.delete(stagiaire);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Stagiaire supprimé avec succès"
            ));

        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression du stagiaire: {}", e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du stagiaire", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la suppression du stagiaire: " + e.getMessage()
            ));
        }
    }
} 