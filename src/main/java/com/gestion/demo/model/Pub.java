package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String titre;
    
    @Column(columnDefinition = "TEXT")
    private String contenu;
    
    private String image;
    
    @ManyToOne
    @JoinColumn(name = "gestionnaire_id")
    @JsonBackReference(value = "gestionnaire-pubs")
    private Gestionnaire gestionnaire;
    
    @ManyToMany
    @JoinTable(
        name = "pub_classe",
        joinColumns = @JoinColumn(name = "pub_id"),
        inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    @JsonManagedReference(value = "pub-classes")
    private Set<Classe> classes = new HashSet<>();

    public void addClasse(Classe classe) {
        classes.add(classe);
        classe.getPubs().add(this);
    }

    public void removeClasse(Classe classe) {
        classes.remove(classe);
        classe.getPubs().remove(this);
    }
} 