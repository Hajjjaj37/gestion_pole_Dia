package com.gestion.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EvenementResponse {
    private Long id;
    private String titre;
    private String description;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private String type;
    private String message;
    private boolean success;
} 