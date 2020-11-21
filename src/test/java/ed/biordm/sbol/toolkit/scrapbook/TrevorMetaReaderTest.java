/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.scrapbook;

import ed.biordm.sbol.toolkit.scrapbook.TrevorMetaReader;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Tomasz Zielinski
 */
public class TrevorMetaReaderTest {
    
    TrevorMetaReader reader;
    
    public TrevorMetaReaderTest() {
    }
    
    @Before
    public void setUp() {
        
        reader = new TrevorMetaReader();
    }

    @Test
    public void readReads() {
        File file = testFile("IBM constructs by ID.csv");
        
        List<String[]> rows = reader.read(file.toPath(), 0);
        assertEquals(137, rows.size());
    }
    
    @Test
    public void parses() {
        File file = testFile("IBM constructs by ID.csv");
        
        Map<String,String[]> rows = reader.parse(file.toPath(), 1);
        assertEquals(136, rows.size());
        assertNotNull(rows.get("IBMc052"));
    }

    @Test
    public void summary() {
       init();
       String id = "IBMc050";
       assertEquals("pSB1A3-BbsI-M86N-cat-Plux2-B32-M86C-SapI",reader.summary(id));
    } 
    
    @Test
    public void pi() {
       init();
       String id = "IBMc050";
       assertEquals("Baojun Wang",reader.pi(id));
    }     
    
    @Test
    public void creator() {
       init();
       String id = "IBMc050";
       assertEquals("Trevor Y. H. Ho",reader.creator(id));
    }     
    
    @Test
    public void origin() {
       init();
       String id = "IBMc050";
       assertEquals("pMB1",reader.originOfRep(id));
    }  
    
    @Test
    public void selection() {
       init();
       String id = "IBMc050";
       assertEquals("Ampicillin / Chloramphenicol",reader.selection(id));
    }    

   

    @Test
    public void backbone() {
       init();
       String id = "IBMc050";
       assertEquals("pSB1A3",reader.backbone(id));
    } 

    @Test
    public void replicates() {
       init();
       String id = "IBMc050";
       assertEquals("E. coli TOP10",reader.replicatesIn(id));
    } 

    @Test
    public void description() {
       init();
       String id = "IBMc050";
       assertEquals("pSB1A3-BbsI-M86N-cat-Plux2-B32-M86C-SapI\n"
               + "Plasmid carrying Golden Gate substitution insert for IBM, M86 intein, using Plux2 as C-lobe promoter",reader.description(id));
    }     
    
    
    @Test
    public void notes() {
       init();
       String id = "IBMc050";
       String exp ="" +
"Creator: Trevor Y. H. Ho\n" +
"Principal Investigator: Baojun Wang\n" +
"BioSafety Level: Level 1\n" +
"Backbone: pSB1A3\n" +
"Origin of replication: pMB1\n" +
"Selection Markers: Ampicillin / Chloramphenicol\n";
       assertEquals(exp,reader.notes(id));
    } 

     
    
    void init() {
        File file = testFile("IBM constructs by ID.csv");
        reader.init(file.toPath(), 1);
    }
    
    
    
    public File testFile(String name) {
        try {
            return new File(this.getClass().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
                
    }    
    
}
