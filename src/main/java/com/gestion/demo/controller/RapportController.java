package com.gestion.demo.controller;

import com.gestion.demo.model.Rapport;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.Stagiaire;
import com.gestion.demo.repository.RapportRepository;
import com.gestion.demo.repository.FormateurRepository;
import com.gestion.demo.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {
    private final RapportRepository rapportRepository;
    private final FormateurRepository formateurRepository;
    private final StagiaireRepository stagiaireRepository;

    @PostMapping
    @Transactional
    public ResponseEntity<?> createRapport(@RequestBody Map<String, Object> request) {
        try {
            // 1. Extraction des données
            String titre = (String) request.get("titre");
            String contenu = (String) request.get("contenu");
            String statut = (String) request.get("statut");
            List<Integer> formateurIds = (List<Integer>) request.get("formateurIds");
            List<Integer> stagiaireIds = (List<Integer>) request.get("stagiaireIds");

            // 2. Validation des champs obligatoires
            if (titre == null || titre.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le titre est obligatoire"));
            }

            if (formateurIds == null || formateurIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Au moins un formateur est requis"));
            }

            if (stagiaireIds == null || stagiaireIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Au moins un stagiaire est requis"));
            }

            // 3. Création du rapport
            Rapport rapport = new Rapport();
            rapport.setTitre(titre.trim());
            rapport.setContenu(contenu != null ? contenu.trim() : "");
            rapport.setDateCreation(LocalDateTime.now());

            // 4. Gestion du statut
            try {
                if (statut != null && !statut.trim().isEmpty()) {
                    rapport.setStatut(Rapport.StatutRapport.valueOf(statut.toUpperCase()));
                } else {
                    rapport.setStatut(Rapport.StatutRapport.EN_ATTENTE);
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", 
                    "Statut invalide. Les valeurs autorisées sont: EN_ATTENTE, VALIDE, REFUSE"
                ));
            }

            // 5. Vérification des formateurs
            Set<Formateur> formateurs = new HashSet<>();
            for (Integer id : formateurIds) {
                Formateur formateur = formateurRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("Formateur non trouvé avec l'ID: " + id));
                formateurs.add(formateur);
            }

            // 6. Vérification des stagiaires
            Set<Stagiaire> stagiaires = new HashSet<>();
            for (Integer id : stagiaireIds) {
                Stagiaire stagiaire = stagiaireRepository.findById(id.longValue())
                    .orElseThrow(() -> new IllegalArgumentException("Stagiaire non trouvé avec l'ID: " + id));
                stagiaires.add(stagiaire);
            }

            // 7. Initialisation des collections
            rapport.setFormateurs(new HashSet<>());
            rapport.setStagiaires(new HashSet<>());

            // 8. Ajout des formateurs
            for (Formateur formateur : formateurs) {
                rapport.getFormateurs().add(formateur);
                if (formateur.getRapports() == null) {
                    formateur.setRapports(new HashSet<>());
                }
                formateur.getRapports().add(rapport);
            }

            // 9. Ajout des stagiaires
            for (Stagiaire stagiaire : stagiaires) {
                rapport.getStagiaires().add(stagiaire);
                if (stagiaire.getRapports() == null) {
                    stagiaire.setRapports(new HashSet<>());
                }
                stagiaire.getRapports().add(rapport);
            }

            // 10. Sauvegarde du rapport
            Rapport savedRapport = rapportRepository.save(rapport);
            
            // 11. Construction de la réponse de succès avec une structure simplifiée
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rapport créé avec succès");
            
            Map<String, Object> rapportResponse = new HashMap<>();
            rapportResponse.put("id", savedRapport.getId());
            rapportResponse.put("titre", savedRapport.getTitre());
            rapportResponse.put("contenu", savedRapport.getContenu());
            rapportResponse.put("dateCreation", savedRapport.getDateCreation());
            rapportResponse.put("statut", savedRapport.getStatut());
            
            // Ajout des formateurs simplifiés
            List<Map<String, Object>> formateursResponse = savedRapport.getFormateurs().stream()
                .map(f -> {
                    Map<String, Object> fMap = new HashMap<>();
                    fMap.put("id", f.getId());
                    fMap.put("nom", f.getNom());
                    fMap.put("prenom", f.getPrenom());
                    fMap.put("email", f.getEmail());
                    fMap.put("specialite", f.getSpecialite());
                    return fMap;
                })
                .collect(Collectors.toList());
            rapportResponse.put("formateurs", formateursResponse);
            
            // Ajout des stagiaires simplifiés
            List<Map<String, Object>> stagiairesResponse = savedRapport.getStagiaires().stream()
                .map(s -> {
                    Map<String, Object> sMap = new HashMap<>();
                    sMap.put("id", s.getId());
                    sMap.put("nom", s.getNom());
                    sMap.put("prenom", s.getPrenom());
                    sMap.put("email", s.getEmail());
                    return sMap;
                })
                .collect(Collectors.toList());
            rapportResponse.put("stagiaires", stagiairesResponse);
            
            response.put("rapport", rapportResponse);
            response.put("status", HttpStatus.CREATED.value());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la création du rapport: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllRapports() {
        try {
            List<Rapport> rapports = rapportRepository.findAll();
            
            // Construction de la réponse simplifiée
            List<Map<String, Object>> rapportsResponse = rapports.stream()
                .map(rapport -> {
                    Map<String, Object> rapportMap = new HashMap<>();
                    rapportMap.put("id", rapport.getId());
                    rapportMap.put("titre", rapport.getTitre());
                    rapportMap.put("contenu", rapport.getContenu());
                    rapportMap.put("dateCreation", rapport.getDateCreation());
                    rapportMap.put("statut", rapport.getStatut());
                    
                    // Formateurs simplifiés
                    List<Map<String, Object>> formateursResponse = rapport.getFormateurs().stream()
                        .map(f -> {
                            Map<String, Object> fMap = new HashMap<>();
                            fMap.put("id", f.getId());
                            fMap.put("nom", f.getNom());
                            fMap.put("prenom", f.getPrenom());
                            fMap.put("email", f.getEmail());
                            fMap.put("specialite", f.getSpecialite());
                            return fMap;
                        })
                        .collect(Collectors.toList());
                    rapportMap.put("formateurs", formateursResponse);
                    
                    // Stagiaires simplifiés
                    List<Map<String, Object>> stagiairesResponse = rapport.getStagiaires().stream()
                        .map(s -> {
                            Map<String, Object> sMap = new HashMap<>();
                            sMap.put("id", s.getId());
                            sMap.put("nom", s.getNom());
                            sMap.put("prenom", s.getPrenom());
                            sMap.put("email", s.getEmail());
                            return sMap;
                        })
                        .collect(Collectors.toList());
                    rapportMap.put("stagiaires", stagiairesResponse);
                    
                    return rapportMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(rapportsResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des rapports: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRapportById(@PathVariable Long id) {
        try {
            Rapport rapport = rapportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rapport non trouvé avec l'ID: " + id));

            // Construction de la réponse simplifiée
            Map<String, Object> rapportResponse = new HashMap<>();
            rapportResponse.put("id", rapport.getId());
            rapportResponse.put("titre", rapport.getTitre());
            rapportResponse.put("contenu", rapport.getContenu());
            rapportResponse.put("dateCreation", rapport.getDateCreation());
            rapportResponse.put("statut", rapport.getStatut());
            
            // Formateurs simplifiés
            List<Map<String, Object>> formateursResponse = rapport.getFormateurs().stream()
                .map(f -> {
                    Map<String, Object> fMap = new HashMap<>();
                    fMap.put("id", f.getId());
                    fMap.put("nom", f.getNom());
                    fMap.put("prenom", f.getPrenom());
                    fMap.put("email", f.getEmail());
                    fMap.put("specialite", f.getSpecialite());
                    return fMap;
                })
                .collect(Collectors.toList());
            rapportResponse.put("formateurs", formateursResponse);
            
            // Stagiaires simplifiés
            List<Map<String, Object>> stagiairesResponse = rapport.getStagiaires().stream()
                .map(s -> {
                    Map<String, Object> sMap = new HashMap<>();
                    sMap.put("id", s.getId());
                    sMap.put("nom", s.getNom());
                    sMap.put("prenom", s.getPrenom());
                    sMap.put("email", s.getEmail());
                    return sMap;
                })
                .collect(Collectors.toList());
            rapportResponse.put("stagiaires", stagiairesResponse);
            
            return ResponseEntity.ok(rapportResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération du rapport: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateRapport(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Rapport rapport = rapportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Rapport non trouvé avec l'ID: " + id));

            String titre = (String) request.get("titre");
            String contenu = (String) request.get("contenu");
            String statut = (String) request.get("statut");

            if (titre != null) {
                rapport.setTitre(titre.trim());
            }
            if (contenu != null) {
                rapport.setContenu(contenu.trim());
            }
            if (statut != null) {
                try {
                    rapport.setStatut(Rapport.StatutRapport.valueOf(statut.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body(Map.of(
                        "error", 
                        "Statut invalide. Les valeurs autorisées sont: EN_ATTENTE, VALIDE, REFUSE"
                    ));
                }
            }

            Rapport updatedRapport = rapportRepository.save(rapport);
            
            // Construction de la réponse simplifiée
            Map<String, Object> rapportResponse = new HashMap<>();
            rapportResponse.put("id", updatedRapport.getId());
            rapportResponse.put("titre", updatedRapport.getTitre());
            rapportResponse.put("contenu", updatedRapport.getContenu());
            rapportResponse.put("dateCreation", updatedRapport.getDateCreation());
            rapportResponse.put("statut", updatedRapport.getStatut());
            
            // Formateurs simplifiés
            List<Map<String, Object>> formateursResponse = updatedRapport.getFormateurs().stream()
                .map(f -> {
                    Map<String, Object> fMap = new HashMap<>();
                    fMap.put("id", f.getId());
                    fMap.put("nom", f.getNom());
                    fMap.put("prenom", f.getPrenom());
                    fMap.put("email", f.getEmail());
                    fMap.put("specialite", f.getSpecialite());
                    return fMap;
                })
                .collect(Collectors.toList());
            rapportResponse.put("formateurs", formateursResponse);
            
            // Stagiaires simplifiés
            List<Map<String, Object>> stagiairesResponse = updatedRapport.getStagiaires().stream()
                .map(s -> {
                    Map<String, Object> sMap = new HashMap<>();
                    sMap.put("id", s.getId());
                    sMap.put("nom", s.getNom());
                    sMap.put("prenom", s.getPrenom());
                    sMap.put("email", s.getEmail());
                    return sMap;
                })
                .collect(Collectors.toList());
            rapportResponse.put("stagiaires", stagiairesResponse);
            
            return ResponseEntity.ok(rapportResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la mise à jour du rapport: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteRapport(@PathVariable Long id) {
        if (!rapportRepository.existsById(id)) {
            throw new IllegalArgumentException("Rapport non trouvé avec l'ID: " + id);
        }
        rapportRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/formateur/{formateurId}")
    public ResponseEntity<?> getRapportsByFormateur(@PathVariable Long formateurId) {
        try {
            // Vérifier si le formateur existe
            Formateur formateur = formateurRepository.findById(formateurId)
                .orElseThrow(() -> new IllegalArgumentException("Formateur non trouvé avec l'ID: " + formateurId));

            // Récupérer les rapports du formateur
            List<Rapport> rapports = rapportRepository.findByFormateursId(formateurId);
            
            // Construction de la réponse simplifiée
            List<Map<String, Object>> rapportsResponse = rapports.stream()
                .map(rapport -> {
                    Map<String, Object> rapportMap = new HashMap<>();
                    rapportMap.put("id", rapport.getId());
                    rapportMap.put("titre", rapport.getTitre());
                    rapportMap.put("contenu", rapport.getContenu());
                    rapportMap.put("dateCreation", rapport.getDateCreation());
                    rapportMap.put("statut", rapport.getStatut());
                    
                    // Formateurs simplifiés
                    List<Map<String, Object>> formateursResponse = rapport.getFormateurs().stream()
                        .map(f -> {
                            Map<String, Object> fMap = new HashMap<>();
                            fMap.put("id", f.getId());
                            fMap.put("nom", f.getNom());
                            fMap.put("prenom", f.getPrenom());
                            fMap.put("email", f.getEmail());
                            fMap.put("specialite", f.getSpecialite());
                            return fMap;
                        })
                        .collect(Collectors.toList());
                    rapportMap.put("formateurs", formateursResponse);
                    
                    // Stagiaires simplifiés
                    List<Map<String, Object>> stagiairesResponse = rapport.getStagiaires().stream()
                        .map(s -> {
                            Map<String, Object> sMap = new HashMap<>();
                            sMap.put("id", s.getId());
                            sMap.put("nom", s.getNom());
                            sMap.put("prenom", s.getPrenom());
                            sMap.put("email", s.getEmail());
                            return sMap;
                        })
                        .collect(Collectors.toList());
                    rapportMap.put("stagiaires", stagiairesResponse);
                    
                    return rapportMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(rapportsResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des rapports du formateur: " + e.getMessage()));
        }
    }

    @GetMapping("/stagiaire/{stagiaireId}")
    public ResponseEntity<?> getRapportsByStagiaire(@PathVariable Long stagiaireId) {
        try {
            // Vérifier si le stagiaire existe
            Stagiaire stagiaire = stagiaireRepository.findById(stagiaireId)
                .orElseThrow(() -> new IllegalArgumentException("Stagiaire non trouvé avec l'ID: " + stagiaireId));

            // Récupérer les rapports du stagiaire
            List<Rapport> rapports = rapportRepository.findByStagiairesId(stagiaireId);
            
            // Construction de la réponse simplifiée
            List<Map<String, Object>> rapportsResponse = rapports.stream()
                .map(rapport -> {
                    Map<String, Object> rapportMap = new HashMap<>();
                    rapportMap.put("id", rapport.getId());
                    rapportMap.put("titre", rapport.getTitre());
                    rapportMap.put("contenu", rapport.getContenu());
                    rapportMap.put("dateCreation", rapport.getDateCreation());
                    rapportMap.put("statut", rapport.getStatut());
                    
                    // Formateurs simplifiés
                    List<Map<String, Object>> formateursResponse = rapport.getFormateurs().stream()
                        .map(f -> {
                            Map<String, Object> fMap = new HashMap<>();
                            fMap.put("id", f.getId());
                            fMap.put("nom", f.getNom());
                            fMap.put("prenom", f.getPrenom());
                            fMap.put("email", f.getEmail());
                            fMap.put("specialite", f.getSpecialite());
                            return fMap;
                        })
                        .collect(Collectors.toList());
                    rapportMap.put("formateurs", formateursResponse);
                    
                    // Stagiaires simplifiés
                    List<Map<String, Object>> stagiairesResponse = rapport.getStagiaires().stream()
                        .map(s -> {
                            Map<String, Object> sMap = new HashMap<>();
                            sMap.put("id", s.getId());
                            sMap.put("nom", s.getNom());
                            sMap.put("prenom", s.getPrenom());
                            sMap.put("email", s.getEmail());
                            return sMap;
                        })
                        .collect(Collectors.toList());
                    rapportMap.put("stagiaires", stagiairesResponse);
                    
                    return rapportMap;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(rapportsResponse);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Erreur lors de la récupération des rapports du stagiaire: " + e.getMessage()));
        }
    }
} 
 