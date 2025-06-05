package com.gestion.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceDTO {
    private Long id;
    private Long stagiaireId;
    private String stagiaireNom;
    private String stagiairePrenom;
    private Long seanceId;
    private String seanceNom;
    private String seanceHeureDebut;
    private String seanceHeureFin;
    private LocalDate dateAbsence;
    private String motif;
    private String formattedDate;
    private boolean valid;

    // Getters & Setters
    // ...
} 