/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.scrapbook;

import ed.biordm.sbol.sbol2easy.scrapbook.TrevorMetaReader;
import ed.biordm.sbol.sbol2easy.scrapbook.TrevorFixer;
import ed.biordm.sbol.sbol2easy.transform.CommonAnnotations;
import static ed.biordm.sbol.sbol2easy.transform.CommonAnnotations.*;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author Tomasz Zielinski
 */
public class TrevorFixerTest {

    private static final String TMP_PATH = "E:/Temp";
    //private static final String TMP_PATH = "D:/Temp/sbol";

    TrevorFixer fixer;

    public TrevorFixerTest() {
    }

    @Before
    public void setUp() {
        fixer = new TrevorFixer();

    }


    
    //@Test
    public void runConversion() throws Exception {

        String fName = "trevor_test_collection.xml";
        SBOLDocument doc = SBOLReader.read(Paths.get(TMP_PATH).resolve(fName).toFile());
        
        //TrevorMetaReader meta = new TrevorMetaReader(Paths.get(TMP_PATH).resolve(""), 1);
        TrevorMetaReader meta = new TrevorMetaReader(testFile("IBM constructs by ID.csv").toPath(), 2);
        
        String version = doc.getCollections().iterator().next().getVersion();
        doc = fixer.fix(doc, meta, version);
        
        doc.getComponentDefinitions().forEach(cd -> {

            assertNotNull(cd.getAnnotation(SBH_DESCRIPTION));
        });

        SBOLValidate.clearErrors();
        SBOLValidate.validateSBOL(doc, true, true, true);
        if (SBOLValidate.getNumErrors() > 0) {
            for (String error : SBOLValidate.getErrors()) {
                System.out.println(error);
            }
            throw new IllegalStateException("Stoping cause of validation errors");
        }        
        
        String outN = "fixed_"+fName.substring(0, fName.lastIndexOf("."))+".sbol";
        Path out = Paths.get(TMP_PATH).resolve(outN);
        SBOLWriter.write(doc, out.toFile());
    }

    @Test
    public void fixWorks() throws Exception {
        String fName = "trevor_test_collection.xml";

        SBOLDocument doc = SBOLReader.read(testFile(fName));
        //doc.setDefaultURIprefix("https://synbiohub.org/user/trevorho/Inteinassistedbisectionmapping/");
        TrevorMetaReader meta = new TrevorMetaReader(testFile("IBM constructs by ID.csv").toPath(), 2);

        String version = doc.getCollections().iterator().next().getVersion();
        doc = fixer.fix(doc, meta, version, false);

        doc.getComponentDefinitions().forEach(cd -> {

            assertNotNull(cd.getAnnotation(SBH_DESCRIPTION));
        });

        SBOLValidate.clearErrors();
        SBOLValidate.validateSBOL(doc, true, true, true);
        if (SBOLValidate.getNumErrors() > 0) {
            for (String error : SBOLValidate.getErrors()) {
                System.out.println(error);
            }
            throw new IllegalStateException("Stoping cause of validation errors");
        }        
        
        String outN = "fixed_text_"+fName+".sbol";
        Path out = Paths.get(TMP_PATH).resolve(outN);
        SBOLWriter.write(doc, out.toFile());

        SBOLReader.read(out.toFile());
        //fail("to see out");
    }

    @Test
    public void testFilesReads() {

        File f = testFile("trevor_test_collection.xml");
        assertNotNull(f);

    }

    public File testFile(String name) {
        try {
            return new File(this.getClass().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }

}
