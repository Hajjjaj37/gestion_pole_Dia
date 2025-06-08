package com.gestion.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "commentaires")
public class Commentaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contenu;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @ManyToOne
    @JoinColumn(name = "gestionnaire_id", nullable = false)
    private Gestionnaire gestionnaire;

    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private Classe classe;

    @ManyToMany
    @JoinTable(
        name = "commentaire_formateurs",
        joinColumns = @JoinColumn(name = "commentaire_id"),
        inverseJoinColumns = @JoinColumn(name = "formateur_id")
    )
    private Set<Formateur> formateurs;

    @ManyToMany
    @JoinTable(
        name = "commentaire_stagiaires",
        joinColumns = @JoinColumn(name = "commentaire_id"),
        inverseJoinColumns = @JoinColumn(name = "stagiaire_id")
    )
    private Set<Stagiaire> stagiaires;

    // Constructeurs
    public Commentaire() {
        this.dateCreation = LocalDateTime.now();
    }

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

    public Gestionnaire getGestionnaire() {
        return gestionnaire;
    }

    public void setGestionnaire(Gestionnaire gestionnaire) {
        this.gestionnaire = gestionnaire;
    }

    public Classe getClasse() {
        return classe;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    public Set<Formateur> getFormateurs() {
        return formateurs;
    }

    public void setFormateurs(Set<Formateur> formateurs) {
        this.formateurs = formateurs;
    }

    public Set<Stagiaire> getStagiaires() {
        return stagiaires;
    }

    public void setStagiaires(Set<Stagiaire> stagiaires) {
        this.stagiaires = stagiaires;
    }
} 