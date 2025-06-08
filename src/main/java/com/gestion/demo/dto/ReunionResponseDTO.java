package com.gestion.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReunionResponseDTO {
    private Long id;
    private String sujet;
    private LocalDateTime dateHeure;
    private String lieu;
    private List<String> formateurs;      // noms ou emails
    private List<String> gestionnaires;   // noms ou emails
} 