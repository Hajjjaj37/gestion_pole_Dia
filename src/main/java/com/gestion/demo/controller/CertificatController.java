package com.gestion.demo.controller;

import com.gestion.demo.dto.CertificatDTO;
import com.gestion.demo.dto.AbsenceCertificatDTO;
import com.gestion.demo.model.Certificat;
import com.gestion.demo.model.Absence;
import com.gestion.demo.model.AbsenceCertificat;
import com.gestion.demo.model.Stagiaire;
import com.gestion.demo.repository.CertificatRepository;
import com.gestion.demo.repository.AbsenceCertificatRepository;
import com.gestion.demo.repository.AbsenceRepository;
import com.gestion.demo.repository.StagiaireRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Slf4j
@RestController
@RequestMapping("/api/certificats")
@RequiredArgsConstructor
public class CertificatController {

    private final CertificatRepository certificatRepository;
    private final AbsenceCertificatRepository absenceCertificatRepository;
    private final StagiaireRepository stagiaireRepository;
    private final AbsenceRepository absenceRepository;

    // Créer un nouveau certificat
    @PostMapping
    public ResponseEntity<?> createCertificat(@RequestBody CertificatDTO dto) {
        try {
            log.info("Début de la création d'un certificat");
            
            if (dto.getStagiaireId() == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("L'ID du stagiaire est requis"));
            }

            Optional<Stagiaire> stagiaireOpt = stagiaireRepository.findById(dto.getStagiaireId());
            if (stagiaireOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Stagiaire non trouvé"));
            }

            Certificat certificat = new Certificat();
            certificat.setStagiaire(stagiaireOpt.get());
            certificat.setType(dto.getType());
            certificat.setDescription(dto.getDescription());
            certificat.setDateEmission(dto.getDateEmission());
            certificat.setFichierUrl(dto.getFichierUrl());

            certificat = certificatRepository.save(certificat);

            // Associer les absences si spécifiées
            if (dto.getAbsenceIds() != null && !dto.getAbsenceIds().isEmpty()) {
                for (Long absenceId : dto.getAbsenceIds()) {
                    Optional<Absence> absenceOpt = absenceRepository.findById(absenceId);
                    if (absenceOpt.isPresent()) {
                        AbsenceCertificat absenceCertificat = new AbsenceCertificat();
                        absenceCertificat.setAbsence(absenceOpt.get());
                        absenceCertificat.setCertificat(certificat);
                        absenceCertificatRepository.save(absenceCertificat);
                    }
                }
            }

            return ResponseEntity.ok(createSuccessResponse("Certificat créé avec succès", toDTO(certificat)));

        } catch (Exception e) {
            log.error("Erreur lors de la création du certificat", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de la création du certificat: " + e.getMessage()));
        }
    }

    // Obtenir tous les certificats
    @GetMapping
    public ResponseEntity<?> getAllCertificats() {
        try {
            List<Certificat> certificats = certificatRepository.findAll();
            List<CertificatDTO> certificatDTOs = certificats.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(createSuccessResponse("Certificats récupérés avec succès", certificatDTOs));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des certificats", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de la récupération des certificats: " + e.getMessage()));
        }
    }

    // Obtenir un certificat par son ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCertificatById(@PathVariable Long id) {
        try {
            Optional<Certificat> certificatOpt = certificatRepository.findById(id);
            if (certificatOpt.isEmpty()) {
                return ResponseEntity.status(404).body(createErrorResponse("Certificat non trouvé"));
            }

            return ResponseEntity.ok(createSuccessResponse("Certificat récupéré avec succès", 
                toDTO(certificatOpt.get())));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération du certificat", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de la récupération du certificat: " + e.getMessage()));
        }
    }

    // Obtenir les certificats d'un stagiaire
    @GetMapping("/stagiaire/{stagiaireId}")
    public ResponseEntity<?> getCertificatsByStagiaire(@PathVariable Long stagiaireId) {
        try {
            List<Certificat> certificats = certificatRepository.findByStagiaireId(stagiaireId);
            List<CertificatDTO> certificatDTOs = certificats.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

            return ResponseEntity.ok(createSuccessResponse("Certificats du stagiaire récupérés avec succès", 
                certificatDTOs));

        } catch (Exception e) {
            log.error("Erreur lors de la récupération des certificats du stagiaire", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de la récupération des certificats du stagiaire: " + e.getMessage()));
        }
    }

    // Associer un certificat à une absence
    @PostMapping("/{certificatId}/absences/{absenceId}")
    public ResponseEntity<?> associateCertificatToAbsence(
            @PathVariable Long certificatId,
            @PathVariable Long absenceId,
            @RequestBody(required = false) AbsenceCertificatDTO dto) {
        try {
            Optional<Certificat> certificatOpt = certificatRepository.findById(certificatId);
            Optional<Absence> absenceOpt = absenceRepository.findById(absenceId);

            if (certificatOpt.isEmpty() || absenceOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Certificat ou absence non trouvé"));
            }

            AbsenceCertificat absenceCertificat = new AbsenceCertificat();
            absenceCertificat.setCertificat(certificatOpt.get());
            absenceCertificat.setAbsence(absenceOpt.get());
            if (dto != null) {
                absenceCertificat.setCommentaire(dto.getCommentaire());
            }

            absenceCertificat = absenceCertificatRepository.save(absenceCertificat);

            return ResponseEntity.ok(createSuccessResponse("Certificat associé à l'absence avec succès", 
                toAbsenceCertificatDTO(absenceCertificat)));

        } catch (Exception e) {
            log.error("Erreur lors de l'association du certificat à l'absence", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de l'association du certificat à l'absence: " + e.getMessage()));
        }
    }

    // API pour enregistrer un certificat pour plusieurs absences d'un stagiaire
    @PostMapping(value = "/stagiaire/{stagiaireId}/absences", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCertificatForAbsences(
            @PathVariable Long stagiaireId,
            @RequestParam("type") String type,
            @RequestParam("description") String description,
            @RequestParam("dateEmission") String dateEmissionStr,
            @RequestParam("absenceIds") String absenceIdsJson,
            @RequestParam("fichier") MultipartFile fichier) {
        try {
            log.info("Début de l'enregistrement d'un certificat avec fichier pour les absences du stagiaire: {}", stagiaireId);

            // 1. Sauvegarder le fichier sur le disque (exemple simple)
            String uploadDir = System.getProperty("user.dir") + "/uploads/";
            String fileName = System.currentTimeMillis() + "_" + fichier.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.createDirectories(filePath.getParent());
            fichier.transferTo(filePath.toFile());

            // 2. Convertir la date
            LocalDate dateEmission = LocalDate.parse(dateEmissionStr);

            // 3. Convertir absenceIds
            ObjectMapper mapper = new ObjectMapper();
            List<Long> absenceIds = mapper.readValue(absenceIdsJson, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {});

            // 4. Vérifier stagiaire
            Optional<Stagiaire> stagiaireOpt = stagiaireRepository.findById(stagiaireId);
            if (stagiaireOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Stagiaire non trouvé"));
            }

            // 5. Vérifier absences
            List<Absence> absences = new ArrayList<>();
            for (Long absenceId : absenceIds) {
                Optional<Absence> absenceOpt = absenceRepository.findById(absenceId);
                if (absenceOpt.isEmpty()) {
                    return ResponseEntity.badRequest().body(createErrorResponse("Absence non trouvée avec l'ID: " + absenceId));
                }
                Absence absence = absenceOpt.get();
                if (!absence.getStagiaire().getId().equals(stagiaireId)) {
                    return ResponseEntity.badRequest().body(createErrorResponse("L'absence " + absenceId + " n'appartient pas au stagiaire"));
                }
                absences.add(absence);
            }

            // 6. Créer le certificat
            Certificat certificat = new Certificat();
            certificat.setStagiaire(stagiaireOpt.get());
            certificat.setType(type);
            certificat.setDescription(description);
            certificat.setDateEmission(dateEmission);
            certificat.setFichierUrl("/uploads/" + fileName); // Stocke le chemin du fichier

            certificat = certificatRepository.save(certificat);

            // 7. Associer le certificat aux absences
            for (Absence absence : absences) {
                AbsenceCertificat absenceCertificat = new AbsenceCertificat();
                absenceCertificat.setAbsence(absence);
                absenceCertificat.setCertificat(certificat);
                absenceCertificatRepository.save(absenceCertificat);
            }

            return ResponseEntity.ok(createSuccessResponse(
                "Certificat enregistré avec succès avec fichier pour " + absences.size() + " absences",
                toDTO(certificat)
            ));

        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement du certificat avec fichier", e);
            return ResponseEntity.status(500)
                .body(createErrorResponse("Erreur lors de l'enregistrement du certificat: " + e.getMessage()));
        }
    }

    // Méthodes utilitaires
    private CertificatDTO toDTO(Certificat certificat) {
        if (certificat == null) {
            return null;
        }

        CertificatDTO dto = new CertificatDTO();
        dto.setId(certificat.getId());
        
        if (certificat.getStagiaire() != null) {
            dto.setStagiaireId(certificat.getStagiaire().getId());
            dto.setStagiaireNom(certificat.getStagiaire().getNom());
            dto.setStagiairePrenom(certificat.getStagiaire().getPrenom());
        }
        
        dto.setType(certificat.getType());
        dto.setDescription(certificat.getDescription());
        dto.setDateEmission(certificat.getDateEmission());
        dto.setFichierUrl(certificat.getFichierUrl());
        
        if (certificat.getAbsenceCertificats() != null) {
            dto.setAbsenceIds(certificat.getAbsenceCertificats().stream()
                .map(ac -> ac.getAbsence().getId())
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    private AbsenceCertificatDTO toAbsenceCertificatDTO(AbsenceCertificat absenceCertificat) {
        if (absenceCertificat == null) {
            return null;
        }

        AbsenceCertificatDTO dto = new AbsenceCertificatDTO();
        dto.setId(absenceCertificat.getId());
        dto.setAbsenceId(absenceCertificat.getAbsence().getId());
        dto.setCertificatId(absenceCertificat.getCertificat().getId());
        dto.setCommentaire(absenceCertificat.getCommentaire());
        
        return dto;
    }

    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    @GetMapping("/fichier/{certificatId}")
    public ResponseEntity<?> getCertificatFile(@PathVariable Long certificatId) {
        try {
            Optional<Certificat> certificatOpt = certificatRepository.findById(certificatId);
            if (certificatOpt.isEmpty()) {
                return ResponseEntity.status(404).body(createErrorResponse("Certificat non trouvé"));
            }
            Certificat certificat = certificatOpt.get();
            String fichierUrl = certificat.getFichierUrl();
            if (fichierUrl == null || fichierUrl.isEmpty()) {
                return ResponseEntity.status(404).body(createErrorResponse("Aucun fichier associé à ce certificat"));
            }

            // Chemin absolu du fichier sur le disque
            String absolutePath = System.getProperty("user.dir") + fichierUrl;
            Path filePath = Paths.get(absolutePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.status(404).body(createErrorResponse("Fichier non trouvé sur le serveur"));
            }

            // Déduire le type MIME
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("Erreur lors de la récupération du fichier: " + e.getMessage()));
        }
    }
} 