/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biordm.sbol.sbol2easy.transform;

import static ed.biordm.sbol.sbol2easy.transform.CommonAnnotations.*;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.xml.namespace.QName;
import org.sbolstandard.core2.Annotation;
import org.sbolstandard.core2.Component;
import org.sbolstandard.core2.ComponentDefinition;
import org.sbolstandard.core2.Identified;
import org.sbolstandard.core2.Location;
import org.sbolstandard.core2.SBOLDocument;
import org.sbolstandard.core2.SBOLValidationException;
import org.sbolstandard.core2.SequenceAnnotation;
import org.sbolstandard.core2.SequenceConstraint;
import org.sbolstandard.core2.SequenceOntology;
import org.sbolstandard.core2.TopLevel;

/**
 *
 * @author tzielins
 */
public class SynBioTamer {
    
    //public static String SeqenceOntoPref = "http://identifiers.org/so/";
    
    public static String DEFAULT_NAMESPACE = "https://bio.ed.ac.uk/forsynbio/";
    
    GenBank2SO genBank2SO = new GenBank2SO();
    
    public SBOLDocument tameForSynBio(SBOLDocument doc) throws SBOLValidationException {
        return tameForSynBio(doc, DEFAULT_NAMESPACE,true);
    }
    
    public SBOLDocument tameForSynBio(SBOLDocument org, String newNameSpace, boolean removeCollections) throws SBOLValidationException {
    
        boolean removeAttachments = true;
        SBOLDocument cpy = new SBOLDocument();
        cpy.setDefaultURIprefix(newNameSpace);
        cpy.setComplete(false);
        
        
        cpy.createCopy(org);
        
        if (removeCollections) {
            cpy.clearCollections();
        }
        
        if (removeAttachments) { // they are remote, not sure how to deal with them as strong validation fails.
            removeAttachments(cpy);
        }
        
        renameNameSpace(cpy, newNameSpace);
        
        fixGenBankRoles(cpy);
        
        removeAnnotations(cpy, List.of(SBH_OWNED, SBH_TOPLEVEL));
        
        cpy.setComplete(org.isComplete());
        return cpy;        
    }    
    
    public void fixGenBankRoles(SBOLDocument doc) {
        
        for (ComponentDefinition def : doc.getComponentDefinitions()) {
            fixGenBankRoles(def);
        }
    }
    
    protected void fixGenBankRoles(ComponentDefinition def) {
        
        if (hasGenericRole(def.getRoles())) {
            mapGenBankFeatureToRole(def)
                .ifPresent( role -> {
                    removeGenericRole(def);
                    def.addRole(role);
                        });
        }
        def.getSequenceAnnotations().forEach( sa -> fixGenBankRoles(sa));
    }    
    
    protected void fixGenBankRoles(SequenceAnnotation def) {
        
        if (hasGenericRole(def.getRoles())) {
            mapGenBankFeatureToRole(def)
                .ifPresent( role -> {
                        try {
                            removeGenericRole(def);
                            def.addRole(role);
                        } catch (SBOLValidationException e) {
                            throw new RuntimeException(e);
                        }
                });
        }
    }
    
    
    public void removeSynBioInternals(SBOLDocument doc) {
        
        removeAnnotations(doc, List.of(SBH_OWNED, SBH_TOPLEVEL));
    }
    
    public void removeAnnotations(SBOLDocument doc, List<QName> subjects) {
       
        for (TopLevel part : doc.getTopLevels()) {
            
            removeAnnotations(part, subjects);
            
            if (part instanceof ComponentDefinition) {
                removeChildrenAnnotations((ComponentDefinition)part, subjects);
            }
        }        
    }
    
    protected void removeAnnotations(Identified part, List<QName> subjects) {
        for (QName subject: subjects) {
            Annotation ann = part.getAnnotation(subject);
            if (ann != null) {
                part.removeAnnotation(ann);
            }
        }        
    }
    
    protected void removeChildrenAnnotations(ComponentDefinition def, List<QName> subjects) {
        
        for (Component child : def.getComponents()) {
            removeAnnotations(child, subjects);
        }
        for ( SequenceAnnotation sa : def.getSequenceAnnotations()) {
            removeAnnotations(sa, subjects);
            for ( Location l : sa.getLocations()) {
                removeAnnotations(l, subjects);
            }
        }
        for ( SequenceConstraint sc : def.getSequenceConstraints()) {
            removeAnnotations(sc, subjects);
        }
    }    
    
    public void renameNameSpace(SBOLDocument doc, String newPrefix) throws SBOLValidationException {
        
        boolean complete = doc.isComplete();
        doc.setDefaultURIprefix(newPrefix);
        
        doc.setComplete(false);
        
        for (TopLevel part : doc.getTopLevels()) {
        
            doc.rename(part, newPrefix, part.getDisplayId(), part.getVersion());
        }
        
        doc.setComplete(complete);        
    }

    
    Optional<URI> mapGenBankFeatureToRole(Identified def) {
        
        return getGenBankFeature(def)
                .map(this::feature2Role);
    }
    Optional<String> getGenBankFeature(Identified def) {
        
        Annotation ann = def.getAnnotation(GB_FEATURE);
        if (ann == null) return Optional.empty();
        
        return Optional.of(ann.getStringValue());
    }
    
    URI feature2Role(String feature) {
        
        return genBank2SO.featureToTerm(feature);
    }

    boolean hasGenericRole(Set<URI> roles) {
        return roles.isEmpty() || roles.contains(SequenceOntology.SEQUENCE_FEATURE);
    }

    void removeGenericRole(ComponentDefinition def) {
        def.removeRole(SequenceOntology.SEQUENCE_FEATURE);
    }

    void removeGenericRole(SequenceAnnotation def) {
        def.removeRole(SequenceOntology.SEQUENCE_FEATURE);
    }

    void removeAttachments(SBOLDocument doc) {
        
        for (ComponentDefinition comp : doc.getComponentDefinitions()) {
            comp.clearAttachments();
        }
    }




}
