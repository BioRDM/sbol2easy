/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.meta;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class ExcelReaderTest {
    
    
    ExcelReader instance;
    public ExcelReaderTest() {
    }
    
    @Before
    public void setUp() {
        instance = new ExcelReader();
    }

    @Test
    public void testSetup() {
        
        assertNotNull(instance);
        
        assertTrue(Files.isRegularFile(testFile("meta_test.xlsx")));
    }
    
    @Test
    public void readsFullRow() throws Exception {
        
        List<String> row = instance.readStringRow(testFile("meta_test.xlsx"), 0, 0);
        
        List<String> exp = List.of("display_id","name","key","description","notes","left");

        assertEquals(exp, row);
        
        row = instance.readStringRow(testFile("meta_test.xlsx"), 0, 2);
        exp = List.of("cs0002_slr0612",	"N cs0002_slr0612");
        assertEquals(exp, row);
    }
    
    @Test
    public void readsRowsPadingEmpty() throws Exception {
        
        List<List<String>> rows = instance.readStringRows(testFile("meta_test.xlsx"), 0, 0, 6);
        
        List<List<String>> exp = List.of(
                List.of("display_id","name","key","description","notes","left"),
                List.of("cs0001_slr0611",	"N cs0001_slr0611",	"123.0","target gene {name}","","AACC"),
                List.of("cs0002_slr0612",	"N cs0002_slr0612",	"","","",""),
                List.of("","missing","","","",""),
                List.of("cs0003_slr0613",	"N cs0003_slr0613",	"123.0","","","")
        );

        //for (int i = 0; i< 6;i++)
        //    assertEquals(exp.get(3).get(i),rows.get(3).get(i));
        
        //assertEquals(exp.get(4), rows.get(4));
        
        assertEquals(exp, rows);
        
        rows = instance.readStringRows(testFile("meta_test.xlsx"), 0, 1, 6);
        assertEquals(exp.subList(1,exp.size()), rows);

    }
    
    public Path testFile(String name) {
        try {
            return Paths.get(this.getClass().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }    
    
}
