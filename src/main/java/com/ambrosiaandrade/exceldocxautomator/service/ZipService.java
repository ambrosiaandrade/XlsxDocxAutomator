package com.ambrosiaandrade.exceldocxautomator.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {

    /**
     * Creates a temporary ZIP file with the contents of the given folder.
     *
     * @param folderPath Folder whose files will be zipped.
     * @param prefix     Prefix for the temporary ZIP filename.
     * @return Path to the created ZIP file.
     * @throws IOException If an I/O error occurs.
     */
    public Path createZip(Path folderPath, String prefix) throws IOException {
        Path zipPath = Files.createTempFile(prefix, ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Files.walk(folderPath)
                    .filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            Path relativePath = folderPath.relativize(file);
                            zos.putNextEntry(new ZipEntry(relativePath.toString()));
                            Files.copy(file, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }

        return zipPath;
    }

    /**
     * Wraps a file path as a Spring Resource for download.
     *
     * @param zipPath  Path to the ZIP file.
     * @return Resource pointing to the file.
     */
    public Resource asResource(Path zipPath) {
        return new FileSystemResource(zipPath.toFile());
    }
}

