package com.gestion.demo.dto;

import java.time.LocalDateTime;

public class EvaluationResponseDTO {
    private Long id;
    private Double note;
    private String commentaire;
    private LocalDateTime dateEvaluation;
    private FormateurDTO formateur;
    private StagiaireDTO stagiaire;
    private ModuleDTO module;

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getNote() {
        return note;
    }

    public void setNote(Double note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDateTime getDateEvaluation() {
        return dateEvaluation;
    }

    public void setDateEvaluation(LocalDateTime dateEvaluation) {
        this.dateEvaluation = dateEvaluation;
    }

    public FormateurDTO getFormateur() {
        return formateur;
    }

    public void setFormateur(FormateurDTO formateur) {
        this.formateur = formateur;
    }

    public StagiaireDTO getStagiaire() {
        return stagiaire;
    }

    public void setStagiaire(StagiaireDTO stagiaire) {
        this.stagiaire = stagiaire;
    }

    public ModuleDTO getModule() {
        return module;
    }

    public void setModule(ModuleDTO module) {
        this.module = module;
    }
} 