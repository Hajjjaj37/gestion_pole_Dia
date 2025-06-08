package com.gestion.demo.repository;

import com.gestion.demo.model.AbsenceCertificat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AbsenceCertificatRepository extends JpaRepository<AbsenceCertificat, Long> {
    List<AbsenceCertificat> findByAbsenceId(Long absenceId);
    List<AbsenceCertificat> findByCertificatId(Long certificatId);
    
    @Query("SELECT ac FROM AbsenceCertificat ac WHERE ac.absence.id = :absenceId AND ac.certificat.id = :certificatId")
    AbsenceCertificat findByAbsenceIdAndCertificatId(
        @Param("absenceId") Long absenceId, 
        @Param("certificatId") Long certificatId
    );
} 