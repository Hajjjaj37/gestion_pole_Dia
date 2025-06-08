package com.gestion.demo.controller;

import com.gestion.demo.model.Gestionnaire;
import com.gestion.demo.model.User;
import com.gestion.demo.model.Role;
import com.gestion.demo.dto.GestionnaireDTO;
import com.gestion.demo.repository.GestionnaireRepository;
import com.gestion.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/gestionnaires")
public class GestionnaireController {

    @Autowired
    private GestionnaireRepository gestionnaireRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Créer un gestionnaire (et un user associé)
    @PostMapping
    public ResponseEntity<GestionnaireDTO> createGestionnaire(@RequestBody GestionnaireDTO dto) {
        // Vérifier si l'email existe déjà dans User
        if (userRepository.existsByUsername(dto.getEmail())) {
            return ResponseEntity.badRequest().body(null);
        }

        // Créer l'utilisateur principal (table user)
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_GESTIONNAIRE);
        user = userRepository.save(user);

        // Créer le gestionnaire (table gestionnaire)
        Gestionnaire gestionnaire = new Gestionnaire();
        gestionnaire.setNom(dto.getNom());
        gestionnaire.setPrenom(dto.getPrenom());
        gestionnaire.setEmail(dto.getEmail());
        gestionnaire.setPassword(user.getPassword()); // même mot de passe encodé
        gestionnaire.setRole("ROLE_GESTIONNAIRE");
        Gestionnaire saved = gestionnaireRepository.save(gestionnaire);

        return ResponseEntity.ok(toDTO(saved));
    }

    // Lister tous les gestionnaires
    @GetMapping
    public ResponseEntity<List<GestionnaireDTO>> getAllGestionnaires() {
        List<GestionnaireDTO> list = gestionnaireRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    // Obtenir un gestionnaire par ID
    @GetMapping("/{id}")
    public ResponseEntity<GestionnaireDTO> getGestionnaireById(@PathVariable Long id) {
        Optional<Gestionnaire> gestionnaireOpt = gestionnaireRepository.findById(id);
        return gestionnaireOpt.map(gestionnaire -> ResponseEntity.ok(toDTO(gestionnaire)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Mettre à jour un gestionnaire
    @PutMapping("/{id}")
    public ResponseEntity<GestionnaireDTO> updateGestionnaire(@PathVariable Long id, @RequestBody GestionnaireDTO dto) {
        Optional<Gestionnaire> gestionnaireOpt = gestionnaireRepository.findById(id);
        if (gestionnaireOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Gestionnaire gestionnaire = gestionnaireOpt.get();
        gestionnaire.setNom(dto.getNom());
        gestionnaire.setPrenom(dto.getPrenom());
        gestionnaire.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            gestionnaire.setPassword(encodedPassword);
            // Mettre à jour aussi le mot de passe dans User
            Optional<User> userOpt = userRepository.findByUsername(dto.getEmail());
            userOpt.ifPresent(user -> {
                user.setPassword(encodedPassword);
                userRepository.save(user);
            });
        }
        Gestionnaire updated = gestionnaireRepository.save(gestionnaire);
        return ResponseEntity.ok(toDTO(updated));
    }

    // Supprimer un gestionnaire
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGestionnaire(@PathVariable Long id) {
        Optional<Gestionnaire> gestionnaireOpt = gestionnaireRepository.findById(id);
        if (gestionnaireOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Gestionnaire gestionnaire = gestionnaireOpt.get();
        // Supprimer aussi le user associé
        userRepository.findByUsername(gestionnaire.getEmail()).ifPresent(userRepository::delete);
        gestionnaireRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    // Mapping entité -> DTO
    private GestionnaireDTO toDTO(Gestionnaire gestionnaire) {
        GestionnaireDTO dto = new GestionnaireDTO();
        dto.setId(gestionnaire.getId());
        dto.setNom(gestionnaire.getNom());
        dto.setPrenom(gestionnaire.getPrenom());
        dto.setEmail(gestionnaire.getEmail());
        dto.setRole(gestionnaire.getRole());
        // Ne JAMAIS exposer le mot de passe dans la réponse
        return dto;
    }
} 