/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.util.Set;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;

/**
 *
 * @author tzielins
 */
public class ComponentUtil {
    
    public ComponentDefinition extractRootComponent(SBOLDocument doc) {
        
        Set<ComponentDefinition> roots = doc.getRootComponentDefinitions();
        if (roots.isEmpty())
            throw new IllegalArgumentException("No component definitions in the document");
        
        if (roots.size() > 1)
            throw new IllegalArgumentException("Can only extract one root compoenent definiotion, found: "+roots.size());
        
        return roots.iterator().next();        
    }
    
    public ComponentDefinition extractComponent(String displayId, SBOLDocument doc) {
        
        return doc.getComponentDefinitions().stream()
                .filter(c -> c.getDisplayId().equals(displayId))
                .max((c1, c2) -> c1.getVersion().compareTo(c2.getVersion()))
                .orElseThrow(() -> new IllegalArgumentException("Missing coponent: "+displayId));
    }
    
    public static void validateSbol(SBOLDocument doc) {
        validateSbol(doc, true);
    }   
    
    public static void validateSbol(SBOLDocument doc, boolean debug) {
        SBOLValidate.clearErrors();
        SBOLValidate.validateSBOL(doc, true, true, true);
        if (debug && (SBOLValidate.getNumErrors() > 0)) {
            for (String error : SBOLValidate.getErrors()) {
                System.out.println("E\t"+error);
            }            
        }        
        if (SBOLValidate.getNumErrors() > 0) {
            throw new IllegalStateException("Stoping cause of validation error: "+SBOLValidate.getErrors().get(0));
        }                
    }    
}
