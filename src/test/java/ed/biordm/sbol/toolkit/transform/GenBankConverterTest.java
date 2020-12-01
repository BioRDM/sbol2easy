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
import java.io.Writer;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

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

    @Test
    public void testWriteComponentDefinition() throws Exception {
        assertNotNull(doc);

        ComponentDefinition sll00199Plasmid = doc.getComponentDefinition("ampr_origin", "1.0.0");
        assertNotNull(sll00199Plasmid);

        Writer sysOutWriter = new BufferedWriter(new OutputStreamWriter(System.out));

        gbConverter.writeComponentDefinition(sll00199Plasmid, sysOutWriter);
        sysOutWriter.flush();
    }
}