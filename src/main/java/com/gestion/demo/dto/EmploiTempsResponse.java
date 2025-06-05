package com.gestion.demo.dto;

import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalDate;

@Data
public class EmploiTempsResponse {
    private Long id;
    private DayOfWeek jour;
    private Integer numeroSeance;
    private SeanceDTO seance;
    private FormateurDTO formateur;
    private ModuleDTO module;
    private ClasseDTO classe;
    private SalleDTO salle;

    @Data
    public static class SeanceDTO {
        private Long id;
        private String nom;
        private String periode;
        private Integer numero;
        private String heureDebut;
        private String heureFin;
    }

    @Data
    public static class FormateurDTO {
        private Long id;
        private String nom;
        private String prenom;
        private String email;
        private String specialite;
    }

    @Data
    public static class ModuleDTO {
        private Long id;
        private String nom;
        private String description;
        private Integer duree;
    }

    @Data
    public static class ClasseDTO {
        private Long id;
        private String nom;
        private String description;
        private LocalDate dateDebut;
        private LocalDate dateFin;
    }

    @Data
    public static class SalleDTO {
        private Long id;
        private String nom;
        private String numero;
        private String description;
        private Integer capacite;
        private String equipement;
    }
} 