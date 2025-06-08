package com.gestion.demo.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

public class CommentaireDTO {
    private Long id;
    private String contenu;
    private LocalDateTime dateCreation;
    private Long gestionnaireId;
    private Long classeId;
    private Set<Long> formateurIds = new HashSet<>();
    private Set<Long> stagiaireIds = new HashSet<>();

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Long getGestionnaireId() {
        return gestionnaireId;
    }

    public void setGestionnaireId(Long gestionnaireId) {
        this.gestionnaireId = gestionnaireId;
    }

    public Long getClasseId() {
        return classeId;
    }

    public void setClasseId(Long classeId) {
        this.classeId = classeId;
    }

    public Set<Long> getFormateurIds() {
        return formateurIds != null ? formateurIds : new HashSet<>();
    }

    public void setFormateurIds(Set<Long> formateurIds) {
        this.formateurIds = formateurIds != null ? formateurIds : new HashSet<>();
    }

    public Set<Long> getStagiaireIds() {
        return stagiaireIds != null ? stagiaireIds : new HashSet<>();
    }

    public void setStagiaireIds(Set<Long> stagiaireIds) {
        this.stagiaireIds = stagiaireIds != null ? stagiaireIds : new HashSet<>();
    }
} 