package com.gestion.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsenceCertificatDTO {
    private Long id;
    private Long absenceId;
    private Long certificatId;
    private String commentaire;
} 