/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
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

    GenBankConverter gbConverter = new GenBankConverter();
    SBOLDocument doc;
    static String SEQUENCE_ONTO_PREF = "http://identifiers.org/so/";

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
            assertTrue(output.contains(label));
        }
    }

    private String getSequenceAnnoLabel(SequenceAnnotation seqAnn) {
        String label = null;
        if (seqAnn.isSetComponent() && seqAnn.getComponent().getDefinition() != null
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
}
