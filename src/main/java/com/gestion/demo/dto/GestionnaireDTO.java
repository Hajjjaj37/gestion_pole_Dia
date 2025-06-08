package com.gestion.demo.dto;

import lombok.Data;

@Data
public class GestionnaireDTO {
    private Long id;
    private String username;
    private String nom;
    private String prenom;
    private String email;
    private String password; // Pour la création/mise à jour uniquement
    private String role;     // Pour affichage, ex: "ROLE_GESTIONNAIRE"
} 