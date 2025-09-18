package com.ambrosiaandrade.exceldocxautomator.service;

import com.ambrosiaandrade.exceldocxautomator.model.MailCredentials;
import com.ambrosiaandrade.exceldocxautomator.sender.EmailStrategy;
import com.ambrosiaandrade.exceldocxautomator.sender.GmailStrategy;
import com.ambrosiaandrade.exceldocxautomator.sender.OutlookStrategy;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class EmailService {

    private EmailStrategy emailStrategy;

    @Autowired
    public EmailService(EmailStrategy emailStrategy) {
        this.emailStrategy = emailStrategy;
    }

    public void send(String to, String subject, String body) {
        emailStrategy.sendEmail(to, subject, body);
    }

    public void send(String to, String subject, Path zipFile) throws MessagingException {
        emailStrategy.sendEmail(to, subject, zipFile);
    }

    public void handleStrategy(MailCredentials creds, String decryptedPassword) {
        String provider = creds.provider();

        EmailStrategy strategy = switch (provider) {
            case "gmail" -> new GmailStrategy();
            case "outlook" -> new OutlookStrategy();
            default -> throw new IllegalArgumentException("Provedor não suportado: " + provider);
        };

        strategy.configure(creds.email(), decryptedPassword);
        emailStrategy = strategy;
    }

}
