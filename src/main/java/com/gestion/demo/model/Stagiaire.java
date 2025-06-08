package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "stagiaires")
@ToString(exclude = {"rapports", "certificats"})
@EqualsAndHashCode(exclude = {"rapports", "certificats"})
public class Stagiaire {
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
    private String telephone;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @ManyToMany(mappedBy = "stagiaires", fetch = FetchType.LAZY)
    private Set<Evenement> evenements = new HashSet<>();

    @ManyToMany(mappedBy = "stagiaires", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JsonIgnoreProperties("stagiaires")
    private Set<Rapport> rapports = new HashSet<>();

    @OneToMany(mappedBy = "stagiaire", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("stagiaire")
    private List<Certificat> certificats;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    @JsonIgnoreProperties("stagiaires")
    private Classe classe;

    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("stagiaire")
    private User user;

    @OneToMany(mappedBy = "stagiaire")
    private Set<Attestation> attestations = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "stagiaire_regional",
        joinColumns = @JoinColumn(name = "stagiaire_id"),
        inverseJoinColumns = @JoinColumn(name = "regional_id")
    )
    private Set<Regional> regionals;

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stagiaire stagiaire = (Stagiaire) o;
        return Objects.equals(id, stagiaire.id);
    }
} 