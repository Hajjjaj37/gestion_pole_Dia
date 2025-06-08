package com.gestion.demo.dto;

import java.util.Set;

public class RegionalResponseDTO {
    private Long id;
    private String nom;
    private SimpleGestionnaireDTO gestionnaire;
    private Set<SimpleClasseDTO> classes;
    private Set<SimpleFormateurDTO> formateurs;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public SimpleGestionnaireDTO getGestionnaire() { return gestionnaire; }
    public void setGestionnaire(SimpleGestionnaireDTO gestionnaire) { this.gestionnaire = gestionnaire; }

    public Set<SimpleClasseDTO> getClasses() { return classes; }
    public void setClasses(Set<SimpleClasseDTO> classes) { this.classes = classes; }

    public Set<SimpleFormateurDTO> getFormateurs() { return formateurs; }
    public void setFormateurs(Set<SimpleFormateurDTO> formateurs) { this.formateurs = formateurs; }
} 