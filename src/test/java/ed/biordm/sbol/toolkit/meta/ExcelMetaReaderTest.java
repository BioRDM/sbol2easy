/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.meta;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class ExcelMetaReaderTest {
    
    ExcelMetaReader instance;
    
    public ExcelMetaReaderTest() {
    }
    
    @Before
    public void setUp() {
        instance = new ExcelMetaReader();
    }
    
    @Test
    public void readMeta() throws Exception {
        
        Path file = testFile("meta_test.xlsx");
        
        List<MetaRecord> metas = instance.readMeta(file);
        
        assertEquals(4, metas.size());
        
        assertEquals("cs0001_slr0611", metas.get(0).displayId.get());
        assertEquals("", metas.get(2).displayId.get());
        assertEquals("N cs0003_slr0613", metas.get(3).name.get());
        
    }
    

    @Test
    public void parseHeader() {
    
        List<String> header = List.of("","display_id","name","key","author","author",
                "description","notes","attachment_filename","left","right","version","");
        
        MetaFormat exp = new MetaFormat();
        exp.displayId = Optional.of(1);
        exp.version = Optional.of(11);
        exp.name = Optional.of(2);
        exp.key = Optional.of(3);
        exp.authors = List.of(4,5);
        exp.description = Optional.of(6);
        exp.notes = Optional.of(7);
        exp.attachment = Optional.of(8);
        exp.extras.put("left",9);
        exp.extras.put("right",10);
        exp.cols = 12;
        
        assertEquals(exp, instance.parseHeader(header));
                
    }
    
    
    @Test
    public void parseMeta() {
    
        List<String> header = List.of("version","display_id","name","key","author","author",
                "description","notes","attachment_filename","left","right","");
        
        MetaFormat def = new MetaFormat();
        def.displayId = Optional.of(1);
        def.version = Optional.of(0);
        def.name = Optional.of(2);
        def.key = Optional.of(3);
        def.authors = List.of(4,5);
        def.description = Optional.of(6);
        def.notes = Optional.of(7);
        def.attachment = Optional.of(8);
        def.extras.put("left",9);
        def.extras.put("right",10);
        def.cols = 11;

        List<String> row = List.of("1.1","id1","name","key","author1","author2",
                "description","notes","attach","A","B");
        
        MetaRecord exp = new MetaRecord();
        exp.displayId = Optional.of("id1");
        exp.version = Optional.of("1.1");
        exp.name = Optional.of("name");
        exp.key = Optional.of("key");
        exp.authors = List.of("author1","author2");
        exp.description = Optional.of("description");
        exp.notes = Optional.of("notes");
        exp.attachment = Optional.of("attach");
        exp.extras.put("left","A");
        exp.extras.put("right","B");
        
        assertEquals(exp, instance.parseMeta(row,def));
                
    }    
    
    @Test
    public void testTrim() {
        
        List<String> list = List.of();
        List<String> exp = List.of();
        
        assertEquals(exp, instance.trim(list));
        
        list = new ArrayList();
        list.add("");
        list.add(null);
        assertEquals(exp, instance.trim(list));
        
        list = List.of("a");
        exp = List.of("a");
        assertEquals(exp, instance.trim(list));
        
        list = List.of("","a");
        exp = List.of("","a");
        assertEquals(exp, instance.trim(list));
        
        list = List.of("","a","","b","","");
        exp = List.of("","a","","b");
        assertEquals(exp, instance.trim(list));
    }
    
    @Test
    public void testIsNotEmpty() {
        
        assertFalse(instance.isListNotEmpty(List.of()));
        assertFalse(instance.isListNotEmpty(List.of("","")));
        assertTrue(instance.isListNotEmpty(List.of("","","a","")));
        
    }
    
    @Test
    public void testLocationToValue() {
        List<String> vals = List.of("a","b","");
        
        assertEquals(Optional.of("b"), instance.locationToValue(Optional.of(1), vals));
        assertEquals(Optional.of(""), instance.locationToValue(Optional.of(2), vals));
        assertEquals(Optional.empty(), instance.locationToValue(Optional.empty(), vals));
                
    }
    
    public Path testFile(String name) {
        try {
            return Paths.get(this.getClass().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }      
    
}
