package com.ambrosiaandrade.exceldocxautomator.controller;

import com.ambrosiaandrade.exceldocxautomator.component.ExcelReader;
import com.ambrosiaandrade.exceldocxautomator.service.ProcessDocumentService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ambrosiaandrade.exceldocxautomator.service.DownloadGoogleSheetService.downloadGoogleSheetCsv;

@Controller
public class UploadController {

    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    private final ProcessDocumentService processDocumentService;
    private final ExcelReader excelReader;

    private static final String PATH_LIST = "/list";
    private static final String PATH_UPLOAD = "/upload";
    private static final String KEY_FILES = "files";
    private static final String KEY_UPLOADED = "uploaded";
    private static final String KEY_ERROR = "error";

    public UploadController(ProcessDocumentService processDocumentService, ExcelReader excelReader) {
        this.processDocumentService = processDocumentService;
        this.excelReader = excelReader;
    }

    // 1) Recebe arquivo (Excel ou CSV)
    @PostMapping(value = PATH_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public ResponseEntity<?> uploadFileExcelOrCsv(@RequestParam("file") MultipartFile file, HttpSession session) {
        processDocumentService.clearPaths();
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERROR, "mensagem de erro"));
        }

        String filename = Optional.ofNullable(file.getOriginalFilename()).orElse("").toLowerCase();

        if (!isValidType(filename)) {
            return ResponseEntity.status(415).body(Map.of(KEY_ERROR, "Tipo de arquivo não suportado"));
        }

        try (InputStream is = file.getInputStream()) {

            List<List<String>> rows;
            if (filename.endsWith(".csv")) {
                rows = excelReader.parseCsv(is);
            } else {
                rows = excelReader.parseExcel(is);
            }

            session.setAttribute(KEY_UPLOADED, rows);
            session.setAttribute(KEY_FILES, processDocumentService.getPaths());

            return ResponseEntity.ok(Map.of("redirectUrl", PATH_LIST, "columns", rows.get(0)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.status(500).body(Map.of(KEY_ERROR, e.getMessage()));

        }
    }

    // 2) Recebe JSON com sheetUrl -> baixa CSV da Google Sheet pública e processa
    @PostMapping(value = PATH_UPLOAD, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> importSheet(@RequestBody Map<String, String> body, HttpSession session) {
        processDocumentService.clearPaths();
        String sheetUrl = body.get("sheetUrl");
        if (sheetUrl == null || sheetUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(KEY_ERROR, "sheetUrl é obrigatório"));
        }

        try (InputStream is = downloadGoogleSheetCsv(sheetUrl)) {
            List<List<String>> rows = excelReader.parseCsv(is);
            session.setAttribute(KEY_UPLOADED, rows);
            session.setAttribute(KEY_FILES, processDocumentService.getPaths());
            return ResponseEntity.ok(Map.of("redirectUrl", PATH_LIST, "columns", rows.get(0)));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(KEY_ERROR, e.getMessage()));
        }
    }

    private boolean isValidType(String filename) {
        return filename.endsWith(".csv") || filename.endsWith(".xls") || filename.endsWith(".xlsx");
    }

}
