package com.gestion.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "absence_certificats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceCertificat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "absence_id", nullable = false)
    private Absence absence;

    @ManyToOne
    @JoinColumn(name = "certificat_id", nullable = false)
    private Certificat certificat;

    private String commentaire; // Commentaire optionnel sur la justification
} 