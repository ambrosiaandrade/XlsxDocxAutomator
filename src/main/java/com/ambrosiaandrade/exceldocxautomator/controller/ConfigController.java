package com.ambrosiaandrade.exceldocxautomator.controller;

import com.ambrosiaandrade.exceldocxautomator.component.CryptoUtil;
import com.ambrosiaandrade.exceldocxautomator.model.MailCredentials;
import com.ambrosiaandrade.exceldocxautomator.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.ambrosiaandrade.exceldocxautomator.component.Constants.KEY_MAILCREDS;

@RestController
public class ConfigController {

    private EmailService emailService;
    private CryptoUtil cryptoUtil;

    public ConfigController(EmailService emailService, CryptoUtil cryptoUtil) {
        this.emailService = emailService;
        this.cryptoUtil = cryptoUtil;
    }

    @PostMapping("/configEmail")
    public ResponseEntity<?> saveConfig(@RequestBody Map<String, String> map,
                                        HttpSession session) {

        String encrypted = cryptoUtil.encrypt(map.get("password"));
        MailCredentials creds = new MailCredentials(map.get("provider"), map.get("email"), encrypted);

        session.setAttribute(KEY_MAILCREDS, creds);

        return ResponseEntity.ok(Map.of("message", "Credenciais salvas com sucesso"));
    }

    @GetMapping("/validateAndSend")
    public ResponseEntity<?> validateAndSend(
            HttpSession session) {

        try {
            MailCredentials creds = (MailCredentials) session.getAttribute(KEY_MAILCREDS);

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

}
