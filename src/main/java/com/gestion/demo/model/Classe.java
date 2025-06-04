package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "classes")
public class Classe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String description;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dateDebut;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false)
    private Date dateFin;

    @ManyToOne
    @JoinColumn(name = "formation_id")
    private Formation formation;

    @ManyToMany(mappedBy = "classes")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Formateur> formateurs = new HashSet<>();

    @OneToMany(mappedBy = "classe", cascade = CascadeType.ALL)
    private List<Stagiaire> stagiaires = new ArrayList<>();
} 