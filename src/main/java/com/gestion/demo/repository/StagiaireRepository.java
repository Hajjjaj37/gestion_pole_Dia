package com.gestion.demo.repository;

import com.gestion.demo.model.Stagiaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StagiaireRepository extends JpaRepository<Stagiaire, Long> {
    Optional<Stagiaire> findByEmail(String email);
    List<Stagiaire> findByClasseId(Long classeId);
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(s) FROM Stagiaire s WHERE s.classe.id = :classeId")
    Long countByClasseId(@Param("classeId") Long classeId);
} 