/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.util.Set;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.SBOLDocument;

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
}
