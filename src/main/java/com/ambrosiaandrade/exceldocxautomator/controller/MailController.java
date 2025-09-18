package com.ambrosiaandrade.exceldocxautomator.controller;

import static com.ambrosiaandrade.exceldocxautomator.component.SessionCleanupListener.getOrCreateSessionFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ambrosiaandrade.exceldocxautomator.component.CryptoUtil;
import com.ambrosiaandrade.exceldocxautomator.component.FolderNameGenerator;
import com.ambrosiaandrade.exceldocxautomator.model.MailCredentials;
import com.ambrosiaandrade.exceldocxautomator.service.EmailService;
import com.ambrosiaandrade.exceldocxautomator.service.ZipService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@RestController
public class MailController {

    private EmailService emailService;
    private CryptoUtil cryptoUtil;
    private ZipService zipService;

    public MailController(EmailService emailService, CryptoUtil cryptoUtil, ZipService zipService) {
        this.emailService = emailService;
        this.cryptoUtil = cryptoUtil;
        this.zipService = zipService;
    }

    @GetMapping("/sendEmailGroup")
    public ResponseEntity<?> sendEmail(@RequestParam("person") String name,
            @RequestParam("email") String recipient,
            HttpSession session) throws IOException, MessagingException {
        MailCredentials creds = getMailCredentials(session);
        if (creds == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nenhuma configuração de e-mail encontrada."));
        }
        try {
            sendGroupEmail(session, creds, name, recipient);
            return ResponseEntity.ok(Map.of("message", "E-mail enviado com sucesso!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtém as credenciais de e-mail da sessão
     */
    private MailCredentials getMailCredentials(HttpSession session) {
        return (MailCredentials) session.getAttribute("mailCreds");
    }

    /**
     * Envia o zip de um grupo para o e-mail informado
     */
    private void sendGroupEmail(HttpSession session, MailCredentials creds, String name, String recipient)
            throws IOException, MessagingException {
        String decryptedPassword = cryptoUtil.decrypt(creds.encryptedPassword());
        emailService.handleStrategy(creds, decryptedPassword);
        String folderName = FolderNameGenerator.generateFolderName(name);
        Path folderPath = Paths.get(getOrCreateSessionFolder(session).toString(), folderName);
        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            throw new IOException("Pasta não encontrada para " + name);
        }
        Path zipPath = zipService.createZip(folderPath, folderName + "_");
        emailService.send(recipient, "Arquivos de " + name, zipPath);
    }

    @GetMapping("/sendAllEmails")
    public ResponseEntity<?> sendAllEmails(HttpSession session) {
        MailCredentials creds = getMailCredentials(session);
        if (creds == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nenhuma configuração de e-mail encontrada."));
        }
        Object groupsObj = session.getAttribute("groups");
        if (!(groupsObj instanceof Map)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nenhum grupo encontrado na sessão."));
        }
        @SuppressWarnings("unchecked")
        Map<String, String> groups = (Map<String, String>) groupsObj;
        int success = 0;
        int fail = 0;
        StringBuilder errors = new StringBuilder();
        for (Map.Entry<String, String> entry : groups.entrySet()) {
            String name = entry.getKey();
            String email = entry.getValue();
            try {
                sendGroupEmail(session, creds, name, email);
                success++;
            } catch (Exception e) {
                fail++;
                errors.append(name).append(": ").append(e.getMessage()).append("; ");
            }
        }
        if (fail == 0) {
            return ResponseEntity.ok(Map.of("message", "Todos os e-mails enviados com sucesso!"));
        } else {
            return ResponseEntity.ok(Map.of(
                    "message", String.format("%d enviados, %d falharam.", success, fail),
                    "errors", errors.toString()));
        }
    }

}
