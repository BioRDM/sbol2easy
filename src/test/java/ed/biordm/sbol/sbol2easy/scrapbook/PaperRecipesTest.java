/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.scrapbook;

import ed.biordm.sbol.sbol2easy.scrapbook.PaperRecipes;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;

/**
 *
 * @author tzielins
 */
public class PaperRecipesTest {
    
    PaperRecipes recipes;
    
    public PaperRecipesTest() {
    }
    
    @Before
    public void setUp() {
        recipes = new PaperRecipes();
    }

    public Path testFile(String name) {
        try {
            return Paths.get(this.getClass().getResource(name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }     
}
