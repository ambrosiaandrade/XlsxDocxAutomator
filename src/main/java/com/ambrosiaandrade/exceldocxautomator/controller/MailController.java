package com.ambrosiaandrade.exceldocxautomator.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailController {

    @GetMapping("/sendEmailGroup")
    public String sendEmail(@RequestParam String person) {
        // lógica para enviar esse arquivo por e-mail
        return "redirect:/list";
    }

    @GetMapping("/sendAllEmails")
    public String sendAllEmails(HttpSession session) {
        // lógica para pegar todos da sessão e enviar por e-mail
        return "redirect:/list";
    }

}
