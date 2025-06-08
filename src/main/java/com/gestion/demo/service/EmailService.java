package com.gestion.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailException;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender emailSender;

    public void envoyerNotification(String destinataire, String sujet, String message) {
        try {
            if (!StringUtils.hasText(destinataire)) {
                logger.error("L'adresse email du destinataire est vide");
                return;
            }

            logger.info("Préparation de l'email pour {}", destinataire);
            
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("mohamedhajjaj460@gmail.com");
            mailMessage.setTo(destinataire);
            mailMessage.setSubject(sujet);
            mailMessage.setText(message);
            
            logger.info("Envoi de l'email à {}", destinataire);
            emailSender.send(mailMessage);
            logger.info("Email envoyé avec succès à {}", destinataire);
        } catch (MailException e) {
            logger.error("Erreur lors de l'envoi de l'email à {}: {}", destinataire, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email: " + e.getMessage(), e);
        }
    }

    public void envoyerNotificationMultiple(String[] destinataires, String sujet, String message) {
        if (destinataires == null || destinataires.length == 0) {
            throw new IllegalArgumentException("La liste des destinataires ne peut pas être vide");
        }
        if (!StringUtils.hasText(sujet)) {
            throw new IllegalArgumentException("Le sujet ne peut pas être vide");
        }
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Le message ne peut pas être vide");
        }

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(destinataires);
            mailMessage.setSubject(sujet);
            mailMessage.setText(message);
            emailSender.send(mailMessage);
            logger.info("Emails envoyés avec succès à {} destinataires", destinataires.length);
        } catch (MailException e) {
            logger.error("Erreur lors de l'envoi des emails: {}", e.getMessage());
            throw new RuntimeException("Impossible d'envoyer les emails. Veuillez réessayer plus tard.", e);
        }
    }
} 