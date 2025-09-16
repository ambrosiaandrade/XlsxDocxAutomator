package com.ambrosiaandrade.exceldocxautomator.controller;

import com.ambrosiaandrade.exceldocxautomator.service.ProcessDocumentService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.ambrosiaandrade.exceldocxautomator.component.SessionCleanupListener.getOrCreateSessionFolder;

@Controller
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    private static final Path BASE_DIR = Paths.get(System.getProperty("java.io.tmpdir"), "upe");

    private final ProcessDocumentService processDocumentService;

    private static final String PATH_LIST = "/list";
    private static final String PATH_GENERATE = "/generate";
    private static final String KEY_FILES = "files";
    private static final String KEY_UPLOADED = "uploaded";
    private static final String KEY_GROUPS = "groups";

    public DocumentController(ProcessDocumentService processDocumentService) {
        this.processDocumentService = processDocumentService;
    }

    @GetMapping(PATH_LIST)
    public String listDocx(Model model, HttpSession session) {
        // Obtém a lista da sessão
        Collection<Path> generatedFiles = (Collection<Path>) session.getAttribute(KEY_FILES);
        if (generatedFiles == null) {
            generatedFiles = new ArrayList<>();
        }
        // Converte Path -> String (nome do arquivo)
        List<String> fileNames = generatedFiles.stream()
                .map(p -> p.getFileName().toString())
                .toList();

        var groups = session.getAttribute(KEY_GROUPS);
        var models = processDocumentService.listModelFiles();

        model.addAttribute(KEY_FILES, fileNames);
        model.addAttribute(KEY_GROUPS, groups);
        model.addAttribute("models", models);

        logger.info("Arquivos gerados encontrados: {}\nArquivos modelos: {}", generatedFiles.size(), models.size());
        return KEY_FILES;
    }

    @GetMapping(PATH_GENERATE)
    public ResponseEntity<?> generateDocument(HttpSession session,
                                              @RequestParam("fieldName") String name,
                                              @RequestParam("fieldEmail") String mail) throws IOException {

        var rows = (List<List<String>>) session.getAttribute(KEY_UPLOADED);

        List<String> groups = new ArrayList<>();
        List<String> header = rows.get(0);
        int indexName = header.indexOf(name);

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> map = new HashMap<>();
            for (int j = 0; j < header.size(); j++) {
                map.put(header.get(j), row.get(j));
            }

            groups.add(row.get(indexName));

            // for each person it will run the process document service
            processDocumentService.run(map, row.get(indexName), getOrCreateSessionFolder(session));
        }

        session.setAttribute(KEY_GROUPS, groups);

        return ResponseEntity.ok(Map.of("redirectUrl", PATH_LIST));
    }

}
