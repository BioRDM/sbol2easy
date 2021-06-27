/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.sbolstandard.core2.SBOLDocument;

/**
 *
 * @author tzielins
 */
public class ComponentAnnotator {
    
    
    public Outcome annotate(SBOLDocument source, Path metaFile, boolean appendDesc, 
            boolean stopOnMissingId, boolean stopOnMissingMeta) {
        
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    
    
    public static class Outcome {
        public List<String> successful = new ArrayList<>();
        public List<String> missingId = new ArrayList<>();
        public List<String> missingMeta = new ArrayList<>();
        
    }
}
