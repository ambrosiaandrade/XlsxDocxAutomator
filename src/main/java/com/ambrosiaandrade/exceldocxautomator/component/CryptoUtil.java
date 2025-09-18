package com.ambrosiaandrade.exceldocxautomator.component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@Component
public class CryptoUtil {

    private final TextEncryptor encryptor;

    public CryptoUtil(@Value("${crypto.secretKey:defaultSecret}") String secretKey,
                      @Value("${crypto.salt}") String salt) {
        // secretKey = senha que você define no servidor (ex: variável de ambiente)
        // salt = string aleatória em hexadecimal (também pode estar no application.properties)
        this.encryptor = Encryptors.text(secretKey, salt);
    }

    public String encrypt(String plainText) {
        return encryptor.encrypt(plainText);
    }

    public String decrypt(String cipherText) {
        return encryptor.decrypt(cipherText);
    }

    public class TestCrypto {
        public static void main(String[] args) {
            // Em produção, isso viria de variáveis de ambiente
            String secretKey = "minha-chave-secreta";
            String salt = "deadbeef"; // precisa ter 16 chars hexadecimais

            CryptoUtil cryptoUtil = new CryptoUtil(secretKey, salt);

            String senhaOriginal = "senhaAppPassword";
            String senhaCriptografada = cryptoUtil.encrypt(senhaOriginal);
            String senhaDescriptografada = cryptoUtil.decrypt(senhaCriptografada);

            System.out.println("Original: " + senhaOriginal);
            System.out.println("Criptografada: " + senhaCriptografada);
            System.out.println("Decriptada: " + senhaDescriptografada);
        }
    }

}
