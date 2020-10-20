/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author jhay
 */
public class FeaturesReaderTest {

    FeaturesReader featuresReader = new FeaturesReader();

    /**
     * Test of readSimpleFeatures method, of class FeaturesReader.
     */
    @Test
    public void testReadSimpleFeatures() throws Exception {
        File file = new File(getClass().getResource("flank-list.xlsx").getFile());

        String expKey = "0684_slr2015_left";
        String expValue = "TGCGATCAACAAATCTTTCGATATAGACCGAAAAAAAGAAATCTCACTGGCCAACTGTTTAAGTTGTTTTGTTGACGGCTTTTGTGGTGCCATGACAATATATCCAAACAATAACTAAATTCTCCCCTAGACCCTCTCAAACCCCAGTTTCAGGGAAAATTATAGAAACAAGTAAACTGTTTTGGTGTTTATTGTCACAGGTTTTGGTGAGCTGGCTAACAATCATTTGTTGTAAATTAACTTATCATCTAGGTGATGTTAGTTGTCACCCATCCTATGACAATATGTGTTTAATTTGACTAGCAATAGTCCAGAGATGTTCTCTAAAACTTCGTTTTTAGGCCCATACTTCACCTTCCTACAATCCCCCTCCCCCCCATCTATTCCCTGTGACTTGAACCCCTTTGTTTGCCTTTAAGAGAAATTTAAAATCCCTTTATTTGAGCTTTAAGCACTTAGCTATTAACCTGAAATTTGACCCAGAAAAACCAATAAAAGACTTCCCCACGGTCTTTGCTGACTTCCCAAAAACCCCAATCGACC";

        // Test worksheet 1 (Left flank)
        Map<String, String> result = featuresReader.readSimpleFeatures(file.toPath(), 0, 0);
        assertEquals(expValue, result.get(expKey));

        expKey = "0002_slr0612_left";
        expValue = "AACATTTGGGGTTAGCGTTCCAGATTGTGGACGATATTTTAGATTTCACTTCCCCCACGGAGGTTTTGGGGAAACCGGCCGGGTCAGATTTAATCAGCGGCAACATCACCGCCCCAGCCCTATTTGCCATGGAAAAATATCCCCTACTTGGTAAATTAATTGAACGGGAATTTGCCCAGGCGGGGGATTTGGAACAGGCCCTGGAATTGGTAGAACAGGGGGATGGTATCCGGCGATCAAGGGAATTGGCCGCGAACCAAGCGCAACTGGCCCGGCAACATCTGAGTGTGCTGGAAATGTCCGCTCCGAGAGAATCTCTGTTGGAATTAGTTGATTATGTGCTTGGTCGTCTCCATTAGGTTTTCCCGTAGATTTTTTCCCAGCGGGCTTGATTGCGTTGAATAAAACTCCCCAAACCATTGTTTTTTACAAACCCTACGGAGTTCTGTGTCAATTTACCGATAATTCTGCCCATCCCCGGCCGACGTTGAAGGATTATATTAATTTGCCAGATTTATATCCC";

        result = featuresReader.readSimpleFeatures(file.toPath(), 1, 0);
        assertEquals(expValue, result.get(expKey));

        // Skip the first 10 rows which includes this key
        expKey = "0009_slr1312_left";
        expValue = "AACCTACAACATCGTTGCCGCCCACGGCTACTTTGGTCGGTTGATCTTCCAATATGCTTCTTTCAACAACAGCCGTTCCTTGCACTTCTTCTTGGGTGCTTGGCCTGTAATCGGCATCTGGTTCACTGCTATGGGTGTAAGCACCATGGCGTTCAACCTGAACGGTTTCAACTTCAACCAGTCCATCTTGGATAGCCAAGGCCGGGTAATCGGCACCTGGGCTGATGTATTGAACCGAGCCAACATCGGTTTTGAAGTAATGCACGAACGCAATGCCCACAACTTCCCCCTCGACTTAGCGTCTGGGGAGCAAGCTCCTGTGGCTTTGACCGCTCCTGCTGTCAACGGTTAATTCCTTGGTGTAATGCCAACTGAATAATCTGCAAATTGCACTCTCCTTCAATGGGGGGTGCTTTTTGCTTGACTGAGTAATCTTCTGATTGCTGATCTTGATTGCCATCGATCGCCGGGGAGTCCGGGGCAGTTACCATTAGAGAGTCTAGAGAATTAATCCATCTTCGATAGAGGAATT";
        result = featuresReader.readSimpleFeatures(file.toPath(), 10, 0);
        assertEquals(null, result.get(expKey));

        // Repeat the tests above using the second (right flank) worksheet
        expKey = "1716_slr1288_right";
        expValue = "AAGGGTAATTAGTGAAACTAAATTAGGTTGAGTTTGATTCCCCTTAGTGCGTTATCTGGAAGAATTTCAACTCGATTAACGTCTCTGTTCGGAGGCTAGCCCCATTGCCCGATCGCCGAAGGATGGGGTTATAATGGCAGGGAAGTAACTACCGCGTTGGTGACTGCCAACAACGTTTTTTGATCTATGCTTGAAAAATAAGGGTGTCTCTGTGCATTCTGCGTGGTAGTAGACCGGGCTCGTACCCGGAAACGAAAACGAGAATTGATGACTCCCCCCTGGTTACACCAGGTCATAAACTGAGGTAAAACGGCGTGGTGGTGCTTCTCCTCTGCTTCTCAGGTTTATCCCTCCCCCCAGAACGGGTTTGAGCTGATTGCTCCCGATTTTTTCCGTCATTCTACATAGTTACCCCAACGCCCTAAGCCAGGGATTCCGGCAGTACTTCTACCTGTTCCTCGGTATCGATTTCTTCGTTCTCAGTTTTTCCAGCCCGCATATCATCAATCACTTTCTTGATCACACTGGCGATCGGTA";

        result = featuresReader.readSimpleFeatures(file.toPath(), 0, 1);
        assertEquals(expValue, result.get(expKey));

        expKey = "3204_ssr7036_right";
        expValue = "TATCTATCTTATCGTTAATCCGTCGATATTCAAGGGGTACAGGCAAATTTAGTGTCGGCAACATCAACGGAACTCCCTCCCTGCCTGAAATTTGGAAGCAAAAACCCTTCTGTTTAGGGACATATACAACACGTCAGCTCAATGATGCAGGGGGGGTGGAGTAATGAAAGTTAGACCCCAGCACTGGCAAGAATGGGTGAATAGCGGCATCGCCCCTGAGATAATCGAAGCTAACCTGAAGAGCCTAGATAACGCCTACGGATGGTTACTGTATTCGGAGAAACTACCCCGGCGTAACGATGGACGATTAAGTCAAGGAATACTGACCCAATACCGTCACCTTGAACAGGGAGGCTGGTATTGCTCTGGATTAGATGCCATAACTCTAGAAGATTCCCAATGGGGCTGTTTTAAACCAGATCAGCCTCGACTCTCCCAAGAAGGGAAGCTAATTAAATATGAACACCCCCCCAAGACCTCAACAGAAGCATTTTGCTTAAAAATCACCCGTCATCAATGGCGACAA";

        // Test worksheet 2 (Right flank)
        result = featuresReader.readSimpleFeatures(file.toPath(), 1, 1);
        assertEquals(expValue, result.get(expKey));

        // Skip the first 10 rows which includes this key
        expKey = "0009_slr1312_right";
        expValue = "TCTGGTAGAAAAAAACAATGGTTGGATTCTAAGCTCTGGAGTGGACTGGGATAGCGCTGAAATTGGATTAATTTTGCTTACTATCCCATCGCAATTTTGCTTAAACTCGACTATTTTTATTTGTTTTGATTGCAGAATCAATTTGGTTTCCTACGGCTCCAACAATTGTAAAAACTCCGCCTCAGATAGTAACTTGATGCCCAAACTTTCCGCCTTGGCGGCCTTACTTCCTGGTTTATCTCCCAAGAGAACATAATCAGTTTTGGTACTTACACTGCTGGTTACTTTACCGCCGCTCTGTTCAATTAATTCCTGAGCTTCTAGGCGACTAAGGTTGGGCAAAGTCCCGGTTAACACAAAGGTTTTGCCCTTTAACTTGCCGCTGTCAGTTTTGGTCTGGTCAATCCCTTGGTTTGCCAATACTAAACCCAACTCCTCTAAATCTTGAATTAATTGTTGATTACCGGGGTTCCTGAACCAATTAACCACGGCTTCGGCAATTTCTGGGCCAATGCCATAGACTCCT";
        result = featuresReader.readSimpleFeatures(file.toPath(), 10, 1);
        assertEquals(null, result.get(expKey));

    }

    /**
     * Test of readMultiFeatures method, of class FeaturesReader.
     */
    @Test
    public void testReadMultiFeatures() throws Exception {
        File file = new File(getClass().getResource("flank-list.xlsx").getFile());

        String expKey = "0684_slr2015_left";
        List<String> expValue = Arrays.asList("TGCGATCAACAAATCTTTCGATATAGACCGAAAAAAAGAAATCTCACTGGCCAACTGTTTAAGTTGTTTTGTTGACGGCTTTTGTGGTGCCATGACAATATATCCAAACAATAACTAAATTCTCCCCTAGACCCTCTCAAACCCCAGTTTCAGGGAAAATTATAGAAACAAGTAAACTGTTTTGGTGTTTATTGTCACAGGTTTTGGTGAGCTGGCTAACAATCATTTGTTGTAAATTAACTTATCATCTAGGTGATGTTAGTTGTCACCCATCCTATGACAATATGTGTTTAATTTGACTAGCAATAGTCCAGAGATGTTCTCTAAAACTTCGTTTTTAGGCCCATACTTCACCTTCCTACAATCCCCCTCCCCCCCATCTATTCCCTGTGACTTGAACCCCTTTGTTTGCCTTTAAGAGAAATTTAAAATCCCTTTATTTGAGCTTTAAGCACTTAGCTATTAACCTGAAATTTGACCCAGAAAAACCAATAAAAGACTTCCCCACGGTCTTTGCTGACTTCCCAAAAACCCCAATCGACC");

        // Test worksheet 1 (Left flank)
        Map<String, List<String>> result = featuresReader.readMultiFeatures(file.toPath(), 0, 0);
        assertEquals(expValue, result.get(expKey));

        expKey = "0002_slr0612_left";
        expValue = Arrays.asList("AACATTTGGGGTTAGCGTTCCAGATTGTGGACGATATTTTAGATTTCACTTCCCCCACGGAGGTTTTGGGGAAACCGGCCGGGTCAGATTTAATCAGCGGCAACATCACCGCCCCAGCCCTATTTGCCATGGAAAAATATCCCCTACTTGGTAAATTAATTGAACGGGAATTTGCCCAGGCGGGGGATTTGGAACAGGCCCTGGAATTGGTAGAACAGGGGGATGGTATCCGGCGATCAAGGGAATTGGCCGCGAACCAAGCGCAACTGGCCCGGCAACATCTGAGTGTGCTGGAAATGTCCGCTCCGAGAGAATCTCTGTTGGAATTAGTTGATTATGTGCTTGGTCGTCTCCATTAGGTTTTCCCGTAGATTTTTTCCCAGCGGGCTTGATTGCGTTGAATAAAACTCCCCAAACCATTGTTTTTTACAAACCCTACGGAGTTCTGTGTCAATTTACCGATAATTCTGCCCATCCCCGGCCGACGTTGAAGGATTATATTAATTTGCCAGATTTATATCCC");

        result = featuresReader.readMultiFeatures(file.toPath(), 1, 0);
        assertEquals(expValue, result.get(expKey));

        // Skip the first 10 rows which includes this key
        expKey = "0009_slr1312_left";
        expValue = Arrays.asList("AACCTACAACATCGTTGCCGCCCACGGCTACTTTGGTCGGTTGATCTTCCAATATGCTTCTTTCAACAACAGCCGTTCCTTGCACTTCTTCTTGGGTGCTTGGCCTGTAATCGGCATCTGGTTCACTGCTATGGGTGTAAGCACCATGGCGTTCAACCTGAACGGTTTCAACTTCAACCAGTCCATCTTGGATAGCCAAGGCCGGGTAATCGGCACCTGGGCTGATGTATTGAACCGAGCCAACATCGGTTTTGAAGTAATGCACGAACGCAATGCCCACAACTTCCCCCTCGACTTAGCGTCTGGGGAGCAAGCTCCTGTGGCTTTGACCGCTCCTGCTGTCAACGGTTAATTCCTTGGTGTAATGCCAACTGAATAATCTGCAAATTGCACTCTCCTTCAATGGGGGGTGCTTTTTGCTTGACTGAGTAATCTTCTGATTGCTGATCTTGATTGCCATCGATCGCCGGGGAGTCCGGGGCAGTTACCATTAGAGAGTCTAGAGAATTAATCCATCTTCGATAGAGGAATT");
        result = featuresReader.readMultiFeatures(file.toPath(), 10, 0);
        assertEquals(null, result.get(expKey));

        // Repeat the tests above using the second (right flank) worksheet
        expKey = "1716_slr1288_right";
        expValue = Arrays.asList("AAGGGTAATTAGTGAAACTAAATTAGGTTGAGTTTGATTCCCCTTAGTGCGTTATCTGGAAGAATTTCAACTCGATTAACGTCTCTGTTCGGAGGCTAGCCCCATTGCCCGATCGCCGAAGGATGGGGTTATAATGGCAGGGAAGTAACTACCGCGTTGGTGACTGCCAACAACGTTTTTTGATCTATGCTTGAAAAATAAGGGTGTCTCTGTGCATTCTGCGTGGTAGTAGACCGGGCTCGTACCCGGAAACGAAAACGAGAATTGATGACTCCCCCCTGGTTACACCAGGTCATAAACTGAGGTAAAACGGCGTGGTGGTGCTTCTCCTCTGCTTCTCAGGTTTATCCCTCCCCCCAGAACGGGTTTGAGCTGATTGCTCCCGATTTTTTCCGTCATTCTACATAGTTACCCCAACGCCCTAAGCCAGGGATTCCGGCAGTACTTCTACCTGTTCCTCGGTATCGATTTCTTCGTTCTCAGTTTTTCCAGCCCGCATATCATCAATCACTTTCTTGATCACACTGGCGATCGGTA");

        // Test worksheet 2 (Right flank)
        result = featuresReader.readMultiFeatures(file.toPath(), 0, 1);
        assertEquals(expValue, result.get(expKey));

        expKey = "3204_ssr7036_right";
        expValue = Arrays.asList("TATCTATCTTATCGTTAATCCGTCGATATTCAAGGGGTACAGGCAAATTTAGTGTCGGCAACATCAACGGAACTCCCTCCCTGCCTGAAATTTGGAAGCAAAAACCCTTCTGTTTAGGGACATATACAACACGTCAGCTCAATGATGCAGGGGGGGTGGAGTAATGAAAGTTAGACCCCAGCACTGGCAAGAATGGGTGAATAGCGGCATCGCCCCTGAGATAATCGAAGCTAACCTGAAGAGCCTAGATAACGCCTACGGATGGTTACTGTATTCGGAGAAACTACCCCGGCGTAACGATGGACGATTAAGTCAAGGAATACTGACCCAATACCGTCACCTTGAACAGGGAGGCTGGTATTGCTCTGGATTAGATGCCATAACTCTAGAAGATTCCCAATGGGGCTGTTTTAAACCAGATCAGCCTCGACTCTCCCAAGAAGGGAAGCTAATTAAATATGAACACCCCCCCAAGACCTCAACAGAAGCATTTTGCTTAAAAATCACCCGTCATCAATGGCGACAA");

        result = featuresReader.readMultiFeatures(file.toPath(), 1, 1);
        assertEquals(expValue, result.get(expKey));

        // Skip the first 10 rows which includes this key
        expKey = "0009_slr1312_right";
        expValue = Arrays.asList("TCTGGTAGAAAAAAACAATGGTTGGATTCTAAGCTCTGGAGTGGACTGGGATAGCGCTGAAATTGGATTAATTTTGCTTACTATCCCATCGCAATTTTGCTTAAACTCGACTATTTTTATTTGTTTTGATTGCAGAATCAATTTGGTTTCCTACGGCTCCAACAATTGTAAAAACTCCGCCTCAGATAGTAACTTGATGCCCAAACTTTCCGCCTTGGCGGCCTTACTTCCTGGTTTATCTCCCAAGAGAACATAATCAGTTTTGGTACTTACACTGCTGGTTACTTTACCGCCGCTCTGTTCAATTAATTCCTGAGCTTCTAGGCGACTAAGGTTGGGCAAAGTCCCGGTTAACACAAAGGTTTTGCCCTTTAACTTGCCGCTGTCAGTTTTGGTCTGGTCAATCCCTTGGTTTGCCAATACTAAACCCAACTCCTCTAAATCTTGAATTAATTGTTGATTACCGGGGTTCCTGAACCAATTAACCACGGCTTCGGCAATTTCTGGGCCAATGCCATAGACTCCT");
        result = featuresReader.readMultiFeatures(file.toPath(), 10, 1);
        assertEquals(null, result.get(expKey));
    }

    @Test
    public void readMultiFeaturesReadsListOfValuesPreservingBlanks() throws Exception {
        File file = new File(getClass().getResource("flank-list.xlsx").getFile());

        // Test worksheet 1 (Right flank)
        Map<String, List<String>> result = featuresReader.readMultiFeatures(file.toPath(), 0, 1);
        String expKey = "name right";
        List<String> expValue = List.of(" Right flank sequence");

        assertEquals(expValue, result.get(expKey));

        expKey = "0001_slr0611_right";
        expValue = List.of("", "Tomek");
        assertEquals(expValue, result.get(expKey));

        expKey = "0002_slr0612_right";
        expValue = List.of("TCAAGGCTCCCTCCCCCCAGGGCATTAAAATAGGAACAGTTGCCGAACTCCCTATCAAGCCGAATCATTAATCATCCCGTTTATGTCCTATCTAATCGCTGTGGTAGCCAACCGCATTGCCGCCGAAGAAGCTTATACAACCTTGGAACAGGCAGGATTTGCCCAAAAGAATTTGACTATCATTGGCACAGGTTATAAAACCGCTGACGAATTTGGCTTGGTGGACCCGAAAAAACAAGCTATCAAAAGGGCAAAGCTCATGGCCATCTGGTTAGTACCCTTTGGTTTCGCTGCCGGTTATTGCTTTAACCTCATCACTGGCTTGAGCACCTTA");
        assertEquals(expValue, result.get(expKey));

        expKey = "0003_slr0613_right";
        expValue = List.of("ACTCCATCTTGAGCGGTAATGACTTCCCCGAAGAAGTTATAAACTTGTAGGCAATTCGGGCAGACCGAAGGGCTTACCAACCGTATTGGGACCAAACTGGGTGCCTGTAAAGGGATTCTGCTACCCTTACCCCCCGCAATTGGTTTAACAGGGGTAAATGACCCTGGGGTGCGGATAAATCCCAGGTAAAGCCCTTGGGCCAACGGGTCCAAACATTGCCGCTTTTCCAGCCAATTTTCGGCCAAAGCTTGGTAAATTCTTTTCCGGACGCCAACCAGAGTCGTCGTTGCACCGAAAAACCAAAATTACCGTTGGAGTGGAGCCACCACAAAGCATTAATGGTGTGCAGGTCTAGGGCAGGAAATTTTTCTACTTCTGTGAAATAGAGCCATTGTCTTTGACTGGCCCCAGGCCCCGCCAGTTCGCACAATTTATCCCGGGTTATTTCATCCGCTGTTTCAAAATCCTGGCTTCCTAGGGCTTCCTGGAGCGGCAGATAATCAATGCCCTGGGCCGATTGTAAGGGGAAAATGCCTGTGGGGTAA",
                "Second Fature");
        assertEquals(expValue, result.get(expKey));

    }

    @Test
    public void readSimpleFeaturesReadsFirstTwoColumnsPreservingBlanks() throws Exception {
        File file = new File(getClass().getResource("flank-list.xlsx").getFile());

        // Test worksheet 1 (Right flank)
        Map<String, String> result = featuresReader.readSimpleFeatures(file.toPath(), 0, 1);
        String expKey = "name right";
        String expValue = " Right flank sequence";

        assertEquals(expValue, result.get(expKey));

        expKey = "0001_slr0611_right";
        expValue = ",Tomek";
        assertEquals(expValue, result.get(expKey));

        expKey = "0002_slr0612_right";
        expValue = "TCAAGGCTCCCTCCCCCCAGGGCATTAAAATAGGAACAGTTGCCGAACTCCCTATCAAGCCGAATCATTAATCATCCCGTTTATGTCCTATCTAATCGCTGTGGTAGCCAACCGCATTGCCGCCGAAGAAGCTTATACAACCTTGGAACAGGCAGGATTTGCCCAAAAGAATTTGACTATCATTGGCACAGGTTATAAAACCGCTGACGAATTTGGCTTGGTGGACCCGAAAAAACAAGCTATCAAAAGGGCAAAGCTCATGGCCATCTGGTTAGTACCCTTTGGTTTCGCTGCCGGTTATTGCTTTAACCTCATCACTGGCTTGAGCACCTTA";
        assertEquals(expValue, result.get(expKey));

        expKey = "0003_slr0613_right";
        expValue = "ACTCCATCTTGAGCGGTAATGACTTCCCCGAAGAAGTTATAAACTTGTAGGCAATTCGGGCAGACCGAAGGGCTTACCAACCGTATTGGGACCAAACTGGGTGCCTGTAAAGGGATTCTGCTACCCTTACCCCCCGCAATTGGTTTAACAGGGGTAAATGACCCTGGGGTGCGGATAAATCCCAGGTAAAGCCCTTGGGCCAACGGGTCCAAACATTGCCGCTTTTCCAGCCAATTTTCGGCCAAAGCTTGGTAAATTCTTTTCCGGACGCCAACCAGAGTCGTCGTTGCACCGAAAAACCAAAATTACCGTTGGAGTGGAGCCACCACAAAGCATTAATGGTGTGCAGGTCTAGGGCAGGAAATTTTTCTACTTCTGTGAAATAGAGCCATTGTCTTTGACTGGCCCCAGGCCCCGCCAGTTCGCACAATTTATCCCGGGTTATTTCATCCGCTGTTTCAAAATCCTGGCTTCCTAGGGCTTCCTGGAGCGGCAGATAATCAATGCCCTGGGCCGATTGTAAGGGGAAAATGCCTGTGGGGTAA," +
                "Second Fature";

        assertEquals(expValue, result.get(expKey));

    }

    /**
     * Test of readWorksheetRows method, of class FeaturesReader.
     */
    @Test
    public void testReadWorksheetRows() throws Exception {
        File file = new File(getClass().getResource("flank-list.xlsx").getFile());
        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            // Test worksheet 1 (Left flank)
            Sheet sheet = workbook.getSheetAt(0);

            Map<String, List<String>> rows = featuresReader.readWorksheetRows(sheet, 0);

            String expKey = "0684_slr2015_left";
            List<String> expValue = Arrays.asList("TGCGATCAACAAATCTTTCGATATAGACCGAAAAAAAGAAATCTCACTGGCCAACTGTTTAAGTTGTTTTGTTGACGGCTTTTGTGGTGCCATGACAATATATCCAAACAATAACTAAATTCTCCCCTAGACCCTCTCAAACCCCAGTTTCAGGGAAAATTATAGAAACAAGTAAACTGTTTTGGTGTTTATTGTCACAGGTTTTGGTGAGCTGGCTAACAATCATTTGTTGTAAATTAACTTATCATCTAGGTGATGTTAGTTGTCACCCATCCTATGACAATATGTGTTTAATTTGACTAGCAATAGTCCAGAGATGTTCTCTAAAACTTCGTTTTTAGGCCCATACTTCACCTTCCTACAATCCCCCTCCCCCCCATCTATTCCCTGTGACTTGAACCCCTTTGTTTGCCTTTAAGAGAAATTTAAAATCCCTTTATTTGAGCTTTAAGCACTTAGCTATTAACCTGAAATTTGACCCAGAAAAACCAATAAAAGACTTCCCCACGGTCTTTGCTGACTTCCCAAAAACCCCAATCGACC");

            assertEquals(expValue, rows.get(expKey));

            expKey = "0002_slr0612_left";
            expValue = Arrays.asList("AACATTTGGGGTTAGCGTTCCAGATTGTGGACGATATTTTAGATTTCACTTCCCCCACGGAGGTTTTGGGGAAACCGGCCGGGTCAGATTTAATCAGCGGCAACATCACCGCCCCAGCCCTATTTGCCATGGAAAAATATCCCCTACTTGGTAAATTAATTGAACGGGAATTTGCCCAGGCGGGGGATTTGGAACAGGCCCTGGAATTGGTAGAACAGGGGGATGGTATCCGGCGATCAAGGGAATTGGCCGCGAACCAAGCGCAACTGGCCCGGCAACATCTGAGTGTGCTGGAAATGTCCGCTCCGAGAGAATCTCTGTTGGAATTAGTTGATTATGTGCTTGGTCGTCTCCATTAGGTTTTCCCGTAGATTTTTTCCCAGCGGGCTTGATTGCGTTGAATAAAACTCCCCAAACCATTGTTTTTTACAAACCCTACGGAGTTCTGTGTCAATTTACCGATAATTCTGCCCATCCCCGGCCGACGTTGAAGGATTATATTAATTTGCCAGATTTATATCCC");

            rows = featuresReader.readWorksheetRows(sheet, 1);
            assertEquals(expValue, rows.get(expKey));

            // Skip the first 10 rows which includes this key
            expKey = "0009_slr1312_left";
            expValue = Arrays.asList("AACCTACAACATCGTTGCCGCCCACGGCTACTTTGGTCGGTTGATCTTCCAATATGCTTCTTTCAACAACAGCCGTTCCTTGCACTTCTTCTTGGGTGCTTGGCCTGTAATCGGCATCTGGTTCACTGCTATGGGTGTAAGCACCATGGCGTTCAACCTGAACGGTTTCAACTTCAACCAGTCCATCTTGGATAGCCAAGGCCGGGTAATCGGCACCTGGGCTGATGTATTGAACCGAGCCAACATCGGTTTTGAAGTAATGCACGAACGCAATGCCCACAACTTCCCCCTCGACTTAGCGTCTGGGGAGCAAGCTCCTGTGGCTTTGACCGCTCCTGCTGTCAACGGTTAATTCCTTGGTGTAATGCCAACTGAATAATCTGCAAATTGCACTCTCCTTCAATGGGGGGTGCTTTTTGCTTGACTGAGTAATCTTCTGATTGCTGATCTTGATTGCCATCGATCGCCGGGGAGTCCGGGGCAGTTACCATTAGAGAGTCTAGAGAATTAATCCATCTTCGATAGAGGAATT");

            // Skip the first 10 rows which includes this key
            rows = featuresReader.readWorksheetRows(sheet, 10);
            assertEquals(null, rows.get(expKey));

            // Repeat the tests above using the second (right flank) worksheet
            sheet = workbook.getSheetAt(1);
            rows = featuresReader.readWorksheetRows(sheet, 0);

            expKey = "1716_slr1288_right";
            expValue = Arrays.asList("AAGGGTAATTAGTGAAACTAAATTAGGTTGAGTTTGATTCCCCTTAGTGCGTTATCTGGAAGAATTTCAACTCGATTAACGTCTCTGTTCGGAGGCTAGCCCCATTGCCCGATCGCCGAAGGATGGGGTTATAATGGCAGGGAAGTAACTACCGCGTTGGTGACTGCCAACAACGTTTTTTGATCTATGCTTGAAAAATAAGGGTGTCTCTGTGCATTCTGCGTGGTAGTAGACCGGGCTCGTACCCGGAAACGAAAACGAGAATTGATGACTCCCCCCTGGTTACACCAGGTCATAAACTGAGGTAAAACGGCGTGGTGGTGCTTCTCCTCTGCTTCTCAGGTTTATCCCTCCCCCCAGAACGGGTTTGAGCTGATTGCTCCCGATTTTTTCCGTCATTCTACATAGTTACCCCAACGCCCTAAGCCAGGGATTCCGGCAGTACTTCTACCTGTTCCTCGGTATCGATTTCTTCGTTCTCAGTTTTTCCAGCCCGCATATCATCAATCACTTTCTTGATCACACTGGCGATCGGTA");

            assertEquals(expValue, rows.get(expKey));

            expKey = "3204_ssr7036_right";
            expValue = Arrays.asList("TATCTATCTTATCGTTAATCCGTCGATATTCAAGGGGTACAGGCAAATTTAGTGTCGGCAACATCAACGGAACTCCCTCCCTGCCTGAAATTTGGAAGCAAAAACCCTTCTGTTTAGGGACATATACAACACGTCAGCTCAATGATGCAGGGGGGGTGGAGTAATGAAAGTTAGACCCCAGCACTGGCAAGAATGGGTGAATAGCGGCATCGCCCCTGAGATAATCGAAGCTAACCTGAAGAGCCTAGATAACGCCTACGGATGGTTACTGTATTCGGAGAAACTACCCCGGCGTAACGATGGACGATTAAGTCAAGGAATACTGACCCAATACCGTCACCTTGAACAGGGAGGCTGGTATTGCTCTGGATTAGATGCCATAACTCTAGAAGATTCCCAATGGGGCTGTTTTAAACCAGATCAGCCTCGACTCTCCCAAGAAGGGAAGCTAATTAAATATGAACACCCCCCCAAGACCTCAACAGAAGCATTTTGCTTAAAAATCACCCGTCATCAATGGCGACAA");

            rows = featuresReader.readWorksheetRows(sheet, 1);
            assertEquals(expValue, rows.get(expKey));

            // Skip the first 10 rows which includes this key
            expKey = "0009_slr1312_right";
            expValue = Arrays.asList("TCTGGTAGAAAAAAACAATGGTTGGATTCTAAGCTCTGGAGTGGACTGGGATAGCGCTGAAATTGGATTAATTTTGCTTACTATCCCATCGCAATTTTGCTTAAACTCGACTATTTTTATTTGTTTTGATTGCAGAATCAATTTGGTTTCCTACGGCTCCAACAATTGTAAAAACTCCGCCTCAGATAGTAACTTGATGCCCAAACTTTCCGCCTTGGCGGCCTTACTTCCTGGTTTATCTCCCAAGAGAACATAATCAGTTTTGGTACTTACACTGCTGGTTACTTTACCGCCGCTCTGTTCAATTAATTCCTGAGCTTCTAGGCGACTAAGGTTGGGCAAAGTCCCGGTTAACACAAAGGTTTTGCCCTTTAACTTGCCGCTGTCAGTTTTGGTCTGGTCAATCCCTTGGTTTGCCAATACTAAACCCAACTCCTCTAAATCTTGAATTAATTGTTGATTACCGGGGTTCCTGAACCAATTAACCACGGCTTCGGCAATTTCTGGGCCAATGCCATAGACTCCT");
            rows = featuresReader.readWorksheetRows(sheet, 10);
            assertEquals(null, rows.get(expKey));
        }
    }

    /**
     * Test of getStringValueFromCell method, of class FeaturesReader.
     */
    @Test
    public void testGetStringValueFromCell() throws Exception {
        File file = new File(getClass().getResource("flank-list.xlsx").getFile());
        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            // Use reflection to access the private method
            Method privateMethod = FeaturesReader.class.getDeclaredMethod("getStringValueFromCell", Cell.class);
            privateMethod.setAccessible(true);

            // Test worksheet 1 (Left flank)
            Sheet sheet = workbook.getSheetAt(0);

            // Test row 684
            Row row = sheet.getRow(684);

            String expKey = "0684_slr2015_left";
            String expValue = "TGCGATCAACAAATCTTTCGATATAGACCGAAAAAAAGAAATCTCACTGGCCAACTGTTTAAGTTGTTTTGTTGACGGCTTTTGTGGTGCCATGACAATATATCCAAACAATAACTAAATTCTCCCCTAGACCCTCTCAAACCCCAGTTTCAGGGAAAATTATAGAAACAAGTAAACTGTTTTGGTGTTTATTGTCACAGGTTTTGGTGAGCTGGCTAACAATCATTTGTTGTAAATTAACTTATCATCTAGGTGATGTTAGTTGTCACCCATCCTATGACAATATGTGTTTAATTTGACTAGCAATAGTCCAGAGATGTTCTCTAAAACTTCGTTTTTAGGCCCATACTTCACCTTCCTACAATCCCCCTCCCCCCCATCTATTCCCTGTGACTTGAACCCCTTTGTTTGCCTTTAAGAGAAATTTAAAATCCCTTTATTTGAGCTTTAAGCACTTAGCTATTAACCTGAAATTTGACCCAGAAAAACCAATAAAAGACTTCCCCACGGTCTTTGCTGACTTCCCAAAAACCCCAATCGACC";

            String resKey = (String) privateMethod.invoke(featuresReader, row.getCell(0));
            String resValue = (String) privateMethod.invoke(featuresReader, row.getCell(1));

            assertEquals(resKey, expKey);
            assertEquals(resValue, expValue);

            // Test worksheet 2 (Right flank)
            sheet = workbook.getSheetAt(1);

            // Test row 1716
            row = sheet.getRow(1716);

            expKey = "1716_slr1288_right";
            expValue = "AAGGGTAATTAGTGAAACTAAATTAGGTTGAGTTTGATTCCCCTTAGTGCGTTATCTGGAAGAATTTCAACTCGATTAACGTCTCTGTTCGGAGGCTAGCCCCATTGCCCGATCGCCGAAGGATGGGGTTATAATGGCAGGGAAGTAACTACCGCGTTGGTGACTGCCAACAACGTTTTTTGATCTATGCTTGAAAAATAAGGGTGTCTCTGTGCATTCTGCGTGGTAGTAGACCGGGCTCGTACCCGGAAACGAAAACGAGAATTGATGACTCCCCCCTGGTTACACCAGGTCATAAACTGAGGTAAAACGGCGTGGTGGTGCTTCTCCTCTGCTTCTCAGGTTTATCCCTCCCCCCAGAACGGGTTTGAGCTGATTGCTCCCGATTTTTTCCGTCATTCTACATAGTTACCCCAACGCCCTAAGCCAGGGATTCCGGCAGTACTTCTACCTGTTCCTCGGTATCGATTTCTTCGTTCTCAGTTTTTCCAGCCCGCATATCATCAATCACTTTCTTGATCACACTGGCGATCGGTA";

            resKey = (String) privateMethod.invoke(featuresReader, row.getCell(0));
            resValue = (String) privateMethod.invoke(featuresReader, row.getCell(1));

            assertEquals(resKey, expKey);
            assertEquals(resValue, expValue);
        }
    }

    /**
     * Test of getValueFromCell method, of class FeaturesReader.
     */
    @Test
    public void testGetValueFromCell() throws Exception {
        File file = new File(getClass().getResource("flank-list.xlsx").getFile());
        try (Workbook workbook = WorkbookFactory.create(file, null, true)) {
            FormulaEvaluator formEval = workbook.getCreationHelper().createFormulaEvaluator();
            formEval.setIgnoreMissingWorkbooks(true);

            // Use reflection to access the private method
            Method privateMethod = FeaturesReader.class.getDeclaredMethod("getValueFromCell", Cell.class);
            privateMethod.setAccessible(true);

            // Test worksheet 1 (Left flank)
            Sheet sheet = workbook.getSheetAt(0);

            Object expKey = "0684_slr2015_left";
            Object expValue = "TGCGATCAACAAATCTTTCGATATAGACCGAAAAAAAGAAATCTCACTGGCCAACTGTTTAAGTTGTTTTGTTGACGGCTTTTGTGGTGCCATGACAATATATCCAAACAATAACTAAATTCTCCCCTAGACCCTCTCAAACCCCAGTTTCAGGGAAAATTATAGAAACAAGTAAACTGTTTTGGTGTTTATTGTCACAGGTTTTGGTGAGCTGGCTAACAATCATTTGTTGTAAATTAACTTATCATCTAGGTGATGTTAGTTGTCACCCATCCTATGACAATATGTGTTTAATTTGACTAGCAATAGTCCAGAGATGTTCTCTAAAACTTCGTTTTTAGGCCCATACTTCACCTTCCTACAATCCCCCTCCCCCCCATCTATTCCCTGTGACTTGAACCCCTTTGTTTGCCTTTAAGAGAAATTTAAAATCCCTTTATTTGAGCTTTAAGCACTTAGCTATTAACCTGAAATTTGACCCAGAAAAACCAATAAAAGACTTCCCCACGGTCTTTGCTGACTTCCCAAAAACCCCAATCGACC";

            // Test row 684
            Row row = sheet.getRow(684);

            Object resKey = (Object) privateMethod.invoke(featuresReader, row.getCell(0));
            Object resValue = (Object) privateMethod.invoke(featuresReader, row.getCell(1));

            assertEquals(resKey, expKey);
            assertEquals(resValue, expValue);

            // Test row 3168
            row = sheet.getRow(3168);

            expKey = "3168_sll7106_left";
            expValue = null;

            resKey = (Object) privateMethod.invoke(featuresReader, row.getCell(0));
            resValue = null;

            // This particular row has no code value column
            try {
                resValue = (Object) privateMethod.invoke(featuresReader, row.getCell(1));
            } catch (Exception e) {

            }

            assertEquals(resKey, expKey);
            assertEquals(resValue, expValue);

            // Test worksheet 2 (Right flank)
            sheet = workbook.getSheetAt(1);

            expKey = "1716_slr1288_right";
            expValue = "AAGGGTAATTAGTGAAACTAAATTAGGTTGAGTTTGATTCCCCTTAGTGCGTTATCTGGAAGAATTTCAACTCGATTAACGTCTCTGTTCGGAGGCTAGCCCCATTGCCCGATCGCCGAAGGATGGGGTTATAATGGCAGGGAAGTAACTACCGCGTTGGTGACTGCCAACAACGTTTTTTGATCTATGCTTGAAAAATAAGGGTGTCTCTGTGCATTCTGCGTGGTAGTAGACCGGGCTCGTACCCGGAAACGAAAACGAGAATTGATGACTCCCCCCTGGTTACACCAGGTCATAAACTGAGGTAAAACGGCGTGGTGGTGCTTCTCCTCTGCTTCTCAGGTTTATCCCTCCCCCCAGAACGGGTTTGAGCTGATTGCTCCCGATTTTTTCCGTCATTCTACATAGTTACCCCAACGCCCTAAGCCAGGGATTCCGGCAGTACTTCTACCTGTTCCTCGGTATCGATTTCTTCGTTCTCAGTTTTTCCAGCCCGCATATCATCAATCACTTTCTTGATCACACTGGCGATCGGTA";

            // Test row 1716
            row = sheet.getRow(1716);

            resKey = (Object) privateMethod.invoke(featuresReader, row.getCell(0));
            resValue = (Object) privateMethod.invoke(featuresReader, row.getCell(1));

            assertEquals(resKey, expKey);
            assertEquals(resValue, expValue);

            // Test row 3455
            row = sheet.getRow(3455);

            expKey = "3455_slr6110_right";
            expValue = null;

            resKey = (Object) privateMethod.invoke(featuresReader, row.getCell(0));
            //resValue = (Object)privateMethod.invoke(featuresReader, row.getCell(1));
            resValue = null;

            // This particular row has no code value column
            try {
                resValue = (Object) privateMethod.invoke(featuresReader, row.getCell(1));
            } catch (Exception e) {

            }

            assertEquals(resKey, expKey);
            assertEquals(resValue, expValue);
        }
    }
}
