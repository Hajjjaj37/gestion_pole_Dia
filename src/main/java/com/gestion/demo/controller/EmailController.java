package com.gestion.demo.controller;

import com.gestion.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/envoyer")
    public String envoyerEmail(@RequestParam String destinataire, 
                             @RequestParam String sujet, 
                             @RequestParam String message) {
        try {
            emailService.envoyerNotification(destinataire, sujet, message);
            return "Email envoyé avec succès";
        } catch (Exception e) {
            return "Erreur lors de l'envoi de l'email: " + e.getMessage();
        }
    }

    @PostMapping("/envoyer-multiple")
    public String envoyerEmailMultiple(@RequestParam String[] destinataires, 
                                     @RequestParam String sujet, 
                                     @RequestParam String message) {
        try {
            emailService.envoyerNotificationMultiple(destinataires, sujet, message);
            return "Emails envoyés avec succès";
        } catch (Exception e) {
            return "Erreur lors de l'envoi des emails: " + e.getMessage();
        }
    }
} 