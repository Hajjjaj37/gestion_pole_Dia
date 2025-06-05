package com.gestion.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormateurRequest {
    private String nom;
    private String prenom;
    private String email;
    private String specialite;
    private String username;
    private String password;
    private Set<Long> classeIds;
    private Long salleId;
} 