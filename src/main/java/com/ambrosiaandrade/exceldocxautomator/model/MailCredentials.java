package com.ambrosiaandrade.exceldocxautomator.model;

public record MailCredentials (String provider,
                               String email,
                               String encryptedPassword) {
}