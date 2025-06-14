package com.gestion.demo.repository;

import com.gestion.demo.model.Attestation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AttestationRepository extends JpaRepository<Attestation, Long> {
    List<Attestation> findByStagiaireId(Long stagiaireId);
    List<Attestation> findByGestionnaireId(Long gestionnaireId);
    List<Attestation> findByStagiaire_Classe_Id(Long classeId);
} 