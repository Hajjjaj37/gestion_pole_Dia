package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Entity
@Table(name = "formations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Formation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nom;
    
    private String description;
    
    private Integer duree;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "formation_module",
        joinColumns = @JoinColumn(name = "formation_id"),
        inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private Set<Module> modules = new HashSet<>();

    @ManyToMany(mappedBy = "formations")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Formateur> formateurs = new HashSet<>();

    @OneToMany(mappedBy = "formation")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Classe> classes = new HashSet<>();

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, description, duree);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Formation formation = (Formation) o;
        return Objects.equals(id, formation.id);
    }
} 