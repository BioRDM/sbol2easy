/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author tzielins
 */
public class ComponentUtilTest {
    
    public ComponentUtilTest() {
    }
    
    ComponentUtil instance;
    SBOLDocument doc;
    
    @Before
    public void setUp() {
        instance = new ComponentUtil();
        
        doc = ComponentUtil.emptyDocument();

    }

    @Test
    public void getsOneRootComponent() throws Exception {
        
        
        try {
            instance.extractRootComponent(doc);
            fail("Exception expected");
        } catch (IllegalArgumentException e){}
        
        ComponentDefinition c1 = doc.createComponentDefinition("uno", "1.0",ComponentDefinition.DNA_REGION);
        
        assertSame(c1, instance.extractRootComponent(doc));
        ComponentDefinition c2 = doc.createComponentDefinition("dos", "1.0",ComponentDefinition.DNA_REGION);
        
        try {
            instance.extractRootComponent(doc);
            fail("Exception expected");
        } catch (IllegalArgumentException e){}
        
        c1.createComponent("c2_insta", AccessType.PUBLIC, c2.getPersistentIdentity());
        
        assertSame(c1, instance.extractRootComponent(doc));
        
    }
    
    @Test
    public void getsLastVersionedComponentByName() throws Exception {
        
        
        String name = "dos";
        
        try {
            instance.extractComponent(name, doc);
            fail("Exception expected");
        } catch (IllegalArgumentException e){}
        
        ComponentDefinition c1 = doc.createComponentDefinition("uno", "1.0",ComponentDefinition.DNA_REGION);

        try {
            instance.extractComponent(name, doc);
            fail("Exception expected");
        } catch (IllegalArgumentException e){}

        
        ComponentDefinition c2 = doc.createComponentDefinition(name, "1.0",ComponentDefinition.DNA_REGION);
        
        assertSame(c2, instance.extractComponent(name, doc));
        
        c1.createComponent("c2_insta", AccessType.PUBLIC, c2.getPersistentIdentity());        
        assertSame(c2, instance.extractComponent(name, doc));
        
        ComponentDefinition c3 = doc.createComponentDefinition(name, "1.2",ComponentDefinition.DNA_REGION);
        assertSame(c3, instance.extractComponent(name, doc));
        
        c1.createComponent("c3_insta", AccessType.PUBLIC, c3.getPersistentIdentity());        
        assertSame(c3, instance.extractComponent(name, doc));
        
    }
    @Test
    public void extractsDisplayIds() throws Exception {
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1","1.0", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        cp1.addRole(SequenceOntology.PROMOTER); 
        
        doc.createComponentDefinition("cp1","1.2", ComponentDefinition.DNA_REGION);
        doc.createComponentDefinition("cp1","0.2", ComponentDefinition.DNA_REGION);
        
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addRole(SequenceOntology.CDS);
        
        ComponentDefinition cp3 = doc.createComponentDefinition("cp3", ComponentDefinition.DNA_REGION);
        cp3.addRole(SequenceOntology.TERMINATOR);
        
        Component cp1i = cont.createComponent("cp1i", AccessType.PUBLIC, cp1.getPersistentIdentity());
        Component cp2i = cont.createComponent("cp2i", AccessType.PUBLIC, cp2.getPersistentIdentity());
        Component cp3i = cont.createComponent("cp3i", AccessType.PUBLIC, cp3.getPersistentIdentity());        
        
        
        Map<String,List<String>> exp = new HashMap<>();
        List<String> l = new ArrayList<>();
        l.add(null);
        exp.put("cont", l);
        exp.put("cp1", List.of("1.2","1.0","0.2"));
        exp.put("cp2", l);
        exp.put("cp3", l);
        
        assertEquals(exp, instance.extractComponentsVersionedDisplayIds(doc));
    }
    
    @Test
    public void createsCmpCopy() throws Exception {
        doc = testDoc("cyano_gen_template.xml");
        String version = "1.0.0";
        
        ComponentDefinition p = doc.getComponentDefinition("backbone", version);
        assertNotNull(p);
        
        Component comp = p.getComponent("ori_instance");
        assertNotNull(comp);
        
        ComponentDefinition dest = doc.createComponentDefinition("testC", ComponentDefinition.DNA_REGION);
        
        Component cpy = instance.createCmpCopy(comp, dest);
        assertNotNull(cpy);
        assertNotNull(dest.getComponent("ori_instance"));
    }
    
    @Test
    public void createsCmpCopyInUnrelatedDoc() throws Exception {
        SBOLDocument org = testDoc("cyano_gen_template.xml");
        String version = "1.0.0";
        
        ComponentDefinition p = org.getComponentDefinition("backbone", version);
        assertNotNull(p);
        
        Component comp = p.getComponent("ori_instance");
        assertNotNull(comp);
        
        doc = instance.emptyDocument();
        ComponentDefinition dest = doc.createComponentDefinition("testC", ComponentDefinition.DNA_REGION);
        
        Component cpy = instance.createCmpCopy(comp, dest);
        assertNotNull(cpy);
        assertNotNull(dest.getComponent("ori_instance"));
    }    
    
            
    @Test
    public void createAnnCopy() throws Exception {
        doc = testDoc("cyano_gen_template.xml");
        String version = "1.0.0";
        
        ComponentDefinition p = doc.getComponentDefinition("backbone", version);
        assertNotNull(p);
        
        SequenceAnnotation s = p.getSequenceAnnotation("AmpR_prom");
        assertNotNull(s);
        
        ComponentDefinition dest = doc.createComponentDefinition("testC", ComponentDefinition.DNA_REGION);
        
        
        SequenceAnnotation cpy = instance.createAnnCopy(s, dest,10);
        assertNotNull(cpy);
        assertSame(cpy, dest.getSequenceAnnotation("AmpR_prom"));
        
        assertEquals(Set.of(SequenceOntology.PROMOTER), cpy.getRoles());
        Annotation a = cpy.getAnnotation(CommonAnnotations.GB_GENE);
        assertNotNull(a);
        assertEquals("bla", a.getStringValue());
        
        Range r = (Range) cpy.getLocation("AmpR_prom");
        assertEquals(10+176, r.getStart());
        assertEquals(10+280, r.getEnd());
        
    }  
    
    @Test
    public void addsAnnotation() throws Exception {
        
        ComponentDefinition comp = doc.createComponentDefinition("comp","1", ComponentDefinition.DNA_REGION);
        
        instance.addAnnotation(comp, CommonAnnotations.SBH_DESCRIPTION, "Tomek");
        
        assertNotNull(comp.getAnnotation(CommonAnnotations.SBH_DESCRIPTION));
        assertEquals("Tomek",comp.getAnnotation(CommonAnnotations.SBH_DESCRIPTION).getStringValue());
        
        instance.addAnnotation(comp, CommonAnnotations.SBH_DESCRIPTION, "Tomek2");
        assertEquals(2, comp.getAnnotations().size());
        assertEquals("Tomek2",comp.getAnnotations().get(1).getStringValue());
    }
    
    @Test
    public void setsAnnotation() throws Exception {
        
        ComponentDefinition comp = doc.createComponentDefinition("comp","1", ComponentDefinition.DNA_REGION);
        
        instance.setAnnotation(comp, CommonAnnotations.SBH_DESCRIPTION, "Tomek");
        
        assertNotNull(comp.getAnnotation(CommonAnnotations.SBH_DESCRIPTION));
        assertEquals("Tomek",comp.getAnnotation(CommonAnnotations.SBH_DESCRIPTION).getStringValue());
        
        instance.setAnnotation(comp, CommonAnnotations.SBH_DESCRIPTION, "Tomek2");
        assertEquals(1, comp.getAnnotations().size());
        assertEquals("Tomek2",comp.getAnnotation(CommonAnnotations.SBH_DESCRIPTION).getStringValue());
    }    
    
    @Test
    public void appendsAnnotation() throws Exception {
        
        ComponentDefinition comp = doc.createComponentDefinition("comp","1", ComponentDefinition.DNA_REGION);
        
        instance.appendAnnotation(comp, CommonAnnotations.SBH_DESCRIPTION, "Tomek");
        
        assertNotNull(comp.getAnnotation(CommonAnnotations.SBH_DESCRIPTION));
        assertEquals("Tomek",comp.getAnnotation(CommonAnnotations.SBH_DESCRIPTION).getStringValue());
        
        instance.appendAnnotation(comp, CommonAnnotations.SBH_DESCRIPTION, "Tomek2");
        assertEquals(1, comp.getAnnotations().size());
        assertEquals("TomekTomek2",comp.getAnnotation(CommonAnnotations.SBH_DESCRIPTION).getStringValue());
    }     
    
    
    public SBOLDocument testDoc(String fileName) throws SBOLValidationException {
        try {
            File file = testFile(fileName);
            SBOLDocument doc = SBOLReader.read(file);
            doc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source");
            doc.setComplete(true);
            doc.setCreateDefaults(true);
        return doc;
        } catch (SBOLValidationException| IOException | org.sbolstandard.core2.SBOLConversionException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }    
    
    public File testFile(String name) {
        try {
            return new File(this.getClass().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }     
    
}
