package com.gestion.demo.service;

import com.gestion.demo.dto.RegionalDTO;
import com.gestion.demo.dto.RegionalResponseDTO;
import com.gestion.demo.dto.SimpleClasseDTO;
import com.gestion.demo.dto.SimpleFormateurDTO;
import com.gestion.demo.dto.SimpleGestionnaireDTO;
import com.gestion.demo.model.Regional;
import com.gestion.demo.model.Gestionnaire;
import com.gestion.demo.model.Classe;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.Stagiaire;
import com.gestion.demo.repository.RegionalRepository;
import com.gestion.demo.repository.GestionnaireRepository;
import com.gestion.demo.repository.ClasseRepository;
import com.gestion.demo.repository.FormateurRepository;
import com.gestion.demo.repository.StagiaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@Service
public class RegionalService {

    @Autowired
    private RegionalRepository regionalRepository;
    @Autowired
    private GestionnaireRepository gestionnaireRepository;
    @Autowired
    private ClasseRepository classeRepository;
    @Autowired
    private FormateurRepository formateurRepository;
    @Autowired
    private StagiaireRepository stagiaireRepository;

    // Création d'un regional
    public Regional createRegional(RegionalDTO dto) {
        Regional regional = new Regional();
        regional.setNom(dto.getNom());

        Gestionnaire gestionnaire = gestionnaireRepository.findById(dto.getGestionnaireId())
                .orElseThrow(() -> new RuntimeException("Gestionnaire non trouvé"));
        regional.setGestionnaire(gestionnaire);

        Set<Classe> classes = new HashSet<>();
        if (dto.getClasseIds() != null) {
            classes.addAll(classeRepository.findAllById(dto.getClasseIds()));
        }
        regional.setClasses(classes);

        Set<Formateur> formateurs = new HashSet<>();
        if (dto.getFormateurIds() != null) {
            formateurs.addAll(formateurRepository.findAllById(dto.getFormateurIds()));
        }
        regional.setFormateurs(formateurs);

        return regionalRepository.save(regional);
    }

    // Mapping vers le DTO de réponse
    public RegionalResponseDTO toDto(Regional regional) {
        RegionalResponseDTO dto = new RegionalResponseDTO();
        dto.setId(regional.getId());
        dto.setNom(regional.getNom());

        // Gestionnaire
        if (regional.getGestionnaire() != null) {
            SimpleGestionnaireDTO gest = new SimpleGestionnaireDTO();
            gest.setId(regional.getGestionnaire().getId());
            gest.setNom(regional.getGestionnaire().getNom());
            gest.setPrenom(regional.getGestionnaire().getPrenom());
            dto.setGestionnaire(gest);
        }

        // Classes
        if (regional.getClasses() != null) {
            Set<SimpleClasseDTO> classes = regional.getClasses().stream().map(cl -> {
                SimpleClasseDTO c = new SimpleClasseDTO();
                c.setId(cl.getId());
                c.setNom(cl.getNom());
                return c;
            }).collect(Collectors.toSet());
            dto.setClasses(classes);
        }

        // Formateurs
        if (regional.getFormateurs() != null) {
            Set<SimpleFormateurDTO> formateurs = regional.getFormateurs().stream().map(f -> {
                SimpleFormateurDTO fDto = new SimpleFormateurDTO();
                fDto.setId(f.getId());
                fDto.setNom(f.getNom());
                fDto.setPrenom(f.getPrenom());
                return fDto;
            }).collect(Collectors.toSet());
            dto.setFormateurs(formateurs);
        }

        return dto;
    }

    // Liste de tous les regionals
    public List<Regional> getAllRegionals() {
        return regionalRepository.findAll();
    }

    // Un regional par id
    public Regional getRegional(Long id) {
        return regionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regional non trouvé"));
    }

    // Modifier un regional
    public Regional updateRegional(Long id, RegionalDTO dto) {
        Regional regional = regionalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Regional non trouvé"));

        if (dto.getNom() != null) regional.setNom(dto.getNom());

        if (dto.getGestionnaireId() != null) {
            Gestionnaire gestionnaire = gestionnaireRepository.findById(dto.getGestionnaireId())
                    .orElseThrow(() -> new RuntimeException("Gestionnaire non trouvé"));
            regional.setGestionnaire(gestionnaire);
        }

        if (dto.getClasseIds() != null) {
            Set<Classe> classes = new HashSet<>(classeRepository.findAllById(dto.getClasseIds()));
            regional.setClasses(classes);
        }

        if (dto.getFormateurIds() != null) {
            Set<Formateur> formateurs = new HashSet<>(formateurRepository.findAllById(dto.getFormateurIds()));
            regional.setFormateurs(formateurs);
        }

        return regionalRepository.save(regional);
    }

    // Supprimer un regional
    public void deleteRegional(Long id) {
        regionalRepository.deleteById(id);
    }

    // Regionals par formateur
    public List<RegionalResponseDTO> getRegionalsByFormateur(Long formateurId) {
        List<Regional> regionals = regionalRepository.findByFormateurs_Id(formateurId);
        return regionals.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Regionals par classe
    public List<RegionalResponseDTO> getRegionalsByClasse(Long classeId) {
        List<Regional> regionals = regionalRepository.findByClasses_Id(classeId);
        return regionals.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void affecterRegionalAuxStagiairesDeClasse(Long regionalId, Long classeId) {
        Regional regional = regionalRepository.findById(regionalId)
            .orElseThrow(() -> new RuntimeException("Regional non trouvé"));
        List<Stagiaire> stagiaires = stagiaireRepository.findByClasse_Id(classeId);
        for (Stagiaire stagiaire : stagiaires) {
            if (stagiaire.getRegionals() == null) {
                stagiaire.setRegionals(new HashSet<>());
            }
            stagiaire.getRegionals().add(regional);
            stagiaireRepository.save(stagiaire);
        }
    }

    public List<RegionalResponseDTO> getRegionalsOfStagiairesByClasse(Long classeId) {
        List<Stagiaire> stagiaires = stagiaireRepository.findByClasse_Id(classeId);
        Set<Regional> regionals = new HashSet<>();
        for (Stagiaire s : stagiaires) {
            if (s.getRegionals() != null) {
                regionals.addAll(s.getRegionals());
            }
        }
        return regionals.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getRegionalsParStagiaireDeClasse(Long classeId) {
        List<Stagiaire> stagiaires = stagiaireRepository.findByClasse_Id(classeId);
        return stagiaires.stream()
            .map(stagiaire -> Map.of(
                "stagiaire", Map.of(
                    "id", stagiaire.getId(),
                    "nom", stagiaire.getNom(),
                    "prenom", stagiaire.getPrenom(),
                    "email", stagiaire.getEmail()
                ),
                "regionals", stagiaire.getRegionals() == null ? List.of() :
                    stagiaire.getRegionals().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
    }
} 