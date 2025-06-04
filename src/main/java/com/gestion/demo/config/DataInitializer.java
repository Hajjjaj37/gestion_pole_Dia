package com.gestion.demo.config;

import com.gestion.demo.model.User;
import com.gestion.demo.model.Role;
import com.gestion.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            log.info("Début de l'initialisation des données...");
            
            // Créer un admin si n'existe pas
            if (!userRepository.existsByUsername("admin")) {
                log.info("Création de l'utilisateur admin...");
                
                User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@example.com")
                    .nom("Admin")
                    .prenom("System")
                    .role(Role.ROLE_ADMIN)
                    .build();
                
                userRepository.save(admin);
                log.info("Utilisateur admin créé avec succès");
            } else {
                log.info("L'utilisateur admin existe déjà");
            }
            
            log.info("Initialisation des données terminée avec succès");
            
        } catch (Exception e) {
            log.error("Erreur lors de l'initialisation des données: {}", e.getMessage(), e);
        }
    }
} 