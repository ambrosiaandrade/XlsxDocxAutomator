package com.ambrosiaandrade.exceldocxautomator.component;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelReader {

    public List<List<String>> parseCsv(InputStream is) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(in, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            for (CSVRecord rec : parser) {
                List<String> row = new ArrayList<>();
                rec.forEach(row::add);
                rows.add(row);
            }
        }
        return rows;
    }

    public List<List<String>> parseExcel(InputStream is) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row r : sheet) {
                List<String> row = new ArrayList<>();
                int last = r.getLastCellNum() < 0 ? 0 : r.getLastCellNum();
                for (int c = 0; c < last; c++) {
                    Cell cell = r.getCell(c);
                    row.add(cellToString(cell));
                }
                rows.add(row);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        return rows;
    }

    private String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) yield cell.getDateCellValue().toString();
                else yield Double.toString(cell.getNumericCellValue());
            }
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK -> "";
            default -> cell.toString();
        };
    }

}