package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "rapports")
@Data
@ToString(exclude = {"formateurs", "stagiaires"})
@EqualsAndHashCode(exclude = {"formateurs", "stagiaires"})
public class Rapport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String contenu;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    private StatutRapport statut;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "rapport_formateur",
        joinColumns = @JoinColumn(name = "rapport_id"),
        inverseJoinColumns = @JoinColumn(name = "formateur_id")
    )
    @JsonIgnoreProperties("rapports")
    private Set<Formateur> formateurs = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "rapport_stagiaire",
        joinColumns = @JoinColumn(name = "rapport_id"),
        inverseJoinColumns = @JoinColumn(name = "stagiaire_id")
    )
    @JsonIgnoreProperties("rapports")
    private Set<Stagiaire> stagiaires = new HashSet<>();

    public enum StatutRapport {
        EN_ATTENTE,
        VALIDE,
        REFUSE
    }
} 