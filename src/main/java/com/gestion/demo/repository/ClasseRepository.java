package com.gestion.demo.repository;

import com.gestion.demo.model.Classe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ClasseRepository extends JpaRepository<Classe, Long> {
    List<Classe> findByFormationId(Long formationId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM stagiaires WHERE classe_id = :classeId", nativeQuery = true)
    void deleteStagiairesFromClasse(@Param("classeId") Long classeId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM formateur_classe WHERE classe_id = :classeId", nativeQuery = true)
    void deleteFormateurClasses(@Param("classeId") Long classeId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM classes WHERE id = :classeId", nativeQuery = true)
    void deleteClasseById(@Param("classeId") Long classeId);

    @Query("SELECT c FROM Classe c LEFT JOIN FETCH c.formation WHERE c.id = :classeId")
    Classe findClasseWithFormation(@Param("classeId") Long classeId);
} 