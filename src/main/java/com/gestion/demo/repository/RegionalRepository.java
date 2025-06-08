package com.gestion.demo.repository;

import com.gestion.demo.model.Regional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegionalRepository extends JpaRepository<Regional, Long> {
    // Tu peux ajouter des méthodes personnalisées ici si besoin
    List<Regional> findByFormateurs_Id(Long formateurId);
    List<Regional> findByClasses_Id(Long classeId);
} 