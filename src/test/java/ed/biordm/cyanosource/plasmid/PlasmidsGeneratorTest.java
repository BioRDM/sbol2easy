/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.cyanosource.plasmid;


import static ed.biordm.cyanosource.plasmid.CyanoTemplate.createTemplatePlasmid;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;

/**
 *
 * @author tzielins
 */
public class PlasmidsGeneratorTest {
    
    
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    
    PlasmidsGenerator instance;
    public PlasmidsGeneratorTest() {
    }
    
    @Before
    public void setUp() {
        instance = new PlasmidsGenerator();        
    }

    @Test
    public void adds1stGenerationPlasmids() throws Exception {
        
        String version = "2.0";
        SBOLDocument doc = instance.cyanoDocument();
        ComponentDefinition templ = createTemplatePlasmid(doc, "1.0");
        
        String gene = "s_x";
        String lSeq= "GANNAG";
        String rSeq= "CCNNNCC";
        
        instance.addGenne1stGenerationPlasmids(templ, gene, lSeq, rSeq, doc, version);
        
        assertNotNull(doc.getComponentDefinition("s_x", version));
        assertNotNull(doc.getComponentDefinition("x_left", version));
        assertNotNull(doc.getComponentDefinition("x_right", version));
        assertNotNull(doc.getComponentDefinition("s_x_flatten", version));
        
        ComponentDefinition flat = doc.getComponentDefinition("s_x_flatten", version);
        String seq = flat.getSequences().iterator().next().getElements();
        assertTrue(seq.contains(lSeq));
        assertTrue(seq.contains(rSeq));
        assertTrue(seq.indexOf(lSeq) < seq.indexOf(rSeq));
        
        
        
    }
    
    @Test
    public void extractsGene() {
        
        String key = "01_g";
        
        key = "01";
        try {
            instance.extractGeneFromId(key);
        } catch (IllegalArgumentException e) {};        
        
        key = "01__a";
        try {
            instance.extractGeneFromId(key);
        } catch (IllegalArgumentException e) {};        

        key = "0002_slr0612_right";
        try {
            instance.extractGeneFromId(key);
        } catch (IllegalArgumentException e) {};        
        
        key = "0002_slr0612";
        assertEquals("slr0612", instance.extractGeneFromId(key));
    }
    
    @Test
    public void extractsGeneO() {
        
        String key = "01_g";
        try {
            instance.extractGeneO(key);
        } catch (IllegalArgumentException e) {};
        
        key = "01";
        try {
            instance.extractGeneO(key);
        } catch (IllegalArgumentException e) {};        
        
        key = "01__a";
        try {
            instance.extractGeneO(key);
        } catch (IllegalArgumentException e) {};        
        
        key = "0002_slr0612_right";
        assertEquals("slr0612", instance.extractGeneO(key));
    }    
    
    @Test
    public void extractsDisplayId() {
        
        String key = "01_g";
        try {
            instance.extractDisplayId(key);
        } catch (IllegalArgumentException e) {};
        
        key = "01";
        try {
            instance.extractDisplayId(key);
        } catch (IllegalArgumentException e) {};        
        
        key = "01__a";
        try {
            instance.extractDisplayId(key);
        } catch (IllegalArgumentException e) {};        
        
        key = "0002_slr0612_right";
        assertEquals("cs0002_slr0612", instance.extractDisplayId(key));
    }    
    
    @Test
    public void readsSequences() throws Exception {
        
        Path file = testFile("flanks.xlsx");
        
        Map<String, String> flanks = instance.readSequences(file, 0);
        assertTrue(flanks.containsKey("cs0002_slr0612"));
        assertEquals(5, flanks.size());
        
        flanks = instance.readSequences(file, 1);
        assertTrue(flanks.containsKey("cs0002_slr0612"));
        assertEquals(4, flanks.size());        
        
    }
    
    protected Path tmpTemplate() throws IOException, SBOLConversionException, SBOLValidationException {
        SBOLDocument templateDoc = instance.cyanoDocument();
        ComponentDefinition template = createTemplatePlasmid(templateDoc, "1.0");
        Path templateFile = tmp.newFile().toPath();
        SBOLWriter.write(templateDoc, templateFile.toFile());
        return templateFile;
    }
    
    @Test
    public void generatePlasmidsFromTemplate() throws Exception {

        String version = "2.1";
        
        Path templateFile = tmpTemplate();
        
        Path file = testFile("flanks.xlsx");
        Map<String, String> leftFlanks = instance.readSequences(file, 0);
        Map<String, String> rightFlanks = instance.readSequences(file, 1);
        
        List<String> genes = List.of("cs0002_slr0612", "cs0005_sll1214");
        SBOLDocument doc = instance.generatePlasmidsFromTemplate(templateFile, genes, version, leftFlanks, rightFlanks);
        assertNotNull(doc);
        
        ComponentDefinition cp = doc.getComponentDefinition("cs0001_slr0611", version);
        assertNull(cp);
        
        cp = doc.getComponentDefinition("cs0001_slr0611", version);
        assertNull(cp);  
        
        cp = doc.getComponentDefinition("cs0004_sll0558", version);
        assertNull(cp);  
        
        
        cp = doc.getComponentDefinition("cs0002_slr0612", version);
        assertNotNull(cp);        
        
        cp = doc.getComponentDefinition("cs0002_slr0612_flatten", version);
        assertNotNull(cp);        
    }    
    
    @Test
    public void generatePlasmidsInSitu() throws Exception {
        
        Path file = testFile("flanks.xlsx");
        Map<String, String> leftFlanks = instance.readSequences(file, 0);
        Map<String, String> rightFlanks = instance.readSequences(file, 1);
        String version = "2.1";
        
        List<String> genes = List.of("cs0002_slr0612", "cs0005_sll1214");
        SBOLDocument doc = instance.generatePlasmidsInSitu(genes, version, leftFlanks, rightFlanks);
        assertNotNull(doc);
        
        ComponentDefinition cp = doc.getComponentDefinition("cs0001_slr0611", version);
        assertNull(cp);
        
        cp = doc.getComponentDefinition("cs0001_slr0611", version);
        assertNull(cp);  
        
        cp = doc.getComponentDefinition("cs0004_sll0558", version);
        assertNull(cp);  
        
        
        cp = doc.getComponentDefinition("cs0002_slr0612", version);
        assertNotNull(cp);        
        
        cp = doc.getComponentDefinition("cs0002_slr0612_flatten", version);
        assertNotNull(cp);        
    }     
    
    @Test
    public void generateFromFile() throws Exception {
        
        Path templateFile = tmpTemplate();
        
        Path file = testFile("flanks.xlsx");
        String version = "2.1";
        
        instance.ONLY_FULL = false;        
        List<SBOLDocument> docs = instance.generateFromFileTemplate(templateFile, file, version, 2);
        assertNotNull(docs);
        
        //assertEquals(2, docs.size());
        
        ComponentDefinition cp;
        
        
        cp = docs.get(0).getComponentDefinition("cs0002_slr0612", version);
        assertNotNull(cp);        
        
        cp = docs.get(0).getComponentDefinition("cs0002_slr0612_flatten", version);
        assertNotNull(cp);        
    }
    
    @Test
    public void generates() throws Exception {
        
        Path templateFile = tmpTemplate();
        
        Path file = testFile("flanks.xlsx");
        String name = "cyano";
        String version = "1.0";
        
        Path outDir = tmp.newFolder().toPath();
        //Path outDir = Paths.get("E:/Temp/sbol-test-20210102");
        
        instance.ONLY_FULL = false;
        instance.generateFromFiles(name, version, templateFile, file, outDir);
        
        Path out = outDir.resolve("sbol");
        Path sbol = out.resolve("cyano_0.xml");
        assertTrue(Files.isRegularFile(sbol));
        
        Path gbs = outDir.resolve("genbank");
        assertTrue(Files.isDirectory(gbs));
        
        assertEquals(4, Files.list(gbs).count());
        
    }
    
    @Test
    public void generateStopsOnMissing() throws Exception {
        
        Path templateFile = tmpTemplate();
        
        Path file = testFile("flanks.xlsx");
        String name = "cyano";
        String version = "1.0";
        
        Path outDir = tmp.newFolder().toPath();
        //Path out = Paths.get("E:/Temp/sbol-test");
        
        try {
            instance.generateFromFiles(name, version, templateFile, file, outDir);
            fail("Exception expected");
        } catch (IllegalArgumentException e) {}
        
        
    }    
           
    @Test
    public void splitKeys() {
        
        List<String> keys = List.of();
        
        List<List<String>> exp = List.of();
        
        assertEquals(exp, instance.splitKeys(keys, 2));
        
        keys = List.of("c");
        exp = List.of(List.of("c"));
        assertEquals(exp, instance.splitKeys(keys, 2));
        
        keys = List.of("c","a");
        exp = List.of(List.of("a","c"));
        assertEquals(exp, instance.splitKeys(keys, 2));
        
        keys = List.of("c","a","b");
        exp = List.of(List.of("a","b"), List.of("c"));
        assertEquals(exp, instance.splitKeys(keys, 2));
        
    }
            
    public Path testFile(String name) {
        try {
            return new File(this.getClass().getResource(name).toURI()).toPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }            
    
}
