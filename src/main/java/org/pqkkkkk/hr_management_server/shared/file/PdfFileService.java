package org.pqkkkkk.hr_management_server.shared.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.pqkkkkk.hr_management_server.shared.Constants.SupportedFileFormat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

@Service
public class PdfFileService<T> implements FileService<T> {
    
    private static final String HTML_TEMPLATE_PATH = "templates/pdf/table-template.html";
    private static final String CSS_TEMPLATE_PATH = "templates/pdf/table-style.css";
    
    private String htmlTemplate;
    private String cssContent;
    
    public PdfFileService() {
        try {
            this.htmlTemplate = loadResourceFile(HTML_TEMPLATE_PATH);
            this.cssContent = loadResourceFile(CSS_TEMPLATE_PATH);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PDF templates from resources", e);
        }
    }
    @Override
    public byte[] exportListToFile(List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be null or empty");
        }

        try {
            // Generate HTML content from data list
            String htmlContent = generateHtmlTable(dataList);
            
            // Convert HTML to PDF using Flying Saucer
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export data to PDF file", e);
        }
    }

    @Override
    public SupportedFileFormat getFileFormat() {
        return SupportedFileFormat.PDF;
    }

    /**
     * Generate HTML table from data list using template
     */
    private String generateHtmlTable(List<T> dataList) {
        if (dataList.isEmpty()) {
            return htmlTemplate
                    .replace("{{CSS_CONTENT}}", cssContent)
                    .replace("{{TITLE}}", "Exported Data")
                    .replace("{{TABLE_HEADERS}}", "")
                    .replace("{{TABLE_ROWS}}", "");
        }
        
        T firstItem = dataList.get(0);
        Field[] fields = firstItem.getClass().getDeclaredFields();
        
        // Generate table headers
        StringBuilder headers = new StringBuilder();
        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            // Convert camelCase to Title Case with spaces
            String headerName = fieldName.replaceAll("([A-Z])", " $1").trim();
            headerName = headerName.substring(0, 1).toUpperCase() + headerName.substring(1);
            headers.append("<th>").append(escapeHtml(headerName)).append("</th>\n");
        }
        
        // Generate table rows
        StringBuilder rows = new StringBuilder();
        for (T item : dataList) {
            rows.append("<tr>\n");
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    Object value = field.get(item);
                    String cellValue = value != null ? value.toString() : "";
                    rows.append("<td>").append(escapeHtml(cellValue)).append("</td>\n");
                } catch (IllegalAccessException e) {
                    rows.append("<td></td>\n");
                }
            }
            rows.append("</tr>\n");
        }
        
        // Replace placeholders in template
        return htmlTemplate
                .replace("{{CSS_CONTENT}}", cssContent)
                .replace("{{TITLE}}", "Exported Data")
                .replace("{{TABLE_HEADERS}}", headers.toString())
                .replace("{{TABLE_ROWS}}", rows.toString());
    }
    
    /**
     * Load resource file from classpath
     */
    private String loadResourceFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        }
    }

    /**
     * Escape HTML special characters to prevent XSS and rendering issues
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
