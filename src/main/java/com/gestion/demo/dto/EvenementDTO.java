package com.gestion.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class EvenementDTO {
    private Long id;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private Set<Long> formateurIds;
    private Set<Long> stagiaireIds;
    private Long salleId;
    private Set<Long> classeIds;
    private Long formationId;
    private Long moduleId;
    private Long gestionnaireId;
} 