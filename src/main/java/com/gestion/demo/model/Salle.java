package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;
import java.util.HashSet;

@Entity
@Data
@Table(name = "salles")
public class Salle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column
    private String description;

    @Column(nullable = false)
    private Integer capacite;

    @Column
    private String equipement;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "salle_formateur",
        joinColumns = @JoinColumn(name = "salle_id"),
        inverseJoinColumns = @JoinColumn(name = "formateur_id")
    )
    private Set<Formateur> formateurs = new HashSet<>();
} 
