package com.gestion.demo.controller;

import com.gestion.demo.model.Attestation;
import com.gestion.demo.model.Stagiaire;
import com.gestion.demo.model.Gestionnaire;
import com.gestion.demo.repository.AttestationRepository;
import com.gestion.demo.repository.StagiaireRepository;
import com.gestion.demo.repository.GestionnaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attestations")
@RequiredArgsConstructor
public class AttestationController {
    private final AttestationRepository attestationRepository;
    private final StagiaireRepository stagiaireRepository;
    private final GestionnaireRepository gestionnaireRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<?> createAttestation(@RequestBody Map<String, Object> request) {
        try {
            // 1. Extraction des données
            String titre = (String) request.get("titre");
            String contenu = (String) request.get("contenu");
            String dateExpiration = (String) request.get("dateExpiration");
            Long stagiaireId = Long.valueOf(request.get("stagiaireId").toString());
            Long gestionnaireId = Long.valueOf(request.get("gestionnaireId").toString());

            // 2. Validation des champs obligatoires
            if (titre == null || titre.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le titre est obligatoire"));
            }

            if (stagiaireId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "L'ID du stagiaire est obligatoire"));
            }

            if (gestionnaireId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "L'ID du gestionnaire est obligatoire"));
            }

            // 3. Vérification du stagiaire
            Stagiaire stagiaire = stagiaireRepository.findById(stagiaireId)
                .orElseThrow(() -> new IllegalArgumentException("Stagiaire non trouvé avec l'ID: " + stagiaireId));

            // 4. Vérification du gestionnaire
            Gestionnaire gestionnaire = gestionnaireRepository.findById(gestionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("Gestionnaire non trouvé avec l'ID: " + gestionnaireId));

            // 5. Création de l'attestation
            Attestation attestation = new Attestation();
            attestation.setTitre(titre.trim());
            attestation.setContenu(contenu != null ? contenu.trim() : "");
            attestation.setDateEmission(LocalDate.now());
            attestation.setDateExpiration(dateExpiration != null ? LocalDate.parse(dateExpiration) : null);
            attestation.setStatut(Attestation.StatutAttestation.EN_ATTENTE);
            attestation.setStagiaire(stagiaire);
            attestation.setGestionnaire(gestionnaire);

            // 6. Sauvegarde de l'attestation
            Attestation savedAttestation = attestationRepository.save(attestation);
            
            // 7. Construction de la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Attestation créée avec succès");
            
            Map<String, Object> attestationResponse = new HashMap<>();
            attestationResponse.put("id", savedAttestation.getId());
            attestationResponse.put("titre", savedAttestation.getTitre());
            attestationResponse.put("contenu", savedAttestation.getContenu());
            attestationResponse.put("dateEmission", savedAttestation.getDateEmission());
            attestationResponse.put("dateExpiration", savedAttestation.getDateExpiration());
            attestationResponse.put("statut", savedAttestation.getStatut());
            
            // Informations du stagiaire
            Map<String, Object> stagiaireInfo = new HashMap<>();
            stagiaireInfo.put("id", stagiaire.getId());
            stagiaireInfo.put("nom", stagiaire.getNom());
            stagiaireInfo.put("prenom", stagiaire.getPrenom());
            stagiaireInfo.put("email", stagiaire.getEmail());
            attestationResponse.put("stagiaire", stagiaireInfo);
            
            // Informations du gestionnaire
            Map<String, Object> gestionnaireInfo = new HashMap<>();
            gestionnaireInfo.put("id", gestionnaire.getId());
            gestionnaireInfo.put("nom", gestionnaire.getNom());
            gestionnaireInfo.put("prenom", gestionnaire.getPrenom());
            gestionnaireInfo.put("email", gestionnaire.getEmail());
            attestationResponse.put("gestionnaire", gestionnaireInfo);
            
            response.put("attestation", attestationResponse);
            response.put("status", HttpStatus.CREATED.value());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la création de l'attestation: " + e.getMessage()));
        }
    }

    @PostMapping("/classe/{classeId}")
    @Transactional
    public ResponseEntity<?> createAttestationsForClasse(
            @PathVariable Long classeId,
            @RequestBody Map<String, Object> request) {
        try {
            // 1. Extraction des données
            String titre = (String) request.get("titre");
            String contenu = (String) request.get("contenu");
            String dateExpiration = (String) request.get("dateExpiration");
            Long gestionnaireId = Long.valueOf(request.get("gestionnaireId").toString());

            // 2. Validation des champs obligatoires
            if (titre == null || titre.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le titre est obligatoire"));
            }

            if (gestionnaireId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "L'ID du gestionnaire est obligatoire"));
            }

            // 3. Vérification du gestionnaire
            Gestionnaire gestionnaire = gestionnaireRepository.findById(gestionnaireId)
                .orElseThrow(() -> new IllegalArgumentException("Gestionnaire non trouvé avec l'ID: " + gestionnaireId));

            // 4. Récupération des stagiaires de la classe
            List<Stagiaire> stagiaires = stagiaireRepository.findByClasseId(classeId);
            if (stagiaires.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun stagiaire trouvé dans cette classe"));
            }

            // 5. Création des attestations pour chaque stagiaire
            List<Map<String, Object>> attestationsResponse = new ArrayList<>();
            
            for (Stagiaire stagiaire : stagiaires) {
                Attestation attestation = new Attestation();
                attestation.setTitre(titre.trim());
                attestation.setContenu(contenu != null ? contenu.trim() : "");
                attestation.setDateEmission(LocalDate.now());
                attestation.setDateExpiration(dateExpiration != null ? LocalDate.parse(dateExpiration) : null);
                attestation.setStatut(Attestation.StatutAttestation.EN_ATTENTE);
                attestation.setStagiaire(stagiaire);
                attestation.setGestionnaire(gestionnaire);

                Attestation savedAttestation = attestationRepository.save(attestation);

                // Construction de la réponse pour chaque attestation
                Map<String, Object> attestationResponse = new HashMap<>();
                attestationResponse.put("id", savedAttestation.getId());
                attestationResponse.put("titre", savedAttestation.getTitre());
                attestationResponse.put("contenu", savedAttestation.getContenu());
                attestationResponse.put("dateEmission", savedAttestation.getDateEmission());
                attestationResponse.put("dateExpiration", savedAttestation.getDateExpiration());
                attestationResponse.put("statut", savedAttestation.getStatut());

                // Informations du stagiaire
                Map<String, Object> stagiaireInfo = new HashMap<>();
                stagiaireInfo.put("id", stagiaire.getId());
                stagiaireInfo.put("nom", stagiaire.getNom());
                stagiaireInfo.put("prenom", stagiaire.getPrenom());
                stagiaireInfo.put("email", stagiaire.getEmail());
                attestationResponse.put("stagiaire", stagiaireInfo);

                // Informations du gestionnaire
                Map<String, Object> gestionnaireInfo = new HashMap<>();
                gestionnaireInfo.put("id", gestionnaire.getId());
                gestionnaireInfo.put("nom", gestionnaire.getNom());
                gestionnaireInfo.put("prenom", gestionnaire.getPrenom());
                gestionnaireInfo.put("email", gestionnaire.getEmail());
                attestationResponse.put("gestionnaire", gestionnaireInfo);

                attestationsResponse.add(attestationResponse);
            }

            // 6. Construction de la réponse finale
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Attestations créées avec succès pour " + stagiaires.size() + " stagiaires");
            response.put("attestations", attestationsResponse);
            response.put("status", HttpStatus.CREATED.value());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la création des attestations: " + e.getMessage()));
        }
    }
} 