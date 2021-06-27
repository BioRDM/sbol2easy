/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.toolkit.transform;

import java.util.Iterator;
import java.util.Set;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.Range;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidate;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;

/**
 *
 * @author tzielins
 */
public class ComponentUtil {
    
    /**
     * name should be sanitized for conversion into display id as alphanumeric
     * with _ (replace all non alphanumeric characters with _)
     *
     * @param name
     * @return The sanitized string
     */
    public String sanitizeName(String name) {
        String cleanName = name.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}]", "_");
        return cleanName;
    }

    void copyMeta(ComponentDefinition src, ComponentDefinition dest) throws SBOLValidationException {
        
        dest.setDescription(src.getDescription());
        dest.setRoles(src.getRoles());
        copyAnnotations(src, dest);
        
    }
    
    
    Component createCmpCopy(Component src, ComponentDefinition dest) throws SBOLValidationException {
        
        Component copy = dest.createComponent(src.getDisplayId(), src.getAccess(), src.getDefinitionIdentity());
        copyMeta(src, copy);
        return copy;
    }
    
    void copyMeta(Component src, Component dst) throws SBOLValidationException {
        dst.setName(src.getName());
        dst.setDescription(src.getDescription());
        dst.setRoles(src.getRoles());
        dst.setWasDerivedFroms(src.getWasDerivedFroms());
        dst.setWasGeneratedBys(src.getWasGeneratedBys());
    }

    SequenceAnnotation createAnnCopy(SequenceAnnotation ann, ComponentDefinition dest, int shift) throws SBOLValidationException {
        
        Iterator<Location> locations = ann.getLocations().iterator();
        Location l = locations.next();
        if (!( l instanceof Range)) {
            throw new IllegalArgumentException("Unsupported location type: "+l.getClass().getSimpleName());
        }
        Range first = (Range)l;
        
        SequenceAnnotation cpy = dest.createSequenceAnnotation(ann.getDisplayId(), first.getDisplayId(), 
                shift+first.getStart(), shift+first.getEnd(), first.getOrientation());
        
        locations.forEachRemaining( loc -> {
            if (!( loc instanceof Range)) {
                throw new IllegalArgumentException("Unsupported location type: "+loc.getClass().getSimpleName());
            }
            try {
                Range range = (Range)loc;
                cpy.addRange(range.getDisplayId(), shift+range.getStart(), shift+range.getEnd(), range.getOrientation());
            } catch (SBOLValidationException e) {
                throw new IllegalStateException(e);
            }
        });
        
        cpy.setDescription(ann.getDescription());
        cpy.setName(ann.getName());
        cpy.setRoles(ann.getRoles());
        copyAnnotations(ann, cpy);
        
        return cpy;
    }
    
    void copyAnnotations(Identified src, Identified dest) throws SBOLValidationException {
        for (Annotation annotation : src.getAnnotations()) {
            
            Annotation newAnn = dest.createAnnotation(annotation.getQName(), "");
		if (annotation.isBooleanValue()) {
			newAnn.setBooleanValue(annotation.getBooleanValue());
		} else if (annotation.isDoubleValue()) {
			newAnn.setDoubleValue(annotation.getDoubleValue());
		} else if (annotation.isIntegerValue()) {
			newAnn.setIntegerValue(annotation.getIntegerValue());
		} else if (annotation.isStringValue()) {
			newAnn.setStringValue(annotation.getStringValue());
		} else if (annotation.isURIValue()) {
			newAnn.setURIValue(annotation.getURIValue());
		} else if (annotation.isNestedAnnotations()) {
			newAnn.setNestedQName(annotation.getNestedQName());
			newAnn.setNestedIdentity(annotation.getNestedIdentity());
			newAnn.setAnnotations(annotation.getAnnotations());
		} else {
			throw new IllegalStateException("SBol validation: sbol-12203");
		}            
        }        
    }
    
    
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
