package com.gestion.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificatDTO {
    private Long id;
    private Long stagiaireId;
    private String stagiaireNom;
    private String stagiairePrenom;
    private String type;
    private String description;
    private LocalDate dateEmission;
    private String fichierUrl;
    private List<Long> absenceIds; // Liste des IDs des absences couvertes par ce certificat
} 