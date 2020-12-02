/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;

/**
 *
 * @author jhay
 */
public class GenBankConverterTest {

    TemplateTransformer templateTransformer = new TemplateTransformer();
    GenBankConverter gbConverter = new GenBankConverter();
    SBOLDocument doc;
    static String SEQUENCE_ONTO_PREF = "http://identifiers.org/so/";

    private static final String TMP_PATH = "D:/Temp/sbol/";

    @Before
    public void generateSBOLDocument() throws IOException, SBOLValidationException, org.sbolstandard.core2.SBOLConversionException {
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

    @Test
    public void testWriteComponentDefinition() throws Exception {
        assertNotNull(doc);

        // ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("backbone", "1.0.0");
        ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("insert", "1.0.0");
        assertNotNull(sll00199Plasmid);

        Writer sysOutWriter = new BufferedWriter(new OutputStreamWriter(System.out));

        gbConverter.writeComponentDefinition(sll00199Plasmid, sysOutWriter);
        sysOutWriter.flush();
    }

    @Test
    public void testWriteFlattenedPlasmid() throws Exception {
        assertNotNull(doc);

        ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("sll00199_codA_Km", "1.0.0");
        assertNotNull(sll00199Plasmid);

        String newName = "sll00199_codA_Km".concat("_johnny");

        ComponentDefinition newPlasmidFlat = templateTransformer.flattenSequences(sll00199Plasmid, newName.concat("_flat"), doc);
        newPlasmidFlat.addRole(new URI(SEQUENCE_ONTO_PREF+"SO:0000637"));

        // Writer sysOutWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        Writer strWriter = new StringWriter();

        gbConverter.writeComponentDefinition(newPlasmidFlat, strWriter);
        strWriter.flush();

        String output = strWriter.toString();
        System.out.println(output);

        /*
        * For some reason, there seems to be an inconsistency: "attB TT" is the
        * dcterms:title for the sequence annotation, but the GenBankConverter is
        * ignoring that and reading the displayId instead. For another 
        * sequence annotation with dcterms:title of "AarI-TGCC overhang", the
        * converter uses that value instead of displayId. What factor makes it
        * discriminate? I see that the "attB TT" anno has a 'note' attribute
        * while the latter does not...
        */

        for (SequenceAnnotation seqAnn : newPlasmidFlat.getSequenceAnnotations()) {
            String label = getSequenceAnnoLabel(seqAnn);
            /*if (seqAnn.isSetName()) {
                label = seqAnn.getName();
            } else if (seqAnn.isSetComponent() && seqAnn.getComponent() != null
                    && seqAnn.getComponent().isSetName()) {
                label = seqAnn.getComponent().getName();
            } else if (seqAnn.isSetComponent() && seqAnn.getComponent().getDefinition() != null
                    && seqAnn.getComponent().getDefinition().isSetName()) {
                label = seqAnn.getComponent().getDefinition().getName();
            }*/

            // GenBank format limits the length of the line to 41 characters?
            label = trimLabel(label);

            System.out.println(label);
            assertTrue(output.contains(label));
        }

        String filePath = TMP_PATH.concat("sll00199_codA_Km_flat.gb");
        writeOutputToFile(output, filePath);
    }

    @Test
    public void testWriteNameLabel() throws Exception {
        assertNotNull(doc);

        // ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("backbone", "1.0.0");
        ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("insert", "1.0.0");
        assertNotNull(sll00199Plasmid);

        for (SequenceAnnotation seqAnn : sll00199Plasmid.getSequenceAnnotations()) {
            Writer strWriter = new StringWriter();
            gbConverter.writeNameLabel(strWriter, seqAnn);
            String output = strWriter.toString();
            strWriter.flush();

            System.out.println(output);
            String label = getSequenceAnnoLabel(seqAnn);

            // GenBank format limits the length of the line to 41 characters?
            label = trimLabel(label);

            System.out.println(label);
            assertTrue(output.contains(label));
        }
    }

    private String getSequenceAnnoLabel(SequenceAnnotation seqAnn) {
        String label = null;
        if (seqAnn.isSetName()) {
            label = seqAnn.getName();
        } else if (seqAnn.isSetComponent() && seqAnn.getComponent().getDefinition() != null
            && seqAnn.getComponent().getDefinition().isSetName()) {
            label = seqAnn.getComponent().getDefinition().getName();
        } else if (seqAnn.isSetComponent() && seqAnn.getComponent().getDefinition() != null
                && seqAnn.getComponent().getDefinition().isSetDisplayId()) {
            label = seqAnn.getComponent().getDefinition().getDisplayId();
        } else if (seqAnn.isSetComponent() && seqAnn.getComponent() != null
                && seqAnn.getComponent().isSetName()) {
            label = seqAnn.getComponent().getName();
        } else if (seqAnn.isSetComponent() && seqAnn.getComponent() != null
                && seqAnn.getComponent().isSetDisplayId()) {
            label = seqAnn.getComponent().getDisplayId();
        } else if (seqAnn.isSetDisplayId()) {
            label = seqAnn.getDisplayId();
        }

        return label;
    }

    private String trimLabel(String label) {
        String labelPrefix = "                     /label=";
        int labelPrefixLen = labelPrefix.length();
        int gbMaxLineLen = 41;

        // GenBank format limits the length of the line to 41 characters?
        if(labelPrefix.concat(label).length() > gbMaxLineLen) {
            label = label.substring(0, (gbMaxLineLen - labelPrefixLen + 1));
        }

        return label;
    }

    private boolean writeOutputToFile(String output, String outputFile) {
        boolean success = false;

        try(FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            //convert string to byte array
            byte[] bytes = output.getBytes();
            //write byte array to file
            bos.write(bytes);
            bos.close();
            fos.close();
            System.out.print("Data written to file successfully.");
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }
}
