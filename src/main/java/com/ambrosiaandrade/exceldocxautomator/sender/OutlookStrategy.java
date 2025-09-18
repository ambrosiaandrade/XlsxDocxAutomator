package com.ambrosiaandrade.exceldocxautomator.sender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Properties;

@Component
public class OutlookStrategy implements EmailStrategy {

    private final JavaMailSenderImpl mailSender;
    private String mailBody = "Prezado estudante, segue em anexo os documentos do estágio. Verifique se as informações estão preenchidas corretamente.";

    public OutlookStrategy() {
        mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.office365.com");
        mailSender.setPort(587);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
    }

    @Override
    public void configure(String username, String password) {
        this.mailSender.setUsername(username);
        this.mailSender.setPassword(password);
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void sendEmail(String to, String subject, Path zipFile) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        // true = multipart
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(mailBody);

        // Adicionando o arquivo .zip como anexo
        FileSystemResource file = new FileSystemResource(zipFile.toFile());
        helper.addAttachment(zipFile.toFile().getName(), file);

        mailSender.send(message);
    }

}
