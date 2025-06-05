package com.gestion.demo.dto;

import lombok.Data;

@Data
public class SalleRequest {
    private String nom;
    private String numero;
    private String description;
    private Integer capacite;
    private String equipement;
} 