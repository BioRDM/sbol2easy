/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.meta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class MetaHelperTest {
    
    MetaHelper instance;
    
    public MetaHelperTest() {
    }
    
    @Before
    public void setUp() {
        instance = new MetaHelper();
    }

    @Test
    public void setTemplateVariableReplaces() {
        
        String template = "I am {gene} {gene} {name}";
        String var = "missing";
        String val = "X";
        
        assertEquals(template, instance.setTemplateVariable(var, val, template));
        
        var = "gene";
        String exp = "I am X X {name}";
        assertEquals(exp, instance.setTemplateVariable(var, val, template));
        
        var = "name";
        exp = "I am {gene} {gene} X";
        assertEquals(exp, instance.setTemplateVariable(var, val, template));
    }
    
    @Test
    public void emptyMetaComparesWithFormat() {
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of("id");
        meta.version = Optional.of("ver");
        meta.name = Optional.of("name");
        meta.authors = List.of("A");
        meta.extras = Map.of("left", "1", "right", "2");
        
        MetaFormat format = new MetaFormat();
        format.displayId = Optional.of(1);
        format.version = Optional.of(2);
        format.name = Optional.of(3);
        format.authors = List.of(4);
        format.extras = Map.of("left", 6, "right", 7);
        
        assertFalse(instance.emptyMeta(meta, format));
        
        meta.version = Optional.of("");
        assertTrue(instance.emptyMeta(meta, format));
        
        meta.version = Optional.of("ver");
        assertFalse(instance.emptyMeta(meta, format));
        
        format.authors = List.of(4,5);
        assertTrue(instance.emptyMeta(meta, format));

        meta.authors = List.of("A","B");
        assertFalse(instance.emptyMeta(meta, format));
        
        format.extras = Map.of("left", 6, "right", 7, "barcode",8);
        assertTrue(instance.emptyMeta(meta, format));
        
        meta.extras = Map.of("left", "1", "right", "2","barcode","");
        assertTrue(instance.emptyMeta(meta, format));
        
        meta.extras = Map.of("left", "1", "right", "2","barcode","3");
        assertFalse(instance.emptyMeta(meta, format));
    }
    
    @Test
    public void calculateIdModifiesIdsWithKeyTemplate() {
        
        MetaRecord meta1 = new MetaRecord();
        meta1.displayId = Optional.of("id");
        meta1.key = Optional.of("A");
        
        MetaRecord meta2 = new MetaRecord();
        meta2.displayId = Optional.of("id{key}");
        meta2.key = Optional.of("B");
        
        List<MetaRecord> list = List.of(meta1, meta2);
        List<MetaRecord> res = instance.calculateIdFromKey(list);
        
        assertSame(list, res);
        assertEquals("id", meta1.displayId.get());
        assertEquals("idB", meta2.displayId.get());
        
    }    
    
}
