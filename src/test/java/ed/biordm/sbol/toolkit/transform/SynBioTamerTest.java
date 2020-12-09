/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.*;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.xml.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.TopLevel;

/**
 *
 * @author tzielins
 */
public class SynBioTamerTest {
    
    String version = "1.0.0";
    SynBioTamer instance;
    
    public SynBioTamerTest() {
    }
    
    @Before
    public void setUp() {
        instance = new SynBioTamer();
    }
    
    @Test
    public void testTameMakesReadyForSynBio() throws Exception {
        File file = new File(getClass().getResource("trevor_test_collection.xml").getFile());
        
        SBOLDocument org = SBOLReader.read(file);
        
        SBOLDocument cpy = instance.tameForSynBio(org);
        
        org.getTopLevels().forEach( part -> {
            assertTrue(part.getIdentity().toString().startsWith("https://synbiohub.org/user/zajawka/trevor_test"));
        });
        
        assertTrue(cpy.getCollections().isEmpty());
        
        cpy.getTopLevels().forEach( part -> {
            assertTrue(part.getIdentity().toString().startsWith(SynBioTamer.DEFAULT_NAMESPACE));
            
            part.setName("fix "+part.getDisplayId());
            part.setDescription("Fixed");
            
            assertTrue(part.getName().startsWith("fix"));
        });
        
        
        ComponentDefinition c180 = cpy.getComponentDefinition("IBMc180", "1");
        assertNotNull(c180);
        
        assertNull(c180.getAnnotation(SBH_OWNED));
        
        SequenceAnnotation an = c180.getSequenceAnnotation("annotation18");
        assertNotNull(an);
        
        Annotation gb = an.getAnnotation(GB_FEATURE);
        assertNotNull(gb);
        assertEquals("insulator",gb.getStringValue());
        assertEquals(Set.of(SequenceOntology.INSULATOR), an.getRoles());

        
        SBOLValidate.clearErrors();
        SBOLValidate.validateSBOL(cpy, true, true, true);
        if (SBOLValidate.getNumErrors() > 0) {
            for (String error : SBOLValidate.getErrors()) {
                System.out.println(error);
            }
            throw new IllegalStateException("Stoping cause of validation errors");
        }

        /*
        try {
            SBOLWriter.write(cpy, "E:/Temp/trevor_fixed.xml");
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }//*/        
        
    }
    
    @Test
    public void renameNamseSpacesRenames() throws Exception {

        SBOLDocument org = testDoc();
        assertTrue(org.isComplete());
        
        String prefix = "http://bio.ed.ac.uk/sbol/test2/";
        assertFalse(prefix.equals(org.getDefaultURIprefix()));

        String id = org.getComponentDefinitions().iterator().next().getIdentity().toString();
        assertFalse(id.startsWith(prefix));
        
        instance.renameNameSpace(org, prefix);
        assertTrue(org.isComplete());
        assertTrue(prefix.equals(org.getDefaultURIprefix()));
        
        for (TopLevel part : org.getTopLevels()) {
            assertTrue(part.getIdentity().toString().startsWith(prefix));
        }        
        
        SBOLValidate.clearErrors();
        SBOLValidate.validateSBOL(org, true, true, true);
        if (SBOLValidate.getNumErrors() > 0) {
            for (String error : SBOLValidate.getErrors()) {
                System.out.println(error);
            }
            SBOLValidate.clearErrors();
            throw new IllegalStateException("Stoping cause of validation errors");
        }
        
        
    }
    
    @Test
    public void removeAnnotationsStripsAnnotationsWithGivenSubject() throws Exception {

        QName prod = new QName("http://sbols.org/genBankConversion#", "product");
        QName owned = new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#","ownedBy");
        
        List<QName> subjects = List.of(prod, owned);
        SBOLDocument org = testDoc();
        
        ComponentDefinition region = org.getComponentDefinition("backbone", version);
        assertNotNull(region);
        assertNotNull(region.getAnnotation(owned));
        
        Component ori = region.getComponent("ori_instance");
        assertNotNull(ori);
        assertNotNull(ori.getAnnotation(owned));
        
        SequenceAnnotation an = region.getSequenceAnnotation("AmpR");
        assertNotNull(an);
        assertNotNull(an.getAnnotation(owned));
        assertNotNull(an.getAnnotation(prod));
        
        instance.removeAnnotations(org, subjects);
        

        assertNull(region.getAnnotation(owned));
        
        assertNull(ori.getAnnotation(owned));
        
        assertNull(an.getAnnotation(owned));
        assertNull(an.getAnnotation(prod));

        
        SBOLValidate.clearErrors();
        SBOLValidate.validateSBOL(org, true, true, true);
        if (SBOLValidate.getNumErrors() > 0) {
            for (String error : SBOLValidate.getErrors()) {
                System.out.println(error);
            }
            SBOLValidate.clearErrors();
            throw new IllegalStateException("Stoping cause of validation errors");
        }
        
        
    }    

    
    @Test
    public void mapsGenBankFeatureToRole() throws Exception {
        
        SBOLDocument doc = new SBOLDocument();
        
   
        doc.setDefaultURIprefix("http://bio.ed.ac.uk/sbol/test/");
        doc.setComplete(true);
        doc.setCreateDefaults(true);

        ComponentDefinition def = doc.createComponentDefinition("comp", ComponentDefinition.DNA_REGION);
        def.addRole(SequenceOntology.ENGINEERED_REGION);
        
        SequenceAnnotation an = def.createSequenceAnnotation("an", "an");
        an.addRole(SequenceOntology.SEQUENCE_FEATURE);
        
        assertEquals(Optional.empty(), instance.mapGenBankFeatureToRole(def));
        assertEquals(Optional.empty(), instance.mapGenBankFeatureToRole(an));
        
        def.createAnnotation(GB_FEATURE, "INSulator");
        an.createAnnotation(GB_FEATURE, "PROmoter");
        
        assertEquals(Optional.of(SequenceOntology.INSULATOR), instance.mapGenBankFeatureToRole(def));
        assertEquals(Optional.of(SequenceOntology.PROMOTER), instance.mapGenBankFeatureToRole(an));
        
    }  
    
    @Test
    public void fixGenBankRoles() throws Exception {
        
        SBOLDocument doc = new SBOLDocument();
        
   
        doc.setDefaultURIprefix("http://bio.ed.ac.uk/sbol/test/");
        doc.setComplete(true);
        doc.setCreateDefaults(true);

        ComponentDefinition def = doc.createComponentDefinition("comp", ComponentDefinition.DNA_REGION);
        //def.addRole(SequenceOntology.ENGINEERED_REGION);
        def.addRole(SequenceOntology.SEQUENCE_FEATURE);
        def.createAnnotation(GB_FEATURE, "insulator");
        
        SequenceAnnotation an = def.createSequenceAnnotation("an", "an");
        an.addRole(SequenceOntology.SEQUENCE_FEATURE);
        an.createAnnotation(GB_FEATURE, "promoter");

        ComponentDefinition def2 = doc.createComponentDefinition("comp2", ComponentDefinition.DNA_REGION);
        def2.createAnnotation(GB_FEATURE, "cds");
        
        instance.fixGenBankRoles(doc);
        
        
        
        assertEquals(Set.of(SequenceOntology.INSULATOR), def.getRoles());
        assertEquals(Set.of(SequenceOntology.PROMOTER), an.getRoles());
        assertEquals(Set.of(SequenceOntology.CDS), def2.getRoles());
        
        
       
        
    }    

    @Test
    public void fixGenBankPreservesNonGenericRoles() throws Exception {
        
        SBOLDocument doc = new SBOLDocument();
        
   
        doc.setDefaultURIprefix("http://bio.ed.ac.uk/sbol/test/");
        doc.setComplete(true);
        doc.setCreateDefaults(true);

        ComponentDefinition def = doc.createComponentDefinition("comp", ComponentDefinition.DNA_REGION);
        def.addRole(SequenceOntology.ENGINEERED_REGION);
        def.createAnnotation(GB_FEATURE, "insulator");
        
        instance.fixGenBankRoles(doc);
        
        assertEquals(Set.of(SequenceOntology.ENGINEERED_REGION), def.getRoles());
        
        def.addRole(SequenceOntology.SEQUENCE_FEATURE);
        
        instance.fixGenBankRoles(doc);
        
        assertEquals(Set.of(SequenceOntology.INSULATOR, SequenceOntology.ENGINEERED_REGION), def.getRoles());
    }    
    
    SBOLDocument testDoc() throws Exception {

        SBOLDocument doc = new SBOLDocument();
        
   
        doc.setDefaultURIprefix("http://bio.ed.ac.uk/sbol/test/");
        doc.setComplete(true);
        doc.setCreateDefaults(true);

        
        String name = "backbone";
        ComponentDefinition region = doc.createComponentDefinition(name, version, ComponentDefinition.DNA_REGION);
        region.addRole(SequenceOntology.ENGINEERED_REGION);
        region.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#","ownedBy"), new URI("https://synbiohub.org/user/zajawka"));

        String seqStr = "CGCTGCTTACAGACAAGCTGTGACCGTCTCCGGGAGCTGCATGTGTCAGAGGTTTTCACCGTCATCACCGAAACGCGCGAGACGAAAGGGCCTCGTGATACGCCTATTTTTATAGGTTAATGTCATGATAATAATGGTTTCTTAGACGTCAGGTGGCACTTTTCGGGGAAATGTGCGCGGAACCCCTATTTGTTTATTTTTCTAAATACATTCAAATATGTATCCGCTCATGAGACAATAACCCTGATAAATGCTTCAATAATATTGAAAAAGGAAGAGTATGAGTATTCAACATTTCCGTGTCGCCCTTATTCCCTTTTTTGCGGCATTTTGCCTTCCTGTTTTTGCTCACCCAGAAACGCTGGTGAAAGTAAAAGATGCTGAAGATCAGTTGGGTGCACGAGTGGGTTACATCGAACTGGATCTCAACAGCGGTAAGATCCTTGAGAGTTTTCGCCCCGAAGAACGTTTTCCAATGATGAGCACTTTTAAAGTTCTGCTATGTGGCGCGGTATTATCCCGTATTGACGCCGGGCAAGAGCAACTCGGTCGCCGCATACACTATTCTCAGAATGACTTGGTTGAGTACTCACCAGTCACAGAAAAGCATCTTACGGATGGCATGACAGTAAGAGAATTATGCAGTGCTGCCATAACCATGAGTGATAACACTGCGGCCAACTTACTTCTGACAACGATCGGAGGACCGAAGGAGCTAACCGCTTTTTTGCACAACATGGGGGATCATGTAACTCGCCTTGATCGTTGGGAACCGGAGCTGAATGAAGCCATACCAAACGACGAGCGTGACACCACGATGCCTGTAGCAATGGCAACAACGTTGCGCAAACTATTAACTGGCGAACTACTTACTCTAGCTTCCCGGCAACAATTAATAGACTGGATGGAGGCGGATAAAGTTGCAGGACCACTTCTGCGCTCGGCCCTTCCGGCTGGCTGGTTTATTGCTGATAAATCTGGAGCCGGTGAGCGTGGTTCTCGCGGTATCATTGCAGCACTGGGGCCAGATGGTAAGCCCTCCCGTATCGTAGTTATCTACACGACGGGGAGTCAGGCAACTATGGATGAACGAAATAGACAGATCGCTGAGATAGGTGCCTCACTGATTAAGCATTGGTAACTGTCAGACCAAGTTTACTCATATATACTTTAGATTGATTTAAAACTTCATTTTTAATTTAAAAGGATCTAGGTGAAGATCCTTTTTGATAATCTCATGACCAAAATCCCTTAACGTGAGTTTTCGTTCCACTGAGCGTCAGACCCCGTAGAAAAGATCAAAGGATCTTCTTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAAAACGCCAGCAACGCGGCCTTTTTACGGTTCCTGGCCTTTTGCTGGCCTTTTGCTCACATGTTCTTTCCTGCGTTATCCCCTGATTCTGTGGATAACCGTATTACCGCCTTTGAGTGAGCTGATACCGCTCGCCGCAGCCGAACGACCGAGCGCAGCGAGTCAGTGAGCGAGGAAGCGGATGAGCGCCCAATACGCAAACCGCCTCTCCCCGCGCGTTGGCCGATTCATTAATGCAGCTGGCACGACAGGTTTCggag";
        Sequence seq = doc.createSequence(name + "_seq", version, seqStr, Sequence.IUPAC_DNA);
        region.addSequence(seq);

        SequenceAnnotation an = region.createSequenceAnnotation("AmpR_prom", "AmpR_prom", 176, 280);
        an.addRole(SequenceOntology.PROMOTER);
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "gene", "gbconv"),
                "bla");

        an = region.createSequenceAnnotation("AmpR", "AmpR", 281, 1141);
        an.addRole(SequenceOntology.CDS);
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note", "gbconv"),
                "confers resistance to ampicillin, carbenicillin, and related antibiotics");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "gene", "gbconv"),
                "bla");
        an.createAnnotation(new QName("http://sbols.org/genBankConversion#", "product", "gbconv"),
                "beta-lactamase");
        an.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#","ownedBy"), new URI("https://synbiohub.org/user/zajawka"));

        ComponentDefinition originD = doc.createComponentDefinition("ori", version, ComponentDefinition.DNA_REGION);
        originD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);
        originD.createAnnotation(new QName("http://sbols.org/genBankConversion#", "note", "gbconv"),
                "high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");
        originD.setDescription("high-copy-number ColE1/pMB1/pBR322/pUC origin of replication");

        seqStr = "TTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAA";
        seq = doc.createSequence("ori_seq", version, seqStr, Sequence.IUPAC_DNA);
        originD.addSequence(seq);

        Component origin = region.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());
        origin.createAnnotation(new QName("http://wiki.synbiohub.org/wiki/Terms/synbiohub#","ownedBy"), new URI("https://synbiohub.org/user/zajawka"));

        an = region.createSequenceAnnotation("ori", "ori", 1312, 1900);
        an.setComponent(origin.getIdentity());
        
        return doc;
    }
    

    
}
