package com.ambrosiaandrade.exceldocxautomator.controller;

import static com.ambrosiaandrade.exceldocxautomator.component.SessionCleanupListener.getOrCreateSessionFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ambrosiaandrade.exceldocxautomator.component.FolderNameGenerator;
import com.ambrosiaandrade.exceldocxautomator.service.ZipService;

import jakarta.servlet.http.HttpSession;

@RestController
public class DownloadController {

    private final ZipService zipService;

    public DownloadController(ZipService zipService) {
        this.zipService = zipService;
    }

    @GetMapping("/downloadGroup")
    public ResponseEntity<Resource> downloadGroup(@RequestParam("person") String name, HttpSession session)
            throws IOException {
        String folderName = FolderNameGenerator.generateFolderName(name);
        Path folderPath = Paths.get(getOrCreateSessionFolder(session).toString(), folderName);

        if (!Files.exists(folderPath) || !Files.isDirectory(folderPath)) {
            return ResponseEntity.notFound().build();
        }

        Path zipPath = zipService.createZip(folderPath, folderName + "_");
        return buildZipResponse(zipPath, folderName + ".zip");
    }

    @GetMapping("/downloadFile")
    public ResponseEntity<Resource> downloadFile(@RequestParam("file") String fileName, HttpSession session)
            throws IOException {
        Path sessionFolder = getOrCreateSessionFolder(session);
        Path filePath = sessionFolder.resolve("generic").resolve(fileName);
        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(filePath.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(resource);
    }

    @GetMapping("/downloadAll")
    public ResponseEntity<Resource> downloadAll(HttpSession session) throws IOException {
        Path rootPath = Paths.get(getOrCreateSessionFolder(session).toString());

        if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
            return ResponseEntity.notFound().build();
        }

        Path zipPath = zipService.createZip(rootPath, "all_");
        return buildZipResponse(zipPath, "all.zip");
    }

    /**
     * Builds a ResponseEntity to return a ZIP file as download.
     *
     * @param zipPath  Path to the ZIP file.
     * @param fileName Name of the ZIP file in the download response.
     * @return ResponseEntity with the ZIP as a downloadable resource.
     */
    private ResponseEntity<Resource> buildZipResponse(Path zipPath, String fileName) {
        Resource resource = new FileSystemResource(zipPath.toFile());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(resource);
    }

}
