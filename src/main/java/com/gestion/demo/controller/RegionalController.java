package com.gestion.demo.controller;

import com.gestion.demo.dto.RegionalDTO;
import com.gestion.demo.dto.RegionalResponseDTO;
import com.gestion.demo.model.Regional;
import com.gestion.demo.service.RegionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/regionals")
public class RegionalController {

    @Autowired
    private RegionalService regionalService;

    @PostMapping
    public ResponseEntity<RegionalResponseDTO> createRegional(@RequestBody RegionalDTO dto) {
        Regional regional = regionalService.createRegional(dto);
        RegionalResponseDTO response = regionalService.toDto(regional);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<RegionalResponseDTO>> getAllRegionals() {
        List<Regional> regionals = regionalService.getAllRegionals();
        List<RegionalResponseDTO> dtos = regionals.stream()
            .map(regionalService::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionalResponseDTO> getRegional(@PathVariable Long id) {
        Regional regional = regionalService.getRegional(id);
        RegionalResponseDTO dto = regionalService.toDto(regional);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Regional> updateRegional(@PathVariable Long id, @RequestBody RegionalDTO dto) {
        Regional regional = regionalService.updateRegional(id, dto);
        return ResponseEntity.ok(regional);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRegional(@PathVariable Long id) {
        regionalService.deleteRegional(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/formateur/{formateurId}")
    public ResponseEntity<List<RegionalResponseDTO>> getRegionalsByFormateur(@PathVariable Long formateurId) {
        List<RegionalResponseDTO> dtos = regionalService.getRegionalsByFormateur(formateurId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/classe/{classeId}")
    public ResponseEntity<List<RegionalResponseDTO>> getRegionalsByClasse(@PathVariable Long classeId) {
        List<RegionalResponseDTO> dtos = regionalService.getRegionalsByClasse(classeId);
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/{regionalId}/affecter-stagiaires/{classeId}")
    public ResponseEntity<?> affecterRegionalAuxStagiairesDeClasse(
            @PathVariable Long regionalId,
            @PathVariable Long classeId) {
        try {
            regionalService.affecterRegionalAuxStagiairesDeClasse(regionalId, classeId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Régional affecté à tous les stagiaires de la classe"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/classe/{classeId}/stagiaires/regionals")
    public ResponseEntity<List<RegionalResponseDTO>> getRegionalsOfStagiairesByClasse(@PathVariable Long classeId) {
        List<RegionalResponseDTO> dtos = regionalService.getRegionalsOfStagiairesByClasse(classeId);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/classe/{classeId}/stagiaires/regionals-detail")
    public ResponseEntity<List<Map<String, Object>>> getRegionalsParStagiaireDeClasse(@PathVariable Long classeId) {
        List<Map<String, Object>> result = regionalService.getRegionalsParStagiaireDeClasse(classeId);
        return ResponseEntity.ok(result);
    }
} 