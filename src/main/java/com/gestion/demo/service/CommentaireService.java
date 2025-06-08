package com.gestion.demo.service;

import com.gestion.demo.dto.CommentaireDTO;
import com.gestion.demo.dto.CommentaireSimpleDTO;
import com.gestion.demo.model.Commentaire;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.Stagiaire;
import com.gestion.demo.model.Gestionnaire;
import com.gestion.demo.model.Classe;
import com.gestion.demo.repository.CommentaireRepository;
import com.gestion.demo.repository.FormateurRepository;
import com.gestion.demo.repository.StagiaireRepository;
import com.gestion.demo.repository.GestionnaireRepository;
import com.gestion.demo.repository.ClasseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommentaireService {

    @Autowired
    private CommentaireRepository commentaireRepository;

    @Autowired
    private FormateurRepository formateurRepository;

    @Autowired
    private StagiaireRepository stagiaireRepository;

    @Autowired
    private GestionnaireRepository gestionnaireRepository;

    @Autowired
    private ClasseRepository classeRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public Commentaire creerCommentaire(CommentaireDTO dto) {
        Commentaire commentaire = new Commentaire();
        commentaire.setContenu(dto.getContenu());
        
        Gestionnaire gestionnaire = gestionnaireRepository.findById(dto.getGestionnaireId())
            .orElseThrow(() -> new RuntimeException("Gestionnaire non trouvé"));
        commentaire.setGestionnaire(gestionnaire);

        Classe classe = classeRepository.findById(dto.getClasseId())
            .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
        commentaire.setClasse(classe);

        Set<Formateur> formateurs = new HashSet<>();
        for (Long formateurId : dto.getFormateurIds()) {
            Formateur formateur = formateurRepository.findById(formateurId)
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
            formateurs.add(formateur);
        }
        commentaire.setFormateurs(formateurs);

        Set<Stagiaire> stagiaires = new HashSet<>();
        for (Long stagiaireId : dto.getStagiaireIds()) {
            Stagiaire stagiaire = stagiaireRepository.findById(stagiaireId)
                .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé"));
            stagiaires.add(stagiaire);
        }
        commentaire.setStagiaires(stagiaires);

        Commentaire savedCommentaire = commentaireRepository.save(commentaire);

        // Envoyer les notifications
        envoyerNotifications(savedCommentaire);

        return savedCommentaire;
    }

    private void envoyerNotifications(Commentaire commentaire) {
        String sujet = "Nouveau commentaire pour la classe " + commentaire.getClasse().getNom();
        
        for (Formateur formateur : commentaire.getFormateurs()) {
            emailService.envoyerNotification(
                formateur.getEmail(),
                sujet,
                commentaire.getContenu()
            );
        }

        for (Stagiaire stagiaire : commentaire.getStagiaires()) {
            emailService.envoyerNotification(
                stagiaire.getEmail(),
                sujet,
                commentaire.getContenu()
            );
        }
    }

    public Commentaire getCommentaire(Long id) {
        return commentaireRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
    }

    public List<Commentaire> getCommentairesByClasse(Long classeId) {
        return commentaireRepository.findByClasseId(classeId);
    }

    public void supprimerCommentaire(Long id) {
        commentaireRepository.deleteById(id);
    }

    public List<CommentaireSimpleDTO> getCommentairesByFormateur(Long formateurId) {
        List<Commentaire> commentaires = commentaireRepository.findByFormateurs_Id(formateurId);
        return commentaires.stream()
            .map(c -> {
                CommentaireSimpleDTO dto = new CommentaireSimpleDTO();
                dto.setId(c.getId());
                dto.setContenu(c.getContenu());
                dto.setDateCreation(c.getDateCreation() != null ? c.getDateCreation().toString() : null);
                return dto;
            })
            .collect(Collectors.toList());
    }

    public List<CommentaireSimpleDTO> getCommentairesByGestionnaire(Long gestionnaireId) {
        List<Commentaire> commentaires = commentaireRepository.findByGestionnaire_Id(gestionnaireId);
        return commentaires.stream()
            .map(c -> {
                CommentaireSimpleDTO dto = new CommentaireSimpleDTO();
                dto.setId(c.getId());
                dto.setContenu(c.getContenu());
                dto.setDateCreation(c.getDateCreation() != null ? c.getDateCreation().toString() : null);
                return dto;
            })
            .collect(Collectors.toList());
    }

    @Transactional
    public Commentaire updateCommentaire(Long id, CommentaireDTO dto) {
        Commentaire commentaire = commentaireRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));

        if (dto.getContenu() != null) {
            commentaire.setContenu(dto.getContenu());
        }

        if (dto.getGestionnaireId() != null) {
            Gestionnaire gestionnaire = gestionnaireRepository.findById(dto.getGestionnaireId())
                .orElseThrow(() -> new RuntimeException("Gestionnaire non trouvé"));
            commentaire.setGestionnaire(gestionnaire);
        }

        if (dto.getClasseId() != null) {
            Classe classe = classeRepository.findById(dto.getClasseId())
                .orElseThrow(() -> new RuntimeException("Classe non trouvée"));
            commentaire.setClasse(classe);
        }

        if (dto.getFormateurIds() != null) {
            Set<Formateur> formateurs = new HashSet<>();
            for (Long formateurId : dto.getFormateurIds()) {
                Formateur formateur = formateurRepository.findById(formateurId)
                    .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
                formateurs.add(formateur);
            }
            commentaire.setFormateurs(formateurs);
        }

        if (dto.getStagiaireIds() != null) {
            Set<Stagiaire> stagiaires = new HashSet<>();
            for (Long stagiaireId : dto.getStagiaireIds()) {
                Stagiaire stagiaire = stagiaireRepository.findById(stagiaireId)
                    .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé"));
                stagiaires.add(stagiaire);
            }
            commentaire.setStagiaires(stagiaires);
        }

        return commentaireRepository.save(commentaire);
    }
} 