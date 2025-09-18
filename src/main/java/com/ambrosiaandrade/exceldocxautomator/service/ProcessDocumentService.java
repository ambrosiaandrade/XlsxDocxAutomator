package com.ambrosiaandrade.exceldocxautomator.service;

import jakarta.servlet.http.HttpSession;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

import static com.ambrosiaandrade.exceldocxautomator.component.FolderNameGenerator.generateFolderName;

@Service
public class ProcessDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDocumentService.class);

    private final String[] guillemet = {"«", "»"};
    private final String[] placeholder = {"<", ">"};
    private final Collection<Path> paths = new ArrayList<>();

    private final ResourcePatternResolver resourcePatternResolver;

    public ProcessDocumentService(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public void run(Map<String, String> dados, String name, Path sessionFolder) {
        var foldername = generateFolderName(name);
        var models = listModelFiles();
        for (String model : models) {
            var filename = handleFileName(name, model);
            fulfillTemplate(dados, filename, foldername, sessionFolder, model);
        }
    }

    private void fulfillTemplate(Map<String, String> dados, String filename, String foldername, Path sessionFolder, String model) {
        try (InputStream templateStream = new ClassPathResource(String.format("model/%s", model)).getInputStream();
             XWPFDocument doc = new XWPFDocument(templateStream)) {

            // Substituir placeholders nos parágrafos
            for (XWPFParagraph p : doc.getParagraphs()) {
                replaceAcrossRuns(p, dados);
            }

            // Substituir placeholders em tabelas também
            for (XWPFTable table : doc.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            replaceAcrossRuns(p, dados);
                        }
                    }
                }
            }

            String baseDir = sessionFolder + "/" + foldername;
            Files.createDirectories(Paths.get(baseDir));

            Path filePath = Paths.get(baseDir, filename);

            try (OutputStream os = Files.newOutputStream(filePath)) {
                doc.write(os);
            }

            paths.add(filePath);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String handleFileName(String name, String filename) {
        var fieldnames = name.split(" ");
        return new StringBuilder(fieldnames[0])
                .append("_")
                .append(fieldnames[fieldnames.length - 1])
                .append("_").append(filename.replace(".docx", ""))
                .append("_").append(LocalDate.now().toString().replace("-", ""))
                .append(".docx")
                .toString().toUpperCase();
    }

    private void replaceText(XWPFParagraph p, Map<String, String> dados) {
        for (XWPFRun run : p.getRuns()) {
            String text = run.getText(0);
            logger.info(text);
            if (text != null) {
                for (Map.Entry<String, String> entry : dados.entrySet()) {
                    String mark1 = guillemet[0] + entry.getKey() + guillemet[1];
                    String mark2 = placeholder[0] + entry.getKey() + placeholder[1];
                    if (text.contains(mark1)) {
                        text = text.replace(mark1, entry.getValue());
                    } else if (text.contains(mark2)) {
                        text = text.replace(mark2, entry.getValue());
                    }
                }
                run.setText(text, 0);
            }
        }
    }

    // fixme ? está perdendo a formatação mas está preenchendo
    private void replaceAcrossRuns(XWPFParagraph p, Map<String, String> dados) {
        StringBuilder buffer = new StringBuilder();
        List<XWPFRun> runs = p.getRuns();

        for (int i = 0; i < runs.size(); i++) {
            String text = runs.get(i).getText(0);
            if (text != null) buffer.append(text);
        }

        String fullText = buffer.toString();

        for (Map.Entry<String, String> entry : dados.entrySet()) {
            String mark1 = guillemet[0] + entry.getKey() + guillemet[1];
            String mark2 = placeholder[0] + entry.getKey() + placeholder[1];
            fullText = fullText.replace(mark1, entry.getValue())
                    .replace(mark2, entry.getValue());
        }

        // Limpar runs antigos e recriar apenas um
        for (int i = runs.size() - 1; i >= 0; i--) {
            p.removeRun(i);
        }
        XWPFRun run = p.createRun();
        run.setText(fullText, 0);
    }


    /**
     * Lista todos os arquivos .docx na pasta 'src/main/resources/model'.
     *
     * @return Uma lista de nomes de arquivos dos modelos disponíveis.
     */
    public List<String> listModelFiles() {
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:model/*.docx");
            return Arrays.stream(resources)
                    .map(Resource::getFilename)
                    .filter(filename -> filename != null && !filename.startsWith("~$"))
                    .toList();
        } catch (IOException e) {
            logger.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    public void clearPaths() {
        this.paths.clear();
    }

    public Collection<Path> getPaths() {
        return this.paths;
    }

}