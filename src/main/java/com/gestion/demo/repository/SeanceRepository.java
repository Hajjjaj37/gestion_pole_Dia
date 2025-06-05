package com.gestion.demo.repository;

import com.gestion.demo.model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {
    // Vous pouvez ajouter des méthodes personnalisées ici si nécessaire
} 