package com.ambrosiaandrade.exceldocxautomator.component;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for generating unique and consistent folder names
 * based on a person's name. The folder name combines a slugified
 * version of the name (only alphanumeric characters) with a short
 * hash to ensure uniqueness.
 */
@Component
public class FolderNameGenerator {

    /**
     * Generates a unique folder name for a given person name.
     *
     * The format is: <SlugifiedName>_<ShortHash>
     * Example: "MariaTorres_5D41AB12"
     *
     * @param name The original person's name (may contain spaces or special characters).
     * @return A safe, unique folder name.
     */
    public static String generateFolderName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        // Slugify: keep only letters and digits
        String slug = name.replaceAll("[^a-zA-Z0-9]", "");

        // Ensure slug is not empty
        if (slug.isBlank()) {
            slug = "User";
        }

        // Generate short hash (first 8 chars of SHA-256)
        String hash = sha256(name).substring(0, 8).toUpperCase();

        return slug + "_" + hash;
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Convert to hex
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

}

