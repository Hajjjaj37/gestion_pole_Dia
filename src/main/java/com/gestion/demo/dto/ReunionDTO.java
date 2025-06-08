package com.gestion.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ReunionDTO {
    private Long id;
    private String sujet;
    private LocalDateTime dateHeure;
    private String lieu;
    private Set<Long> formateurIds;
    private Set<Long> gestionnaireIds;
} 