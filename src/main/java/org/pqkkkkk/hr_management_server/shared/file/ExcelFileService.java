package org.pqkkkkk.hr_management_server.shared.file;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.pqkkkkk.hr_management_server.shared.Constants.SupportedFileFormat;
import org.springframework.stereotype.Service;

@Service
public class ExcelFileService<T> implements FileService<T> {
    @Override
    public byte[] exportListToFile(List<T> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be null or empty");
        }

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Data");
            Class<?> clazz = dataList.get(0).getClass();
            Field[] fields = clazz.getDeclaredFields();
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < fields.length; i++) {
                fields[i].setAccessible(true);
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(formatFieldName(fields[i].getName()));
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            for (int rowIdx = 0; rowIdx < dataList.size(); rowIdx++) {
                Row row = sheet.createRow(rowIdx + 1);
                T data = dataList.get(rowIdx);
                
                for (int colIdx = 0; colIdx < fields.length; colIdx++) {
                    Cell cell = row.createCell(colIdx);
                    Field field = fields[colIdx];
                    field.setAccessible(true);
                    
                    try {
                        Object value = field.get(data);
                        setCellValue(cell, value);
                    } catch (IllegalAccessException e) {
                        cell.setCellValue("");
                    }
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < fields.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to export data to Excel file", e);
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
    
    private String formatFieldName(String fieldName) {
        // Convert camelCase to Title Case with spaces
        return fieldName.replaceAll("([A-Z])", " $1")
                       .trim()
                       .substring(0, 1).toUpperCase() + 
               fieldName.replaceAll("([A-Z])", " $1").trim().substring(1);
    }
    
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDate) {
            cell.setCellValue(value.toString());
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(value.toString());
        } else if (value instanceof Enum) {
            cell.setCellValue(((Enum<?>) value).name());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    @Override
    public SupportedFileFormat getFileFormat() {
        return SupportedFileFormat.EXCEL;
    }
}
