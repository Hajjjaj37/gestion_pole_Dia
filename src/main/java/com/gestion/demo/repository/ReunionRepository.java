package com.gestion.demo.repository;

import com.gestion.demo.model.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Long> {
    List<Reunion> findByFormateursId(Long formateurId);
    List<Reunion> findByGestionnairesId(Long gestionnaireId);
} 