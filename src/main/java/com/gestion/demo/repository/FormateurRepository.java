package com.gestion.demo.repository;

import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface FormateurRepository extends JpaRepository<Formateur, Long> {
    
    // Trouver un formateur par email
    Optional<Formateur> findByEmail(String email);
    
    // Trouver un formateur par utilisateur
    Optional<Formateur> findByUser(User user);
    
    // Vérifier si un email existe déjà
    boolean existsByEmail(String email);

    // Supprimer les associations formateur-classe
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM formateur_classe WHERE formateur_id = :formateurId", nativeQuery = true)
    void deleteFormateurClasses(@Param("formateurId") Long formateurId);

    // Trouver un formateur avec ses classes
    @Query("SELECT f FROM Formateur f LEFT JOIN FETCH f.classes WHERE f.id = :formateurId")
    Optional<Formateur> findFormateurWithClasses(@Param("formateurId") Long formateurId);

    // Trouver un formateur avec son utilisateur
    @Query("SELECT f FROM Formateur f LEFT JOIN FETCH f.user WHERE f.id = :formateurId")
    Optional<Formateur> findFormateurWithUser(@Param("formateurId") Long formateurId);

    // Trouver un formateur avec ses classes et son utilisateur
    @Query("SELECT f FROM Formateur f LEFT JOIN FETCH f.classes LEFT JOIN FETCH f.user WHERE f.id = :formateurId")
    Optional<Formateur> findFormateurWithClassesAndUser(@Param("formateurId") Long formateurId);
} 