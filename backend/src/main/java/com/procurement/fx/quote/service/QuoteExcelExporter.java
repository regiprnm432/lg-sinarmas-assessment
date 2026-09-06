package com.procurement.fx.quote.service;

import com.procurement.fx.quote.dto.QuoteResponseDto;
import com.procurement.fx.quote.model.BudgetFlag;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Component responsible for generating styled, audit-ready Excel (.xlsx) spreadsheets
 * for supplier quote comparisons.
 */
@Component
public class QuoteExcelExporter {

    private static final Logger log = LoggerFactory.getLogger(QuoteExcelExporter.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private static final String[] HEADERS = {
            "ID",
            "Supplier Name",
            "Item Code",
            "Quote Amount",
            "Quote Currency",
            "FX Rate (to USD)",
            "Converted (USD)",
            "Budget Limit (USD)",
            "Variance (USD)",
            "Budget Status",
            "Best Value",
            "Rate Fetched At",
            "Created Date"
    };

    /**
     * Converts a list of quote responses into a styled Excel workbook byte array.
     *
     * @param quotes list of quote DTOs to export
     * @return raw byte array of the .xlsx file
     */
    public byte[] exportToExcel(List<QuoteResponseDto> quotes) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Quote Comparison");
            sheet.setDisplayGridlines(true);

            DataFormat dataFormat = workbook.createDataFormat();

            // Setup Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 10);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setBorder(headerStyle, BorderStyle.THIN);

            // Data Cell Styles
            CellStyle standardStyle = workbook.createCellStyle();
            setBorder(standardStyle, BorderStyle.THIN);

            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(centerStyle, BorderStyle.THIN);

            CellStyle currencyUsdStyle = workbook.createCellStyle();
            currencyUsdStyle.setDataFormat(dataFormat.getFormat("$#,##0.00"));
            currencyUsdStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorder(currencyUsdStyle, BorderStyle.THIN);

            CellStyle numberStyle = workbook.createCellStyle();
            numberStyle.setDataFormat(dataFormat.getFormat("#,##0.00"));
            numberStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorder(numberStyle, BorderStyle.THIN);

            CellStyle rateStyle = workbook.createCellStyle();
            rateStyle.setDataFormat(dataFormat.getFormat("0.000000"));
            rateStyle.setAlignment(HorizontalAlignment.RIGHT);
            setBorder(rateStyle, BorderStyle.THIN);

            // Status Styles
            CellStyle withinBudgetStyle = workbook.createCellStyle();
            Font greenFont = workbook.createFont();
            greenFont.setBold(true);
            greenFont.setColor(IndexedColors.GREEN.getIndex());
            withinBudgetStyle.setFont(greenFont);
            withinBudgetStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(withinBudgetStyle, BorderStyle.THIN);

            CellStyle overBudgetStyle = workbook.createCellStyle();
            Font redFont = workbook.createFont();
            redFont.setBold(true);
            redFont.setColor(IndexedColors.RED.getIndex());
            overBudgetStyle.setFont(redFont);
            overBudgetStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(overBudgetStyle, BorderStyle.THIN);

            CellStyle unknownStatusStyle = workbook.createCellStyle();
            Font orangeFont = workbook.createFont();
            orangeFont.setBold(true);
            orangeFont.setColor(IndexedColors.DARK_YELLOW.getIndex());
            unknownStatusStyle.setFont(orangeFont);
            unknownStatusStyle.setAlignment(HorizontalAlignment.CENTER);
            setBorder(unknownStatusStyle, BorderStyle.THIN);

            // Best Value Style
            CellStyle bestValueStyle = workbook.createCellStyle();
            Font goldFont = workbook.createFont();
            goldFont.setBold(true);
            goldFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            bestValueStyle.setFont(goldFont);
            bestValueStyle.setAlignment(HorizontalAlignment.CENTER);
            bestValueStyle.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
            bestValueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setBorder(bestValueStyle, BorderStyle.THIN);

            // Render Header Row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(24);
            for (int col = 0; col < HEADERS.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERS[col]);
                cell.setCellStyle(headerStyle);
            }

            // Render Data Rows
            int rowIndex = 1;
            for (QuoteResponseDto q : quotes) {
                Row row = sheet.createRow(rowIndex++);
                row.setHeightInPoints(18);

                Cell cell0 = row.createCell(0);
                cell0.setCellValue(q.getId() != null ? q.getId() : 0);
                cell0.setCellStyle(centerStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(q.getSupplierName() != null ? q.getSupplierName() : "-");
                cell1.setCellStyle(standardStyle);

                Cell cell2 = row.createCell(2);
                cell2.setCellValue(q.getItemCode() != null ? q.getItemCode() : "-");
                cell2.setCellStyle(centerStyle);

                Cell cell3 = row.createCell(3);
                if (q.getQuoteAmount() != null) {
                    cell3.setCellValue(q.getQuoteAmount().doubleValue());
                }
                cell3.setCellStyle(numberStyle);

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(q.getQuoteCurrency() != null ? q.getQuoteCurrency() : "-");
                cell4.setCellStyle(centerStyle);

                Cell cell5 = row.createCell(5);
                if (q.getRateUsed() != null) {
                    cell5.setCellValue(q.getRateUsed().doubleValue());
                }
                cell5.setCellStyle(rateStyle);

                Cell cell6 = row.createCell(6);
                if (q.getConvertedAmount() != null) {
                    cell6.setCellValue(q.getConvertedAmount().doubleValue());
                }
                cell6.setCellStyle(currencyUsdStyle);

                Cell cell7 = row.createCell(7);
                if (q.getBudgetAmount() != null) {
                    cell7.setCellValue(q.getBudgetAmount().doubleValue());
                }
                cell7.setCellStyle(currencyUsdStyle);

                Cell cell8 = row.createCell(8);
                if (q.getBudgetAmount() != null && q.getConvertedAmount() != null) {
                    BigDecimal variance = q.getBudgetAmount().subtract(q.getConvertedAmount());
                    cell8.setCellValue(variance.doubleValue());
                }
                cell8.setCellStyle(currencyUsdStyle);

                Cell cell9 = row.createCell(9);
                BudgetFlag flag = q.getBudgetFlag();
                cell9.setCellValue(flag != null ? flag.name() : "UNKNOWN");
                if (flag == BudgetFlag.WITHIN_BUDGET) {
                    cell9.setCellStyle(withinBudgetStyle);
                } else if (flag == BudgetFlag.OVER_BUDGET) {
                    cell9.setCellStyle(overBudgetStyle);
                } else {
                    cell9.setCellStyle(unknownStatusStyle);
                }

                Cell cell10 = row.createCell(10);
                if (q.isCheapest()) {
                    cell10.setCellValue("★ BEST VALUE");
                    cell10.setCellStyle(bestValueStyle);
                } else {
                    cell10.setCellValue("No");
                    cell10.setCellStyle(centerStyle);
                }

                Cell cell11 = row.createCell(11);
                cell11.setCellValue(q.getRateFetchedAt() != null ? q.getRateFetchedAt().format(DATE_FORMATTER) : "-");
                cell11.setCellStyle(centerStyle);

                Cell cell12 = row.createCell(12);
                cell12.setCellValue(q.getCreatedAt() != null ? q.getCreatedAt().format(DATE_FORMATTER) : "-");
                cell12.setCellStyle(centerStyle);
            }

            // Freeze header row
            sheet.createFreezePane(0, 1);

            // Enable auto-filter
            if (quotes.size() > 0) {
                sheet.setAutoFilter(new CellRangeAddress(0, quotes.size(), 0, HEADERS.length - 1));
            }

            // Auto-size columns
            for (int col = 0; col < HEADERS.length; col++) {
                sheet.autoSizeColumn(col);
                sheet.setColumnWidth(col, Math.min(sheet.getColumnWidth(col) + 1000, 12000));
            }

            workbook.write(out);
            log.info("Successfully exported {} quotes to Excel spreadsheet.", quotes.size());
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Failed to generate Excel spreadsheet: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel spreadsheet", e);
        }
    }

    private void setBorder(CellStyle style, BorderStyle border) {
        style.setBorderTop(border);
        style.setBorderBottom(border);
        style.setBorderLeft(border);
        style.setBorderRight(border);
    }
}
