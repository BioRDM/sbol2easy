/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import static ed.biordm.sbol.toolkit.transform.CommonAnnotations.SO;
import static ed.biordm.sbol.toolkit.transform.SynBioTamer.DEFAULT_NAMESPACE;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.sbolstandard.core2.AccessType;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.OrientationType;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.RestrictionType;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceConstraint;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author jhay
 */
public class TemplateTransformerTest {

    TemplateTransformer templateTransformer = new TemplateTransformer();
    SBOLDocument doc;
    static String SEQUENCE_ONTO_PREF = "http://identifiers.org/so/";

    @Before
    public void generateSBOLDocument() throws IOException, SBOLValidationException, SBOLConversionException {
        String fName = "cyano_template.xml";
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
    public void testAmprSubComponents() throws Exception {
        String ampRDispId = "ampr_origin";
        String version = "1.0.0";

        ComponentDefinition ampR = doc.getComponentDefinition(ampRDispId, version);
        assertNotNull(ampR);

        for (Component cmp : ampR.getComponents()) {
            //System.out.println(cmp.getDisplayId());
        }

        for (SequenceAnnotation seqAnn : ampR.getSequenceAnnotations()) {
            //System.out.println(seqAnn.getDisplayId());
        }

        String templateDispId = "cyano_codA_Km";
        String newName = "johnny_cyano_codA_Km";
        String desc = "test plasmid from template";

        ComponentDefinition templatePlasmid = doc.getComponentDefinition(templateDispId, version);
        ComponentDefinition newCmp = templateTransformer.instantiateFromTemplate(templatePlasmid,
                newName, version, desc, doc);

        String ampRCmpDispId = "ampR";
        ComponentDefinition newAmpR = newCmp.getComponent(ampRCmpDispId).getDefinition();

        for (Component cmp : newAmpR.getComponents()) {
            //System.out.println(cmp.getDisplayId());
            assertTrue(ampR.getComponents().contains(cmp));
        }

        for (SequenceAnnotation seqAnn : newAmpR.getSequenceAnnotations()) {
            //System.out.println(seqAnn.getDisplayId());
            assertTrue(ampR.getSequenceAnnotations().contains(seqAnn));
        }

        // Test flattened sequence annotations
        ComponentDefinition newCmpFlat = templateTransformer.flattenSequences(newCmp, "johnny_cyano_codA_Km_flat", doc);

        int start = 1;
        int seqAnnCount = 1;
        for (Component cmp : newCmp.getSortedComponents()) {
            //System.out.println(cmp.getDisplayId());

            ComponentDefinition curCmpDef = cmp.getDefinition();
            //System.out.println(curCmpDef.getDisplayId());

            for (SequenceAnnotation seqAnn : curCmpDef.getSequenceAnnotations()) {
                System.out.println(seqAnn.getDisplayId());

                for (Location loc : seqAnn.getLocations()) {
                    Range range = (Range) loc;
                    //System.out.println(range.getStart());
                    //System.out.println(range.getEnd());

                    //String seqAnnDispId = newCmp

                    SequenceAnnotation seqAnn2 = newCmp.createSequenceAnnotation(seqAnn.getDisplayId(), seqAnn.getDisplayId(), start + range.getStart() - 1, start + range.getEnd() - 1, OrientationType.INLINE);
                    //seqAnn2.setComponent(cmp.getIdentity());
                }
            }
        }

        String[] saDisplayIdsArr = new String[]{"ori", "AmpR_prom", "AmpR",};
        Set<String> saDisplayIds = new HashSet<>( Arrays.asList(saDisplayIdsArr) );

        for (SequenceAnnotation seqAnn : newCmp.getSequenceAnnotations()) {
            //System.out.println("New sequence annotation:");
            //System.out.println(seqAnn.getDisplayId());
            assertTrue(saDisplayIds.contains(seqAnn.getDisplayId()));
        }

        for (SequenceAnnotation seqAnn : newCmpFlat.getSequenceAnnotations()) {
            //System.out.println("New sequence annotation:");
            //System.out.println(seqAnn.getDisplayId());
            assertTrue(saDisplayIds.contains(seqAnn.getDisplayId()));
        }
    }

    @Test
    public void instantiateFromTemplateCreatesNewDefinitionWithGivenAttributesPreservingExistingFeatures() throws Exception {

        assertNotNull(doc);

        ComponentDefinition org = doc.getComponentDefinition("ampr_origin", "1.0.0");
        org.createAnnotation(new QName("https://ed.ac.uk/", "bio"), "tomek");

        assertNotNull(org);

        String newName = "escape! Me";
        String newVersion = "1.0.1";
        String newDescription = "Deep copy of DNA_REGION component";

        ComponentDefinition newCmp = templateTransformer.instantiateFromTemplate(org,
                newName, newVersion, newDescription, doc);

        assertNotSame(org, newCmp);

        assertEquals(newName, newCmp.getName());
        assertEquals("escape__Me", newCmp.getDisplayId());
        assertEquals(newVersion, newCmp.getVersion());
        assertEquals(newDescription, newCmp.getDescription());
        assertEquals(org.getTypes(), newCmp.getTypes());
        assertEquals(org.getRoles(), newCmp.getRoles());
        assertEquals(org.getSequences(), newCmp.getSequences());
        assertEquals(org.getAnnotations(), newCmp.getAnnotations());

        assertEquals(org.getSequenceAnnotations().size(), newCmp.getSequenceAnnotations().size());
        for (SequenceAnnotation orgAnn : org.getSequenceAnnotations()) {
            SequenceAnnotation cpy = newCmp.getSequenceAnnotation(orgAnn.getDisplayId());
            assertNotNull(cpy);
            assertEquals(orgAnn.getRoles(), cpy.getRoles());
            assertEquals(orgAnn.getLocations().size(), cpy.getLocations().size());
            if (orgAnn.getComponent() != null) {
                Component orgComp = orgAnn.getComponent();
                Component cpyComp = cpy.getComponent();
                assertNotNull(cpyComp);
                assertEquals(orgComp.getDefinitionIdentity(), cpyComp.getDefinitionIdentity());
            }
        }

        assertEquals(org.getComponents().size(), newCmp.getComponents().size());
        for (Component orgComp : org.getComponents()) {
            Component cpy = newCmp.getComponent(orgComp.getDisplayId());
            assertNotNull(cpy);
            assertEquals(orgComp.getDefinitionIdentity(), cpy.getDefinitionIdentity());
        }
    }

    @Test
    public void testInstantiateFromTemplatePreservingSequenceConstraints() throws Exception {
        assertNotNull(doc);

        ComponentDefinition org = doc.getComponentDefinition("ampr_origin", "1.0.0");
        org.createAnnotation(new QName("https://ed.ac.uk/", "bio"), "johnny");

        assertNotNull(org);

        String newName = "escape! Me";
        String newVersion = "1.0.1";
        String newDescription = "Deep copy of DNA_REGION component";

        ComponentDefinition newCmp = templateTransformer.instantiateFromTemplate(org,
                newName, newVersion, newDescription, doc);

        assertNotSame(org, newCmp);

        assertEquals(newName, newCmp.getName());
        assertEquals("escape__Me", newCmp.getDisplayId());
        assertEquals(newVersion, newCmp.getVersion());
        assertEquals(newDescription, newCmp.getDescription());
        assertEquals(org.getTypes(), newCmp.getTypes());
        assertEquals(org.getRoles(), newCmp.getRoles());
        assertEquals(org.getSequences(), newCmp.getSequences());
        assertEquals(org.getAnnotations(), newCmp.getAnnotations());

        assertEquals(org.getSequenceConstraints().size(), newCmp.getSequenceConstraints().size());

        for (SequenceConstraint orgCon : org.getSequenceConstraints()) {
            SequenceConstraint cpy = newCmp.getSequenceConstraint(orgCon.getDisplayId());
            assertNotNull(cpy);
            /*assertEquals(orgCon.getRoles(), cpy.getRoles());
            assertEquals(orgCon.getLocations().size(), cpy.getLocations().size());
            if (orgCon.getComponent() != null) {
                Component orgComp = orgCon.getComponent();
                Component cpyComp = cpy.getComponent();
                assertNotNull(cpyComp);
                assertEquals(orgComp.getDefinitionIdentity(), cpyComp.getDefinitionIdentity());
            }*/
        }

        assertEquals(org.getComponents().size(), newCmp.getComponents().size());
        for (Component orgComp : org.getComponents()) {
            Component cpy = newCmp.getComponent(orgComp.getDisplayId());
            assertNotNull(cpy);
            assertEquals(orgComp.getDefinitionIdentity(), cpy.getDefinitionIdentity());
        }
    }

    @Test
    public void concretizeComponentReplacesComponentWithANewConcreteDefinition() throws Exception {

        assertNotNull(doc);
        ComponentDefinition parent = doc.getComponentDefinition("cyano_codA_Km", "1.0.0");
        assertNotNull(parent);

        String genericComponentId = "right";
        Component replaced = parent.getComponent(genericComponentId);
        //System.out.println("Replaced component displayId: " + replaced.getDisplayId());
        assertNotNull(replaced);
        ComponentDefinition replacedDef = doc.getComponentDefinition(replaced.getDefinitionIdentity());
        assertNotNull(replacedDef);

        String newName = "right!/new";
        String newSequence = "GATTACA";

        ComponentDefinition newDef = templateTransformer.concretizePart(parent, genericComponentId, newName, newSequence, doc);
        assertNotNull(newDef);

        //it is being replaced
        assertNull(parent.getComponent(genericComponentId));

        String newDisplayId = templateTransformer.util.sanitizeName(newName);

        Component newComp = parent.getComponent(newDisplayId);
        assertNotNull(newComp);

        assertEquals(newDisplayId, newComp.getName());
        assertEquals(parent.getVersion(), newComp.getVersion());
        assertEquals(newDef.getIdentity(), newComp.getDefinitionIdentity());
        assertEquals(replaced.getRoles(), newComp.getRoles());

        //check if newDefinition is correct        
        assertEquals(newDisplayId, newDef.getName());
        assertEquals(newDisplayId, newDef.getDisplayId());

        assertTrue(newDef.getSequences().stream().findFirst().isPresent());
        Sequence seq = newDef.getSequences().stream().findFirst().get();
        assertEquals(newSequence, seq.getElements());
        assertEquals(Sequence.IUPAC_DNA, seq.getEncoding());

        assertEquals(replacedDef.getTypes(), newDef.getTypes());
        assertEquals(replacedDef.getRoles(), newDef.getRoles());

        //write teest if the sequences constraints have been replaced with new one
        //that points to newComp instead to the replaced
        Set<SequenceConstraint> parentSCs = parent.getSequenceConstraints();
        Set<SequenceConstraint> npSCs = newDef.getSequenceConstraints();

        for (SequenceConstraint sc : npSCs) {
            ComponentDefinition objectCD = sc.getObjectDefinition();
            ComponentDefinition subjectCD = sc.getSubjectDefinition();

            if(objectCD == replacedDef || subjectCD == replacedDef) {
                //System.out.println("Old SequenceConstraint detected!");
                assertTrue(Boolean.FALSE);
            } else {
                if(objectCD == newDef) {
                    assertEquals(newComp, objectCD.getComponent(newDisplayId));
                } else if(subjectCD == newDef) {
                    assertEquals(newComp, subjectCD.getComponent(newDisplayId));
                }
            }
        }
    }

    /**
     * Test of concretizeComponent method, of class TemplateTransformer.
     */
    @Test
    public void testConcretizeComponent() throws Exception {
        Set<ComponentDefinition> cmpDefs = doc.getComponentDefinitions();
        int cmpCount = 0;

        for (ComponentDefinition cmpDef : cmpDefs) {
            //System.out.println(cmpDef.getDisplayId());
            //System.out.println(cmpDef.getIdentity());

            String cmpDefId = cmpDef.getDisplayId();

            if (cmpDefId.equals("sll00199_codA_Km") || cmpDefId.equals("cyano_codA_Km")) {
                // Create new sub-component belonging to one of these parent CDs
                Component subCmp = cmpDef.getComponent("left");
                // String genericComponentId = subCmp.getIdentity().toString();
                String genericComponentId = subCmp.getDisplayId();
                //System.out.println(genericComponentId);
                String newName = "test_left_".concat(String.valueOf(cmpCount));
                String newSequence = "GATTACA";

                // Get list of original components in parent before concretize
                List<Component> origCmps = cmpDef.getSortedComponents();

                ComponentDefinition newSubCmpDef = templateTransformer.concretizePart(cmpDef, genericComponentId,
                        newName, newSequence, doc);

                // Get child components and verify they match in new component
                List<Component> newCmps = cmpDef.getSortedComponents();
                int count = 0;

                for (Component child : origCmps) {
                    Component newCmp = newCmps.get(count);

                    //System.out.println(child.getDisplayId());
                    //System.out.println(newCmp.getDisplayId());
                }

                cmpCount += 1;
                // Get sequence constraints and verify they match in new component

                // Get sequence annos and verify they match in new component
            }
        }
    }

    /**
     * Test of instantiateFromTemplate method, of class TemplateTransformer.
     */
    @Test
    public void testFlattenSequences() throws Exception {
        assertNotNull(doc);
        ComponentDefinition template = doc.getComponentDefinition("sll00199_codA_Km", "1.0.0");
        assertNotNull(template);

        // Assume we are adding a new sequence to the component
        String version = "1.0.0"; // should this be the version of the component definition?

        int cmpCount = 0;

        for (Sequence sequence : template.getSequences()) {
            //System.out.println("Initial sequence: " + sequence.getElements());
        }

        String newName = "sll00199_codA_Km!/new_1";
        ComponentDefinition parent = (ComponentDefinition) doc.createCopy(template, "copy", "1.0.0");
        templateTransformer.flattenSequences(parent, newName, doc);

        assertNotNull(parent);

        String expFlattenedSeq = buildSll00199CodAKmSequence();
        for (Sequence sequence : parent.getSequences()) {
            //System.out.println("Flattened sequence:\n" + sequence.getElements());
            assertEquals(expFlattenedSeq, sequence.getElements());
        }

        for (Component cmp : template.getComponents()) {
            ComponentDefinition cmpDef = cmp.getDefinition();
            String newSequence = "GATTACA";
            Sequence seq = doc.createSequence(cmpDef.getDisplayId() + "_" + cmpCount + "_seq", version,
                    newSequence, Sequence.IUPAC_DNA);
            cmpDef.addSequence(seq);
        }

        newName = "sll00199_codA_Km!/new_2";
        parent = (ComponentDefinition) doc.createCopy(template, "copy2", "1.0.0");
        templateTransformer.flattenSequences(parent, newName, doc);

        assertNotNull(parent);

        for (Sequence sequence : parent.getSequences()) {
            //System.out.println("Flattened sequence:\n" + sequence.getElements());
            assertNotEquals(expFlattenedSeq, sequence.getElements());
        }
    }

    private String buildSll00199CodAKmSequence() {
        assertNotNull(doc);

        // ampr -> left -> insert -> right -> gap
        Sequence amprOrigSeq = doc.getSequence("ampr_origin_seq", "1.0.0");
        Sequence leftSeq = doc.getSequence("sll00199_left_seq", "1.0.0");
        Sequence codAKmSeq = doc.getSequence("codA_Km_seq", "1.0.0");
        Sequence rightSeq = doc.getSequence("sll00199_right_seq", "1.0.0");
        Sequence gapSeq = doc.getSequence("gap_seq", "1.0.0");

        assertNotNull(amprOrigSeq);
        assertNotNull(leftSeq);
        assertNotNull(codAKmSeq);
        assertNotNull(rightSeq);
        assertNotNull(gapSeq);

        String flattenedSequence = amprOrigSeq.getElements();
        flattenedSequence = flattenedSequence.concat(leftSeq.getElements());
        flattenedSequence = flattenedSequence.concat(codAKmSeq.getElements());
        flattenedSequence = flattenedSequence.concat(rightSeq.getElements());
        flattenedSequence = flattenedSequence.concat(gapSeq.getElements());

        return flattenedSequence;
    }

    /**
     * Test of addChildSequenceAnnotations method, of class TemplateTransformer.
     */
    @Test
    public void testAddChildren() throws SBOLValidationException {
        assertNotNull(doc);
        //ComponentDefinition template = doc.getComponentDefinition("sll00199_codA_Km", "1.0.0");
        ComponentDefinition template = doc.getComponentDefinition("cyano_codA_Km", "1.0.0");
        assertNotNull(template);

        String[] saDisplayIdsArr = new String[]{"ori", "ori_instance", "AmpR_prom", "null", "ann1", "gap", "AmpR", "ann2", "insert"};
        Set<String> saDisplayIds = new HashSet<>( Arrays.asList(saDisplayIdsArr) );

        Set<SequenceAnnotation> childSeqAnns = new HashSet<>();
        templateTransformer.addChildSequenceAnnotations(template, doc, childSeqAnns);

        for (SequenceAnnotation seqAnn : childSeqAnns) {
            //System.out.println(seqAnn.getIdentity());
            //System.out.println(seqAnn.getComponentIdentity());
            assertTrue(saDisplayIds.contains(seqAnn.getDisplayId()));
        }
    }

    /**
    * Test of rebuildSequences method, of class TemplateTransformer.
    */
    @Test
    public void testRebuildSequences() throws SBOLValidationException {
        assertNotNull(doc);
        ComponentDefinition template = doc.getComponentDefinition("sll00199_codA_Km", "1.0.0");
        //ComponentDefinition template = doc.getComponentDefinition("cyano_codA_Km", "1.0.0");
        assertNotNull(template);

        Map<Component, List<Sequence>> cmpSeqMap = new HashMap<>();
        cmpSeqMap = templateTransformer.rebuildSequences(template, template, doc, cmpSeqMap);
        //templateTransformer.addCustomSequenceAnnotations(template, cmpSeqMap);

        String[] saDisplayIdsArr = new String[]{"ori", "ori_instance", "AmpR_prom", "null", "ann1", "gap", "AmpR", "ann2", "insert"};
        Set<String> saDisplayIds = new HashSet<>( Arrays.asList(saDisplayIdsArr) );

        /*for (SequenceAnnotation seqAnn : childSeqAnns) {
            //System.out.println(seqAnn.getComponentDefinition().getDisplayId());
            //System.out.println(seqAnn.getComponent().getDisplayId());
            System.out.println(seqAnn.getIdentity());
            System.out.println(seqAnn.getComponentIdentity());
            assertTrue(saDisplayIds.contains(seqAnn.getDisplayId()));
        }*/

        ComponentDefinition templateFlat = doc.getComponentDefinition("sll00199_codA_Km_flat", "1.0.0");
        //ComponentDefinition template = doc.getComponentDefinition("cyano_codA_Km", "1.0.0");
        assertNotNull(template);

        for (SequenceAnnotation seqAnn : templateFlat.getSequenceAnnotations()) {
            //System.out.println(seqAnn.getComponentDefinition().getDisplayId());
            //System.out.println(seqAnn.getComponent().getDisplayId());
            //System.out.println(seqAnn.getIdentity());
            //System.out.println(seqAnn.getComponentIdentity());
            assertTrue(saDisplayIds.contains(seqAnn.getDisplayId()));
        }

        Integer[] startsArr = new Integer[]{92, 1228, 197};
        Set<Integer> starts = new HashSet<>( Arrays.asList(startsArr) );

        Integer[] endsArr = new Integer[]{196, 1816, 1057};
        Set<Integer> ends = new HashSet<>( Arrays.asList(endsArr) );

        // template.getSequenceAnnotations().addAll(childSeqAnns);
        //template.createSequenceAnnotation(displayId, locationId, 0, 0, OrientationType.INLINE)
        for (SequenceAnnotation seqAnn : template.getSortedSequenceAnnotations()) {
            //System.out.println(seqAnn.getComponentDefinition().getDisplayId());
            //System.out.println(seqAnn.getComponent().getDisplayId());
            //template.createSequenceAnnotation(seqAnn.getDisplayId(), seqAnn.getLocation(displayId), seqAnn.);
            List<Location> locations = seqAnn.getSortedLocations();

            for(Location loc : locations) {
                Range range = (Range) loc;
                //System.out.println(range.getStart());
                assertTrue(starts.contains((Integer)range.getStart()));
                assertTrue(ends.contains((Integer)range.getEnd()));
            }
            assertTrue(saDisplayIds.contains(seqAnn.getDisplayId()));
        }
    }

    @Test
    public void testAddSequenceAnnotationsToParent() throws Exception {
        assertNotNull(doc);

        ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("sll00199_codA_Km", "1.0.0");
        assertNotNull(sll00199Plasmid);

        ComponentDefinition rightFlank = sll00199Plasmid.getComponent("right").getDefinition();
        assertNotNull(rightFlank);

        // Check that no SequenceAnnotation objects are added when the passed child has none
        templateTransformer.addSequenceAnnotationsToParent(sll00199Plasmid, rightFlank, 1);
        Set<SequenceAnnotation> sll00199PlasmidSAs = sll00199Plasmid.getSequenceAnnotations();

        assertEquals(0, sll00199PlasmidSAs.size());

        // add the SequenceAnnotations from the child 'ampR' component
        ComponentDefinition childCmp = sll00199Plasmid.getComponent("ampR").getDefinition();
        templateTransformer.addSequenceAnnotationsToParent(sll00199Plasmid, childCmp, 1);
        sll00199PlasmidSAs = sll00199Plasmid.getSequenceAnnotations();

        Set<Component> children = sll00199Plasmid.getComponents();
        int childSeqAnnCount = 0;

        for (Component child : children) {
            ComponentDefinition cmpDef = child.getDefinition();
            childSeqAnnCount += cmpDef.getSequenceAnnotations().size();
        }

        assertEquals(childSeqAnnCount, sll00199PlasmidSAs.size());
    }

    @Test
    public void testReplaceComponent() throws Exception {
        assertNotNull(doc);
        ComponentDefinition parent = doc.getComponentDefinition("cyano_codA_Km", "1.0.0");
        assertNotNull(parent);

        String genericComponentId = "right";
        Component replaced = parent.getComponent(genericComponentId);
        //System.out.println("Replaced component displayId: " + replaced.getDisplayId());
        assertNotNull(replaced);
        ComponentDefinition replacedDef = doc.getComponentDefinition(replaced.getDefinitionIdentity());
        assertNotNull(replacedDef);

        String newDisplayId = "test_ampr_origin";
        ComponentDefinition replacementDef = doc.getComponentDefinition("ampr_origin", "1.0.0");
        Component replacement = parent.createComponent(newDisplayId, AccessType.PUBLIC, replacementDef.getIdentity());

        assertNotNull(replacementDef);
        assertNotNull(replacement);

        //ComponentDefinition newDef = templateTransformer.concretizePart(parent, genericComponentId, newName, newSequence, doc);
        templateTransformer.replaceComponent(parent, replaced, replacement);

        //it is being replaced
        assertNull(parent.getComponent(genericComponentId));

        Component newComp = parent.getComponent(newDisplayId);
        assertNotNull(newComp);
    }

    @Test
    public void testUpdateBarcodeSequence() throws Exception {
        String originalSeq = "ATGaGAAGAGCACGGTAGCCTTNNNNNNNNNNNNNNNNNNTGCCCAGTCTTCTGCCTAAGGCAGGTGCCGCGGTGCGGGTGCCAGGGCGTGCCCCCGGGCTCCCCGGGCGCGTACTCCACtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCCGCGGTGCGGGTGCCAGGGCGTGCCCTTGGGCTCCCCGGGCGCGTACTCCACCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCataa";
        String barcodeStr = "tagctcagtcctaggtat";
        String expectSeq = "ATGaGAAGAGCACGGTAGCCTT".concat(barcodeStr).concat("TGCCCAGTCTTCTGCCTAAGGCAGGTGCCGCGGTGCGGGTGCCAGGGCGTGCCCCCGGGCTCCCCGGGCGCGTACTCCACtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCCGCGGTGCGGGTGCCAGGGCGTGCCCTTGGGCTCCCCGGGCGCGTACTCCACCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCataa");

        int startIdx = 22;
        int endIdx = 40;

        String newSeq = templateTransformer.updateBarcodeSequence(originalSeq, barcodeStr, startIdx, endIdx);

        assertEquals(expectSeq, newSeq);

        // longer than diff
        barcodeStr = "ggataacaatttcacacatactag";
        expectSeq = "ATGaGAAGAGCACGGTAGCCTT".concat(barcodeStr).concat("TGCCCAGTCTTCTGCCTAAGGCAGGTGCCGCGGTGCGGGTGCCAGGGCGTGCCCCCGGGCTCCCCGGGCGCGTACTCCACtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCCGCGGTGCGGGTGCCAGGGCGTGCCCTTGGGCTCCCCGGGCGCGTACTCCACCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCataa");
        newSeq = templateTransformer.updateBarcodeSequence(originalSeq, barcodeStr, startIdx, endIdx);

        assertEquals(expectSeq, newSeq);

        // shorter than diff
        barcodeStr = "agctcagtcctagg";
        expectSeq = "ATGaGAAGAGCACGGTAGCCTT".concat(barcodeStr).concat("TGCCCAGTCTTCTGCCTAAGGCAGGTGCCGCGGTGCGGGTGCCAGGGCGTGCCCCCGGGCTCCCCGGGCGCGTACTCCACtttacagctagctcagtcctaggtattatgctagctattgtgagcggataacaatttcacacatactagagaaagaggagaaatactaaATGTCTAACAACGCGCTGCAAACCATCATCAATGCACGCCTGCCTGGAGAGGAAGGGTTGTGGCAGATTCACTTACAGGACGGCAAAATCTCCGCGATCGACGCACAATCTGGGGTTATGCCGATCACCGAAAACTCTTTGGATGCCGAACAAGGGTTAGTCATTCCCCCATTCGTTGAACCACATATTCACCTGGATACTACTCAGACAGCCGGTCAGCCCAATTGGAACCAGTCCGGTACGCTGTTCGAAGGTATCGAACGATGGGCGGAGCGAAAAGCTCTACTCACGCATGACGATGTCAAGCAACGGGCCTGGCAGACCCTGAAGTGGCAGATCGCCAACGGAATACAGCACGTACGCACTCACGTGGATGTTTCCGATGCCACTTTGACGGCATTGAAGGCAATGCTCGAAGTTAAGCAGGAAGTAGCCCCGTGGATTGACTTGCAAATCGCTGCCTTCCCTCAGGAAGGCATCCTAAGTTATCCGAATGGAGAAGCGCTCCTGGAGGAGGCATTGCGGTTAGGAGCAGACGTGGTGGGAGCGATTCCCCATTTCGAGTTTACCCGCGAGTACGGTGTTGAATCTCTGCATAAAACATTTGCTTTAGCTCAGAAGTATGACCGTCTGATCGACGTACACTGCGACGAGATCGATGACGAACAGAGTCGCTTCGTGGAGACGGTGGCTGCGCTGGCGCATCACGAAGGCATGGGTGCACGTGTAACTGCAAGCCATACGACGGCTATGCACAGCTATAATGGGGCATATACATCTCGTTTGTTCCGATTACTAAAAATGAGCGGAATCAACTTTGTTGCCAATCCATTGGTCAACATTCATCTACAAGGACGCTTCGACACCTACCCGAAACGGCGAGGAATCACACGAGTTAAGGAAATGCTAGAGTCTGGTATCAATGTGTGTTTCGGGCATGATGACGTGTGTGGTCCCTGGTACCCTCTAGGAACAGCCAACATGCTGCAAGTTCTCCACATGGGTCTACACGTGTGTCAACTCATGGGGTATGGACAAATTAACGATGGACTCAATCTAATTACACACCATTCCGCCCGAACACTGAACCTCCAGGATTACGGGATCGCGGCGGGAAATTCTGCCAACCTCATCATTCTGCCCGCGGAAAACGGGTTCGACGCTCTACGCCGTCAAGTGCCAGTTCGGTATTCTGTTCGTGGGGGTAAGGTAATTGCAAGTACCCAACCGGCTCAGACCACGGTCTATTTAGAGCAACCGGAAGCTATCGACTACAAACGATGAgcttcaaataaaacgaaaggctcagtcgaaagactgggcctttcgttttatctgttgtttgtcggtgaacgctctctactagagtcacactggctcaccttcgggtgggcctttctgcgcgctCTGAGGTCTGCCTCGTGAAGAAGGTGTTGCTGACTCATACCAGGCCTGAATCGCCCCATCATCCAGCCAGAAAGTGAGGGAGCCACGGTTGATGAGAGCTTTGTTGTAGGTGGACCAGTTGGTGATTTTGAACTTTTGCTTTGCCACGGAACGGTCTGCGTTGTCGGGAAGATGCGTGATCTGATCCTTCAACTCAGCAAAAGTTCGATTTATTCAACAAAGCCGCCGTCCCGTCAAGTCAGCGTAATGCTCTGCCAGTGTTACAACCAATTAACCAATTCTGATTAGAAAAACTCATCGAGCATCAAATGAAACTGCAATTTATTCATATCAGGATTATCAATACCATATTTTTGAAAAAGCCGTTTCTGTAATGAAGGAGAAAACTCACCGAGGCAGTTCCATAGGATGGCAAGATCCTGGTATCGGTCTGCGATTCCGACTCGTCCAACATCAATACAACCTATTAATTTCCCCTCGTCAAAAATAAGGTTATCAAGTGAGAAATCACCATGAGTGACGACTGAATCCGGTGAGAATGGCAAAAGCTTATGCATTTCTTTCCAGACTTGTTCAACAGGCCAGCCATTACGCTCGTCATCAAAATCACTCGCATCAACCAAACCGTTATTCATTCGTGATTGCGCCTGAGCGAGACGAAATACGCGATCGCTGTTAAAAGGACAATTACAAACAGGAATCGAATGCAACCGGCGCAGGAACACTGCCAGCGCATCAACAATATTTTCACCTGAATCAGGATATTCTTCTAATACCTGGAATGCTGTTTTCCCGGGGATCGCAGTGGTGAGTAACCATGCATCATCAGGAGTACGGATAAAATGCTTGATGGTCGGAAGAGGCATAAATTCCGTCAGCCAGTTTAGTCTGACCATCTCATCTGTAACATCATTGGCAACGCTACCTTTGCCATGTTTCAGAAACAACTCTGGCGCATCGGGCTTCCCATACAATCGATAGATTGTCGCACCTGATTGCCCGACATTATCGCGAGCCCATTTATACCCATATAAATCAGCATCCATGTTGGAATTTAATCGCGGCCTCGAGCAAGACGTTTCCCGTTGAATATGGCTCATAACACCCCTTGTATTACTGTTTATGTAAGCAGACAGTTTTATTGTTCATGATGATATATTTTTATCTTGTGCAATGTAACATCAGAGATTTTGAGACACAACGTGGCTTTCCGCGGTGCGGGTGCCAGGGCGTGCCCTTGGGCTCCCCGGGCGCGTACTCCACCACCTGCCATTGGGAGAAGACTTGGGAGCTCTTCataa");
        newSeq = templateTransformer.updateBarcodeSequence(originalSeq, barcodeStr, startIdx, endIdx);

        assertEquals(expectSeq, newSeq);
    }

    /**
     * Test of entire E2E process for instantiating/conretizing/flattening
     * methods, of class TemplateTransformer.
     */
    @Test
    public void testCreateNewPlasmid() throws Exception {
        // Get original sll00199 component definition for comparison
        assertNotNull(doc);

        ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("sll00199_codA_Km", "1.0.0");
        assertNotNull(sll00199Plasmid);

        ComponentDefinition sll00199PlasmidFlat = doc.getComponentDefinition("sll00199_codA_Km_flat", "1.0.0");
        assertNotNull(sll00199PlasmidFlat);

        // Copy Template
        ComponentDefinition templatePlasmid = doc.getComponentDefinition("cyano_codA_Km", "1.0.0");
        assertNotNull(templatePlasmid);

        String newName = "sll00199_codA_Km_johnny";
        String version = "1.0.0";
        String description = "Test plasmid creation";
        ComponentDefinition newPlasmid = templateTransformer.instantiateFromTemplate(templatePlasmid, newName, version, description, doc);

        assertEquals(sll00199Plasmid.getComponents().size(), newPlasmid.getComponents().size());

        // Check component instances match
        for (Component cmp : sll00199Plasmid.getSortedComponents()) {
            assertNotNull(newPlasmid.getComponent(cmp.getDisplayId()));
        }

        for (Component cmp : newPlasmid.getSortedComponents()) {
            assertNotNull(sll00199Plasmid.getComponent(cmp.getDisplayId()));
        }

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

        // Check component instances match
        for (Component cmp : sll00199Plasmid.getSortedComponents()) {
            //System.out.println(cmp.getDisplayId());
            //assertNotNull(newPlasmid.getComponent(cmp.getDisplayId()));
        }

        for (Component cmp : newPlasmid.getSortedComponents()) {
            //System.out.println(cmp.getDisplayId());
            //assertNotNull(sll00199Plasmid.getComponent(cmp.getDisplayId()));
        }

        // Check left and right flank sequences match
        ComponentDefinition sll00199LtCD = sll00199Plasmid.getComponent("left").getDefinition();
        ComponentDefinition sll00199RtCD = sll00199Plasmid.getComponent("right").getDefinition();

        ComponentDefinition newLtCD = newPlasmid.getComponent("test_left").getDefinition();
        ComponentDefinition newRtCD = newPlasmid.getComponent("test_right").getDefinition();

        Set<Sequence> sll00199LtCDSeqs = sll00199LtCD.getSequences();
        Set<Sequence> sll00199RtCDSeqs = sll00199RtCD.getSequences();

        // assuming only one sequence per flank
        String sll00199LtSeqEls = ((Sequence)sll00199LtCDSeqs.toArray()[0]).getElements();
        String sll00199RtSeqEls = ((Sequence)sll00199RtCDSeqs.toArray()[0]).getElements();

        Set<Sequence> newLtCDSeqs = newLtCD.getSequences();
        Set<Sequence> newRtCDSeqs = newRtCD.getSequences();

        // assuming only one sequence per flank
        String newLtSeqEls = ((Sequence)newLtCDSeqs.toArray()[0]).getElements();
        String newRtSeqEls = ((Sequence)newRtCDSeqs.toArray()[0]).getElements();

        assertEquals(ltSeq, sll00199LtSeqEls);
        assertEquals(ltSeq, newLtSeqEls);
        assertEquals(sll00199LtSeqEls, newLtSeqEls);

        assertEquals(rtSeq, sll00199RtSeqEls);
        assertEquals(rtSeq, newRtSeqEls);
        assertEquals(sll00199RtSeqEls, newRtSeqEls);

        // Check sequence constraints match
        Set<SequenceConstraint> sll00199SCs = sll00199Plasmid.getSequenceConstraints();
        Set<SequenceConstraint> npSCs = newPlasmid.getSequenceConstraints();

        for (SequenceConstraint sc : npSCs) {
            SequenceConstraint npSc = newPlasmid.getSequenceConstraint(sc.getDisplayId());
            assertNotNull(sll00199Plasmid.getSequenceConstraint(npSc.getDisplayId()));
        }

        for (SequenceConstraint sc : sll00199SCs) {
            SequenceConstraint sll00199Sc = sll00199Plasmid.getSequenceConstraint(sc.getDisplayId());
            assertNotNull(newPlasmid.getSequenceConstraint(sll00199Sc.getDisplayId()));
        }

        // Check sequence annotations match
        Set<SequenceAnnotation> sll00199SAs = sll00199Plasmid.getSequenceAnnotations();
        Set<SequenceAnnotation> npSAs = newPlasmid.getSequenceAnnotations();

        // Get sequence annos and verify they match in new component
        for (SequenceAnnotation seqAnn : npSAs) {
            //System.out.println(seqAnn.getIdentity());
            //System.out.println(seqAnn.getComponentIdentity());
        }

        for (SequenceAnnotation seqAnn : sll00199SAs) {
            //System.out.println(seqAnn.getIdentity());
            //System.out.println(seqAnn.getComponentIdentity());
        }

        // Add the flattened sequences to the parent component's SequenceAnnotation components
        ComponentDefinition newPlasmidFlat = templateTransformer.flattenSequences(newPlasmid, newName.concat("_flat"), doc);
        newPlasmidFlat.addRole(new URI(SEQUENCE_ONTO_PREF+"SO:0000637"));

        // Check component instances match
        for (Component cmp : sll00199PlasmidFlat.getSortedComponents()) {
            //System.out.println(cmp.getDisplayId());
            //assertNotNull(newPlasmidFlat.getComponent(cmp.getDisplayId()));
        }

        for (Component cmp : newPlasmidFlat.getSortedComponents()) {
            //System.out.println(cmp.getDisplayId());
            //assertNotNull(sll00199PlasmidFlat.getComponent(cmp.getDisplayId()));
        }

        Set<SequenceAnnotation> sll00199PlasmidFlatSAs = sll00199PlasmidFlat.getSequenceAnnotations();
        Set<SequenceAnnotation> npFlatSAs = newPlasmidFlat.getSequenceAnnotations();

        // Get sequence annos and verify they match in new component
        for (SequenceAnnotation seqAnn : npFlatSAs) {
            // How to verify these objets are equivalent in each plasmid?
            //System.out.println(seqAnn.getIdentity());
            //System.out.println(seqAnn.getComponentIdentity());
        }

        //assertEquals(sll00199PlasmidFlatSAs.size(), npFlatSAs.size());

        // why does this method return 'NNN...' strings for new plasmid?
        // something to do with the SequenceAnnotations having null linked components.
        // But can't set the components on the SAs because of circular reference error?
        //System.out.println(sll00199PlasmidFlat.getImpliedNucleicAcidSequence());
        //System.out.println(newPlasmidFlat.getImpliedNucleicAcidSequence());

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
    }
    
    
    
    public SBOLDocument testDoc(String fileName) throws SBOLValidationException {
        try {
            File file = testFile(fileName);
            SBOLDocument doc = SBOLReader.read(file);
            doc.setDefaultURIprefix("http://bio.ed.ac.uk/biordm");
            doc.setComplete(true);
            doc.setCreateDefaults(true);
        return doc;
        } catch (SBOLValidationException| IOException | SBOLConversionException e) {
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
