package com.gestion.demo.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StagiaireRequest {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private Long classeId;  // ID de la classe associée
} 