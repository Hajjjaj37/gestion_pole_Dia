package com.gestion.demo.controller;

import com.gestion.demo.model.Pub;
import com.gestion.demo.model.Classe;
import com.gestion.demo.model.Gestionnaire;
import com.gestion.demo.repository.PubRepository;
import com.gestion.demo.repository.ClasseRepository;
import com.gestion.demo.repository.GestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pubs")
public class PubController {

    @Autowired
    private PubRepository pubRepository;
    
    @Autowired
    private ClasseRepository classeRepository;
    
    @Autowired
    private GestionnaireRepository gestionnaireRepository;

    @GetMapping
    public List<Pub> getAllPubs() {
        return pubRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pub> getPubById(@PathVariable Long id) {
        return pubRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createPubWithFormData(
            @RequestParam("titre") String titre,
            @RequestParam("contenu") String contenu,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam("gestionnaireId") Long gestionnaireId,
            @RequestParam("classesIds") String classesIdsStr) {
        
        try {
            // Validation des entrées
            if (titre == null || titre.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le titre est requis");
            }
            if (contenu == null || contenu.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Le contenu est requis");
            }
            if (gestionnaireId == null) {
                return ResponseEntity.badRequest().body("L'ID du gestionnaire est requis");
            }

            // Convertir la chaîne JSON en liste de Long
            List<Long> classesIds;
            try {
                ObjectMapper mapper = new ObjectMapper();
                classesIds = mapper.readValue(classesIdsStr, new TypeReference<List<Long>>() {});
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Format invalide pour classesIds. Utilisez le format [1,2,3]");
            }

            if (classesIds == null || classesIds.isEmpty()) {
                return ResponseEntity.badRequest().body("Au moins une classe est requise");
            }

            // Créer une nouvelle instance de Pub
            Pub pub = new Pub();
            pub.setTitre(titre);
            pub.setContenu(contenu);
            
            // Gérer l'image
            if (image != null && !image.isEmpty()) {
                // Créer le dossier images dans le répertoire de travail
                String uploadDir = System.getProperty("user.dir") + File.separator + "images";
                File imagesDir = new File(uploadDir);
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs(); // Utiliser mkdirs() au lieu de mkdir()
                }
                
                // Générer un nom de fichier unique
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                String imagePath = uploadDir + File.separator + fileName;
                
                // Sauvegarder le fichier
                File dest = new File(imagePath);
                image.transferTo(dest);
                
                // Stocker le chemin relatif dans la base de données
                pub.setImage("images/" + fileName);
            }
            
            // Récupérer le gestionnaire
            Gestionnaire gestionnaire = gestionnaireRepository.findById(gestionnaireId)
                .orElseThrow(() -> new RuntimeException("Gestionnaire non trouvé avec l'id: " + gestionnaireId));
            pub.setGestionnaire(gestionnaire);
            
            // Récupérer et ajouter les classes une par une
            for (Long classeId : classesIds) {
                Classe classe = classeRepository.findById(classeId)
                    .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'id: " + classeId));
                pub.addClasse(classe);
            }
            
            // Sauvegarder la pub
            Pub savedPub = pubRepository.save(pub);
            
            // Créer une réponse simplifiée
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedPub.getId());
            response.put("titre", savedPub.getTitre());
            response.put("contenu", savedPub.getContenu());
            response.put("image", savedPub.getImage());
            response.put("gestionnaire", Map.of(
                "id", savedPub.getGestionnaire().getId(),
                "nom", savedPub.getGestionnaire().getNom(),
                "prenom", savedPub.getGestionnaire().getPrenom(),
                "email", savedPub.getGestionnaire().getEmail()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Erreur lors de la création de la pub: " + 
                (e.getMessage() != null ? e.getMessage() : "Erreur inconnue");
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMessage);
        }
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<?> updatePub(
            @PathVariable Long id,
            @RequestParam(value = "titre", required = false) String titre,
            @RequestParam(value = "contenu", required = false) String contenu,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "gestionnaireId", required = false) Long gestionnaireId,
            @RequestParam(value = "classesIds", required = false) String classesIdsStr) {
        
        try {
            // Récupérer la pub existante
            Pub existingPub = pubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pub non trouvée avec l'id: " + id));

            // Mettre à jour les champs si fournis
            if (titre != null && !titre.trim().isEmpty()) {
                existingPub.setTitre(titre);
            }
            
            if (contenu != null && !contenu.trim().isEmpty()) {
                existingPub.setContenu(contenu);
            }

            // Gérer la mise à jour de l'image
            if (image != null && !image.isEmpty()) {
                // Supprimer l'ancienne image si elle existe
                if (existingPub.getImage() != null && !existingPub.getImage().isEmpty()) {
                    String oldImagePath = System.getProperty("user.dir") + File.separator + existingPub.getImage();
                    File oldImageFile = new File(oldImagePath);
                    if (oldImageFile.exists()) {
                        oldImageFile.delete();
                    }
                }

                // Sauvegarder la nouvelle image
                String uploadDir = System.getProperty("user.dir") + File.separator + "images";
                File imagesDir = new File(uploadDir);
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs();
                }
                
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                String imagePath = uploadDir + File.separator + fileName;
                
                File dest = new File(imagePath);
                image.transferTo(dest);
                
                existingPub.setImage("images/" + fileName);
            }

            // Mettre à jour le gestionnaire si fourni
            if (gestionnaireId != null) {
                Gestionnaire gestionnaire = gestionnaireRepository.findById(gestionnaireId)
                    .orElseThrow(() -> new RuntimeException("Gestionnaire non trouvé avec l'id: " + gestionnaireId));
                existingPub.setGestionnaire(gestionnaire);
            }

            // Mettre à jour les classes si fournies
            if (classesIdsStr != null && !classesIdsStr.isEmpty()) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    List<Long> classesIds = mapper.readValue(classesIdsStr, new TypeReference<List<Long>>() {});
                    
                    // Vider la liste existante des classes
                    existingPub.getClasses().clear();
                    
                    // Ajouter les nouvelles classes
                    for (Long classeId : classesIds) {
                        Classe classe = classeRepository.findById(classeId)
                            .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'id: " + classeId));
                        existingPub.addClasse(classe);
                    }
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body("Format invalide pour classesIds. Utilisez le format [1,2,3]");
                }
            }

            // Sauvegarder les modifications
            Pub updatedPub = pubRepository.save(existingPub);
            
            // Créer une réponse simplifiée
            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedPub.getId());
            response.put("titre", updatedPub.getTitre());
            response.put("contenu", updatedPub.getContenu());
            response.put("image", updatedPub.getImage());
            response.put("gestionnaire", Map.of(
                "id", updatedPub.getGestionnaire().getId(),
                "nom", updatedPub.getGestionnaire().getNom(),
                "prenom", updatedPub.getGestionnaire().getPrenom(),
                "email", updatedPub.getGestionnaire().getEmail()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Erreur lors de la mise à jour de la pub: " + 
                (e.getMessage() != null ? e.getMessage() : "Erreur inconnue");
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMessage);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePub(@PathVariable Long id) {
        try {
            // Vérifier si la pub existe
            Pub pub = pubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pub non trouvée avec l'id: " + id));

            // Supprimer l'image associée si elle existe
            if (pub.getImage() != null && !pub.getImage().isEmpty()) {
                String imagePath = System.getProperty("user.dir") + File.separator + pub.getImage();
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }

            // Supprimer la pub de la base de données
            pubRepository.delete(pub);

            return ResponseEntity.ok()
                .body(Map.of(
                    "message", "Pub supprimée avec succès",
                    "id", id
                ));

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Erreur lors de la suppression de la pub: " + 
                (e.getMessage() != null ? e.getMessage() : "Erreur inconnue");
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMessage);
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<?> getPubImage(@PathVariable Long id) {
        try {
            Pub pub = pubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pub non trouvée avec l'id: " + id));
            
            if (pub.getImage() == null || pub.getImage().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            // Construire le chemin complet de l'image
            String imagePath = System.getProperty("user.dir") + File.separator + pub.getImage();
            File imageFile = new File(imagePath);
            
            if (!imageFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            // Lire le fichier image
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            
            // Déterminer le type MIME de l'image
            String contentType = Files.probeContentType(imageFile.toPath());
            if (contentType == null) {
                contentType = "image/jpeg"; // Type par défaut
            }
            
            // Créer les headers de la réponse
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageBytes.length);
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la récupération de l'image: " + e.getMessage());
        }
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<?> getPubsByClasse(@PathVariable Long classeId) {
        try {
            // Vérifier si la classe existe
            Classe classe = classeRepository.findById(classeId)
                .orElseThrow(() -> new RuntimeException("Classe non trouvée avec l'id: " + classeId));

            // Récupérer toutes les pubs de la classe
            List<Pub> pubs = pubRepository.findByClassesId(classeId);

            // Créer une liste de réponses simplifiées
            List<Map<String, Object>> response = pubs.stream()
                .map(pub -> {
                    Map<String, Object> pubMap = new HashMap<>();
                    pubMap.put("id", pub.getId());
                    pubMap.put("titre", pub.getTitre());
                    pubMap.put("contenu", pub.getContenu());
                    pubMap.put("image", pub.getImage());
                    pubMap.put("gestionnaire", Map.of(
                        "id", pub.getGestionnaire().getId(),
                        "nom", pub.getGestionnaire().getNom(),
                        "prenom", pub.getGestionnaire().getPrenom(),
                        "email", pub.getGestionnaire().getEmail()
                    ));
                    return pubMap;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Erreur lors de la récupération des pubs: " + 
                (e.getMessage() != null ? e.getMessage() : "Erreur inconnue");
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMessage);
        }
    }
} 