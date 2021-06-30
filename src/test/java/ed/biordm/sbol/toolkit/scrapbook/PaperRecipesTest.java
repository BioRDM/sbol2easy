/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.scrapbook;

import ed.biordm.sbol.toolkit.transform.CommonAnnotations;
import ed.biordm.sbol.toolkit.transform.ComponentAnnotator;
import ed.biordm.sbol.toolkit.transform.ComponentFlattener;
import static ed.biordm.sbol.toolkit.transform.ComponentUtil.emptyDocument;
import static ed.biordm.sbol.toolkit.transform.ComponentUtil.saveValidSbol;
import ed.biordm.sbol.toolkit.transform.LibraryGenerator;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLConversionException;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLReader;
import org.sbolstandard.core2.SBOLValidationException;

/**
 *
 * @author tzielins
 */
public class PaperRecipesTest {
    
    Path tempDir = Paths.get("E:/Temp");        
    Path outDir = tempDir.resolve("sbol2easy_"+LocalDate.now());
    
    PaperRecipes recipes;
    
    public PaperRecipesTest() {
    }
    
    @Before
    public void setUp() {
        recipes = new PaperRecipes();
    }

    @Test
    public void run()  {
        
        try {
            Files.createDirectories(outDir);

            String version = "1.0";
            
            // generating template
            Path templateFile = outDir.resolve("template.xml");
            SBOLDocument doc = emptyDocument();
            ComponentDefinition template = recipes.assemblePlasmidTemplate(doc, version);
            saveValidSbol(doc, templateFile);
            
            //generating library
            Path testFile = testFile("library_def.xlsx");
            assertTrue(Files.isRegularFile(testFile));            
            Path libraryDef = outDir.resolve("library_def.xlsx");
            Files.copy(testFile, libraryDef);
            LibraryGenerator generator = new LibraryGenerator();
            generator.generateFromFiles("library", version, templateFile, libraryDef, outDir, true);
            
            Path libraryFile = outDir.resolve("library.1.xml");
            assertTrue(Files.isRegularFile(libraryFile));
            
            //flatten
            doc = emptyDocument();
            SBOLDocument in = SBOLReader.read(libraryFile.toFile());
            // does not have sequence needs removal
            //template = in.getComponentDefinition(template.getPersistentIdentity());
            //in.removeComponentDefinition(template);
            ComponentFlattener flattener = new ComponentFlattener();
            flattener.flattenDesigns(in, " flat", doc, false);
            Path flatFile = outDir.resolve("flat.xml");
            saveValidSbol(doc, flatFile);
            
            //annotate flatten
            testFile = testFile("flat_annotation.xlsx");
            assertTrue(Files.isRegularFile(testFile));            
            Path annotDef = outDir.resolve("flat_annotation.xlsx");
            Files.copy(testFile, annotDef);
            
            in = SBOLReader.read(flatFile.toFile());
            in.setDefaultURIprefix(CommonAnnotations.BIORDM_PREF);
            ComponentAnnotator annotator = new ComponentAnnotator();
            annotator.annotate(in, annotDef, false, true, true);
            Path annotatedFile = outDir.resolve("flat_described.xml");
            saveValidSbol(in, annotatedFile);                    
            
            
        } catch (IOException | SBOLConversionException | SBOLValidationException e) {
            System.out.println("Failed: "+e.getMessage());
            e.printStackTrace(System.out);
            throw new RuntimeException(e);
        }
    }
    
    
    public Path testFile(String name) {
        try {
            return Paths.get(this.getClass().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }     
}
