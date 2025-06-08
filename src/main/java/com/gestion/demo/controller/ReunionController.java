package com.gestion.demo.controller;

import com.gestion.demo.dto.ReunionDTO;
import com.gestion.demo.dto.ReunionResponseDTO;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.Gestionnaire;
import com.gestion.demo.model.Reunion;
import com.gestion.demo.repository.FormateurRepository;
import com.gestion.demo.repository.GestionnaireRepository;
import com.gestion.demo.repository.ReunionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reunions")
@RequiredArgsConstructor
public class ReunionController {

    private final ReunionRepository reunionRepository;
    private final FormateurRepository formateurRepository;
    private final GestionnaireRepository gestionnaireRepository;

    @PostMapping
    public ResponseEntity<?> createReunion(@RequestBody ReunionDTO dto) {
        Reunion reunion = new Reunion();
        reunion.setSujet(dto.getSujet());
        reunion.setDateHeure(dto.getDateHeure());
        reunion.setLieu(dto.getLieu());

        Set<Formateur> formateurs = new HashSet<>();
        if (dto.getFormateurIds() != null) {
            for (Long id : dto.getFormateurIds()) {
                formateurRepository.findById(id).ifPresent(formateurs::add);
            }
        }
        reunion.setFormateurs(formateurs);

        Set<Gestionnaire> gestionnaires = new HashSet<>();
        if (dto.getGestionnaireIds() != null) {
            for (Long id : dto.getGestionnaireIds()) {
                gestionnaireRepository.findById(id).ifPresent(gestionnaires::add);
            }
        }
        reunion.setGestionnaires(gestionnaires);

        Reunion saved = reunionRepository.save(reunion);

        // Mapping vers DTO pour éviter la récursivité
        ReunionResponseDTO response = new ReunionResponseDTO();
        response.setId(saved.getId());
        response.setSujet(saved.getSujet());
        response.setDateHeure(saved.getDateHeure());
        response.setLieu(saved.getLieu());
        response.setFormateurs(saved.getFormateurs().stream().map(Formateur::getNom).collect(Collectors.toList()));
        response.setGestionnaires(saved.getGestionnaires().stream().map(Gestionnaire::getNom).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/formateur/{formateurId}")
    public ResponseEntity<?> getReunionsByFormateur(@PathVariable Long formateurId) {
        Optional<Formateur> formateur = formateurRepository.findById(formateurId);
        if (formateur.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ReunionResponseDTO> reunions = reunionRepository.findByFormateursId(formateurId)
            .stream()
            .map(reunion -> {
                ReunionResponseDTO dto = new ReunionResponseDTO();
                dto.setId(reunion.getId());
                dto.setSujet(reunion.getSujet());
                dto.setDateHeure(reunion.getDateHeure());
                dto.setLieu(reunion.getLieu());
                dto.setFormateurs(reunion.getFormateurs().stream().map(Formateur::getNom).collect(Collectors.toList()));
                dto.setGestionnaires(reunion.getGestionnaires().stream().map(Gestionnaire::getNom).collect(Collectors.toList()));
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(reunions);
    }

    @GetMapping("/gestionnaire/{gestionnaireId}")
    public ResponseEntity<?> getReunionsByGestionnaire(@PathVariable Long gestionnaireId) {
        Optional<Gestionnaire> gestionnaire = gestionnaireRepository.findById(gestionnaireId);
        if (gestionnaire.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<ReunionResponseDTO> reunions = reunionRepository.findByGestionnairesId(gestionnaireId)
            .stream()
            .map(reunion -> {
                ReunionResponseDTO dto = new ReunionResponseDTO();
                dto.setId(reunion.getId());
                dto.setSujet(reunion.getSujet());
                dto.setDateHeure(reunion.getDateHeure());
                dto.setLieu(reunion.getLieu());
                dto.setFormateurs(reunion.getFormateurs().stream().map(Formateur::getNom).collect(Collectors.toList()));
                dto.setGestionnaires(reunion.getGestionnaires().stream().map(Gestionnaire::getNom).collect(Collectors.toList()));
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(reunions);
    }

    @GetMapping("/all-with-formateurs")
    public ResponseEntity<?> getAllReunionsWithFormateurs() {
        List<ReunionResponseDTO> reunions = reunionRepository.findAll()
            .stream()
            .map(reunion -> {
                ReunionResponseDTO dto = new ReunionResponseDTO();
                dto.setId(reunion.getId());
                dto.setSujet(reunion.getSujet());
                dto.setDateHeure(reunion.getDateHeure());
                dto.setLieu(reunion.getLieu());
                dto.setFormateurs(reunion.getFormateurs().stream().map(Formateur::getNom).collect(Collectors.toList()));
                dto.setGestionnaires(reunion.getGestionnaires().stream().map(Gestionnaire::getNom).collect(Collectors.toList()));
                return dto;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(reunions);
    }

    @GetMapping("/gestionnaires-participants")
    public ResponseEntity<?> getGestionnairesInReunions() {
        List<Gestionnaire> gestionnaires = gestionnaireRepository.findAll()
            .stream()
            .filter(gestionnaire -> gestionnaire.getReunions() != null && !gestionnaire.getReunions().isEmpty())
            .collect(Collectors.toList());

        List<Map<String, Object>> response = gestionnaires.stream()
            .map(gestionnaire -> {
                Map<String, Object> gestionnaireInfo = new HashMap<>();
                gestionnaireInfo.put("id", gestionnaire.getId());
                gestionnaireInfo.put("nom", gestionnaire.getNom());
                gestionnaireInfo.put("nombreReunions", gestionnaire.getReunions() != null ? gestionnaire.getReunions().size() : 0);
                gestionnaireInfo.put("reunions", gestionnaire.getReunions() != null ? gestionnaire.getReunions().stream()
                    .map(reunion -> {
                        Map<String, Object> reunionInfo = new HashMap<>();
                        reunionInfo.put("id", reunion.getId());
                        reunionInfo.put("sujet", reunion.getSujet());
                        reunionInfo.put("dateHeure", reunion.getDateHeure());
                        return reunionInfo;
                    })
                    .collect(Collectors.toList()) : new ArrayList<>());
                return gestionnaireInfo;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/gestionnaires-en-reunion")
    public ResponseEntity<?> getGestionnairesEnReunion() {
        List<Gestionnaire> gestionnairesAvecReunions = gestionnaireRepository.findAllWithReunions();
        System.out.println("Nombre de gestionnaires avec réunions: " + gestionnairesAvecReunions.size());

        List<Map<String, Object>> response = gestionnairesAvecReunions
            .stream()
            .map(gestionnaire -> {
                Map<String, Object> gestionnaireInfo = new HashMap<>();
                gestionnaireInfo.put("id", gestionnaire.getId());
                gestionnaireInfo.put("nom", gestionnaire.getNom());
                gestionnaireInfo.put("email", gestionnaire.getEmail());
                gestionnaireInfo.put("nombreReunions", gestionnaire.getReunions().size());
                gestionnaireInfo.put("reunions", gestionnaire.getReunions().stream()
                    .map(reunion -> {
                        Map<String, Object> reunionInfo = new HashMap<>();
                        reunionInfo.put("id", reunion.getId());
                        reunionInfo.put("sujet", reunion.getSujet());
                        reunionInfo.put("dateHeure", reunion.getDateHeure());
                        reunionInfo.put("lieu", reunion.getLieu());
                        return reunionInfo;
                    })
                    .collect(Collectors.toList()));
                return gestionnaireInfo;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-data")
    public ResponseEntity<?> testData() {
        Map<String, Object> result = new HashMap<>();
        
        // Vérifier les gestionnaires
        List<Gestionnaire> gestionnaires = gestionnaireRepository.findAll();
        result.put("nombreGestionnaires", gestionnaires.size());
        result.put("gestionnaires", gestionnaires.stream()
            .map(g -> {
                Map<String, Object> gInfo = new HashMap<>();
                gInfo.put("id", g.getId());
                gInfo.put("nom", g.getNom());
                gInfo.put("nombreReunions", g.getReunions() != null ? g.getReunions().size() : 0);
                return gInfo;
            })
            .collect(Collectors.toList()));

        // Vérifier les réunions
        List<Reunion> reunions = reunionRepository.findAll();
        result.put("nombreReunions", reunions.size());
        result.put("reunions", reunions.stream()
            .map(r -> {
                Map<String, Object> rInfo = new HashMap<>();
                rInfo.put("id", r.getId());
                rInfo.put("sujet", r.getSujet());
                rInfo.put("nombreGestionnaires", r.getGestionnaires() != null ? r.getGestionnaires().size() : 0);
                return rInfo;
            })
            .collect(Collectors.toList()));

        return ResponseEntity.ok(result);
    }

    @GetMapping("/reunions-gestionnaires")
    public ResponseEntity<?> getReunionsGestionnaires() {
        List<Map<String, Object>> response = reunionRepository.findAll()
            .stream()
            .filter(reunion -> reunion.getGestionnaires() != null && !reunion.getGestionnaires().isEmpty())
            .map(reunion -> {
                Map<String, Object> reunionInfo = new HashMap<>();
                reunionInfo.put("id", reunion.getId());
                reunionInfo.put("sujet", reunion.getSujet());
                reunionInfo.put("dateHeure", reunion.getDateHeure());
                reunionInfo.put("lieu", reunion.getLieu());
                reunionInfo.put("nombreGestionnaires", reunion.getGestionnaires().size());
                reunionInfo.put("gestionnaires", reunion.getGestionnaires().stream()
                    .map(gestionnaire -> {
                        Map<String, Object> gestionnaireInfo = new HashMap<>();
                        gestionnaireInfo.put("id", gestionnaire.getId());
                        gestionnaireInfo.put("nom", gestionnaire.getNom());
                        gestionnaireInfo.put("email", gestionnaire.getEmail());
                        return gestionnaireInfo;
                    })
                    .collect(Collectors.toList()));
                return reunionInfo;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reunions-formateurs")
    public ResponseEntity<?> getReunionsFormateurs() {
        List<Map<String, Object>> response = reunionRepository.findAll()
            .stream()
            .filter(reunion -> reunion.getFormateurs() != null && !reunion.getFormateurs().isEmpty())
            .map(reunion -> {
                Map<String, Object> reunionInfo = new HashMap<>();
                reunionInfo.put("id", reunion.getId());
                reunionInfo.put("sujet", reunion.getSujet());
                reunionInfo.put("dateHeure", reunion.getDateHeure());
                reunionInfo.put("lieu", reunion.getLieu());
                reunionInfo.put("nombreFormateurs", reunion.getFormateurs().size());
                reunionInfo.put("formateurs", reunion.getFormateurs().stream()
                    .map(formateur -> {
                        Map<String, Object> formateurInfo = new HashMap<>();
                        formateurInfo.put("id", formateur.getId());
                        formateurInfo.put("nom", formateur.getNom());
                        formateurInfo.put("email", formateur.getEmail());
                        return formateurInfo;
                    })
                    .collect(Collectors.toList()));
                return reunionInfo;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReunion(@PathVariable Long id, @RequestBody ReunionDTO dto) {
        Optional<Reunion> reunionOpt = reunionRepository.findById(id);
        if (reunionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Reunion reunion = reunionOpt.get();
        
        // Mise à jour des champs de base
        reunion.setSujet(dto.getSujet());
        reunion.setDateHeure(dto.getDateHeure());
        reunion.setLieu(dto.getLieu());

        // Mise à jour des formateurs
        if (dto.getFormateurIds() != null) {
            Set<Formateur> formateurs = new HashSet<>();
            for (Long formateurId : dto.getFormateurIds()) {
                formateurRepository.findById(formateurId).ifPresent(formateurs::add);
            }
            reunion.setFormateurs(formateurs);
        }

        // Mise à jour des gestionnaires
        if (dto.getGestionnaireIds() != null) {
            Set<Gestionnaire> gestionnaires = new HashSet<>();
            for (Long gestionnaireId : dto.getGestionnaireIds()) {
                gestionnaireRepository.findById(gestionnaireId).ifPresent(gestionnaires::add);
            }
            reunion.setGestionnaires(gestionnaires);
        }

        Reunion updated = reunionRepository.save(reunion);

        // Mapping vers DTO pour la réponse
        ReunionResponseDTO response = new ReunionResponseDTO();
        response.setId(updated.getId());
        response.setSujet(updated.getSujet());
        response.setDateHeure(updated.getDateHeure());
        response.setLieu(updated.getLieu());
        response.setFormateurs(updated.getFormateurs().stream().map(Formateur::getNom).collect(Collectors.toList()));
        response.setGestionnaires(updated.getGestionnaires().stream().map(Gestionnaire::getNom).collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReunion(@PathVariable Long id) {
        Optional<Reunion> reunionOpt = reunionRepository.findById(id);
        if (reunionOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Supprimer la réunion
        reunionRepository.deleteById(id);

        return ResponseEntity.ok().body("Réunion supprimée avec succès");
    }
} 