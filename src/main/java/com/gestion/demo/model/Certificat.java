package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "certificats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Certificat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stagiaire_id", nullable = false)
    private Stagiaire stagiaire;

    private String type; // Type de certificat (médical, administratif, etc.)
    private String description;
    private LocalDate dateEmission;
    private String fichierUrl; // URL ou chemin du fichier du certificat

    @OneToMany(mappedBy = "certificat", cascade = CascadeType.ALL)
    private List<AbsenceCertificat> absenceCertificats;
} 