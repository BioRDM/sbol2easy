/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SO;
import static ed.biordm.sbol.toolkit.transform.ComponentUtil.emptyDocument;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.RestrictionType;
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
public class ComponentFlattenerTest {
    
    public ComponentFlattenerTest() {
    }
    
    ComponentFlattener instance = new ComponentFlattener();
    
    SBOLDocument doc;
    
    @Before
    public void initSBOLDocument() throws IOException, SBOLValidationException, org.sbolstandard.core2.SBOLConversionException {

        doc = emptyDocument();
        
     }
    
    @Test
    public void flattensUsesChildDispIdAndDefForNaming() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        cp1.addRole(SequenceOntology.PROMOTER); 
                
        Component cp1i = cont.createComponent("cp1i", AccessType.PUBLIC, cp1.getPersistentIdentity());

        ComponentDefinition flat = instance.flattenDesign(cont, "flat", doc);
        
        String seq = instance.getSequenceElements(flat, Sequence.IUPAC_DNA)
                .get();
        
        assertEquals("AAA", seq);
        
        List<SequenceAnnotation> anns = flat.getSortedSequenceAnnotations();
        
        SequenceAnnotation ann = anns.get(0);
        assertEquals(cp1.getRoles(), ann.getRoles());
        assertEquals(cp1.getDisplayId(), ann.getName());
        assertEquals(cp1i.getDisplayId(), ann.getDisplayId());
        
        assertTrue(flat.getComponents().isEmpty());
    }
    
    @Test
    public void flattensCopiesChildrenCompAsAnnotations() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        cp1.addRole(SequenceOntology.PROMOTER); 
        
        Sequence sq2 = doc.createSequence("sq2", "CCCC", Sequence.IUPAC_DNA);
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addSequence(sq2);
        cp2.addRole(SequenceOntology.CDS);
        
        Sequence sq3 = doc.createSequence("sq3", "TT", Sequence.IUPAC_DNA);
        ComponentDefinition cp3 = doc.createComponentDefinition("cp3", ComponentDefinition.DNA_REGION);
        cp3.addSequence(sq3);
        cp3.addRole(SequenceOntology.TERMINATOR);
        
        Component cp1i = cont.createComponent("cp1i", AccessType.PUBLIC, cp1.getPersistentIdentity());
        Component cp2i = cont.createComponent("cp2i", AccessType.PUBLIC, cp2.getPersistentIdentity());
        Component cp3i = cont.createComponent("cp3i", AccessType.PUBLIC, cp3.getPersistentIdentity());
  
        cont.createSequenceConstraint("cs1", RestrictionType.PRECEDES, cp1i.getIdentity(), cp2i.getIdentity());
        cont.createSequenceConstraint("cs2", RestrictionType.PRECEDES, cp2i.getIdentity(), cp3i.getIdentity());

        ComponentDefinition flat = instance.flattenDesign(cont, "flat", doc);
        
        String seq = instance.getSequenceElements(flat, Sequence.IUPAC_DNA)
                .get();
        
        assertEquals("AAACCCCTT", seq);
        
        List<SequenceAnnotation> anns = flat.getSortedSequenceAnnotations();
        
        SequenceAnnotation ann = anns.get(0);
        assertEquals(cp1.getRoles(), ann.getRoles());
        
        ann = anns.get(1);
        assertEquals(cp2.getRoles(), ann.getRoles());
        
        ann = anns.get(2);
        assertEquals(cp3.getRoles(), ann.getRoles());
        
        assertTrue(flat.getComponents().isEmpty());
    }
    
    @Test
    public void flattensCopiesChildrensAnnotationsAsOwn() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        cp1.addRole(SequenceOntology.PROMOTER); 
        
        Sequence sq2 = doc.createSequence("sq2", "CCCC", Sequence.IUPAC_DNA);
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addSequence(sq2);
        cp2.addRole(SequenceOntology.CDS);
        
        SequenceAnnotation cp2ann1 = cp2.createSequenceAnnotation("AarI_2", "AarI_2", 1, 2);
        cp2ann1.addRole(SO("SO:0001687"));
        
        SequenceAnnotation cp2ann2 = cp2.createSequenceAnnotation("SapI_ATG_over", "SapI_ATG_over", 2, 4);
        cp2ann2.addRole(SO("SO:0001933"));
        cp2ann2.setName("SapI-ATG overhang");        
        
        Sequence sq3 = doc.createSequence("sq3", "TT", Sequence.IUPAC_DNA);
        ComponentDefinition cp3 = doc.createComponentDefinition("cp3", ComponentDefinition.DNA_REGION);
        cp3.addSequence(sq3);
        cp3.addRole(SequenceOntology.TERMINATOR);
        
        Component cp1i = cont.createComponent("cp1i", AccessType.PUBLIC, cp1.getPersistentIdentity());
        Component cp2i = cont.createComponent("cp2i", AccessType.PUBLIC, cp2.getPersistentIdentity());
        Component cp3i = cont.createComponent("cp3i", AccessType.PUBLIC, cp3.getPersistentIdentity());
  
        cont.createSequenceConstraint("cs1", RestrictionType.PRECEDES, cp1i.getIdentity(), cp2i.getIdentity());
        cont.createSequenceConstraint("cs2", RestrictionType.PRECEDES, cp2i.getIdentity(), cp3i.getIdentity());

        ComponentDefinition flat = instance.flattenDesign(cont, "flat", doc);
        
        String seq = instance.getSequenceElements(flat, Sequence.IUPAC_DNA)
                .get();
        
        assertEquals("AAACCCCTT", seq);
        
        List<SequenceAnnotation> anns = flat.getSortedSequenceAnnotations();
        
        SequenceAnnotation ann = anns.get(0);
        assertEquals(cp1.getRoles(), ann.getRoles());

        ann = anns.get(1);
        assertEquals(cp2ann1.getRoles(), ann.getRoles());
        
        ann = anns.get(2);
        assertEquals(cp2.getRoles(), ann.getRoles());
        
        ann = anns.get(3);
        assertEquals(cp2ann2.getRoles(), ann.getRoles());
    }
    
    
    @Test
    public void flattensCopiesConcreteChildrenComponentsAsOwnAnnotationToComponent() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        cp1.addRole(SequenceOntology.PROMOTER); 
        
        Sequence sq2 = doc.createSequence("sq2", "CCCC", Sequence.IUPAC_DNA);
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addSequence(sq2);
        cp2.addRole(SequenceOntology.CDS);
        
        ComponentDefinition subCp1 = doc.createComponentDefinition("subCp1", ComponentDefinition.DNA_REGION);
        subCp1.addRole(SO("SO:0001687"));
        Component subCp1i = cp2.createComponent("subCp1i", AccessType.PUBLIC, subCp1.getPersistentIdentity());
        
        SequenceAnnotation cp2ann1 = cp2.createSequenceAnnotation("AarI_2", "AarI_2", 1, 2);
        cp2ann1.setComponent(subCp1i.getPersistentIdentity());
        
        
        Component cp1i = cont.createComponent("cp1i", AccessType.PUBLIC, cp1.getPersistentIdentity());
        Component cp2i = cont.createComponent("cp2i", AccessType.PUBLIC, cp2.getPersistentIdentity());
  
        cont.createSequenceConstraint("cs1", RestrictionType.PRECEDES, cp1i.getIdentity(), cp2i.getIdentity());

        ComponentDefinition flat = instance.flattenDesign(cont, "flat", doc);
        
        String seq = instance.getSequenceElements(flat, Sequence.IUPAC_DNA)
                .get();
        
        assertEquals("AAACCCC", seq);
        
        List<SequenceAnnotation> anns = flat.getSortedSequenceAnnotations();
        
        SequenceAnnotation ann = anns.get(0);
        assertEquals(cp1.getRoles(), ann.getRoles());

        ann = anns.get(1);
        assertEquals(cp2ann1.getRoles(), ann.getRoles());
        assertEquals(cp2ann1.getDisplayId(), ann.getDisplayId());
        assertEquals(subCp1.getPersistentIdentity(), ann.getComponentDefinition().getPersistentIdentity());
        
        ann = anns.get(2);
        assertEquals(cp2.getRoles(), ann.getRoles());

        assertEquals(1, flat.getComponents().size());
        
    }    
    
    
    @Test
    public void flattensRecursivelyUntilConcreteComponents() throws Exception {
        
        // first subctomponent, abstract with inner structure
        ComponentDefinition sub1 = doc.createComponentDefinition("sub1", ComponentDefinition.DNA_REGION);
        sub1.addRole(SO("SO:0001500"));
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        cp1.addRole(SequenceOntology.PROMOTER); 
        
        Sequence sq2 = doc.createSequence("sq2", "CCCC", Sequence.IUPAC_DNA);
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addSequence(sq2);
        cp2.addRole(SequenceOntology.CDS);
        
        SequenceAnnotation cp2ann1 = cp2.createSequenceAnnotation("AarI_2", "AarI_2", 1, 2);
        cp2ann1.addRole(SO("SO:0001687"));
        
        ComponentDefinition subCp1 = doc.createComponentDefinition("subCp1", ComponentDefinition.DNA_REGION);
        subCp1.addRole(SO("SO:0001600"));
        Component subCp1i = cp2.createComponent("subCp1i", AccessType.PUBLIC, subCp1.getPersistentIdentity());        
        SequenceAnnotation cp2ann2 = cp2.createSequenceAnnotation("Over_2", "Over_2", 2, 4);
        cp2ann2.setComponent(subCp1i.getPersistentIdentity());
        
        
        Sequence sq3 = doc.createSequence("sq3", "TT", Sequence.IUPAC_DNA);
        ComponentDefinition cp3 = doc.createComponentDefinition("cp3", ComponentDefinition.DNA_REGION);
        cp3.addSequence(sq3);
        cp3.addRole(SequenceOntology.TERMINATOR);
        
        Component cp1i = sub1.createComponent("cp1i", AccessType.PUBLIC, cp1.getPersistentIdentity());
        Component cp2i = sub1.createComponent("cp2i", AccessType.PUBLIC, cp2.getPersistentIdentity());
        Component cp3i = sub1.createComponent("cp3i", AccessType.PUBLIC, cp3.getPersistentIdentity());
  
        sub1.createSequenceConstraint("cs1", RestrictionType.PRECEDES, cp1i.getIdentity(), cp2i.getIdentity());
        sub1.createSequenceConstraint("cs2", RestrictionType.PRECEDES, cp2i.getIdentity(), cp3i.getIdentity());
        
        // second subcomponent, concrete
        Sequence sq4 = doc.createSequence("sq4", "GGGG", Sequence.IUPAC_DNA);
        ComponentDefinition sub2 = doc.createComponentDefinition("sub2", ComponentDefinition.DNA_REGION);
        sub2.addSequence(sq4);
        sub2.addRole(SO("SO:0001800"));
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        Component sub1i = cont.createComponent("sub1i", AccessType.PUBLIC, sub1.getPersistentIdentity());
        Component sub2i = cont.createComponent("sub2i", AccessType.PUBLIC, sub2.getPersistentIdentity());
  
        cont.createSequenceConstraint("cs1", RestrictionType.PRECEDES, sub1i.getIdentity(), sub2i.getIdentity());
        

        ComponentDefinition flat = instance.flattenDesign(cont, "flat", doc);
        
        String seq = instance.getSequenceElements(flat, Sequence.IUPAC_DNA)
                .get();
        
        assertEquals("AAACCCCTTGGGG", seq);
        
        List<SequenceAnnotation> anns = flat.getSortedSequenceAnnotations();
        
        SequenceAnnotation ann = anns.get(0);
        assertEquals(cp1.getRoles(), ann.getRoles());
        
        ann = anns.get(1);
        assertEquals(sub1.getRoles(), ann.getRoles());
        
        ann = anns.get(2);
        assertEquals(cp2ann1.getRoles(), ann.getRoles());
        assertEquals(cp2ann1.getDisplayId(), ann.getDisplayId());
        
        ann = anns.get(3);
        assertEquals(cp2.getRoles(), ann.getRoles());

        ann = anns.get(4);
        assertEquals(cp2ann2.getRoles(), ann.getRoles());
        assertEquals(cp2ann2.getDisplayId(), ann.getDisplayId());
        assertEquals(subCp1.getPersistentIdentity(), ann.getComponentDefinition().getPersistentIdentity());
        
        ann = anns.get(5);
        assertEquals(cp3.getRoles(), ann.getRoles());
        
        ann = anns.get(6);
        assertEquals(sub2.getRoles(), ann.getRoles());        
        
        assertEquals(1, flat.getComponents().size());
    }
    
    @Test
    public void flattensRecursivelyIntoNewDocument() throws Exception {
        
        // first subctomponent, abstract with inner structure
        ComponentDefinition sub1 = doc.createComponentDefinition("sub1", ComponentDefinition.DNA_REGION);
        sub1.addRole(SO("SO:0001500"));
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        cp1.addRole(SequenceOntology.PROMOTER); 
        
        Sequence sq2 = doc.createSequence("sq2", "CCCC", Sequence.IUPAC_DNA);
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addSequence(sq2);
        cp2.addRole(SequenceOntology.CDS);
        
        SequenceAnnotation cp2ann1 = cp2.createSequenceAnnotation("AarI_2", "AarI_2", 1, 2);
        cp2ann1.addRole(SO("SO:0001687"));
        
        ComponentDefinition subCp1 = doc.createComponentDefinition("subCp1", ComponentDefinition.DNA_REGION);
        subCp1.addRole(SO("SO:0001600"));
        Component subCp1i = cp2.createComponent("subCp1i", AccessType.PUBLIC, subCp1.getPersistentIdentity());        
        SequenceAnnotation cp2ann2 = cp2.createSequenceAnnotation("Over_2", "Over_2", 2, 4);
        cp2ann2.setComponent(subCp1i.getPersistentIdentity());
        
        
        Sequence sq3 = doc.createSequence("sq3", "TT", Sequence.IUPAC_DNA);
        ComponentDefinition cp3 = doc.createComponentDefinition("cp3", ComponentDefinition.DNA_REGION);
        cp3.addSequence(sq3);
        cp3.addRole(SequenceOntology.TERMINATOR);
        
        Component cp1i = sub1.createComponent("cp1i", AccessType.PUBLIC, cp1.getPersistentIdentity());
        Component cp2i = sub1.createComponent("cp2i", AccessType.PUBLIC, cp2.getPersistentIdentity());
        Component cp3i = sub1.createComponent("cp3i", AccessType.PUBLIC, cp3.getPersistentIdentity());
  
        sub1.createSequenceConstraint("cs1", RestrictionType.PRECEDES, cp1i.getIdentity(), cp2i.getIdentity());
        sub1.createSequenceConstraint("cs2", RestrictionType.PRECEDES, cp2i.getIdentity(), cp3i.getIdentity());
        
        // second subcomponent, concrete
        Sequence sq4 = doc.createSequence("sq4", "GGGG", Sequence.IUPAC_DNA);
        ComponentDefinition sub2 = doc.createComponentDefinition("sub2", ComponentDefinition.DNA_REGION);
        sub2.addSequence(sq4);
        sub2.addRole(SO("SO:0001800"));
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        Component sub1i = cont.createComponent("sub1i", AccessType.PUBLIC, sub1.getPersistentIdentity());
        Component sub2i = cont.createComponent("sub2i", AccessType.PUBLIC, sub2.getPersistentIdentity());
  
        cont.createSequenceConstraint("cs1", RestrictionType.PRECEDES, sub1i.getIdentity(), sub2i.getIdentity());
        

        SBOLDocument dest = emptyDocument();
        
        ComponentDefinition flat = instance.flattenDesign(cont, "flat", dest);
        
        String seq = instance.getSequenceElements(flat, Sequence.IUPAC_DNA)
                .get();
        
        assertEquals("AAACCCCTTGGGG", seq);
        
        List<SequenceAnnotation> anns = flat.getSortedSequenceAnnotations();
        
        SequenceAnnotation ann = anns.get(0);
        assertEquals(cp1.getRoles(), ann.getRoles());
        
        ann = anns.get(1);
        assertEquals(sub1.getRoles(), ann.getRoles());
        
        ann = anns.get(2);
        assertEquals(cp2ann1.getRoles(), ann.getRoles());
        assertEquals(cp2ann1.getDisplayId(), ann.getDisplayId());
        
        ann = anns.get(3);
        assertEquals(cp2.getRoles(), ann.getRoles());

        ann = anns.get(4);
        assertEquals(cp2ann2.getRoles(), ann.getRoles());
        assertEquals(cp2ann2.getDisplayId(), ann.getDisplayId());
        assertEquals(subCp1.getPersistentIdentity(), ann.getComponentDefinition().getPersistentIdentity());
        
        ann = anns.get(5);
        assertEquals(cp3.getRoles(), ann.getRoles());
        
        ann = anns.get(6);
        assertEquals(sub2.getRoles(), ann.getRoles());        
        
        assertEquals(1, flat.getComponents().size());
        
        Component newC = flat.getComponents().iterator().next();
        //System.out.println(newC.getPersistentIdentity());
        //System.out.println(newC.getDefinition().getPersistentIdentity());
        //System.out.println(flat.getPersistentIdentity());
        
    }    
    
    
    @Test
    public void flattensAllTopLevelsFromADocument() throws Exception {
        
        // first subctomponent, abstract with inner structure
        ComponentDefinition sub1 = doc.createComponentDefinition("sub1", ComponentDefinition.DNA_REGION);
        sub1.addRole(SO("SO:0001500"));
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        cp1.addRole(SequenceOntology.PROMOTER); 
        
        Sequence sq2 = doc.createSequence("sq2", "CCCC", Sequence.IUPAC_DNA);
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addSequence(sq2);
        cp2.addRole(SequenceOntology.CDS);
        
        SequenceAnnotation cp2ann1 = cp2.createSequenceAnnotation("AarI_2", "AarI_2", 1, 2);
        cp2ann1.addRole(SO("SO:0001687"));
        
        ComponentDefinition subCp1 = doc.createComponentDefinition("subCp1", ComponentDefinition.DNA_REGION);
        subCp1.addRole(SO("SO:0001600"));
        Component subCp1i = cp2.createComponent("subCp1i", AccessType.PUBLIC, subCp1.getPersistentIdentity());        
        SequenceAnnotation cp2ann2 = cp2.createSequenceAnnotation("Over_2", "Over_2", 2, 4);
        cp2ann2.setComponent(subCp1i.getPersistentIdentity());
        
        
        Sequence sq3 = doc.createSequence("sq3", "TT", Sequence.IUPAC_DNA);
        ComponentDefinition cp3 = doc.createComponentDefinition("cp3", ComponentDefinition.DNA_REGION);
        cp3.addSequence(sq3);
        cp3.addRole(SequenceOntology.TERMINATOR);
        
        Component cp1i = sub1.createComponent("cp1i", AccessType.PUBLIC, cp1.getPersistentIdentity());
        Component cp2i = sub1.createComponent("cp2i", AccessType.PUBLIC, cp2.getPersistentIdentity());
        Component cp3i = sub1.createComponent("cp3i", AccessType.PUBLIC, cp3.getPersistentIdentity());
  
        sub1.createSequenceConstraint("cs1", RestrictionType.PRECEDES, cp1i.getIdentity(), cp2i.getIdentity());
        sub1.createSequenceConstraint("cs2", RestrictionType.PRECEDES, cp2i.getIdentity(), cp3i.getIdentity());
        
        // second subcomponent, concrete
        Sequence sq4 = doc.createSequence("sq4", "GGGG", Sequence.IUPAC_DNA);
        ComponentDefinition sub2 = doc.createComponentDefinition("sub2", ComponentDefinition.DNA_REGION);
        sub2.addSequence(sq4);
        sub2.addRole(SO("SO:0001800"));
        
        

        SBOLDocument dest = emptyDocument();

        List<ComponentDefinition> flattened = instance.flattenDesigns(doc, "_flat", dest);
        
        assertEquals(2, flattened.size());
        
        assertNotNull(dest.getComponentDefinition(sub1.getDisplayId()+"_flat",sub1.getVersion()));
        assertNotNull(dest.getComponentDefinition(sub2.getDisplayId()+"_flat",sub2.getVersion()));
        
        ComponentDefinition flat = dest.getComponentDefinition(sub1.getDisplayId()+"_flat",sub1.getVersion());
        String seq = instance.getSequenceElements(flat, Sequence.IUPAC_DNA)
                .get();
        
        assertEquals("AAACCCCTT", seq);
        
        
    }    
    
    @Test
    public void getJoinedSequenceElementsExtractsSimpleCases() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        assertTrue(instance.getJoinedSequenceElements(cont, Sequence.IUPAC_DNA).isEmpty());
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        
        assertTrue(instance.getJoinedSequenceElements(cp1, Sequence.IUPAC_PROTEIN).isEmpty());
        assertEquals("AAA", instance.getJoinedSequenceElements(cp1, Sequence.IUPAC_DNA).get());
                
        // proteinf first not dna
        Sequence sq2 = doc.createSequence("sq2p", "ALA", Sequence.IUPAC_PROTEIN);
        cp1.addSequence(sq2);
        assertEquals("ALA", instance.getJoinedSequenceElements(cp1, Sequence.IUPAC_PROTEIN).get());

    }    
    
    @Test
    public void getJoinedSequenceElementsJoinsRecursively() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        
        // proteinf first not dna
        Sequence sq2 = doc.createSequence("sq2p", "ALA", Sequence.IUPAC_PROTEIN);
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addSequence(sq2);

        Sequence sq3 = doc.createSequence("sq3", "TT", Sequence.IUPAC_DNA);
        ComponentDefinition cp3 = doc.createComponentDefinition("cp3", ComponentDefinition.DNA_REGION);
        cp3.addSequence(sq3);
        
        Component i1 = cont.createComponent("cp1", AccessType.PUBLIC, cp1.getIdentity());
        Component i2 = cont.createComponent("cp2", AccessType.PUBLIC, cp2.getIdentity());
        Component i3 = cont.createComponent("cp3", AccessType.PUBLIC, cp3.getIdentity());        
        
        cont.createSequenceConstraint("cs1", RestrictionType.PRECEDES, i1.getPersistentIdentity(), i2.getPersistentIdentity());
        cont.createSequenceConstraint("cs2", RestrictionType.PRECEDES, i2.getPersistentIdentity(), i3.getPersistentIdentity());
        
        
        try {
            instance.getJoinedSequenceElements(cont, Sequence.IUPAC_DNA);
        } catch (IllegalArgumentException e) {
            // expected as cp2 does not have dna
        }
        
        sq2 = doc.createSequence("sq2", "C", Sequence.IUPAC_DNA);
        cp2.addSequence(sq2);        
        
        String seq = instance.getJoinedSequenceElements(cont, Sequence.IUPAC_DNA).get();
        assertEquals("AAACTT", seq); 
        
        Sequence sq4 = doc.createSequence("sq4", "GG", Sequence.IUPAC_DNA);
        ComponentDefinition cp4 = doc.createComponentDefinition("cp4", ComponentDefinition.DNA_REGION);
        cp4.addSequence(sq4);
        
        ComponentDefinition cont2 = doc.createComponentDefinition("cont2", ComponentDefinition.DNA_REGION);
        Component i4 = cont2.createComponent("cp4", AccessType.PUBLIC, cont.getIdentity());
        Component i5 = cont2.createComponent("cp5", AccessType.PUBLIC, cp4.getIdentity());
        
        cont2.createSequenceConstraint("cs3", RestrictionType.PRECEDES, i4.getPersistentIdentity(), i5.getPersistentIdentity());
        
        seq = instance.getJoinedSequenceElements(cont2, Sequence.IUPAC_DNA).get();
        assertEquals("AAACTTGG", seq); 
        
    }    
    
    @Test
    public void getJoinedSequenceLengthWorksWithSimpleCases() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        try {
            instance.getJoinedSequenceLength(cont, Sequence.IUPAC_DNA);
            fail();
        } catch (IllegalArgumentException e) {}
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        
        assertEquals(3, instance.getJoinedSequenceLength(cp1, Sequence.IUPAC_DNA));
                
    }    
    
    @Test
    public void getJoinedSequenceLengthCalculatesRecursively() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);
        
        // proteinf first not dna
        Sequence sq2 = doc.createSequence("sq2p", "ALA", Sequence.IUPAC_PROTEIN);
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addSequence(sq2);

        Sequence sq3 = doc.createSequence("sq3", "TT", Sequence.IUPAC_DNA);
        ComponentDefinition cp3 = doc.createComponentDefinition("cp3", ComponentDefinition.DNA_REGION);
        cp3.addSequence(sq3);
        
        Component i1 = cont.createComponent("cp1", AccessType.PUBLIC, cp1.getIdentity());
        Component i2 = cont.createComponent("cp2", AccessType.PUBLIC, cp2.getIdentity());
        Component i3 = cont.createComponent("cp3", AccessType.PUBLIC, cp3.getIdentity());        
        
        cont.createSequenceConstraint("cs1", RestrictionType.PRECEDES, i1.getPersistentIdentity(), i2.getPersistentIdentity());
        cont.createSequenceConstraint("cs2", RestrictionType.PRECEDES, i2.getPersistentIdentity(), i3.getPersistentIdentity());
        
        
        try {
            instance.getJoinedSequenceLength(cont, Sequence.IUPAC_DNA);
        } catch (IllegalArgumentException e) {
            // expected as cp2 does not have dna
        }
        
        sq2 = doc.createSequence("sq2", "C", Sequence.IUPAC_DNA);
        cp2.addSequence(sq2);        
        
        int len = instance.getJoinedSequenceLength(cont, Sequence.IUPAC_DNA);
        assertEquals("AAACTT".length(), len); 
        
        Sequence sq4 = doc.createSequence("sq4", "GG", Sequence.IUPAC_DNA);
        ComponentDefinition cp4 = doc.createComponentDefinition("cp4", ComponentDefinition.DNA_REGION);
        cp4.addSequence(sq4);
        
        ComponentDefinition cont2 = doc.createComponentDefinition("cont2", ComponentDefinition.DNA_REGION);
        Component i4 = cont2.createComponent("cp4", AccessType.PUBLIC, cont.getIdentity());
        Component i5 = cont2.createComponent("cp5", AccessType.PUBLIC, cp4.getIdentity());
        
        cont2.createSequenceConstraint("cs3", RestrictionType.PRECEDES, i4.getPersistentIdentity(), i5.getPersistentIdentity());
        
        len = instance.getJoinedSequenceLength(cont2, Sequence.IUPAC_DNA);
        assertEquals("AAACTTGG".length(), len); 
        
    }    
    
    
    @Test
    public void convertComponentsToFeaturesAddSequenceAnnotation() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("comp1", ComponentDefinition.DNA_REGION);
        cp1.addRole(SequenceOntology.PROMOTER);
        cp1.addSequence(sq1);

        // some inner noise
        ComponentDefinition subD = doc.createComponentDefinition("sub", ComponentDefinition.DNA_REGION);
        subD.addRole(SequenceOntology.PROMOTER);
        Component sub = cp1.createComponent("sub", AccessType.PUBLIC, subD.getIdentity());
        SequenceAnnotation ann = cp1.createSequenceAnnotation("ann1", "ann1", 1, 2, OrientationType.REVERSECOMPLEMENT);
        ann.setComponent(sub.getPersistentIdentity());        
        ann  = cp1.createSequenceAnnotation("ann2", "ann2", 1, 3);
        ann.addRole(SequenceOntology.CDS);
        
        Sequence sq2 = doc.createSequence("sq2", "CC", Sequence.IUPAC_DNA);
        ComponentDefinition cp2 = doc.createComponentDefinition("comp2", ComponentDefinition.DNA_REGION);
        cp2.setName("Comp 2");
        cp2.addRole(SequenceOntology.CDS);
        cp2.addSequence(sq2);

        Sequence sq3 = doc.createSequence("sq3", "TTTT", Sequence.IUPAC_DNA);
        ComponentDefinition cp3 = doc.createComponentDefinition("comp3", ComponentDefinition.DNA_REGION);
        cp3.addSequence(sq3);
        cp3.addRole(SequenceOntology.TERMINATOR);
        cp3.createAnnotation(CommonAnnotations.SBH_DESCRIPTION, new URI("https://biodare.ed.ac.uk"));

        
        List<Component> list = List.of(
                cont.createComponent("cp1", AccessType.PUBLIC, cp1.getIdentity()),
                cont.createComponent("cp2", AccessType.PUBLIC, cp2.getIdentity()),
                cont.createComponent("cp3", AccessType.PUBLIC, cp3.getIdentity())
        );
        
        ComponentDefinition dest = doc.createComponentDefinition("dest", ComponentDefinition.DNA_REGION);
        
        instance.convertComponentsToFeatures(list, dest);
        
        ann = dest.getSequenceAnnotation("cp1");
        assertNotNull(ann);
        assertEquals(1, ((Range)ann.getLocation("cp1")).getStart());
        assertEquals(3, ((Range)ann.getLocation("cp1")).getEnd());
        assertEquals(Set.of(SequenceOntology.PROMOTER), ann.getRoles());
        assertEquals("comp1", ann.getName());

        ann = dest.getSequenceAnnotation("cp2");
        assertNotNull(ann);
        assertEquals(4, ((Range)ann.getLocation("cp2")).getStart());
        assertEquals(5, ((Range)ann.getLocation("cp2")).getEnd());
        assertEquals(Set.of(SequenceOntology.CDS), ann.getRoles());
        assertEquals("Comp 2", ann.getName());

        ann = dest.getSequenceAnnotation("cp3");
        assertNotNull(ann);
        assertEquals(6, ((Range)ann.getLocation("cp3")).getStart());
        assertEquals(9, ((Range)ann.getLocation("cp3")).getEnd());
        assertEquals(Set.of(SequenceOntology.TERMINATOR), ann.getRoles());
        assertEquals("comp3", ann.getName());  
        assertEquals(new URI("https://biodare.ed.ac.uk"), ann.getAnnotation(CommonAnnotations.SBH_DESCRIPTION).getURIValue());
        
        assertEquals(3, dest.getSequenceAnnotations().size());
        
    }


    @Test
    public void copySequenceFeaturesCopiesSeque() throws Exception {
        
        ComponentDefinition cont = doc.createComponentDefinition("cont", ComponentDefinition.DNA_REGION);
        
        Sequence sq1 = doc.createSequence("sq1", "AAA", Sequence.IUPAC_DNA);
        ComponentDefinition cp1 = doc.createComponentDefinition("cp1", ComponentDefinition.DNA_REGION);
        cp1.addSequence(sq1);

        ComponentDefinition subD = doc.createComponentDefinition("sub", ComponentDefinition.DNA_REGION);
        subD.addRole(SequenceOntology.PROMOTER);
        Component sub = cp1.createComponent("sub", AccessType.PUBLIC, subD.getIdentity());
        SequenceAnnotation ann = cp1.createSequenceAnnotation("ann1", "ann1", 1, 2, OrientationType.REVERSECOMPLEMENT);
        ann.setComponent(sub.getPersistentIdentity());
        
        ann  = cp1.createSequenceAnnotation("ann2", "ann2", 1, 3);
        ann.addRole(SequenceOntology.CDS);
        
        Sequence sq2 = doc.createSequence("sq2", "CC", Sequence.IUPAC_DNA);
        ComponentDefinition cp2 = doc.createComponentDefinition("cp2", ComponentDefinition.DNA_REGION);
        cp2.addSequence(sq2);

        Sequence sq3 = doc.createSequence("sq3", "TTTT", Sequence.IUPAC_DNA);
        ComponentDefinition cp3 = doc.createComponentDefinition("cp3", ComponentDefinition.DNA_REGION);
        cp3.addSequence(sq3);

        ann  = cp3.createSequenceAnnotation("ann3", "ann3", 2, 4);
        ann.addRole(SequenceOntology.TERMINATOR);
        ann.createAnnotation(CommonAnnotations.SBH_DESCRIPTION, new URI("https://biodare.ed.ac.uk"));
        
        List<Component> list = List.of(
                cont.createComponent("cp1", AccessType.PUBLIC, cp1.getIdentity()),
                cont.createComponent("cp2", AccessType.PUBLIC, cp2.getIdentity()),
                cont.createComponent("cp3", AccessType.PUBLIC, cp3.getIdentity())
        );
        
        ComponentDefinition dest = doc.createComponentDefinition("dest", ComponentDefinition.DNA_REGION);
        
        instance.copySequenceFeatures(list, dest);
        
        ann = dest.getSequenceAnnotation("ann1");
        assertNotNull(ann);
        assertEquals(ann.getComponent().getDefinitionIdentity(), subD.getPersistentIdentity());
        assertEquals(1, ((Range)ann.getLocation("ann1")).getStart());
        assertNotNull(dest.getComponent("sub"));

        ann = dest.getSequenceAnnotation("ann2");
        assertNotNull(ann);
        assertEquals(Set.of(SequenceOntology.CDS), ann.getRoles());
        assertEquals(1, ((Range)ann.getLocation("ann2")).getStart());
        assertEquals(3, ((Range)ann.getLocation("ann2")).getEnd());

        ann = dest.getSequenceAnnotation("ann3");
        assertNotNull(ann);
        assertEquals(Set.of(SequenceOntology.TERMINATOR), ann.getRoles());
        assertEquals(7, ((Range)ann.getLocation("ann3")).getStart());
        assertEquals(9, ((Range)ann.getLocation("ann3")).getEnd());
        assertEquals(new URI("https://biodare.ed.ac.uk"), ann.getAnnotation(CommonAnnotations.SBH_DESCRIPTION).getURIValue());
        
        
    }
    
    
}
