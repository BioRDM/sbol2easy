/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.cyanosource.plasmid;


import static ed.biordm.cyanosource.plasmid.CyanoTemplate.createTemplatePlasmid;
import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

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
        
        String gene = "x";
        String lSeq= "GANNAG";
        String rSeq= "CCNNNCC";
        
        instance.addGenne1stGenerationPlasmids(templ, gene, lSeq, rSeq, doc, version);
        
        assertNotNull(doc.getComponentDefinition("x", version));
        assertNotNull(doc.getComponentDefinition("x_left", version));
        assertNotNull(doc.getComponentDefinition("x_right", version));
        assertNotNull(doc.getComponentDefinition("x_flatten", version));
        
        ComponentDefinition flat = doc.getComponentDefinition("x_flatten", version);
        String seq = flat.getSequences().iterator().next().getElements();
        assertTrue(seq.contains(lSeq));
        assertTrue(seq.contains(rSeq));
        assertTrue(seq.indexOf(lSeq) < seq.indexOf(rSeq));
        
        
        
    }
    
    @Test
    public void extractsGene() {
        
        String key = "01_g";
        try {
            instance.extractGene(key);
        } catch (IllegalArgumentException e) {};
        
        key = "01";
        try {
            instance.extractGene(key);
        } catch (IllegalArgumentException e) {};        
        
        key = "01__a";
        try {
            instance.extractGene(key);
        } catch (IllegalArgumentException e) {};        
        
        key = "0002_slr0612_right";
        assertEquals("slr0612", instance.extractGene(key));
    }
    
    @Test
    public void readsSequences() throws Exception {
        
        Path file = testFile("flanks.xlsx");
        
        Map<String, String> flanks = instance.readSequences(file, 0);
        assertTrue(flanks.containsKey("slr0612"));
        assertEquals(5, flanks.size());
        
        flanks = instance.readSequences(file, 1);
        assertTrue(flanks.containsKey("slr0612"));
        assertEquals(4, flanks.size());        
        
    }
    
    
    @Test
    public void generatePlasmids() throws Exception {
        
        Path file = testFile("flanks.xlsx");
        Map<String, String> leftFlanks = instance.readSequences(file, 0);
        Map<String, String> rightFlanks = instance.readSequences(file, 1);
        String version = "2.1";
        
        List<String> genes = List.of("slr0612", "sll1214");
        SBOLDocument doc = instance.generatePlasmids(genes, version, leftFlanks, rightFlanks);
        assertNotNull(doc);
        
        ComponentDefinition cp = doc.getComponentDefinition("slr0611", version);
        assertNull(cp);
        
        cp = doc.getComponentDefinition("slr0611", version);
        assertNull(cp);  
        
        cp = doc.getComponentDefinition("sll0558", version);
        assertNull(cp);  
        
        
        cp = doc.getComponentDefinition("slr0612", version);
        assertNotNull(cp);        
        
        cp = doc.getComponentDefinition("sll1214_flatten", version);
        assertNotNull(cp);        
    }    
    
    @Test
    public void generateFromFile() throws Exception {
        
        Path file = testFile("flanks.xlsx");
        String version = "2.1";
        
        List<SBOLDocument> docs = instance.generateFromFile(file, version, 2);
        assertNotNull(docs);
        
        //assertEquals(2, docs.size());
        
        ComponentDefinition cp;
        
        
        cp = docs.get(1).getComponentDefinition("slr0612", version);
        assertNotNull(cp);        
        
        cp = docs.get(1).getComponentDefinition("sll1214_flatten", version);
        assertNotNull(cp);        
    }
    
    @Test
    public void generates() throws Exception {
        
        Path file = testFile("flanks.xlsx");
        String name = "cyano";
        String version = "1.0";
        
        Path out = tmp.newFolder().toPath();
        
        instance.generate(name, version, file, out);
        
        Path sbol = out.resolve("cyano_0.sbol");
        assertTrue(Files.isRegularFile(sbol));
        
        Path gbs = out.resolve("genbank");
        assertTrue(Files.isDirectory(gbs));
        
        assertEquals(4, Files.list(gbs).count());
        
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
