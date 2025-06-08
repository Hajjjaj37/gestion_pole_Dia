package com.gestion.demo.controller;

import com.gestion.demo.dto.EvenementRequest;
import com.gestion.demo.dto.EvenementResponse;
import com.gestion.demo.model.Evenement;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.Stagiaire;
import com.gestion.demo.model.Salle;
import com.gestion.demo.model.Classe;
import com.gestion.demo.repository.EvenementRepository;
import com.gestion.demo.repository.FormateurRepository;
import com.gestion.demo.repository.StagiaireRepository;
import com.gestion.demo.repository.SalleRepository;
import com.gestion.demo.repository.ClasseRepository;
import com.gestion.demo.repository.FormationRepository;
import com.gestion.demo.repository.ModuleRepository;
import com.gestion.demo.repository.GestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/evenements")
@CrossOrigin(origins = "*")
public class EvenementController {

    private static final Logger logger = LoggerFactory.getLogger(EvenementController.class);

    @Autowired
    private EvenementRepository evenementRepository;
    
    @Autowired
    private FormateurRepository formateurRepository;
    
    @Autowired
    private StagiaireRepository stagiaireRepository;
    
    @Autowired
    private SalleRepository salleRepository;
    
    @Autowired
    private ClasseRepository classeRepository;
    
    @Autowired
    private FormationRepository formationRepository;
    
    @Autowired
    private ModuleRepository moduleRepository;
    
    @Autowired
    private GestionnaireRepository gestionnaireRepository;

    // Get all events
    @GetMapping
    public ResponseEntity<List<Evenement>> getAllEvenements() {
        try {
            List<Evenement> evenements = evenementRepository.findAll();
            return ResponseEntity.ok(evenements);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Get event by ID
    @GetMapping("/{id}")
    public ResponseEntity<Evenement> getEvenementById(@PathVariable Long id) {
        try {
            Optional<Evenement> evenement = evenementRepository.findById(id);
            return evenement.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de l'événement avec l'ID: " + id, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Create new event
    @PostMapping
    public ResponseEntity<EvenementResponse> createEvenement(@RequestBody EvenementRequest request) {
        EvenementResponse response = new EvenementResponse();
        
        try {
            // Validation de base
            if (request == null) {
                response.setSuccess(false);
                response.setMessage("La requête ne peut pas être nulle");
                return ResponseEntity.badRequest().body(response);
            }

            // Validation des champs obligatoires
            if (request.getTitre() == null || request.getTitre().trim().isEmpty()) {
                response.setSuccess(false);
                response.setMessage("Le titre est obligatoire");
                return ResponseEntity.badRequest().body(response);
            }

            if (request.getDateDebut() == null || request.getDateFin() == null) {
                response.setSuccess(false);
                response.setMessage("Les dates de début et de fin sont obligatoires");
                return ResponseEntity.badRequest().body(response);
            }

            // Création de l'événement
            Evenement evenement = new Evenement();
            evenement.setTitre(request.getTitre());
            evenement.setDescription(request.getDescription());
            evenement.setDateDebut(request.getDateDebut());
            evenement.setDateFin(request.getDateFin());
            evenement.setType(request.getType());
            
            // Set salle
            if (request.getSalleId() != null) {
                Optional<Salle> salle = salleRepository.findById(request.getSalleId());
                if (salle.isPresent()) {
                    evenement.setSalle(salle.get());
                }
            }

            evenement.setCreatedAt(LocalDateTime.now());
            evenement.setUpdatedAt(LocalDateTime.now());

            // Première sauvegarde pour obtenir l'ID
            Evenement savedEvenement = evenementRepository.save(evenement);

            // Ajout des relations après la sauvegarde
            if (request.getFormateurIds() != null && !request.getFormateurIds().isEmpty()) {
                Set<Formateur> formateurs = new HashSet<>();
                for (Long id : request.getFormateurIds()) {
                    formateurRepository.findById(id).ifPresent(formateurs::add);
                }
                savedEvenement.setFormateurs(formateurs);
            }

            if (request.getStagiaireIds() != null && !request.getStagiaireIds().isEmpty()) {
                Set<Stagiaire> stagiaires = new HashSet<>();
                for (Long id : request.getStagiaireIds()) {
                    stagiaireRepository.findById(id).ifPresent(stagiaires::add);
                }
                savedEvenement.setStagiaires(stagiaires);
            }

            if (request.getClasseIds() != null && !request.getClasseIds().isEmpty()) {
                Set<Classe> classes = new HashSet<>();
                for (Long id : request.getClasseIds()) {
                    classeRepository.findById(id).ifPresent(classes::add);
                }
                savedEvenement.setClasses(classes);
            }

            // Deuxième sauvegarde pour les relations
            savedEvenement = evenementRepository.save(savedEvenement);

            // Préparation de la réponse
            response.setId(savedEvenement.getId());
            response.setTitre(savedEvenement.getTitre());
            response.setDescription(savedEvenement.getDescription());
            response.setDateDebut(savedEvenement.getDateDebut());
            response.setDateFin(savedEvenement.getDateFin());
            response.setType(savedEvenement.getType());
            response.setSuccess(true);
            response.setMessage("Événement créé avec succès");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'événement", e);
            response.setSuccess(false);
            response.setMessage("Erreur lors de la création de l'événement: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Update event
    @PutMapping("/{id}")
    public ResponseEntity<EvenementResponse> updateEvenement(@PathVariable Long id, @RequestBody EvenementRequest request) {
        EvenementResponse response = new EvenementResponse();
        
        try {
            Optional<Evenement> evenementOpt = evenementRepository.findById(id);
            if (evenementOpt.isPresent()) {
                Evenement evenement = evenementOpt.get();
                
                // Mise à jour des champs de base
                if (request.getTitre() != null) {
                    evenement.setTitre(request.getTitre());
                }
                if (request.getDescription() != null) {
                    evenement.setDescription(request.getDescription());
                }
                if (request.getDateDebut() != null) {
                    evenement.setDateDebut(request.getDateDebut());
                }
                if (request.getDateFin() != null) {
                    evenement.setDateFin(request.getDateFin());
                }
                if (request.getType() != null) {
                    evenement.setType(request.getType());
                }
                
                // Mise à jour de la salle
                if (request.getSalleId() != null) {
                    Optional<Salle> salle = salleRepository.findById(request.getSalleId());
                    if (salle.isPresent()) {
                        evenement.setSalle(salle.get());
                    }
                }
                
                // Mise à jour des formateurs
                if (request.getFormateurIds() != null) {
                    Set<Formateur> formateurs = new HashSet<>();
                    for (Long formateurId : request.getFormateurIds()) {
                        formateurRepository.findById(formateurId).ifPresent(formateurs::add);
                    }
                    evenement.setFormateurs(formateurs);
                }
                
                // Mise à jour des stagiaires
                if (request.getStagiaireIds() != null) {
                    Set<Stagiaire> stagiaires = new HashSet<>();
                    for (Long stagiaireId : request.getStagiaireIds()) {
                        stagiaireRepository.findById(stagiaireId).ifPresent(stagiaires::add);
                    }
                    evenement.setStagiaires(stagiaires);
                }
                
                // Mise à jour des classes
                if (request.getClasseIds() != null) {
                    Set<Classe> classes = new HashSet<>();
                    for (Long classeId : request.getClasseIds()) {
                        classeRepository.findById(classeId).ifPresent(classes::add);
                    }
                    evenement.setClasses(classes);
                }
                
                evenement.setUpdatedAt(LocalDateTime.now());
                
                // Sauvegarde des modifications
                Evenement updatedEvenement = evenementRepository.save(evenement);
                
                // Préparation de la réponse
                response.setId(updatedEvenement.getId());
                response.setTitre(updatedEvenement.getTitre());
                response.setDescription(updatedEvenement.getDescription());
                response.setDateDebut(updatedEvenement.getDateDebut());
                response.setDateFin(updatedEvenement.getDateFin());
                response.setType(updatedEvenement.getType());
                response.setSuccess(true);
                response.setMessage("Événement modifié avec succès");
                
                return ResponseEntity.ok(response);
            } else {
                response.setSuccess(false);
                response.setMessage("Événement non trouvé");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la modification de l'événement", e);
            response.setSuccess(false);
            response.setMessage("Erreur lors de la modification de l'événement: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Delete event
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvenement(@PathVariable Long id) {
        return evenementRepository.findById(id)
                .map(evenement -> {
                    evenementRepository.delete(evenement);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get events by formateur
    @GetMapping("/formateur/{formateurId}")
    public ResponseEntity<List<Evenement>> getEvenementsByFormateur(@PathVariable Long formateurId) {
        try {
            if (!formateurRepository.existsById(formateurId)) {
                return ResponseEntity.notFound().build();
            }
            List<Evenement> evenements = evenementRepository.findByFormateursId(formateurId);
            return ResponseEntity.ok(evenements);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements du formateur: " + formateurId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Get events by stagiaire
    @GetMapping("/stagiaire/{stagiaireId}")
    public ResponseEntity<List<Evenement>> getEvenementsByStagiaire(@PathVariable Long stagiaireId) {
        try {
            if (!stagiaireRepository.existsById(stagiaireId)) {
                return ResponseEntity.notFound().build();
            }
            List<Evenement> evenements = evenementRepository.findByStagiairesId(stagiaireId);
            return ResponseEntity.ok(evenements);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements du stagiaire: " + stagiaireId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Get events by salle
    @GetMapping("/salle/{salleId}")
    public ResponseEntity<List<Evenement>> getEvenementsBySalle(@PathVariable Long salleId) {
        try {
            if (!salleRepository.existsById(salleId)) {
                return ResponseEntity.notFound().build();
            }
            List<Evenement> evenements = evenementRepository.findBySalleId(salleId);
            return ResponseEntity.ok(evenements);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements de la salle: " + salleId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Get events by classe
    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<Evenement>> getEvenementsByClasse(@PathVariable Long classeId) {
        try {
            if (!classeRepository.existsById(classeId)) {
                return ResponseEntity.notFound().build();
            }
            
            List<Evenement> evenements = evenementRepository.findByClassesId(classeId);
            if (evenements.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(evenements);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements de la classe: " + classeId, e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Get events by date range
    @GetMapping("/date-range")
    public ResponseEntity<List<Evenement>> getEvenementsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        try {
            if (startDate == null || endDate == null) {
                return ResponseEntity.badRequest().build();
            }
            List<Evenement> evenements = evenementRepository.findByDateDebutBetween(startDate, endDate);
            return ResponseEntity.ok(evenements);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements par date", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Get formateurs for an event
    @GetMapping("/{evenementId}/formateurs")
    public ResponseEntity<Set<Formateur>> getFormateursForEvenement(@PathVariable Long evenementId) {
        try {
            Optional<Evenement> evenement = evenementRepository.findById(evenementId);
            if (evenement.isPresent()) {
                return ResponseEntity.ok(evenement.get().getFormateurs());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des formateurs", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Get stagiaires for an event
    @GetMapping("/{evenementId}/stagiaires")
    public ResponseEntity<Set<Stagiaire>> getStagiairesForEvenement(@PathVariable Long evenementId) {
        try {
            Optional<Evenement> evenement = evenementRepository.findById(evenementId);
            if (evenement.isPresent()) {
                return ResponseEntity.ok(evenement.get().getStagiaires());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des stagiaires", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Get classes for an event
    @GetMapping("/{evenementId}/classes")
    public ResponseEntity<Set<Classe>> getClassesForEvenement(@PathVariable Long evenementId) {
        try {
            Optional<Evenement> evenement = evenementRepository.findById(evenementId);
            if (evenement.isPresent()) {
                return ResponseEntity.ok(evenement.get().getClasses());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des classes", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Add formateur to event
    @PostMapping("/{evenementId}/formateur/{formateurId}")
    public ResponseEntity<Evenement> addFormateurToEvenement(
            @PathVariable Long evenementId,
            @PathVariable Long formateurId) {
        try {
            Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
            Optional<Formateur> formateurOpt = formateurRepository.findById(formateurId);

            if (evenementOpt.isPresent() && formateurOpt.isPresent()) {
                Evenement evenement = evenementOpt.get();
                Formateur formateur = formateurOpt.get();
                
                if (evenement.getFormateurs() == null) {
                    evenement.setFormateurs(new HashSet<>());
                }
                evenement.getFormateurs().add(formateur);
                
                return ResponseEntity.ok(evenementRepository.save(evenement));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout du formateur", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Add stagiaire to event
    @PostMapping("/{evenementId}/stagiaire/{stagiaireId}")
    public ResponseEntity<Evenement> addStagiaireToEvenement(
            @PathVariable Long evenementId,
            @PathVariable Long stagiaireId) {
        try {
            Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
            Optional<Stagiaire> stagiaireOpt = stagiaireRepository.findById(stagiaireId);

            if (evenementOpt.isPresent() && stagiaireOpt.isPresent()) {
                Evenement evenement = evenementOpt.get();
                Stagiaire stagiaire = stagiaireOpt.get();
                
                if (evenement.getStagiaires() == null) {
                    evenement.setStagiaires(new HashSet<>());
                }
                evenement.getStagiaires().add(stagiaire);
                
                return ResponseEntity.ok(evenementRepository.save(evenement));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout du stagiaire", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Add classe to event
    @PostMapping("/{evenementId}/classe/{classeId}")
    public ResponseEntity<Evenement> addClasseToEvenement(
            @PathVariable Long evenementId,
            @PathVariable Long classeId) {
        try {
            Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
            Optional<Classe> classeOpt = classeRepository.findById(classeId);

            if (evenementOpt.isPresent() && classeOpt.isPresent()) {
                Evenement evenement = evenementOpt.get();
                Classe classe = classeOpt.get();
                
                if (evenement.getClasses() == null) {
                    evenement.setClasses(new HashSet<>());
                }
                evenement.getClasses().add(classe);
                
                return ResponseEntity.ok(evenementRepository.save(evenement));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout de la classe", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Set salle for event
    @PostMapping("/{evenementId}/salle/{salleId}")
    public ResponseEntity<Evenement> setSalleForEvenement(
            @PathVariable Long evenementId,
            @PathVariable Long salleId) {
        try {
            Optional<Evenement> evenementOpt = evenementRepository.findById(evenementId);
            Optional<Salle> salleOpt = salleRepository.findById(salleId);

            if (evenementOpt.isPresent() && salleOpt.isPresent()) {
                Evenement evenement = evenementOpt.get();
                Salle salle = salleOpt.get();
                evenement.setSalle(salle);
                return ResponseEntity.ok(evenementRepository.save(evenement));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout de la salle", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Afficher un événement avec toutes ses relations
    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getEvenementDetails(@PathVariable Long id) {
        try {
            Optional<Evenement> evenementOpt = evenementRepository.findById(id);
            if (evenementOpt.isPresent()) {
                Evenement evenement = evenementOpt.get();
                Map<String, Object> response = new HashMap<>();
                
                // Informations de base de l'événement
                response.put("id", evenement.getId());
                response.put("titre", evenement.getTitre());
                response.put("description", evenement.getDescription());
                response.put("dateDebut", evenement.getDateDebut());
                response.put("dateFin", evenement.getDateFin());
                response.put("type", evenement.getType());
                
                // Salle
                if (evenement.getSalle() != null) {
                    Map<String, Object> salle = new HashMap<>();
                    salle.put("id", evenement.getSalle().getId());
                    salle.put("nom", evenement.getSalle().getNom());
                    salle.put("capacite", evenement.getSalle().getCapacite());
                    response.put("salle", salle);
                }
                
                // Formateurs
                if (evenement.getFormateurs() != null) {
                    List<Map<String, Object>> formateurs = evenement.getFormateurs().stream()
                        .map(f -> {
                            Map<String, Object> formateur = new HashMap<>();
                            formateur.put("id", f.getId());
                            formateur.put("nom", f.getNom());
                            formateur.put("prenom", f.getPrenom());
                            formateur.put("email", f.getEmail());
                            return formateur;
                        })
                        .collect(Collectors.toList());
                    response.put("formateurs", formateurs);
                }
                
                // Stagiaires
                if (evenement.getStagiaires() != null) {
                    List<Map<String, Object>> stagiaires = evenement.getStagiaires().stream()
                        .map(s -> {
                            Map<String, Object> stagiaire = new HashMap<>();
                            stagiaire.put("id", s.getId());
                            stagiaire.put("nom", s.getNom());
                            stagiaire.put("prenom", s.getPrenom());
                            stagiaire.put("email", s.getEmail());
                            return stagiaire;
                        })
                        .collect(Collectors.toList());
                    response.put("stagiaires", stagiaires);
                }
                
                // Classes
                if (evenement.getClasses() != null) {
                    List<Map<String, Object>> classes = evenement.getClasses().stream()
                        .map(c -> {
                            Map<String, Object> classe = new HashMap<>();
                            classe.put("id", c.getId());
                            classe.put("nom", c.getNom());
                            return classe;
                        })
                        .collect(Collectors.toList());
                    response.put("classes", classes);
                }
                
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des détails de l'événement", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Afficher tous les événements avec leurs relations
    @GetMapping("/all/details")
    public ResponseEntity<List<Map<String, Object>>> getAllEvenementsDetails() {
        try {
            List<Evenement> evenements = evenementRepository.findAll();
            List<Map<String, Object>> response = evenements.stream()
                .map(evenement -> {
                    Map<String, Object> eventDetails = new HashMap<>();
                    
                    // Informations de base
                    eventDetails.put("id", evenement.getId());
                    eventDetails.put("titre", evenement.getTitre());
                    eventDetails.put("description", evenement.getDescription());
                    eventDetails.put("dateDebut", evenement.getDateDebut());
                    eventDetails.put("dateFin", evenement.getDateFin());
                    eventDetails.put("type", evenement.getType());
                    
                    // Salle
                    if (evenement.getSalle() != null) {
                        Map<String, Object> salle = new HashMap<>();
                        salle.put("id", evenement.getSalle().getId());
                        salle.put("nom", evenement.getSalle().getNom());
                        eventDetails.put("salle", salle);
                    }
                    
                    // Formateurs
                    if (evenement.getFormateurs() != null) {
                        List<Map<String, Object>> formateurs = evenement.getFormateurs().stream()
                            .map(f -> {
                                Map<String, Object> formateur = new HashMap<>();
                                formateur.put("id", f.getId());
                                formateur.put("nom", f.getNom());
                                formateur.put("prenom", f.getPrenom());
                                formateur.put("email", f.getEmail());
                                return formateur;
                            })
                            .collect(Collectors.toList());
                        eventDetails.put("formateurs", formateurs);
                    }
                    
                    // Stagiaires
                    if (evenement.getStagiaires() != null) {
                        List<Map<String, Object>> stagiaires = evenement.getStagiaires().stream()
                            .map(s -> {
                                Map<String, Object> stagiaire = new HashMap<>();
                                stagiaire.put("id", s.getId());
                                stagiaire.put("nom", s.getNom());
                                stagiaire.put("prenom", s.getPrenom());
                                stagiaire.put("email", s.getEmail());
                                return stagiaire;
                            })
                            .collect(Collectors.toList());
                        eventDetails.put("stagiaires", stagiaires);
                    }
                    
                    // Classes
                    if (evenement.getClasses() != null) {
                        List<Map<String, Object>> classes = evenement.getClasses().stream()
                            .map(c -> {
                                Map<String, Object> classe = new HashMap<>();
                                classe.put("id", c.getId());
                                classe.put("nom", c.getNom());
                                return classe;
                            })
                            .collect(Collectors.toList());
                        eventDetails.put("classes", classes);
                    }
                    
                    return eventDetails;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Afficher les événements par formateur
    @GetMapping("/formateur/{formateurId}/details")
    public ResponseEntity<List<Map<String, Object>>> getEvenementsByFormateurDetails(@PathVariable Long formateurId) {
        try {
            if (!formateurRepository.existsById(formateurId)) {
                return ResponseEntity.notFound().build();
            }
            
            List<Evenement> evenements = evenementRepository.findByFormateursId(formateurId);
            List<Map<String, Object>> response = evenements.stream()
                .map(evenement -> {
                    Map<String, Object> eventDetails = new HashMap<>();
                    eventDetails.put("id", evenement.getId());
                    eventDetails.put("titre", evenement.getTitre());
                    eventDetails.put("description", evenement.getDescription());
                    eventDetails.put("dateDebut", evenement.getDateDebut());
                    eventDetails.put("dateFin", evenement.getDateFin());
                    eventDetails.put("type", evenement.getType());
                    
                    // Salle
                    if (evenement.getSalle() != null) {
                        Map<String, Object> salle = new HashMap<>();
                        salle.put("id", evenement.getSalle().getId());
                        salle.put("nom", evenement.getSalle().getNom());
                        eventDetails.put("salle", salle);
                    }
                    
                    // Formateurs
                    if (evenement.getFormateurs() != null) {
                        List<Map<String, Object>> formateurs = evenement.getFormateurs().stream()
                            .map(f -> {
                                Map<String, Object> formateur = new HashMap<>();
                                formateur.put("id", f.getId());
                                formateur.put("nom", f.getNom());
                                formateur.put("prenom", f.getPrenom());
                                formateur.put("email", f.getEmail());
                                return formateur;
                            })
                            .collect(Collectors.toList());
                        eventDetails.put("formateurs", formateurs);
                    }
                    
                    // Stagiaires
                    if (evenement.getStagiaires() != null) {
                        List<Map<String, Object>> stagiaires = evenement.getStagiaires().stream()
                            .map(s -> {
                                Map<String, Object> stagiaire = new HashMap<>();
                                stagiaire.put("id", s.getId());
                                stagiaire.put("nom", s.getNom());
                                stagiaire.put("prenom", s.getPrenom());
                                stagiaire.put("email", s.getEmail());
                                return stagiaire;
                            })
                            .collect(Collectors.toList());
                        eventDetails.put("stagiaires", stagiaires);
                    }
                    
                    // Classes
                    if (evenement.getClasses() != null) {
                        List<Map<String, Object>> classes = evenement.getClasses().stream()
                            .map(c -> {
                                Map<String, Object> classe = new HashMap<>();
                                classe.put("id", c.getId());
                                classe.put("nom", c.getNom());
                                return classe;
                            })
                            .collect(Collectors.toList());
                        eventDetails.put("classes", classes);
                    }
                    
                    return eventDetails;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements du formateur", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // Afficher les événements par classe
    @GetMapping("/classe/{classeId}/details")
    public ResponseEntity<List<Map<String, Object>>> getEvenementsByClasseDetails(@PathVariable Long classeId) {
        try {
            if (!classeRepository.existsById(classeId)) {
                return ResponseEntity.notFound().build();
            }
            
            List<Evenement> evenements = evenementRepository.findByClassesId(classeId);
            List<Map<String, Object>> response = evenements.stream()
                .map(evenement -> {
                    Map<String, Object> eventDetails = new HashMap<>();
                    eventDetails.put("id", evenement.getId());
                    eventDetails.put("titre", evenement.getTitre());
                    eventDetails.put("dateDebut", evenement.getDateDebut());
                    eventDetails.put("dateFin", evenement.getDateFin());
                    eventDetails.put("type", evenement.getType());
                    
                    if (evenement.getSalle() != null) {
                        Map<String, Object> salle = new HashMap<>();
                        salle.put("id", evenement.getSalle().getId());
                        salle.put("nom", evenement.getSalle().getNom());
                        eventDetails.put("salle", salle);
                    }
                    
                    return eventDetails;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements de la classe", e);
            return ResponseEntity.badRequest().build();
        }
    }
} 