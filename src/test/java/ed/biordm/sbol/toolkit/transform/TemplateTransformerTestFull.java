/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceConstraint;

/**
 *
 * @author jhay
 */
public class TemplateTransformerTestFull {

    TemplateTransformer templateTransformer = new TemplateTransformer();
    SBOLDocument doc;
    static String SEQUENCE_ONTO_PREF = "http://identifiers.org/so/";

    @Before
    public void generateSBOLDocument() throws IOException, SBOLValidationException, SBOLConversionException {
        String fName = "cyano_full_template_2.xml";
        File file = new File(getClass().getResource(fName).getFile());

        try {
            doc = SBOLReader.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        doc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
        doc.setComplete(true);
        doc.setCreateDefaults(true);
    }

    /**
     * Test of instantiateFromTemplate method, of class TemplateTransformer.
     */
    @Test
    public void testInstantiateFromTemplate() throws Exception {
        SBOLDocument doc = new SBOLDocument();
        String defaultURIPrefix = "http://bio.ed.ac.uk/a_mccormick/cyano_source/";

        doc.setDefaultURIprefix(defaultURIPrefix);
        doc.setComplete(true);
        doc.setCreateDefaults(true);

        // Test creation of new component definition based on DNA_REGION template
        ComponentDefinition region = doc.createComponentDefinition("region1", "1.0.0", ComponentDefinition.DNA_REGION);
        //TopLevel tl = doc.createCopy(region, "region2");

        String newName = "region2";
        String newVersion = "1.0.1";
        String newDescription = "Deep copy of DNA_REGION component";

        ComponentDefinition newCmp = templateTransformer.instantiateFromTemplate(region,
                newName, newVersion, newDescription, doc);

        assertEquals(newName, newCmp.getDisplayId());
        assertEquals(newVersion, newCmp.getVersion());
        assertEquals(newDescription, newCmp.getDescription());
        assertEquals(region.getTypes(), newCmp.getTypes());
        assertEquals(region.getRoles(), newCmp.getRoles());
    }

    /**
     * Test if the sub-components from the AmpR component definition are properly
     * copied to the new parent plasmid.
     */
    @Test
    public void testBackboneSubComponents() throws Exception {
        String backboneDispId = "backbone";
        String version = "1.0.0";

        ComponentDefinition backbone = doc.getComponentDefinition(backboneDispId, version);
        assertNotNull(backbone);

        for (Component cmp : backbone.getComponents()) {
            //System.out.println(cmp.getDisplayId());
        }

        for (SequenceAnnotation seqAnn : backbone.getSequenceAnnotations()) {
            //System.out.println(seqAnn.getDisplayId());
        }

        String templateDispId = "cyano_codA_Km";
        String newName = "johnny_cyano_codA_Km";
        String desc = "test plasmid from template";

        ComponentDefinition templatePlasmid = doc.getComponentDefinition(templateDispId, version);
        ComponentDefinition newCmp = templateTransformer.instantiateFromTemplate(templatePlasmid,
                newName, version, desc, doc);

        String backboneCmpDispId = "backbone";
        ComponentDefinition newAmpR = newCmp.getComponent(backboneCmpDispId).getDefinition();

        for (Component cmp : newAmpR.getComponents()) {
            //System.out.println(cmp.getDisplayId());
            assertTrue(backbone.getComponents().contains(cmp));
        }

        for (SequenceAnnotation seqAnn : newAmpR.getSequenceAnnotations()) {
            //System.out.println(seqAnn.getDisplayId());
            assertTrue(backbone.getSequenceAnnotations().contains(seqAnn));
        }

        for (Component cmp : newCmp.getSortedComponents()) {
            System.out.println(cmp.getDisplayId());

            ComponentDefinition curCmpDef = cmp.getDefinition();
            System.out.println(curCmpDef.getDisplayId());

            for (SequenceAnnotation seqAnn : curCmpDef.getSequenceAnnotations()) {
                System.out.println(seqAnn.getDisplayId());
            }
        }
    }

    @Test
    public void testFlattenSequences() throws Exception {
        // Get original sll00199 component definition for comparison
        assertNotNull(doc);

        ComponentDefinition sll00199PlasmidFlat = doc.getComponentDefinition("sll00199_codA_Km_flat", "1.0.0");
        assertNotNull(sll00199PlasmidFlat);

        ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("sll00199_codA_Km", "1.0.0");
        assertNotNull(sll00199Plasmid);

        String newName = "sll00199_codA_Km".concat("_johnny");
        // Add the flattened sequences to the parent component's SequenceAnnotation components
        /*
        // N.B.: had to rename 'BsmBI' and 'BsmBI_2' sequence annotation objects
        //       display IDs in 'backbone_end' component definition, because
        //       they violate the unique naming rule (since 'backbone' component
        //       definition also contains the same annotations)
        */
        ComponentDefinition newPlasmidFlat = templateTransformer.flattenSequences(sll00199Plasmid, newName.concat("_flat"), doc);
        newPlasmidFlat.addRole(new URI(SEQUENCE_ONTO_PREF+"SO:0000637"));

        System.out.println(newPlasmidFlat.getDescription());

        // Check component instances match
        for (Component cmp : sll00199PlasmidFlat.getSortedComponents()) {
            System.out.println(cmp.getDisplayId());
            assertNotNull(newPlasmidFlat.getComponent(cmp.getDisplayId()));
        }

        for (Component cmp : newPlasmidFlat.getSortedComponents()) {
            System.out.println(cmp.getDisplayId());
            assertNotNull(sll00199PlasmidFlat.getComponent(cmp.getDisplayId()));
        }

        // Check sequence constraints match
        Set<SequenceConstraint> sll00199SFlatSCs = sll00199PlasmidFlat.getSequenceConstraints();
        Set<SequenceConstraint> npFlatSCs = newPlasmidFlat.getSequenceConstraints();

        for (SequenceConstraint sc : npFlatSCs) {
            SequenceConstraint npSc = newPlasmidFlat.getSequenceConstraint(sc.getDisplayId());
            //assertEquals(sc.getSubject().getDefinition().getWasDerivedFroms(), npSc.getSubject().getDefinition().getWasDerivedFroms());
            assertNotNull(sll00199PlasmidFlat.getSequenceConstraint(npSc.getDisplayId()));
        }

        for (SequenceConstraint sc : sll00199SFlatSCs) {
            SequenceConstraint sll00199Sc = sll00199PlasmidFlat.getSequenceConstraint(sc.getDisplayId());
            //assertNotNull(npSc);
            //assertEquals(sc.getObject().getDefinition().getWasDerivedFroms(), npSc.getObject().getDefinition().getWasDerivedFroms());
            //assertEquals(sc.getSubject().getDefinition().getWasDerivedFroms(), npSc.getSubject().getDefinition().getWasDerivedFroms());
            assertNotNull(newPlasmidFlat.getSequenceConstraint(sll00199Sc.getDisplayId()));
        }

        Set<SequenceAnnotation> sll00199PlasmidFlatSAs = sll00199PlasmidFlat.getSequenceAnnotations();
        Set<SequenceAnnotation> npFlatSAs = newPlasmidFlat.getSequenceAnnotations();

        // Get sequence annos and verify they match in new component
        for (SequenceAnnotation seqAnn : npFlatSAs) {
            // How to verify these objets are equivalent in each plasmid?
            System.out.println(seqAnn.getIdentity());
            System.out.println(seqAnn.getComponentIdentity());
            /*
            * The new plasmid contains an additional 'ann3' and 'ann4' custom annotations
            * which the original flattened plasmid does not contain!
            */
            if (seqAnn.getDisplayId().equals("ann3") || seqAnn.getDisplayId().equals("ann4")) {
                System.out.println("These custom annotations are not present in original flattened plasmid!");
            } else {
                // Waaaaay more sequence annotations in new plasmid - but is that correct?
                //assertNotNull(sll00199PlasmidFlat.getSequenceAnnotation(seqAnn.getDisplayId()));   
            }
        }

        // Get sequence annos and verify they match in new component
        for (SequenceAnnotation seqAnn : sll00199PlasmidFlatSAs) {
            // How to verify these objets are equivalent in each plasmid?
            System.out.println(seqAnn.getIdentity());
            System.out.println(seqAnn.getComponentIdentity());
            assertNotNull(newPlasmidFlat.getSequenceAnnotation(seqAnn.getDisplayId()));
        }
    }

    @Test
    public void testAddCustomSequenceAnnotations() throws Exception {
        // Get original sll00199 component definition for comparison
        assertNotNull(doc);

        // Copy Template
        ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("sll00199_codA_Km", "1.0.0");
        assertNotNull(sll00199Plasmid);

        ComponentDefinition sll00199PlasmidFlat = doc.getComponentDefinition("sll00199_codA_Km_flat", "1.0.0");
        assertNotNull(sll00199PlasmidFlat);

        Map<Component, List<Sequence>> cmpSeqMap = new HashMap<>();
        //cmpSeqMap = templateTransformer.rebuildSequences(sll00199Plasmid, sll00199Plasmid, doc, cmpSeqMap);
        templateTransformer.addCustomSequenceAnnotations(sll00199Plasmid, cmpSeqMap);
        
        Set<SequenceAnnotation> sll00199PlasmidFlatSAs = sll00199PlasmidFlat.getSequenceAnnotations();
        Set<SequenceAnnotation> npFlatSAs = sll00199Plasmid.getSequenceAnnotations();

        // will be empty because no sub-components have been added to parent yet
        assertEquals(0, npFlatSAs.size());

        cmpSeqMap = templateTransformer.rebuildSequences(sll00199Plasmid, sll00199Plasmid, doc, cmpSeqMap);
        templateTransformer.addCustomSequenceAnnotations(sll00199Plasmid, cmpSeqMap);
        npFlatSAs = sll00199Plasmid.getSequenceAnnotations();

        assertEquals(33, npFlatSAs.size());

        // Get sequence annos and verify they match in new component
        for (SequenceAnnotation seqAnn : npFlatSAs) {
            // How to verify these objets are equivalent in each plasmid?
            System.out.println(seqAnn.getIdentity());
            System.out.println(seqAnn.getComponentIdentity());
        }
    }

    /*@Test
    public void testCreateNewFlatPlasmid() throws Exception {
        // Get original sll00199 component definition for comparison
        assertNotNull(doc);

        ComponentDefinition sll00199PlasmidFlat = doc.getComponentDefinition("sll00199_codA_Km_flat", "1.0.0");
        assertNotNull(sll00199PlasmidFlat);

        // Copy Template
        ComponentDefinition templatePlasmid = doc.getComponentDefinition("cyano_codA_Km", "1.0.0");
        assertNotNull(templatePlasmid);

        String newName = "sll00199_codA_Km_johnny";
        String version = "1.0.0";
        String description = "Test plasmid creation";
        ComponentDefinition newPlasmid = templateTransformer.instantiateFromTemplate(templatePlasmid, newName, version, description, doc);

        ComponentDefinition originD = doc.getComponentDefinition("ori", version);
        Component origin =  newPlasmid.createComponent("ori_instance", AccessType.PUBLIC, originD.getIdentity());
        //SequenceAnnotation an = newPlasmid.createSequenceAnnotation("ori", "ori", 1228, 1816);
        //an.setComponent(origin.getIdentity());

        // Left sequence elements
        // doc.getSequence("sll00199_left_seq")
        String ltSeq = "caaggcaaaaccaccgttatcagcagaacgacggcgggaaaaaatgattaaacgaaaaaatttgcaaggattcatagcggttgcccaatctaactcagggagcgacttcagcccacaaaaaacaccactgggcctactgggctattcccattatcatctacattgaagggatagcaagctaatttttatgacggcgatcgccaaaaacaaagaaaattcagcaattaccgtgggtagcaaaaaatccccatctaaagttcagtaaatatagctagaacaaccaagcattttcggcaaagtactattcagatagaacgagaaatgagcttgttctatccgcccggggctgaggctgtataatctacgacgggctgtcaaacattgtgataccatgggcagaagaaaggaaaaacgtccctgatcgcctttttgggcacggagtagggcgttaccccggcccgttcaaccacaagtccctatAGATACAATCGCCAAGAAGT";
        String genericCmpId = "left";

        templateTransformer.concretizePart(newPlasmid, genericCmpId, "test_left",
                ltSeq, doc);

        // Right sequence elements
        // doc.getSequence("sll00199_right_seq")
        String rtSeq = "tcagccagctcaatctgtgtgtcgttgatttaagcttaatgctacggggtctgtctccaactccctcagcttctcgcaatggcaaggcaaataatgtttctcttgctgagtagatgttcaggaggacggatcgaaagtctacaaaacagattcttgaccaagccatctacttagaaaaacttctgcgttttggcgatcgcatcttttaagcgagatgcgatttttttgtccattagtttgtattttaatactcttttgttgtttgatttcgtccaagcttttcttggtatgtgggatcttccgtgcccaaaattttatcccagaaagtgaaatatagtcatttcaattaacgatgagagaatttaatgtaaaattatggagtgtacaaaatgaacaggtttaaacaatggcttacagtttagatttaaggcaaagggtagtagcttatatagaagctggaggaaaaataactgaggcttccaagatatataaaataggaaaagcctcgatatacagatggttaaatagagtagatttaagcccaacaaaagtagagcgtcgccatagg";
        genericCmpId = "right";

        templateTransformer.concretizePart(newPlasmid, genericCmpId, "test_right",
                rtSeq, doc);


        ComponentDefinition newLtCD = newPlasmid.getComponent("test_left").getDefinition();
        ComponentDefinition newRtCD = newPlasmid.getComponent("test_right").getDefinition();

        Set<Sequence> newLtCDSeqs = newLtCD.getSequences();
        Set<Sequence> newRtCDSeqs = newRtCD.getSequences();

        // assuming only one sequence per flank
        String newLtSeqEls = ((Sequence)newLtCDSeqs.toArray()[0]).getElements();
        String newRtSeqEls = ((Sequence)newRtCDSeqs.toArray()[0]).getElements();

        assertEquals(ltSeq, newLtSeqEls);

        assertEquals(rtSeq, newRtSeqEls);

        Set<SequenceAnnotation> npSAs = newPlasmid.getSequenceAnnotations();

        // Get sequence annos and verify they match in new component
        for (SequenceAnnotation seqAnn : npSAs) {
            System.out.println(seqAnn.getIdentity());
            System.out.println(seqAnn.getComponentIdentity());
        }

        // Add the flattened sequences to the parent component's SequenceAnnotation components
        ComponentDefinition newPlasmidFlat = templateTransformer.flattenSequences(newPlasmid, newName.concat("_flat"), doc);
        newPlasmidFlat.addRole(new URI(SEQUENCE_ONTO_PREF+"SO:0000637"));

        // Add arbitrary(?) SequenceAnnotations. What are the rules for these annotations?
        Component seqCmp = newPlasmidFlat.getComponent("ampR");
        //an.setComponent(seqCmp.getIdentity());

        seqCmp = newPlasmidFlat.getComponent("test_left");
        //an.setComponent(seqCmp.getIdentity());

        // Check component instances match
        for (Component cmp : sll00199PlasmidFlat.getSortedComponents()) {
            System.out.println(cmp.getDisplayId());
            //assertNotNull(newPlasmidFlat.getComponent(cmp.getDisplayId()));
        }

        for (Component cmp : newPlasmidFlat.getSortedComponents()) {
            System.out.println(cmp.getDisplayId());
            //assertNotNull(sll00199PlasmidFlat.getComponent(cmp.getDisplayId()));
        }

        Set<SequenceAnnotation> sll00199PlasmidFlatSAs = sll00199PlasmidFlat.getSequenceAnnotations();
        Set<SequenceAnnotation> npFlatSAs = newPlasmidFlat.getSequenceAnnotations();

        // Get sequence annos and verify they match in new component
        for (SequenceAnnotation seqAnn : npFlatSAs) {
            // How to verify these objets are equivalent in each plasmid?
            System.out.println(seqAnn.getIdentity());
            System.out.println(seqAnn.getComponentIdentity());
        }

        //assertEquals(sll00199PlasmidFlatSAs.size(), npFlatSAs.size());

        // why does this method return 'NNN...' strings for new plasmid?
        // something to do with the SequenceAnnotations having null linked components.
        // But can't set the components on the SAs because of circular reference error?
        System.out.println(sll00199PlasmidFlat.getImpliedNucleicAcidSequence());
        System.out.println(newPlasmidFlat.getImpliedNucleicAcidSequence());

        // This test fails
        assertEquals(sll00199PlasmidFlat.getImpliedNucleicAcidSequence().length(),
                newPlasmidFlat.getImpliedNucleicAcidSequence().length());

        Set<Sequence> sll00199PlasmidFlatSeqs = sll00199PlasmidFlat.getSequences();
        Set<Sequence> npFlatSeqs = newPlasmidFlat.getSequences();
        
        String npFlatSeqEls = ((Sequence)npFlatSeqs.toArray()[0]).getElements();
        String sll00199PlasmidFlatSeqEls = ((Sequence)sll00199PlasmidFlatSeqs.toArray()[0]).getElements();

        assertEquals(npFlatSeqEls, sll00199PlasmidFlatSeqEls);

        // Check sequence constraints match
        Set<SequenceConstraint> sll00199SFlatSCs = sll00199PlasmidFlat.getSequenceConstraints();
        Set<SequenceConstraint> npFlatSCs = newPlasmidFlat.getSequenceConstraints();

        for (SequenceConstraint sc : npFlatSCs) {
            SequenceConstraint npSc = newPlasmid.getSequenceConstraint(sc.getDisplayId());
            //assertEquals(sc.getSubject().getDefinition().getWasDerivedFroms(), npSc.getSubject().getDefinition().getWasDerivedFroms());
            assertNotNull(sll00199PlasmidFlat.getSequenceConstraint(npSc.getDisplayId()));
        }

        for (SequenceConstraint sc : sll00199SFlatSCs) {
            SequenceConstraint sll00199Sc = sll00199PlasmidFlat.getSequenceConstraint(sc.getDisplayId());
            //assertNotNull(npSc);
            //assertEquals(sc.getObject().getDefinition().getWasDerivedFroms(), npSc.getObject().getDefinition().getWasDerivedFroms());
            //assertEquals(sc.getSubject().getDefinition().getWasDerivedFroms(), npSc.getSubject().getDefinition().getWasDerivedFroms());
            assertNotNull(newPlasmid.getSequenceConstraint(sll00199Sc.getDisplayId()));
        }
    }*/
}
