package com.gestion.demo.dto;

import java.util.Set;

public class RegionalDTO {
    private String nom;
    private Long gestionnaireId;
    private Set<Long> classeIds;
    private Set<Long> formateurIds;

    // Getters & Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Long getGestionnaireId() { return gestionnaireId; }
    public void setGestionnaireId(Long gestionnaireId) { this.gestionnaireId = gestionnaireId; }

    public Set<Long> getClasseIds() { return classeIds; }
    public void setClasseIds(Set<Long> classeIds) { this.classeIds = classeIds; }

    public Set<Long> getFormateurIds() { return formateurIds; }
    public void setFormateurIds(Set<Long> formateurIds) { this.formateurIds = formateurIds; }
} 