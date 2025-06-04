package com.gestion.demo.controller;

import com.gestion.demo.dto.LoginRequest;
import com.gestion.demo.dto.RegisterRequest;
import com.gestion.demo.dto.FormateurRequest;
import com.gestion.demo.model.User;
import com.gestion.demo.model.Formateur;
import com.gestion.demo.model.Role;
import com.gestion.demo.repository.UserRepository;
import com.gestion.demo.repository.FormateurRepository;
import com.gestion.demo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final FormateurRepository formateurRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            log.info("=== DÉBUT DE L'ENREGISTREMENT D'UTILISATEUR ===");
            log.info("Données reçues: {}", request);

            // Vérifier si l'utilisateur existe déjà
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Bad Request",
                    "message", "Username déjà utilisé"
                ));
            }

            // Créer un nouvel utilisateur
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setNom(request.getNom());
            user.setPrenom(request.getPrenom());
            user.setRole(request.getRole() != null ? request.getRole() : Role.ROLE_USER);

            // Sauvegarder l'utilisateur
            user = userRepository.save(user);

            // Préparer la réponse
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilisateur créé avec succès");
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("email", user.getEmail());
            userData.put("nom", user.getNom());
            userData.put("prenom", user.getPrenom());
            userData.put("role", user.getRole());
            
            response.put("data", userData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de l'enregistrement de l'utilisateur", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", "Erreur lors de l'enregistrement de l'utilisateur: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/register/formateur")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> registerFormateur(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody FormateurRequest request) {
        
        // Vérifier le token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Token manquant ou invalide");
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User adminUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier si l'utilisateur est ADMIN ou SUPER_ADMIN
        if (!adminUser.getRole().equals(Role.ROLE_ADMIN) && !adminUser.getRole().equals(Role.ROLE_SUPER_ADMIN)) {
            return ResponseEntity.badRequest().body("Accès non autorisé");
        }

        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email déjà utilisé");
        }

        // Vérifier si le username existe déjà
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username déjà utilisé");
        }

        // Créer l'utilisateur
        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_FORMATEUR)
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .build();
        userRepository.save(user);

        // Créer le formateur
        var formateur = Formateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .specialite(request.getSpecialite())
                .user(user)
                .build();
        formateurRepository.save(formateur);

        // Générer le token
        var jwtToken = jwtService.generateToken(user);

        // Préparer la réponse
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtToken);
        response.put("username", user.getUsername());
        response.put("role", user.getRole().name());
        response.put("formateur", formateur);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Vérifier si l'utilisateur existe
            var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

            // Authentifier l'utilisateur
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            // Générer le token JWT
            var token = jwtService.generateToken(user);

            // Retourner la réponse
            return ResponseEntity.ok(Map.of(
                "token", token,
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole()
                )
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized",
                "message", "Identifiants invalides"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "error", "Internal Server Error",
                "message", "Erreur lors de l'authentification"
            ));
        }
    }
} 