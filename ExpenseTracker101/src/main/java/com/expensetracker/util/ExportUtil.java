package com.expensetracker.util;

import com.expensetracker.model.Expense;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ExportUtil {

    /**
     * Export expenses to PDF format
     */
    public byte[] exportToPdf(List<Expense> expenses, Map<String, String> categoryMap,
                              LocalDate startDate, LocalDate endDate) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Add title
            com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 18, com.itextpdf.text.Font.BOLD);
            Paragraph title = new Paragraph("Expense Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Add date range
            com.itextpdf.text.Font normalFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12);
            com.itextpdf.text.Font boldFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD);
            Paragraph dateRange = new Paragraph();
            dateRange.add(new Chunk("Date Range: ", boldFont));
            dateRange.add(new Chunk(startDate.format(formatter) + " to " + endDate.format(formatter), normalFont));
            document.add(dateRange);
            document.add(Chunk.NEWLINE);

            // Add total
            BigDecimal total = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            Paragraph totalParagraph = new Paragraph();
            totalParagraph.add(new Chunk("Total Expenses: ", boldFont));
            totalParagraph.add(new Chunk("$" + total.toString(), normalFont));
            document.add(totalParagraph);
            document.add(Chunk.NEWLINE);

            // Create table
            PdfPTable table = new PdfPTable(4); // 4 columns
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 3, 2, 1});

            // Add table headers
            Stream.of("Date", "Description", "Category", "Amount")
                    .forEach(columnTitle -> {
                        PdfPCell header = new PdfPCell();
                        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                        header.setBorderWidth(2);
                        header.setPhrase(new Phrase(columnTitle, boldFont));
                        table.addCell(header);
                    });

            // Add expense data
            for (Expense expense : expenses) {
                table.addCell(expense.getDate().format(formatter));
                table.addCell(expense.getDescription());
                table.addCell(categoryMap.getOrDefault(expense.getCategoryId(), "Unknown"));
                table.addCell("$" + expense.getAmount().toString());
            }

            document.add(table);
            document.close();

        } catch (DocumentException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * Export expenses to CSV format
     */
    public byte[] exportToCsv(List<Expense> expenses, Map<String, String> categoryMap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(baos))) {
            // Add header row
            String[] header = {"Date", "Description", "Category", "Amount"};
            writer.writeNext(header);

            // Add expense data
            for (Expense expense : expenses) {
                String[] row = {
                        expense.getDate().format(formatter),
                        expense.getDescription(),
                        categoryMap.getOrDefault(expense.getCategoryId(), "Unknown"),
                        expense.getAmount().toString()
                };
                writer.writeNext(row);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    /**
     * Export expenses to Excel format
     */
    public byte[] exportToExcel(List<Expense> expenses, Map<String, String> categoryMap,
                                LocalDate startDate, LocalDate endDate) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expense Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"Date", "Description", "Category", "Amount"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add expense data
            int rowNum = 1;
            for (Expense expense : expenses) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(expense.getDate().format(formatter));
                row.createCell(1).setCellValue(expense.getDescription());
                row.createCell(2).setCellValue(categoryMap.getOrDefault(expense.getCategoryId(), "Unknown"));
                row.createCell(3).setCellValue(expense.getAmount().doubleValue());
            }

            // Add total row
            Row totalRow = sheet.createRow(rowNum + 1);
            CellStyle totalStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);

            Cell totalLabelCell = totalRow.createCell(2);
            totalLabelCell.setCellValue("Total:");
            totalLabelCell.setCellStyle(totalStyle);

            Cell totalValueCell = totalRow.createCell(3);
            BigDecimal total = expenses.stream()
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalValueCell.setCellValue(total.doubleValue());
            totalValueCell.setCellStyle(totalStyle);

            // Autosize columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }
}
