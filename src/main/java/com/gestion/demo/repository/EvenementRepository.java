package com.gestion.demo.repository;

import com.gestion.demo.model.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, Long> {
    List<Evenement> findByDateDebutBetween(LocalDateTime start, LocalDateTime end);
    List<Evenement> findByFormateursId(Long formateurId);
    List<Evenement> findByStagiairesId(Long stagiaireId);
    List<Evenement> findBySalleId(Long salleId);
    
    @Query("SELECT e FROM Evenement e JOIN e.classes c WHERE c.id = :classeId")
    List<Evenement> findByClassesId(@Param("classeId") Long classeId);
} 