package com.ambrosiaandrade.exceldocxautomator.component;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility class for handling document-related operations, specifically for
 * finding column indices in a spreadsheet header.
 */
@Component
public class DocumentUtils {

    private DocumentUtils() {}

    /**
     * Retrieves the index of the name column from a list of header strings.
     * The search is case-insensitive and checks for common name-related keywords.
     *
     * @param header The list of strings representing the spreadsheet header.
     * @return The zero-based index of the name column, or -1 if not found.
     */
    public static int getNameIndex(List<String> header) {
        Set<String> nameOptions = new HashSet<>(Arrays.asList("nome", "name", "es_nome"));
        for (int i = 0; i < header.size(); i++) {
            if (nameOptions.contains(header.get(i).toLowerCase())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Retrieves the index of the email column from a list of header strings.
     * The search is case-insensitive and checks for common email-related keywords.
     *
     * @param header The list of strings representing the spreadsheet header.
     * @return The zero-based index of the email column, or -1 if not found.
     */
    public static int getEMailIndex(List<String> header) {
        Set<String> emailOptions = new HashSet<>(Arrays.asList("endereço de e-mail", "e-mail", "email", "mail", "es_e-mail"));
        for (int i = 0; i < header.size(); i++) {
            if (emailOptions.contains(header.get(i).toLowerCase())) {
                return i;
            }
        }
        return -1;
    }
}
