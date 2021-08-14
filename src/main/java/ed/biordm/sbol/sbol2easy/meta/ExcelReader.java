/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.meta;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Utility class for reading Excel files.
 * It reads Excel row as list of string (list of cell values) 
 * @author tzielins
 */
public class ExcelReader {

    /**
     * Reads given row from excel file
     * @param file 
     * @param sheetNr 0-based sheet to be read from
     * @param rowNr 0-based row number which should be read
     * @return value of that row as List of strings ("" for missing collumns)
     * @throws IOException 
     */
    public List<String> readStringRow(Path file, int sheetNr, int rowNr) throws IOException {

        try (Workbook workbook = WorkbookFactory.create(file.toFile(), null, true)) {

            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);
            
            Sheet sheet = workbook.getSheetAt(sheetNr);

            return readStringRow(sheet, rowNr, formEval);
        } catch (NotOLE2FileException e) {
            throw new IOException("Not valid excel: " + e.getMessage(), e);
        }
        
    }
    
    /**
     * Reads all rows from an Excel file as List of List of String.
     * @param file
     * @param sheetNr 0-based sheet to be read
     * @param skip how many initial rows should be skipped
     * @param cols how many collumns should be read for each row (missing columns are returned as "")
     * @return list of rows values, each rows is a list of string, no nulls
     * @throws IOException 
     */
    public List<List<String>> readStringRows(Path file, int sheetNr, int skip, int cols) throws IOException {

        try (Workbook workbook = WorkbookFactory.create(file.toFile(), null, true)) {

            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);
            
            Sheet sheet = workbook.getSheetAt(sheetNr);

            return readStringRows(sheet, skip, cols, formEval);
        } catch (NotOLE2FileException e) {
            throw new IOException("Not valid excel: " + e.getMessage(), e);
        }
        
    }  
    
    List<List<String>> readStringRows(Sheet sheet, int skip, int cols, FormulaEvaluator formEval) {
        
        List<List<String>> rows = new ArrayList<>();
        for (Row row: sheet) {
            if (row.getRowNum() < skip) continue;
            
            List<String> cells = readStringRow(row, cols, formEval);
            rows.add(cells);
        }
        return rows;
    }    
    
    List<String> readStringRow(Sheet sheet, int rowNr, FormulaEvaluator formEval) {
        
        Row row = sheet.getRow(rowNr);
        if (row == null) return new ArrayList<>();
        
        return readStringRow(row, formEval);
    }   
    
    List<String> readStringRow(Row row, FormulaEvaluator formEval) {
        
        int cols = row.getLastCellNum();
        return readStringRow(row, cols, formEval);
    }

    List<String> readStringRow(Row row, int cols, FormulaEvaluator formEval) {
        
        List<String> vals = new ArrayList<>();

        for (int cn = 0; cn < cols; cn++) {
            Cell cell = row.getCell(cn, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

            String cellValue = getStringValueFromCell(cell, formEval).trim();

            vals.add(cellValue);
        }        
        return vals;
    }    
    
    


    // Utility method to get String value of cell based on cell type
    private String getStringValueFromCell(Cell cell, FormulaEvaluator formEval) {
        Object cellValue = getValueFromCell(cell, formEval);

        if (cellValue instanceof Number) {
            return String.valueOf(cellValue);
        } else if (cellValue instanceof String) {
            return (String) cellValue;
        } else if (cellValue instanceof Boolean) {
            return String.valueOf(cellValue);
        } else if (cellValue instanceof Date) {
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            return df.format(cellValue);
        } else {
            if (cellValue != null)
                throw new IllegalArgumentException("Unsuported cell value: "+cellValue.getClass());
            else return "";
        }

    }

    // Utility method to get cell value based on cell type
    private Object getValueFromCell(Cell cell, FormulaEvaluator formEval) {

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case FORMULA:
                return formEval.evaluate(cell).getStringValue();
            case BLANK:
                return "";
            default:
                return "";
        }
    }





}
