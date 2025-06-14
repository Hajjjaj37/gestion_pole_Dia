package com.gestion.demo.repository;

import com.gestion.demo.model.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    List<Evaluation> findByFormateurId(Long formateurId);
    List<Evaluation> findByStagiaireId(Long stagiaireId);
    List<Evaluation> findByModuleId(Long moduleId);
    List<Evaluation> findByFormateurIdAndStagiaireId(Long formateurId, Long stagiaireId);
} 