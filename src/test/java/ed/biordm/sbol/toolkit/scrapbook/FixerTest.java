/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.scrapbook;

import ed.biordm.sbol.toolkit.scrapbook.MetaReader;
import ed.biordm.sbol.toolkit.scrapbook.Fixer;
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
import org.sbolstandard.core2.SBOLWriter;
import org.sbolstandard.core2.SequenceOntology;

/**
 *
 * @author Tomasz Zielinski
 */
public class FixerTest {

    // private static final String TMP_PATH = "E:/Temp";
    private static final String TMP_PATH = "D:/Temp/sbol";

    Fixer fixer;

    public FixerTest() {
    }

    @Before
    public void setUp() {
        fixer = new Fixer();

    }

    //@Test
    public void so() {
        SequenceOntology so = new SequenceOntology();

        URI t = SequenceOntology.NAMESPACE;; //so.getURIbyId("SO:0002232");
        assertNotNull(t);
        System.out.println(t);

        System.out.println(Fixer.selection);
        fail();
    }

    @Test
    public void fixWorks() throws Exception {
        String fName = "Inteinassistedbisectionmapping_collection-fixed.sbol";

        SBOLDocument doc = SBOLReader.read(testFile(fName));
        doc.setDefaultURIprefix("https://synbiohub.org/user/trevorho/Inteinassistedbisectionmapping/");
        MetaReader meta = new MetaReader(testFile("IBM constructs by ID.csv").toPath(), 1);

        String version = doc.getCollections().iterator().next().getVersion();
        fixer.fix(doc, meta, version);

        doc.getComponentDefinitions().forEach(cd -> {

            assertNotNull(cd.getAnnotation(Fixer.description));
        });

        String outN = "Inteinassistedbisectionmapping_collection-fixed-tz.sbol";
        Path out = Paths.get(TMP_PATH).resolve(outN);
        SBOLWriter.write(doc, out.toFile());

        SBOLDocument fixed = SBOLReader.read(out.toFile());
        fixed.getComponentDefinitions().forEach(cd -> {

            assertNotNull(cd.getAnnotation(Fixer.description));
            //assertNotNull(cd.getAnnotation(Fixer.selection));
        });

        //fail("to see out");
    }

    @Test
    public void testFilesReads() {

        File f = testFile("Inteinassistedbisectionmapping_collection-fixed.sbol");
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
