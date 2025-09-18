package com.ambrosiaandrade.exceldocxautomator.controller;

import com.ambrosiaandrade.exceldocxautomator.component.CryptoUtil;
import com.ambrosiaandrade.exceldocxautomator.component.FolderNameGenerator;
import com.ambrosiaandrade.exceldocxautomator.model.MailCredentials;
import com.ambrosiaandrade.exceldocxautomator.service.EmailService;
import com.ambrosiaandrade.exceldocxautomator.service.ZipService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.ambrosiaandrade.exceldocxautomator.component.SessionCleanupListener.getOrCreateSessionFolder;

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

    @PostMapping("/configEmail")
    public ResponseEntity<?> saveConfig(@RequestBody Map<String, String> map,
            HttpSession session) {

        String encrypted = cryptoUtil.encrypt(map.get("password"));
        MailCredentials creds = new MailCredentials(map.get("provider"), map.get("email"), encrypted);

        session.setAttribute("mailCreds", creds);

        return ResponseEntity.ok(Map.of("message", "Credenciais salvas com sucesso"));
    }

    @GetMapping("/validateAndSend")
    public ResponseEntity<?> validateAndSend(
            HttpSession session) {

        try {
            MailCredentials creds = (MailCredentials) session.getAttribute("mailCreds");

            if (creds == null) {
                return ResponseEntity.badRequest().body("Nenhuma configuração de e-mail encontrada.");
            }

            String decryptedPassword = cryptoUtil.decrypt(creds.encryptedPassword());

            emailService.handleStrategy(creds, decryptedPassword);
            emailService.send(creds.email(), "Teste de configuração",
                    "Se você recebeu este e-mail, a configuração está correta ✅");

            return ResponseEntity.ok(Map.of("message", "E-mail de teste enviado com sucesso!"));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Falha ao autenticar/enviar ❌: " + e.getMessage()));
        }
    }

    @GetMapping("/sendEmailGroup")
    public ResponseEntity<?> sendEmail(@RequestParam("person") String name,
                                                         @RequestParam("email") String recipient,
                                                         HttpSession session) throws IOException, MessagingException {

        MailCredentials creds = (MailCredentials) session.getAttribute("mailCreds");

        if (creds == null) {
            return ResponseEntity.badRequest().body(Map.of("error","Nenhuma configuração de e-mail encontrada."));
        }

        String decryptedPassword = cryptoUtil.decrypt(creds.encryptedPassword());

        emailService.handleStrategy(creds, decryptedPassword);

        String folderName = FolderNameGenerator.generateFolderName(name);
        Path folderPath = Paths.get(getOrCreateSessionFolder(session).toString(), folderName);

        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return ResponseEntity.notFound().build();
        }

        Path zipPath = zipService.createZip(folderPath, folderName + "_");

        emailService.send(recipient, "Arquivos de " + name, zipPath);

        return ResponseEntity.ok(Map.of("message", "E-mails enviados com sucesso!"));
    }

    // todo
    @GetMapping("/sendAllEmails")
    public String sendAllEmails(HttpSession session) {
        // lógica para pegar todos da sessão e enviar por e-mail
        return "redirect:/list";
    }

}
