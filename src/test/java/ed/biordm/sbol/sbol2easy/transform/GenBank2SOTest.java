/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.transform;

import ed.biordm.sbol.sbol2easy.transform.GenBank2SO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author Tomasz Zielinski
 */
public class GenBank2SOTest {
    
    GenBank2SO instance;
    SequenceOntology so = new SequenceOntology();
    
    public GenBank2SOTest() {
    }
    
    @Before
    public void setUp() {
        
        Map<String,String> features = Map.of("CDS","SO:0000316");
        instance = new GenBank2SO(features);
    }
    
    @Test
    public void conversionsReturnsDefaultsOnMissing() {
        instance = new GenBank2SO(Map.of());
        
        assertEquals(SequenceOntology.SEQUENCE_FEATURE, instance.featureToTerm("cds"));
        assertEquals("misc_feature", instance.termToFeature(SequenceOntology.PROMOTER));
    }
    
    @Test
    public void deafaultFeaturesAreMappedCaseAgnostic() {
        
        instance = new GenBank2SO();
        assertEquals(SequenceOntology.CDS, instance.featureToTerm("cds"));
        assertEquals(SequenceOntology.CDS, instance.featureToTerm("cdS"));
        

        assertEquals(so.getURIbyId("SO:0000627"), instance.featureToTerm("InsulaTOR"));
        
    }
    
    @Test
    public void defaultTermsAreMappedPreservingCases() {
        
        instance = new GenBank2SO();
        assertEquals("CDS",instance.termToFeature(SequenceOntology.CDS));
        assertEquals("gap", instance.termToFeature("http://www.sequenceontology.org/browser/current_svn/term/SO:0000730"));
        assertEquals("Insulator", instance.termToFeature("SO:0000627"));
        assertEquals("gap", instance.termToFeature("SO:0000730"));
    }

    @Test
    public void readsDefaultConversionPreservingOrder() {
        
        Map<String, String> features = GenBank2SO.readDefaultConversion();
        assertEquals("SO:0000316", features.get("CDS"));
        assertEquals("SO:0000627", features.get("Insulator"));
        
        List<String> keys = new ArrayList(features.keySet());
        assertTrue(keys.indexOf("CDS") < keys.indexOf("Insulator"));
        assertTrue(keys.indexOf("J_segment") < keys.indexOf("J_gene_segment"));
        assertTrue(keys.indexOf("gap") < keys.indexOf("assembly_gap"));
    }
    
    @Test
    public void invertUsesFirstValueToInverTerms() {
        
        Map<String,String> map = new LinkedHashMap<>();
        map.put("A","TA");
        map.put("BB","TB");
        map.put("AA","TA");
        map.put("B","TB");
        
        Map<String,String> exp = Map.of("TA","A","TB","BB");
        
        assertEquals(exp, GenBank2SO.invert(map));
    }
    
    /*@Test
    public void lowerKeys() {
        Map<String,String> map = Map.of("aA","a","B","B","CCc","c");
        
        Map<String,String> exp =Map.of("aa","a","b","B","ccc","c");
        
        assertEquals(exp, GenBank2SO.lowerKeys(map));
    }*/
    
}
