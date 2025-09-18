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
import java.util.stream.Stream;

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
     * Cleans up old temporary directories that may have been left over from previous sessions.
     * <p>
     * This method is automatically executed upon the application's startup due to the
     * {@link PostConstruct} annotation. It iterates through all subdirectories in the
     * designated base temporary directory and recursively deletes them. This ensures
     * that the application starts with a clean slate, freeing up disk space and
     * preventing orphaned files from accumulating, particularly in cases where the
     * server was shut down improperly.
     * </p>
     */
    @PostConstruct
    public void cleanupOldTempDirs() {
        log.info("Cleaning up old temp directories in {}", BASE_DIR);
        try (Stream<Path> pathStream = Files.list(BASE_DIR)) {
            pathStream.filter(Files::isDirectory)
                    .forEach(dir -> {
                        try {
                            FileSystemUtils.deleteRecursively(dir);
                            log.info("Deleted old temp folder: {}", dir);
                        } catch (IOException e) {
                            log.error("Failed to delete old temp folder: {}", dir, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Error accessing base temp directory: {}", BASE_DIR, e);
        }
    }

    /**
     * Deletes the temporary directory associated with a user's session.
     * <p>
     * This method is automatically called by the servlet container when a session is invalidated,
     * either due to timeout, explicit invalidation, or a server shutdown. It ensures that
     * temporary files created during the session are cleaned up from the file system
     * to free up disk space and maintain security.
     * </p>
     *
     * @param se the {@link HttpSessionEvent} containing the session that is about to be destroyed.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        Path userDir = BASE_DIR.resolve(se.getSession().getId());
        if (Files.exists(userDir)) {
            try {
                FileSystemUtils.deleteRecursively(userDir);
                log.info("Deleted temp folder for session: {}", userDir);
            } catch (IOException e) {
                log.error("Failed to delete temp folder for session: {}", userDir, e);
            }
        }
    }

    /**
     * Retrieves or creates a temporary directory for a given user session.
     * <p>
     * This method checks if a directory for the current session ID already exists.
     * If not, it creates a new directory to store temporary files and documents
     * generated during the session's lifetime. The path to this directory is
     * also stored as a session attribute for easy retrieval later.
     * </p>
     *
     * @param session the {@link HttpSession} object for which to create the directory.
     * @return the {@link Path} to the session-specific temporary folder.
     * @throws IOException if an I/O error occurs while creating the directories.
     */
    public static Path getOrCreateSessionFolder(HttpSession session) throws IOException {
        Path sessionDir = BASE_DIR.resolve(session.getId());
        if (!Files.exists(sessionDir)) {
            Files.createDirectories(sessionDir);
            log.info("Created session folder: {}", sessionDir);
        }
        session.setAttribute("SESSION_FOLDER", sessionDir.toString());
        return sessionDir;
    }

}
