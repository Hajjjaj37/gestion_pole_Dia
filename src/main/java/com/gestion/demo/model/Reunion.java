package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import java.util.Date;

@Entity
@Table(name = "reunions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reunion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private Date dateDebut;
    private Date dateFin;

    @ManyToOne
    @JoinColumn(name = "gestionnaire_id")
    private Gestionnaire gestionnaire;

    private String sujet;

    private LocalDateTime dateHeure;

    private String lieu;

    @ManyToMany
    @JoinTable(
        name = "reunion_formateurs",
        joinColumns = @JoinColumn(name = "reunion_id"),
        inverseJoinColumns = @JoinColumn(name = "formateur_id")
    )
    private Set<Formateur> formateurs;

    @ManyToMany
    @JoinTable(
        name = "reunion_gestionnaires",
        joinColumns = @JoinColumn(name = "reunion_id"),
        inverseJoinColumns = @JoinColumn(name = "gestionnaire_id")
    )
    private Set<Gestionnaire> gestionnaires = new HashSet<>();

    public Set<Gestionnaire> getGestionnaires() {
        return gestionnaires;
    }

    public void setGestionnaires(Set<Gestionnaire> gestionnaires) {
        this.gestionnaires = gestionnaires;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sujet, dateHeure, lieu);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reunion reunion = (Reunion) o;
        return Objects.equals(id, reunion.id) &&
               Objects.equals(sujet, reunion.sujet) &&
               Objects.equals(dateHeure, reunion.dateHeure) &&
               Objects.equals(lieu, reunion.lieu);
    }
} 