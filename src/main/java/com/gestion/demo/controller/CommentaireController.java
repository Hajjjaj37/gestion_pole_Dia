package com.gestion.demo.controller;

import com.gestion.demo.dto.CommentaireDTO;
import com.gestion.demo.model.Commentaire;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.service.CommentaireService;
import com.gestion.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import com.gestion.demo.dto.CommentaireSimpleDTO;

@RestController
@RequestMapping("/api/commentaires")
public class CommentaireController {

    private static final Logger logger = LoggerFactory.getLogger(CommentaireController.class);

    @Autowired
    private CommentaireService commentaireService;

    @Autowired
    private EmailService emailService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> creerCommentaire(@RequestBody CommentaireDTO commentaireDTO) {
        try {
            // Créer le commentaire
            Commentaire commentaire = commentaireService.creerCommentaire(commentaireDTO);
            
            // Envoyer les notifications par email aux formateurs
            if (commentaireDTO.getFormateurIds() != null && !commentaireDTO.getFormateurIds().isEmpty()) {
                String sujet = "Nouveau commentaire pour la classe " + commentaire.getClasse().getNom();
                String message = String.format(
                    "Un nouveau commentaire a été ajouté :\n\n" +
                    "Contenu : %s\n" +
                    "Date : %s\n" +
                    "Gestionnaire : %s %s\n" +
                    "Classe : %s",
                    commentaire.getContenu(),
                    commentaire.getDateCreation(),
                    commentaire.getGestionnaire().getNom(),
                    commentaire.getGestionnaire().getPrenom(),
                    commentaire.getClasse().getNom()
                );

                // Envoyer l'email à chaque formateur
                for (Formateur formateur : commentaire.getFormateurs()) {
                    try {
                        logger.info("Tentative d'envoi d'email à {}", formateur.getEmail());
                        emailService.envoyerNotification(
                            formateur.getEmail(),
                            sujet,
                            message
                        );
                        logger.info("Email envoyé avec succès à {}", formateur.getEmail());
                    } catch (Exception e) {
                        logger.error("Erreur lors de l'envoi de l'email à {}: {}", 
                            formateur.getEmail(), e.getMessage());
                        // Ne pas propager l'erreur pour éviter d'interrompre le processus
                    }
                }
            }

            // Créer la réponse
            Map<String, Object> response = Map.of(
                "success", true,
                "message", "Commentaire créé avec succès",
                "data", Map.of(
                    "id", commentaire.getId(),
                    "contenu", commentaire.getContenu(),
                    "dateCreation", commentaire.getDateCreation(),
                    "gestionnaire", Map.of(
                        "id", commentaire.getGestionnaire().getId(),
                        "nom", commentaire.getGestionnaire().getNom(),
                        "prenom", commentaire.getGestionnaire().getPrenom(),
                        "email", commentaire.getGestionnaire().getEmail()
                    ),
                    "classe", Map.of(
                        "id", commentaire.getClasse().getId(),
                        "nom", commentaire.getClasse().getNom()
                    ),
                    "formateurs", commentaire.getFormateurs().stream()
                        .map(f -> Map.of(
                            "id", f.getId(),
                            "nom", f.getNom(),
                            "prenom", f.getPrenom(),
                            "email", f.getEmail()
                        ))
                        .toList()
                )
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur lors de la création du commentaire: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la création du commentaire: " + e.getMessage()
                ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCommentaire(@PathVariable Long id) {
        try {
            Commentaire commentaire = commentaireService.getCommentaire(id);
            return ResponseEntity.ok(commentaire);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Commentaire non trouvé"));
        }
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<?> getCommentairesByClasse(@PathVariable Long classeId) {
        try {
            List<Commentaire> commentaires = commentaireService.getCommentairesByClasse(classeId);
            return ResponseEntity.ok(commentaires);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erreur lors de la récupération des commentaires"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> supprimerCommentaire(@PathVariable Long id) {
        try {
            commentaireService.supprimerCommentaire(id);
            return ResponseEntity.ok(Map.of("message", "Commentaire supprimé avec succès"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Erreur lors de la suppression du commentaire"));
        }
    }

    @GetMapping("/formateur/{formateurId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> getCommentairesByFormateur(@PathVariable Long formateurId) {
        try {
            List<CommentaireSimpleDTO> commentaires = commentaireService.getCommentairesByFormateur(formateurId);
            return ResponseEntity.ok(commentaires);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Erreur lors de la récupération des commentaires du formateur"));
        }
    }

    @GetMapping("/gestionnaire/{gestionnaireId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> getCommentairesByGestionnaire(@PathVariable Long gestionnaireId) {
        try {
            List<CommentaireSimpleDTO> commentaires = commentaireService.getCommentairesByGestionnaire(gestionnaireId);
            return ResponseEntity.ok(commentaires);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Erreur lors de la récupération des commentaires du gestionnaire"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GESTIONNAIRE')")
    public ResponseEntity<?> updateCommentaire(@PathVariable Long id, @RequestBody CommentaireDTO commentaireDTO) {
        try {
            Commentaire updated = commentaireService.updateCommentaire(id, commentaireDTO);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Commentaire modifié avec succès",
                "data", Map.of(
                    "id", updated.getId(),
                    "contenu", updated.getContenu(),
                    "dateCreation", updated.getDateCreation()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Erreur lors de la modification du commentaire : " + e.getMessage()));
        }
    }
} 