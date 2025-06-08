package com.gestion.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EvenementRequest {
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String type;
    private Long salleId;
    private List<Long> formateurIds;
    private List<Long> stagiaireIds;
    private List<Long> classeIds;
} 