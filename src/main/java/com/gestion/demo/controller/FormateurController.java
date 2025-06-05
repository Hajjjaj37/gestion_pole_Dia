package com.gestion.demo.controller;

import com.gestion.demo.dto.FormateurRequest;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.Formation;
import com.gestion.demo.model.Role;
import com.gestion.demo.model.User;
import com.gestion.demo.model.Classe;
import com.gestion.demo.model.Salle;
import com.gestion.demo.repository.FormateurRepository;
import com.gestion.demo.repository.FormationRepository;
import com.gestion.demo.repository.UserRepository;
import com.gestion.demo.repository.ClasseRepository;
import com.gestion.demo.repository.SalleRepository;
import com.gestion.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/formateurs")
@RequiredArgsConstructor
public class FormateurController {

    private final FormateurRepository formateurRepository;
    private final UserRepository userRepository;
    private final FormationRepository formationRepository;
    private final ClasseRepository classeRepository;
    private final SalleRepository salleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> createFormateur(@RequestBody FormateurRequest request) {
        try {
            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom du formateur est requis"
                ));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le mot de passe est requis"
                ));
            }

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom d'utilisateur est requis"
                ));
            }

            // Vérifier si l'email existe déjà
            if (formateurRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email déjà utilisé"
                ));
            }

            // Vérifier si le username existe déjà
            if (userRepository.existsByUsername(request.getUsername())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Nom d'utilisateur déjà utilisé"
                ));
            }

            // Créer l'utilisateur
            User newUser = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .email(request.getEmail())
                    .nom(request.getNom())
                    .prenom(request.getPrenom())
                    .role(Role.ROLE_FORMATEUR)
                    .build();

            // Sauvegarder l'utilisateur d'abord
            newUser = userRepository.save(newUser);

            // Créer le formateur
            Formateur formateur = new Formateur();
            formateur.setNom(request.getNom());
            formateur.setPrenom(request.getPrenom());
            formateur.setEmail(request.getEmail());
            formateur.setSpecialite(request.getSpecialite());
            formateur.setUser(newUser);

            // Ajouter la salle si un salleId est fourni
            if (request.getSalleId() != null) {
                Salle salle = salleRepository.findById(request.getSalleId())
                    .orElseThrow(() -> new RuntimeException("Salle non trouvée avec l'ID: " + request.getSalleId()));
                formateur.setSalle(salle);
            }

            // Ajouter les classes si des IDs sont fournis
            if (request.getClasseIds() != null && !request.getClasseIds().isEmpty()) {
                Set<Classe> classes = new HashSet<>(classeRepository.findAllById(request.getClasseIds()));
                formateur.setClasses(classes);
            }

            // Sauvegarder le formateur
            formateur = formateurRepository.save(formateur);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Formateur créé avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", formateur.getId());
            data.put("nom", formateur.getNom());
            data.put("prenom", formateur.getPrenom());
            data.put("email", formateur.getEmail());
            data.put("specialite", formateur.getSpecialite());
            
            // Ajouter les informations de la salle
            if (formateur.getSalle() != null) {
                Map<String, Object> salleData = new HashMap<>();
                salleData.put("id", formateur.getSalle().getId());
                salleData.put("nom", formateur.getSalle().getNom());
                salleData.put("numero", formateur.getSalle().getNumero());
                salleData.put("description", formateur.getSalle().getDescription());
                salleData.put("capacite", formateur.getSalle().getCapacite());
                salleData.put("equipement", formateur.getSalle().getEquipement());
                data.put("salle", salleData);
            }
            
            // Ajouter les classes
            List<Map<String, Object>> classesData = new ArrayList<>();
            for (Classe classe : formateur.getClasses()) {
                Map<String, Object> classeData = new HashMap<>();
                classeData.put("id", classe.getId());
                classeData.put("nom", classe.getNom());
                classeData.put("description", classe.getDescription());
                classeData.put("dateDebut", classe.getDateDebut());
                classeData.put("dateFin", classe.getDateFin());
                classesData.add(classeData);
            }
            data.put("classes", classesData);
            
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la création du formateur", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la création du formateur: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('FORMATEUR')")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String username = jwtService.extractUsername(jwt);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        Formateur formateur = formateurRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("formateur", formateur);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATEUR')")
    public ResponseEntity<?> getFormateurById(@PathVariable Long id) {
        try {
            Formateur formateur = formateurRepository.findFormateurWithClassesAndUser(id)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé avec l'ID: " + id));

            Map<String, Object> formateurData = new HashMap<>();
            formateurData.put("id", formateur.getId());
            formateurData.put("nom", formateur.getNom());
            formateurData.put("prenom", formateur.getPrenom());
            formateurData.put("email", formateur.getEmail());
            formateurData.put("specialite", formateur.getSpecialite());
            
            // Ajouter les informations de l'utilisateur
            if (formateur.getUser() != null) {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", formateur.getUser().getId());
                userData.put("username", formateur.getUser().getUsername());
                userData.put("role", formateur.getUser().getRole().name());
                formateurData.put("user", userData);
            }
            
            // Ajouter les informations de la salle
            if (formateur.getSalle() != null) {
                Map<String, Object> salleData = new HashMap<>();
                salleData.put("id", formateur.getSalle().getId());
                salleData.put("nom", formateur.getSalle().getNom());
                salleData.put("numero", formateur.getSalle().getNumero());
                salleData.put("description", formateur.getSalle().getDescription());
                salleData.put("capacite", formateur.getSalle().getCapacite());
                salleData.put("equipement", formateur.getSalle().getEquipement());
                formateurData.put("salle", salleData);
            }
            
            // Ajouter les classes
            List<Map<String, Object>> classesData = new ArrayList<>();
            if (formateur.getClasses() != null) {
                for (Classe classe : formateur.getClasses()) {
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
                    
                    classesData.add(classeData);
                }
            }
            formateurData.put("classes", classesData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Formateur récupéré avec succès");
            response.put("data", formateurData);

            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération du formateur", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la récupération du formateur: " + e.getMessage()
                ));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATEUR')")
    public ResponseEntity<?> getAllFormateurs() {
        try {
            List<Formateur> formateurs = formateurRepository.findAll();
            
            List<Map<String, Object>> formateursData = new ArrayList<>();
            for (Formateur formateur : formateurs) {
                Map<String, Object> formateurData = new HashMap<>();
                formateurData.put("id", formateur.getId());
                formateurData.put("nom", formateur.getNom());
                formateurData.put("prenom", formateur.getPrenom());
                formateurData.put("email", formateur.getEmail());
                formateurData.put("specialite", formateur.getSpecialite());
                
                // Ajouter les informations de l'utilisateur
                if (formateur.getUser() != null) {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", formateur.getUser().getId());
                    userData.put("username", formateur.getUser().getUsername());
                    userData.put("role", formateur.getUser().getRole().name());
                    formateurData.put("user", userData);
                }
                
                // Ajouter les informations de la salle
                if (formateur.getSalle() != null) {
                    Map<String, Object> salleData = new HashMap<>();
                    salleData.put("id", formateur.getSalle().getId());
                    salleData.put("nom", formateur.getSalle().getNom());
                    salleData.put("numero", formateur.getSalle().getNumero());
                    salleData.put("description", formateur.getSalle().getDescription());
                    salleData.put("capacite", formateur.getSalle().getCapacite());
                    salleData.put("equipement", formateur.getSalle().getEquipement());
                    formateurData.put("salle", salleData);
                }
                
                // Ajouter les classes
                List<Map<String, Object>> classesData = new ArrayList<>();
                if (formateur.getClasses() != null) {
                    for (Classe classe : formateur.getClasses()) {
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
                        
                        classesData.add(classeData);
                    }
                }
                formateurData.put("classes", classesData);
                
                formateursData.add(formateurData);
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Formateurs récupérés avec succès",
                "data", formateursData
            ));
            
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des formateurs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la récupération des formateurs: " + e.getMessage()
                ));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateFormateur(@PathVariable Long id, @RequestBody FormateurRequest request) {
        try {
            log.info("Début de la modification du formateur avec l'id: {}", id);
            
            // Vérifier si le formateur existe
            Formateur formateur = formateurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé avec l'id: " + id));

            // Validation des champs requis
            if (request.getNom() == null || request.getNom().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Le nom du formateur est requis"
                ));
            }

            // Vérifier si l'email existe déjà pour un autre formateur
            if (!formateur.getEmail().equals(request.getEmail()) && 
                formateurRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Email déjà utilisé par un autre formateur"
                ));
            }

            // Mise à jour des informations du formateur
            formateur.setNom(request.getNom().trim());
            formateur.setPrenom(request.getPrenom() != null ? request.getPrenom().trim() : "");
            formateur.setEmail(request.getEmail());
            formateur.setSpecialite(request.getSpecialite());

            // Mise à jour du mot de passe si fourni
            if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
                User user = formateur.getUser();
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
            }

            // Mise à jour des classes si fournies
            if (request.getClasseIds() != null) {
                Set<Classe> classes = new HashSet<>(classeRepository.findAllById(request.getClasseIds()));
                formateur.setClasses(classes);
            }

            // Sauvegarder les modifications
            formateur = formateurRepository.save(formateur);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Formateur modifié avec succès");
            
            Map<String, Object> data = new HashMap<>();
            data.put("id", formateur.getId());
            data.put("nom", formateur.getNom());
            data.put("prenom", formateur.getPrenom());
            data.put("email", formateur.getEmail());
            data.put("specialite", formateur.getSpecialite());
            
            // Ajouter les classes
            List<Map<String, Object>> classesData = new ArrayList<>();
            for (Classe classe : formateur.getClasses()) {
                Map<String, Object> classeData = new HashMap<>();
                classeData.put("id", classe.getId());
                classeData.put("nom", classe.getNom());
                classeData.put("description", classe.getDescription());
                classeData.put("dateDebut", classe.getDateDebut());
                classeData.put("dateFin", classe.getDateFin());
                classesData.add(classeData);
            }
            data.put("classes", classesData);
            
            response.put("data", data);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Erreur lors de la modification du formateur", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la modification du formateur", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la modification du formateur: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<?> deleteFormateur(@PathVariable Long id) {
        try {
            log.info("Début de la suppression du formateur avec l'id: {}", id);
            
            // Vérifier si le formateur existe
            Formateur formateur = formateurRepository.findFormateurWithUser(id)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé avec l'id: " + id));

            // 1. Supprimer les associations formateur-classe
            formateurRepository.deleteFormateurClasses(id);

            // 2. Supprimer le formateur d'abord
            formateurRepository.delete(formateur);

            // 3. Supprimer l'utilisateur associé
            if (formateur.getUser() != null) {
                userRepository.delete(formateur.getUser());
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Formateur supprimé avec succès"
            ));

        } catch (RuntimeException e) {
            log.error("Erreur lors de la suppression du formateur", e);
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du formateur", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Erreur lors de la suppression du formateur: " + e.getMessage()
            ));
        }
    }
} 