package com.ambrosiaandrade.exceldocxautomator.service;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DownloadGoogleSheetService {

    private DownloadGoogleSheetService(){}

    // Baixa CSV de uma Google Sheet pública (constrói URL /export?format=csv)
    public static InputStream downloadGoogleSheetCsv(String sheetUrl) throws IOException {
        // extrai spreadsheetId
        Pattern p = Pattern.compile("/d/([a-zA-Z0-9-_]+)");
        Matcher m = p.matcher(sheetUrl);
        if (!m.find()) throw new IllegalArgumentException("Não foi possível extrair ID da URL da Google Sheet");
        String spreadsheetId = m.group(1);

        // tenta extrair gid (se tiver)
        String gid = "0";
        Pattern gidPattern = Pattern.compile("[#&]gid=(\\d+)");
        Matcher mg = gidPattern.matcher(sheetUrl);
        if (mg.find()) gid = mg.group(1);

        String exportUrl = "https://docs.google.com/spreadsheets/d/" + spreadsheetId + "/export?format=csv&gid=" + gid;

        // GET simples
        URL url = new URL(exportUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "Java/HttpClient");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(15000);

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            // melhor copiar tudo para memória temporária e devolver InputStream seguro
            try (InputStream is = conn.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                is.transferTo(baos);
                return new ByteArrayInputStream(baos.toByteArray());
            }
        } else {
            String msg;
            try (InputStream es = conn.getErrorStream()) {
                msg = es == null ? "HTTP " + code : new String(es.readAllBytes(), StandardCharsets.UTF_8);
            }
            throw new IOException("Falha ao baixar Google Sheet: " + code + " - " + msg);
        }
    }

}
