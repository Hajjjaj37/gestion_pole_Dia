package com.gestion.demo.repository;

import com.gestion.demo.model.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SalleRepository extends JpaRepository<Salle, Long> {
    boolean existsByNom(String nom);
    boolean existsByNumero(String numero);

    @Query("SELECT DISTINCT s FROM Salle s LEFT JOIN FETCH s.formateurs f")
    List<Salle> findAllWithFormateurs();

    @Query("SELECT DISTINCT s FROM Salle s LEFT JOIN FETCH s.formateurs f WHERE s.id = :id")
    Optional<Salle> findByIdWithFormateurs(@Param("id") Long id);
} 