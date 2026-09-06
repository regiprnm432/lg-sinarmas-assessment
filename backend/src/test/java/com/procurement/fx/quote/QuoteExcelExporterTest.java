package com.procurement.fx.quote;

import com.procurement.fx.quote.dto.QuoteResponseDto;
import com.procurement.fx.quote.model.BudgetFlag;
import com.procurement.fx.quote.service.QuoteExcelExporter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuoteExcelExporterTest {

    private QuoteExcelExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new QuoteExcelExporter();
    }

    @Test
    @DisplayName("Export quotes to Excel should generate valid .xlsx with correct rows and styling")
    void testExportToExcelSuccess() throws IOException {
        List<QuoteResponseDto> quotes = new ArrayList<>();

        QuoteResponseDto q1 = new QuoteResponseDto();
        q1.setId(1L);
        q1.setSupplierName("LG Display Paju");
        q1.setItemCode("OLED-PANEL-55");
        q1.setQuoteAmount(new BigDecimal("500000000"));
        q1.setQuoteCurrency("KRW");
        q1.setBaseCurrency("USD");
        q1.setBudgetAmount(new BigDecimal("400000.00"));
        q1.setConvertedAmount(new BigDecimal("370000.00"));
        q1.setBudgetFlag(BudgetFlag.WITHIN_BUDGET);
        q1.setRateUsed(new BigDecimal("0.000740"));
        q1.setRateFetchedAt(LocalDateTime.now());
        q1.setCreatedAt(LocalDateTime.now());
        q1.setCheapest(true);
        quotes.add(q1);

        QuoteResponseDto q2 = new QuoteResponseDto();
        q2.setId(2L);
        q2.setSupplierName("Samsung SDI Cheonan");
        q2.setItemCode("OLED-PANEL-55");
        q2.setQuoteAmount(new BigDecimal("600000000"));
        q2.setQuoteCurrency("KRW");
        q2.setBaseCurrency("USD");
        q2.setBudgetAmount(new BigDecimal("400000.00"));
        q2.setConvertedAmount(new BigDecimal("444000.00"));
        q2.setBudgetFlag(BudgetFlag.OVER_BUDGET);
        q2.setRateUsed(new BigDecimal("0.000740"));
        q2.setRateFetchedAt(LocalDateTime.now());
        q2.setCreatedAt(LocalDateTime.now());
        q2.setCheapest(false);
        quotes.add(q2);

        byte[] excelBytes = exporter.exportToExcel(quotes);

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0, "Excel output byte array must not be empty");

        // Validate by parsing the workbook back
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheet("Quote Comparison");
            assertNotNull(sheet, "Workbook must contain 'Quote Comparison' sheet");

            // Check header row
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow, "Header row must exist");
            assertEquals("ID", headerRow.getCell(0).getStringCellValue());
            assertEquals("Supplier Name", headerRow.getCell(1).getStringCellValue());
            assertEquals("Item Code", headerRow.getCell(2).getStringCellValue());
            assertEquals("Budget Status", headerRow.getCell(9).getStringCellValue());
            assertEquals("Best Value", headerRow.getCell(10).getStringCellValue());

            // Verify row count
            assertEquals(3, sheet.getPhysicalNumberOfRows());

            // Verify first quote
            Row row1 = sheet.getRow(1);
            assertEquals("LG Display Paju", row1.getCell(1).getStringCellValue());
            assertEquals("OLED-PANEL-55", row1.getCell(2).getStringCellValue());
            assertEquals("KRW", row1.getCell(4).getStringCellValue());
            assertEquals("WITHIN_BUDGET", row1.getCell(9).getStringCellValue());
            assertEquals("★ BEST VALUE", row1.getCell(10).getStringCellValue());

            // Verify second quote
            Row row2 = sheet.getRow(2);
            assertEquals("Samsung SDI Cheonan", row2.getCell(1).getStringCellValue());
            assertEquals("OVER_BUDGET", row2.getCell(9).getStringCellValue());
            assertEquals("No", row2.getCell(10).getStringCellValue());
        }
    }

    @Test
    @DisplayName("Export empty quotes list should generate valid sheet with only headers")
    void testExportEmptyQuotesList() throws IOException {
        byte[] excelBytes = exporter.exportToExcel(new ArrayList<>());

        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelBytes))) {
            Sheet sheet = workbook.getSheet("Quote Comparison");
            assertNotNull(sheet);
            assertEquals(1, sheet.getPhysicalNumberOfRows()); // Header only
        }
    }
}
