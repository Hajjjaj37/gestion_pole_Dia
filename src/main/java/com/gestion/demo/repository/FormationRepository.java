package com.gestion.demo.repository;

import com.gestion.demo.model.Formation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FormationRepository extends JpaRepository<Formation, Long> {
    @Query(value = "SELECT f.id as formation_id, f.nom as formation_nom, f.description as formation_description, " +
                   "f.duree as formation_duree, m.id as module_id, m.nom as module_nom, " +
                   "m.description as module_description, m.duree as module_duree " +
                   "FROM formations f " +
                   "LEFT JOIN formation_module fm ON f.id = fm.formation_id " +
                   "LEFT JOIN modules m ON fm.module_id = m.id", 
           nativeQuery = true)
    List<Object[]> findAllFormationsWithModules();

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM formation_module WHERE formation_id = :formationId", nativeQuery = true)
    void deleteFormationModules(@Param("formationId") Long formationId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM stagiaires WHERE classe_id IN (SELECT id FROM classes WHERE formation_id = :formationId)", nativeQuery = true)
    void deleteStagiairesFromClasses(@Param("formationId") Long formationId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM formateur_classe WHERE classe_id IN (SELECT id FROM classes WHERE formation_id = :formationId)", nativeQuery = true)
    void deleteFormateurClasses(@Param("formationId") Long formationId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM classes WHERE formation_id = :formationId", nativeQuery = true)
    void deleteFormationClasses(@Param("formationId") Long formationId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM formations WHERE id = :formationId", nativeQuery = true)
    void deleteFormationById(@Param("formationId") Long formationId);
}