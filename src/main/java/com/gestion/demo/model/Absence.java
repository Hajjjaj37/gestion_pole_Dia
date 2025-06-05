package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "absences")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Absence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relation ManyToOne avec Stagiaire
    @ManyToOne
    @JoinColumn(name = "stagiaire_id")
    private Stagiaire stagiaire;

    // Relation ManyToOne avec Seance
    @ManyToOne
    @JoinColumn(name = "seance_id")
    private Seance seance;

    private LocalDate dateAbsence;

    private String motif; // optionnel : raison de l'absence

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Stagiaire getStagiaire() { return stagiaire; }
    public void setStagiaire(Stagiaire stagiaire) { this.stagiaire = stagiaire; }

    public Seance getSeance() { return seance; }
    public void setSeance(Seance seance) { this.seance = seance; }

    public LocalDate getDateAbsence() { return dateAbsence; }
    public void setDateAbsence(LocalDate dateAbsence) { this.dateAbsence = dateAbsence; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
} 