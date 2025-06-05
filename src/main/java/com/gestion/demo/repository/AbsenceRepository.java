package com.gestion.demo.repository;

import com.gestion.demo.model.Absence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Long> {
    
    @Query("SELECT a FROM Absence a " +
           "JOIN FETCH a.stagiaire s " +
           "JOIN FETCH s.classe c " +
           "WHERE c.id = :classeId")
    List<Absence> findByClasseId(@Param("classeId") Long classeId);
    
    @Query("SELECT a FROM Absence a " +
           "JOIN FETCH a.stagiaire s " +
           "JOIN FETCH s.classe c " +
           "WHERE c.id = :classeId AND a.dateAbsence = :date")
    List<Absence> findByClasseIdAndDate(@Param("classeId") Long classeId, @Param("date") LocalDate date);
    
    @Query("SELECT a FROM Absence a WHERE a.stagiaire.id = :stagiaireId")
    List<Absence> findByStagiaireId(@Param("stagiaireId") Long stagiaireId);
    
    @Query(value = "SELECT * FROM absences WHERE stagiaire_id = :stagiaireId", nativeQuery = true)
    List<Absence> findByStagiaireIdNative(@Param("stagiaireId") Long stagiaireId);
    
    @Query("SELECT a FROM Absence a WHERE a.seance.id = :seanceId")
    List<Absence> findBySeanceId(@Param("seanceId") Long seanceId);
    
    @Query("SELECT a FROM Absence a WHERE a.stagiaire.classe.id = :classeId AND a.dateAbsence BETWEEN :startDate AND :endDate")
    List<Absence> findByClasseIdAndDateBetween(
        @Param("classeId") Long classeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
} 