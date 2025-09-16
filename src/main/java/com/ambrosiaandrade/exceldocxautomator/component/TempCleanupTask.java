package com.ambrosiaandrade.exceldocxautomator.component;

import com.ambrosiaandrade.exceldocxautomator.controller.DocumentController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;

/**
 * Scheduled task that periodically cleans up old
 * temporary folders used for storing generated documents.
 *
 * This acts as a failsafe mechanism in case session cleanup
 * is not triggered (e.g. abrupt shutdown or forgotten downloads).
 */
@Component
public class TempCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(TempCleanupTask.class);

    private static final String BASE_DIR = System.getProperty("java.io.tmpdir") + "/upe";

    /**
     * Cleans up folders older than a given threshold.
     *
     * Runs every hour and deletes directories older than 6 hours.
     */
    @Scheduled(fixedRate = 1000 * 60 * 60) // every 1 hour
    public void cleanupOldFiles() {
        File base = new File(BASE_DIR);
        if (base.exists() && base.isDirectory()) {
            File[] dirs = base.listFiles(File::isDirectory);
            if (dirs != null) {
                for (File dir : dirs) {
                    long age = System.currentTimeMillis() - dir.lastModified();
                    if (age > 1000L * 60 * 60 * 6) { // older than 6 hours
                        try {
                            FileSystemUtils.deleteRecursively(dir);
                            logger.info("Cleaning old files");
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                        }
                    }
                }
            }
        }
    }
}

