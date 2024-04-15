package edu.ntu.fit.gsmarenaparser.services;

import edu.ntu.fit.gsmarenaparser.models.ParsedProductData;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ExcelWriterService {
    public byte[] getExcelFile(List<ParsedProductData> parsedDataList) throws IllegalArgumentException, IOException {
        if (parsedDataList == null) {
            throw new IllegalArgumentException("parsedDataList is null");
        }
        if (parsedDataList.isEmpty()) {
            throw new IllegalArgumentException("parsedDataList is empty");
        }
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writeToExcel(parsedDataList, workbook);
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
        catch (IOException e) {
            System.out.println("Couldn't get the excel file.");
            return new byte[0];
        }
    }

    private void writeToExcel(List<ParsedProductData> parsedDataList, Workbook workbook) {
        for (int i = 0; i < parsedDataList.size(); i++) {
            createPage(workbook, parsedDataList.get(i), STR."Device Specs \{i + 1}");
        }
    }

    private void createPage(Workbook workbook, ParsedProductData parsedData, String pageName) {
        CellStyle centeredStyle = workbook.createCellStyle();
        centeredStyle.setWrapText(true);
        centeredStyle.setAlignment(HorizontalAlignment.CENTER);
        centeredStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        centeredStyle.setFont(boldFont);

        CellStyle leftCenteredStyle = workbook.createCellStyle();
        leftCenteredStyle.setWrapText(true);
        leftCenteredStyle.setAlignment(HorizontalAlignment.LEFT);
        leftCenteredStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        Sheet sheet = workbook.createSheet(pageName);
        sheet.setDefaultColumnWidth(25);
        Row header1 = sheet.createRow(0);
        Row header2 = sheet.createRow(1);
        Row contentRow = sheet.createRow(2);

        Map<String, Map<String, String>> content = parsedData.getContent();
        int totalColumnCount = 0;
        for (Map.Entry<String, Map<String, String>> entry: content.entrySet()) {
            Map<String, String> block = entry.getValue();
            totalColumnCount += block.size();
        }

        List<Cell> headerCells1 = new ArrayList<>();
        List<Cell> headerCells2 = new ArrayList<>();
        List<Cell> contentRowCells = new ArrayList<>();
        for (int i = 0; i < totalColumnCount; i++) {
            Cell headerCell1 = header1.createCell(i);
            headerCell1.setCellStyle(centeredStyle);
            headerCells1.add(headerCell1);

            Cell headerCell2 = header2.createCell(i);
            headerCell2.setCellStyle(centeredStyle);
            headerCells2.add(headerCell2);

            Cell contentRowCell = contentRow.createCell(i);
            contentRowCell.setCellStyle(leftCenteredStyle);
            contentRowCells.add(contentRowCell);
        }

        int columnIndex = 0;
        for (Map.Entry<String, Map<String, String>> entry: content.entrySet()) {
            headerCells1.get(columnIndex).setCellValue(entry.getKey());
            Map<String, String> block = entry.getValue();
            if (block.size() > 1) {
                sheet.addMergedRegion(new CellRangeAddress(0, 0, columnIndex, columnIndex + block.size() - 1));
            }

            for (Map.Entry<String, String> spec: block.entrySet()) {
                headerCells2.get(columnIndex).setCellValue(spec.getKey());
                contentRowCells.get(columnIndex).setCellValue(spec.getValue());
                columnIndex++;
            }
        }
    }
}
