package com.qa.nal.utils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.*;

public class ExcelReader {

    public static List<String> readDescriptionsFromExcel(String filePath, String sheetName) {
        List<String> descriptions = new ArrayList<>();
        try (FileInputStream file = new FileInputStream(new File(filePath))) {
            Workbook workbook = WorkbookFactory.create(file);
            Sheet sheet = workbook.getSheet(sheetName);

            if (sheet == null) {
                throw new RuntimeException("Sheet '" + sheetName + "' not found.");
            }

            // Start from Row 1 (skip header row at index 0)
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row != null) {
                    Cell descriptionCell = row.getCell(1); // Column B (description)
                    if (descriptionCell != null) {
                        String description = descriptionCell.getCellType() == CellType.STRING
                            ? descriptionCell.getStringCellValue().trim()
                            : String.valueOf(descriptionCell.getNumericCellValue()).trim();
                        if (!description.isEmpty()) {
                            descriptions.add(description);
                        }
                    }
                }
            }
            System.out.println("descriptions loaded from Excel: " + descriptions); // Debug log
        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel: " + e.getMessage(), e);
        }
        return descriptions;
    }
}