package com.gestion.demo.dto;

import lombok.Data;
import java.util.Date;

@Data
public class ClasseRequest {
    private String nom;
    private String description;
    private Date dateDebut;
    private Date dateFin;
    private Long formationId;
} 