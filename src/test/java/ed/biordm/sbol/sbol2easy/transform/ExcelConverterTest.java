/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.transform;

import ed.biordm.sbol.sbol2easy.transform.FeaturesReader;
import ed.biordm.sbol.sbol2easy.transform.TemplateTransformer;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.Sequence;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author jhay
 */
public class ExcelConverterTest {

    FeaturesReader featuresReader = new FeaturesReader();
    TemplateTransformer templateTransformer = new TemplateTransformer();
    SBOLDocument templateDoc;
    ComponentDefinition cyanoTemplate;
    String templateFilename = "cyano_template.xml";
    static String SEQUENCE_ONTO_PREF = "http://identifiers.org/so/";

    // private static final String TMP_PATH = "E:/Temp/";
    private static final String TMP_PATH = "D:/Temp/sbol/";

    @Before
    public void generateSBOLDocument() throws IOException, SBOLValidationException, SBOLConversionException {
        File file = new File(getClass().getResource(templateFilename).getFile());

        try {
            templateDoc = SBOLReader.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        templateDoc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
        templateDoc.setComplete(true);
        templateDoc.setCreateDefaults(true);
    }

    @Before
    public void initCyanoTemplate() throws IOException, SBOLValidationException, SBOLConversionException {
        assertNotNull(templateDoc);
        String templateVersion = "1.0.0";
        cyanoTemplate = templateDoc.getComponentDefinition("cyano_codA_Km", templateVersion);
    }

    //@Test
    public void testReadMultiFeatures() throws SBOLValidationException, Exception, IOException {
        assertNotNull(cyanoTemplate);

        File file = new File(getClass().getResource("flank-list.xlsx").getFile());

        // Test worksheet 1 (Left flank)
        Map<String, List<String>> leftSheet = featuresReader.readMultiFeatures(file.toPath(), 0, 0);

        // Test worksheet 2 (Right flank)
        Map<String, List<String>> rightSheet = featuresReader.readMultiFeatures(file.toPath(), 0, 1);

        leftSheet.forEach((key, value) -> {
            List<String> colVals = (List<String>) value;
            String leftFlankName = key;
            String leftFlankSequence = "";
            String rightFlankSequence = "";

            if (value.size() > 0) {
                leftFlankSequence = value.get(0);
            }

            String[] nameStrs = leftFlankName.split("_left");
            // prepend the organism type so it's valid (must begin with letter)
            String flankNamePrefix = "codA_Km_".concat(nameStrs[0]);

            leftFlankName = flankNamePrefix.concat("_left");
            String rightFlankName = flankNamePrefix.concat("_right");

            List<String> rightFlankVal = rightSheet.get(rightFlankName);

            if (value.size() > 0) {
                rightFlankSequence = value.get(0);
            }

            //String lfGenericId = "left_flank";
            //String rfGenericId = "right_flank";
            String lfGenericId = "left";
            String rfGenericId = "right";

            // Create new plasmid with the two replaced flank components
            // String newPlasmidName = flankNamePrefix.concat("_codA_Km");
            String newPlasmidName = flankNamePrefix;
            String version = "1.0.0";
            String description = "Plasmid with both flanks replaced";

            // Create copy of top level plasmid template doc
            File sbolFile;
            SBOLDocument newDoc = null;

            try {
                sbolFile = copySBOLFile(newPlasmidName);
                newDoc = readSBOLFile(sbolFile);
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (SBOLValidationException ex) {
                ex.printStackTrace();
            } catch (SBOLConversionException ex) {
                ex.printStackTrace();
            }

            ComponentDefinition newCyanoCD;
            ComponentDefinition newLeftFlank;
            ComponentDefinition newRightFlank;
            ComponentDefinition newCyanoCDFlat;

            try {
                newCyanoCD = templateTransformer.
                        instantiateFromTemplate(cyanoTemplate, newPlasmidName, version, description, newDoc);
                newCyanoCD.addType(SequenceOntology.CIRCULAR);
                //engineered plasmid
                newCyanoCD.addRole(new URI(SEQUENCE_ONTO_PREF + "SO:0000637"));

                newLeftFlank = templateTransformer.concretizePart(newCyanoCD, lfGenericId, leftFlankName, leftFlankSequence, newDoc);
                newRightFlank = templateTransformer.concretizePart(newCyanoCD, rfGenericId, rightFlankName, rightFlankSequence, newDoc);

                // Should concretizePart do this bit?
                /*for (Sequence seq : newLeftFlank.getSequences()) {
                    newDoc.createSequence(leftFlankName.concat("_seq"), seq.getElements(), Sequence.IUPAC_DNA);
                }

                for (Sequence seq : newRightFlank.getSequences()) {
                    newDoc.createSequence(rightFlankName.concat("_seq"), seq.getElements(), Sequence.IUPAC_DNA);
                }*/
                // Finally, flatten the new sequences into the parent plasmid definition
                //newCyanoCDFlat = templateTransformer.flattenSequences(newCyanoCD, newPlasmidName.concat("_flat"), newDoc);

                // Add arbitrary(?) SequenceAnnotations. What are the rules for these annotations?
                //Component seqCmp = newCyanoCDFlat.getComponent("ampR");
            } catch (SBOLValidationException ex) {
                ex.printStackTrace();
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }

            // Remove extraneous components/definitions
            newDoc = cleanSBOLDocument(newDoc);

            String fName = newPlasmidName;

            SBOLValidate.validateSBOL(newDoc, true, true, true);
            if (SBOLValidate.getNumErrors() > 0) {
                for (String error : SBOLValidate.getErrors()) {
                    System.out.println(error);
                }
                throw new IllegalStateException("Stoping cause of validation errors");
            }

            try {
                SBOLWriter.write(newDoc, TMP_PATH + fName + ".xml");
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (SBOLConversionException ex) {
                ex.printStackTrace();
            }
        });

    }

    private SBOLDocument readSBOLFile(File file) throws SBOLValidationException, SBOLConversionException, IOException {
        SBOLDocument doc = new SBOLDocument();

        try {
            doc = SBOLReader.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        doc.setDefaultURIprefix("http://bio.ed.ac.uk/a_mccormick/cyano_source/");
        doc.setComplete(true);
        doc.setCreateDefaults(true);

        return doc;
    }

    private File copySBOLFile(String newFileName) throws IOException {
        File templateFile = new File(getClass().getResource(templateFilename).getFile());
        String filePath = templateFile.getParentFile().getAbsolutePath();
        File outputFile = new File(filePath.concat("/").concat(newFileName).concat(".xml"));

        if (!outputFile.exists()) {
            Files.copy(templateFile.toPath(), outputFile.toPath());
        }

        return outputFile;
    }

    private SBOLDocument cleanSBOLDocument(SBOLDocument doc) {
        try {
            ComponentDefinition oldCD = doc.getComponentDefinition("sll00199_codA_Km", "1.0.0");
            ComponentDefinition oldCDFlat = doc.getComponentDefinition("sll00199_codA_Km_flat", "1.0.0");
            doc.removeComponentDefinition(oldCD);
            doc.removeComponentDefinition(oldCDFlat);

            oldCD = doc.getComponentDefinition("sll00199_right", "1.0.0");
            doc.removeComponentDefinition(oldCD);

            oldCD = doc.getComponentDefinition("sll00199_left", "1.0.0");
            doc.removeComponentDefinition(oldCD);

            Sequence oldSeq = doc.getSequence("sll00199_right_seq", "1.0.0");
            doc.removeSequence(oldSeq);

            oldSeq = doc.getSequence("sll00199_left_seq", "1.0.0");
            doc.removeSequence(oldSeq);

            oldSeq = doc.getSequence("sll00199_codA_Km_flat_seq", "1.0.0");
            doc.removeSequence(oldSeq);
        } catch (SBOLValidationException ex) {
            Logger.getLogger(ExcelConverterTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        return doc;
    }

}
