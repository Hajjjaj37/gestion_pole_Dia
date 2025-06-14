package com.gestion.demo.dto;

import java.time.LocalDate;

public class AttestationDTO {
    private Long id;
    private String titre;
    private String contenu;
    private LocalDate dateEmission;
    private LocalDate dateExpiration;
    private String statut;
    private Long stagiaireId;
    private String stagiaireNom;
    private String stagiairePrenom;
    // + autres champs utiles

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public LocalDate getDateEmission() { return dateEmission; }
    public void setDateEmission(LocalDate dateEmission) { this.dateEmission = dateEmission; }
    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public Long getStagiaireId() { return stagiaireId; }
    public void setStagiaireId(Long stagiaireId) { this.stagiaireId = stagiaireId; }
    public String getStagiaireNom() { return stagiaireNom; }
    public void setStagiaireNom(String stagiaireNom) { this.stagiaireNom = stagiaireNom; }
    public String getStagiairePrenom() { return stagiairePrenom; }
    public void setStagiairePrenom(String stagiairePrenom) { this.stagiairePrenom = stagiairePrenom; }
} 