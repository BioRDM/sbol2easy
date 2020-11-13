/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.io.File;
import java.io.IOException;
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
import org.sbolstandard.core2.SequenceAnnotation;

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
        String fName = "cyano_full_template.xml";
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
}
