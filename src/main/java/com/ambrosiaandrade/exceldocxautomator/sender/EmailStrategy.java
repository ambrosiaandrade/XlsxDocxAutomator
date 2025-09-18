package com.ambrosiaandrade.exceldocxautomator.sender;

import jakarta.mail.MessagingException;

import java.nio.file.Path;

public interface EmailStrategy {
    void sendEmail(String to, String subject, String body);
    void sendEmail(String to, String subject, Path zipFile) throws MessagingException;
    void configure(String username, String password);
}
