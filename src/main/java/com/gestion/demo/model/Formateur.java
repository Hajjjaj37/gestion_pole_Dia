package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "formateurs")
@ToString(exclude = {"rapports", "formations", "classes"})
@EqualsAndHashCode(exclude = {"rapports", "formations", "classes"})
public class Formateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String specialite;

    @ManyToMany(mappedBy = "formateurs", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties("formateurs")
    private Set<Rapport> rapports = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "formation_formateur",
        joinColumns = @JoinColumn(name = "formateur_id"),
        inverseJoinColumns = @JoinColumn(name = "formation_id")
    )
    @JsonIgnoreProperties("formateurs")
    private Set<Formation> formations = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "formateur_classe",
        joinColumns = @JoinColumn(name = "formateur_id"),
        inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    @JsonIgnoreProperties("formateurs")
    private Set<Classe> classes = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("formateur")
    private User user;

    @ManyToOne
    @JoinColumn(name = "salle_id")
    @JsonIgnoreProperties("formateurs")
    private Salle salle;

    @OneToMany(mappedBy = "formateur", cascade = CascadeType.ALL)
    private Set<Evaluation> evaluations = new HashSet<>();

    public Set<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(Set<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public void addEvaluation(Evaluation evaluation) {
        evaluations.add(evaluation);
        evaluation.setFormateur(this);
    }

    public void removeEvaluation(Evaluation evaluation) {
        evaluations.remove(evaluation);
        evaluation.setFormateur(null);
    }
} 