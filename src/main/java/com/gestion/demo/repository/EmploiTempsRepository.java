package com.gestion.demo.repository;

import com.gestion.demo.model.EmploiTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface EmploiTempsRepository extends JpaRepository<EmploiTemps, Long> {
    boolean existsByJourAndSeanceIdAndClasseId(DayOfWeek jour, Long seanceId, Long classeId);
    boolean existsByJourAndSeanceIdAndFormateurId(DayOfWeek jour, Long seanceId, Long formateurId);
    boolean existsByJourAndSeanceIdAndSalleId(DayOfWeek jour, Long seanceId, Long salleId);
    boolean existsByClasseId(Long classeId);
    List<EmploiTemps> findByClasseId(Long classeId);
    List<EmploiTemps> findByJourOrderBySeanceNumero(DayOfWeek jour);
    List<EmploiTemps> findByJourOrderByNumeroSeance(DayOfWeek jour);
    List<EmploiTemps> findByClasseIdAndJourOrderByNumeroSeance(Long classeId, DayOfWeek jour);
} 