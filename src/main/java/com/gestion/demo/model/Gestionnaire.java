package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "gestionnaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gestionnaire {
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
    private String password;

    @Column(nullable = false)
    private String role;

    @ManyToMany(mappedBy = "gestionnaires")
    private Set<Reunion> reunions = new HashSet<>();

    @OneToMany(mappedBy = "gestionnaire")
    private Set<Attestation> attestations = new HashSet<>();

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Set<Reunion> getReunions() {
        return reunions;
    }

    public void setReunions(Set<Reunion> reunions) {
        this.reunions = reunions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gestionnaire gestionnaire = (Gestionnaire) o;
        return Objects.equals(id, gestionnaire.id) &&
               Objects.equals(nom, gestionnaire.nom) &&
               Objects.equals(email, gestionnaire.email);
    }
} 