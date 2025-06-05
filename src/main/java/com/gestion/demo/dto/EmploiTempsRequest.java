package com.gestion.demo.dto;

import lombok.Data;
import java.time.DayOfWeek;

@Data
public class EmploiTempsRequest {
    private DayOfWeek jour;
    private Long seanceId;
    private Long formateurId;
    private Long moduleId;
    private Long classeId;
    private Long salleId;
} 