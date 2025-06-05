package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "seances")
@Data
public class Seance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom; // ex: "Séance 1", "Séance 2", etc.

    @Column(nullable = false)
    private String periode; // "Matin" ou "Après-midi"

    @Column(nullable = false)
    private Integer numero; // 1, 2, 3, ou 4

    @Column(nullable = false)
    private String heureDebut; // ex: "08:00"

    @Column(nullable = false)
    private String heureFin; // ex: "09:30"
} 