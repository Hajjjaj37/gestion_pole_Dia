package com.gestion.demo.dto;

import lombok.Data;
import java.util.Set;

@Data
public class FormationRequest {
    private String nom;
    private String description;
    private Integer duree;
    private Set<Long> moduleIds;
} 