package com.gestion.demo.repository;

import com.gestion.demo.model.Gestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GestionnaireRepository extends JpaRepository<Gestionnaire, Long> {
    Optional<Gestionnaire> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("SELECT DISTINCT g FROM Gestionnaire g JOIN FETCH g.reunions WHERE g.reunions IS NOT EMPTY")
    List<Gestionnaire> findAllWithReunions();
} 