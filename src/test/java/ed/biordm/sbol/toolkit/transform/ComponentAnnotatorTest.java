/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import ed.biordm.sbol.toolkit.meta.MetaFormat;
import ed.biordm.sbol.toolkit.meta.MetaRecord;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.CREATOR;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SBH_DESCRIPTION;
import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SBH_NOTES;
import ed.biordm.sbol.toolkit.transform.ComponentAnnotator.Outcome;
import static ed.biordm.sbol.toolkit.transform.ComponentUtil.emptyDocument;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

/**
 *
 * @author tzielins
 */
public class ComponentAnnotatorTest {
    
    ComponentAnnotator instance;
    SBOLDocument doc;
    
    public ComponentAnnotatorTest() {
    }
    
    @Before
    public void setUp() {
        instance = new ComponentAnnotator();
        doc = emptyDocument();
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
    public void chechMissingDataGathersIds() throws Exception {
        
        Map<String,List<String>> idsWithVersions = Map.of(
                "cp1", List.of("1.1"),
                "cp2", List.of("")
        );       
        
        MetaFormat format = new MetaFormat();
        format.displayId = Optional.of(0);
        format.version = Optional.of(1);
        
        MetaRecord r1 = new MetaRecord();
        r1.displayId = Optional.of("cp1");
        r1.version = Optional.of("1.1");
        
        MetaRecord r2 = new MetaRecord();
        r2.displayId = Optional.of("cp2");

        MetaRecord r3 = new MetaRecord();
        r3.displayId = Optional.of("cp3");

        List<MetaRecord> meta = List.of(r1,r2,r3);
        
        Outcome status = instance.checkMissingMeta(idsWithVersions, meta, format);
        assertEquals(List.of("cp3"), status.missingId);
        assertEquals(List.of("cp2","cp3"), status.missingMeta);
        
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
    public void addsNotes() throws Exception {
        ComponentDefinition comp = doc.createComponentDefinition("comp","1.0", ComponentDefinition.DNA_REGION);
        
        String displayId = "comp";
        String name = "my name";
        String variable = "";
        
        Optional<String> note = Optional.of("A note");
        
        instance.addNotes(comp, note, false, displayId, variable, name);
        
        assertEquals("A note", instance.util.getAnnotationValue(comp, SBH_NOTES));
        
        note = Optional.of(" {name}");        
        instance.addNotes(comp, note, false, displayId, variable, name);        
        assertEquals("A note my name", instance.util.getAnnotationValue(comp, SBH_NOTES));
        
        instance.addNotes(comp, note, true, displayId, variable, name);        
        assertEquals(" my name", instance.util.getAnnotationValue(comp, SBH_NOTES));
    }
    
    @Test
    public void addsDescription() throws Exception {
        ComponentDefinition comp = doc.createComponentDefinition("comp","1.0", ComponentDefinition.DNA_REGION);
        
        String displayId = "comp";
        String name = "my name";
        String variable = "";
        
        Optional<String> desc = Optional.of("desc");
        
        instance.addDescription(comp, desc, false, displayId, variable, name);
        
        assertEquals("desc", instance.util.getAnnotationValue(comp, SBH_DESCRIPTION));
        
        desc = Optional.of(" {variable}");        
        instance.addDescription(comp, desc, false, displayId, variable, name);        
        assertEquals("desc ", instance.util.getAnnotationValue(comp, SBH_DESCRIPTION));
        
        instance.addDescription(comp, desc, true, displayId, variable, name);        
        assertEquals(" ", instance.util.getAnnotationValue(comp, SBH_DESCRIPTION));
    }    
    
    @Test
    public void setsSbolDescription() throws Exception {
        ComponentDefinition comp = doc.createComponentDefinition("comp","1.0", ComponentDefinition.DNA_REGION);
        
        String displayId = "comp";
        String name = "my name";
        String variable = "a";
        
        Optional<String> desc = Optional.of("desc");
        
        instance.setSummary(comp, desc, displayId, variable, name);        
        assertEquals("desc", comp.getDescription());
        
        desc = Optional.of("{displayId} {name} {variable}");        
        instance.setSummary(comp, desc, displayId, variable, name);        
        assertEquals("comp my name a", comp.getDescription());
        
    }     
    
    @Test
    public void addsAuthors() throws Exception {
        ComponentDefinition comp = doc.createComponentDefinition("comp","1.0", ComponentDefinition.DNA_REGION);
        
        List<String> authors = List.of("Tomasz","Ben");

        instance.addAuthors(comp, authors);
        
        List<String> vals = comp.getAnnotations().stream()
                .filter( a -> a.getQName().equals(CREATOR))
                .map( a -> a.getStringValue())
                .collect(Collectors.toList());
        
        
        assertEquals(authors, vals);
        
    }    
    
    @Test
    public void chechMissingDataThrowsIfNoId() throws Exception {
        
        Map<String,List<String>> idsWithVersions = Map.of(
                "cp1", List.of("1.1"),
                "cp2", List.of("")
        );       
    
        MetaFormat format = new MetaFormat();
        format.displayId = Optional.of(0);
        format.version = Optional.of(1);
        
        MetaRecord r1 = new MetaRecord();
        r1.displayId = Optional.empty();
        
        List<MetaRecord> meta = List.of(r1);
        try {
            instance.checkMissingMeta(idsWithVersions, meta, format);
            fail();
        } catch (IllegalArgumentException e) {
            
        }
        
        r1.displayId = Optional.of(" ");
        try {
            instance.checkMissingMeta(idsWithVersions, meta, format);
            fail();
        } catch (IllegalArgumentException e) {
            
        }
        
    }
    
    @Test
    public void validateCompletness() {
        Outcome status = new Outcome();
        
        instance.validateCompletness(status, true, true);
        
        status.missingId = List.of("123");
        try {
            instance.validateCompletness(status, true, true);
            fail();
        } catch (IllegalArgumentException e){};
        
        instance.validateCompletness(status, false, true);
        
        status.missingMeta =List.of("A");
        try {
            instance.validateCompletness(status, false, true);
            fail();
        } catch (IllegalArgumentException e){};
        
        instance.validateCompletness(status, false, false);
        
    }
    
    @Test
    public void annotatesDesigns() throws Exception {
        
        Path excel = testFile("annot_test.xlsx");
        assertTrue(Files.isRegularFile(excel));
        
        String version = "1.0";
        
        ComponentDefinition c1 = doc.createComponentDefinition("cs0001_slr0611",version, ComponentDefinition.DNA_REGION);
        ComponentDefinition c2 = doc.createComponentDefinition("cs0002_slr0612",version, ComponentDefinition.DNA_REGION);
                
        Outcome status = instance.annotate(doc, excel, false, false, false);
        
        assertEquals(List.of(c1.getDisplayId(), c2.getDisplayId()), status.successful);
        assertEquals(List.of("cs0003_slr0613"), status.missingId);
        assertEquals(List.of("cs0002_slr0612", "cs0003_slr0613"), status.missingMeta);
        
        assertEquals("cs0001_slr0611 flat", c1.getName());
        assertEquals("cs0002_slr0612 flat", c2.getName());

        assertEquals("1.0", c1.getDescription());
        assertEquals("", c2.getDescription());
    }
    
    public Path testFile(String name) {
        try {
            return Paths.get(this.getClass().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }     
    
}
