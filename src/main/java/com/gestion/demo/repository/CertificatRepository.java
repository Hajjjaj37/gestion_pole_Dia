package com.gestion.demo.repository;

import com.gestion.demo.model.Certificat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CertificatRepository extends JpaRepository<Certificat, Long> {
    List<Certificat> findByStagiaireId(Long stagiaireId);
    
    @Query("SELECT c FROM Certificat c JOIN c.absenceCertificats ac WHERE ac.absence.id = :absenceId")
    List<Certificat> findByAbsenceId(@Param("absenceId") Long absenceId);
} 