/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.transform;

import ed.biordm.sbol.sbol2easy.transform.Outcome;
import ed.biordm.sbol.sbol2easy.transform.ComponentUtil;
import ed.biordm.sbol.sbol2easy.transform.LibraryGenerator;
import ed.biordm.sbol.sbol2easy.meta.MetaFormat;
import ed.biordm.sbol.sbol2easy.meta.MetaRecord;
import static ed.biordm.sbol.sbol2easy.transform.CommonAnnotations.SBH_DESCRIPTION;
import static ed.biordm.sbol.sbol2easy.transform.ComponentUtil.emptyDocument;
import static ed.biordm.sbol.sbol2easy.transform.ComponentUtil.saveValidSbol;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author tzielins
 */
public class LibraryGeneratorTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    
    LibraryGenerator instance;
    ComponentUtil util = new ComponentUtil();
    
    
    public LibraryGeneratorTest() {
    }
    
    @Before
    public void setUp() {
        instance = new LibraryGenerator();
    }

    @Test
    public void chechMissingDataGathersIds() throws Exception {
                
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
        
        Outcome status = instance.checkMissingMeta(meta, format);
        assertEquals(List.of(), status.missingId);
        assertEquals(List.of("cp2","cp3"), status.missingMeta);
        
    } 
    
    @Test
    public void split() {
        
        List<MetaRecord> keys = List.of();
        
        List<List<MetaRecord>> exp = List.of();
        
        assertEquals(exp, instance.splitMeta(keys, 2));
        
        MetaRecord r1 = new MetaRecord();
        r1.displayId = Optional.of("c");
        
        MetaRecord r2 = new MetaRecord();
        r2.displayId = Optional.of("a");
        
        MetaRecord r3 = new MetaRecord();
        r3.displayId = Optional.of("b");
        
        keys = List.of(r1);
        exp = List.of(List.of(r1));
        assertEquals(exp, instance.splitMeta(keys, 2));
        
        keys = List.of(r1,r2);
        exp = List.of(List.of(r2,r1));
        assertEquals(exp, instance.splitMeta(keys, 2));
        
        keys = List.of(r1,r2,r3);
        exp = List.of(List.of(r2,r3), List.of(r1));
        assertEquals(exp, instance.splitMeta(keys, 2));
        
    }
    
    @Test 
    public void instantiateComponentMakesNewInstanceUsingMetaIds() throws Exception {
        SBOLDocument doc = emptyDocument();
        
        ComponentDefinition template = testingTemplate(doc);
        template.setDescription("Should stay");
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of("D1");
        
        ComponentDefinition res = instance.instantiateComponent(template, meta, "2.0", doc);
        assertNotNull(res);
        assertEquals("D1", res.getDisplayId());
        assertEquals("2.0", res.getVersion());
        assertEquals("Should stay", template.getDescription());
        
        meta.displayId = Optional.of("D2");
        meta.version = Optional.of("1");
        
        res = instance.instantiateComponent(template, meta, "2.0", doc);
        assertNotNull(res);
        assertEquals("D2", res.getDisplayId());
        assertEquals("1", res.getVersion());        
    }
    
    @Test 
    public void instantiateComponentThrowsExceptionOnMissingCompLabel() throws Exception {
        SBOLDocument doc = emptyDocument();
        
        ComponentDefinition template = testingTemplate(doc);
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of("D1");
        meta.extras.put("missing", "AAA");
        
        try {
            instance.instantiateComponent(template, meta, "2.0", doc);
            fail();
        } catch (IllegalArgumentException e) {};
    }    
    
    @Test 
    public void instantiateComponentIgnoresColumnsWithHash() throws Exception {
        SBOLDocument doc = emptyDocument();
        
        ComponentDefinition template = testingTemplate(doc);
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of("D1");
        meta.extras.put("#missing", "AAA");
        
        ComponentDefinition res = instance.instantiateComponent(template, meta, "2.0", doc);
        assertNotNull(res);
    }    
    
    @Test 
    public void instantiateComponentIgnoresExtrasWithoutSequenceValue() throws Exception {
        SBOLDocument doc = emptyDocument();
        
        ComponentDefinition template = testingTemplate(doc);
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of("D1");
        meta.extras.put("missing", "");
        
        ComponentDefinition res = instance.instantiateComponent(template, meta, "2.0", doc);
        assertNotNull(res);
    }   
    
    @Test 
    public void instantiateComponentMakesNewUsingExtrasAsConcreteInstances() throws Exception {
        SBOLDocument doc = emptyDocument();
        
        ComponentDefinition template = testingTemplate(doc);
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of("D1");
        meta.extras.put("prom", "TT");
        meta.extras.put("codA", "CCC");
        
        ComponentDefinition res = instance.instantiateComponent(template, meta, "2.0", doc);
        assertNotNull(res);
        assertEquals("D1", res.getDisplayId());
        assertEquals("2.0", res.getVersion());
        
        List<Component> comps = res.getSortedComponents();
        Component comp = comps.get(0);
        assertEquals("ori", comp.getDisplayId());
        
        comp = comps.get(1);
        assertEquals("D1_prom", comp.getDisplayId());
        assertEquals("TT", comp.getDefinition().getSequences().iterator().next().getElements());
        
        comp = comps.get(2);
        assertEquals("D1_codA", comp.getDisplayId());
        assertEquals("CCC", comp.getDefinition().getSequences().iterator().next().getElements());
        
    }  
    
    @Test
    public void describeComponentUsesExistingDescriptionsAsTemplates() throws Exception {
        
        SBOLDocument doc = emptyDocument();
        
        ComponentDefinition template = testingTemplate(doc);        
        template.setDescription("Old {key}");
        util.setAnnotation(template, SBH_DESCRIPTION, "Name {name}");
        
        
        MetaRecord meta = new MetaRecord();
        //meta.displayId = Optional.of("D1");
        meta.key = Optional.of("gene");
        meta.name = Optional.of("{key} {displayId}");
        
        instance.describeComponent(template, meta);
        
        assertEquals("gene template", template.getName());
        assertEquals("Old gene", template.getDescription());
        assertEquals("Name gene template", util.getAnnotationValue(template, SBH_DESCRIPTION));
        
    }
    
    @Test
    public void describeComponentAppendsDescriptionsFromMeta() throws Exception {
        
        SBOLDocument doc = emptyDocument();
        
        ComponentDefinition template = testingTemplate(doc);        
        template.setDescription("Old ");
        
        
        
        MetaRecord meta = new MetaRecord();
        //meta.displayId = Optional.of("D1");
        meta.key = Optional.of("gene");
        meta.name = Optional.of("{key} {displayId}");
        meta.summary = Optional.of("New");
        
        instance.describeComponent(template, meta);
        
        assertEquals("gene template", template.getName());
        assertEquals("Old New", template.getDescription());
    } 
    
    @Test
    public void generateFromTemplateReturnsIdsOfCreated() throws Exception {
    
        SBOLDocument doc = emptyDocument();        
        ComponentDefinition template = testingTemplate(doc);        
        
        List<MetaRecord> metas = new ArrayList<>();
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of("A1");
        meta.extras.put("prom", "TT");
        metas.add(meta);
        
        meta = new MetaRecord();
        meta.displayId = Optional.of("A2");
        meta.extras.put("prom", "TTT");
        meta.version = Optional.of("1.0");
        metas.add(meta);        
        
        String version = "2.0";
        
        List<String> generated = instance.generateFromTemplate(template, metas, version, doc);
        assertEquals(List.of("A1","A2"), generated);
        
        assertNotNull(doc.getComponentDefinition("A1", version));
        assertNotNull(doc.getComponentDefinition("A2", "1.0"));
        
    }
    
    @Test
    public void generateOneBatchFromFileCreatesDocumentAndUpdatesOutcome() throws Exception {
        
        SBOLDocument doc = emptyDocument();        
        ComponentDefinition template = testingTemplate(doc);        
        
        Path templateFile = tmp.newFile().toPath();
        saveValidSbol(doc, templateFile);
        
        List<MetaRecord> metas = new ArrayList<>();
        
        MetaRecord meta = new MetaRecord();
        meta.displayId = Optional.of("A1");
        meta.extras.put("prom", "TT");
        metas.add(meta);
        
        meta = new MetaRecord();
        meta.displayId = Optional.of("A2");
        meta.extras.put("prom", "TTT");
        meta.version = Optional.of("1.0");
        metas.add(meta);        
        
        String version = "2.0";
        
        Outcome outcome = new Outcome();
        
        doc = instance.generateOneBatchFromFile(templateFile, metas, version, outcome);
        assertNotNull(doc);
        assertEquals(List.of("A1","A2"), outcome.successful);
        
        assertNotNull(doc.getComponentDefinition("A1", version));
        assertNotNull(doc.getComponentDefinition("A2", "1.0"));        
    }
    
    @Test
    public void generatesFromFiles() throws Exception {
        Path outDir = tmp.newFolder().toPath();
        //Path outDir = Paths.get("E:/Temp/sbol");
        //Files.createDirectories(outDir);
     
        SBOLDocument doc = emptyDocument();        
        ComponentDefinition template = testingTemplate(doc);        
        
        Path templateFile = tmp.newFile().toPath();
        saveValidSbol(doc, templateFile);

        Path metaFile = testFile("generate_test.xlsx");
        
        String fileName = "lib";
        int batchSize = 1;
        boolean stopOnMissing = true;
        String defVersion = "1.0";
        
        Outcome out = instance.generateFromFiles(fileName, defVersion, templateFile, metaFile, outDir, stopOnMissing, batchSize);
        assertEquals(3, out.successful.size());
        
        assertEquals(3, Files.list(outDir).count());
        
        assertTrue(out.successful.contains("c2_toc"));
        
    }
    
    
    ComponentDefinition testingTemplate(SBOLDocument doc) throws SBOLValidationException {
        String version = "1.0";

        ComponentDefinition region = doc.createComponentDefinition("template", version, ComponentDefinition.DNA_REGION);
        region.addRole(SequenceOntology.ENGINEERED_REGION);

        ComponentDefinition originD = doc.createComponentDefinition("oriD", version, ComponentDefinition.DNA_REGION);
        originD.addRole(SequenceOntology.ORIGIN_OF_REPLICATION);        
        String seqStr = "TTGAGATCCTTTTTTTCTGCGCGTAATCTGCTGCTTGCAAACAAAAAAACCACCGCTACCAGCGGTGGTTTGTTTGCCGGATCAAGAGCTACCAACTCTTTTTCCGAAGGTAACTGGCTTCAGCAGAGCGCAGATACCAAATACTGTTCTTCTAGTGTAGCCGTAGTTAGGCCACCACTTCAAGAACTCTGTAGCACCGCCTACATACCTCGCTCTGCTAATCCTGTTACCAGTGGCTGCTGCCAGTGGCGATAAGTCGTGTCTTACCGGGTTGGACTCAAGACGATAGTTACCGGATAAGGCGCAGCGGTCGGGCTGAACGGGGGGTTCGTGCACACAGCCCAGCTTGGAGCGAACGACCTACACCGAACTGAGATACCTACAGCGTGAGCTATGAGAAAGCGCCACGCTTCCCGAAGGGAGAAAGGCGGACAGGTATCCGGTAAGCGGCAGGGTCGGAACAGGAGAGCGCACGAGGGAGCTTCCAGGGGGAAACGCCTGGTATCTTTATAGTCCTGTCGGGTTTCGCCACCTCTGACTTGAGCGTCGATTTTTGTGATGCTCGTCAGGGGGGCGGAGCCTATGGAAA"; 
        Sequence seq = doc.createSequence("ori_seq", version, seqStr, Sequence.IUPAC_DNA);
        originD.addSequence(seq);
        
        Component origin = region.createComponent("ori", AccessType.PUBLIC, originD.getIdentity());

        ComponentDefinition prom = doc.createComponentDefinition("promD", version, ComponentDefinition.DNA_REGION);
        prom.addRole(SequenceOntology.PROMOTER);

        Component promI = region.createComponent("prom", AccessType.PUBLIC, prom.getIdentity());
        
        ComponentDefinition codA = doc.createComponentDefinition("codAD", version, ComponentDefinition.DNA_REGION);
        codA.addRole(SequenceOntology.CDS);

        Component codAI = region.createComponent("codA", AccessType.PUBLIC, codA.getIdentity());
        
        region.createSequenceConstraint("cs1", RestrictionType.PRECEDES, origin.getPersistentIdentity(), promI.getPersistentIdentity());
        region.createSequenceConstraint("cs2", RestrictionType.PRECEDES, promI.getPersistentIdentity(), codAI.getPersistentIdentity());
        
        return region;
        
    }
    
    public Path testFile(String name) {
        try {
            return Paths.get(this.getClass().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }      
    
}
