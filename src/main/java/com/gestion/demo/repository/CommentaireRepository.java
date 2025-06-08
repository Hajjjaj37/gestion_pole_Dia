package com.gestion.demo.repository;

import com.gestion.demo.model.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    List<Commentaire> findByClasseId(Long classeId);
    List<Commentaire> findByGestionnaireId(Long gestionnaireId);
    List<Commentaire> findByFormateurs_Id(Long formateurId);
    List<Commentaire> findByGestionnaire_Id(Long gestionnaireId);
} 