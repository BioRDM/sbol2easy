/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.io.IOException;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.poifs.filesystem.NotOLE2FileException;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author tzielins
 */
public class FeaturesReader {

    /**
     * Reads features to be used by transformations from excel file. First
     * column contains object id, second is the feature value. It can ignore
     * header rows and read from particular sheet
     *
     * @param file
     * @param skipRows
     * @param sheetNr
     * @return map with id, value pairs from the read rows
     */
    public Map<String, String> readSimpleFeatures(Path file, int skipRows, int sheetNr) throws IOException, ExcelFormatException {
        Map<String, String> features = new HashMap<>();
        try {
            Workbook workbook = WorkbookFactory.create(file.toFile(), null, true);
            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            Sheet sheet = workbook.getSheetAt(sheetNr);

            Map<String, List<String>> rows = readWorksheetRows(sheet);
            /*Iterator<Map.Entry<String, List<String>>> entries = rows.entrySet().iterator();
            
            while ( entries.hasNext() ) {
                Map.Entry<String, List<String>> entry = entries.next();
                List<String> colVals = entry.getValue();
                String featureVal = new String();
                System.out.println(entry.getKey());
                if(colVals.size() > 1) {
                   featureVal = String.join(",", colVals); 
                } else {
                   if(colVals.size() > 0) {
                       featureVal = colVals.get(0);
                       System.out.println(featureVal);
                   }
                }
                System.out.println(features.get(entry.getKey()));
                features.put(entry.getKey(), new String(featureVal));
                System.out.println(features.get(entry.getKey()));
            }*/
            rows.forEach((key, value) -> {
                System.out.println(key);
                System.out.println(value);
                List<String> colVals = (List<String>)value;
                String featureVal = new String();
                if(colVals.size() > 1) {
                   featureVal = String.join(",", colVals); 
                } else {
                   if(colVals.size() > 0) {
                       featureVal = colVals.get(0); 
                   }
                }
                if(colVals.size() > 0) {
                // features.put(key, featureVal);
                    features.put(key, value.get(0));
                } else {
                    features.put(key, "");
                }
            });
            
            /*BiConsumer<String, List<String>> action = (key, value) -> 
            { 
                List<String> colVals = (List<String>)value;
                String featureVal = new String();
                if(colVals.size() > 1) {
                   featureVal = String.join(",", colVals); 
                } else {
                   if(colVals.size() > 0) {
                       featureVal = colVals.get(0); 
                   }
                }
                if(colVals.size() > 0) {
                // features.put(key, featureVal);
                    features.put(key, value.get(0));
                } else {
                    features.put(key, "");
                }
            };
            
            rows.forEach(action);*/
        } catch (IllegalArgumentException | NotOLE2FileException e) {
            throw new ExcelFormatException("Not valid excel: " + e.getMessage(), e);
        }
        return features;
    }

    /**
     * Reads multiple features represented as multiple values in consecutive
     * columns. First column contains object id, the following the values. It
     * can ignore header rows and read from particular sheet
     *
     * @param file
     * @param skipRows
     * @param sheetNr
     * @return map with id and the list of read features values
     */
    public Map<String, List<String>> readMultiFeatures(Path file, int skipRows, int sheetNr) {
        throw new UnsupportedOperationException("Not supported yet.");

    }

    /**
     * Iterates through all rows in the provided worksheet and 
     *
     * @param worksheet
     * @return map with id and the list of read column values
     */
    protected Map<String, List<String>> readWorksheetRows(Sheet worksheet) {
        Map<String, List<String>> rows = new HashMap<>();

        // https://knpcode.com/java-programs/read-excel-file-java-using-apache-poi/
        Iterator<Row> rowItr = worksheet.iterator();
        while (rowItr.hasNext()) {
            Row row = rowItr.next();
            // skip header (First row)
            if (row.getRowNum() == 0) {
                continue;
            }
            Iterator<Cell> cellItr = row.cellIterator();
            List<String> colVals = new ArrayList<>();

            // Iterate each cell in a row
            while (cellItr.hasNext()) {
                Cell cell = cellItr.next();
                int index = cell.getColumnIndex();

                String cellValue = getStringValueFromCell(cell);

                if(index == 0) {
                    rows.put(cellValue, colVals);
                } else {
                    colVals.add(cellValue);
                }
            }
        }
        
        return rows;
    }

    // Utility method to get String value of cell based on cell type
    private String getStringValueFromCell(Cell cell) {
        String stringCellVal = new String();
        Object cellValue = getValueFromCell(cell);

        if (cellValue instanceof Number) {
            System.out.println("This is an Integer");
            stringCellVal = String.valueOf(cellValue);
        } else if(cellValue instanceof String) {
            System.out.println("This is a String");
            stringCellVal = (String)cellValue;
        } else if(cellValue instanceof Boolean) {
            System.out.println("This is a Boolean");
            stringCellVal = String.valueOf(cellValue);
        } else if(cellValue instanceof Date) {
            System.out.println("This is a Date");
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            stringCellVal = df.format(cellValue);
        }
        
        return stringCellVal;
    }

    // Utility method to get cell value based on cell type
    private Object getValueFromCell(Cell cell) {
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
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    /*
    * Are we expected to be able to parse both old Excel 2003 and newer Excel
    * 2007 formats? If so, check out "How to Read both Excel 2003 and 2007 format"
    * https://www.codejava.net/coding/how-to-read-excel-files-in-java-using-apache-poi
     */
}
