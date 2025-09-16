package com.ambrosiaandrade.exceldocxautomator.component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Listener para limpeza de pastas temporárias de usuários quando a sessão expira.
 * <p>
 * Cada usuário terá uma subpasta dentro de /tmp/upe identificada por SESSION_FOLDER_NAME.
 * Ao destruir a sessão, a pasta correspondente será deletada recursivamente.
 * Também faz cleanup de pastas antigas na inicialização da aplicação.
 */
@Component
public class SessionCleanupListener implements HttpSessionListener {

    private static final Logger log = LoggerFactory.getLogger(SessionCleanupListener.class);
    private static final Path BASE_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "upe");

    /**
     * Limpa pastas órfãs na inicialização do aplicativo.
     */
    @PostConstruct
    public void cleanupOldTempDirs() {
        log.info("Cleaning up old temp directories in {}", BASE_DIR);
        try {
            if (Files.exists(BASE_DIR) && Files.isDirectory(BASE_DIR)) {
                Files.list(BASE_DIR)
                        .filter(Files::isDirectory)
                        .forEach(dir -> {
                            try {
                                FileSystemUtils.deleteRecursively(dir);
                                log.info("Deleted old temp folder: {}", dir);
                            } catch (IOException e) {
                                log.error("Failed to delete old temp folder: {}", dir, e);
                            }
                        });
            }
        } catch (IOException e) {
            log.error("Error accessing base temp directory: {}", BASE_DIR, e);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        Object folderName = se.getSession().getAttribute("FOLDER_NAME");
        if (folderName != null) {
            Path userDir = BASE_DIR.resolve(folderName.toString());
            if (Files.exists(userDir)) {
                try {
                    FileSystemUtils.deleteRecursively(userDir);
                    log.info("Deleted temp folder for session: {}", userDir);
                } catch (IOException e) {
                    log.error("Failed to delete temp folder for session: {}", userDir, e);
                }
            }
        }
    }

    /**
     * Cria a pasta da sessão, se não existir.
     */
    public static Path getOrCreateSessionFolder(HttpSession session) throws IOException {
        Path sessionDir = BASE_DIR.resolve(session.getId());

        if (!Files.exists(sessionDir)) {
            Files.createDirectories(sessionDir);
            log.info("Created session folder: {}", sessionDir);
        }

        // Salva o caminho da pasta da sessão na sessão
        session.setAttribute("SESSION_FOLDER", sessionDir.toString());

        return sessionDir;
    }

}
