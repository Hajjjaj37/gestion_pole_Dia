package com.gestion.demo.repository;

import com.gestion.demo.model.Rapport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RapportRepository extends JpaRepository<Rapport, Long> {
    List<Rapport> findByFormateursId(Long formateurId);
    List<Rapport> findByStagiairesId(Long stagiaireId);
} 