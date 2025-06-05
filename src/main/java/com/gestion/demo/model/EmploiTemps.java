package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "emploi_temps")
@Data
public class EmploiTemps {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private Classe classe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek jour;

    @Column(name = "numero_seance", nullable = false)
    private Integer numeroSeance;

    @ManyToOne
    @JoinColumn(name = "seance_id", nullable = false)
    @JsonManagedReference
    private Seance seance;

    @ManyToOne
    @JoinColumn(name = "formateur_id", nullable = false)
    @JsonManagedReference
    private Formateur formateur;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    @JsonManagedReference
    private Module module;

    @ManyToOne
    @JoinColumn(name = "salle_id", nullable = false)
    @JsonManagedReference
    private Salle salle;
} 