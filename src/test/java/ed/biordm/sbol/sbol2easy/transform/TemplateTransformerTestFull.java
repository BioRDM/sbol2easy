/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.transform;

import ed.biordm.sbol.sbol2easy.transform.ComponentFlattener;
import ed.biordm.sbol.sbol2easy.transform.TemplateTransformer;
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
    ComponentFlattener flattener = new ComponentFlattener();
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
        ComponentDefinition newPlasmidFlat = flattener.flattenDesign(sll00199Plasmid, newName.concat("_flat"), doc);
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



}
