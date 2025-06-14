package com.gestion.demo.controller;

import com.gestion.demo.dto.*;
import com.gestion.demo.model.*;
import com.gestion.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/evaluations")
public class EvaluationController {

    @Autowired
    private EvaluationRepository evaluationRepository;

    @Autowired
    private FormateurRepository formateurRepository;

    @Autowired
    private StagiaireRepository stagiaireRepository;

    @Autowired
    private ModuleEntityRepository moduleRepository;

    @GetMapping
    public ResponseEntity<List<EvaluationResponseDTO>> getAllEvaluations() {
        List<Evaluation> evaluations = evaluationRepository.findAll();
        List<EvaluationResponseDTO> responseDTOs = evaluations.stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluationResponseDTO> getEvaluationById(@PathVariable Long id) {
        return evaluationRepository.findById(id)
            .map(this::convertToResponseDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createEvaluation(@RequestBody EvaluationDTO evaluationDTO) {
        try {
            Evaluation evaluation = new Evaluation();
            evaluation.setNote(evaluationDTO.getNote());
            evaluation.setCommentaire(evaluationDTO.getCommentaire());
            evaluation.setDateEvaluation(evaluationDTO.getDateEvaluation());

            Formateur formateur = formateurRepository.findById(evaluationDTO.getFormateurId())
                .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
            evaluation.setFormateur(formateur);

            Stagiaire stagiaire = stagiaireRepository.findById(evaluationDTO.getStagiaireId())
                .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé"));
            evaluation.setStagiaire(stagiaire);

            ModuleEntity moduleEntity = moduleRepository.findById(evaluationDTO.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));
            evaluation.setModule(moduleEntity);

            Evaluation savedEvaluation = evaluationRepository.save(evaluation);
            return ResponseEntity.ok(convertToResponseDTO(savedEvaluation));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la création de l'évaluation: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvaluation(@PathVariable Long id, @RequestBody EvaluationDTO evaluationDTO) {
        try {
            return evaluationRepository.findById(id)
                .map(existingEvaluation -> {
                    existingEvaluation.setNote(evaluationDTO.getNote());
                    existingEvaluation.setCommentaire(evaluationDTO.getCommentaire());
                    existingEvaluation.setDateEvaluation(evaluationDTO.getDateEvaluation());

                    Formateur formateur = formateurRepository.findById(evaluationDTO.getFormateurId())
                        .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
                    existingEvaluation.setFormateur(formateur);

                    Stagiaire stagiaire = stagiaireRepository.findById(evaluationDTO.getStagiaireId())
                        .orElseThrow(() -> new RuntimeException("Stagiaire non trouvé"));
                    existingEvaluation.setStagiaire(stagiaire);

                    ModuleEntity moduleEntity = moduleRepository.findById(evaluationDTO.getModuleId())
                        .orElseThrow(() -> new RuntimeException("Module non trouvé"));
                    existingEvaluation.setModule(moduleEntity);

                    Evaluation updatedEvaluation = evaluationRepository.save(existingEvaluation);
                    return ResponseEntity.ok(convertToResponseDTO(updatedEvaluation));
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la mise à jour de l'évaluation: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvaluation(@PathVariable Long id) {
        try {
            return evaluationRepository.findById(id)
                .map(evaluation -> {
                    evaluationRepository.delete(evaluation);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors de la suppression de l'évaluation: " + e.getMessage());
        }
    }

    @GetMapping("/formateur/{formateurId}")
    public ResponseEntity<List<EvaluationResponseDTO>> getEvaluationsByFormateur(@PathVariable Long formateurId) {
        List<Evaluation> evaluations = evaluationRepository.findByFormateurId(formateurId);
        List<EvaluationResponseDTO> responseDTOs = evaluations.stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/stagiaire/{stagiaireId}")
    public ResponseEntity<List<EvaluationResponseDTO>> getEvaluationsByStagiaire(@PathVariable Long stagiaireId) {
        List<Evaluation> evaluations = evaluationRepository.findByStagiaireId(stagiaireId);
        List<EvaluationResponseDTO> responseDTOs = evaluations.stream()
            .map(this::convertToResponseDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/modules/classes")
    @PreAuthorize("hasAnyRole('ADMIN', 'FORMATEUR')")
    public ResponseEntity<?> getAllModulesWithClassGrades() {
        try {
            // Récupérer tous les modules
            List<ModuleEntity> modules = moduleRepository.findAll();
            
            List<Map<String, Object>> modulesData = new ArrayList<>();
            
            for (ModuleEntity module : modules) {
                Map<String, Object> moduleData = new HashMap<>();
                moduleData.put("moduleId", module.getId());
                moduleData.put("moduleNom", module.getNom());
                moduleData.put("description", module.getDescription());
                
                // Récupérer toutes les évaluations pour ce module
                List<Evaluation> evaluations = evaluationRepository.findByModuleId(module.getId());
                
                // Grouper les évaluations par classe
                Map<Long, List<Evaluation>> evaluationsByClasse = evaluations.stream()
                    .collect(Collectors.groupingBy(eval -> 
                        eval.getStagiaire().getClasse().getId()));
                
                List<Map<String, Object>> classesData = new ArrayList<>();
                
                for (Map.Entry<Long, List<Evaluation>> entry : evaluationsByClasse.entrySet()) {
                    Long classeId = entry.getKey();
                    List<Evaluation> classeEvaluations = entry.getValue();
                    
                    Map<String, Object> classeData = new HashMap<>();
                    classeData.put("classeId", classeId);
                    classeData.put("classeNom", classeEvaluations.get(0).getStagiaire().getClasse().getNom());
                    
                    // Calculer la moyenne de la classe
                    double moyenne = classeEvaluations.stream()
                        .mapToDouble(Evaluation::getNote)
                        .average()
                        .orElse(0.0);
                    classeData.put("moyenne", moyenne);
                    
                    // Détails des notes
                    List<Map<String, Object>> notesData = classeEvaluations.stream()
                        .map(eval -> {
                            Map<String, Object> noteData = new HashMap<>();
                            noteData.put("stagiaireId", eval.getStagiaire().getId());
                            noteData.put("stagiaireNom", eval.getStagiaire().getNom());
                            noteData.put("stagiairePrenom", eval.getStagiaire().getPrenom());
                            noteData.put("note", eval.getNote());
                            noteData.put("dateEvaluation", eval.getDateEvaluation());
                            noteData.put("commentaire", eval.getCommentaire());
                            return noteData;
                        })
                        .collect(Collectors.toList());
                    
                    classeData.put("notes", notesData);
                    classesData.add(classeData);
                }
                
                moduleData.put("classes", classesData);
                modulesData.add(moduleData);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Modules et notes récupérés avec succès");
            response.put("data", modulesData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false,
                    "message", "Erreur lors de la récupération des modules et notes: " + e.getMessage()
                ));
        }
    }

    private EvaluationResponseDTO convertToResponseDTO(Evaluation evaluation) {
        EvaluationResponseDTO dto = new EvaluationResponseDTO();
        dto.setId(evaluation.getId());
        dto.setNote(evaluation.getNote());
        dto.setCommentaire(evaluation.getCommentaire());
        dto.setDateEvaluation(evaluation.getDateEvaluation());
        
        if (evaluation.getFormateur() != null) {
            FormateurDTO formateurDTO = new FormateurDTO();
            formateurDTO.setId(evaluation.getFormateur().getId());
            formateurDTO.setNom(evaluation.getFormateur().getNom());
            formateurDTO.setPrenom(evaluation.getFormateur().getPrenom());
            formateurDTO.setEmail(evaluation.getFormateur().getEmail());
            dto.setFormateur(formateurDTO);
        }
        
        if (evaluation.getStagiaire() != null) {
            StagiaireDTO stagiaireDTO = new StagiaireDTO();
            stagiaireDTO.setId(evaluation.getStagiaire().getId());
            stagiaireDTO.setNom(evaluation.getStagiaire().getNom());
            stagiaireDTO.setPrenom(evaluation.getStagiaire().getPrenom());
            stagiaireDTO.setEmail(evaluation.getStagiaire().getEmail());
            dto.setStagiaire(stagiaireDTO);
        }
        
        if (evaluation.getModule() != null) {
            ModuleDTO moduleDTO = new ModuleDTO();
            moduleDTO.setId(evaluation.getModule().getId());
            moduleDTO.setNom(evaluation.getModule().getNom());
            moduleDTO.setDescription(evaluation.getModule().getDescription());
            dto.setModule(moduleDTO);
        }
        
        return dto;
    }
}