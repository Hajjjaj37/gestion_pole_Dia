package com.gestion.demo.controller;

import com.gestion.demo.model.Seance;
import com.gestion.demo.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/seances")
@RequiredArgsConstructor
public class SeanceController {

    private final SeanceRepository seanceRepository;

    @PostMapping
    public ResponseEntity<?> createSeance(@RequestBody Seance seance) {
        try {
            log.info("Début de la création de la séance");
            
            // Validation des données
            if (seance.getNom() == null || seance.getNom().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Le nom de la séance est requis");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (seance.getPeriode() == null || seance.getPeriode().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "La période est requise");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (seance.getNumero() == null || seance.getNumero() < 1 || seance.getNumero() > 4) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Le numéro de séance doit être entre 1 et 4");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (seance.getHeureDebut() == null || seance.getHeureDebut().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "L'heure de début est requise");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (seance.getHeureFin() == null || seance.getHeureFin().trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "L'heure de fin est requise");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Sauvegarder la séance
            seance = seanceRepository.save(seance);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Séance créée avec succès");
            response.put("data", seance);

            log.info("Séance créée avec succès");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la création de la séance", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la création de la séance: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSeances() {
        try {
            log.info("Début de la récupération des séances");
            
            List<Seance> seances = seanceRepository.findAll();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Séances récupérées avec succès");
            response.put("data", seances);

            log.info("Séances récupérées avec succès");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des séances", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération des séances: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getSeanceById(@PathVariable Long id) {
        try {
            log.info("Début de la récupération de la séance avec l'id: {}", id);
            
            Optional<Seance> seanceOpt = seanceRepository.findById(id);
            if (seanceOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Séance non trouvée avec l'id: " + id);
                return ResponseEntity.status(404).body(errorResponse);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Séance récupérée avec succès");
            response.put("data", seanceOpt.get());

            log.info("Séance récupérée avec succès");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération de la séance", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération de la séance: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSeance(@PathVariable Long id, @RequestBody Seance seance) {
        try {
            log.info("Début de la modification de la séance avec l'id: {}", id);
            
            Optional<Seance> seanceOpt = seanceRepository.findById(id);
            if (seanceOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Séance non trouvée avec l'id: " + id);
                return ResponseEntity.status(404).body(errorResponse);
            }

            Seance existingSeance = seanceOpt.get();
            
            // Mise à jour des champs
            existingSeance.setNom(seance.getNom());
            existingSeance.setPeriode(seance.getPeriode());
            existingSeance.setNumero(seance.getNumero());
            existingSeance.setHeureDebut(seance.getHeureDebut());
            existingSeance.setHeureFin(seance.getHeureFin());

            // Sauvegarder les modifications
            existingSeance = seanceRepository.save(existingSeance);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Séance modifiée avec succès");
            response.put("data", existingSeance);

            log.info("Séance modifiée avec succès");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la modification de la séance", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la modification de la séance: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSeance(@PathVariable Long id) {
        try {
            log.info("Début de la suppression de la séance avec l'id: {}", id);
            
            if (!seanceRepository.existsById(id)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Séance non trouvée avec l'id: " + id);
                return ResponseEntity.status(404).body(errorResponse);
            }

            seanceRepository.deleteById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Séance supprimée avec succès");

            log.info("Séance supprimée avec succès");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de la séance", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la suppression de la séance: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
} 