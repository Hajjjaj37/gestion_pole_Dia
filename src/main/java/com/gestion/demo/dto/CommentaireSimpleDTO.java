package com.gestion.demo.dto;

public class CommentaireSimpleDTO {
    private Long id;
    private String contenu;
    private String dateCreation;
    // ... autres champs utiles

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }
} 