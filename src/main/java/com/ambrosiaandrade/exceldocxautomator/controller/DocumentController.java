package com.ambrosiaandrade.exceldocxautomator.controller;

import com.ambrosiaandrade.exceldocxautomator.model.Student;
import com.ambrosiaandrade.exceldocxautomator.model.UnidadeConcedente;
import com.ambrosiaandrade.exceldocxautomator.service.ProcessDocumentService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.file.Path;
import java.util.*;

import static com.ambrosiaandrade.exceldocxautomator.component.Constants.*;
import static com.ambrosiaandrade.exceldocxautomator.component.DocumentUtils.getEMailIndex;
import static com.ambrosiaandrade.exceldocxautomator.component.DocumentUtils.getNameIndex;

@Controller
public class DocumentController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final ProcessDocumentService documentService;

    public DocumentController(ProcessDocumentService documentService) {
        this.documentService = documentService;
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
        if (groups == null) groups = Collections.emptyList();

        var models = documentService.listModelFiles();

        model.addAttribute(KEY_FILES, fileNames);
        model.addAttribute(KEY_GROUPS, groups);
        model.addAttribute(KEY_MODELS, models);

        logger.info("Arquivos gerados encontrados: {}\nArquivos modelos: {}", generatedFiles.size(), models.size());
        return KEY_FILES;
    }

    @GetMapping(PATH_GENERIC_LIST)
    public String listGeneric(Model model, HttpSession session) {
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
        if (groups == null) groups = Collections.emptyList();

        model.addAttribute(KEY_FILES, fileNames);
        model.addAttribute(KEY_GROUPS, groups);

        logger.info("Arquivos genéricos criados: {}", generatedFiles.size());
        return KEY_GENERIC_FILES;
    }

    @GetMapping(PATH_SUMMARY)
    public String listSummary(Model model, HttpSession session) {
        var schools = (Set<UnidadeConcedente>) session.getAttribute(KEY_SCHOOLS);
        var students = (List<Student>) session.getAttribute(KEY_STUDENTS);

        if (schools == null) schools = Collections.emptySet();
        if (students == null) students = Collections.emptyList();

        model.addAttribute(KEY_SCHOOLS, schools);
        model.addAttribute(KEY_STUDENTS, students);

        logger.info("Escolas: {}, Estudantes: {}", schools.size(), students.size());
        return KEY_SUMMARY;
    }

    @GetMapping(PATH_GENERATE)
    public ResponseEntity<?> generateDocument(HttpSession session) {

        List<List<String>> rows;
        Object doc;

        try {
            rows = (List<List<String>>) session.getAttribute(KEY_UPLOADED_EXCEL);
            doc = session.getAttribute(KEY_UPLOADED_WORD);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERROR, e.getMessage()));
        }

        if (rows == null || rows.isEmpty()) {
            return ResponseEntity.ok(Map.of(KEY_ERROR, "É necessário fazer o upload do(s) arquivo(s) primeiro!"));
        }

        Map<String, String> groups = new HashMap<>();
        Set<UnidadeConcedente> schools = new HashSet<>();
        List<Student> students = new ArrayList<>();
        List<String> header = rows.get(0);
        int indexName = getNameIndex(header);
        int indexMail = getEMailIndex(header);

        for (int i = 1; i < rows.size(); i++) {
            List<String> row = rows.get(i);
            Map<String, String> map = new HashMap<>();
            for (int j = 0; j < header.size(); j++) {
                map.put(header.get(j), row.get(j));
            }

            if (indexName != -1 && indexMail != -1 && !row.get(indexName).isBlank()) {
                groups.put(row.get(indexName), row.get(indexMail));
            }

            documentService.processDocument(map, session);
            if (map.containsKey("ES_MATRÍCULA")) {
                var uc = documentService.createUC(map);
                var student = new Student(map.get("ES_NOME"), map.get("ES_MATRÍCULA"), map.get("ES_CURSO"), uc.cnpj());
                schools.add(uc);
                students.add(student);
            }
        }

        if (!groups.isEmpty()) {
            session.setAttribute(KEY_GROUPS, groups);
        }

        if (!schools.isEmpty()) {
            session.setAttribute(KEY_SCHOOLS, schools);
            session.setAttribute(KEY_STUDENTS, students);
        }

        if (doc != null) {
            return ResponseEntity.ok(Map.of(KEY_REDIRECT_URL, PATH_GENERIC_LIST));
        }

        return ResponseEntity.ok(Map.of(KEY_REDIRECT_URL, PATH_LIST));
    }

}
