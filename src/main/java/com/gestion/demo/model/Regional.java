package com.gestion.demo.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "regional")
public class Regional {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    // Many-to-One: Chaque regional est créé par un seul gestionnaire
    @ManyToOne
    @JoinColumn(name = "gestionnaire_id", nullable = false)
    private Gestionnaire gestionnaire;

    // Many-to-Many avec Classe (table pivot classe_regional)
    @ManyToMany
    @JoinTable(
        name = "classe_regional",
        joinColumns = @JoinColumn(name = "regional_id"),
        inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    private Set<Classe> classes;

    // Many-to-Many avec Formateur (table pivot formateur_regional)
    @ManyToMany
    @JoinTable(
        name = "formateur_regional",
        joinColumns = @JoinColumn(name = "regional_id"),
        inverseJoinColumns = @JoinColumn(name = "formateur_id")
    )
    private Set<Formateur> formateurs;

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public Gestionnaire getGestionnaire() { return gestionnaire; }
    public void setGestionnaire(Gestionnaire gestionnaire) { this.gestionnaire = gestionnaire; }

    public Set<Classe> getClasses() { return classes; }
    public void setClasses(Set<Classe> classes) { this.classes = classes; }

    public Set<Formateur> getFormateurs() { return formateurs; }
    public void setFormateurs(Set<Formateur> formateurs) { this.formateurs = formateurs; }
} 