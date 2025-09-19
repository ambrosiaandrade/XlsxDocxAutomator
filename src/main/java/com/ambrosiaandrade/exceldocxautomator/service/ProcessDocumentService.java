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

import static com.ambrosiaandrade.exceldocxautomator.component.Constants.KEY_UPLOADED_WORD;
import static com.ambrosiaandrade.exceldocxautomator.component.Constants.KEY_WORD_NAME;
import static com.ambrosiaandrade.exceldocxautomator.component.FolderNameGenerator.generateFolderName;
import static com.ambrosiaandrade.exceldocxautomator.component.SessionCleanupListener.getOrCreateSessionFolder;

/**
 * Service responsible for processing Word document templates (.docx), replacing
 * placeholders with provided data,
 * and saving the generated documents to the appropriate session folders. Also
 * provides utilities for listing available models.
 */
@Service
public class ProcessDocumentService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessDocumentService.class);

    private final String[] guillemet = {"«", "»"};
    private final String[] placeholder = {"<", ">"};
    private final Collection<Path> paths = new ArrayList<>();

    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * Constructor for ProcessDocumentService.
     *
     * @param resourcePatternResolver the resource pattern resolver for loading
     *                                model files
     */
    public ProcessDocumentService(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * Processes a document based on the user's flow. If the session contains the
     * generic Word file (KEY_UPLOADED_WORD),
     * it uses the generic flow; otherwise, it uses the template flow.
     *
     * @param dados         Map containing data to fill in the placeholders
     * @param name          Name used for generating output filenames and folders
     * @param sessionFolder Path to the session folder where documents will be saved
     * @param session       HttpSession to check for the generic Word file
     * @throws IOException
     */ // todo remover String name pq posso pegar do mapa
    public void processDocument(Map<String, String> dados, HttpSession session) {
        try {
            Path sessionFolder = getOrCreateSessionFolder(session);
            Object wordBytesObj = session.getAttribute(KEY_UPLOADED_WORD);
            if (wordBytesObj instanceof byte[]) {
                logger.info("Detected generic flow (session contains uploaded Word file). Processing generic document.");
                processGenericDocument(dados, sessionFolder, (byte[]) wordBytesObj, session.getAttribute(KEY_WORD_NAME).toString());
            } else {
                logger.info("No uploaded Word file in session. Processing using template flow.");
                processTemplateDocuments(dados, dados.get("ES_NOME"), sessionFolder);
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Processes all available model templates, replacing placeholders with the
     * provided data and saving the generated documents.
     *
     * @param dados         Map containing data to fill in the placeholders
     * @param name          Name used for generating output filenames and folders
     * @param sessionFolder Path to the session folder where documents will be saved
     */
    private void processTemplateDocuments(Map<String, String> dados, String name, Path sessionFolder) {
        logger.info("Starting template-based document processing for name: {} in session folder: {}", name,
                sessionFolder);
        var foldername = generateFolderName(name);
        var models = listModelFiles();
        logger.info("Found {} model(s) to process: {}", models.size(), models);
        for (String model : models) {
            String baseDir = sessionFolder + "/" + foldername;
            fulfillTemplate(dados, baseDir, model);
        }
        logger.info("Template-based document processing completed for name: {}", name);
    }

    /**
     * Processes a generic document from the provided Word bytes and Excel data
     * saved in the session.
     *
     * @param dados         Map containing data to fill in the placeholders
     * @param sessionFolder Path to the session folder
     * @param wordBytes     Byte array of the Word template file
     * @param wordName      Name of the Word template file
     */
    private void processGenericDocument(Map<String, String> dados, Path sessionFolder, byte[] wordBytes, String wordName) {
        logger.info("Fulfilling generic template for session folder: {}", sessionFolder);
        try (InputStream templateStream = new java.io.ByteArrayInputStream(wordBytes);
             XWPFDocument doc = new XWPFDocument(templateStream)) {

            replacePlaceholders(doc, dados);

            String baseDir = sessionFolder + "/generic";
            Files.createDirectories(Paths.get(baseDir));
            String filename = UUID.randomUUID().toString().substring(0, 5) + "_" + wordName;
            Path filePath = Paths.get(baseDir, filename);

            saveDocument(doc, filePath);
            paths.add(filePath);
            logger.debug("Generic template fulfilled and saved to: {}", filePath);
        } catch (Exception e) {
            logger.error("Error fulfilling generic template: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Fulfills a specific Word template by replacing placeholders with provided
     * data and saves the result.
     *
     * @param dados   Map containing data to fill in the placeholders
     * @param baseDir Output filename for the generated document
     * @param model   Name of the model template file
     */
    private void fulfillTemplate(Map<String, String> dados, String baseDir, String model) {
        String filename = handleFileName(model);
        logger.info("Fulfilling template: {} with output filename: {}", model, filename);
        try (InputStream templateStream = new ClassPathResource(String.format("model/%s", model)).getInputStream();
             XWPFDocument doc = new XWPFDocument(templateStream)) {

            replacePlaceholders(doc, dados);

            Files.createDirectories(Paths.get(baseDir));
            Path filePath = Paths.get(baseDir, filename);

            saveDocument(doc, filePath);
            paths.add(filePath);
            logger.info("Template fulfilled and saved to: {}", filePath);
        } catch (Exception e) {
            logger.error("Error fulfilling template {}: {}", model, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Replaces placeholders in all paragraphs and tables of the document.
     *
     * @param doc   The XWPFDocument to process
     * @param dados Map containing data to fill in the placeholders
     */
    private void replacePlaceholders(XWPFDocument doc, Map<String, String> dados) {
        // Replace placeholders in paragraphs
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceAcrossRuns(p, dados);
        }
        // Replace placeholders in tables
        for (XWPFTable table : doc.getTables()) {
            for (XWPFTableRow row : table.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    for (XWPFParagraph p : cell.getParagraphs()) {
                        replaceAcrossRuns(p, dados);
                    }
                }
            }
        }
    }

    /**
     * Saves the XWPFDocument to the specified file path.
     *
     * @param doc      The XWPFDocument to save
     * @param filePath The path to save the document to
     * @throws IOException if an I/O error occurs
     */
    private void saveDocument(XWPFDocument doc, Path filePath) throws IOException {
        try (OutputStream os = Files.newOutputStream(filePath)) {
            doc.write(os);
        }
    }

    // fulfillGenericTemplate is now handled by processGenericDocument (private)

    /**
     * Generates an output filename based on the provided name and model filename.
     *
     * @param filename The model filename
     * @return The generated output filename in uppercase
     */
    private String handleFileName(String filename) {
        String result = new StringBuilder()
                .append(filename.replace(".docx", ""))
                .append("_").append(LocalDate.now().toString().replace("-", ""))
                .append(".docx")
                .toString();
        logger.info("Generated output filename: {}", result);
        return result;
    }

    /**
     * Replaces placeholders in a paragraph's runs with the provided data.
     *
     * @param p     The paragraph to process
     * @param dados Map containing data to fill in the placeholders
     */
    private void replaceText(XWPFParagraph p, Map<String, String> dados) {
        for (XWPFRun run : p.getRuns()) {
            String text = run.getText(0);
            logger.debug("Original run text: {}", text);
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
                logger.debug("Updated run text: {}", text);
            }
        }
    }

    /**
     * Replaces placeholders across all runs in a paragraph with the provided data.
     * Note: This may lose some formatting as all runs are merged into one.
     *
     * @param p     The paragraph to process
     * @param dados Map containing data to fill in the placeholders
     */
    private void replaceAcrossRuns(XWPFParagraph p, Map<String, String> dados) {
        StringBuilder buffer = new StringBuilder();
        List<XWPFRun> runs = p.getRuns();

        for (int i = 0; i < runs.size(); i++) {
            String text = runs.get(i).getText(0);
            if (text != null)
                buffer.append(text);
        }

        String fullText = buffer.toString();
        logger.debug("Paragraph full text before replacement: {}", fullText);

        for (Map.Entry<String, String> entry : dados.entrySet()) {
            String mark1 = guillemet[0] + entry.getKey() + guillemet[1];
            String mark2 = placeholder[0] + entry.getKey() + placeholder[1];
            fullText = fullText.replace(mark1, entry.getValue())
                    .replace(mark2, entry.getValue());
        }

        // Remove old runs and create a single new run
        for (int i = runs.size() - 1; i >= 0; i--) {
            p.removeRun(i);
        }
        XWPFRun run = p.createRun();
        run.setText(fullText, 0);
        logger.debug("Paragraph full text after replacement: {}", fullText);
    }

    /**
     * Lists all .docx files in the 'src/main/resources/model' folder.
     *
     * @return A list of available model filenames
     */
    public List<String> listModelFiles() {
        logger.info("Listing all model files in 'model' folder");
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:model/*.docx");
            List<String> result = Arrays.stream(resources)
                    .map(Resource::getFilename)
                    .filter(filename -> filename != null && !filename.startsWith("~$"))
                    .toList();
            logger.info("Found {} model file(s): {}", result.size(), result);
            return result;
        } catch (IOException e) {
            logger.error("Error listing model files: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Clears the collection of generated file paths.
     */
    public void clearPaths() {
        logger.info("Clearing all generated file paths");
        this.paths.clear();
    }

    /**
     * Returns the collection of generated file paths.
     *
     * @return Collection of generated file paths
     */
    public Collection<Path> getPaths() {
        logger.info("Retrieving all generated file paths");
        return this.paths;
    }

}